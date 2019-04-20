## Spring Boot微信点餐（七）项目优化

### 使用MyBatis

添加依赖

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.3.2</version>
</dependency>
```

#### 基于注解开发

新建mapper包，里面存放的都是Mapper接口。

为了让Spring知道那些mapper下的接口是MyBatis-Mapper，而不是普通的接口。在SpringApplication类上加上@MapperScan注解

```java
@MapperScan(basePackages = "com.shy.wechatsell.mapper")
```

表明该包下的接口都作为Mapper，可以被Spring扫描到并注入到容器中。

或者给每个Mapper接口上都加上@Mapper注解，让Spring知道这是个Mapper文件。

```java
@Mapper
public interface ProductCategoryMapper {...｝
// 加了@MapperScan后无需再加@Mapper注解
```

完整的Mapper-Inteface如下，使用@Insert、@Select、@Update、@Delete表明sql操作的性质。

```java
package com.shy.wechatsell.mapper;

import com.shy.wechatsell.dataobject.ProductCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/31 10:06
 */
//@Mapper，加了@MapperScan后无需此注解
public interface ProductCategoryMapper {

    @Insert("insert into product_category(category_name, category_type) values (#{categoryName, jdbcType=VARCHAR}, #{categoryType, jdbcType=INTEGER})")
    int insert(ProductCategory productCategory);

    @Select("select * from product_category where category_type = #{categoryType}")
    @Results({
            @Result(column = "category_id", property = "categoryId"),
            @Result(column = "category_name", property = "categoryName"),
            @Result(column = "category_type", property = "categoryType")
    })
    ProductCategory findByCategoryType(Integer categoryType);

    @Select("select * from product_category where category_name = #{categoryName}")
    @Results({
            @Result(column = "category_id", property = "categoryId",id = true),
            @Result(column = "category_name", property = "categoryName"),
            @Result(column = "category_type", property = "categoryType")
    })
    List<ProductCategory> findByCategoryName(String categoryName);

    @Update("update product_category set category_name = #{categoryName} where category_type = #{categoryType}")
    int updateByCategoryType(@Param("categoryName") String categoryName,
                             @Param("categoryType") Integer categoryType);

    @Update("update product_category set category_name = #{categoryName} where category_type = #{categoryType}")
    int updateByObject(ProductCategory productCategory);

    @Delete("delete from product_category where category_type = #{categoryType}")
    int deleteByCategoryType(Integer categoryType);

    ProductCategory selectByCategoryType(Integer categoryType);
}

```

@Results、@Result注解作用就是指定了一个ResultMap，如下

```java
@Results({
    @Result(column = "category_id", property = "categoryId"),
    @Result(column = "category_name", property = "categoryName"),
    @Result(column = "category_type", property = "categoryType")
})
```

其xml配置的如下代码是一个意思

```xml
<resultMap id="BaseResultMap" type="com.shy.wechatsell.dataobject.ProductCategory">
    <id column="category_id" property="categoryId" jdbcType="INTEGER" />
    <result column="category_name" property="categoryName" jdbcType="VARCHAR" />
    <result column="category_type" property="categoryType" jdbcType="INTEGER" />
</resultMap>
```

可以发现上面的@Results不像xml配置一样指定了id，如果要让其他方法能引用到该ResultMap，可以添加使用id属性，如下

```java
@Results(id = "BaseResultMap",value = {
    @Result(column = "category_id", property = "categoryId"),
    @Result(column = "category_name", property = "categoryName"),
    @Result(column = "category_type", property = "categoryType")
})
```

然后在Service中就可以直接注入Mapper了，注意IDEA可能会报红，提示不能Autowired，不用管，其实是可以运行成功的。

#### 基于xml的开发

虽说不太推荐xml的方法，还是记录一下。

在resource下新建mapper文件夹，里面存放xml文件，文件名和Mapper接口名字最好一致。

在application.yml找那个配置mapper-xml的路径

```yml
mybatis:
  mapper-locations: classpath:mapper/*.xml
```

这段配置告诉Spring：类路径（resource）下mapper文件夹中的所有xml结尾的文件都是MyBatis的mapper。

用了xml就不用在Mapper接口中用注解了，如下

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.shy.wechatsell.mapper.ProductCategoryMapper" >

    <resultMap id="BaseResultMap" type="com.shy.wechatsell.dataobject.ProductCategory">
        <id column="category_id" property="categoryId" jdbcType="INTEGER" />
        <result column="category_name" property="categoryName" jdbcType="VARCHAR" />
        <result column="category_type" property="categoryType" jdbcType="INTEGER" />
    </resultMap>

    <select id="selectByCategoryType" resultMap="BaseResultMap" parameterType="java.lang.Integer">
        select category_id, category_name, category_type
        from product_category
        where category_type = #{categoryType, jdbcType=INTEGER}
    </select>
</mapper>
```

namespace处填写所对应Mapper接口的全限定名。

如果想在日志中显示详细的sql操作，可以如下配置

```yml
logging:
  level:
    com.shy.wechatsell.mapper: trace
```

表示对于mapper包，使用trace级别的日志。

### Redis分布式锁

模拟一个秒杀场景，如下面的代码。

将商品总量、商品库存、订单用一个Map来模拟了，初始化为id为123456的商品有10000件，库存10000件，订单0个。

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.service.RedisLock;
import com.shy.wechatsell.util.KeyUtil;
import com.shy.wechatsell.service.SecKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 廖师兄
 * 2017-08-06 23:18
 */
@Service
public class SecKillServiceImpl implements SecKillService {

    private static final int TIMEOUT = 10 * 1000; //超时时间 10s

    @Autowired
    private RedisLock redisLock;

    /**
     * 国庆活动，皮蛋粥特价，限量100000份
     */
    static Map<String,Integer> products;
    static Map<String,Integer> stock;
    static Map<String,String> orders;
    static
    {
        /**
         * 模拟多个表，商品信息表，库存表，秒杀成功订单表
         */
        products = new HashMap<>();
        stock = new HashMap<>();
        orders = new HashMap<>();
        products.put("12", 100000);
        stock.put("12", 100000);
    }

    private String queryMap(String productId)
    {
        return "国庆活动，皮蛋粥特价，限量份"
                + products.get(productId)
                +" 还剩：" + stock.get(productId)+" 份"
                +" 该商品成功下单用户数目："
                +  orders.size() +" 人" ;
    }

    @Override
    public String querySecKillProductInfo(String productId)
    {
        return this.queryMap(productId);
    }

    @Override
    public void orderProductMockDiffUser(String productId)
    {
        //加锁

        //1.查询该商品库存，为0则活动结束。
        int stockNum = stock.get(productId);
        if(stockNum == 0) {
            throw new SellException(100,"活动结束");
        }else {
            //2.下单(模拟不同用户openid不同)
            orders.put(KeyUtil.genUniqueKey(),productId);
            //3.减库存
            stockNum =stockNum-1;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stock.put(productId,stockNum);
        }

        //解锁

    }
}

```

Controller

```java
package com.shy.wechatsell.controller;

import com.shy.wechatsell.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 廖师兄
 * 2017-08-06 23:16
 */
@RestController
@RequestMapping("/skill")
@Slf4j
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    /**
     * 查询秒杀活动特价商品的信息
     * @param productId
     * @return
     */
    @GetMapping("/query/{productId}")
    public String query(@PathVariable String productId) {
        return secKillService.querySecKillProductInfo(productId);
    }


    /**
     * 秒杀，没有抢到获得"哎呦喂,xxxxx",抢到了会返回剩余的库存量
     * @param productId
     * @return
     */
    @GetMapping("/order/{productId}")
    public String skill(@PathVariable String productId) {
        log.info("@skill request, productId:" + productId);
        secKillService.orderProductMockDiffUser(productId);
        return secKillService.querySecKillProductInfo(productId);
    }
}

```

关键是orderProductMockDiffUser方法，秒杀的逻辑体现在其中。在高并发场景，如果这段代码不加锁，可能很多线程会同时访问到`stockNum =stockNum-1;`这行代码，因为`stockNum是存在内存中的而非外部的数据库中（数据库可以使用forUpdate加锁）。所以拿两个线程来说，A、B线程同时执行到这一行会读取到相同的stockNum=10000，A执行后库存减1得到stockNumber=9999，B下单后也减去1再次将stockNumber更新为9999，即下了两单，库存只减少了1。这被称为“超卖”。

假如这个库存还是放在内存中，可以给该方法加上synchronized关键字，实现同步，方法中的代码任何时候只有一个线程能执行，规避了超卖的问题。

但是如果应用是在分布式架构下呢，synchronized是Java关键字，存在于内存中，只是保证在单个服务器上是线程安全的。无法保证在分布式下的线程安全。

这就引出了分布式锁，这里采用Redis实现。先来了解两个Redis命令

```bash
[SETNX key value] : 将key设置值为value，如果key不存在，这种情况下等同SET命令。 当key存在时，什么也不做。SETNX是”SET if Not eXists”的简写。

[GETSET key value]: 读取并返回key的旧值，并设置key的新值
```

基于这两个命令来写一个Redis锁

```java
package com.shy.wechatsell.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by 廖师兄
 * 2017-08-07 23:55
 */
@Component
@Slf4j
public class RedisLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @param key
     * @param value 当前时间+超时时间
     * @return
     */
    public boolean lock(String key, String value) {
        if(redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }
        //currentValue=A   这两个线程的value都是B  其中一个线程拿到锁
        String currentValue = redisTemplate.opsForValue().get(key);
        //如果锁过期
        if (!StringUtils.isEmpty(currentValue)
                && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            //获取上一个锁的时间
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key, String value) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        }catch (Exception e) {
            log.error("【redis分布式锁】解锁异常, {}", e);
        }
    }

}
```

针对商品秒杀的例子，这里的key就是商品id，value是一个时间戳，用“当前时间+过期时间”表示，注意必须设置过期时间，不然会造成死锁。

下面这样简单的写法看似没有问题。但是在多线程下，假设加锁成功了，在解锁之前发生了异常造成解锁失败，那么后面的线程进如lock方法发现被设置过后都直接返回false了——即后续的线程都会加锁失败。这就产生了死锁。

```java
public boolean lock(String key, String value) {
    // SETNX命令, 设置成功返回true, 设置失败说明key不为空，被其他线程设置过了，返回false
    if(redisTemplate.opsForValue().setIfAbsent(key, value)) {
        return true;
    }
    return false;
}

```

现在再看正确的实现

```java
public boolean lock(String key, String value) {
    if(redisTemplate.opsForValue().setIfAbsent(key, value)) {
        return true;
    }
    
    // 如果线程A、B都加锁失败了；会执行到此
    // A、B读取到的，currentValue=S；且这两个线程的value都是M，其中一个线程拿到锁
    String currentValue = redisTemplate.opsForValue().get(key);
    //如果锁过期
    if (!StringUtils.isEmpty(currentValue)
        && Long.parseLong(currentValue) < System.currentTimeMillis()) {
        //获取上一个锁的时间
        String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
        if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
            return true;
        }
    }

    return false;
}
```

首先，如果后续线程加锁失败，将进入判断中，如果锁过期了，当前线程就可以获得锁并设置新值返回true，这就解决了死锁问题。

其次，下面的判断。判断过期后为什么还要进行一次判断呢？

```java
if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
    return true;
}
```

假设线程A、B都加锁失败了；会执行到此

```java
String currentValue = redisTemplate.opsForValue().get(key);
```

A、B读取到的都是currentValue=S；且这两个线程的value都是M（即他们在同一个时刻抢该商品），只有其中一个线程可以拿到锁。然后假设currentValue这个旧锁已经过期了，则会进入内层的if判断中。下面这行代码只有一个线程能执行到，假设是线程A执行到了。

```java
String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
```

GETSET获取的是旧锁的值，此时oldValue和currentValue一样，且线程A将key设置成了新值M，线程A获取锁成功将返回A，当线程B执行GETSET时，取到的是A刚才设置的值M，和currentValue不一样，所以会返回false。这一切都是由于Redis是单进程单线程的——即任何时候只有一个线程可以执行Redis命令。

然后修改秒杀方法，加上redis锁。

```java
@Override
public void orderProductMockDiffUser(String productId)
{
    //加锁
    long time = System.currentTimeMillis() + TIMEOUT;
    // 加锁失败抛出异常
    if (!redisLock.lock(productId,String.valueOf(time))) {
        throw new SellException(101, "人太多了，换个姿势试一试~");
    }
    //1.查询该商品库存，为0则活动结束。
    int stockNum = stock.get(productId);
    if(stockNum == 0) {
        throw new SellException(100,"活动结束");
    }else {
        //2.下单(模拟不同用户openid不同)
        orders.put(KeyUtil.genUniqueKey(),productId);
        //3.减库存
        stockNum =stockNum-1;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stock.put(productId,stockNum);
    }

    //解锁,该time加的锁由该time来解
    redisLock.unlock(productId, String.valueOf(time));
}
```

Redis适合做分布式锁，一个很重要的原因是：Redis是单进程单线程的。和线程同步的synchronized比，有如下优点

- Redis是外部数据库，支持分布式
- 上述的例子中key是商品id，value是时间戳。可以更细粒度的控制锁，即针对相同id的商品才加锁。而synchronized不能做到如此细化的粒度，不管任何id的商品都需要同步。



分布式锁所做的事情其实是：多台机器上多个进程对**同一个数据**进行操作的**互斥**，比如这里用相同的商品id作为key。

### Redis缓存

缓存的几个术语

- 命中：从缓存中获取数据，取到后返回
- 失效：若缓存有过期时间，到了时间就失效了
- 更新：程序将数据存到数据库中，再放回缓存中

实际场景中，先去缓存中取数据，取到就不用执行数据库的查询方法了；取不到再从数据库中查找，同时将值更新到缓存。

![](http://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-31_15-57-34.png)

在SpringApplication的类上加上注解

```java
@EnableCaching
```

就开启了缓存功能。虽然没有明确说要使用Redis作为缓存，但是application.yml中对redis做了配置，所以@Enablecaching就使用了Redis。

注意缓存的对象必须序列化，即implements Serializable，并定义一个像下面这样的serialVersionUID

```java
private static final long serialVersionUID = 5009141888601690207L;
```

接下来给方法添加注解以支持缓存。基于注解的缓存其实是AOP实现的。

- @Cacheable：Spring在调用方法前，先从缓存中查找方法的返回值。如果找到就直接返回缓存的值，不再调用该方法。否则，方法会被调用，方法的返回值会被放到缓存中。
- @Cacheput：Spring应该将返回值放到缓存中，在方法调用前并不会检查缓存，方法始终会被调用
- @CacheEvict：表明Spring应该在缓存中清除一个或者多个条目

这些注解可以用在方法上也可以作用于类，用在类上表示应用到类上的所有方法。@CacheEvict可以用在返回值是void的方法上，而其余两个必须放在有返回值的方法上。

一般给查询的方法添加@Cacheable注解，因为查询方法并不改变数据，因此可以放到缓存中，下次获取时就不用去数据库中查找了，直接从缓存中返回即可，可以节省时间和资源。比如给如下 的find方法加缓存

```java
@Override
@Cacheable(cacheNames = "product",key = "123")
public ProductInfo findOne(String productId) {
    Optional<ProductInfo> optionalProductInfo = repository.findById(productId);
    if (!optionalProductInfo.isPresent()) {
        throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
    }
    return optionalProductInfo.get();
}
```

第一次获取执行该方法时，会将返回值ProductInfo放到缓存，以后再访问该方法就不会执行该方法了，直接从缓存中获得。参数key是在缓存product中的键，值是productInfo。

**如果不指定key，将使用方法参数的值作为key**。

这里有个问题，此时我们查询其他productId的商品，也会调用这个函数，命中缓存了，返回了却不是本商品的productInfo。有没有什么办法呢?

可以使用SpEL表达式自定义key

```java
@Cacheable(cacheNames = "product",key = "#productId")
public ProductInfo findOne(String productId) {...}
```

这样就引用了方法参数的productId作为key。

可以通过`#bean-id`、`#该方法上的参数名`来自定义key。

Spring还提供了多个用来定义缓存规则的SpEL扩展。

- #root.args ：传递给缓存方法的参数，形式是数组
- #root.caches：该方法执行所对应的缓存，形式是数组
- #root.target：目标对象
- #root.targetClass：目标对象对用的Class类，是#root.target.class的简写
- #root.method：缓存方法
- #root .methodName：缓存方法的名字，是#root.method.name的简写
- #result：方法的返回值，不能用在@Cacheable上
- #Argument：任意的方法参数名，如上面的#productId或者参数索引如#p0或#a0

现在还有问题，我们查询时将值缓存了，万一该值更新了呢？下次查询依然返回的是缓存中的旧值。所以应该在每次更新数据时，将缓存中的旧值也更新。这样每次查询得到的都是最新值了。如下面的保存/更新方法，我们使用了@CachePut，每次更新都会将值放到缓存中。key中使用了#result.productId和上面的#productId一一对应。

```java
@Override
@CachePut(cacheNames = "product",key = "#result.productId")
public ProductInfo save(ProductInfo productInfo) {
    return repository.save(productInfo);
}
```

还有种策略是使用@CacheEvict，每次保存/更新就清空缓存，这样下次查询时会从数据库中查得，同时放进缓存，获取到的也是最新值。@CachePut和@Cacheable搭配适合方法返回值一致的情况，而@CacheEvict任何方法上都可以使用，当返回值不一致时，可以使用@CacheEvict + @Cacheable的策略。

@Cacheable和@Cacheput有一些共有的属性

- value或者cacheName：要使用的缓存名字
- condition: SpEl表达式，如果得到的值是false，不会将缓存应用到方法上
- unless：SpEl表达式，用来计算自定义的缓存Key，如果得到的值是true，不会将返回值放到缓存中。和condition不同，unless是在方法被调用后对该表达式求值，因此可以获取到方法的返回结果#result。如果在condition中使用了，得到的#result将是null。
- key：SpEl表达式，用来自定义的缓存key

举个例子，该注解限定了openid的长度大于10，缓存才能作用于方法，而且当返回值的code不是0，就不放入缓存中，即只有当code=0（表示请求成功），才进行缓存，并且缓存的名字是order，key是参数openid的值。很多时候都会这样使用，毕竟请求失败的数据是没有必要放到缓存中的。

```java
@Cacheable(cacheNames = "order",key="#openid", condition = "#openid.length() > 1", unless = "#result.getCode() != 0")
public ResultVO<List<OrderDetail>> list(@RequestParam("openid") String openid,
                                            @RequestParam(value = "page",defaultValue = "0") Integer page,
                                            @RequestParam(value = "size",defaultValue = "10") Integer size) {...}
```


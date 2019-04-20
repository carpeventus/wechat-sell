## Spring Boot微信点餐（二）

pom中加入相关依赖，数据库使用MySQL+JPA

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

依赖中还使用了lombok，主要是为了简化代码。

application.yml配置datasource

```yml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    username: root
    password: 123456
    url: jdbc:mysql://10.175.55.182:3306/sell?characterEncoding=utf-8&useSSL=false

  jpa:
    show-sql: true
```

### 商品类目

先定义商品类目实体类，该类对应了数据库中的product_category表

```java
package com.shy.wechatsell.dataobject;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * 类目
 * @author Haiyu
 * @date 2018/10/15 9:40
 */
@Entity
@Data
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    /** 类目名字 */
    private String categoryName;
    /** 类目编号 */
    private Integer categoryType;
    /** 创建日期 */
    private Date createTime;
    /** 更新日期 */
    private Date updateTime;
}
```

@Data是lombok的注解，自动为类生成getter/setter、toString()、equals()、hashCode()方法

@GeneratedValue自增策略，使用MySQL，这里策略一定要明确指定`strategy = GenerationType.IDENTITY`，不指定默认是AUTO，但是发现插入时主键并没有自增...

再写一个类继承JpaRepository就可以实现简单的sql操作了。

```java
package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Haiyu
 * @date 2018/10/15 9:48
 */
public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Integer> {
}

```

泛型中，第一个参数是实体Entity的类型，第二个是Entity类中ID的类型。

来测试下插入、更新、查询

```java
package com.shy.wechatsell;

import com.shy.wechatsell.dataobject.ProductCategory;
import com.shy.wechatsell.repository.ProductCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Haiyu
 * @date 2018/10/15 9:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ProductCategoryTest {
    @Autowired
    private ProductCategoryRepository repository;
    @Test
    public void saveTest() {
        ProductCategory category = new ProductCategory();
        category.setCategoryName("女生最爱");
        category.setCategoryType(3);
        repository.save(category);
    }

    @Test
    public void updateTest() {
        ProductCategory category = repository.findById(2).get();
        category.setCategoryName("促销");
        repository.save(category);
    }

    @Test
    public void findOneTest() {
        ProductCategory category = repository.findById(1).get();
        log.info(category.toString());
    }
}

```

注意在updateTest()中，我们是先查询得到一个对象后再set的（而不是new出一个对象来set），由于查询得到的对象本来就有一个日期（上次更新的时间），如果不对日期进行set，该日期在本次更新中会原封不动的被设置回去了，导致的结果是更新后update_time字段并没有被更新。究其原因，是因为在insert和update中默认对所有字段都进行插入或更新，如果想要有选择性地只针对我们set的字段进行更新，可以使用如下两个注解：

- **@DynamicInsert：**设置为true,表示insert对象的时候，生成动态的insert语句，只插入被set了的字段；其余字段不set默认是null就不会加入到insert语句当中。该属性默认false。比如希望数据库插入日期或时间戳字段时，在对象字段的为空的情况下，该字段能自动填入系统当前时间。

- **@DynamicUpdate**：设置为true，表示update对象的时候，生成动态的update语句，只更新被set了的字段，其他字段不会被更新，默认false。比如只想更新某个属性，但是却把整个对象的属性都更新了，这并不是我们希望的结果，如果要实现更改了哪些字段，就只更新哪些字段就使用该注解。

在ProductCategory实体类上加上@DynamicUpdate注解，即可实现在update时只更新被set了的字段，update_time字段如果不set，那么将使用数据库默认的值，这里update_time在数据库中被设置了`ON UPDATE CURRENT_TIMESTAMP`，那么本次更新后update_time将被更新为当前系统时间。 

如果我们想实现：测试中产生的任何数据都不写入到数据库中，可以在测试方法上添加@Transactional注解，该注解在service层上使用时当抛出异常时才回滚，而用在测试环境中，不管结果如何都是会回滚的。

```java
@Test
@Transactional
public void saveTest() {
    ProductCategory category = new ProductCategory();
    category.setCategoryName("男生最爱");
    category.setCategoryType(4);
    ProductCategory result = repository.save(category);
    Assert.assertNotNull(result); // 只要插入成功，result就不为null
}
```

然后写service接口

```java
package com.shy.wechatsell.service;

import com.shy.wechatsell.dataobject.ProductCategory;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 13:20
 */
public interface CategoryService {
    /**
     * 查找所有类目
     * @return
     */
    List<ProductCategory> findAll();

    /**
     * 根据id查找类目
     * @param id
     * @return
     */
    ProductCategory findOne(Integer id);

    /**
     * 根据传入的类目编号查询
     * @param categoryList
     * @return
     */
    List<ProductCategory> findByCategoryTypes(List<Integer> categoryList);

    /**
     * 保存一个类目
     * @param category
     * @return
     */
    ProductCategory save(ProductCategory category);

}

```

然后是实现类

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.ProductCategory;
import com.shy.wechatsell.repository.ProductCategoryRepository;
import com.shy.wechatsell.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 13:25
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private ProductCategoryRepository repository;
    @Override
    public List<ProductCategory> findAll() {
        return repository.findAll();
    }

    @Override
    public ProductCategory findOne(Integer id) {
        return repository.findById(id).get();
    }

    @Override
    public List<ProductCategory> findByCategoryTypes(List<Integer> categoryList) {
        return repository.findByCategoryTypeIn(categoryList);
    }

    @Override
    public ProductCategory save(ProductCategory category) {
        return repository.save(category);
    }
}

```

写一个单元测试，测试各个方法

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.ProductCategory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 13:29
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class CategoryServiceImplTest {
    @Autowired
    private CategoryServiceImpl categoryService;

    @Test
    public void findAll() {
        List<ProductCategory> categories = categoryService.findAll();
        Assert.assertNotEquals(0, categories.size());
    }

    @Test
    public void findOne() {
        ProductCategory result = categoryService.findOne(1);
        Assert.assertEquals(new Integer(1),result.getCategoryId());
    }

    @Test
    public void findByCategoryTypes() {
        List<ProductCategory> categories = categoryService.findByCategoryTypes(Arrays.asList(1, 2, 3));
        Assert.assertNotEquals(0, categories.size());
    }

    @Test
    public void save() {
        ProductCategory category = new ProductCategory();
        category.setCategoryName("女生专享");
        category.setCategoryType(10);
        ProductCategory result = categoryService.save(category);
        Assert.assertNotNull(result);
    }
}
```

### 商品信息

```java
package com.shy.wechatsell.dataobject;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author Haiyu
 * @date 2018/10/15 16:29
 */
@Data
@Entity
@DynamicInsert
public class ProductInfo {
    @Id
    private String productId;

    /** 商品名字 */
    private String productName;

    /** 商品价格 */
    private BigDecimal productPrice;

    /** 商品库存 */
    private Integer productStock;

    /** 商品描述 */
    private String productDescription;

    /** 商品小图 */
    private String productIcon;

    /** 商品状态，0上架、1下架 */
    private Integer productStatus;

    /** 商品类目 */
    private Integer categoryType;
}

```

Repository

```java
package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 16:56
 */
public interface ProductInfoRepository extends JpaRepository<ProductInfo, String> {
    /**
     * 按照商品的状态查询
     * @param productStatus
     * @return
     */
    List<ProductInfo> findByProductStatus(Integer productStatus);
}

```

Service

```java
package com.shy.wechatsell.service;

import com.shy.wechatsell.dataobject.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 17:28
 */
public interface ProductService {
    /**
     * 根据id查找某一个商品
     * @param productId
     * @return
     */
    ProductInfo findOne(String productId);

    /**
     * 查找所有上架的商品
     * @return
     */
    List<ProductInfo> findUpAll();

    /**
     * 查找所有商品
     * @param pageable
     * @return
     */
    Page<ProductInfo> findAll(Pageable pageable);

    /**
     * 保存商品
     * @param productInfo
     * @return
     */
    ProductInfo save(ProductInfo productInfo);

    // TODO 加库存
    // TODO 减库存
}

```

ServiceImpl

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.enums.ProductStatusEnum;
import com.shy.wechatsell.repository.ProductInfoRepository;
import com.shy.wechatsell.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 17:35
 */
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductInfoRepository repository;

    @Override
    public ProductInfo findOne(String productId) {
        return repository.findById(productId).get();
    }

    @Override
    public List<ProductInfo> findUpAll() {
        return repository.findByProductStatus(ProductStatusEnum.UP.getCode());
    }

    @Override
    public Page<ProductInfo> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public ProductInfo save(ProductInfo productInfo) {
        return repository.save(productInfo);
    }
}

```

商品的状态：上架还是下架用了枚举，0表示上架、1表示下架。

```java
package com.shy.wechatsell.enums;

import lombok.Getter;

/**
 * @author Haiyu
 * @date 2018/10/15 17:39
 */
@Getter
public enum ProductStatusEnum {
    /**
     * 商品上架的状态码
     */
    UP(0,"上架"),
    /**
     * 商品下架的状态码
     */
    DOWN(1,"下架")
    ;

    private int code;
    private String message;

    ProductStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

```

针对ProductServiceImpl写个测试类

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.ProductInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 18:03
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductServiceImplTest {
    @Autowired
    private ProductServiceImpl productService;
    @Test
    public void findOne() {
        ProductInfo productInfo = productService.findOne("12");
        Assert.assertEquals("12", productInfo.getProductId());
    }

    @Test
    public void findUpAll() {
        List<ProductInfo> productInfos = productService.findUpAll();
        Assert.assertNotEquals(0, productInfos.size());
    }

    @Test
    public void findAll() {
        // PageRequest.of(0, 2);按照size=2划分page，返回第0页的数据
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<ProductInfo> productInfos = productService.findAll(pageRequest);
        // 获取所有元素的个数
        System.out.println(productInfos.getTotalElements());
        // 按照size划分为一页，总共分成了多少页
        System.out.println(productInfos.getTotalPages());
        // 获取当前Slice所在的index
        System.out.println(productInfos.getNumber());
        // 获取当前Slice包含的元素个数
        System.out.println(productInfos.getNumberOfElements());
        // 获取PageRequest的size大小
        System.out.println(productInfos.getSize());
        // 获取请求当前Slice的Pageable对象，在这里就是PageRequest
        System.out.println(productInfos.getPageable());
        // 获取查询到的数据
        System.out.println(productInfos.getContent());
    }

    @Test
    public void save() {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId("14");
        productInfo.setProductName("秘制凉皮");
        productInfo.setProductPrice(new BigDecimal(9));
        productInfo.setProductDescription("秘制的好吃的");
        productInfo.setProductIcon("http://image.test2.jpg");
        productInfo.setCategoryType(1);
        productInfo.setProductStock(100);
        ProductInfo result = productService.save(productInfo);
        Assert.assertNotNull(result);
    }
}
```

主要看findall()方法，需要传入一个Pageable对象，PageRequest是Pageable的一个实现类，`PageRequst.of(in page, int size)`有两个重要的参数

- page，第几页，从0开始
- size，按照size的大小分页
- page = 1，size = 10；表示每10个元素为一页。这个参数组合的表示：第1页的 10个数据（从0开始，所以按照习惯来说其实是第二页的10元素）

### 买家商品API

前端代码在虚拟机中已经写好，我们只需返回数据即可。要在前端展示出所有上架的商品，需要访问`/sell/buyer/product/list`，这是别人写好的，方法名一定要对应上。

需要返回给前端的数据格式如下

```json
{
    "code": 0,
    "msg": "成功",
    "data": [
        {
            "name": "热榜",
            "type": 1,
            "foods": [
                {
                    "id": "123456",
                    "name": "皮蛋粥",
                    "price": 1.2,
                    "description": "好吃的皮蛋粥",
                    "icon": "http://xxx.com",
                }
            ]
        },
        {
            "name": "好吃的",
            "type": 2,
            "foods": [
                {
                    "id": "123457",
                    "name": "慕斯蛋糕",
                    "price": 10.9,
                    "description": "美味爽口",
                    "icon": "http://xxx.com",
                }
            ]
        }
    ]
}
```

最外层的code、msg、data由ResultVO封装

```java
package com.shy.wechatsell.vo;

import lombok.Data;


/**
 * HTTP请求返回的最外层对象
 *
 * @author Haiyu
 * @date 2018/10/16 9:13
 */
@Data
public class ResultVO<T> {
    /** 状态码 */
    private Integer code;
    /** 状态信息 */
    private String msg;
    /** 数据 */
    private T data;
}

```

data中的name、type、foods由ProductVO封装

```java
package com.shy.wechatsell.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 商品-包含类目
 *
 * @author Haiyu
 * @date 2018/10/16 9:16
 */
@Data
public class ProductVO {
    @JsonProperty("name")
    private String categoryName;

    @JsonProperty("type")
    private Integer categoryType;

    @JsonProperty("foods")
    private List<ProductInfoVO> productInfoVOList;
}

```

为了和前端中的属性名保持一致，使用了@JsonProperty，可以把一个属性序列化为另外一个名称，如

```java
@JsonProperty("name")
private String categoryName;
```

把categoryName序列化成name，这样前端收到的JSONObject的属性名就变成了name。同时该注解也可以进行反序列化，将JSONObject中的name属性值映射到categoryName中。

foods中的属性封装在ProductInfoVO中

```java
package com.shy.wechatsell.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品详情（出于安全考虑，不直接使用ProductInfo）
 *
 * @author Haiyu
 * @date 2018/10/16 9:20
 */
@Data
public class ProductInfoVO {
    @JsonProperty("id")
    private String productId;

    @JsonProperty("name")
    private String productName;

    @JsonProperty("price")
    private BigDecimal productPrice;

    @JsonProperty("description")
    private String productDescription;

    @JsonProperty("icon")
    private String productIcon;
}

```

针对不同状态码code，需要返回不同的ResultVO

- code=0，成功
- code=1，失败
- 成功时，data也可能为null

为此写一个工具类，如下

```java
package com.shy.wechatsell.util;

import com.shy.wechatsell.vo.ResultVO;

/**
 * @author Haiyu
 * @date 2018/10/16 10:42
 */
public class ResultVOUtil {
    public static ResultVO success(Object object) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        resultVO.setData(object);
        return resultVO;
    }

    public static ResultVO success() {
        return success(null);
    }

    public static ResultVO error() {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("失败");
        return resultVO;
    }
}

```

下面是买家端商品列表的Controller，主要逻辑是将所有上架的商品归类到对应的类目下。

```java
package com.shy.wechatsell.controller;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.shy.wechatsell.dataobject.ProductCategory;
import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.service.CategoryService;
import com.shy.wechatsell.service.ProductService;
import com.shy.wechatsell.vo.ProductInfoVO;
import com.shy.wechatsell.vo.ProductVO;
import com.shy.wechatsell.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.ResultVOUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Haiyu
 * @date 2018/10/16 9:11
 */
@RestController
@RequestMapping("/buyer/product")
public class BuyerProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public ResultVO list() {
        // 1. 查询所有已上架的商品
        List<ProductInfo> productInfoList = productService.findUpAll();
        // 已上架的商品类目编号
        List<Integer> categoryTypeList = productInfoList.stream()
                .map(ProductInfo::getCategoryType)
                .collect(Collectors.toList());
        // 2.上架商品的所有类目
        List<ProductCategory> productCategories = categoryService.findByCategoryTypes(categoryTypeList);

        // 3.数据封装
        List<ProductVO> productVOList = new ArrayList<>();
        for (ProductCategory productCategory : productCategories) {
            ProductVO productVO = new ProductVO();
            productVO.setCategoryName(productCategory.getCategoryName());
            productVO.setCategoryType(productCategory.getCategoryType());

            List<ProductInfoVO> productInfoVOList = new ArrayList<>();
            for (ProductInfo productInfo : productInfoList) {
                // 商品的类目和当前类目一样，才把该商品展示在当前类目下
                if (productInfo.getCategoryType().equals(productCategory.getCategoryType())) {
                    ProductInfoVO productInfoVO = new ProductInfoVO();
                    // 只会拷贝匹配的属性（属性名相同）
                    BeanUtils.copyProperties(productInfo,productInfoVO);
                    productInfoVOList.add(productInfoVO);
                }
            }
            productVO.setProductInfoVOList(productInfoVOList);
            productVOList.add(productVO);
        }
        return ResultVOUtil.success(productVOList);
    }
}

```

虚拟机中使用了nginx作为服务器，可以将server_name设置成自己喜欢的域名。虚拟机的IP地址为10.175.55.182，同时将proxy_pass中的ip改成本机的ip，为了让浏览器可以解析到自=定义的域名，在hosts文件中添加如下一行

```
10.175.55.182	sunhaiyu.me
```

浏览器输入sunhaiyu.me会被转发到本机来处理。

### 订单主表

订单有如下几个状态

```java
package com.shy.wechatsell.enums;

import lombok.Getter;

/**
 * @author Haiyu
 * @date 2018/10/16 12:41
 */
@Getter
public enum OrderStatusEnum {
    /**
     * 新下的订单
     */
    NEW(0, "新订单"),
    /**
     * 订单已经完成
     */
    FINISHED(1, "已完成"),
    /**
     * 订单被取消
     */
    CANCEL(0, "取消");

    private Integer code;
    private String msg;

    OrderStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

```

订单的支付状态如下

```java
package com.shy.wechatsell.enums;

import lombok.Getter;

/**
 * @author Haiyu
 * @date 2018/10/16 12:44
 */
@Getter
public enum PayStatusEnum {
    /**
     * 未支付
     */
    WAIT(0,"等待支付"),
    /**
     * 支付成功
     */
    SUCCESS(1, "支付成功")
    ;

    private Integer code;
    private String msg;

    PayStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

```

订单主表，默认订单状态是“新订单”，支付状态默认“等待支付”。

```java
package com.shy.wechatsell.dataobject;

import com.shy.wechatsell.enums.OrderStatusEnum;
import com.shy.wechatsell.enums.PayStatusEnum;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Haiyu
 * @date 2018/10/16 12:33
 */
@Data
@Entity
@DynamicUpdate
public class OrderMaster {
    @Id
    private String orderId;

    /** 买家姓名 */
    private String buyerName;

    /** 买家电话 */
    private String buyerPhone;

    /** 买家地址 */
    private String buyerAddress;

    /** 买家微信openid */
    private String buyerOpenid;

    /** 订单总金额 */
    private BigDecimal orderAmount;

    /** 订单状态，默认0是新下单 */
    private Integer orderStatus = OrderStatusEnum.NEW.getCode();

    /** 订单支付状态，默认未支付 */
    private Integer payStatus = PayStatusEnum.WAIT.getCode();

    /** 订单创建时间 */
    private Date createTime;

    /** 订单更新时间 */
    private Date updateTime;

}

```

### 订单详情

然后是订单详情，一个订单下可能包含多件商品（订单详情），是一对多的关系。

```java
package com.shy.wechatsell.dataobject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author Haiyu
 * @date 2018/10/16 12:54
 */
@Data
@Entity
public class OrderDetail {
    @Id
    private String detailId;

    /** 所在订单号 */
    private String orderId;

    /** 商品id */
    private String productId;

    /** 商品名字 */
    private String productName;

    /** 商品价格 */
    private BigDecimal productPrice;

    /** 商品数量 */
    private Integer productQuantity;

    /** 商品小图 */
    private String productIcon;
}

```

写对应的Repository

```java
package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.OrderMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Haiyu
 * @date 2018/10/16 13:06
 */
public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {
    /**
     * 根据微信openid查找某个用户的所有订单
     *
     * @param buyerOpenid 买家微信openid
     * @param pageable
     * @return 分页
     */
    Page<OrderMaster> findByBuyerOpenid(String buyerOpenid, Pageable pageable);
}

```



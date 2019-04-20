## Spring Boot微信点餐（三）

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
    CANCEL(2, "取消");

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

订单service

```java
package com.shy.wechatsell.service;

import com.shy.wechatsell.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Haiyu
 * @date 2018/10/17 9:39
 */
public interface OrderService {
    /** 创建订单 */
    OrderDTO create(OrderDTO orderDTO);

    /** 查询单个订单 */
    OrderDTO findOne(String orderId);

    /** 查询订单列表 */
    Page<OrderDTO> findList(String buyerOpenid, Pageable pageable);

    /** 取消订单 */
    OrderDTO cancel(OrderDTO orderDTO);

    /** 完成订单 */
    OrderDTO finish(OrderDTO orderDTO);

    /** 支付订单 */
    OrderDTO paid(OrderDTO orderDTO);
}

```

#### 订单的创建

订单service的实现类中，先来写create方法。

创建订单，会被nixnx转发到`POST /sell/buyer/order/create`，前台传来的数据如下

```java
name: "张三"
phone: "18868822111"
address: "慕课网总部"
openid: "ew3euwhd7sjw9diwkq" //用户的微信openid
items: [{
    productId: "1423113435324",
    productQuantity: 2 //购买数量
}]
```

根据这些参数，要构造一个订单实体，一个订单有多个订单详情，为了保持实体类和数据库表的字段一一对应，单独用一个OrderDTO来包装订单对象。和OrderMaster比，多了一个`List<OrderDetail> orderDetailList;`

```java
package com.shy.wechatsell.dto;

import com.shy.wechatsell.dataobject.OrderDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/17 9:37
 */
@Data
public class OrderDTO {
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
    private Integer orderStatus;

    /** 订单支付状态，默认未支付 */
    private Integer payStatus;

    /** 订单创建时间 */
    private Date createTime;

    /** 订单更新时间 */
    private Date updateTime;

    /** 一个订单对应的多个订单详情 */
    private List<OrderDetail> orderDetailList;

}

```

前台不能传单价过来，从数据库的prodcut_info获取单价，和订单详情里面的商品数量相乘并累加起来，就可以得到订单的总价。

订单号和订单详情号的生成，使用了时间戳+随机值的方法生成。

```java
package com.shy.wechatsell.util;

import java.util.Random;

/**
 * @author Haiyu
 * @date 2018/10/17 10:14
 */
public class KeyUtil {
    /**
     * 生成唯一主键
     * 格式：时间+随机数
     * 为了防止多线程下产生相同的key，就不能保证key的唯一性，所以加上同步
     * @return
     */
    public static synchronized String genUniqueKey() {
        Random random = new Random();
        // 100000~999999,六位随机数，不能使用random.nextInt(1000000)，这样不能保证一定是6位
        Integer number = random.nextInt(900000) + 100000;
        return System.currentTimeMillis() + String.valueOf(number);
    }
}

```

在创建订单的方法中，订单和订单详情都要写入数据库中。具体逻辑如下：

- 从订单详情中，根据product_id获取商品信息，如果商品信息为null，说明该商品不存在
- 计算订单详情中一件商品的价格，单价*数量；遍历所有订单详情，累加得到订单的总价
- 将订单详情写入数据库中
- 将订单写入数据库中
- 减去相应商品的库存

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.OrderDetail;
import com.shy.wechatsell.dataobject.OrderMaster;
import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.dto.CartDTO;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.OrderStatusEnum;
import com.shy.wechatsell.enums.PayStatusEnum;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.repository.OrderDetailRepository;
import com.shy.wechatsell.repository.OrderMasterRepository;
import com.shy.wechatsell.service.OrderService;
import com.shy.wechatsell.service.ProductService;
import com.shy.wechatsell.util.KeyUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Haiyu
 * @date 2018/10/17 9:44
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderMasterRepository orderMasterRepository;
    
    @Override
    @Transactional
    public OrderDTO create(OrderDTO orderDTO) {
        // 下单的那一刻就会产生一条订单号
        String orderId = KeyUtil.genUniqueKey();
        // 总价
        BigDecimal orderAmount = new BigDecimal(BigInteger.ZERO);
        // 1.查询商品（数量和价格）
        for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
            ProductInfo productInfo = productService.findOne(orderDetail.getProductId());
            if (productInfo == null) {
                throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
            }
            // 2.计算一件商品的总价，遍历所有商品累加得到订单总价
            orderAmount = productInfo.getProductPrice()
                    .multiply(new BigDecimal(orderDetail.getProductQuantity()))
                    .add(orderAmount);
            // 3.写入订单详情（order_detail）
            // 注意一定要先拷贝属性，再设置，不然拷贝时会将set的值覆盖
            BeanUtils.copyProperties(productInfo, orderDetail);
            orderDetail.setOrderId(orderId);
            orderDetail.setDetailId(KeyUtil.genUniqueKey());
            orderDetailRepository.save(orderDetail);
        }

        // 3.写入订单数据（order_master）
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(orderId);
         BeanUtils.copyProperties(orderDTO, orderMaster);
        orderMaster.setOrderAmount(orderAmount);
        // 属性拷贝后，将默认值覆盖了，所有要重新设置
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());
        // 4.减库存
        // 购物车
        orderMasterRepository.save(orderMaster);
        List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream()
                .map(e -> new CartDTO(e.getProductId(),e.getProductQuantity()))
                .collect(Collectors.toList());
        productService.decreaseStock(cartDTOList);
        return orderDTO;
    }

}

```

前端中传过来的items是一个列表，列表中每个元素只有商品id和购买数量，据此封装成一个CarDTO。`List<CartDTO>`就代表了items，可以理解成购物车。

改变库存是ProductService的事，在其中新增方法decreaseStock和increaseStock。

```java
@Override
public void decreaseStock(List<CartDTO> cartDTOList) {
    for (CartDTO cartDTO : cartDTOList) {
        ProductInfo productInfo = repository.findById(cartDTO.getProductId()).get();
        Integer result = productInfo.getProductStock() - cartDTO.getProductQuantity();
        if (result < 0) {
            throw new SellException(ResultEnum.PRODUCT_STOCK_ERROR);
        }
        productInfo.setProductStock(result);
        repository.save(productInfo);
    }
}
```

#### 订单的查询

- 根据订单号查询单条记录
- 根据微信openid查询某用户所有订单

```java
@Override
public OrderDTO findOne(String orderId) {
    Optional<OrderMaster> optionalOrderMaster = orderMasterRepository.findById(orderId);
    if (!optionalOrderMaster.isPresent()) {
        throw new SellException(ResultEnum.ORDER_NOT_EXIST);
    }
    OrderMaster orderMaster = optionalOrderMaster.get();
    List<OrderDetail> orderDetailList = orderDetailRepository.findByOrderId(orderId);
    if (CollectionUtils.isEmpty(orderDetailList)) {
        throw new SellException(ResultEnum.ORDER_DETAIL_NOT_EXIST);
    }
    OrderDTO orderDTO = new OrderDTO();
    BeanUtils.copyProperties(orderMaster, orderDTO);
    orderDTO.setOrderDetailList(orderDetailList);
    return orderDTO;
}

@Override
public Page<OrderDTO> findList(String buyerOpenid, Pageable pageable) {
    Page<OrderMaster> orderMasterPage = orderMasterRepository.findByBuyerOpenid(buyerOpenid,pageable);
    List<OrderDTO> orderDTOList = OrderMaster2OrderDTOConverter.convertList(orderMasterPage.getContent());
    return new PageImpl<>(orderDTOList,pageable,orderMasterPage.getTotalElements());
}
```

#### 订单的取消

- 只有新订单才能被取消
- 修改订单状态
- 退还库存
- 退款

```java
@Override
@Transactional
public OrderDTO cancel(OrderDTO orderDTO) {
    OrderMaster orderMaster = new OrderMaster();
    // 1.判断订单状态，必须是新下的订单才能被取消（已完成和已取消状态下不能操作）
    if (!OrderStatusEnum.NEW.getCode().equals(orderDTO.getOrderStatus())) {
        log.error("【取消订单】订单状态不正确，orderId={};orderStatus={}",orderDTO.getOrderId(),orderDTO.getOrderStatus());
        throw new SellException(ResultEnum.ORDER_STATUS_ERROR);
    }

    // 2.修改订单状态，先改变DTO的状态，然后再拷贝属性，保证orderMaster和orderDTO的状态都改变了
    orderDTO.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
    BeanUtils.copyProperties(orderDTO, orderMaster);
    OrderMaster result = orderMasterRepository.save(orderMaster);
    if (result == null) {
        log.error("【取消订单】更新失败,orderMaster={}",orderMaster);
        throw new SellException(ResultEnum.ORDER_UPDATE_FAIL);
    }

    // 3.返还库存（增加库存）
    if (CollectionUtils.isEmpty(orderDTO.getOrderDetailList())) {
        log.error("【取消订单】订单中无商品详情,orderMaster={}",orderMaster);
        throw new SellException(ResultEnum.ORDER_DETAIL_NOT_EXIST);
    }
    List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream()
        .map(e -> new CartDTO(e.getProductId(), e.getProductQuantity()))
        .collect(Collectors.toList());
    productService.increaseStock(cartDTOList);
    // 4.如果已经付款，需要退款
    if (PayStatusEnum.SUCCESS.getCode().equals(orderDTO.getPayStatus())) {
        // TODO 退款逻辑
    }
    return orderDTO;
}
```

#### 订单的支付

- 只有新订单可以支付
- 只有未支付状态下可以被支付
- 修改支付状态

```java
@Override
@Transactional
public OrderDTO paid(OrderDTO orderDTO) {
    // 判断订单状态
    if (!OrderStatusEnum.NEW.getCode().equals(orderDTO.getOrderStatus())) {
        log.error("【订单支付完成】订单状态不正确，orderId={};orderStatus={}",orderDTO.getOrderId(),orderDTO.getOrderStatus());
        throw new SellException(ResultEnum.ORDER_STATUS_ERROR);
    }
    // 判断支付状态
    if (!PayStatusEnum.WAIT.getCode().equals(orderDTO.getPayStatus())) {
        log.error("【订单支付完成】支付状态不正确，orderDTO={}",orderDTO);
        throw new SellException(ResultEnum.ORDER_PAY_STATUS_ERROR);
    }
    // 修改支付状态
    orderDTO.setPayStatus(PayStatusEnum.SUCCESS.getCode());
    OrderMaster orderMaster = new OrderMaster();
    BeanUtils.copyProperties(orderDTO, orderMaster);
    OrderMaster result = orderMasterRepository.save(orderMaster);
    if (result == null) {
        log.error("【订单支付完成】更新失败,orderMaster={}",orderMaster);
        throw new SellException(ResultEnum.ORDER_UPDATE_FAIL);
    }
    return orderDTO;
}
```

#### 订单的完成

- 只有新订单可以被完成
- 修改订单状态

```java
@Override
public OrderDTO finish(OrderDTO orderDTO) {
    // 只有在新新单状态下才能被改成已完成
    if (!OrderStatusEnum.NEW.getCode().equals(orderDTO.getOrderStatus())) {
        log.error("【完成订单】订单状态不正确，orderId={};orderStatus={}",orderDTO.getOrderId(),orderDTO.getOrderStatus());
        throw new SellException(ResultEnum.ORDER_STATUS_ERROR);
    }
    orderDTO.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
    OrderMaster orderMaster = new OrderMaster();
    BeanUtils.copyProperties(orderDTO, orderMaster);
    OrderMaster result = orderMasterRepository.save(orderMaster);
    if (result == null) {
        log.error("【完成订单】更新失败,orderMaster={}",orderMaster);
        throw new SellException(ResultEnum.ORDER_UPDATE_FAIL);
    }
    return orderDTO;
}
```

### 订单相关API-Controller的编写

#### 订单创建

前端的请求被转发到以下地址

```html
POST /sell/buyer/order/create
```

前端传过来的参数如下

```json
name: "张三"
phone: "18868822111"
address: "慕课网总部"
openid: "ew3euwhd7sjw9diwkq" //用户的微信openid
items: [{
    productId: "1423113435324",
    productQuantity: 2 //购买数量
}]
```

返回给前端的数据是这样的

```json
{
  "code": 0,
  "msg": "成功",
  "data": {
      "orderId": "147283992738221" 
  }
}
```

使用一个实体类来表示表示以上表单数据，同时使用了@Valid验证了表单

```java
package com.shy.wechatsell.form;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 为了进行数据绑定，属性名和表单中传过来的字段名保持一致
 * @author Haiyu
 * @date 2018/10/19 8:57
 */
@Data
public class OrderForm {
    /** 用户名 */
    @NotEmpty(message = "用户名必填")
    private String name;

    /** 用户手机 */
    @NotEmpty(message = "手机号必填")
    private String phone;

    /** 微信的openid */
    @NotEmpty(message = "微信openid必填")
    private String openid;

    /** 用户地址 */
    @NotEmpty(message = "用户地址必填")
    private String address;

    /** 购物车 */
    @NotEmpty(message = "购物车不能为空")
    private String items;
}

```

controller中create方法的逻辑如下

```java
@PostMapping("/create")
public ResultVO<Map<String,String>> create(@Valid OrderForm orderForm, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        throw new SellException(ResultEnum.ORDER_PARAM_ERROR.getCode(),bindingResult.getFieldError().getDefaultMessage());
    }
    OrderDTO orderDTO = OrderForm2OrderDTO.convert(orderForm);
    if (CollectionUtils.isEmpty(orderDTO.getOrderDetailList())) {
        log.error("【创建订单】购物车不能为空");
        throw new SellException(ResultEnum.ORDER_CART_EMPTY);
    }
    OrderDTO orderDTOResult = orderService.create(orderDTO);
    Map<String,String> data = new HashMap<>(1);
    data.put("orderId", orderDTOResult.getOrderId());
    return ResultVOUtil.success(data);
}
```

因为创建订单需要传入的是OrderDTO，所以写了一个OrderForm到OrderDTO的转换。为了将items这样的json字符串转成List，用到了GSON这个库。

```java
package com.shy.wechatsell.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shy.wechatsell.dataobject.OrderDetail;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.form.OrderForm;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/19 9:11
 */
@Slf4j
public class OrderForm2OrderDTO {

    public static OrderDTO convert(OrderForm orderForm) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBuyerName(orderForm.getName());
        orderDTO.setBuyerPhone(orderForm.getPhone());
        orderDTO.setBuyerAddress(orderForm.getAddress());
        orderDTO.setBuyerOpenid(orderForm.getOpenid());

        Gson gson = new Gson();
        List<OrderDetail> orderDetailList = null;
        try {
            orderDetailList = gson.fromJson(orderForm.getItems(), new TypeToken<List<OrderDetail>>(){}.getType());
        } catch (Exception e) {
            log.error("【对象装换】错误，string={}",orderForm.getItems());
            throw new SellException(ResultEnum.ORDER_PARAM_ERROR);
        }

        orderDTO.setOrderDetailList(orderDetailList);
        return orderDTO;
    }
}

```

#### 订单列表

请求被转发到

```html
GET /sell/buyer/order/list
```

表单中需要传入的参数如下

```json
openid: 18eu2jwk2kse3r42e2e
page: 0 //从第0页开始
size: 10
```

返回给前端的数据如下

```json
{
  "code": 0,
  "msg": "成功",
  "data": [
    {
      "orderId": "161873371171128075",
      "buyerName": "张三",
      "buyerPhone": "18868877111",
      "buyerAddress": "慕课网总部",
      "buyerOpenid": "18eu2jwk2kse3r42e2e",
      "orderAmount": 0,
      "orderStatus": 0,
      "payStatus": 0,
      "createTime": 1490171219,
      "updateTime": 1490171219,
      "orderDetailList": null
    },
    {
      "orderId": "161873371171128076",
      "buyerName": "张三",
      "buyerPhone": "18868877111",
      "buyerAddress": "慕课网总部",
      "buyerOpenid": "18eu2jwk2kse3r42e2e",
      "orderAmount": 0,
      "orderStatus": 0,
      "payStatus": 0,
      "createTime": 1490171219,
      "updateTime": 1490171219,
      "orderDetailList": null
    }]
}
```

可知data中其实就是`List<OrderDTO>`

```java
@GetMapping("/list")
public ResultVO<List<OrderDTO>> list(@RequestParam("openid") String openid,
                                        @RequestParam(value = "page",defaultValue = "0") Integer page,
                                        @RequestParam(value = "size",defaultValue = "10") Integer size) {
    if (StringUtils.isEmpty(openid)) {
        log.error("【查询订单】错误，openid不能为空");
        throw new SellException(ResultEnum.ORDER_PARAM_ERROR);
    }
    PageRequest pageRequest = PageRequest.of(page,size);
    Page<OrderDTO> orderDTOPage = orderService.findList(openid, pageRequest);
    // 查询到的订单中，订单详情不是必须的，orderDetailList却一致显示null，如果不想显示，可以不对空字段进行序列化
    // 这样返回到前端的数据就没有orderDetailList这个字段了
    return ResultVOUtil.success(orderDTOPage.getContent());
}
```

因为在订单主表的列表中，订单详情不是必须的，因此，如果不需要将orderDetailList传给前端。可以在OrderDTO类上加如下注解

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
```

则不会对当前类中null属性进行序列化。注解的方式作用范围只在当前类，如果要对所有类都有这样的要求，可以在application.yml中全局配置。

```yaml
spring:
	jackson:
		# 属性为空的不参与序列化
		default-property-inclusion: non_null
```

#### 订单详情、订单取消

这两个的代码类似，放在一块儿说。

订单的查询地址、订单取消地址分别如下

```html
<!--订单详情的查看-->
GET /sell/buyer/order/detail
<!--订单的取消-->
POST /sell/buyer/order/cancel
```

都传入下面这样的参数

```json
openid: 18eu2jwk2kse3r42e2e
orderId: 161899085773669363
```

查看订单详情，首先要根据订单号查询出该订单以及对应的订单详情。但不是任何人都有权查看的，只有下单的本人可以查看，为此必须要先经过身份的验证。检查传入的微信openid，和订单的openid对比就可以核实。

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.service.BuyerService;
import com.shy.wechatsell.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Haiyu
 * @date 2018/10/19 11:53
 */
@Service
@Slf4j
public class BuyerServiceImpl implements BuyerService {
    @Autowired
    private OrderService orderService;

    @Override
    public OrderDTO findOrderOne(String openid, String orderId) {
        return checkOrderOwner(openid, orderId);
    }

    private OrderDTO checkOrderOwner(String openid, String orderId) {
        OrderDTO orderDTO = orderService.findOne(orderId);
        if (!orderDTO.getBuyerOpenid().equals(openid)) {
            log.error("【查询订单】订单的openid不一致,openid={},orderId=", openid);
            throw new SellException(ResultEnum.ORDER_OWNER_ERROR);
        }
        return orderDTO;
    }
}

```

controller中的代码就简洁了很多

```java
@GetMapping("/detail")
public ResultVO<OrderDTO> detail(@RequestParam("openid") String openid,
                                 @RequestParam("orderId") String orderId) {
    OrderDTO orderDTO = buyerService.findOrderOne(openid,orderId);
    return ResultVOUtil.success(orderDTO);
}
```

取消订单也是类似，首先要获得订单，然后取消。

```java
@PostMapping("/cancel")
public ResultVO<OrderDTO> cancel(@RequestParam("openid") String openid,
                                 @RequestParam("orderId") String orderId) {
    OrderDTO orderDTO = buyerService.findOrderOne(openid,orderId);
    OrderDTO result = orderService.cancel(orderDTO);
    return ResultVOUtil.success(result);
}
```


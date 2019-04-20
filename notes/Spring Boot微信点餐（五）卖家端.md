## Spring Boot微信点餐（五）卖家端

### 订单

#### 订单查询与分页

订单查询在卖家端已经实现了，不过那会儿是根据某个openid查询对应用户的所有订单。卖家端能看到的当然是全部用户的所有订单了。在OrderService中新增查询所有订单的方法

```java
@Override
public Page<OrderDTO> findList(Pageable pageable) {
    Page<OrderMaster> orderMasterPage = orderMasterRepository.findAll(pageable);
    List<OrderDTO> orderDTOList = OrderMaster2OrderDTOConverter.convertList(orderMasterPage.getContent());
    return new PageImpl<>(orderDTOList,pageable,orderMasterPage.getTotalElements());
}
```

然后在Controller中

```java
package com.shy.wechatsell.controller;

import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Haiyu
 * @date 2018/10/26 22:45
 */
@Controller
@Slf4j
@RequestMapping("/seller/order")
public class SellerOrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 查询所有订单
     *
     * @param page 第几页
     * @param size 一页有几条
     * @return
     */
    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                       Model model) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<OrderDTO> orderDTOPage = orderService.findList(pageRequest);
        model.addAttribute("orderDTOPage", orderDTOPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);

        return "order/list";
    }
}

```

默认显示第一页，一页10条数据。因为Pageable是从0开始计数的，所所以参数中的page要减去1才正确。返回的模板文件为list.ftl，使用到了bootstrap。

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>卖家商品列表</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
</head>
<body>
<div class="container">
    <div class="row clearfix">
        <div class="col-md-12 column">
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>订单id</th>
                    <th>姓名</th>
                    <th>手机号</th>
                    <th>地址</th>
                    <th>金额</th>
                    <th>订单状态</th>
                    <th>支付状态</th>
                    <th>创建时间</th>
                    <th colspan="2">操作</th>
                </tr>
                </thead>
                <tbody>
                    <#list orderDTOPage.content as orderDTO>
                    <tr>
                        <td>${orderDTO.orderId}</td>
                        <td>${orderDTO.buyerName}</td>
                        <td>${orderDTO.buyerPhone}</td>
                        <td>${orderDTO.buyerAddress}</td>
                        <td>${orderDTO.orderAmount}</td>
                        <td>${orderDTO.getOrderStatusEnum().msg}</td>
                        <td>${orderDTO.getPayStatusEnum().msg}</td>
                        <td>${orderDTO.createTime}</td>
                        <td>
                            <a href="/sell/seller/order/detail?orderId=${orderDTO.orderId}">详情</a>
                        </td>
                        <td>
                            <#if orderDTO.orderStatus == 0>
                                <a href="/sell/seller/order/cancel?orderId=${orderDTO.orderId}">取消</a>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </tbody>
            </table>
            <#--分页-->
            <div class="col-md-12 column">
                <ul class="pagination pull-right" >
                    <#if currentPage lte 1>
                        <li class="disabled">
                            <a href="#">上一页</a>
                        </li>
                    <#else>
                        <li>
                            <a href="/sell/seller/order/list?page=${currentPage - 1}&size=${size}">上一页</a>
                        </li>
                    </#if>

                    <#list 1..orderDTOPage.getTotalPages() as index>
                    <#if currentPage == index>
                        <li class="disabled">
                            <a href="/sell/seller/order/list?page=${index}&size=${size}">${index}</a>
                        </li>
                    <#else>
                        <li class="">
                            <a href="/sell/seller/order/list?page=${index}&size=${size}">${index}</a>
                        </li>
                    </#if>
                    </#list>

                    <#if currentPage gte orderDTOPage.getTotalPages()>
                        <li class="disabled">
                            <a href="#">下一页</a>
                        </li>
                    <#else>
                        <li>
                            <a href="/sell/seller/order/list?page=${currentPage + 1}&size=${size}">下一页</a>
                        </li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>
</body>
</html>

```

这个页面主要是以表格的形式呈现出表格，同时实现了分页的功能。Page的getTotalPages()方法可以获取中页数，以此决定下方的按钮个数，每个按钮是一个超链接，其实就是改变了page和size的参数而已。

![订单](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-27_16-50-14.png?x-oss-process=style/small)

#### 订单详情与取消/完结订单

详情和取消实现如下。

```java
// 订单详情
@GetMapping("/detail")
public String detail(@RequestParam("orderId") String orderId,
                     Model model) {

    OrderDTO orderDTO = null;
    try {
        orderDTO = orderService.findOne(orderId);
    } catch (SellException e) {
        log.info("【卖家端订单详情】orderId={}", orderId);
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", "/sell/seller/order/list");
        return "common/error";
    }
    model.addAttribute("orderDTO", orderDTO);
    return "order/detail";
}
// 取消订单
@GetMapping("/cancel")
public String cancel(@RequestParam("orderId") String orderId,
                     Model model) {
    try {
        OrderDTO orderDTO = orderService.findOne(orderId);
        orderService.cancel(orderDTO);
    } catch (SellException e) {
        log.info("【卖家取消订单】，orderId={}", orderId);
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", "/sell/seller/order/list");
        return "common/error";
    }
    model.addAttribute("msg", ResultEnum.ORDER_CANCEL_SUCCESS.getMsg());
    model.addAttribute("url", "/sell/seller/order/list");
    return "common/success";
}
```

当取消订单和查看订单详情时发生异常，将会在前端页面显示给用户，并设置3秒后自动跳转会订单页面。

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>卖家错误提示</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
</head>
<body>
<div class="container">
    <div class="row clearfix">
        <div class="col-md-12 column">
            <div class="alert alert-dismissable alert-danger">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                <h4>
                    错误!
                </h4> <strong>${msg}</strong><a href="${url}" class="alert-link">3秒后自动跳转</a>
            </div>
        </div>
    </div>
</div>
<script>
    setTimeout('location.href="${url!}"',3000);
</script>
</body>
</html>
```

查看订单详情如果没有发生异常，则显示detail.ftl

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>商品列表详情</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
</head>
<body>
<div class="container">
    <div class="row clearfix">
    <#--订单-->
        <div class="col-md-4 column">
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>订单id</th>
                    <th>订单总金额</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>${orderDTO.orderId}</td>
                    <td>${orderDTO.orderAmount}</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <#--订单详情-->
    <div class="row clearfix">
        <div class="col-md-8 column">

            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>商品id</th>
                    <th>商品名称</th>
                    <th>价格</th>
                    <th>数量</th>
                    <th>总额</th>
                </tr>
                </thead>
                <tbody>
                    <#list orderDTO.orderDetailList as detail>
                    <tr>
                        <td>${detail.productId}</td>
                        <td>${detail.productName}</td>
                        <td>${detail.productPrice}</td>
                        <td>${detail.productQuantity}</td>
                        <td>${detail.productPrice} * ${detail.productQuantity}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </div>
    <#--操作-->
    <#if orderDTO.orderStatus == 0>
        <div class="row clearfix">
            <div class="col-md-12 column">
                <a href="/sell/seller/order/finish?orderId=${orderDTO.orderId}" type="button"
                   class="btn btn-default btn-primary">
                    完结订单
                </a>
                <a href="/sell/seller/order/cancel?orderId=${orderDTO.orderId}" type="button"
                   class="btn btn-danger btn-default">
                    取消订单
                </a>

            </div>
        </div>
    </#if>
</div>
</body>
</html>

```

订单详情页面如下，注意只有新订单才能被取消和完结。如过订单状态是其他，不会显示这两个按钮。

![订单详情](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-27_16-50-33.png?x-oss-process=style/small)

取消订单和订单页的“取消”超链接功能是一样的，现在来实现完结订单的功能。

```java
@GetMapping("/finish")
public String finish(@RequestParam("orderId") String orderId,
                     Model model) {
    try {
        OrderDTO orderDTO = orderService.findOne(orderId);
        orderService.finish(orderDTO);
    } catch (SellException e) {
        log.info("【微信卖家完结订单】订单不存在，orderId={}", orderId);
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", "/sell/seller/order/list");
        return "common/error";
    }
    model.addAttribute("msg", ResultEnum.ORDER_FINISH_SUCCESS.getMsg());
    model.addAttribute("url", "/sell/seller/order/list");
    return "common/success";
}
```

很简单，就是把cancel改成了finish而已。取消订单和完结订单成功都会显示一个成功界面，和错误界面一样，3秒自动跳转会订单页面。

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>成功提示</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
</head>
<body>
<div class="container">
    <div class="row clearfix">
        <div class="col-md-12 column">
            <div class="alert alert-dismissable alert-success">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                <h4>
                    成功!
                </h4> <strong>${msg}</strong><a href="${url}" class="alert-link">3秒后自动跳转</a>
            </div>
        </div>
    </div>
</div>
<script>
    setTimeout('location.href="${url!}"',3000);
</script>
</body>
</html>
```

### 商品

#### 商品列表与商品的上下架

商品列表包括了数据库中所有上架/下架的商品，卖家可以对商品进行上下架操作。

![](http://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-28_16-36-11.png)

侧边栏以及样式是给定，不用管。

查询所有商品的方法ProductService以前就写好了，直接看Controller中

```java
package com.shy.wechatsell.controller;

import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Haiyu
 * @date 2018/10/28 14:54
 */

@Controller
@Slf4j
@RequestMapping("/seller/product")
public class SellerProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                       Model model) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<ProductInfo> productInfoPage = productService.findAll(pageRequest);
        model.addAttribute("productInfoPage", productInfoPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);

        return "product/list";
    }

}

```

和卖家订单页面很类似。product/list.ftl如下，就是上面图片中的效果。

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>卖家商品列表</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
    <link rel="stylesheet" href="/sell/css/style.css">
</head>
<body>
<div id="wrapper" class="toggled">
<#--导航栏-->
    <#include "../common/nav.ftl">
<#--主体内容-->
    <div id="page-content-wrapper">
        <div class="container">
            <div class="row clearfix">
                <div class="col-md-12 column">
                    <table class="table table-bordered">
                        <thead>
                        <tr>
                            <th>商品id</th>
                            <th>名称</th>
                            <th>图片</th>
                            <th>单价</th>
                            <th>库存</th>
                            <th>描述</th>
                            <th>类目</th>
                            <th>创建时间</th>
                            <th>修改时间</th>
                            <th colspan="2">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                    <#list productInfoPage.content as productInfo>
                    <tr>
                        <td>${productInfo.productId}</td>
                        <td>${productInfo.productName}</td>
                        <td><img src="${productInfo.productIcon}" width="100" height="100" alt=""></td>
                        <td>${productInfo.productPrice}</td>
                        <td>${productInfo.productStock}</td>
                        <td>${productInfo.productDescription}</td>
                        <td>${productInfo.categoryType}</td>
                        <td>${productInfo.createTime}</td>
                        <td>${productInfo.updateTime}</td>
                        <td>
                            <a href="/sell/seller/product/index?productId=${productInfo.productId}">修改</a>
                        </td>
                        <td>
                            <#if productInfo.getProductStatusEnum().message == "上架">
                                <a href="/sell/seller/product/off_sale?productId=${productInfo.productId}">下架</a>
                            <#else>
                                <a href="/sell/seller/product/on_sale?productId=${productInfo.productId}">上架</a>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                        </tbody>
                    </table>
                <#--分页-->
                    <div class="col-md-12 column">
                        <ul class="pagination pull-right">
                    <#if currentPage lte 1>
                        <li class="disabled">
                            <a href="#">上一页</a>
                        </li>
                    <#else>
                        <li>
                            <a href="/sell/seller/product/list?page=${currentPage - 1}&size=${size}">上一页</a>
                        </li>
                    </#if>

                    <#list 1..productInfoPage.getTotalPages() as index>
                        <#if currentPage == index>
                        <li class="disabled">
                            <a href="/sell/seller/product/list?page=${index}&size=${size}">${index}</a>
                        </li>
                        <#else>
                        <li class="">
                            <a href="/sell/seller/product/list?page=${index}&size=${size}">${index}</a>
                        </li>
                        </#if>
                    </#list>

                    <#if currentPage gte productInfoPage.getTotalPages()>
                        <li class="disabled">
                            <a href="#">下一页</a>
                        </li>
                    <#else>
                        <li>
                            <a href="/sell/seller/product/list?page=${currentPage + 1}&size=${size}">下一页</a>
                        </li>
                    </#if>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

```

对已经上架的商品，只有下架操作；相反，对已经下架的操作，只有上架操作。

ProductService中新增商品的上下架操作。根据productId先查出商品，然后修改状态，最后再保存即可。

```java
@Override
public ProductInfo onSale(String productId) {
    Optional<ProductInfo> optionalProductInfo = repository.findById(productId);
    if (!optionalProductInfo.isPresent()) {
        throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
    }
    ProductInfo productInfo = optionalProductInfo.get();
    productInfo.setProductStatus(ProductStatusEnum.UP.getCode());
    ProductInfo result = repository.save(productInfo);
    return result;
}

@Override
public ProductInfo offSale(String productId) {
    Optional<ProductInfo> optionalProductInfo = repository.findById(productId);
    if (!optionalProductInfo.isPresent()) {
        throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
    }
    ProductInfo productInfo = optionalProductInfo.get();
    productInfo.setProductStatus(ProductStatusEnum.DOWN.getCode());
    ProductInfo result = repository.save(productInfo);
    return result;
}
```

最后在Controller中

 ```java
@GetMapping("/on_sale")
public String onSale(@RequestParam("productId") String productId,
                     Model model) {
    try {
        productService.onSale(productId);
    } catch (SellException e) {
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", "/sell/seller/product/list");
        return "common/error";
    }
    model.addAttribute("url", "/sell/seller/product/list");
    return "common/success";
}

@GetMapping("/off_sale")
public String offSale(@RequestParam("productId") String productId,
                      Model model) {
    try {
        productService.offSale(productId);
    } catch (SellException e) {
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", "/sell/seller/product/list");
        return "common/error";
    }
    model.addAttribute("url", "/sell/seller/product/list");
    return "common/success";
}
 ```

有些页面没有传msg或者url的，freemarker会报空指针异常。通过指定默认值，如`${msg!}`、`${msg!"default_value"}`解决。

#### 商品新增和修改

修改和新增使用同一个页面，同一个方法，区别是：修改时携带productId，便于从数据库中查询，将原来的值填充到页面中，设置了新值后更新回去；而新增不携带productId，页面内容为空，需要我们填入。这两者的区别是通过判断productId是否为空体现的。

在商品列表界面中点击修改，链接像下面这样

```html
http://localhost:8080/sell/seller/product/index?productId=12
```

跳到Controller中的index方法中处理

```java
@GetMapping("/index")
public String index(@RequestParam(value = "productId",required = false) String productId,
                    Model model) {

    if (!StringUtils.isEmpty(productId)) {
        ProductInfo productInfo = productService.findOne(productId);
        model.addAttribute("productInfo", productInfo);
    }

    List<ProductCategory> categoryList = categoryService.findAll();
    model.addAttribute("categoryList",categoryList);
    return "product/index";
}
```

可以看到如果productId不为空，将从数据库中查询并添加到Model中。

来看下index.ftl页面

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>商品新增/修改</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
    <link rel="stylesheet" href="/sell/css/style.css">
</head>
<body>
<div id="wrapper" class="toggled">
<#--导航栏-->
    <#include "../common/nav.ftl">
<#--主体内容-->
    <div id="page-content-wrapper">
        <div class="container">
            <div class="row clearfix">
                <div class="col-md-12 column">
                    <form role="form" method="post" action="/sell/seller/product/save">
                        <div class="form-group">
                            <label for="productName">名称</label>
                            <input id="productName" type="text" name="productName" value="${(productInfo.productName)!}"
                                   class="form-control"/>
                        </div>
                        <div class="form-group">
                            <label for="productPrice">价格</label>
                            <input id="productPrice" type="text" name="productPrice"
                                   value="${(productInfo.productPrice)!}" class="form-control"/>
                        </div>
                        <div class="form-group">
                            <label for="productStock">库存</label>
                            <input id="productStock" type="text" name="productStock"
                                   value="${(productInfo.productStock)!}" class="form-control"/>
                        </div>
                        <div class="form-group">
                            <label for="productDescription">描述</label>
                            <input id="productDescription" type="text" name="productDescription"
                                   value="${(productInfo.productDescription)!}" class="form-control"/>
                        </div>
                        <div class="form-group">
                            <label for="productIcon">图片</label>
                            <input id="productIcon" type="text" name="productIcon" value="${(productInfo.productIcon)!}"
                                   class="form-control"/>
                            <#if (productInfo.productIcon)??>
                                <img src="${(productInfo.productIcon)!}" width="200" height="200">
                            </#if>
                        </div>

                        <div class="form-group">
                            <label for="categoryType">类目</label>
                            <select id="categoryType" name="categoryType" class="form-control">
                                <#list categoryList as category>
                                    <option value="${category.categoryType}"
                                            <#if (productInfo.categoryType)?? && category.categoryType==productInfo.categoryType>
                                                selected
                                            </#if>
                                    >
                                        ${category.categoryName}
                                    </option>
                                </#list>
                            </select>
                        </div>
                        <input type="hidden" name="productId" value="${(productInfo.productId)!}">
                        <button type="submit" class="btn btn-default btn-primary">提交</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

```

修改和新增共用这个页面。

![](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-29_08-45-12.png)

这个表单采用POST提交到/save，来Controller中看看save方法

```java
@PostMapping("/save")
public String save(@Valid ProductForm form, BindingResult bindingResult,Model model) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
        model.addAttribute("url", "/sell/seller/product/index");
        return "common/error";
    }
    try {
        ProductInfo productInfo = null;
        // 如果productId为空说明是新增
        if (StringUtils.isEmpty(form.getProductId())) {
            form.setProductId(KeyUtil.genUniqueKey());
            productInfo = new ProductInfo();
            // 否则是更新，需要先查询再更新
        } else {
            productInfo = productService.findOne(form.getProductId());
        }
        BeanUtils.copyProperties(form, productInfo);
        productService.save(productInfo);
    } catch (SellException e) {
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", "/sell/seller/product/index");
        return "common/error";
    }
    model.addAttribute("url","/sell/seller/product/list");
    return "common/success";
}
```

也是判断了productId，不为空说明是修改，为空说明是新增，此时需要注意给商品添加productId，不然存到数据库中是空的。修改或新增成功后会跳转到商品列表界面。

save方法的入参，我们构造了一个表单对象。并对参数做了限制。

```java
package com.shy.wechatsell.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author Haiyu
 * @date 2018/10/28 21:32
 */
@Data
public class ProductForm {
    private String productId;

    /** 商品名字 */
    @NotBlank(message = "商品名字不能为空")
    private String productName;

    /** 商品价格 */
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格不能为负")
    private BigDecimal productPrice;

    /** 商品库存 */
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0,message = "商品库存不能为负")
    private Integer productStock;

    /** 商品类目 */
    @NotNull(message = "商品类目不能为空")
    private Integer categoryType;

    /** 商品描述 */
    private String productDescription;

    /** 商品小图 */
    private String productIcon;
}

```

`javax.validation`包下的如下几个注解，使用场景的区别如下

- @NotEmpty，适用于字符串和集合类
- @NotBlank，适用于字符串，要求至少有一个非空格字符
- @NotNull，适用于任何类型

### 商品类目

这部分和商品太相似了，很多代码copy过来稍微改改就成了。

#### 列表

类目列表的查询，如下的list方法

```java
package com.shy.wechatsell.controller;

import com.shy.wechatsell.dataobject.ProductCategory;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.form.CategoryForm;
import com.shy.wechatsell.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/28 22:49
 */
@Controller
@RequestMapping("/seller/category")
public class SellerCategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model) {
        List<ProductCategory> categoryList =  categoryService.findAll();
        model.addAttribute("categoryList", categoryList);
        return "category/list";
    }
}

```

list.ftl如下

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>商品类目列表</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
    <link rel="stylesheet" href="/sell/css/style.css">
</head>
<body>
<div id="wrapper" class="toggled">
<#--导航栏-->
    <#include "../common/nav.ftl">
<#--主体内容-->
    <div id="page-content-wrapper">
        <div class="container">
            <div class="row clearfix">
                <div class="col-md-12 column">
                    <table class="table table-bordered">
                        <thead>
                        <tr>
                            <th>商品类目id</th>
                            <th>类目名称</th>
                            <th>类目编号</th>
                            <th>创建时间</th>
                            <th>修改时间</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                    <#list categoryList as category>
                    <tr>
                        <td>${category.categoryId}</td>
                        <td>${category.categoryName}</td>
                        <td>${category.categoryType}</td>
                        <td>${category.createTime}</td>
                        <td>${category.updateTime}</td>
                        <td>
                            <a href="/sell/seller/category/index?categoryId=${category.categoryId}">修改</a>
                        </td>
                    </tr>
                    </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

```

商品类目列表页面如下

![](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-29_08-45-34.png)

点击右边的修改，或者新增，可以跳到修改和新增的index页面，Controller中index方法如下

```java
@GetMapping("/index")
public String index(@RequestParam(value = "categoryId",required = false) Integer categoryId,
                    Model model) {
    if (categoryId != null) {
        ProductCategory category = categoryService.findOne(categoryId);
        model.addAttribute("category",category);
    }

    return "category/index";
}
```

因为categoryId是Integer类型的，不传入是null，所有没有使用StringUtils（用了也可以，因为包含了判null）。和商品的index方法类似，不再赘述。

index.ftl如下

```html
<html>
<head>
    <meta charset="UTF-8">
    <title>商品类目新增/修改</title>
    <link href="https://cdn.bootcss.com/twitter-bootstrap/3.0.1/css/bootstrap.min.css" rel=stylesheet>
    <link rel="stylesheet" href="/sell/css/style.css">
</head>
<body>
<div id="wrapper" class="toggled">
<#--导航栏-->
    <#include "../common/nav.ftl">
<#--主体内容-->
    <div id="page-content-wrapper">
        <div class="container">
            <div class="row clearfix">
                <div class="col-md-12 column">
                    <form role="form" method="post" action="/sell/seller/category/save">
                        <div class="form-group">
                            <label for="categoryName">类目名称</label>
                            <input id="categoryName" type="text" name="categoryName" value="${(category.categoryName)!}"
                                   class="form-control"/>
                        </div>
                        <div class="form-group">
                            <label for="categoryType">类目编号</label>
                            <input id="categoryType" type="text" name="categoryType"
                                   value="${(category.categoryType)!}" class="form-control"/>
                        </div>

                        <input type="hidden" name="categoryId" value="${(category.categoryId)!}">
                        <button type="submit" class="btn btn-default btn-primary">提交</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

```

![](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-29_08-45-47.png)

同样的，修改和新增页面就是一个表单，点击提交后，跳到save方法。

```java
@PostMapping("/save")
public String save(@Valid CategoryForm form, BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
        model.addAttribute("url", "/sell/seller/category/index");
        return "common/error";
    }
    try {
        ProductCategory productCategory = null;
        // 如果categoryId为空说明是新增
        if (form.getCategoryId() == null) {
            productCategory = new ProductCategory();
            // productCategory的id是自增的，所以无需手动设置
            // 否则是更新，需要先查询再更新
        } else {
            productCategory = categoryService.findOne(form.getCategoryId());
        }
        BeanUtils.copyProperties(form, productCategory);
        categoryService.save(productCategory);
    } catch (SellException e) {
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", "/sell/seller/category/index");
        return "common/error";
    }
    model.addAttribute("url","/sell/seller/category/list");
    return "common/success";
}
```

和商品的保存代码几乎一样。要注意的是由于categoryId是Integer类型的，freemarker中使用`${categoryId!}`为其指定默认值是null（不像字符串类型的指定为空串`""`）,所以这里判断直接用的是 `form.getCategoryId() == null`。我们也为入参写了一个CategoryForm来封装表单提交的内容。

```java
package com.shy.wechatsell.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Haiyu
 * @date 2018/10/28 23:26
 */
@Data
public class CategoryForm {
    private Integer categoryId;
    /** 类目名字 */
    @NotBlank(message = "类目名称不能为空")
    private String categoryName;
    /** 类目编号 */
    @NotNull(message = "类目编号不能为空")
    private Integer categoryType;
}

```


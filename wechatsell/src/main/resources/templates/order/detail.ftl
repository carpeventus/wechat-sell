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

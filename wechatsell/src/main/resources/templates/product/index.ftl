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

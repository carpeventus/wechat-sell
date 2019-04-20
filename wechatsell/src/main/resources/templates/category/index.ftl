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

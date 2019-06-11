## Spring Boot微信点餐（一）

### 项目设计

**角色设计**

- 买家（微信手机端）
- 卖家（浏览器PC端）

**功能分析**

![](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2019-06-11_20-56-03.png)

**角色关系图**

![](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2019-06-11_20-56-18.png)

**部署架构**

微信端和PC端都会发起数据请求，请求首先会到达Ngnix服务器，前端的代码已经写好了，直接使用即可；如果请求的是后端接口，会转发到Tomcat服务器（分布式，其实是多台服务器）。如果项目做了缓存，会使用到Redis，否则会查询MySQL数据库。

![](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2019-06-11_20-56-26.png)

### 数据库设计

![](https://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2019-06-11_20-56-35.png)

一个类目下有多个商品，每个商品又归属于某一个类目，所有类目和商品属于一对多的关系。订单详情表包含了商品信息、和订单id，而订单表中包含了买家信息、订单状态、支付状态等信息，一个订单可以包含多个订单详情，或者说一个订单详情归属于某一个订单，因此订单和订单详情是一对多的关系。

```mysql
-- 类目
create table `product_category` (
    `category_id` int not null auto_increment,
    `category_name` varchar(64) not null comment '类目名字',
    `category_type` int not null comment '类目编号',
    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`category_id`)
);

-- 商品
create table `product_info` (
    `product_id` varchar(32) not null,
    `product_name` varchar(64) not null comment '商品名称',
    `product_price` decimal(8,2) not null comment '单价',
    `product_stock` int not null comment '库存',
    `product_description` varchar(64) comment '描述',
    `product_icon` varchar(512) comment '小图',
    `product_status` tinyint(3) DEFAULT '0' COMMENT '商品状态,0正常1下架',
    `category_type` int not null comment '类目编号',
    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`product_id`)
);

-- 订单
create table `order_master` (
    `order_id` varchar(32) not null,
    `buyer_name` varchar(32) not null comment '买家名字',
    `buyer_phone` varchar(32) not null comment '买家电话',
    `buyer_address` varchar(128) not null comment '买家地址',
    `buyer_openid` varchar(64) not null comment '买家微信openid',
    `order_amount` decimal(8,2) not null comment '订单总金额',
    `order_status` tinyint(3) not null default '0' comment '订单状态, 默认为新下单',
    `pay_status` tinyint(3) not null default '0' comment '支付状态, 默认未支付',
    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`order_id`),
    key `idx_buyer_openid` (`buyer_openid`)
);

-- 订单商品
create table `order_detail` (
    `detail_id` varchar(32) not null,
    `order_id` varchar(32) not null,
    `product_id` varchar(32) not null,
    `product_name` varchar(64) not null comment '商品名称',
    `product_price` decimal(8,2) not null comment '当前价格,单位分',
    `product_quantity` int not null comment '数量',
    `product_icon` varchar(512) comment '小图',
    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`detail_id`),
    key `idx_order_id` (`order_id`)
);

-- 卖家(登录后台使用, 卖家登录之后可能直接采用微信扫码登录，不使用账号密码)
create table `seller_info` (
    `id` varchar(32) not null,
    `username` varchar(32) not null,
    `password` varchar(32) not null,
    `openid` varchar(64) not null comment '微信openid',
    `create_time` timestamp not null default current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`id`)
) comment '卖家信息表';

```

建表语句中注意几点

- 如果同时定义DEFAULT CURRENT_TIMESTAMP和ON UPDATE CURRENT_TIMESTAMP，列值默认使用当前的时间戳，并且自动更新（每次修改都由数据库更新为当期时间）。
- 如果都不定义DEFAULT和ON UPDATE子句，那么它等同于DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP。
- 如果只有DEFAULT CURRENT_TIMESTAMP，而没有ON UPDATE，列值默认为当前时间戳但不自动更新。
- 如果没用DEFAULT子句，但有ON UPDATE CURRENT_TIMESTAMP子句，列默认为0并自动更新。

`decimal(8,2)`，表示限制了该字段可以存储8位数字，小数位数2位。

### 日志配置

需求如下

- 日志按天滚动分割

- info和error日志输出到不同文件

日志使用Slf4j + Logback，logback的配置文件如下，放在appication.yml的同级目录下。

```java
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <!--用于Console的输出-->
    <appender name="consoleLog" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <pattern>
                %d - %msg%n
            </pattern>
        </layout>
    </appender>
    <!--滚动日志文件输出-->
    <appender name="fileInfoLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!--LevelFilter：指定特定的level级别，match时拒绝，mismatch时接受
        下面的配置意思是：只把ERROR排除在外，其余级别接受-->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>DENY</onMatch>
            <onMismatch>ACCEPT</onMismatch>
        </filter>
        <encoder>
            <pattern>
                %msg%n
            </pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>C:\Users\haiyu\Documents\log\info-%d.log</fileNamePattern>
        </rollingPolicy>
    </appender>

    <appender name="fileErrorLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!--ThresholdFilter：设置的level及其以上的级别会被保留，其下的级别被过滤-->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>

        <encoder>
            <pattern>
                %msg%n
            </pattern>
        </encoder>
        <!--滚动策略-->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--路径-->
            <fileNamePattern>C:\Users\haiyu\Documents\log\error-%d.log</fileNamePattern>
        </rollingPolicy>
    </appender>

    <!--root表示用于整个项目,设置的level表示，该级别及其以上的级别才会输出，该级别以下的过滤掉
    从高到低：
    error
    warn
    info
    debug
    trace
    -->
    <root level="info">
        <appender-ref ref="consoleLog" />
        <appender-ref ref="fileInfoLog" />
        <appender-ref ref="fileErrorLog" />
    </root>
</configuration>
```

`%d - %msg%n`表示输出日期和日志信息。

`TimeBasedRollingPolicy`根据时间来设定的日志文件滚动策略。在fileNamePattern中使用`%d`说明是按天生成日志文件（即一天一个）。上面日志设置的最终目的是，将ERROR单独输出到一个文件中，同时INFO级别及其以上的信息（除ERROR外）输出到另一个文件。


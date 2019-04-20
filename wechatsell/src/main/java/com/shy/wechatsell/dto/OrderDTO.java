package com.shy.wechatsell.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shy.wechatsell.dataobject.OrderDetail;
import com.shy.wechatsell.enums.OrderStatusEnum;
import com.shy.wechatsell.enums.PayStatusEnum;
import com.shy.wechatsell.serialize.Date2LongSerializer;
import com.shy.wechatsell.util.EnumUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/17 9:37
 */
@Data
// 为空的字段不会被序列化，可以在application.yml中做全局配置
//@JsonInclude(JsonInclude.Include.NON_NULL)
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
    // 将class的属性按照using中的类的方法序列化成其他属性
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date createTime;

    /** 订单更新时间 */
    @JsonSerialize(using = Date2LongSerializer.class)
    private Date updateTime;

    /** 一个订单对应的多个订单详情 */
    private List<OrderDetail> orderDetailList;

    // @JsonIgnore会忽略对该字段的序列化
    @JsonIgnore
    public OrderStatusEnum getOrderStatusEnum() {
        return EnumUtil.getByCode(orderStatus, OrderStatusEnum.class);
    }

    @JsonIgnore
    public PayStatusEnum getPayStatusEnum() {
        return EnumUtil.getByCode(payStatus, PayStatusEnum.class);
    }
}

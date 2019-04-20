package com.shy.wechatsell.enums;

import lombok.Getter;

/**
 * @author Haiyu
 * @date 2018/10/16 12:41
 */
@Getter
public enum OrderStatusEnum implements CodeEnum{
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

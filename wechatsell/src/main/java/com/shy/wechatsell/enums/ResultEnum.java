package com.shy.wechatsell.enums;

import lombok.Getter;

/**
 * @author Haiyu
 * @date 2018/10/17 9:57
 */
@Getter
public enum ResultEnum {
    /** 创建订单时前台传过来的参数不正确 */
    SUCCESS(0, "成功"),
    ORDER_PARAM_ERROR(1, "订单参数不正确"),
    PRODUCT_NOT_EXIST(10,"商品不存在"),
    PRODUCT_STOCK_ERROR(11,"商品库存不足"),
    ORDER_NOT_EXIST(12,"订单不存在"),
    ORDER_DETAIL_NOT_EXIST(13,"订单详情不存在"),
    ORDER_STATUS_ERROR(14,"订单状态异常"),
    ORDER_UPDATE_FAIL(15,"订单更新失败"),
    ORDER_DETAIL_EMPTY(16,"订单详情为空"),
    ORDER_PAY_STATUS_ERROR(17,"订单支付状态不正确"),
    ORDER_CART_EMPTY(18,"购物车不能为空"),
    ORDER_OWNER_ERROR(19,"订单不属于当前用户"),
    WECHAT_MP_ERROR(20,"微信公众账号方面错误"),
    WECHAT_PAY_NOTIFY_MONEY_VERIFY_ERROR(21,"微信支付异步通知金额校验不通过"),
    ORDER_CANCEL_SUCCESS(22,"订单取消成功"),
    ORDER_FINISH_SUCCESS(23,"订单完结成功"),
    LOGIN_FAILED(24,"登录失败"),
    LOGOUT_SUCCESS(25,"登出成功"),
    ;
    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

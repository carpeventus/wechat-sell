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

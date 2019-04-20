package com.shy.wechatsell.enums;

import lombok.Getter;

/**
 * @author Haiyu
 * @date 2018/10/15 17:39
 */
@Getter
public enum ProductStatusEnum implements CodeEnum{
    /**
     * 商品上架的状态码
     */
    UP(0,"上架"),
    /**
     * 商品下架的状态码
     */
    DOWN(1,"下架")
    ;

    private Integer code;
    private String message;

    ProductStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

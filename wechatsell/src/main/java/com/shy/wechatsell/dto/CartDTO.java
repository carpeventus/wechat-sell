package com.shy.wechatsell.dto;

import lombok.Data;

/**
 * @author Haiyu
 * @date 2018/10/17 16:16
 */
@Data
public class CartDTO {
    private String productId;
    private Integer productQuantity;

    public CartDTO(String productId, Integer productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }
}

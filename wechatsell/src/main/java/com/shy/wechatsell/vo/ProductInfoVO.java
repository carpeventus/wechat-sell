package com.shy.wechatsell.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品详情（出于安全考虑，不直接使用ProductInfo）
 *
 * @author Haiyu
 * @date 2018/10/16 9:20
 */
@Data
public class ProductInfoVO implements Serializable {

    private static final long serialVersionUID = 1632640773448668119L;
    @JsonProperty("id")
    private String productId;

    @JsonProperty("name")
    private String productName;

    @JsonProperty("price")
    private BigDecimal productPrice;

    @JsonProperty("description")
    private String productDescription;

    @JsonProperty("icon")
    private String productIcon;
}

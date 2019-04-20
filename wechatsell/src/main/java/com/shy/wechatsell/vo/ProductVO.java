package com.shy.wechatsell.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品-包含类目
 *
 * @author Haiyu
 * @date 2018/10/16 9:16
 */
@Data
public class ProductVO implements Serializable {
    private static final long serialVersionUID = -1378641221058075574L;
    @JsonProperty("name")
    private String categoryName;

    @JsonProperty("type")
    private Integer categoryType;

    @JsonProperty("foods")
    private List<ProductInfoVO> productInfoVOList;
}

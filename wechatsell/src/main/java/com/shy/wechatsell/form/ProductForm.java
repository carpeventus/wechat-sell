package com.shy.wechatsell.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author Haiyu
 * @date 2018/10/28 21:32
 */
@Data
public class ProductForm {
    private String productId;

    /** 商品名字 */
    @NotBlank(message = "商品名字不能为空")
    private String productName;

    /** 商品价格 */
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格不能为负")
    private BigDecimal productPrice;

    /** 商品库存 */
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0,message = "商品库存不能为负")
    private Integer productStock;

    /** 商品类目 */
    @NotNull(message = "商品类目不能为空")
    private Integer categoryType;

    /** 商品描述 */
    private String productDescription;

    /** 商品小图 */
    private String productIcon;
}

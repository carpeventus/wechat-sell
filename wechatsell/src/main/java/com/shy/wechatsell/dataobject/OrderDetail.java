package com.shy.wechatsell.dataobject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Haiyu
 * @date 2018/10/16 12:54
 */
@Data
@Entity
public class OrderDetail {

    @Id
    private String detailId;

    /** 所在订单号 */
    private String orderId;

    /** 商品id */
    private String productId;

    /** 商品名字 */
    private String productName;

    /** 商品价格 */
    private BigDecimal productPrice;

    /** 商品数量 */
    private Integer productQuantity;

    /** 商品小图 */
    private String productIcon;
}

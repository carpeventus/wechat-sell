package com.shy.wechatsell.service;

import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.dto.CartDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 17:28
 */
public interface ProductService {
    /**
     * 根据id查找某一个商品
     * @param productId
     * @return
     */
    ProductInfo findOne(String productId);

    /**
     * 查找所有上架的商品
     * @return
     */
    List<ProductInfo> findUpAll();

    /**
     * 查找所有商品
     * @param pageable
     * @return
     */
    Page<ProductInfo> findAll(Pageable pageable);

    /**
     * 保存商品
     * @param productInfo
     * @return
     */
    ProductInfo save(ProductInfo productInfo);

    /**
     * 加库存
     * @param cartDTOList
     */
    void increaseStock(List<CartDTO> cartDTOList);

    /**
     * 减库存
     * @param cartDTOList
     */
    void decreaseStock(List<CartDTO> cartDTOList);

    /**
     * 商品上架
     * @param productId
     * @return
     */
    ProductInfo onSale(String productId);

    /**
     * 商品下架
     * @param productId
     * @return
     */
    ProductInfo offSale(String productId);
}

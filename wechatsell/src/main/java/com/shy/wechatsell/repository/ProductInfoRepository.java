package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 16:56
 */
public interface ProductInfoRepository extends JpaRepository<ProductInfo, String> {
    /**
     * 按照商品的状态查询
     * @param productStatus
     * @return
     */
    List<ProductInfo> findByProductStatus(Integer productStatus);
}

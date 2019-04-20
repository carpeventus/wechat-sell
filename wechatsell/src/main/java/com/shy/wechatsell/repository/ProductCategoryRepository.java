package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 9:48
 */
public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Integer> {
    /**
     * 根据类目编号查找商品类目
     * @param categoryTypes
     * @return
     */
    List<ProductCategory> findByCategoryTypeIn(List<Integer> categoryTypes);
}

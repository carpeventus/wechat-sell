package com.shy.wechatsell.service;

import com.shy.wechatsell.dataobject.ProductCategory;

import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 13:20
 */
public interface CategoryService {
    /**
     * 查找所有类目
     * @return
     */
    List<ProductCategory> findAll();

    /**
     * 根据id查找类目
     * @param id
     * @return
     */
    ProductCategory findOne(Integer id);

    /**
     * 根据传入的类目编号查询
     * @param categoryList
     * @return
     */
    List<ProductCategory> findByCategoryTypes(List<Integer> categoryList);

    /**
     * 保存一个类目
     * @param category
     * @return
     */
    ProductCategory save(ProductCategory category);

}

package com.shy.wechatsell.controller;

import com.shy.wechatsell.dataobject.ProductCategory;
import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.service.CategoryService;
import com.shy.wechatsell.service.ProductService;
import com.shy.wechatsell.vo.ProductInfoVO;
import com.shy.wechatsell.vo.ProductVO;
import com.shy.wechatsell.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.shy.wechatsell.util.ResultVOUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Haiyu
 * @date 2018/10/16 9:11
 */
@RestController
@RequestMapping("/buyer/product")
public class BuyerProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
//    将返回结果添加至缓存，在方法执行前验证如果命中缓存，则该方法不会得到执行
    @Cacheable(cacheNames = "product",key = "123")
    public ResultVO list() {
        // 1. 查询所有已上架的商品
        List<ProductInfo> productInfoList = productService.findUpAll();
        // 已上架的商品类目编号
        List<Integer> categoryTypeList = productInfoList.stream()
                .map(ProductInfo::getCategoryType)
                .collect(Collectors.toList());
        // 2.上架商品的所有类目
        List<ProductCategory> productCategories = categoryService.findByCategoryTypes(categoryTypeList);

        // 3.数据封装
        List<ProductVO> productVOList = new ArrayList<>();
        for (ProductCategory productCategory : productCategories) {
            ProductVO productVO = new ProductVO();
            productVO.setCategoryName(productCategory.getCategoryName());
            productVO.setCategoryType(productCategory.getCategoryType());

            List<ProductInfoVO> productInfoVOList = new ArrayList<>();
            for (ProductInfo productInfo : productInfoList) {
                // 商品的类目和当前类目一样，才把该商品展示在当前类目下
                if (productInfo.getCategoryType().equals(productCategory.getCategoryType())) {
                    ProductInfoVO productInfoVO = new ProductInfoVO();
                    // 只会拷贝匹配的属性（属性名相同）
                    BeanUtils.copyProperties(productInfo,productInfoVO);
                    productInfoVOList.add(productInfoVO);
                }
            }
            productVO.setProductInfoVOList(productInfoVOList);
            productVOList.add(productVO);
        }
        return ResultVOUtil.success(productVOList);
    }
}

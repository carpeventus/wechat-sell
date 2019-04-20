package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.ProductInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 18:03
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductServiceImplTest {
    @Autowired
    private ProductServiceImpl productService;
    @Test
    public void findOne() {
        ProductInfo productInfo = productService.findOne("12");
        Assert.assertEquals("12", productInfo.getProductId());
    }

    @Test
    public void findUpAll() {
        List<ProductInfo> productInfos = productService.findUpAll();
        Assert.assertNotEquals(0, productInfos.size());
    }

    @Test
    public void findAll() {
        // PageRequest.of(0, 2);按照size=2划分page，返回第0页的数据
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<ProductInfo> productInfos = productService.findAll(pageRequest);
        // 获取所有元素的个数
        System.out.println(productInfos.getTotalElements());
        // 按照size划分为一页，总共分成了多少页
        System.out.println(productInfos.getTotalPages());
        // 获取当前Slice所在的index
        System.out.println(productInfos.getNumber());
        // 获取当前Slice包含的元素个数
        System.out.println(productInfos.getNumberOfElements());
        // 获取PageRequest的size大小
        System.out.println(productInfos.getSize());
        // 获取请求当前Slice的Pageable对象，在这里就是PageRequest
        System.out.println(productInfos.getPageable());
        // 获取查询到的数据
        System.out.println(productInfos.getContent());
    }

    @Test
    public void save() {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId("15");
        productInfo.setProductName("葱爆牛肉");
        productInfo.setProductPrice(new BigDecimal(20));
        productInfo.setProductDescription("清真");
        productInfo.setProductIcon("http://image.test3.jpg");
        productInfo.setCategoryType(3);
        productInfo.setProductStock(100);
        ProductInfo result = productService.save(productInfo);
        Assert.assertNotNull(result);
    }
}
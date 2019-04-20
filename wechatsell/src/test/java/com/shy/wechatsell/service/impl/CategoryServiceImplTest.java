package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.ProductCategory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 13:29
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class CategoryServiceImplTest {
    @Autowired
    private CategoryServiceImpl categoryService;

    @Test
    public void findAll() {
        List<ProductCategory> categories = categoryService.findAll();
        Assert.assertNotEquals(0, categories.size());
    }

    @Test
    public void findOne() {
        ProductCategory result = categoryService.findOne(1);
        Assert.assertEquals(new Integer(1),result.getCategoryId());
    }

    @Test
    public void findByCategoryTypes() {
        List<ProductCategory> categories = categoryService.findByCategoryTypes(Arrays.asList(1, 2, 3));
        Assert.assertNotEquals(0, categories.size());
    }

    @Test
    public void save() {
//        ProductCategory category = categoryService.findOne(3);
        ProductCategory category = new ProductCategory();
        category.setCategoryName("女生专享");
        category.setCategoryType(4);
        ProductCategory result = categoryService.save(category);
        Assert.assertNotNull(result);
    }
}
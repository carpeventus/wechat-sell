package com.shy.wechatsell;

import com.shy.wechatsell.dataobject.ProductCategory;
import com.shy.wechatsell.repository.ProductCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/15 9:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ProductCategoryTest {
    @Autowired
    private ProductCategoryRepository repository;

    @Test
    @Transactional
    public void saveTest() {
        ProductCategory category = new ProductCategory();
        category.setCategoryName("男生最爱");
        category.setCategoryType(4);
        ProductCategory result = repository.save(category);
        Assert.assertNotNull(result);
    }

    @Test
    public void updateTest() {
        ProductCategory category = repository.findById(2).get();
        category.setCategoryName("女生最爱");
        repository.save(category);
    }

    @Test
    public void findOneTest() {
        ProductCategory category = repository.findById(1).get();
        log.info(category.toString());
    }

    @Test
    public void findByCategoryTypeTest() {
        List<Integer> categoryTypes = Arrays.asList(2, 3, 4);
        List<ProductCategory> categories = repository.findByCategoryTypeIn(categoryTypes);
        Assert.assertNotEquals(0, categories.size());
    }

}

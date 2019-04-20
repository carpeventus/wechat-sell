package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.ProductInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Haiyu
 * @date 2018/10/15 17:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductInfoRepositoryTest {
    @Autowired
    private ProductInfoRepository repository;

    @Test
    public void findByProductStatus() {
        List<ProductInfo> productInfos = repository.findByProductStatus(0);
        Assert.assertNotEquals(0, productInfos.size());

    }

    @Test
    public void saveTest() {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId("13");
        productInfo.setProductName("冰封");
        productInfo.setCategoryType(1);
        productInfo.setProductDescription("爽");
        productInfo.setProductPrice(new BigDecimal(2));
        productInfo.setProductStock(100);
        productInfo.setProductIcon("http://image.ice_peak.jpg");
        ProductInfo result =repository.save(productInfo);
        Assert.assertNotNull(result);
    }
}
package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.OrderMaster;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/16 13:15
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderMasterRepositoryTest {
    @Autowired
    private OrderMasterRepository repository;

    @Test
    public void saveTest() {
        OrderMaster orderMaster = new OrderMaster();
        orderMaster.setOrderId("DD002");
        orderMaster.setBuyerOpenid("abc123");
        orderMaster.setBuyerName("李雷");
        orderMaster.setBuyerAddress("朝阳区");
        orderMaster.setBuyerPhone("13255555555");
        orderMaster.setOrderAmount(new BigDecimal(33));
        OrderMaster result = repository.save(orderMaster);
        Assert.assertNotNull(result);
    }

    @Test
    public void findByBuyerOpenid() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<OrderMaster> orderMasterPage =  repository.findByBuyerOpenid("abc123",pageable);
        Assert.assertNotEquals(0, orderMasterPage.getContent().size());
    }
}
package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.OrderDetail;
import com.shy.wechatsell.dataobject.OrderMaster;
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
 * @date 2018/10/16 13:15
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderDetailRepositoryTest {
    @Autowired
    private OrderDetailRepository repository;

    @Test
    public void saveTest() {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setDetailId("1234");
        orderDetail.setOrderId("DD001");
        orderDetail.setProductId("12");
        orderDetail.setProductName("肉夹馍");
        orderDetail.setProductQuantity(2);
        orderDetail.setProductPrice(new BigDecimal(9));
        orderDetail.setProductIcon("http://image.test.jpg");
        OrderDetail result = repository.save(orderDetail);
        Assert.assertNotNull(result);
    }

    @Test
    public void findByOrderId() {
        List<OrderDetail> orderDetailList =  repository.findByOrderId("DD001");
        assertNotEquals(0, orderDetailList.size());
    }
}
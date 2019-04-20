package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.OrderDetail;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.OrderStatusEnum;
import com.shy.wechatsell.enums.PayStatusEnum;
import com.shy.wechatsell.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/17 16:33
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class OrderServiceImplTest {
    private final String BUYER_OPENID = "oTgZpwTgorulVMHTszCrbA63vVFQ";
    private final String ORDER_ID = "1539767288565313230";

    @Autowired
    private OrderService orderService;

    @Test
    public void create() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBuyerName("二狗");
        orderDTO.setBuyerPhone("13367890231");
        orderDTO.setBuyerAddress("北京市");
        orderDTO.setBuyerOpenid(BUYER_OPENID);

        List<OrderDetail> orderDetailList = new ArrayList<>();

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProductId("12");
        orderDetail.setProductQuantity(4);

        OrderDetail orderDetailOther = new OrderDetail();
        orderDetailOther.setProductId("13");
        orderDetailOther.setProductQuantity(1);

        orderDetailList.add(orderDetail);
        orderDetailList.add(orderDetailOther);

        orderDTO.setOrderDetailList(orderDetailList);
        orderService.create(orderDTO);
    }

    @Test
    public void findOne() {
        OrderDTO orderDTO = orderService.findOne(ORDER_ID);
        log.info("【查询单个订单】result={}",orderDTO);
        Assert.assertEquals(ORDER_ID,orderDTO.getOrderId());
    }

    @Test
    public void findList() {
        PageRequest request = PageRequest.of(0, 3);
        Page<OrderDTO> orderDTOPage = orderService.findList(BUYER_OPENID,request);
        log.info("{}这个买家的订单记录：{}",BUYER_OPENID, orderDTOPage.getContent());
        Assert.assertNotEquals(0, orderDTOPage.getTotalElements());
    }

    @Test
    public void cancel() {
        OrderDTO orderDTO = orderService.findOne(ORDER_ID);
        OrderDTO result = orderService.cancel(orderDTO);
        Assert.assertEquals(OrderStatusEnum.CANCEL.getCode(), result.getOrderStatus());
    }

    @Test
    public void finish() {
        OrderDTO orderDTO = orderService.findOne(ORDER_ID);
        OrderDTO result = orderService.finish(orderDTO);
        Assert.assertEquals(OrderStatusEnum.FINISHED.getCode(), result.getOrderStatus());
    }

    @Test
    public void paid() {
        OrderDTO orderDTO = orderService.findOne(ORDER_ID);
        OrderDTO result = orderService.paid(orderDTO);
        Assert.assertEquals(PayStatusEnum.SUCCESS.getCode(), result.getPayStatus());
    }

    @Test
    public void findAll() {
        PageRequest request = PageRequest.of(0, 3);
        Page<OrderDTO> orderDTOPage = orderService.findList(request);
        Assert.assertNotEquals(0, orderDTOPage.getTotalElements());
    }
}
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.service.OrderService;
import com.shy.wechatsell.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author Haiyu
 * @date 2018/10/25 22:03
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class PayServiceImplTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PayService payService;

    @Test
    public void create() {
        OrderDTO orderDTO = orderService.findOne("1539766989014790623");
        payService.create(orderDTO);
    }
}
package com.shy.wechatsell.service;

import com.shy.wechatsell.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Haiyu
 * @date 2018/10/17 9:39
 */
public interface OrderService {
    /** 创建订单 */
    OrderDTO create(OrderDTO orderDTO);

    /** 查询单个订单 */
    OrderDTO findOne(String orderId);

    /** 查询订某用户的单列表 */
    Page<OrderDTO> findList(String buyerOpenid, Pageable pageable);

    /** 查询所有订单列表 */
    Page<OrderDTO> findList(Pageable pageable);

    /** 取消订单 */
    OrderDTO cancel(OrderDTO orderDTO);

    /** 完成订单 */
    OrderDTO finish(OrderDTO orderDTO);

    /** 支付订单 */
    OrderDTO paid(OrderDTO orderDTO);
}

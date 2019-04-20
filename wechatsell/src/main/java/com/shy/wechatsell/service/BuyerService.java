package com.shy.wechatsell.service;

import com.shy.wechatsell.dto.OrderDTO;

/**
 * @author Haiyu
 * @date 2018/10/19 11:51
 */
public interface BuyerService {
    /** 查询一个用户的某个订单 */
    OrderDTO findOrderOne(String openid, String orderId);
}

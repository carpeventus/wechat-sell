package com.shy.wechatsell.service;

import com.shy.wechatsell.dto.OrderDTO;

/**
 * @author Haiyu
 * @date 2018/10/30 15:44
 */
public interface PushMesageService {
    /**
     *  推送订单消息变动的消息
     * @param orderDTO
     */
    void orderStatus(OrderDTO orderDTO);
}

package com.shy.wechatsell.service;

import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.model.RefundResponse;
import com.shy.wechatsell.dto.OrderDTO;

/**
 * @author Haiyu
 * @date 2018/10/25 18:59
 */
public interface PayService {
    /**
     * 支付订单的创建
     * @param orderDTO
     * @return
     */
    PayResponse create(OrderDTO orderDTO);

    /**
     * 微信异步回调通知
     * @param notifyData
     * @return
     */
    PayResponse notify(String notifyData);

    /**
     * 退款
     * @param orderDTO
     * @return
     */
    RefundResponse refund(OrderDTO orderDTO);
}

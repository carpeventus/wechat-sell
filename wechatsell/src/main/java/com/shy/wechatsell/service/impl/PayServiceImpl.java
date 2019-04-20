package com.shy.wechatsell.service.impl;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.model.RefundRequest;
import com.lly835.bestpay.model.RefundResponse;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.service.OrderService;
import com.shy.wechatsell.service.PayService;
import com.shy.wechatsell.util.JsonUtil;
import com.shy.wechatsell.util.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Haiyu
 * @date 2018/10/25 18:59
 */
@Service
@Slf4j
public class PayServiceImpl implements PayService {

    private final String orderName = "微信点餐订单";

    @Autowired
    private BestPayServiceImpl bestPayService;
    @Autowired
    private OrderService orderService;

    @Override
    public PayResponse create(OrderDTO orderDTO) {
        PayRequest payRequest = new PayRequest();
        payRequest.setOpenid(orderDTO.getBuyerOpenid());
        payRequest.setOrderAmount(orderDTO.getOrderAmount().doubleValue());
        payRequest.setOrderId(orderDTO.getOrderId());
        payRequest.setOrderName(orderName);
        payRequest.setPayTypeEnum(BestPayTypeEnum.WXPAY_H5);
        log.info("【微信支付request】= {}", JsonUtil.toJson(payRequest));
        PayResponse payResponse = bestPayService.pay(payRequest);
        log.info("【微信支付response】= {}",JsonUtil.toJson(payResponse));
        return payResponse;
    }

    @Override
    public PayResponse notify(String notifyData) {
        // 1. 验证签名
        // 2. 检查支付状态
        // 3. 支付金额对比
        // 4. 支付人对比（下单人==支付人）
        // 1、2的检验best-pay-sdk已经帮我们做好了

        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("【微信异步回调通知】response={}",JsonUtil.toJson(payResponse));
        // 查询订单
        OrderDTO orderDTO = orderService.findOne(payResponse.getOrderId());
        if (orderDTO == null) {
            log.error("【微信支付通知】订单不存在；orderId={}", payResponse.getOrderId());
            throw new SellException(ResultEnum.ORDER_NOT_EXIST);
        }
        // 判断金额是否一致,一个是BigDecimal类型，一个是Double，由于可能存在精度的丢失，比如0.01变成0.0100000011001等
        // 所以可以假设如果两个金额相减小于0.01就认为金额相等
        if (!MathUtil.equals(orderDTO.getOrderAmount().doubleValue(),payResponse.getOrderAmount())) {
            log.error("【微信异步回调通知】orderId={}, 微信通知金额{}，系统金额{}",
                    orderDTO.getOrderId(),
                    payResponse.getOrderAmount(),
                    orderDTO.getOrderAmount());
            throw new SellException(ResultEnum.WECHAT_PAY_NOTIFY_MONEY_VERIFY_ERROR);
        }
        // 修改订单的支付状态
        orderService.paid(orderDTO);
        return payResponse;
    }

    @Override
    public RefundResponse refund(OrderDTO orderDTO) {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderAmount(orderDTO.getOrderAmount().doubleValue());
        refundRequest.setOrderId(orderDTO.getOrderId());
        refundRequest.setPayTypeEnum(BestPayTypeEnum.WXPAY_H5);
        log.info("【微信退款】request={}",JsonUtil.toJson(refundRequest));
        RefundResponse refundResponse = bestPayService.refund(refundRequest);
        log.info("【微信退款】response={}",JsonUtil.toJson(refundResponse));
        return refundResponse;
    }
}

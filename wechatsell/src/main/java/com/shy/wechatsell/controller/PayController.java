package com.shy.wechatsell.controller;

import com.lly835.bestpay.model.PayResponse;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.service.OrderService;
import com.shy.wechatsell.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author Haiyu
 * @date 2018/10/25 18:53
 */
@Controller
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PayService payService;

    @GetMapping("/create")
    public String create(@RequestParam("orderId") String orderId,
                        @RequestParam("returnUrl") String returnUrl,
                         Model model) {
        // 1. 查询订单
        OrderDTO orderDTO = orderService.findOne(orderId);
        if (orderDTO == null) {
            throw new SellException(ResultEnum.ORDER_NOT_EXIST);
        }
        // 2. 发起支付
        PayResponse payResponse = payService.create(orderDTO);
        model.addAttribute("payResponse",payResponse);
        model.addAttribute("returnUrl", returnUrl);
        return "pay/create";
    }

    /**
     * 微信异步回调通知，以及支付成功后通知微信
     * @param notifyData 支付后微信携带的xml数据，故用@RequestBody
     * @return
     */
    @PostMapping("/notify")
    public String notify(@RequestBody String notifyData) {
        payService.notify(notifyData);
        return "pay/success";
    }
}

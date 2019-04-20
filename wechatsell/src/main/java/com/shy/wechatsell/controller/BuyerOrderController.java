package com.shy.wechatsell.controller;

import com.shy.wechatsell.converter.OrderForm2OrderDTO;
import com.shy.wechatsell.dataobject.OrderDetail;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.form.OrderForm;
import com.shy.wechatsell.service.BuyerService;
import com.shy.wechatsell.service.OrderService;
import com.shy.wechatsell.util.ResultVOUtil;
import com.shy.wechatsell.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Haiyu
 * @date 2018/10/19 8:43
 */
@RestController
@RequestMapping("/buyer/order")
@Slf4j
public class BuyerOrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private BuyerService buyerService;

    @PostMapping("/create")
    public ResultVO<Map<String,String>> create(@Valid OrderForm orderForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new SellException(ResultEnum.ORDER_PARAM_ERROR.getCode(),bindingResult.getFieldError().getDefaultMessage());
        }
        OrderDTO orderDTO = OrderForm2OrderDTO.convert(orderForm);
        if (CollectionUtils.isEmpty(orderDTO.getOrderDetailList())) {
            log.error("【创建订单】购物车不能为空");
            throw new SellException(ResultEnum.ORDER_CART_EMPTY);
        }
        OrderDTO orderDTOResult = orderService.create(orderDTO);
        Map<String,String> data = new HashMap<>(1);
        data.put("orderId", orderDTOResult.getOrderId());
        return ResultVOUtil.success(data);
    }

    @GetMapping("/list")
    @Cacheable(cacheNames = "order",key="#openid", condition = "#openid.length() > 1", unless = "#result.getCode() != 0")
    public ResultVO<List<OrderDTO>> list(@RequestParam("openid") String openid,
                                            @RequestParam(value = "page",defaultValue = "0") Integer page,
                                            @RequestParam(value = "size",defaultValue = "10") Integer size) {
        if (StringUtils.isEmpty(openid)) {
            log.error("【查询订单】错误，openid不能为空");
            throw new SellException(ResultEnum.ORDER_PARAM_ERROR);
        }
        PageRequest pageRequest = PageRequest.of(page,size);
        Page<OrderDTO> orderDTOPage = orderService.findList(openid, pageRequest);
        // 查询到的订单中，订单详情不是必须的，orderDetailList却一致显示null，如果不想显示，可以不对空字段进行序列化
        // 这样返回到前端的数据就没有orderDetailList这个字段了
        return ResultVOUtil.success(orderDTOPage.getContent());
    }

    @GetMapping("/detail")
    public ResultVO<OrderDTO> detail(@RequestParam("openid") String openid,
                                     @RequestParam("orderId") String orderId) {
        OrderDTO orderDTO = buyerService.findOrderOne(openid,orderId);
        return ResultVOUtil.success(orderDTO);
    }

    @PostMapping("/cancel")
    public ResultVO<OrderDTO> cancel(@RequestParam("openid") String openid,
                                     @RequestParam("orderId") String orderId) {
        OrderDTO orderDTO = buyerService.findOrderOne(openid,orderId);
        OrderDTO result = orderService.cancel(orderDTO);
        return ResultVOUtil.success(result);
    }

}

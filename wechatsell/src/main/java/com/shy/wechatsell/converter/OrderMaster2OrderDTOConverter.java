package com.shy.wechatsell.converter;

import com.shy.wechatsell.dataobject.OrderMaster;
import com.shy.wechatsell.dto.OrderDTO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Haiyu
 * @date 2018/10/18 10:30
 */
public class OrderMaster2OrderDTOConverter {
    public static OrderDTO convert(OrderMaster orderMaster) {
        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(orderMaster, orderDTO);
        return orderDTO;
    }
    public static List<OrderDTO> convertList(List<OrderMaster> orderMasterList) {
        return orderMasterList.stream()
                .map(OrderMaster2OrderDTOConverter::convert)
                .collect(Collectors.toList());
    }
}

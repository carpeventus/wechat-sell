package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.OrderMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Haiyu
 * @date 2018/10/16 13:06
 */
public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {
    /**
     * 根据微信openid查找某个用户的所有订单
     *
     * @param buyerOpenid 买家微信openid
     * @param pageable
     * @return 分页
     */
    Page<OrderMaster> findByBuyerOpenid(String buyerOpenid, Pageable pageable);
}

package com.shy.wechatsell.service;

import com.shy.wechatsell.dataobject.SellerInfo;

/**
 * @author Haiyu
 * @date 2018/10/29 16:35
 */
public interface SellerService {
    SellerInfo findSellerInfoByOpenid(String openid);
}

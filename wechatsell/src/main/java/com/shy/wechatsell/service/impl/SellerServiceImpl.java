package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.SellerInfo;
import com.shy.wechatsell.repository.SellerInfoRepository;
import com.shy.wechatsell.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Haiyu
 * @date 2018/10/29 16:35
 */
@Service
public class SellerServiceImpl implements SellerService {
    @Autowired
    private SellerInfoRepository repository;

    @Override
    public SellerInfo findSellerInfoByOpenid(String openid) {
        return repository.findByOpenid(openid);
    }
}

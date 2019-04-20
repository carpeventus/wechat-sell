package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.dto.CartDTO;
import com.shy.wechatsell.enums.ProductStatusEnum;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.repository.ProductInfoRepository;
import com.shy.wechatsell.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Haiyu
 * @date 2018/10/15 17:35
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductInfoRepository repository;

    @Override
    @Cacheable(cacheNames = "product",key = "#productId")
    public ProductInfo findOne(String productId) {
        Optional<ProductInfo> optionalProductInfo = repository.findById(productId);
        if (!optionalProductInfo.isPresent()) {
            throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
        }
        return optionalProductInfo.get();
    }

    @Override
    public List<ProductInfo> findUpAll() {
        return repository.findByProductStatus(ProductStatusEnum.UP.getCode());
    }

    @Override
    public Page<ProductInfo> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @CachePut(cacheNames = "product",key = "#result.productId")
    public ProductInfo save(ProductInfo productInfo) {
        return repository.save(productInfo);
    }

    @Override
    public void increaseStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO :cartDTOList) {
            ProductInfo productInfo = repository.findById(cartDTO.getProductId()).get();
            Integer result = productInfo.getProductStock() + cartDTO.getProductQuantity();
            productInfo.setProductStock(result);
            repository.save(productInfo);
        }
    }

    @Override
    public void decreaseStock(List<CartDTO> cartDTOList) {
        for (CartDTO cartDTO : cartDTOList) {
            ProductInfo productInfo = repository.findById(cartDTO.getProductId()).get();
            Integer result = productInfo.getProductStock() - cartDTO.getProductQuantity();
            if (result < 0) {
                throw new SellException(ResultEnum.PRODUCT_STOCK_ERROR);
            }
            productInfo.setProductStock(result);
            repository.save(productInfo);
        }
    }

    @Override
    public ProductInfo onSale(String productId) {
        Optional<ProductInfo> optionalProductInfo = repository.findById(productId);
        if (!optionalProductInfo.isPresent()) {
            throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
        }
        ProductInfo productInfo = optionalProductInfo.get();
        productInfo.setProductStatus(ProductStatusEnum.UP.getCode());
        ProductInfo result = repository.save(productInfo);
        return result;
    }

    @Override
    public ProductInfo offSale(String productId) {
        Optional<ProductInfo> optionalProductInfo = repository.findById(productId);
        if (!optionalProductInfo.isPresent()) {
            throw new SellException(ResultEnum.PRODUCT_NOT_EXIST);
        }
        ProductInfo productInfo = optionalProductInfo.get();
        productInfo.setProductStatus(ProductStatusEnum.DOWN.getCode());
        ProductInfo result = repository.save(productInfo);
        return result;
    }
}

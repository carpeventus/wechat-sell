package com.shy.wechatsell.controller;

import com.shy.wechatsell.dataobject.ProductCategory;
import com.shy.wechatsell.dataobject.ProductInfo;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.form.ProductForm;
import com.shy.wechatsell.service.CategoryService;
import com.shy.wechatsell.service.ProductService;
import com.shy.wechatsell.util.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/28 14:54
 */

@Controller
@Slf4j
@RequestMapping("/seller/product")
public class SellerProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                       Model model) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<ProductInfo> productInfoPage = productService.findAll(pageRequest);
        model.addAttribute("productInfoPage", productInfoPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);

        return "product/list";
    }

    @GetMapping("/on_sale")
    public String onSale(@RequestParam("productId") String productId,
                         Model model) {
        try {
            productService.onSale(productId);
        } catch (SellException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("url", "/sell/seller/product/list");
            return "common/error";
        }
        model.addAttribute("url", "/sell/seller/product/list");
        return "common/success";
    }

    @GetMapping("/off_sale")
    public String offSale(@RequestParam("productId") String productId,
                         Model model) {
        try {
            productService.offSale(productId);
        } catch (SellException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("url", "/sell/seller/product/list");
            return "common/error";
        }
        model.addAttribute("url", "/sell/seller/product/list");
        return "common/success";
    }

    @GetMapping("/index")
    public String index(@RequestParam(value = "productId",required = false) String productId,
                        Model model) {

        if (!StringUtils.isEmpty(productId)) {
            ProductInfo productInfo = productService.findOne(productId);
            model.addAttribute("productInfo", productInfo);
        }

        List<ProductCategory> categoryList = categoryService.findAll();
        model.addAttribute("categoryList",categoryList);
        return "product/index";
    }

    @PostMapping("/save")
//    加了该注解的方法会每次都执行，方法返回后更新都清除缓存，那么买家查询时候就从数据库中查到最新的，再将最新的设置会redis中
//    @CacheEvict(cacheNames = "product",key = "123")
    public String save(@Valid ProductForm form, BindingResult bindingResult,Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("msg", bindingResult.getFieldError().getDefaultMessage());
            model.addAttribute("url", "/sell/seller/product/index");
            return "common/error";
        }
        try {
            ProductInfo productInfo = null;
            // 如果productId为空说明是新增
            if (StringUtils.isEmpty(form.getProductId())) {
                form.setProductId(KeyUtil.genUniqueKey());
                productInfo = new ProductInfo();
                // 否则是更新，需要先查询再更新
            } else {
                productInfo = productService.findOne(form.getProductId());
            }
            BeanUtils.copyProperties(form, productInfo);
            productService.save(productInfo);
        } catch (SellException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("url", "/sell/seller/product/index");
            return "common/error";
        }
        model.addAttribute("url","/sell/seller/product/list");
        return "common/success";
    }
}

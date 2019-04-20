package com.shy.wechatsell.controller;

import com.shy.wechatsell.config.ProjectUrlConfig;
import com.shy.wechatsell.constant.CookieConstant;
import com.shy.wechatsell.constant.RedisConstant;
import com.shy.wechatsell.dataobject.SellerInfo;
import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.service.SellerService;
import com.shy.wechatsell.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 卖家用户
 * @author Haiyu
 * @date 2018/10/29 20:41
 */
@Controller
@RequestMapping("/user")
public class SellerUserController {
    @Autowired
    private SellerService sellerService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProjectUrlConfig projectUrlConfig;
    @GetMapping("/login")
    public String login(@RequestParam("openid") String openid, Model model, HttpServletResponse response) {
        // 1. openid和数据库中的openid匹配
        SellerInfo sellerInfo = sellerService.findSellerInfoByOpenid(openid);
        if (sellerInfo == null) {
            model.addAttribute("msg", ResultEnum.LOGIN_FAILED.getMsg());
            model.addAttribute("url", "/sell/seller/order/list");
            return "common/error";
        }
        // 2. 设置token至redis
        String token = UUID.randomUUID().toString();
        Integer expire = RedisConstant.EXPIRE;
        stringRedisTemplate.opsForValue().set(String.format(RedisConstant.TOKEN_PREFIX,token), openid, expire, TimeUnit.SECONDS);
        // 3. 设置token至cookie
        CookieUtil.set(response, CookieConstant.TOKEN, token, expire);
        // 如何校验用户呢？从cookie里面取出token和其value值，从redis中查询得到openid，如果有openid的话，说明用户已经登录了
        return "redirect:" + projectUrlConfig.getSell() + "/sell/seller/order/list";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         Model model) {
        Cookie cookie = CookieUtil.get(request, CookieConstant.TOKEN);
        if (cookie != null) {
            // 1.清除redis中的token
            stringRedisTemplate.opsForValue().getOperations().delete(String.format(RedisConstant.TOKEN_PREFIX,cookie.getValue()));

            // 2. 清除cookie
            CookieUtil.set(response,CookieConstant.TOKEN,null, 0);
        }

        model.addAttribute("msg",ResultEnum.LOGOUT_SUCCESS.getMsg());
        model.addAttribute("url","/sell/seller/order/list");
        return "common/success";
    }

}

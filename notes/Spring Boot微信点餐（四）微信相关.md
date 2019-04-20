## SpringBoot微信点餐（四）微信相关

### 微信网页授权

使用第三方SDK可以大大简化微信相关的流程。pom.xml中添加依赖

```xml
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>weixin-java-mp</artifactId>
    <version>3.2.0</version>
</dependency>
```

申请一个[微信公众平台接口测试帐号](https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login)

手机扫码登录后可以获得一个公众平台测试号，测试信息如下

```
appID:wxcc07xxxxxxx
appsecret:91b6e0c4xxxxxxxxxxxxxxxxxxxxx
```

扫瞄下方的测试号二维码，添加关注，以便后序调试。

在下面的网页帐号 -> 网页授权获取用户基本信息中修改**授权回调域名**。

> 授权回调域名：用户在网页授权页同意授权给公众号后，微信会将授权数据传给一个回调页面，回调页面需在此域名下，以确保安全可靠。沙盒号回调地址支持域名和ip，正式公众号回调地址只支持域名。

因为本地调试使用的ip为127.0.0.1，这个地址手机是无法解析的。所以需要给其绑定一个域名，让手机对该域名的访问转发到localhost上。

去[NATAPP](https://natapp.cn)购买VIP_1型的隧道，由于微信屏蔽了三级域名，所以再花几块钱绑定一个二级域名，就可以用于微信调试了。

下载NATAPP的config.ini配置文件并复制购买后authtoken。此时可以打开NATAPP的客户端了，显示

```bash
Forwarding http://sunsell.natapp1.cc -> 127.0.0.1:8080
```

将购买的`sunsell.natapp1.cc`二级域名填入网页帐号 -> 网页授权获取用户基本信息即可。

在application.yml中配置微信公众平台的appId和appSecret

```yml
wechat:
  # 公众号的唯一标识
  mpAppId: wxcc07xxxxxxx
  # 公众号的唯一标识对应的密钥
  mpAppSecret: 91b6e0c4xxxxxxxxxxxxxxxxxxxxx
```

将该配置映射成实体类

```java
package com.shy.wechatsell.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Haiyu
 * @date 2018/10/25 10:22
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WxAccountConfig {
    private String mpAppId;
    private String mpAppSecret;
}

```

获取code和access_token及openid需要用到WxMpService、WxMpConfigStorage，引入上述微信公众平台的属性，在Java配置类中配置一下Bean。

```java
package com.shy.wechatsell.config;

import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Haiyu
 * @date 2018/10/25 10:06
 */
@Configuration
public class WechatMpConfig {
    @Autowired
    private WxAccountConfig wxAccountConfig;

    @Bean
    public WxMpService wxMpService() {
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage());
        return wxMpService;
    }

    @Bean
    public WxMpConfigStorage wxMpConfigStorage() {
        WxMpInMemoryConfigStorage wxMpInMemoryConfigStorage = new WxMpInMemoryConfigStorage();
        wxMpInMemoryConfigStorage.setAppId(wxAccountConfig.getMpAppId());
        wxMpInMemoryConfigStorage.setSecret(wxAccountConfig.getMpAppSecret());
        return wxMpInMemoryConfigStorage;
    }
}

```

上面配置了 WxMpService 的Bean，在Controller中直接引入。可以看到这个sdk大大简化可微信网页授权的流程。

```bash
1 第一步：用户同意授权，获取code

2 第二步：通过code换取网页授权access_token

3 第三步：刷新access_token（如果需要）

4 第四步：拉取用户信息(需scope为 snsapi_userinfo)
```

WechatController如下

```java
package com.shy.wechatsell.controller;

import com.shy.wechatsell.enums.ResultEnum;
import com.shy.wechatsell.exception.SellException;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Haiyu
 * @date 2018/10/25 10:01
 */

@Controller
@Slf4j
@RequestMapping("/wechat")
public class WechatController {
    @Autowired
    private WxMpService wxMpService;

    /**
     * 用户授权后获取code
     * @param returnUrl
     * @return
     */
    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl) {
        String url = "http://sunsell.natapp1.cc/sell/wechat/userInfo";
        String redirectUrl = null;
        try {
            redirectUrl = wxMpService.oauth2buildAuthorizationUrl(url, WxConsts.OAuth2Scope.SNSAPI_USERINFO, URLEncoder.encode(returnUrl, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * 根据code换区access_token、openid
     * @param code
     * @param returnUrl
     * @return
     */
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,
                         @RequestParam("state") String returnUrl) {
        WxMpOAuth2AccessToken wxMpOAuth2AccessToken = null;
        try {
            wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
        } catch (WxErrorException e) {
            log.error("【微信网页授权】{}", e);
            e.printStackTrace();
            throw new SellException(ResultEnum.WECHAT_MP_ERROR.getCode(),e.getError().getErrorMsg());
        }
        String openId = wxMpOAuth2AccessToken.getOpenId();
        return "redirect:" + returnUrl + "?openid=" + openId;
    }
}

```

由于NATAPP的功劳，在手机端访问下面这个网址

```html
http://sunsell.natapp1.cc/sell/wechat/authorize?returnUrl=http://www.baidu.com
```

将被转发到

```html
http://127.0.0.1:8080/sell/wechat/authorize?returnUrl=http://www.baidu.com
```

即由WechatController的/sell/wechat/authorize来处理。然后回重定向到/sell/wechat/userInfo处理，在该方法中可以获取到access_token和openid，最后会被重定向至returnUrl，且附带了openid参数。如下

```html
http://www.baidu.com?openid=xxxxxxxxxxx
```

最后访问wechatsell.com让其转发到 http://sunsell.natapp1.cc/sell/wechat/authorize?returnUrl=http://wechatsell.com/#/，获取到openid后最后重定向到 http://wechatsell.com/#/?openid=xxxx

### 微信支付

使用第三方SDK

```xml
<dependency>
    <groupId>cn.springboot</groupId>
    <artifactId>best-pay-sdk</artifactId>
    <version>1.1.0</version>
</dependency>
```

微信支付需要商户号的如下信息，给WechatAccountConfig增加几个字段

```java
/** 商户号id */
private String mchId;

/** 商户密钥 */
private String mchKey;

/** 商户证书地址 */
private String keyPath;

/** 微信支付异步通知地址 */
private String notifyUrl;
```

微信支付BestPayServiceImpl，主要是在WxPayH5Config中设置了WechatAccountConfig中的属性。

```java
package com.shy.wechatsell.config;

import com.lly835.bestpay.config.WxPayH5Config;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Haiyu
 * @date 2018/10/25 19:15
 */
@Configuration
public class WechatPayConfig {
    @Autowired
    private WechatAccountConfig wechatAccountConfig;

    @Bean
    public BestPayServiceImpl bestPayService() {
        BestPayServiceImpl bestPayService = new BestPayServiceImpl();
        bestPayService.setWxPayH5Config(wxPayH5Config());
        return bestPayService;
    }

    @Bean
    public WxPayH5Config wxPayH5Config() {
        WxPayH5Config wxPayH5Config = new WxPayH5Config();
        wxPayH5Config.setAppId(wechatAccountConfig.getMpAppId());
        wxPayH5Config.setAppSecret(wechatAccountConfig.getMpAppSecret());
        wxPayH5Config.setMchId(wechatAccountConfig.getMchId());
        wxPayH5Config.setMchKey(wechatAccountConfig.getMchKey());
        wxPayH5Config.setKeyPath(wechatAccountConfig.getKeyPath());
        wxPayH5Config.setNotifyUrl(wechatAccountConfig.getNotifyUrl());
        return wxPayH5Config;
    }

}

```

配置完成，就然后可以下订单了。传入PayRequest，需要订单号、openid、总金额等参数，返回PayResponse对象。

```java
package com.shy.wechatsell.service.impl;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.service.PayService;
import com.shy.wechatsell.util.JsonUtil;
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
}

```

看一下打印的日志

```bash
【微信支付request】= {
  "payTypeEnum": "WXPAY_H5",
  "orderId": "1539766989014790623",
  "orderAmount": 0.01,
  "orderName": "微信点餐订单",
  "openid": "oTgZpwTgorulVMHTszCrbA63vVFQ"
}
【微信支付response】= {
  "appId": "wxd898fcb01713c658",
  "timeStamp": "1540522160",
  "nonceStr": "vKGfbITyc0ywiqfc",
  "packAge": "prepay_id\u003dwx26104921241168c01207d9a43772003520",
  "signType": "MD5",
  "paySign": "080A26BB54E158918F9713577FB92241"
}

```

使用的是freemarker模板，微信支付的示例代码填入pay/create.ftl中

```javascript
<script>
    function onBridgeReady() {
        WeixinJSBridge.invoke(
                'getBrandWCPayRequest', {
                    "appId": "${payResponse.appId}",     //公众号名称，由商户传入
                    "timeStamp": "${payResponse.timeStamp}",         //时间戳，自1970年以来的秒数
                    "nonceStr": "${payResponse.nonceStr}", //随机串
                    "package": "${payResponse.packAge}",
                    "signType": "${payResponse.signType}",         //微信签名方式：
                    "paySign": "${payResponse.paySign}" //微信签名
                },
                function (res) {
                    // if (res.err_msg == "get_brand_wcpay_request:ok") {
                        // 使用以上方式判断前端返回,微信团队郑重提示：
                        //res.err_msg将在用户支付成功后返回ok，但并不保证它绝对可靠。
                    // }
                    // 刷新到该页面
                    location.href = "${returnUrl}"
                });
    }

    if (typeof WeixinJSBridge == "undefined") {
        if (document.addEventListener) {
            document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
        } else if (document.attachEvent) {
            document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
            document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
        }
    } else {
        onBridgeReady();
    }
</script>
```

然后在controller中，返回该视图

```java
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
}

```

当访问http://sunsell.natapp1.cc/sell/pay/create?orderId=1539766989014790623&returnUrl=http://www.baidu.com时，支付完成后将跳转到百度。

支付后，订单支付状态还没有改变。需要异步通知，在异步通知中修改订单支付状态。

#### 微信异步通知

PayService中

```java
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
```

在application.yml中我们配置过这个异步通知路径了，如下

```yml
notifyUrl: http://sunsell.natapp1.cc/sell/pay/notify
```

在PayController中调用，可以看到地址就是上面配置的sell/pay/notify

```java
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
```

notifyData的内容类似下面这样，因此参数采用了@RequestBody。

```xml
<xml>
  <appid><![CDATA[wx2421b1c4370ec43b]]></appid>
  <attach><![CDATA[支付测试]]></attach>
  <bank_type><![CDATA[CFT]]></bank_type>
  <fee_type><![CDATA[CNY]]></fee_type>
  <is_subscribe><![CDATA[Y]]></is_subscribe>
  <mch_id><![CDATA[10000100]]></mch_id>
  <nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str>
  <openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid>
  <out_trade_no><![CDATA[1409811653]]></out_trade_no>
  <result_code><![CDATA[SUCCESS]]></result_code>
  <return_code><![CDATA[SUCCESS]]></return_code>
  <sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign>
  <sub_mch_id><![CDATA[10000100]]></sub_mch_id>
  <time_end><![CDATA[20140903131540]]></time_end>
  <total_fee>1</total_fee>
<coupon_fee_0><![CDATA[10]]></coupon_fee_0>
<coupon_count><![CDATA[1]]></coupon_count>
<coupon_type><![CDATA[CASH]]></coupon_type>
<coupon_id><![CDATA[10000]]></coupon_id> 
  <trade_type><![CDATA[JSAPI]]></trade_type>
  <transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id>
</xml>
```

> 支付完成后，微信会把相关支付结果和用户信息发送给商户，商户需要接收处理，并返回应答。

需要返回的应答如下，这里以视图success.ftl的形式返回。

```xml
<xml>
    <return_code><![CDATA[SUCCESS]]></return_code>
    <return_msg><![CDATA[OK]]></return_msg>
</xml>
```

#### 微信退款

PayService中，需要填入总金额和订单号

```java
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
```

在之前取消订单的逻辑中，我们有个TODO还没做，现在可以把坑填上了。

```java
// 4.如果已经付款，需要退款
if (PayStatusEnum.SUCCESS.getCode().equals(orderDTO.getPayStatus())) {
    payService.refund(orderDTO);
}
```


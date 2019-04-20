## Spring Boot微信点餐（六）微信登录登出

### 分布式session

分布式系统：旨在支持用用程序和服务的开发，可以利用物理架构由多个自治的处理元素，不共享内存，但通过网络发送消息合作。

分布式和集群的区别与联系：分布式强调不同的功能模块的结点。而集群指的是相同业务功能的结点。分布式结点中的每个结点都可以作为集群。

为什么这个项目用到了redis？因为在分布式下，后端程序被部署到多个服务器上，一般分为水平扩展和垂直扩展。水平扩展就相当于做了一个集群；垂直扩展其实就是拆分服务，不同的服务器部署不同的服务，后端程序的功能被拆分到不同服务器上。在 用户登录时，使用的服务器A，再访问其他服务时，是由服务器B进行处理的，而服务器B并没有保存用户的session信息，导致用户无法访问该服务。因此需要一个统一管理session信息的服务，当其他服务需要访问session信息的时候，都去找该服务，这个服务通常是使用redis集群实现。如下图所示

![](http://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-29_23-45-26.png)

### 微信登录

卖家信息实体

```java
package com.shy.wechatsell.dataobject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Haiyu
 * @date 2018/10/29 16:23
 */
@Data
@Entity
public class SellerInfo {
    @Id
    private String id;
    private String username;
    private String password;
    private String openid;
}

```

卖家DAO层

```java
package com.shy.wechatsell.repository;

import com.shy.wechatsell.dataobject.SellerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Haiyu
 * @date 2018/10/29 16:26
 */
public interface SellerInfoRepository extends JpaRepository<SellerInfo,String> {
    SellerInfo findByOpenid(String openid);
}

```

SellerService实现了按照给定的openid查找对应的SellerInfo

```java
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

```

我们想要实现用户通过微信扫码登录，微信的登录授权使用的是微信开放平台，其appId和appSecret和微信公众号是不同的。微信公众号和开放平台都是用WxMpService，不过配置的appId和appSecret不同而已。配置如下bean，注意这个WxMpService的bean id是wxOpenService，前面也有一个WxMpService，其bean id是wxMpService。现在spring容器中同一个类有两个bean，只是注入的参数不同而已。还有一种情况是一个接口的不同实现类，当要注入到接口中时，spring怎么知道注入哪个实现类呢，这时也会发生自动装配冲突。

这些情况下，spring是怎么确定注入哪个bean呢？

```java
1、变量名必须和bean id一致，比如变量名使用wxMpService，对应着bean id为wxMpService的实例对象
2、使用@Qualifier("...")限定要注入的bean id
3、使用@Resource(name="...")限定要注入的bean id
```

回到正题，下面是wxOpenService的配置

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
 * @date 2018/10/29 16:46
 */
@Configuration
public class WechatOpenConfig {
    @Autowired
    private WechatAccountConfig wechatAccountConfig;

    @Bean
    public WxMpService wxOpenService() {
        WxMpService wxOpenService = new WxMpServiceImpl();
        wxOpenService.setWxMpConfigStorage(wxOpenConfigStorage());
        return wxOpenService;
    }
    @Bean
    public WxMpConfigStorage wxOpenConfigStorage() {
        WxMpInMemoryConfigStorage wxMpConfigStorage = new WxMpInMemoryConfigStorage();
        wxMpConfigStorage.setAppId(wechatAccountConfig.getOpenAppId());
        wxMpConfigStorage.setSecret(wechatAccountConfig.getOpenAppSecret());
        return wxMpConfigStorage;
    }
}

```

然后是登录的授权了，在WechatController中新增以下方法，和之前公众号的网页授权很类似。用户在访问登录链接时，诱导用户进入`/qrAuthorize?returnUrl=http://sunsell.natapp1.cc/sell/user/login`，

```java
@GetMapping("/qrAuthorize")
public String qrAuthorize(@RequestParam("returnUrl") String returnUrl) {
    String url = projectUrlConfig.getWechatOpenAuthorize()+"/sell/wechat/qrUserInfo";
    String redirectUrl = null;
    try {
        redirectUrl = wxOpenService.buildQrConnectUrl(url, WxConsts.QrConnectScope.SNSAPI_LOGIN,URLEncoder.encode(returnUrl,"UTF-8"));
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
    return "redirect:" + redirectUrl;
}

@GetMapping("/qrUserInfo")
public String qrUserInfo(@RequestParam("code") String code,
                         @RequestParam("state") String returnUrl) {
    WxMpOAuth2AccessToken wxMpOAuth2AccessToken = null;
    try {
        wxMpOAuth2AccessToken = wxOpenService.oauth2getAccessToken(code);
    } catch (WxErrorException e) {
        log.error("【微信网页授权】{}", e);
        e.printStackTrace();
        throw new SellException(ResultEnum.WECHAT_MP_ERROR.getCode(),e.getError().getErrorMsg());
    }
    String openId = wxMpOAuth2AccessToken.getOpenId();
    return "redirect:" + returnUrl + "?openid=" + openId;
}
```

扫码后获取到openid，然后重定向到`http://sunsell.natapp1.cc/sell/user/login?openid=xxxxxxxx`

`/login`在SellerUserController中

```java
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

}

```

其实扫码登录就是为了获取到扫码者的openid，拿这个openid和数据库中的卖家进行匹配，如果数据库中不存在这个openid，说明他不是卖家，没有权限登录。注意这里考虑到分布式session，使用到redis。如果openid存在，说明该扫码者确实是卖家，可以登录。为了在后续请求中保持用户的信息，跟踪用户的行为，登录后的操作应该在会话（session）中完成，如何验证用户信息呢？这里我们生成一个token存到redis中，同时将这个token保存到cookie中，在验证用户信息时，从cookie里面取出token和其value值，从redis中根据token查询得到，如果存在对应的openid，说明用户已经登录了。

上面一些常量抽取出来了

关于Redis的一些常量

```java
package com.shy.wechatsell.constant;

/**
 * @author Haiyu
 * @date 2018/10/29 21:29
 */
public interface RedisConstant {
    /** 过期时间2小时 */
    Integer EXPIRE = 7200;

    /** Token前缀，%s供String.format格式化 */
    String TOKEN_PREFIX = "token_%s";
}

```

关于Cookie的一些常量

```java
package com.shy.wechatsell.constant;

/**
 * Cookie相关常量
 * @author Haiyu
 * @date 2018/10/29 22:00
 */
public interface CookieConstant {
    String TOKEN = "token";
    Integer MAX_AGE = 7200;
}

```

Cookie的set和get也写了一个工具类

```java
package com.shy.wechatsell.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Haiyu
 * @date 2018/10/29 21:56
 */
public class CookieUtil {
    /**
     * 设置cookie
     * @param httpServletResponse
     * @param name
     * @param value
     * @param maxAge
     */
    public static void set(HttpServletResponse httpServletResponse,
                           String name,
                           String value,
                           Integer maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        httpServletResponse.addCookie(cookie);
    }

    /**
     * 获取cookie
     * @param request
     * @param name
     * @return
     */
    public static Cookie get(HttpServletRequest request, String name) {
        Map<String, Cookie> cookieMap = readCookieMap(request);
        if (cookieMap.containsKey(name)) {
            return readCookieMap(request).get(name);
        }
        return null;
    }

    /**
     * 将cookie封装成map
     * @param request
     * @return
     */
    public static Map<String,Cookie> readCookieMap(HttpServletRequest request) {
        Map<String,Cookie> cookieMap = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies).forEach(cookie -> cookieMap.put(cookie.getName(), cookie));
        }
        return cookieMap;
    }
}

```

### 微信登出

登出就很简单了，只需要将携带token的cookie清空，同时将token从Redis中删除即可。没有了这些信息，浏览器就没有了继续访问的“令牌”，服务器验证不通过，用户就需要重新登录啦。

```java
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
```

清除cookie要注意，不能直接在原cookie上直接setMaxAge，一般这样做清除cookie都是会失败的。正确的做法是，新建一个Cookie去覆盖原来的Cookie，但是怎么确定要覆盖的是到底是哪个cookie，因此还需要同时设置path，domain等信息，这样就能定位到想要覆盖的cookie，最后记得将cookie设置到响应中即response.addCookie

 `CookieUtil.set`正是上面的做法，如下

```java
 CookieUtil.set(response,CookieConstant.TOKEN,null, 0);

// CookieUtil的set方法
public static void set(HttpServletResponse httpServletResponse,
                           String name,
                           String value,
                           Integer maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        httpServletResponse.addCookie(cookie);
    }
```

### AOP实现身份验证

像订单列表，商品上下架等操作，必须在用户登录后才能进行操作。可以使用AOP实现用户的身份验证，即在访问卖家端操作的地址之前，必须先验证cookie，如果没有携带了token的cookie，或者携带token 的cookie在redis中查找不到，说明该访问者还没有登录。产生异常后，在捕获异常中重定向到登录界面。

```java
package com.shy.wechatsell.aspect;

import com.shy.wechatsell.constant.CookieConstant;
import com.shy.wechatsell.constant.RedisConstant;
import com.shy.wechatsell.exception.SellerAuthorizeException;
import com.shy.wechatsell.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 卖家登录授权切面
 * @author Haiyu
 * @date 2018/10/30 14:56
 */
@Aspect
@Component
@Slf4j
public class SellerAuthorizeAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 除了SellerUserController（因为涉及登录登出），controller包下所有Seller开头的Controller中的所有方法都要经过验证
    @Pointcut("execution(public * com.shy.wechatsell.controller.Seller*.*(..)) && !execution(public * com.shy.wechatsell.controller.SellerUserController.*(..))")
    public void verify() {}

    @Before("verify()")
    public void doVerify() {
        // 1. 为了拿到cookie，需要得到request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 2. 拿到cookie
        Cookie cookie = CookieUtil.get(request, CookieConstant.TOKEN);
        if (cookie == null) {
            log.warn("【登录校验】Cookie中查不到token");
            throw new SellerAuthorizeException();
        }
        // 3. 去redis查询
        String tokenValue = stringRedisTemplate.opsForValue().get(String.format(RedisConstant.TOKEN_PREFIX,cookie.getValue()));
        if (StringUtils.isEmpty(tokenValue)) {
            log.warn("【登录校验】Redis中查不到token");
            throw new SellerAuthorizeException();
        }

    }

}

```

除了SellerUserController（因为这里面涉及到卖家的登录和登出），其他所有Seller开头的Controller都必须在访问前验证cookie。

捕获到的SellerAuthorizeException异常，统一由ExceptionHandler处理

```java
package com.shy.wechatsell.handler;

import com.shy.wechatsell.config.ProjectUrlConfig;
import com.shy.wechatsell.exception.SellerAuthorizeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Haiyu
 * @date 2018/10/30 15:16
 */
@ControllerAdvice
public class SellerExceptionHandler {
    @Autowired
    private ProjectUrlConfig projectUrlConfig;

    // 拦截登录异常，若发生异常，说明用户未登录，则引导用户重新登录
    @ExceptionHandler(SellerAuthorizeException.class)
    public String handlerAuthorizeException() {
        return "redirect:"+
                projectUrlConfig.getWechatOpenAuthorize()+
                "/sell/wechat/qrAuthorize"+
                "?returnUrl="+
                projectUrlConfig.getSell()+
                "/sell/user/login";
    }
}

```

SellerAuthorizeException只是一个表示，里面没有任何内容。

```java
package com.shy.wechatsell.exception;

/**
 * @author Haiyu
 * @date 2018/10/30 15:11
 */
public class SellerAuthorizeException extends RuntimeException{
}

```

`@ControllerAdvice`是一个`@Component`，用于定义`@ExceptionHandler`，`@InitBinder`和`@ModelAttribute`方法，适用于所有使用`@RequestMapping`方法。

**一定要注意，登录也使用外网地址sunsell.natapp1.cc，因为login成功后重定向的地址就是外网地址。如果一开始使用localhost登录，重定向到sunsell.natapp1.cc后cookie就丢失了，因为cookie不可跨域**。

### 微信模板消息推送

我们想在卖家完结订单时，通过微信给买家推送一条消息。

```java
package com.shy.wechatsell.service.impl;

import com.shy.wechatsell.config.WechatAccountConfig;
import com.shy.wechatsell.dto.OrderDTO;
import com.shy.wechatsell.service.PushMesageService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Haiyu
 * @date 2018/10/30 15:45
 */
@Service
@Slf4j
public class PushMessageServiceImpl implements PushMesageService {
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private WechatAccountConfig wechatAccountConfig;

    @Override
    public void orderStatus(OrderDTO orderDTO) {
        WxMpTemplateMessage wxMpTemplateMessage = new WxMpTemplateMessage();
        // 设置微信模板消息id
        wxMpTemplateMessage.setTemplateId(wechatAccountConfig.getTemplateId().get("orderStatus"));
        // 设置openid
        wxMpTemplateMessage.setToUser(wechatAccountConfig.getOpenAppId());
        // first,keyword1...remark这些是模板定义的name
        List<WxMpTemplateData> data = Arrays.asList(
                new WxMpTemplateData("first", "亲，请记得收货。"),
                new WxMpTemplateData("keyword1", "微信点餐"),
                new WxMpTemplateData("keyword2", "18868812345"),
                new WxMpTemplateData("keyword3", orderDTO.getOrderId()),
                new WxMpTemplateData("keyword4", orderDTO.getOrderStatusEnum().getMsg()),
                new WxMpTemplateData("keyword5", "￥" + orderDTO.getOrderAmount()),
                new WxMpTemplateData("remark", "欢迎再次光临！")
        );
        // 设置消息data
        wxMpTemplateMessage.setData(data);

        try {
            wxMpService.getTemplateMsgService().sendTemplateMsg(wxMpTemplateMessage);
        } catch (WxErrorException e) {
            // 这里捕获了异常但是并没有作处理，因为消息是可有可无了，不能因为消息推送失败影响整个事务
            log.error("【微信模板消息】发送失败，{}", e);
        }
    }
}

```

application.yml中配置的

```yml
wechat:
	templateId:
    	orderStatus: e-Cqq67QxD6YNI41iRiqawEYdFavW_7pc7LyEMb-yeQ
```

对应

```java
@ConfigurationProperties(prefix = "wechat")
// ...
/** 微信模版id */
private Map<String, String> templateId;
```

即orderStatus: xxxx这样的键值对对应Java中的Map。

然后在OrderServiceImpl中的finish()方法中，添加如下代码

```java
// 推送微信模板消息
pushMesageService.orderStatus(orderDTO);
```

买家客户端就能收到推送消息了，当然可以在订单create时候也推送，看需求了。

### WebSocket消息推送

我们希望在订单页面，当有买家下了订单时候，卖家端能后收到消息，并且在收到消息后进行弹窗+音乐提醒。我们用WebSocket。

>WebSocket 是 HTML5 开始提供的一种在单个 TCP 连接上进行全双工通讯的协议。
>
>WebSocket 使得客户端和服务器之间的数据交换变得更加简单，允许服务端主动向客户端推送数据。在 WebSocket API 中，浏览器和服务器只需要完成一次握手，两者之间就直接可以创建持久性的连接，并进行双向数据传输。
>
>在 WebSocket API 中，浏览器和服务器只需要做一个握手的动作，然后，浏览器和服务器之间就形成了一条快速通道。两者之间就直接可以数据互相传送。

在order/list.ftl中，写如下的js，需要重点完成的是`WebSocket('ws://sunsell.natapp1.cc/sell/webSocket')`中的地址，以及当收到消息时候的弹窗和播放音乐逻辑。

```html
<script>
    var websocket = null;
    if ('WebSocket' in window) {
        websocket = new WebSocket('ws://sunsell.natapp1.cc/sell/webSocket');
    } else {
        alert('该浏览器不支持websocket!');
    }

    websocket.onopen = function (event) {
        console.log('建立连接');
    }

    websocket.onclose = function (event) {
        console.log('连接关闭');
    }

    websocket.onmessage = function (event) {
        console.log('收到消息:' + event.data);
        //弹窗提醒, 播放音乐
        $('#myModal').modal('show');
        document.getElementById('notice').play();
    };

    websocket.onerror = function () {
        alert('websocket通信发生错误！');
    }

    window.onbeforeunload = function () {
        websocket.close();
    }

</script>
```

首先在后端需要支持WebSocket

引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

注入到Spring容器中

```java
package com.shy.wechatsell.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author haiyu
 */
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}

```

接下来配置WebSocket服务器端，`@ServerEndpoint("/webSocket")`，表示客户端可以通过该URL与服务端通信，即上面的`ws://sunsell.natapp1.cc/sell/webSocket`

```java
package com.shy.wechatsell.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Haiyu
 * @date 2018/10/30 17:23
 */

/**
 * ServerEndpoint注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 * ServerEndpoint可以把当前类变成websocket服务类
 */
@Component
@ServerEndpoint("/webSocket")
@Slf4j
public class WebSocket {
    private Session session;

    private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();


    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        log.info("【websocket消息】有新的连接,总数={}",webSocketSet.size());
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        log.info("【websocket消息】连接断开,总数={}", webSocketSet.size());
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("【websocket消息】收到消息,message={}", message);
    }

    /**
     * 发送消息到给所有商家（这里只有一个商家，就简单的发给全部商家了）
     * @param message
     */
    public void sendMessage(String message) {
        for (WebSocket webSocket: webSocketSet) {
            log.info("【websocket消息】广播消息,message={}",message);
            try {
                webSocket.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

```

关键是sendMessage方法，可以给浏览器发送消息。

我们在创建订单时，将消息推送给浏览器。在OrderServiceImpl的create方法中新增

```java
// 发送websocket消息
webSocket.sendMessage("您有新的订单,orderId="+orderId);
```

order/list.ftl中，收到消息到会触发

```java
websocket.onmessage = function (event) {
        console.log('收到消息:' + event.data);
        //弹窗提醒, 播放音乐
        $('#myModal').modal('show');
        document.getElementById('notice').play();
    };
```

弹窗和音乐的实现如下，注意要导入jquery和bootstrap的js。弹窗的查看订单按钮就是刷新当前页面（订单列表），而关闭同时关闭了音乐的播放。

```html
<#--弹窗-->
<div class="modal fade" id="myModal" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="myModalLabel">
                    提醒
                </h4>
            </div>
            <div class="modal-body">
                您有新的订单
            </div>
            <div class="modal-footer">
                <button onclick="javascript:document.getElementById('notice').pause()" type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                <button onclick="location.reload()" type="button" class="btn btn-primary">查看订单</button>
            </div>
        </div>

    </div>
</div>

<#--播放音乐-->
<audio id="notice" loop="loop">
    <source src="/sell/mp3/song.mp3">
</audio>
<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<script src="https://cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
```

当买家下单，会给浏览器发送一个websocket消息，效果如图

![](http://picmeup.oss-cn-hangzhou.aliyuncs.com/coding/Snipaste_2018-10-30_21-36-21.png)




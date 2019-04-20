package com.shy.wechatsell.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Haiyu
 * @date 2018/10/25 10:22
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatAccountConfig {
    /** 公众平台开发者账号id */
    private String mpAppId;

    /** 开发者账号对应的密钥 */
    private String mpAppSecret;

    /** 商户号id */
    private String mchId;

    /** 商户密钥 */
    private String mchKey;

    /** 商户证书地址 */
    private String keyPath;

    /** 微信支付异步通知地址 */
    private String notifyUrl;

    /** 开发平台账号 */
    private String openAppId;

    /** 开发平台账号对应的密钥 */
    private String openAppSecret;

    /** 微信模版id */
    private Map<String, String> templateId;
}

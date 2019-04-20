package com.shy.wechatsell.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Haiyu
 * @date 2018/10/29 20:32
 */
@Data
@Component
@ConfigurationProperties(prefix = "project-url")
public class ProjectUrlConfig {
    /** 微信公众平台授权地址 */
    private String wechatMpAuthorize;

    /** 微信公开放平台授权地址 */
    private String wechatOpenAuthorize;

    /** 点餐项目的路径 */
    private String sell;
}

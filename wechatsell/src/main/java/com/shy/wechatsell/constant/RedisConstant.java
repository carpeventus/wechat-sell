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

package com.shy.wechatsell.util;

import java.util.Random;

/**
 * @author Haiyu
 * @date 2018/10/17 10:14
 */
public class KeyUtil {
    /**
     * 生成唯一主键
     * 格式：时间+随机数
     * 为了防止多线程下产生相同的key，就不能保证key的唯一性，所以加上同步
     * @return
     */
    public static synchronized String genUniqueKey() {
        Random random = new Random();
        // 100000~999999,六位随机数，不能使用random.nextInt(1000000)，这样不能保证一定是6位
        Integer number = random.nextInt(900000) + 100000;
        return System.currentTimeMillis() + String.valueOf(number);
    }
}

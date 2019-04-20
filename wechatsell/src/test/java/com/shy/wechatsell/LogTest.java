package com.shy.wechatsell;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Haiyu
 * @date 2018/10/14 15:09
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class LogTest {

    @Test
    public void logTest() {
        log.info("info info");
        log.error("error info");
        log.warn("warn info");
        log.debug("debug info");
        log.trace("trace info");
    }
}

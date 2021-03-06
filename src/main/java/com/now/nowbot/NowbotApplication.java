package com.now.nowbot;

import com.now.nowbot.config.FileConfig;
import com.now.nowbot.config.OSUConfig;
import com.now.nowbot.config.QQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({QQConfig.class, FileConfig.class, OSUConfig.class})
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@EnableScheduling
@EnableRetry
public class NowbotApplication {
    private static Logger log = LoggerFactory.getLogger(NowbotApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(NowbotApplication.class, args);
    }
}

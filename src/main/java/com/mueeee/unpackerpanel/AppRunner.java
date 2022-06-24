package com.mueeee.unpackerpanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Execute after the application started
 */
@Component
public class AppRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AppRunner.class);

    @Value("${server.port}")
    private int serverPort;

    @Value("${app.device}")
    private String appDevice;

    public static String APP_DEVICE = null;

    @PostConstruct
    public void init() {
        APP_DEVICE = appDevice;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 打印日志
        log.info("Server is running on 127.0.0.1:" + serverPort);
    }

}

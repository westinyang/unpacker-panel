package com.mueeee.unpackerpanel;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    // unpacker-panel
    // unpacker-panel.exe --server.port=8888
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.setBannerMode(Banner.Mode.OFF);       // 关闭Banner打印
        // springApplication.setAddCommandLineProperties(false);   // 禁用命令行参数设置
        springApplication.run(filterArgs(args));
    }

    /**
     * 过滤命令行参数，实现参数白名单效果
     * @param args 原始参数列表
     * @return
     */
    public static String[] filterArgs(String[] args) {
        if (args.length == 0) {
            return args;
        }
        List<String> finalArgs = new ArrayList<>();
        for (String arg : args) {
            // 服务端口号
            if (arg.startsWith("--server.port=")) {
                try {
                    Integer.parseInt(arg.split("=")[1]);
                    finalArgs.add(arg);
                } catch (Exception ignored) {
                }
            }
        }
        if (finalArgs.isEmpty()) {
            return new String[0];
        }
        return finalArgs.toArray(new String[0]);
    }

}

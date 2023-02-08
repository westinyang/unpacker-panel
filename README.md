# Unpacker Panel

## 项目介绍

基于Youpk脱壳机的一键脱壳Web面板

- 目前仅支持单个设备控制，不支持多设备并发和任务队列（如果并发会导致程序异常）  
- [关于脱壳失败和项目其他用途的探讨](https://github.com/westinyang/unpacker-panel/discussions/6)

## 使用说明

> 该工具仅仅用来学习交流, 请勿用于非法用途, 否则后果自付！

1. ADB环境
   - windows `自己配置好adb环境变量`
   - linux `sudo apt install android-tools-adb`
2. 电脑连接刷好Youpk的Pixel 1代，或者编译Youpk适配的其他安卓设备
3. 运行程序，不指定端口默认就是：8888
   - windows 直接打开或者指定端口运行
    ```batch
    unpacker-panel.exe
    unpacker-panel.exe --server.port=8888
    ```
   - linux
    ```shell
    chmod 777 unpacker-panel
    ./unpacker-panel
    ./unpacker-panel --server.port=8888
    ```
4. 浏览器访问 `http://ip:port`

## 开发技术

> - GraalVM让Java再次变得强大，使用native-image把程序编译为目标平台的可执行文件，脱离jvm直接运行，启动速度快，内存负载低。
> - 关于GraalVM技术实践，请参考我的另一个开源项目：[java-graalvm-start](https://github.com/westinyang/java-graalvm-start)

- 后端
    - GraalVM CE 22.1.0 (native-image) (based on Java 17)
    - SpringBoot 2.7
    - apk-parser
    - hutool
- 前端
    - jquery 3.5
    - bootstrap 5
    - bootstrap-fileinput
    - bootstrap-icons
    - clipboard
    - layer

## 项目截图

网页面板

![light.png](./screenshot/light.png)

后台日志

![log.png](./screenshot/log.png)

## 仓库地址

https://github.com/westinyang/unpacker-panel

## 技术交流

- 知识星球：[码力全开](https://docs.qq.com/doc/DQVlkcnlQUEFiQ3Rl)

## 写在最后

> 膜拜 Youlor 大佬提供的技术和思路，开发这个项目也是在学习技术和实践应用，底层由 Youpk 强力驱动！

- [Youpk: 又一款基于ART的主动调用的脱壳机](https://bbs.pediy.com/thread-259854.htm)

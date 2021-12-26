# Unpacker Panel

## 介绍
基于Youpk脱壳机的一键脱壳Web面板

> 目前仅支持单个设备控制，不支持并发和任务队列（如果并发会导致程序异常）  
> 暂不开源，大佬轻喷，发行版支持windows、linux，入群下载：190033401

## 说明

- adb `sudo apt install android-tools-adb`
- 电脑连接刷好Youpk的Pixel 1代，或者编译Youpk适配的其他安卓设备
- 运行程序，默认端口：8888（后台运行可以用 nohup / screen / tmux 等命令 ）
    ```batch
    unpacker-panel.exe
    unpacker-panel.exe --server.port=8888
    ```
- 浏览器访问 `http://ip:port`

## 截图

网页面板

![](./screenshot/light.png)

后台日志

![](./screenshot/log.png)

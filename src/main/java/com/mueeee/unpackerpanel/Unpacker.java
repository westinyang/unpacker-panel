package com.mueeee.unpackerpanel;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.mueeee.unpackerpanel.common.ApiResult;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Unpacker {

    private static final Logger log = LoggerFactory.getLogger(Unpacker.class);

    public static Process exec(String command, boolean isShell) throws IOException {
        StringBuilder sb = new StringBuilder("adb ");
        // 是否指定设备编号
        if (StrUtil.isNotEmpty(AppRunner.APP_DEVICE)) {
            sb.append(MessageFormat.format("-s {0} ", AppRunner.APP_DEVICE));
        }
        // 是否shell，加双引号
        if (isShell) {
            command = MessageFormat.format("shell \"{0}\"", command);
        }
        sb.append(command);
        // 完整命令
        String fullCommand = sb.toString();
        // 判断操作系统
        String commander = "";
        String arg1 = "";
        String osName = System.getProperty("os.name");
        if (Pattern.matches("Windows.*", osName)) {
            commander = "cmd.exe";
            arg1 = "/c";
        } else if (Pattern.matches("Linux.*", osName)) {
            commander = "/bin/sh";
            arg1 = "-c";
            // Linux上$符需要做特殊转义，把 $ 符号 替换为 \$
            fullCommand = fullCommand.replaceAll("\\$", "\\\\\\$");
        } else if (Pattern.matches("Mac.*", osName)) {
            // Mac暂未测试，先按照Linux的设置来
            commander = "/bin/sh";
            arg1 = "-c";
            // Linux上$符需要做特殊转义，把 $ 符号 替换为 \$
            fullCommand = fullCommand.replaceAll("\\$", "\\\\\\$");
        }
        // 最终命令
        String[] finalCommand = new String[] {commander,arg1, fullCommand};
        log.debug("{}", Arrays.toString(finalCommand));
        return Runtime.getRuntime().exec(finalCommand);
    }

    public static ApiResult unpack(String APK_PATH, ApkMeta apkMeta) {
        var APK_PACKAGE = apkMeta.getPackageName();

        var startTimeMillis = System.currentTimeMillis();
        log.info("====================================================================================================");
        log.info("任务开始");
        try {
            // 卸載App
//            log.info("卸載App");
            exec(MessageFormat.format("pm uninstall {0} > /dev/null 2>&1", APK_PACKAGE), true).waitFor();
            // 安裝App
            log.info("安裝App");
            exec(MessageFormat.format("install {0}", APK_PATH), false).waitFor();
            // 赋予权限
//            log.info("赋予权限");
            exec(MessageFormat.format("LIST=$(dumpsys package {0} | tr '' '' ''\\n'' | grep ''^android.permission.'' | grep -v '':$''); PERMS=$(pm list  permissions -d -g); for i in $LIST; do if echo $PERMS | grep -q $i; then echo grant $i; pm grant {1} $i; fi; done;", APK_PACKAGE, APK_PACKAGE), true).waitFor();
            // 配置包名
//            log.info("配置包名，{}", APK_PACKAGE);
            exec(MessageFormat.format("echo {0} > /data/local/tmp/unpacker.config", APK_PACKAGE), true).waitFor();
            // 清理日志
//            log.info("清理日志");
            exec("logcat -c", true).waitFor();
            // 打开App
            log.info("打开App");
            exec(MessageFormat.format("am start -S $(dumpsys package {0} | grep -A 1 android.intent.action.MAIN | tr '' '' ''\\n'' | grep ''/'')", APK_PACKAGE), true).waitFor();

            // 监听日志...
            log.info("监听日志...");
            var beginTime = System.currentTimeMillis();
            final AtomicBoolean isBegin = new AtomicBoolean(false);
            final AtomicBoolean isEnd = new AtomicBoolean(false);
            // 启动监听日志的线程
            Future<Integer> futureTask = ThreadUtil.execAsync(() -> {
                var process = exec("logcat -s unpacker", true);
                var bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                var line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    // log.debug("- " + line);
                    // [unpack:415]unpack begin! || [unpack:424]unpack end!
                    if (line.contains("]unpack begin!")) {
                        isBegin.set(true);
                        log.info("- 监听到脱壳开始日志，{}", line);
                    } else if (line.contains("]unpack end!")) {
                        isEnd.set(true);
                        log.info("- 监听到脱壳结束日志，{}", line);
                        break;
                    }
                }
                bufferedReader.close();
                return 1;
            });
            // 等待 begin，超时时间20秒
            while (true) {
                //noinspection BusyWait
                Thread.sleep(1000);
                if (!isBegin.get()) {
                    if ((System.currentTimeMillis() - beginTime) > 20 * 1000) {
                        futureTask.cancel(true);
                        break;
                    }
                } else {
                    break;
                }
            }
            // 如果等待到 begin，开始等待 end，重置开始时间，超时时间60秒，否则跳过
            if (isBegin.get()) {
                beginTime = System.currentTimeMillis();
                while (true) {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                    if (!isEnd.get()) {
                        if ((System.currentTimeMillis() - beginTime) > 60 * 1000) {
                            futureTask.cancel(true);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            // 日志监听结果最终判定
            log.info("监听结束，isBegin: {}，isEnd: {}", isBegin.get(), isEnd.get());
            if (!isBegin.get()) {
                ApiResult tmp = ApiResult.failure("脱壳失败，等待“unpack begin”日志超时（> 20s）");
                log.error(tmp.getMessage());
                interruptClean(APK_PACKAGE, startTimeMillis);
                return tmp;
            }
            if (!isEnd.get()) {
                ApiResult tmp = ApiResult.failure("脱壳失败，等待“unpack end”日志超时（> 60s）");
                log.error(tmp.getMessage());
                interruptClean(APK_PACKAGE, startTimeMillis);
                return tmp;
            }

            // 拉取dex
            var pullToPath = new File(APK_PATH).getParent() + File.separator + "unpacker";
            log.info("拉取dex");
            exec(MessageFormat.format("pull /data/data/{0}/unpacker {1}", APK_PACKAGE, pullToPath), false).waitFor();
            // 卸载App
            log.info("卸載App");
            exec(MessageFormat.format("pm uninstall {0} > /dev/null 2>&1", APK_PACKAGE), true).waitFor();
            // 清理dex
//            log.info("清理dex");
            exec(MessageFormat.format("rm -rf /data/data/{0}/unpacker", APK_PACKAGE), true);
            // 打印耗时
            var timeConsuming = (System.currentTimeMillis() - startTimeMillis) / 1000;
            log.info("任务结束，耗时：{}s", timeConsuming);

            return ApiResult.success();
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("任务异常，{}", e.getMessage());
            try {
                interruptClean(APK_PACKAGE, startTimeMillis);
            } catch (Exception ignored) {
            }
            return ApiResult.failure("任务异常：" + e.getMessage());
        }
    }

    private static void interruptClean(String APK_PACKAGE, long startTimeMillis) throws IOException, InterruptedException {
        // 卸载App
        log.info("卸載App");
        exec(MessageFormat.format("pm uninstall {0} > /dev/null 2>&1", APK_PACKAGE), true).waitFor();
        // 打印耗时
        var timeConsuming = (System.currentTimeMillis() - startTimeMillis) / 1000;
        log.info("任务结束，耗时：{}s", timeConsuming);
    }

}

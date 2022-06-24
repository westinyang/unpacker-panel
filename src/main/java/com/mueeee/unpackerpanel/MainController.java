package com.mueeee.unpackerpanel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.*;
import cn.hutool.extra.servlet.ServletUtil;
import com.mueeee.unpackerpanel.common.ApiResult;
import com.mueeee.unpackerpanel.common.MD5Util;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;

/**
 * Main Controller
 */
@Controller
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    // 根目录
    public static final String ROOT_DIR = new File("").getAbsolutePath();
    // 文件上传目录
    public static final String UPLOAD_DIR = ROOT_DIR + File.separator + "uploads";

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        Cookie theme = ServletUtil.getCookie(request, "theme");
        if (theme != null && StrUtil.isNotEmpty(theme.getValue())) {
            request.setAttribute("theme", theme.getValue());
        }

        // QQGroup num
        // System.out.println(Base64.getEncoder().encodeToString("190033401".getBytes(Charset.defaultCharset())));
        var QQGroupNum = new String(Base64.getDecoder().decode("MTkwMDMzNDAx"), Charset.defaultCharset());
        request.setAttribute("QQGroupNum", QQGroupNum);

        // QQGroup QRCode
        // System.out.println(Base64.getEncoder().encodeToString("/assets/img/qq-group.png".getBytes(Charset.defaultCharset())));
        var QQGroupQRCode = new String(Base64.getDecoder().decode("L2Fzc2V0cy9pbWcvcXEtZ3JvdXAucG5n"), Charset.defaultCharset());
        request.setAttribute("QQGroupQRCode", QQGroupQRCode);

        return "index";
    }

    @ResponseBody
    @PostMapping("/upload")
    public Object upload(MultipartFile file) {
        try {
            // 如果不存在则创建
            FileUtil.mkdir(UPLOAD_DIR);
            // 临时路径
            String tmpFilePath = UPLOAD_DIR + File.separator + "app.apk";
            // 保存文件
            File tmpFile = FileUtil.writeFromStream(file.getInputStream(), tmpFilePath);
            // 计算文件md5
            String tmpFileMd5 = MD5Util.md5(tmpFile);
            // 以文件md5值创建新文件夹
            String uploadFileDir = UPLOAD_DIR + File.separator + tmpFileMd5;
            FileUtil.mkdir(uploadFileDir);
            // tmp.apk 移动到 fileMd5Dir
            FileUtil.move(Paths.get(tmpFilePath), Paths.get(uploadFileDir), true);
            // 上传文件路径（最终）
            String uploadFilePath = uploadFileDir + File.separator + "app.apk";

            // 解析App
            log.info("====================================================================================================");
            log.info("解析App...");
            ApkFile apkFile = null;
            ApkMeta apkMeta = null;
            try {
                apkFile = new ApkFile(new File(uploadFilePath));
                apkMeta = apkFile.getApkMeta();
            } catch (Exception e) {
                log.error("App信息解析失败，{}", e.getMessage());
            } finally {
                if (apkFile != null) {
                    apkFile.close();
                }
            }
            if (apkFile == null || apkMeta == null || !StringUtils.hasLength(apkMeta.getPackageName())) {
                try {
                    FileUtil.del(uploadFileDir);
                } catch (Exception ignored) {
                }
                return ApiResult.failure("App信息解析失败");
            }
            log.info("解析成功，名字: {}, 包名: {}", apkMeta.getLabel(), apkMeta.getPackageName());

            // 脱壳任务
            var unpackBeginTime = System.currentTimeMillis();
            ApiResult taskResult = Unpacker.unpack(uploadFilePath, apkMeta);
            var unpackUseTime = (System.currentTimeMillis() - unpackBeginTime) / 1000;
            // 任务失败，删除文件目录，返回响应
            if (!taskResult.isSuccess()) {
                try {
                    FileUtil.del(uploadFileDir);
                } catch (Exception ignored) {
                }
                return taskResult;
            }

            // 压缩unpacker目录
            String unpackerDir = uploadFileDir + File.separator + "unpacker";
            if (!FileUtil.exist(unpackerDir)) {
                return ApiResult.failure("设备未连接或其他异常...");
            }
            ZipUtil.zip(unpackerDir);
            // 删除unpacker目录
            FileUtil.del(unpackerDir);
            // ...
            var name = MessageFormat.format("{0}-{1}.zip", apkMeta.getPackageName(), tmpFileMd5);
            var link = "/download/" + tmpFileMd5;
            return ApiResult.success(new HashMap<String, Object>(){{
                put("time", unpackUseTime); // 耗时
                put("name", name);          // 下载文件名
                put("link", link);          // 下载链接
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.failure(e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public void download(HttpServletResponse response, @PathVariable String id) {
        if (StrUtil.isEmpty(id)) {
            return;
        }

        var zipPath = UPLOAD_DIR + File.separator + id  + File.separator +  "unpacker.zip";
        if (!FileUtil.exist(zipPath)) {
            return;
        }

        try {
            var contentType = ObjectUtil.defaultIfNull(FileUtil.getMimeType(zipPath), "application/octet-stream");
            var fileName = MessageFormat.format("{0}.zip", id);
            ServletUtil.write(response, FileUtil.getInputStream(zipPath), contentType, fileName);
        } catch (Exception ignored) {
        }
    }

}

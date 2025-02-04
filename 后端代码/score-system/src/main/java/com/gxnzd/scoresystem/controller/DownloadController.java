package com.gxnzd.scoresystem.controller;

import com.gxnzd.scoresystem.utils.CommonResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class DownloadController {

    @GetMapping("/download")
    public CommonResult<String> download(@RequestParam String fileName, HttpServletResponse response) {
        // 从 resources 目录下加载文件
        Resource resource = new ClassPathResource("download/" + fileName + ".xlsx");
        if (!resource.exists()) {
            return new CommonResult<>(500, "文件不存在", null);
        }
        // 设置响应类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 设置编码格式
        response.setCharacterEncoding("utf-8");
        // 设置URLEncoder.encode 防止中文乱码
        fileName = URLEncoder.encode(fileName + "模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        // 设置响应头
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        // 获取文件输入流
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            // 从输入流读取数据，并写入响应输出流
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CommonResult<>(500, "下载失败", null);
    }

}

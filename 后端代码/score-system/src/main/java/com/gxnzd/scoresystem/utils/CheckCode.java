package com.gxnzd.scoresystem.utils;

import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CheckCode {

    private int CAPTCHA_WIDTH = 100;
    private int CAPTCHA_HEIGHT = 40;
    private String code;

    public String getCode() {
        return code;
    }

    public CheckCode(int CAPTCHA_WIDTH, int CAPTCHA_HEIGHT) {
        this.CAPTCHA_WIDTH = CAPTCHA_WIDTH;
        this.CAPTCHA_HEIGHT = CAPTCHA_HEIGHT;
    }

    public String generateCheckCode() {
        String base = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int size = base.length();
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= 4; i++) {
            //产生0到size-1的随机值
            int index = r.nextInt(size);
            //在base字符串中获取下标为index的字符
            char c = base.charAt(index);
            //将c放入到StringBuffer中去
            sb.append(c);
        }
        return sb.toString();
    }

    // 生成验证码图像并返回Base64编码
    public String generateCaptchaImageBase64() throws IOException {
        // 生成随机验证码字符
        String captchaText = generateCheckCode();
        this.code = captchaText;
        // 创建图片
        BufferedImage image = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // 设置图像背景色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);

        // 设置文本字体
        Font font = new Font("Arial", Font.BOLD, 20);
        graphics.setFont(font);

        // 设置文本颜色
        graphics.setColor(Color.BLACK);
        graphics.drawString(captchaText, 30, 30);

        // 关闭图形对象
        graphics.dispose();

        // 将图像写入ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);

        // 将ByteArrayOutputStream转换为Base64编码
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return "data:image/png;base64," + base64Image;
    }

}

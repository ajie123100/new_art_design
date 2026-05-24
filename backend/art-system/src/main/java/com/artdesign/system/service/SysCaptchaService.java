package com.artdesign.system.service;

import com.artdesign.common.exception.BusinessException;
import com.artdesign.system.config.CaptchaConfig;
import com.artdesign.system.domain.dto.CaptchaResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class SysCaptchaService {
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String IMAGE_PREFIX = "data:image/png;base64,";

    private final SecureRandom random = new SecureRandom();
    private final CaptchaConfig captchaConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysConfigService configService;

    public SysCaptchaService(CaptchaConfig captchaConfig, RedisTemplate<String, Object> redisTemplate, SysConfigService configService) {
        this.captchaConfig = captchaConfig;
        this.redisTemplate = redisTemplate;
        this.configService = configService;
    }

    public CaptchaResponse createCaptcha() {
        if (!isCaptchaEnabled()) {
            return new CaptchaResponse(false, null, null);
        }

        String uuid = UUID.randomUUID().toString();
        String content;
        String answer;

        if ("math".equals(captchaConfig.getType())) {
            MathExpression expression = generateMathExpression();
            content = expression.display;
            answer = String.valueOf(expression.result);
        } else {
            content = randomCode(captchaConfig.getLength());
            answer = content;
        }

        String key = CAPTCHA_KEY_PREFIX + uuid;
        redisTemplate.opsForValue().set(key, answer, Duration.ofMinutes(captchaConfig.getExpire()));

        return new CaptchaResponse(true, uuid, IMAGE_PREFIX + renderImage(content));
    }

    public void validate(String uuid, String code) {
        if (!isCaptchaEnabled()) {
            return;
        }

        if (!hasText(uuid) || !hasText(code)) {
            throw new BusinessException("验证码不能为空");
        }

        String key = CAPTCHA_KEY_PREFIX + uuid;
        Object answer = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);

        if (answer == null) {
            throw new BusinessException("验证码已失效");
        }

        if (!answer.toString().equalsIgnoreCase(code.trim())) {
            throw new BusinessException("验证码错误");
        }
    }

    private MathExpression generateMathExpression() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int op = random.nextInt(2);
        if (op == 0) {
            return new MathExpression(a + "+" + b + "=?", a + b);
        }
        if (a < b) {
            int temp = a;
            a = b;
            b = temp;
        }
        return new MathExpression(a + "-" + b + "=?", a - b);
    }

    private String randomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    private String renderImage(String text) {
        int width = captchaConfig.getWidth();
        int height = captchaConfig.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, width, height);

            drawNoise(graphics, width, height);
            drawCode(graphics, text, width, height);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException ex) {
            throw new BusinessException("验证码生成失败");
        } finally {
            graphics.dispose();
        }
    }

    private void drawCode(Graphics2D graphics, String code, int width, int height) {
        int fontSize = Math.min(24, height * 2 / 3);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        int charWidth = (width - 20) / code.length();
        for (int i = 0; i < code.length(); i++) {
            graphics.setColor(randomColor(40, 120));
            int x = 10 + i * charWidth + random.nextInt(charWidth / 2);
            int y = height / 2 + fontSize / 3 + random.nextInt(6);
            graphics.rotate(Math.toRadians(random.nextInt(25) - 12), x, y);
            graphics.drawString(String.valueOf(code.charAt(i)).toUpperCase(Locale.ROOT), x, y);
            graphics.rotate(Math.toRadians(-(random.nextInt(25) - 12)), x, y);
        }
    }

    private void drawNoise(Graphics2D graphics, int width, int height) {
        for (int i = 0; i < 5; i++) {
            graphics.setColor(randomColor(130, 210));
            graphics.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));
        }
        for (int i = 0; i < 20; i++) {
            graphics.setColor(randomColor(160, 230));
            graphics.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
        }
    }

    private Color randomColor(int min, int max) {
        int range = max - min;
        return new Color(min + random.nextInt(range), min + random.nextInt(range), min + random.nextInt(range));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isCaptchaEnabled() {
        String configValue = configService.getConfigKey("sys.account.captcha");
        if (hasText(configValue)) {
            return Boolean.parseBoolean(configValue);
        }
        return captchaConfig.isEnabled();
    }

    private record MathExpression(String display, int result) {
    }
}

package com.example.coreserver.service.algorithm;

import com.example.coreserver.entity.algorithm.DetectionTarget;
import com.example.coreserver.entity.algorithm.ObjectDetection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ImageProcessor {

    // 预定义不同目标的颜色（带透明度）
    private static final Color[] TARGET_COLORS = {
            new Color(255, 0, 0, 200),    // Red
            new Color(0, 255, 0, 200),    // Green
            new Color(0, 0, 255, 200),    // Blue
            new Color(255, 255, 0, 200),  // Yellow
            new Color(255, 0, 255, 200)   // Magenta
    };

    @Value("${video.stream.quality:0.8}") // 压缩质量 (0.0-1.0)
    private float compressionQuality;

    /**
     * 处理视频帧，绘制检测框并返回压缩后的二进制流
     */
    public byte[] processDetectionFrame(byte[] imageData, List<DetectionTarget> targets) throws IOException {
        if (imageData == null || imageData.length == 0) return new byte[0];

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                log.warn("无法解码图像数据");
                return new byte[0];
            }

            // 优化绘制性能（直接操作图像缓存）
            Graphics2D g2d = image.createGraphics();
            setupGraphicsQuality(g2d);

            // 绘制所有有效目标
            drawAllTargets(g2d, targets);

            g2d.dispose();
            return compressImage(image); // 压缩并转换为二进制
        }
    }

    /**
     * 配置图形绘制参数
     */
    private void setupGraphicsQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * 绘制所有目标框和标签
     */
    private void drawAllTargets(Graphics2D g2d, List<DetectionTarget> targets) {
        if (targets == null) return;

        for (int i = 0; i < targets.size(); i++) {
            DetectionTarget target = targets.get(i);
            if (isValidTarget(target)) {
                drawSingleTarget(g2d, target, i);
            }
        }
    }

    /**
     * 绘制目标框和标签
     */
    private void drawSingleTarget(Graphics2D g2d, DetectionTarget target, int index) {
        // 选择颜色
        Color color = TARGET_COLORS[index % TARGET_COLORS.length];
        int x = target.getLeft();
        int y = target.getTop();
        int width = target.getWidth();
        int height = target.getHeight();

        // 绘制目标框
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, width, height);

        // 绘制标签背景和文字
        String label = String.format("Target %d", index + 1);
        drawLabel(g2d, label, x, y);
    }

    /**
     * 绘制标签背景和文字
     */
    private void drawLabel(Graphics2D g2d, String text, int x, int y) {
        Font font = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics();

        // 计算标签尺寸
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        // 绘制背景
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(x, y - textHeight, textWidth + 8, textHeight);

        // 绘制文字
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x + 4, y - 4);
    }

    /**
     * 校验目标位置有效性
     */
    private boolean isValidTarget(DetectionTarget target) {
        return target != null &&
                target.getLeft() >= 0 &&
                target.getTop() >= 0 &&
                target.getWidth() > 5 &&
                target.getHeight() > 5;
    }

    /**
     * 压缩图像为JPEG二进制流
     */
    private byte[] compressImage(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 使用自定义压缩参数
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            // 设置压缩质量
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(compressionQuality);

            // 压缩并写入输出流
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();

            return baos.toByteArray();
        }
    }

    /**
     * 获取处理后的视频帧（带异常处理）
     */
    public byte[] getProcessedFrame(byte[] imageData, ObjectDetection detection) {
        if (detection == null || imageData == null) return imageData;

        try {
            return processDetectionFrame(
                    imageData,
                    detection.getTargets()
            );
        } catch (IOException e) {
            log.error("Processing failed, returning original data", e);
            return imageData; // Fallback to original
        }
    }
}
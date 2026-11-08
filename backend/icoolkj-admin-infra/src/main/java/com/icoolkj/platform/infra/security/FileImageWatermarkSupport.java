package com.icoolkj.platform.infra.security;

import com.icoolkj.platform.common.enums.FileSensitivityEnum;
import com.icoolkj.platform.infra.storage.StorageProperties;
import com.icoolkj.platform.infra.util.ImageContentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Set;

/** Optional text watermark for jpeg/png when sensitivity is high enough. */
@Component
public class FileImageWatermarkSupport {

    private static final Logger log = LoggerFactory.getLogger(FileImageWatermarkSupport.class);
    private static final Set<String> WATERMARK_EXTS = Set.of("jpg", "jpeg", "png");

    private final StorageProperties storageProperties;

    public FileImageWatermarkSupport(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public byte[] maybeWatermark(byte[] content, String ext, String contentType, String sensitivityLevel) {
        StorageProperties.Watermark watermark = storageProperties.getSecurity().getWatermark();
        if (!watermark.isEnabled() || content == null || content.length == 0) {
            return content;
        }
        FileSensitivityEnum min = FileSensitivityEnum.fromCode(watermark.getMinSensitivity());
        if (min == null) {
            min = FileSensitivityEnum.SECRET;
        }
        if (!FileSensitivityEnum.isAtLeast(sensitivityLevel, min)) {
            return content;
        }
        String normalizedExt = ImageContentValidator.normalizeExt(ext);
        if (!WATERMARK_EXTS.contains(normalizedExt)) {
            if (StringUtils.hasText(contentType)) {
                String ct = contentType.toLowerCase(Locale.ROOT);
                if (ct.contains("jpeg") || ct.contains("jpg")) {
                    normalizedExt = "jpg";
                } else if (ct.contains("png")) {
                    normalizedExt = "png";
                } else {
                    return content;
                }
            } else {
                return content;
            }
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                return content;
            }
            Graphics2D g = image.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                g.setColor(Color.GRAY);
                int fontSize = Math.max(16, image.getWidth() / 30);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
                String text = StringUtils.hasText(watermark.getText()) ? watermark.getText() : "iCoolKJ";
                int x = Math.max(10, image.getWidth() / 10);
                int y = Math.max(fontSize + 10, image.getHeight() - fontSize);
                g.drawString(text, x, y);
            } finally {
                g.dispose();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // Always re-encode as JPEG for a simple, consistent download payload.
            if (!ImageIO.write(image, "jpg", out)) {
                return content;
            }
            return out.toByteArray();
        } catch (Exception ex) {
            log.warn("Watermark failed, returning original: {}", ex.getMessage());
            return content;
        }
    }
}

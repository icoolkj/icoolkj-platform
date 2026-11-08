package com.icoolkj.platform.infra.util;

import com.icoolkj.platform.common.exception.BusinessException;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** 头像缩略图生成 */
public final class ImageResizeUtils {

    private ImageResizeUtils() {
    }

    public static byte[] resizeSquareJpeg(byte[] sourceBytes, int size) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(sourceBytes)) {
            BufferedImage source = ImageIO.read(in);
            if (source == null) {
                throw new BusinessException("error.avatar.invalid_image");
            }
            BufferedImage square = cropSquare(source);
            BufferedImage resized = resize(square, size, size);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(resized, "jpg", out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException("error.avatar.process_failed");
        }
    }

    private static BufferedImage cropSquare(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int side = Math.min(width, height);
        int x = (width - side) / 2;
        int y = (height - side) / 2;
        return source.getSubimage(x, y, side, side);
    }

    private static BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return target;
    }
}

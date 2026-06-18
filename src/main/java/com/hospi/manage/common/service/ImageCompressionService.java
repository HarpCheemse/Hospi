package com.hospi.manage.common.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Service
public class ImageCompressionService {

    public byte[] toWebp(MultipartFile file, int targetWidth, float quality) throws IOException {

        BufferedImage original = ImageIO.read(file.getInputStream());

        if (original == null) {
            throw new IllegalArgumentException("Unsupported image type");
        }

        int targetHeight = (original.getHeight() * targetWidth) / original.getWidth();

        BufferedImage resized = new BufferedImage(
                targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(original,
                0,
                0,
                targetWidth,
                targetHeight,
                null);
        g.dispose();

        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByFormatName("webp");

        if (!writers.hasNext()) {
            throw new IllegalStateException("No WebP writer found");
        }

        ImageWriter writer = writers.next();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);

        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            String[] types = param.getCompressionTypes();
            if (types != null && types.length > 0) {
                param.setCompressionType(types[0]);
            }

            param.setCompressionQuality(quality);
        }

        writer.write(null,
                new IIOImage(resized,
                        null,
                        null),
                param);

        ios.close();
        writer.dispose();

        return baos.toByteArray();
    }
}
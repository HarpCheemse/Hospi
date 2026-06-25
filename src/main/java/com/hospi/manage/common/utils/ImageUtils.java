package com.hospi.manage.common.utils;

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

/** Static utility for resizing and compressing images to WebP format. */
public class ImageUtils {

    private ImageUtils() {}

    /**
     * Resize an uploaded image to the given width (preserving aspect ratio) and compress it to WebP.
     *
     * @param file        the uploaded image file
     * @param targetWidth desired width in pixels
     * @param quality     compression quality (0.0 – 1.0)
     * @return the compressed image as a byte array
     * @throws IOException              if the image cannot be read or written
     * @throws IllegalArgumentException if the image format is unsupported or the file is not a valid image
     * @throws IllegalStateException    if no WebP image writer is available in the runtime
     */
    public static byte[] compressWebP(
            MultipartFile file,
            int targetWidth,
            float quality
    ) throws IOException {

        BufferedImage original =
                ImageIO.read(file.getInputStream());

        if (original == null) {
            throw new IllegalArgumentException(
                    "Unsupported image type"
            );
        }

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        int targetHeight =
                (originalHeight * targetWidth)
                        / originalWidth;

        // KEEP TRANSPARENCY
        BufferedImage resized =
                new BufferedImage(
                        targetWidth,
                        targetHeight,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g = resized.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        g.drawImage(
                original,
                0,
                0,
                targetWidth,
                targetHeight,
                null
        );

        g.dispose();

        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByMIMEType(
                        "image/webp"
                );

        if (!writers.hasNext()) {
            throw new IllegalStateException(
                    "No WebP writer found"
            );
        }

        ImageWriter writer = writers.next();

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        ImageOutputStream ios =
                ImageIO.createImageOutputStream(baos);

        writer.setOutput(ios);

        ImageWriteParam param =
                writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {

            param.setCompressionMode(
                    ImageWriteParam.MODE_EXPLICIT
            );
            param.setCompressionType(
                    param.getCompressionTypes()[0]
            );
            param.setCompressionQuality(quality);
        }

        writer.write(
                null,
                new IIOImage(resized, null, null),
                param
        );

        ios.close();
        writer.dispose();

        return baos.toByteArray();
    }
}
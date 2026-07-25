package com.hospi.manage.common.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for {@link ImageCompressionService}. */
class ImageCompressionServiceTest {

    private final ImageCompressionService service = new ImageCompressionService();

    @Test
    void toWebp_shouldConvertValidImage() throws Exception {
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 50);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);

        MultipartFile file = new MockMultipartFile(
                "image", "test.png", "image/png", baos.toByteArray());

        byte[] result = service.toWebp(file, 200, 0.75f);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void toWebp_shouldThrow_whenNotAnImage() {
        MultipartFile file = new MockMultipartFile(
                "image", "test.txt", "text/plain", "not an image".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> service.toWebp(file, 200, 0.75f));
    }
}

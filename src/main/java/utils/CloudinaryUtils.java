package utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public final class CloudinaryUtils {

    private static final Cloudinary cloudinary;

    static {
        try {
            Properties props = new Properties();
            props.load(CloudinaryUtils.class
                    .getClassLoader()
                    .getResourceAsStream("config/config.properties"));

            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", props.getProperty("cloudinary.cloud_name"),
                    "api_key", props.getProperty("cloudinary.api_key"),
                    "api_secret", props.getProperty("cloudinary.api_secret"),
                    "secure", true
            ));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private CloudinaryUtils() {
    }

    public static String upload(Part part, String folder) throws IOException {
        byte[] bytes = part.getInputStream().readAllBytes();
        Map result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "folder", folder,
                "format", "webp",
                "quality", "auto",
                "fetch_format", "auto"
        ));
        return (String) result.get("secure_url");
    }
}

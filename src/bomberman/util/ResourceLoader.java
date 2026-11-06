package bomberman.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích dùng chung để tải và quản lý tài nguyên (hình ảnh sprite, icon, background).
 * Áp dụng cơ chế Cache để tối ưu hóa hiệu năng, tránh đọc đĩa nhiều lần.
 */
public class ResourceLoader {
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    /**
     * Tải hình ảnh từ tài nguyên (Resource / ClassPath) hoặc đường dẫn file hệ thống.
     *
     * @param fileName Tên file ảnh (vd: "player.png", "wall.png") hoặc đường dẫn tương đối (vd: "assets/Background.png")
     * @return BufferedImage nếu tìm thấy ảnh, hoặc null nếu không tìm thấy
     */
    public static BufferedImage loadImage(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        String cleanName = fileName.replace("\\", "/");
        while (cleanName.startsWith("/")) {
            cleanName = cleanName.substring(1);
        }

        if (imageCache.containsKey(cleanName)) {
            return imageCache.get(cleanName);
        }

        BufferedImage img = null;

        // 1. Thử tải qua ClassLoader Resource
        try {
            var url = ResourceLoader.class.getResource("/" + cleanName);
            if (url == null && !cleanName.startsWith("assets/images/")) {
                url = ResourceLoader.class.getResource("/assets/images/" + cleanName);
            }
            if (url != null) {
                img = ImageIO.read(url);
            }
        } catch (Exception ignored) {}

        // 2. Thử các đường dẫn tương đối trên hệ thống tập tin
        if (img == null) {
            String[] possiblePaths = {
                cleanName,
                "assets/images/" + cleanName,
                "assets/" + cleanName,
                "BTL/Project-OOP-Bomberman/" + cleanName,
                "Project-OOP-Bomberman/" + cleanName,
                "BTL/Project-OOP-Bomberman/assets/images/" + cleanName,
                "Project-OOP-Bomberman/assets/images/" + cleanName
            };
            for (String path : possiblePaths) {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    try {
                        img = ImageIO.read(f);
                        if (img != null) break;
                    } catch (Exception ignored) {}
                }
            }
        }

        if (img != null) {
            imageCache.put(cleanName, img);
        } else {
            System.err.println("[ResourceLoader] Không thể tải ảnh: " + fileName);
        }

        return img;
    }

    /**
     * Xóa cache hình ảnh khi cần giải phóng bộ nhớ.
     */
    public static void clearCache() {
        imageCache.clear();
    }
}

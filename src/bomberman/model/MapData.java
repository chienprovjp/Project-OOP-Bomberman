package bomberman.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý nạp bản đồ các màn chơi (Level 1, 2, 3) từ file cấu hình .txt.
 * Tác giả nguyên bản: nguyentaianquoc06
 */
public class MapData {

    public static final int GRASS = 0;
    public static final int WALL  = 1;
    public static final int BRICK = 2;

    public static final int LEVEL_COUNT = 9;

    /**
     * Nạp dữ liệu bản đồ theo số thứ tự màn chơi (1 -> LEVEL_COUNT).
     * @param level Số thứ tự màn chơi (1, 2, 3)
     * @return Mảng 2 chiều int[rows][cols] chứa mã các ô bản đồ.
     */
    public static int[][] loadLevel(int level) {
        if (level < 1 || level > LEVEL_COUNT) {
            level = 1;
        }

        String fileName = "level" + level + ".txt";

        // 1. Thử nạp từ File hệ thống trực tiếp (các đường dẫn tương đối phổ biến)
        String[] possibleFilePaths = {
            "assets/maps/" + fileName,
            "src/Map/" + fileName,
            "assets/" + fileName,
            "BTL/Project-OOP-Bomberman/assets/maps/" + fileName,
            "Project-OOP-Bomberman/assets/maps/" + fileName
        };

        for (String p : possibleFilePaths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
                    int[][] map = parseMapFromReader(reader);
                    if (map != null) return sanitizeSpawn(map);
                } catch (Exception e) {
                    System.err.println("[MapData] Lỗi đọc file " + p + ": " + e.getMessage());
                }
            }
        }

        // 2. Thử nạp qua ClassLoader Resource
        String[] resourcePaths = {
            "/assets/maps/" + fileName,
            "/src/Map/" + fileName,
            "/" + fileName
        };

        for (String rPath : resourcePaths) {
            InputStream is = MapData.class.getResourceAsStream(rPath);
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    int[][] map = parseMapFromReader(reader);
                    if (map != null) return sanitizeSpawn(map);
                } catch (Exception e) {
                    System.err.println("[MapData] Lỗi đọc resource " + rPath + ": " + e.getMessage());
                }
            }
        }

        System.err.println("[MapData] Không tìm thấy file map cho màn " + level + ". Sử dụng map dự phòng.");
        return createFallbackMap();
    }

    private static int[][] parseMapFromReader(BufferedReader reader) throws Exception {
        List<int[]> rowList = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            int[] row = new int[parts.length];
            for (int col = 0; col < parts.length; col++) {
                row[col] = Integer.parseInt(parts[col]);
            }
            rowList.add(row);
        }

        if (rowList.isEmpty()) return null;
        return rowList.toArray(new int[0][]);
    }

    /**
     * Đảm bảo khu vực xuất phát của Player (hàng 1 cột 1) và các ô kề cạnh
     * luôn là cỏ trống (GRASS) để Player không bị kẹt trong gạch.
     */
    private static int[][] sanitizeSpawn(int[][] map) {
        if (map.length > 2 && map[0].length > 2) {
            if (map[1][1] == BRICK) map[1][1] = GRASS;
            if (map[1][2] == BRICK) map[1][2] = GRASS;
            if (map[2][1] == BRICK) map[2][1] = GRASS;
        }
        return map;
    }

    /**
     * Bản đồ mặc định dự phòng nếu không tìm thấy file.
     */
    private static int[][] createFallbackMap() {
        int rows = 13;
        int cols = 21;
        int[][] map = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1 || (r % 2 == 0 && c % 2 == 0)) {
                    map[r][c] = WALL;
                } else {
                    map[r][c] = (Math.random() < 0.28) ? BRICK : GRASS;
                }
            }
        }
        map[1][1] = GRASS;
        map[1][2] = GRASS;
        map[2][1] = GRASS;
        return map;
    }
}


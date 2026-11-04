package src.Map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MapData {

    public static final int GRASS = 0;
    public static final int WALL = 1;
    public static final int BRICK = 2;

    public static final int LEVEL_COUNT = 3;
    private static int[][] map;
    public static int ROWS;
    public static int COLS;
    private static int currentLevel;

    static {
        loadLevel(1);
    }

    public static void loadLevel(int level) {
        if (level < 1 || level > LEVEL_COUNT) {
            throw new IllegalArgumentException(
                "Man choi khong hop le: " + level
            );
        }

        int[][] selectedMap = loadMap("/src/Map/level" + level + ".txt");
        map = selectedMap;
        ROWS = selectedMap.length;
        COLS = selectedMap[0].length;
        currentLevel = level;
    }

    public static int getCurrentLevel() {
        return currentLevel;
    }

    private static int[][] loadMap(String path) {
        List<int[]> rows = new ArrayList<>();
        InputStream input = MapData.class.getResourceAsStream(path);
        if (input == null) {
            throw new IllegalStateException("Khong tim thay file map: " + path);
        }

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(input, StandardCharsets.UTF_8)
        )) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                int[] row = new int[parts.length];
                for (int col = 0; col < parts.length; col++) {
                    int value;
                    try {
                        value = Integer.parseInt(parts[col]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                            "Gia tri khong phai so tai dong " + lineNumber
                                + ", cot " + (col + 1) + ": " + parts[col],e
                        );
                    }

                    if (value < GRASS || value > BRICK) {
                        throw new IllegalArgumentException(
                            "Ma o khong hop le tai dong " + lineNumber
                                + ", cot " + (col + 1) + ": " + value
                        );
                    }

                    row[col] = value;
                }

                if (!rows.isEmpty() && row.length != rows.get(0).length) {
                    throw new IllegalArgumentException(
                        "So cot khong dong deu tai dong " + lineNumber
                    );
                }

                rows.add(row);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Loi doc file map: " + path, e);
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("File map rong: " + path);
        }

        return rows.toArray(new int[0][]);
    }

    public static int getTile(int row, int col) {
        if (!isInsideMap(row, col)) {
            return WALL;
        }

        return map[row][col];
    }

    public static boolean isInsideMap(int row, int col) {
        return row >= 0 && row < ROWS
            && col >= 0 && col < COLS;
    }

    public static boolean isWalkable(int row, int col) {
        return isInsideMap(row, col)
            && map[row][col] == GRASS;
    }

    public static boolean isDestructible(int row, int col) {
        return isInsideMap(row, col)
            && map[row][col] == BRICK;
    }

    public static boolean destroyBrick(int row, int col) {
        if (isDestructible(row, col)) {
            map[row][col] = GRASS;
            return true;
        }

        return false;
    }
}
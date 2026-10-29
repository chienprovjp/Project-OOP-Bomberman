package src.Map;

public class MapData {

    public static final int GRASS = 0;
    public static final int WALL = 1;
    public static final int BRICK = 2;

    public static final int ROWS = 13;
    public static final int COLS = 13;

    private static final int[][] map = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 0, 0, 2, 0, 2, 0, 0, 0, 2, 0, 1},
        {1, 0, 1, 2, 0, 1, 0, 2, 0, 1, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 1},
        {1, 0, 1, 2, 1, 0, 1, 2, 1, 0, 1, 0, 1},
        {1, 2, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1},
        {1, 0, 1, 0, 2, 1, 2, 1, 0, 1, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 2, 1},
        {1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 1},
        {1, 0, 1, 2, 1, 0, 2, 0, 0, 1, 0, 1, 1},
        {1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

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

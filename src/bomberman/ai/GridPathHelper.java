package bomberman.ai;

import bomberman.model.Bomb;
import bomberman.model.Enemy;
import bomberman.model.GameMap;
import bomberman.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp tiện ích dùng chung cho các thuật toán tìm đường trên lưới ô vuông (BFS, A*, Random).
 * Giúp tái sử dụng code chuyển đổi tọa độ pixel <-> ô lưới, kiểm tra ô hợp lệ và nhận diện vùng nguy hiểm của bom.
 */
public final class GridPathHelper {
    // 4 hướng di chuyển hợp lệ trên lưới (không đi chéo): Lên, Xuống, Trái, Phải
    public static final int[][] DIRS = {
        { 0, -1 }, // UP    (dCol = 0, dRow = -1)
        { 0,  1 }, // DOWN  (dCol = 0, dRow = +1)
        {-1,  0 }, // LEFT  (dCol = -1, dRow = 0)
        { 1,  0 }  // RIGHT (dCol = +1, dRow = 0)
    };

    public static final Player.Direction[] DIR_ENUMS = {
        Player.Direction.UP,
        Player.Direction.DOWN,
        Player.Direction.LEFT,
        Player.Direction.RIGHT
    };

    private GridPathHelper() {
    }

    /**
     * Lấy chỉ số CỘT (col) của tâm thực thể trên lưới bản đồ.
     */
    public static int getCenterCol(int x, int width) {
        return (x + width / 2) / GameMap.TILE_SIZE;
    }

    /**
     * Lấy chỉ số HÀNG (row) của tâm thực thể trên lưới bản đồ.
     */
    public static int getCenterRow(int y, int height) {
        return (y + height / 2) / GameMap.TILE_SIZE;
    }

    /**
     * Kiểm tra một ô (row, col) trên bản đồ có thể đi xuyên qua được không.
     */
    public static boolean isWalkableTile(GameMap map, int row, int col, boolean canPassBrick, List<Bomb> bombs) {
        if (map == null || row < 0 || row >= map.getRows() || col < 0 || col >= map.getCols()) {
            return false;
        }

        int tile = map.getTile(row, col);
        if (tile == GameMap.GRASS) {
            // Cỏ luôn đi qua được (trừ khi có bom đang nằm trên ô đó)
        } else if (tile == GameMap.BRICK) {
            if (!canPassBrick) {
                return false;
            }
        } else {
            // Tường đá (WALL = 1) hoặc rào cản khác đều chặn hoàn toàn
            return false;
        }

        // Không bước vào ô đang có bom chưa nổ
        if (bombs != null) {
            for (Bomb b : bombs) {
                if (!b.isExploded() && b.getTileRow() == row && b.getTileCol() == col) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Kiểm tra xem ô (row, col) có đang nằm trong tầm tia lửa sắp nổ của bất kỳ quả bom nào không.
     * Dùng để tính điểm phạt cho thuật toán A* giúp quái né bom thông minh.
     */
    public static boolean isDangerTile(int row, int col, List<Bomb> bombs, GameMap map) {
        if (bombs == null || bombs.isEmpty() || map == null) {
            return false;
        }

        for (Bomb b : bombs) {
            if (b.isExploded()) continue;
            int bRow = b.getTileRow();
            int bCol = b.getTileCol();
            int radius = b.getRadius();

            if (row == bRow && col == bCol) {
                return true;
            }

            // Kiểm tra theo 4 tia nổ chữ thập từ tâm quả bom
            for (int[] d : DIRS) {
                for (int step = 1; step <= radius; step++) {
                    int nr = bRow + d[1] * step;
                    int nc = bCol + d[0] * step;
                    if (nr < 0 || nr >= map.getRows() || nc < 0 || nc >= map.getCols()) break;
                    int tile = map.getTile(nr, nc);
                    if (tile == GameMap.WALL || tile == GameMap.BARRIER) break;
                    if (nr == row && nc == col) return true;
                    if (tile == GameMap.BRICK) break; // Tia lửa dừng lại ở viên gạch đầu tiên
                }
            }
        }
        return false;
    }

    /**
     * Chuyển đổi bước nhảy từ ô (fromCol, fromRow) sang ô kề cạnh (toCol, toRow) thành hướng Player.Direction.
     */
    public static Player.Direction directionBetween(int fromCol, int fromRow, int toCol, int toRow) {
        int dc = toCol - fromCol;
        int dr = toRow - fromRow;
        if (dr < 0) return Player.Direction.UP;
        if (dr > 0) return Player.Direction.DOWN;
        if (dc < 0) return Player.Direction.LEFT;
        if (dc > 0) return Player.Direction.RIGHT;
        return null;
    }

    /**
     * Chiến lược dự phòng an toàn khi không tìm được đường tới Player:
     * ưu tiên tiếp tục đi thẳng hoặc rẽ sang nhánh hợp lệ không vướng bom/tường.
     */
    public static Player.Direction pickSafeRandomDirection(Enemy enemy, GameMap map, List<Bomb> bombs, boolean canPassBrick) {
        int curCol = getCenterCol(enemy.getX(), enemy.getWidth());
        int curRow = getCenterRow(enemy.getY(), enemy.getHeight());
        Player.Direction currentDir = enemy.getDirection();

        List<Player.Direction> safeDirs = new ArrayList<>();
        List<Player.Direction> validDirs = new ArrayList<>();

        for (int i = 0; i < DIRS.length; i++) {
            int nc = curCol + DIRS[i][0];
            int nr = curRow + DIRS[i][1];
            if (isWalkableTile(map, nr, nc, canPassBrick, bombs)) {
                Player.Direction dir = DIR_ENUMS[i];
                validDirs.add(dir);
                if (!isDangerTile(nr, nc, bombs, map)) {
                    safeDirs.add(dir);
                }
            }
        }

        List<Player.Direction> pool = !safeDirs.isEmpty() ? safeDirs : validDirs;
        if (pool.isEmpty()) {
            return currentDir;
        }

        // Nếu hướng hiện tại vẫn an toàn và không muốn rẽ liên tục (70% giữ nguyên hướng)
        if (pool.contains(currentDir) && Math.random() < 0.70) {
            return currentDir;
        }

        // Tránh quay đầu 180 độ ngay lập tức nếu còn lựa chọn rẽ khác
        Player.Direction opposite = oppositeOf(currentDir);
        if (pool.size() > 1) {
            pool.remove(opposite);
        }

        Collections.shuffle(pool);
        return pool.get(0);
    }

    public static Player.Direction oppositeOf(Player.Direction dir) {
        if (dir == null) return Player.Direction.DOWN;
        return switch (dir) {
            case UP    -> Player.Direction.DOWN;
            case DOWN  -> Player.Direction.UP;
            case LEFT  -> Player.Direction.RIGHT;
            case RIGHT -> Player.Direction.LEFT;
        };
    }
}

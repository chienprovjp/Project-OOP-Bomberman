package bomberman.ai;

import bomberman.model.Bomb;
import bomberman.model.Enemy;
import bomberman.model.GameMap;
import bomberman.model.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Chiến lược tìm đường bằng Thuật toán Tìm kiếm theo Chiều rộng (Breadth-First Search - BFS).
 *
 * Nguyên lý hoạt động:
 * - Loang đều từng lớp (level-by-level) từ vị trí của Quái bằng Hàng đợi (Queue FIFO).
 * - Vì mỗi ô kề nhau có chi phí đi chuyển bằng 1 (đồ thị không trọng số),
 *   lần đầu tiên BFS chạm tới ô của Player chính là đường đi ngắn nhất theo số bước.
 * - Giới hạn tầm phát hiện (detectionRadius) để cân bằng độ khó ở mức Dễ/Thường.
 */
public class BFSStrategy implements MoveStrategy {
    private final int detectionRadius;

    public BFSStrategy() {
        this(7);
    }

    public BFSStrategy(int detectionRadius) {
        this.detectionRadius = detectionRadius;
    }

    @Override
    public Player.Direction chooseDirection(Enemy enemy, GameMap map, List<Bomb> bombs, Player target) {
        if (target == null || map == null) {
            return GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, enemy.canPassBrick());
        }

        int startCol = GridPathHelper.getCenterCol(enemy.getX(), enemy.getWidth());
        int startRow = GridPathHelper.getCenterRow(enemy.getY(), enemy.getHeight());
        int targetCol = GridPathHelper.getCenterCol(target.getX(), target.getWidth());
        int targetRow = GridPathHelper.getCenterRow(target.getY(), target.getHeight());

        // Kiểm tra khoảng cách ước lượng (Manhattan) xem Player có trong tầm nhìn hay không
        int manhattanDist = Math.abs(startCol - targetCol) + Math.abs(startRow - targetRow);
        if (manhattanDist == 0 || manhattanDist > detectionRadius) {
            return GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, enemy.canPassBrick());
        }

        int rows = map.getRows();
        int cols = map.getCols();
        boolean canPassBrick = enemy.canPassBrick();

        boolean[][] visited = new boolean[rows][cols];
        int[][] parentRow = new int[rows][cols];
        int[][] parentCol = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                parentRow[r][c] = -1;
                parentCol[r][c] = -1;
            }
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[] { startCol, startRow });
        visited[startRow][startCol] = true;

        boolean found = false;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int c = curr[0];
            int r = curr[1];

            if (c == targetCol && r == targetRow) {
                found = true;
                break;
            }

            for (int[] d : GridPathHelper.DIRS) {
                int nc = c + d[0];
                int nr = r + d[1];

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
                    // Cho phép đi vào ô đích ngay cả khi có điều kiện đặc biệt, các ô khác phải đi qua được
                    boolean isTargetCell = (nc == targetCol && nr == targetRow);
                    if (isTargetCell || GridPathHelper.isWalkableTile(map, nr, nc, canPassBrick, bombs)) {
                        visited[nr][nc] = true;
                        parentCol[nr][nc] = c;
                        parentRow[nr][nc] = r;
                        queue.offer(new int[] { nc, nr });
                    }
                }
            }
        }

        if (!found) {
            return GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, canPassBrick);
        }

        // Truy vết ngược từ ô đích (targetRow, targetCol) về ô ngay cạnh ô xuất phát (startRow, startCol)
        int stepCol = targetCol;
        int stepRow = targetRow;
        while (parentRow[stepRow][stepCol] != -1) {
            int pCol = parentCol[stepRow][stepCol];
            int pRow = parentRow[stepRow][stepCol];
            if (pCol == startCol && pRow == startRow) {
                return GridPathHelper.directionBetween(startCol, startRow, stepCol, stepRow);
            }
            stepCol = pCol;
            stepRow = pRow;
        }

        return GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, canPassBrick);
    }

    @Override
    public String getStrategyName() {
        return "BFS (R=" + detectionRadius + ")";
    }
}

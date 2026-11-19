package bomberman.ai;

import bomberman.model.Bomb;
import bomberman.model.Enemy;
import bomberman.model.GameMap;
import bomberman.model.Player;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Chiến lược tìm đường bằng Thuật toán A* (A-Star Pathfinding).
 *
 * Công thức đánh giá tại mỗi ô x:
 *     f(x) = g(x) + h(x)
 * Trong đó:
 * - g(x): Chi phí/khoảng cách thực tế đã đi từ ô xuất phát của Quái đến ô x.
 *         Mỗi bước qua 1 ô bình thường tốn chi phí = 1; nếu ô x nằm trong vùng
 *         bom sắp nổ (khi bật avoidBombs), cộng thêm chi phí phạt để quái chủ động né.
 * - h(x): Hàm Heuristic ước lượng khoảng cách còn lại từ ô x đến mục tiêu (Player),
 *         sử dụng khoảng cách Manhattan:
 *             h(x) = width + height = |col_x - col_target| + |row_x - row_target|
 *         (Vì trong Bomberman chỉ đi 4 hướng ngang/dọc, không đi chéo nên Manhattan
 *          là hàm ước lượng chuẩn mực, không bao giờ đánh giá quá chi phí thực tế).
 * - f(x): Tổng chi phí ước lượng khi đi qua ô x. Hàng đợi ưu tiên (PriorityQueue)
 *         luôn lấy ô có f(x) nhỏ nhất để mở rộng trước.
 */
public class AStarStrategy implements MoveStrategy {
    private static final int STEP_COST = 1;
    private static final int BOMB_DANGER_PENALTY = 12;

    private final int detectionRadius;
    private final boolean avoidBombs;

    public AStarStrategy() {
        this(10, true);
    }

    public AStarStrategy(int detectionRadius, boolean avoidBombs) {
        this.detectionRadius = detectionRadius;
        this.avoidBombs = avoidBombs;
    }

    /**
     * Đại diện một nút (ô lưới) trong cây tìm kiếm A*.
     */
    private static class Node implements Comparable<Node> {
        final int col;
        final int row;
        final int g; // Khoảng cách thực tế từ điểm xuất phát -> ô hiện tại
        final int h; // Khoảng cách ước lượng (Heuristic) từ ô hiện tại -> đích
        final int f; // Tổng chi phí: f = g + h
        final Node parent;

        Node(int col, int row, int g, int h, Node parent) {
            this.col = col;
            this.row = row;
            this.g = g;
            this.h = h;
            this.f = calculateF(g, h);
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            // Ưu tiên nút có tổng f(x) nhỏ nhất; nếu f bằng nhau, ưu tiên h(x) nhỏ hơn (gần đích hơn)
            if (this.f != other.f) {
                return Integer.compare(this.f, other.f);
            }
            return Integer.compare(this.h, other.h);
        }
    }

    /**
     * Tính g(x): Chi phí thực tế tích lũy từ điểm bắt đầu đến ô kế tiếp (nextCol, nextRow).
     */
    public int calculateG(int currentG, int nextCol, int nextRow, GameMap map, List<Bomb> bombs) {
        int stepCost = STEP_COST;
        if (avoidBombs && GridPathHelper.isDangerTile(nextRow, nextCol, bombs, map)) {
            stepCost += BOMB_DANGER_PENALTY;
        }
        return currentG + stepCost;
    }

    /**
     * Tính h(x): Khoảng cách ước lượng Manhattan từ ô (col, row) tới đích (targetCol, targetRow).
     * h(x) = width + height
     */
    public int calculateH(int col, int row, int targetCol, int targetRow) {
        int width = Math.abs(col - targetCol);   // Khoảng cách theo chiều ngang (số cột lệch)
        int height = Math.abs(row - targetRow);  // Khoảng cách theo chiều dọc (số hàng lệch)
        return width + height;
    }

    /**
     * Tính f(x) = g(x) + h(x).
     */
    public static int calculateF(int g, int h) {
        return g + h;
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

        int initialH = calculateH(startCol, startRow, targetCol, targetRow);
        if (initialH == 0 || initialH > detectionRadius) {
            return GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, enemy.canPassBrick());
        }

        int rows = map.getRows();
        int cols = map.getCols();
        boolean canPassBrick = enemy.canPassBrick();

        // bestG lưu chi phí g(x) thấp nhất đã từng đạt được tới mỗi ô
        int[][] bestG = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            Arrays.fill(bestG[r], Integer.MAX_VALUE);
        }
        boolean[][] closedSet = new boolean[rows][cols];

        PriorityQueue<Node> openList = new PriorityQueue<>();
        bestG[startRow][startCol] = 0;
        openList.offer(new Node(startCol, startRow, 0, initialH, null));

        Node goalNode = null;

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (closedSet[current.row][current.col]) {
                continue;
            }
            closedSet[current.row][current.col] = true;

            if (current.col == targetCol && current.row == targetRow) {
                goalNode = current;
                break;
            }

            // Mở rộng 4 ô lân cận (Lên, Xuống, Trái, Phải)
            for (int[] d : GridPathHelper.DIRS) {
                int nc = current.col + d[0];
                int nr = current.row + d[1];

                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || closedSet[nr][nc]) {
                    continue;
                }

                boolean isTargetCell = (nc == targetCol && nr == targetRow);
                if (!isTargetCell && !GridPathHelper.isWalkableTile(map, nr, nc, canPassBrick, bombs)) {
                    continue;
                }

                int tentativeG = calculateG(current.g, nc, nr, map, bombs);
                if (tentativeG < bestG[nr][nc]) {
                    bestG[nr][nc] = tentativeG;
                    int h = calculateH(nc, nr, targetCol, targetRow);
                    openList.offer(new Node(nc, nr, tentativeG, h, current));
                }
            }
        }

        if (goalNode == null) {
            return GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, canPassBrick);
        }

        // Truy vết ngược từ đích về nút con trực tiếp của điểm xuất phát
        Node step = goalNode;
        while (step.parent != null && !(step.parent.col == startCol && step.parent.row == startRow)) {
            step = step.parent;
        }

        Player.Direction chosen = GridPathHelper.directionBetween(startCol, startRow, step.col, step.row);
        return (chosen != null) ? chosen : GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, canPassBrick);
    }

    @Override
    public String getStrategyName() {
        return "A* (R=" + (detectionRadius == Integer.MAX_VALUE ? "ALL" : detectionRadius)
                + (avoidBombs ? ", NéBom" : "") + ")";
    }
}

package bomberman.ai;

import bomberman.model.Bomb;
import bomberman.model.Enemy;
import bomberman.model.GameMap;
import bomberman.model.Player;

import java.util.List;

/**
 * Strategy Pattern (Mẫu thiết kế Chiến lược):
 * Giao diện định nghĩa chiến lược tìm đường cho các loài Kẻ địch (Enemy).
 * Cho phép thay đổi linh hoạt thuật toán AI (Random, BFS, A*) tại runtime tùy theo loại quái và độ khó.
 */
public interface MoveStrategy {
    /**
     * Tính toán hướng di chuyển tối ưu tiếp theo cho kẻ địch.
     *
     * @param enemy  Đối tượng kẻ địch đang cần tìm đường
     * @param map    Bản đồ hiện tại (chứa tường, gạch, cỏ)
     * @param bombs  Danh sách bom đang có trên sân (để chặn đường hoặc né)
     * @param target Mục tiêu cần truy đuổi (Player), có thể null
     * @return Hướng di chuyển tiếp theo (UP, DOWN, LEFT, RIGHT)
     */
    Player.Direction chooseDirection(Enemy enemy, GameMap map, List<Bomb> bombs, Player target);

    /**
     * Tên thuật toán AI đang dùng (dùng cho log hoặc hiển thị debug).
     */
    String getStrategyName();
}

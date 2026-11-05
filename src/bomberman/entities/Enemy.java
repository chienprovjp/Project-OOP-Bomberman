package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

/**
 * Lớp trừu tượng đại diện cho Kẻ địch (Enemy) trong game Bomberman.
 *
 * Áp dụng tính Kế thừa (Inheritance) và Trừu tượng (Abstraction) trong OOP:
 * - Kế thừa từ Entity: Tái sử dụng vị trí (x, y) và khung va chạm (hitbox).
 * - Là lớp cha cung cấp khung sườn chung cho mọi loại quái vật (Ballom, Oneal, v.v.).
 * - Định nghĩa phương thức trừu tượng move(...) để từng loại quái tự cài đặt thuật toán di chuyển riêng.
 */
public abstract class Enemy extends Entity {
    // Tốc độ di chuyển tính theo pixel mỗi khung hình
    protected int speed = 2;

    // Hướng di chuyển hiện tại của kẻ địch (sử dụng enum Direction từ Player)
    protected Player.Direction direction = Player.Direction.RIGHT;

    // Trạng thái còn sống hay đã bị tiêu diệt
    protected boolean alive = true;

    // Điểm thưởng khi tiêu diệt kẻ địch này
    protected int scoreReward = 100;

    // Bộ sinh số ngẫu nhiên phục vụ thuật toán di chuyển
    protected static final Random random = new Random();

    public Enemy(int startX, int startY, int width, int height, int speed) {
        super(startX, startY, width, height);
        this.speed = speed;
        changeRandomDirection();
    }

    /**
     * Phương thức trừu tượng: Di chuyển và xử lý va chạm với bản đồ và bom.
     * Mỗi loại quái vật con (ví dụ Ballom, Oneal...) sẽ tự định nghĩa thuật toán riêng.
     */
    public abstract void move(GameMap map, List<Bomb> bombs);

    /**
     * Đổi sang một hướng đi ngẫu nhiên trong 4 hướng (UP, DOWN, LEFT, RIGHT).
     */
    protected void changeRandomDirection() {
        Player.Direction[] dirs = Player.Direction.values();
        this.direction = dirs[random.nextInt(dirs.length)];
    }

    /**
     * Kiểm tra xem nếu đi tới vị trí (targetX, targetY) thì có va chạm với quả bom nào không.
     * Kẻ địch thông thường không thể đi xuyên qua bom.
     */
    protected boolean checkBombCollision(int targetX, int targetY, List<Bomb> bombs) {
        if (bombs == null || bombs.isEmpty()) {
            return false;
        }

        Rectangle targetBox = new Rectangle(targetX, targetY, width, height);
        for (Bomb b : bombs) {
            if (!b.isExploded() && targetBox.intersects(b.getBounds())) {
                return true;
            }
        }
        return false;
    }

    // --- Các hàm Getter & Setter ---
    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        this.alive = false;
    }

    public int getScoreReward() {
        return scoreReward;
    }

    public Player.Direction getDirection() {
        return direction;
    }
}

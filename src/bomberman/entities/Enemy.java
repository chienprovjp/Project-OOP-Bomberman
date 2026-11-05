package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

/**
 * Lớp trừu tượng định nghĩa các thuộc tính và hành vi chung của Kẻ địch (Enemy).
 */
public abstract class Enemy extends Entity {
    protected int speed = 2;
    protected Player.Direction direction = Player.Direction.RIGHT;
    protected boolean alive = true;
    protected int scoreReward = 100;
    protected static final Random random = new Random();

    public Enemy(int startX, int startY, int width, int height, int speed) {
        super(startX, startY, width, height);
        this.speed = speed;
        changeRandomDirection();
    }

    // Từng loại quái vật con sẽ tự cài đặt logic di chuyển riêng
    public abstract void move(GameMap map, List<Bomb> bombs);

    // Đổi sang một hướng ngẫu nhiên
    protected void changeRandomDirection() {
        Player.Direction[] dirs = Player.Direction.values();
        this.direction = dirs[random.nextInt(dirs.length)];
    }

    // Kiểm tra va chạm với các quả bom đang hoạt động
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

    public boolean isAlive() { return alive; }
    public void kill() { this.alive = false; }
    public int getScoreReward() { return scoreReward; }
    public Player.Direction getDirection() { return direction; }
}

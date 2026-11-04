package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kẻ địch cơ bản Ballom (quả bóng cam di chuyển ngẫu nhiên).
 * Không đi xuyên được tường đá, tường gạch hay quả bom.
 */
public class Ballom extends Enemy {
    private int animationTick = 0;
    private int deathAnimation = 0;

    public Ballom(int startX, int startY) {
        super(startX, startY, (int) (GameMap.TILE_SIZE * 0.70), (int) (GameMap.TILE_SIZE * 0.70), 2);
        this.scoreReward = 100;
    }

    @Override
    public void update() {
        animationTick++;
        if (!alive) {
            deathAnimation++;
        }
    }

    @Override
    public void move(GameMap map, List<Bomb> bombs) {
        if (!alive) return;

        int dx = 0;
        int dy = 0;

        switch (direction) {
            case UP    -> dy = -speed;
            case DOWN  -> dy = speed;
            case LEFT  -> dx = -speed;
            case RIGHT -> dx = speed;
        }

        boolean willCollide = map.checkCollision(x + dx, y + dy, width, height)
                || checkBombCollision(x + dx, y + dy, bombs);

        if (!willCollide) {
            x += dx;
            y += dy;

            // Tại các ngã rẽ thông thoáng, có 2% xác suất tự đổi hướng ngẫu nhiên
            if (random.nextInt(100) < 2) {
                tryChangeToAvailableDirection(map, bombs);
            }
        } else {
            // Khi va chạm vật cản -> bắt buộc đổi sang một hướng đi được
            tryChangeToAvailableDirection(map, bombs);
        }
    }

    private void tryChangeToAvailableDirection(GameMap map, List<Bomb> bombs) {
        List<Player.Direction> candidates = new ArrayList<>();
        for (Player.Direction dir : Player.Direction.values()) {
            if (dir == this.direction) continue; // Ưu tiên hướng khác
            int testDx = 0;
            int testDy = 0;
            switch (dir) {
                case UP    -> testDy = -speed;
                case DOWN  -> testDy = speed;
                case LEFT  -> testDx = -speed;
                case RIGHT -> testDx = speed;
            }
            if (!map.checkCollision(x + testDx, y + testDy, width, height)
                    && !checkBombCollision(x + testDx, y + testDy, bombs)) {
                candidates.add(dir);
            }
        }

        if (!candidates.isEmpty()) {
            Collections.shuffle(candidates);
            this.direction = candidates.get(0);
        } else {
            // Nếu không có hướng rẽ nào, quay đầu ngược lại
            this.direction = switch (this.direction) {
                case UP    -> Player.Direction.DOWN;
                case DOWN  -> Player.Direction.UP;
                case LEFT  -> Player.Direction.RIGHT;
                case RIGHT -> Player.Direction.LEFT;
            };
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            // Hiệu ứng khi bị tiêu diệt
            g.setColor(new Color(251, 146, 60, Math.max(0, 200 - deathAnimation * 10)));
            g.fillOval(x - deathAnimation / 2, y - deathAnimation / 2, width + deathAnimation, height + deathAnimation);
            return;
        }

        // Tọa độ vẽ
        int drawX = x - (GameMap.TILE_SIZE - width) / 2;
        int drawY = y - (GameMap.TILE_SIZE - height) / 2;

        // 1. Bóng mờ dưới chân
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 2, y + height - 3, width - 4, 6);

        // Hiệu ứng phập phồng của quả bóng
        int pulse = (animationTick / 10) % 2 == 0 ? 1 : 0;
        int bodyW = width + pulse;
        int bodyH = height - pulse;
        int bodyX = x - pulse / 2;
        int bodyY = y + pulse;

        // 2. Thân Ballom (màu cam đỏ tươi sáng đặc trưng)
        g.setColor(new Color(249, 115, 22)); // Màu cam rực
        g.fillOval(bodyX, bodyY, bodyW, bodyH);

        g.setColor(new Color(194, 65, 12)); // Viền cam đậm
        g.drawOval(bodyX, bodyY, bodyW, bodyH);

        // 3. Vệt sáng highlight trên đầu
        g.setColor(new Color(255, 255, 255, 140));
        g.fillOval(bodyX + bodyW / 4, bodyY + 4, bodyW / 3, bodyH / 4);

        // 4. Mắt to nhìn theo hướng di chuyển
        int eyeOffsetX = switch (direction) {
            case LEFT  -> -3;
            case RIGHT -> 3;
            default    -> 0;
        };
        int eyeOffsetY = switch (direction) {
            case UP    -> -2;
            case DOWN  -> 2;
            default    -> 0;
        };

        int eye1X = bodyX + bodyW / 2 - 8 + eyeOffsetX;
        int eye2X = bodyX + bodyW / 2 + 2 + eyeOffsetX;
        int eyeY  = bodyY + bodyH / 3 + eyeOffsetY;

        // Tròng trắng
        g.setColor(Color.WHITE);
        g.fillOval(eye1X, eyeY, 6, 8);
        g.fillOval(eye2X, eyeY, 6, 8);

        // Con ngươi đen
        g.setColor(Color.BLACK);
        int pupilOffsetX = eyeOffsetX / 2;
        int pupilOffsetY = eyeOffsetY / 2;
        g.fillOval(eye1X + 2 + pupilOffsetX, eyeY + 2 + pupilOffsetY, 3, 4);
        g.fillOval(eye2X + 2 + pupilOffsetX, eyeY + 2 + pupilOffsetY, 3, 4);

        // 5. Miệng cười ngộ nghĩnh
        g.setColor(new Color(124, 45, 18));
        g.drawArc(bodyX + bodyW / 2 - 4 + eyeOffsetX, bodyY + bodyH / 2 + 4, 8, 5, 0, -180);
    }
}

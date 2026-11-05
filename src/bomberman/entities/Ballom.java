package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kẻ địch Ballom: quái vật quả bóng cam di chuyển và tự đổi hướng khi gặp vật cản.
 */
public class Ballom extends Enemy {
    private int animationTick = 0;
    private int deathAnimation = 0;

    public Ballom(int startX, int startY) {
        super(startX, startY,
              (int) (GameMap.TILE_SIZE * 0.70),
              (int) (GameMap.TILE_SIZE * 0.70),
              2);
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

        // Tính độ dời theo hướng đang đi
        int dx = 0;
        int dy = 0;
        switch (direction) {
            case UP    -> dy = -speed;
            case DOWN  -> dy = speed;
            case LEFT  -> dx = -speed;
            case RIGHT -> dx = speed;
        }

        // Kiểm tra va chạm với bản đồ và bom
        boolean willCollide = map.checkCollision(x + dx, y + dy, width, height)
                || checkBombCollision(x + dx, y + dy, bombs);

        if (!willCollide) {
            x += dx;
            y += dy;
            // Xác suất ~2% tự đổi hướng tại ngã rẽ
            if (random.nextInt(100) < 2) {
                tryChangeToAvailableDirection(map, bombs);
            }
        } else {
            // Gặp vật cản -> rẽ sang hướng thông thoáng
            tryChangeToAvailableDirection(map, bombs);
        }
    }

    // Tìm các hướng đi không bị cản để rẽ
    private void tryChangeToAvailableDirection(GameMap map, List<Bomb> bombs) {
        List<Player.Direction> candidates = new ArrayList<>();

        for (Player.Direction dir : Player.Direction.values()) {
            if (dir == this.direction) continue;

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
            // Ngõ cụt: quay đầu 180 độ
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
        // Hiệu ứng mờ dần khi chết
        if (!alive) {
            int alpha = Math.max(0, 200 - deathAnimation * 10);
            g.setColor(new Color(251, 146, 60, alpha));
            g.fillOval(x - deathAnimation / 2, y - deathAnimation / 2,
                    width + deathAnimation, height + deathAnimation);
            return;
        }

        // Bóng mờ
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 2, y + height - 3, width - 4, 6);

        // Thân bóng màu cam
        int pulse = (animationTick / 10) % 2 == 0 ? 1 : 0;
        int bodyW = width + pulse;
        int bodyH = height - pulse;
        int bodyX = x - pulse / 2;
        int bodyY = y + pulse;

        g.setColor(new Color(249, 115, 22));
        g.fillOval(bodyX, bodyY, bodyW, bodyH);
        g.setColor(new Color(194, 65, 12));
        g.drawOval(bodyX, bodyY, bodyW, bodyH);

        // Vệt sáng
        g.setColor(new Color(255, 255, 255, 140));
        g.fillOval(bodyX + bodyW / 4, bodyY + 4, bodyW / 3, bodyH / 4);

        // Mắt nhìn theo hướng di chuyển
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

        g.setColor(Color.WHITE);
        g.fillOval(eye1X, eyeY, 6, 8);
        g.fillOval(eye2X, eyeY, 6, 8);

        g.setColor(Color.BLACK);
        g.fillOval(eye1X + 2 + eyeOffsetX / 2, eyeY + 2 + eyeOffsetY / 2, 3, 4);
        g.fillOval(eye2X + 2 + eyeOffsetX / 2, eyeY + 2 + eyeOffsetY / 2, 3, 4);

        // Miệng
        g.setColor(new Color(124, 45, 18));
        g.drawArc(bodyX + bodyW / 2 - 4 + eyeOffsetX, bodyY + bodyH / 2 + 4, 8, 5, 0, -180);
    }
}

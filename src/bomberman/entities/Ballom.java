package bomberman.entities;

import bomberman.gui.GameMap;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kẻ địch Ballom: quái vật quả bóng cam di chuyển và tự đổi hướng khi gặp vật cản.
 */
public class Ballom extends Enemy {
    private int animationTick = 0;
    private int deathAnimation = 0;

    // Ảnh kẻ địch Ballom
    private static BufferedImage sprite = loadBallomSprite();

    public Ballom(int startX, int startY) {
        super(startX, startY,
              (int) (GameMap.TILE_SIZE * 0.70),
              (int) (GameMap.TILE_SIZE * 0.70),
              2);
        this.scoreReward = 100;
    }

    private static BufferedImage loadBallomSprite() {
        String fileName = "ballom.png";
        try {
            var url = Ballom.class.getResource("/assets/images/" + fileName);
            if (url != null) return ImageIO.read(url);
        } catch (Exception ignored) {}

        String[] paths = {
            "assets/images/" + fileName,
            "BTL/Project-OOP-Bomberman/assets/images/" + fileName,
            "Project-OOP-Bomberman/assets/images/" + fileName
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try {
                    return ImageIO.read(f);
                } catch (Exception ignored) {}
            }
        }
        return null;
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

        int pulse = (animationTick / 10) % 2 == 0 ? 1 : 0;
        int bodyW = width + pulse;
        int bodyH = height - pulse;
        int bodyX = x - pulse / 2;
        int bodyY = y + pulse;

        if (sprite != null) {
            // Vẽ ảnh ballom.png chuẩn bạch tuộc cam
            g.drawImage(sprite, bodyX, bodyY, bodyW, bodyH, null);
        } else {
            // Vẽ dự phòng hình bạch tuộc cam nếu thiếu ảnh
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(x + 2, y + height - 3, width - 4, 6);

            // Chân xúc tu bên dưới
            g.setColor(new Color(230, 92, 80));
            g.fillOval(bodyX + 2, bodyY + bodyH - 6, 7, 7);
            g.fillOval(bodyX + 8, bodyY + bodyH - 4, 7, 7);
            g.fillOval(bodyX + bodyW - 15, bodyY + bodyH - 4, 7, 7);
            g.fillOval(bodyX + bodyW - 9, bodyY + bodyH - 6, 7, 7);

            // Thân hình vòm san hô
            g.setColor(new Color(230, 92, 80));
            g.fillOval(bodyX, bodyY + 3, bodyW, bodyH - 5);
            g.setColor(new Color(53, 34, 61));
            g.drawOval(bodyX, bodyY + 3, bodyW, bodyH - 5);

            // Cọng ăng-ten trên đỉnh
            g.fillRect(bodyX + bodyW / 2 - 1, bodyY, 2, 4);

            // Vùng sáng đầu
            g.setColor(new Color(246, 138, 124));
            g.fillOval(bodyX + 3, bodyY + 5, bodyW - 6, (bodyH - 5) / 2);
            g.setColor(new Color(255, 255, 255, 160));
            g.fillOval(bodyX + 6, bodyY + 6, 7, 4);

            // Hướng nhìn mắt
            int eyeOffsetX = switch (direction) {
                case LEFT  -> -2;
                case RIGHT -> 2;
                default    -> 0;
            };
            int eyeOffsetY = switch (direction) {
                case UP    -> -2;
                case DOWN  -> 2;
                default    -> 0;
            };

            int eye1X = bodyX + bodyW / 2 - 8 + eyeOffsetX;
            int eye2X = bodyX + bodyW / 2 + 2 + eyeOffsetX;
            int eyeY  = bodyY + bodyH / 3 + 1 + eyeOffsetY;

            // Mắt dọc viền trắng
            g.setColor(Color.WHITE);
            g.fillOval(eye1X, eyeY, 6, 9);
            g.fillOval(eye2X, eyeY, 6, 9);

            g.setColor(new Color(26, 23, 39));
            g.fillOval(eye1X + 1, eyeY + 1, 4, 7);
            g.fillOval(eye2X + 1, eyeY + 1, 4, 7);

            g.setColor(Color.WHITE);
            g.fillOval(eye1X + 2, eyeY + 2, 2, 3);
            g.fillOval(eye2X + 2, eyeY + 2, 2, 3);

            // Miệng
            g.setColor(new Color(53, 34, 61));
            g.fillRect(bodyX + bodyW / 2 - 3 + eyeOffsetX, bodyY + bodyH / 2 + 5, 6, 2);
        }
    }
}

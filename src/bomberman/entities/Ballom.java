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
 * Kẻ địch Ballom: quái vật quả bóng cam di chuyển bám tim đường và tự đổi hướng khi gặp vật cản.
 */
public class Ballom extends Enemy {
    private int animationTick = 0;
    private int deathAnimation = 0;
    private int turnCooldown = 0; // Cooldown tránh rẽ liên tục tại cùng một giao lộ

    // Ảnh kẻ địch Ballom
    private static BufferedImage sprite = loadBallomSprite();

    public Ballom(int startX, int startY) {
        // Hitbox 44x44 gần kín ô gạch 48x48 (chỉ hở 2px mỗi bên), triệt tiêu lắc lư
        super(startX, startY, 44, 44, 2);
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
        if (turnCooldown > 0) {
            turnCooldown--;
        }
        if (!alive) {
            deathAnimation++;
        }
    }

    @Override
    public void move(GameMap map, List<Bomb> bombs) {
        if (!alive) return;

        int tileSize = GameMap.TILE_SIZE;
        boolean moved = false;

        // Căn tim đường tự động theo trục vuông góc và di chuyển
        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            x = alignToCenter(x, width, tileSize, speed);

            int dy = (direction == Player.Direction.UP) ? -speed : speed;
            if (!map.checkCollision(x, y + dy, width, height) && !checkBombCollision(x, y + dy, bombs)) {
                y += dy;
                moved = true;
            }
        } else {
            y = alignToCenter(y, height, tileSize, speed);

            int dx = (direction == Player.Direction.LEFT) ? -speed : speed;
            if (!map.checkCollision(x + dx, y, width, height) && !checkBombCollision(x + dx, y, bombs)) {
                x += dx;
                moved = true;
            }
        }

        if (moved) {
            // Khi tới đúng tâm giao lộ và hết cooldown, có tỉ lệ rẽ sang nhánh đường khác
            if (turnCooldown == 0 && isAtTileCenter(tileSize) && random.nextInt(100) < 30) {
                tryTurnAtIntersection(map, bombs);
            }
        } else {
            // Gặp vật cản (tường hoặc bom) -> bắt buộc đổi hướng
            tryChangeToAvailableDirection(map, bombs);
        }
    }

    // Kiểm tra Ballom có đang ở gần tâm của ô hiện tại trên trục đang di chuyển không
    private boolean isAtTileCenter(int tileSize) {
        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            int row = (y + height / 2) / tileSize;
            int targetY = row * tileSize + (tileSize - height) / 2;
            return Math.abs(y - targetY) <= speed;
        } else {
            int col = (x + width / 2) / tileSize;
            int targetX = col * tileSize + (tileSize - width) / 2;
            return Math.abs(x - targetX) <= speed;
        }
    }

    // Thử rẽ sang nhánh vuông góc tại giao lộ (ngã 3, ngã 4)
    private void tryTurnAtIntersection(GameMap map, List<Bomb> bombs) {
        List<Player.Direction> perpendicularDirs = new ArrayList<>();

        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            perpendicularDirs.add(Player.Direction.LEFT);
            perpendicularDirs.add(Player.Direction.RIGHT);
        } else {
            perpendicularDirs.add(Player.Direction.UP);
            perpendicularDirs.add(Player.Direction.DOWN);
        }

        List<Player.Direction> available = new ArrayList<>();
        for (Player.Direction dir : perpendicularDirs) {
            int testDx = 0;
            int testDy = 0;
            switch (dir) {
                case UP    -> testDy = -speed;
                case DOWN  -> testDy = speed;
                case LEFT  -> testDx = -speed;
                case RIGHT -> testDx = speed;
            }

            int testX = x;
            int testY = y;
            if (dir == Player.Direction.UP || dir == Player.Direction.DOWN) {
                int col = (x + width / 2) / GameMap.TILE_SIZE;
                testX = col * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - width) / 2;
            } else {
                int row = (y + height / 2) / GameMap.TILE_SIZE;
                testY = row * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - height) / 2;
            }

            if (!map.checkCollision(testX + testDx, testY + testDy, width, height)
                    && !checkBombCollision(testX + testDx, testY + testDy, bombs)) {
                available.add(dir);
            }
        }

        if (!available.isEmpty()) {
            Collections.shuffle(available);
            this.direction = available.get(0);
            if (this.direction == Player.Direction.UP || this.direction == Player.Direction.DOWN) {
                int col = (x + width / 2) / GameMap.TILE_SIZE;
                this.x = col * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - width) / 2;
            } else {
                int row = (y + height / 2) / GameMap.TILE_SIZE;
                this.y = row * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - height) / 2;
            }
            this.turnCooldown = 20;
        }
    }

    // Tìm các hướng đi không bị cản để rẽ khi gặp vật cản
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

            int testX = x;
            int testY = y;
            if (dir == Player.Direction.UP || dir == Player.Direction.DOWN) {
                int col = (x + width / 2) / GameMap.TILE_SIZE;
                testX = col * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - width) / 2;
            } else {
                int row = (y + height / 2) / GameMap.TILE_SIZE;
                testY = row * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - height) / 2;
            }

            if (!map.checkCollision(testX + testDx, testY + testDy, width, height)
                    && !checkBombCollision(testX + testDx, testY + testDy, bombs)) {
                candidates.add(dir);
            }
        }

        if (!candidates.isEmpty()) {
            Collections.shuffle(candidates);
            this.direction = candidates.get(0);
            if (this.direction == Player.Direction.UP || this.direction == Player.Direction.DOWN) {
                int col = (x + width / 2) / GameMap.TILE_SIZE;
                this.x = col * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - width) / 2;
            } else {
                int row = (y + height / 2) / GameMap.TILE_SIZE;
                this.y = row * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - height) / 2;
            }
            this.turnCooldown = 20;
        } else {
            // Ngõ cụt: quay đầu 180 độ
            this.direction = switch (this.direction) {
                case UP    -> Player.Direction.DOWN;
                case DOWN  -> Player.Direction.UP;
                case LEFT  -> Player.Direction.RIGHT;
                case RIGHT -> Player.Direction.LEFT;
            };
            this.turnCooldown = 20;
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

        // Kích thước hiển thị 44x44 (kèm nhịp phập phồng nhẹ), vừa khít hitbox 44x44
        int pulse = (animationTick / 10) % 2 == 0 ? 1 : 0;
        int spriteW = 44 + pulse;
        int spriteH = 44 - pulse;
        int drawX = x - (spriteW - width) / 2;
        int drawY = y - (spriteH - height) / 2;

        if (sprite != null) {
            // Vẽ ảnh ballom.png chuẩn kích thước 44x44
            g.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
        } else {
            // Vẽ dự phòng nếu thiếu ảnh
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(x + 2, y + height - 3, width - 4, 6);

            g.setColor(new Color(249, 115, 22));
            g.fillOval(drawX, drawY, spriteW, spriteH);
            g.setColor(new Color(194, 65, 12));
            g.drawOval(drawX, drawY, spriteW, spriteH);

            g.setColor(new Color(255, 255, 255, 140));
            g.fillOval(drawX + spriteW / 4, drawY + 4, spriteW / 3, spriteH / 4);

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

            int eye1X = drawX + spriteW / 2 - 8 + eyeOffsetX;
            int eye2X = drawX + spriteW / 2 + 2 + eyeOffsetX;
            int eyeY  = drawY + spriteH / 3 + eyeOffsetY;

            g.setColor(Color.WHITE);
            g.fillOval(eye1X, eyeY, 6, 8);
            g.fillOval(eye2X, eyeY, 6, 8);

            g.setColor(Color.BLACK);
            g.fillOval(eye1X + 2 + eyeOffsetX / 2, eyeY + 2 + eyeOffsetY / 2, 3, 4);
            g.fillOval(eye2X + 2 + eyeOffsetX / 2, eyeY + 2 + eyeOffsetY / 2, 3, 4);

            g.setColor(new Color(124, 45, 18));
            g.drawArc(drawX + spriteW / 2 - 4 + eyeOffsetX, drawY + spriteH / 2 + 4, 8, 5, 0, -180);
        }
    }
}

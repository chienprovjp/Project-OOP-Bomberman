package bomberman.model;

import bomberman.util.ResourceLoader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quái Ghost: Có khả năng bay xuyên qua Tường gạch (BRICK), chỉ bị cản bởi
 * Tường đá (WALL) và Bom. Điểm thưởng: 300.
 */
public class Ghost extends Enemy {

    private int animationTick = 0;
    private int turnCooldown = 0;

    private static BufferedImage sprite = ResourceLoader.loadImage("ghost.png");

    public Ghost(int startX, int startY) {
        super(startX, startY, 44, 44, 2);
        this.scoreReward = 300;
    }

    @Override
    public void update() {
        super.update();
        if (!alive) return;

        animationTick++;
        if (turnCooldown > 0) {
            turnCooldown--;
        }
    }

    @Override
    public void move(GameMap map, List<Bomb> bombs) {
        move(map, bombs, null);
    }

    @Override
    public void move(GameMap map, List<Bomb> bombs, Player player) {
        if (!alive) return;

        int tileSize = GameMap.TILE_SIZE;
        boolean moved = false;

        // Căn tim đường và bay xuyên qua BRICK (chỉ cản bởi WALL và Bom)
        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            x = alignToCenter(x, width, tileSize, speed);

            int dy = (direction == Player.Direction.UP) ? -speed : speed;
            if (!map.checkWallCollision(x, y + dy, width, height) && !checkBombCollision(x, y + dy, bombs)) {
                y += dy;
                moved = true;
            }
        } else {
            y = alignToCenter(y, height, tileSize, speed);

            int dx = (direction == Player.Direction.LEFT) ? -speed : speed;
            if (!map.checkWallCollision(x + dx, y, width, height) && !checkBombCollision(x + dx, y, bombs)) {
                x += dx;
                moved = true;
            }
        }

        // Xử lý đổi hướng
        if (moved) {
            if (turnCooldown == 0 && isAtTileCenter(tileSize)) {
                if (player != null && isPlayerNearby(player, 5) && random.nextInt(100) < 40) {
                    tryTurnTowardsPlayer(map, bombs, player);
                } else if (random.nextInt(100) < 30) {
                    tryTurnAtIntersection(map, bombs);
                }
            }
        } else {
            // Khi gặp Tường đá hoặc Bom
            tryChangeToAvailableDirection(map, bombs);
        }
    }

    // Kiểm tra người chơi có trong bán kính tileRadius ô hay không
    private boolean isPlayerNearby(Player player, int tileRadius) {
        int tileSize = GameMap.TILE_SIZE;
        int myCol = (x + width / 2) / tileSize;
        int myRow = (y + height / 2) / tileSize;
        int pCol = (player.getX() + player.getWidth() / 2) / tileSize;
        int pRow = (player.getY() + player.getHeight() / 2) / tileSize;

        return (Math.abs(myCol - pCol) + Math.abs(myRow - pRow)) <= tileRadius;
    }

    // Thử rẽ vuông góc về phía người chơi
    private void tryTurnTowardsPlayer(GameMap map, List<Bomb> bombs, Player player) {
        int tileSize = GameMap.TILE_SIZE;
        int myCol = (x + width / 2) / tileSize;
        int myRow = (y + height / 2) / tileSize;
        int pCol = (player.getX() + player.getWidth() / 2) / tileSize;
        int pRow = (player.getY() + player.getHeight() / 2) / tileSize;

        List<Player.Direction> perpendicularDirs = new ArrayList<>();
        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            if (pCol > myCol) perpendicularDirs.add(Player.Direction.RIGHT);
            if (pCol < myCol) perpendicularDirs.add(Player.Direction.LEFT);
        } else {
            if (pRow > myRow) perpendicularDirs.add(Player.Direction.DOWN);
            if (pRow < myRow) perpendicularDirs.add(Player.Direction.UP);
        }

        for (Player.Direction dir : perpendicularDirs) {
            if (canMoveInDirection(dir, map, bombs)) {
                snapAndTurn(dir);
                return;
            }
        }
    }

    // Thử rẽ sang các nhánh vuông góc tại giao lộ
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
            if (canMoveInDirection(dir, map, bombs)) {
                available.add(dir);
            }
        }

        if (!available.isEmpty()) {
            Collections.shuffle(available);
            snapAndTurn(available.get(0));
        }
    }

    // Chọn hướng đi khả dụng khi bị cản bởi tường đá hoặc bom
    private void tryChangeToAvailableDirection(GameMap map, List<Bomb> bombs) {
        List<Player.Direction> candidates = new ArrayList<>();
        for (Player.Direction dir : Player.Direction.values()) {
            if (dir == this.direction) continue;
            if (canMoveInDirection(dir, map, bombs)) {
                candidates.add(dir);
            }
        }

        if (!candidates.isEmpty()) {
            Collections.shuffle(candidates);
            snapAndTurn(candidates.get(0));
        } else {
            snapAndTurn(getOppositeDirection(this.direction));
        }
    }

    private boolean canMoveInDirection(Player.Direction dir, GameMap map, List<Bomb> bombs) {
        int tileSize = GameMap.TILE_SIZE;
        int col = (x + width / 2) / tileSize;
        int row = (y + height / 2) / tileSize;

        int centerX = col * tileSize + (tileSize - width) / 2;
        int centerY = row * tileSize + (tileSize - height) / 2;

        int testX = centerX;
        int testY = centerY;
        switch (dir) {
            case UP    -> testY -= speed;
            case DOWN  -> testY += speed;
            case LEFT  -> testX -= speed;
            case RIGHT -> testX += speed;
        }

        return !map.checkWallCollision(testX, testY, width, height)
                && !checkBombCollision(testX, testY, bombs);
    }

    private void snapAndTurn(Player.Direction newDir) {
        this.direction = newDir;
        int tileSize = GameMap.TILE_SIZE;
        if (this.direction == Player.Direction.UP || this.direction == Player.Direction.DOWN) {
            int col = (x + width / 2) / tileSize;
            this.x = col * tileSize + (tileSize - width) / 2;
        } else {
            int row = (y + height / 2) / tileSize;
            this.y = row * tileSize + (tileSize - height) / 2;
        }
        this.turnCooldown = 20;
    }

    private Player.Direction getOppositeDirection(Player.Direction dir) {
        return switch (dir) {
            case UP    -> Player.Direction.DOWN;
            case DOWN  -> Player.Direction.UP;
            case LEFT  -> Player.Direction.RIGHT;
            case RIGHT -> Player.Direction.LEFT;
        };
    }

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

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            drawDeathEffect(g, new Color(168, 85, 247));
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hiệu ứng bay bồng bềnh hình sin
        int floatOffsetY = (int) (Math.sin(animationTick * 0.12) * 3);
        int drawX = x;
        int drawY = y + floatOffsetY;

        // Bóng mờ dưới chân
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(x + 4, y + height - 2, width - 8, 5);

        // Hào quang tím bao quanh
        g2d.setColor(new Color(168, 85, 247, 45));
        g2d.fillOval(drawX - 4, drawY - 4, width + 8, height + 8);

        // Thân hình bán trong suốt (Alpha = 0.85)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));

        if (sprite != null) {
            if (direction == Player.Direction.LEFT) {
                g2d.drawImage(sprite, drawX + width, drawY, -width, height, null);
            } else {
                g2d.drawImage(sprite, drawX, drawY, width, height, null);
            }
        } else {
            drawFallback(g2d, drawX, drawY);
        }

        g2d.dispose();
    }

    private void drawFallback(Graphics2D g, int drawX, int drawY) {
        g.setColor(new Color(168, 85, 247));
        g.fillRoundRect(drawX + 4, drawY + 2, width - 8, height - 6, 20, 20);

        g.setColor(Color.RED);
        int eyeOffset = (direction == Player.Direction.LEFT) ? -4 : (direction == Player.Direction.RIGHT ? 4 : 0);
        g.fillOval(drawX + 12 + eyeOffset, drawY + 12, 6, 8);
        g.fillOval(drawX + 26 + eyeOffset, drawY + 12, 6, 8);
    }
}

package bomberman.model;

import bomberman.ai.MoveStrategy;
import bomberman.ai.RandomStrategy;
import bomberman.component.Entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lớp trừu tượng định nghĩa các thuộc tính và hành vi chung của Kẻ địch (Enemy).
 * Tích hợp cả:
 * 1. Hệ thống hoạt ảnh chết (chớp sáng + tan biến thành hạt linh hồn).
 * 2. Strategy Pattern (MoveStrategy: Random / BFS / A*) và di chuyển căn tim đường dùng chung.
 */
public abstract class Enemy extends Entity {
    protected int speed = 2;
    protected Player.Direction direction = Player.Direction.RIGHT;
    protected boolean alive = true;
    protected boolean deathFinished = false;
    protected int deathAnimationTick = 0;
    protected static final int DEATH_ANIM_DURATION = 24; // ~0.4 giây diễn hoạt khi chết
    protected int scoreReward = 100;
    protected boolean canPassBrick = false;
    protected int turnCooldown = 0;
    protected MoveStrategy moveStrategy = new RandomStrategy();
    protected static final Random random = new Random();

    public Enemy(int startX, int startY, int width, int height, int speed) {
        super(startX, startY, width, height);
        this.speed = speed;
        changeRandomDirection();
    }

    // Từng loại quái vật con sẽ tự cài đặt logic di chuyển riêng
    public abstract void move(GameMap map, List<Bomb> bombs);

    // Nạp chồng hỗ trợ truyền Player (phục vụ AI BFS / A*)
    public void move(GameMap map, List<Bomb> bombs, Player player) {
        moveAlongGrid(map, bombs, player);
    }

    @Override
    public void update() {
        if (!alive) {
            deathAnimationTick++;
            if (deathAnimationTick >= DEATH_ANIM_DURATION) {
                deathFinished = true;
            }
            return;
        }
        if (turnCooldown > 0) {
            turnCooldown--;
        }
    }

    public MoveStrategy getMoveStrategy() {
        return moveStrategy;
    }

    public void setMoveStrategy(MoveStrategy moveStrategy) {
        if (moveStrategy != null) {
            this.moveStrategy = moveStrategy;
        }
    }

    public boolean canPassBrick() {
        return canPassBrick;
    }

    /**
     * Cơ chế di chuyển bám tim đường kết hợp MoveStrategy (Random / BFS / A*).
     * Khi ở đúng tâm ô gạch và hết cooldown, quái sẽ hỏi chiến lược AI để chọn hướng tối ưu.
     */
    protected void moveAlongGrid(GameMap map, List<Bomb> bombs, Player target) {
        if (!alive) return;

        int tileSize = GameMap.TILE_SIZE;

        if (turnCooldown == 0 && isAtTileCenter(tileSize) && moveStrategy != null) {
            Player.Direction aiDir = moveStrategy.chooseDirection(this, map, bombs, target);
            if (aiDir != null && canMoveInDirection(aiDir, map, bombs)) {
                if (aiDir != this.direction) {
                    this.direction = aiDir;
                    snapAxisToTileCenter(aiDir, tileSize);
                    this.turnCooldown = 14;
                } else {
                    this.turnCooldown = 10;
                }
            }
        }

        boolean moved = stepCurrentDirection(map, bombs, tileSize);
        if (!moved) {
            Player.Direction aiDir = (moveStrategy != null) ? moveStrategy.chooseDirection(this, map, bombs, target) : null;
            if (aiDir != null && canMoveInDirection(aiDir, map, bombs)) {
                this.direction = aiDir;
                snapAxisToTileCenter(aiDir, tileSize);
                this.turnCooldown = 14;
            } else {
                tryPickAvailableDirection(map, bombs);
            }
        }
    }

    protected boolean stepCurrentDirection(GameMap map, List<Bomb> bombs, int tileSize) {
        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            x = alignToCenter(x, width, tileSize, speed);
            int dy = (direction == Player.Direction.UP) ? -speed : speed;
            if (!checkTileObstacle(x, y + dy, map, bombs)) {
                y += dy;
                return true;
            }
        } else {
            y = alignToCenter(y, height, tileSize, speed);
            int dx = (direction == Player.Direction.LEFT) ? -speed : speed;
            if (!checkTileObstacle(x + dx, y, map, bombs)) {
                x += dx;
                return true;
            }
        }
        return false;
    }

    protected boolean checkTileObstacle(int targetX, int targetY, GameMap map, List<Bomb> bombs) {
        if (checkBombCollision(targetX, targetY, bombs)) {
            return true;
        }
        return canPassBrick
                ? map.checkWallCollision(targetX, targetY, width, height)
                : map.checkCollision(targetX, targetY, width, height);
    }

    protected boolean isAtTileCenter(int tileSize) {
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

    protected boolean canMoveInDirection(Player.Direction dir, GameMap map, List<Bomb> bombs) {
        int tileSize = GameMap.TILE_SIZE;
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
            int col = (x + width / 2) / tileSize;
            testX = col * tileSize + (tileSize - width) / 2;
        } else {
            int row = (y + height / 2) / tileSize;
            testY = row * tileSize + (tileSize - height) / 2;
        }
        return !checkTileObstacle(testX + testDx, testY + testDy, map, bombs);
    }

    protected void snapAxisToTileCenter(Player.Direction dir, int tileSize) {
        if (dir == Player.Direction.UP || dir == Player.Direction.DOWN) {
            int col = (x + width / 2) / tileSize;
            this.x = col * tileSize + (tileSize - width) / 2;
        } else {
            int row = (y + height / 2) / tileSize;
            this.y = row * tileSize + (tileSize - height) / 2;
        }
    }

    protected void tryPickAvailableDirection(GameMap map, List<Bomb> bombs) {
        List<Player.Direction> candidates = new ArrayList<>();
        for (Player.Direction dir : Player.Direction.values()) {
            if (dir != this.direction && canMoveInDirection(dir, map, bombs)) {
                candidates.add(dir);
            }
        }
        if (!candidates.isEmpty()) {
            Collections.shuffle(candidates);
            this.direction = candidates.get(0);
            snapAxisToTileCenter(this.direction, GameMap.TILE_SIZE);
        } else {
            this.direction = switch (this.direction) {
                case UP    -> Player.Direction.DOWN;
                case DOWN  -> Player.Direction.UP;
                case LEFT  -> Player.Direction.RIGHT;
                case RIGHT -> Player.Direction.LEFT;
            };
        }
        this.turnCooldown = 16;
    }

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

    // Hiệu ứng khi chết: chớp sáng rồi tan biến thành hạt linh hồn
    protected void drawDeathEffect(Graphics g, Color primaryColor) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = x + width / 2;
        int cy = y + height / 2;

        if (deathAnimationTick < 7) {
            // Chớp sáng & rung giật nhẹ
            int shake = (deathAnimationTick % 2 == 0) ? 2 : -2;
            Color flashCol = (deathAnimationTick % 2 == 0) ? new Color(255, 255, 255, 230) : primaryColor;
            g2d.setColor(flashCol);
            g2d.fillOval(x + shake, y, width, height);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x + shake, y, width, height);
        } else {
            // Hạt linh hồn bay lên và mờ dần
            float progress = (deathAnimationTick - 7) / (float) (DEATH_ANIM_DURATION - 7);
            float alpha = Math.max(0.0f, 1.0f - progress);
            int spread = (int) (progress * 22);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int ringSize = (int) (width * (0.8f + progress * 0.6f));
            g2d.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), (int) (alpha * 120)));
            g2d.fillOval(cx - ringSize / 2, cy - ringSize / 2, ringSize, ringSize);

            int pCount = 6;
            for (int i = 0; i < pCount; i++) {
                double angle = i * (Math.PI * 2 / pCount);
                int px = cx + (int) (Math.cos(angle) * spread);
                int py = cy + (int) (Math.sin(angle) * spread) - (int) (progress * 14);
                int pSize = Math.max(2, (int) (6 * (1.0f - progress * 0.5f)));

                g2d.setColor(Color.WHITE);
                g2d.fillOval(px - pSize / 2, py - pSize / 2, pSize, pSize);
                g2d.setColor(primaryColor);
                g2d.drawOval(px - pSize / 2, py - pSize / 2, pSize, pSize);
            }
        }
        g2d.dispose();
    }

    public boolean isAlive() { return alive; }
    public boolean isDeathFinished() { return deathFinished; }
    public void kill() {
        if (!alive) return;
        this.alive = false;
        this.deathAnimationTick = 0;
    }
    public int getScoreReward() { return scoreReward; }
    public Player.Direction getDirection() { return direction; }
}

package bomberman.model;

import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Quái Oneal: Tốc độ cao (speed = 3 khi đuổi), phát hiện người chơi khi có Line of Sight
 * hoặc trong bán kính 7 ô và rượt đuổi theo khoảng cách Manhattan. Điểm thưởng: 200.
 */
public class Oneal extends Enemy {

    private static final int NORMAL_SPEED = 2; // Tốc độ tuần tra
    private static final int CHASE_SPEED = 3;  // Tốc độ rượt đuổi
    private static final int DETECT_RANGE = 7; // Bán kính phát hiện (7 ô)

    private boolean chasing = false;
    private int animationTick = 0;
    private int turnCooldown = 0;

    private static BufferedImage sprite = ResourceLoader.loadImage("oneal.png");

    static {
        if (sprite == null) {
            sprite = ResourceLoader.loadImage("ballom_blue.png");
        }
    }

    public Oneal(int startX, int startY) {
        super(startX, startY, 44, 44, NORMAL_SPEED);
        this.scoreReward = 200;
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
        int myCol = (x + width / 2) / tileSize;
        int myRow = (y + height / 2) / tileSize;

        // Phát hiện người chơi theo tầm nhìn hoặc bán kính 7 ô
        if (player != null) {
            int pCol = (player.getX() + player.getWidth() / 2) / tileSize;
            int pRow = (player.getY() + player.getHeight() / 2) / tileSize;

            int dist = Math.abs(myCol - pCol) + Math.abs(myRow - pRow);
            boolean hasLOS = hasClearLineOfSight(map, myCol, myRow, pCol, pRow);

            if (dist <= DETECT_RANGE || hasLOS) {
                this.chasing = true;
                this.speed = CHASE_SPEED;
            } else {
                this.chasing = false;
                this.speed = NORMAL_SPEED;
            }
        } else {
            this.chasing = false;
            this.speed = NORMAL_SPEED;
        }

        // Căn tim đường và di chuyển
        boolean moved = false;
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

        // Xử lý đổi hướng
        if (moved) {
            if (turnCooldown == 0 && isAtTileCenter(tileSize)) {
                if (chasing && player != null) {
                    tryTurnTowardsPlayer(map, bombs, player);
                } else if (random.nextInt(100) < 30) {
                    tryTurnAtIntersection(map, bombs);
                }
            }
        } else {
            // Khi gặp vật cản
            if (chasing && player != null) {
                chooseBestChaseDirection(map, bombs, player);
            } else {
                chooseRandomAvailableDirection(map, bombs);
            }
        }
    }

    // Kiểm tra đường thẳng tầm nhìn (Line of Sight) tới người chơi
    private boolean hasClearLineOfSight(GameMap map, int c1, int r1, int c2, int r2) {
        if (c1 == c2) {
            int startRow = Math.min(r1, r2) + 1;
            int endRow = Math.max(r1, r2) - 1;
            for (int r = startRow; r <= endRow; r++) {
                int tile = map.getTile(r, c1);
                if (tile == GameMap.WALL || tile == GameMap.BRICK) {
                    return false;
                }
            }
            return true;
        } else if (r1 == r2) {
            int startCol = Math.min(c1, c2) + 1;
            int endCol = Math.max(c1, c2) - 1;
            for (int c = startCol; c <= endCol; c++) {
                int tile = map.getTile(r1, c);
                if (tile == GameMap.WALL || tile == GameMap.BRICK) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Rẽ vuông góc tại ngã rẽ nếu rút ngắn khoảng cách tới người chơi
    private void tryTurnTowardsPlayer(GameMap map, List<Bomb> bombs, Player player) {
        int tileSize = GameMap.TILE_SIZE;
        int pCol = (player.getX() + player.getWidth() / 2) / tileSize;
        int pRow = (player.getY() + player.getHeight() / 2) / tileSize;
        int myCol = (x + width / 2) / tileSize;
        int myRow = (y + height / 2) / tileSize;

        int currentDist = Math.abs(myCol - pCol) + Math.abs(myRow - pRow);

        List<Player.Direction> perpendicular = new ArrayList<>();
        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            perpendicular.add(Player.Direction.LEFT);
            perpendicular.add(Player.Direction.RIGHT);
        } else {
            perpendicular.add(Player.Direction.UP);
            perpendicular.add(Player.Direction.DOWN);
        }

        Player.Direction bestDir = null;
        int bestDist = currentDist;

        for (Player.Direction dir : perpendicular) {
            if (canMoveInDirection(dir, map, bombs)) {
                int nextCol = myCol + (dir == Player.Direction.RIGHT ? 1 : (dir == Player.Direction.LEFT ? -1 : 0));
                int nextRow = myRow + (dir == Player.Direction.DOWN ? 1 : (dir == Player.Direction.UP ? -1 : 0));
                int d = Math.abs(nextCol - pCol) + Math.abs(nextRow - pRow);

                if (d < bestDist) {
                    bestDist = d;
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null) {
            snapAndTurn(bestDir);
        }
    }

    // Chọn hướng đi có khoảng cách Manhattan tới Player ngắn nhất
    private void chooseBestChaseDirection(GameMap map, List<Bomb> bombs, Player player) {
        int tileSize = GameMap.TILE_SIZE;
        int pCol = (player.getX() + player.getWidth() / 2) / tileSize;
        int pRow = (player.getY() + player.getHeight() / 2) / tileSize;
        int myCol = (x + width / 2) / tileSize;
        int myRow = (y + height / 2) / tileSize;

        List<Player.Direction> candidates = new ArrayList<>();
        for (Player.Direction dir : Player.Direction.values()) {
            if (dir == this.direction) continue;
            if (canMoveInDirection(dir, map, bombs)) {
                candidates.add(dir);
            }
        }

        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingInt(dir -> {
                int nextCol = myCol + (dir == Player.Direction.RIGHT ? 1 : (dir == Player.Direction.LEFT ? -1 : 0));
                int nextRow = myRow + (dir == Player.Direction.DOWN ? 1 : (dir == Player.Direction.UP ? -1 : 0));
                return Math.abs(nextCol - pCol) + Math.abs(nextRow - pRow);
            }));

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

        return !map.checkCollision(testX, testY, width, height)
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
        this.turnCooldown = 15;
    }

    private Player.Direction getOppositeDirection(Player.Direction dir) {
        return switch (dir) {
            case UP    -> Player.Direction.DOWN;
            case DOWN  -> Player.Direction.UP;
            case LEFT  -> Player.Direction.RIGHT;
            case RIGHT -> Player.Direction.LEFT;
        };
    }

    private void chooseRandomAvailableDirection(GameMap map, List<Bomb> bombs) {
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

    private void tryTurnAtIntersection(GameMap map, List<Bomb> bombs) {
        List<Player.Direction> perpendicular = new ArrayList<>();
        if (direction == Player.Direction.UP || direction == Player.Direction.DOWN) {
            perpendicular.add(Player.Direction.LEFT);
            perpendicular.add(Player.Direction.RIGHT);
        } else {
            perpendicular.add(Player.Direction.UP);
            perpendicular.add(Player.Direction.DOWN);
        }
        List<Player.Direction> available = new ArrayList<>();
        for (Player.Direction dir : perpendicular) {
            if (canMoveInDirection(dir, map, bombs)) {
                available.add(dir);
            }
        }
        if (!available.isEmpty()) {
            Collections.shuffle(available);
            snapAndTurn(available.get(0));
        }
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
            drawDeathEffect(g, new Color(56, 189, 248));
            return;
        }

        // Nhịp lơ lửng phập phồng (nhanh hơn khi đuổi)
        int animRate = chasing ? 4 : 6;
        int step = (animationTick / animRate) % 4;
        int pulseW = (step == 1) ? 2 : (step == 3 ? -1 : 0);
        int pulseH = (step == 1) ? -2 : (step == 3 ? 1 : 0);
        int spriteW = width + pulseW;
        int spriteH = height + pulseH;
        int drawX = x - (spriteW - width) / 2;
        int drawY = y - (spriteH - height) / 2;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hào quang xanh cảnh báo khi rượt đuổi
        if (chasing) {
            g2d.setColor(new Color(56, 189, 248, 80));
            g2d.fillOval(drawX - 4, drawY - 4, spriteW + 8, spriteH + 8);
        }

        // Bóng dưới chân
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(x + 2, y + height - 3, width - 4, 6);

        if (sprite != null) {
            if (direction == Player.Direction.LEFT) {
                g2d.drawImage(sprite, drawX + spriteW, drawY, -spriteW, spriteH, null);
            } else {
                g2d.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
            }
        } else {
            g2d.setColor(new Color(56, 189, 248));
            g2d.fillOval(drawX, drawY, spriteW, spriteH);
            g2d.setColor(new Color(2, 132, 199));
            g2d.drawOval(drawX, drawY, spriteW, spriteH);
        }

        g2d.dispose();
    }

    public boolean isChasing() {
        return chasing;
    }
}

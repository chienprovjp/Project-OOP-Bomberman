package bomberman.model;

import bomberman.component.Entity;
import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Lớp nhân vật người chơi (Player).
 */
public class Player extends Entity {
    private int speed = Math.max(3, GameMap.TILE_SIZE / 12);

    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Direction direction = Direction.DOWN;
    private Direction lastDirection = Direction.DOWN;

    // Sprite hình ảnh 4 hướng
    private BufferedImage spriteDown;
    private BufferedImage spriteUp;
    private BufferedImage spriteLeft;
    private BufferedImage spriteRight;

    public Player(int startX, int startY) {
        // Khung va chạm (hitbox) 44x44 gần kín ô gạch 48x48 (chỉ hở 2px mỗi bên), triệt tiêu lắc lư
        super(startX, startY, 44, 44);
        loadSprites();
    }

    private void loadSprites() {
        spriteDown = ResourceLoader.loadImage("player_down.png");
        spriteUp = ResourceLoader.loadImage("player_up.png");
        spriteLeft = ResourceLoader.loadImage("player_left.png");
        spriteRight = ResourceLoader.loadImage("player_right.png");

        // Dùng ảnh mặc định nếu thiếu sprite hướng
        BufferedImage defaultSprite = ResourceLoader.loadImage("player.png");
        if (defaultSprite == null) {
            defaultSprite = ResourceLoader.loadImage("player1.png");
        }

        if (spriteDown == null) spriteDown = defaultSprite;
        if (spriteUp == null) spriteUp = defaultSprite;
        if (spriteLeft == null) spriteLeft = defaultSprite;
        if (spriteRight == null) spriteRight = defaultSprite;
    }

    @Override
    public void update() {}

    public void move(GameMap map) {
        move(map, null);
    }

    // Di chuyển và căn tim đường tự động, hỗ trợ bo góc mượt mà
    public void move(GameMap map, List<Bomb> bombs) {
        Direction primaryDir = null;
        Direction secondaryDir = null;

        boolean verticalPressed = isMovingUp || isMovingDown;
        boolean horizontalPressed = isMovingLeft || isMovingRight;

        if (verticalPressed && horizontalPressed) {
            // Khi nhấn cả 2 trục, ưu tiên hướng vừa nhấn sau cùng
            if (lastDirection == Direction.UP || lastDirection == Direction.DOWN) {
                primaryDir = isMovingUp ? Direction.UP : Direction.DOWN;
                secondaryDir = isMovingLeft ? Direction.LEFT : Direction.RIGHT;
            } else {
                primaryDir = isMovingLeft ? Direction.LEFT : Direction.RIGHT;
                secondaryDir = isMovingUp ? Direction.UP : Direction.DOWN;
            }
        } else if (verticalPressed) {
            primaryDir = isMovingUp ? Direction.UP : Direction.DOWN;
        } else if (horizontalPressed) {
            primaryDir = isMovingLeft ? Direction.LEFT : Direction.RIGHT;
        }

        if (primaryDir == null) return;

        direction = primaryDir;

        // Thử đi theo hướng ưu tiên, nếu bị kẹt tường thì chuyển sang hướng phụ còn lại
        if (!tryMove(primaryDir, map, bombs) && secondaryDir != null) {
            if (tryMove(secondaryDir, map, bombs)) {
                direction = secondaryDir;
            }
        }
    }

    // Thực hiện di chuyển theo 1 hướng và tự căn tim đường / hút góc bo viền
    private boolean tryMove(Direction dir, GameMap map, List<Bomb> bombs) {
        int tileSize = GameMap.TILE_SIZE;

        if (dir == Direction.UP || dir == Direction.DOWN) {
            int dy = (dir == Direction.UP) ? -speed : speed;
            int col = (x + width / 2) / tileSize;
            int targetX = col * tileSize + (tileSize - width) / 2;

            // 1. Kiểm tra đi thẳng theo trục Y tại vị trí X hiện tại
            if (!map.checkCollision(x, y + dy, width, height)
                    && !checkBombCollision(x, y + dy, width, height, bombs)) {
                y += dy;
                // Căn chỉnh trục X bám sát tim cột
                int alignedX = alignToCenter(x, width, tileSize, speed);
                if (alignedX != x && !map.checkCollision(alignedX, y, width, height)
                        && !checkBombCollision(alignedX, y, width, height, bombs)) {
                    x = alignedX;
                }
                return true;
            }

            // 2. Nếu bị cản mép gờ, kiểm tra xem lối vào tại tâm cột targetX có thông không (Hút góc tự động)
            if (!map.checkCollision(targetX, y + dy, width, height)
                    && !checkBombCollision(targetX, y + dy, width, height, bombs)) {
                int step = Math.min(speed, Math.abs(targetX - x));
                int nudgeX = x + (targetX > x ? step : -step);
                if (!map.checkCollision(nudgeX, y, width, height)
                        && !checkBombCollision(nudgeX, y, width, height, bombs)) {
                    x = nudgeX;
                }
                if (!map.checkCollision(x, y + dy, width, height)
                        && !checkBombCollision(x, y + dy, width, height, bombs)) {
                    y += dy;
                }
                return true;
            }
        } else if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            int dx = (dir == Direction.LEFT) ? -speed : speed;
            int row = (y + height / 2) / tileSize;
            int targetY = row * tileSize + (tileSize - height) / 2;

            // 1. Kiểm tra đi thẳng theo trục X tại vị trí Y hiện tại
            if (!map.checkCollision(x + dx, y, width, height)
                    && !checkBombCollision(x + dx, y, width, height, bombs)) {
                x += dx;
                // Căn chỉnh trục Y bám sát tim hàng
                int alignedY = alignToCenter(y, height, tileSize, speed);
                if (alignedY != y && !map.checkCollision(x, alignedY, width, height)
                        && !checkBombCollision(x, alignedY, width, height, bombs)) {
                    y = alignedY;
                }
                return true;
            }

            // 2. Nếu bị cản mép gờ, kiểm tra xem lối vào tại tâm hàng targetY có thông không (Hút góc tự động)
            if (!map.checkCollision(x + dx, targetY, width, height)
                    && !checkBombCollision(x + dx, targetY, width, height, bombs)) {
                int step = Math.min(speed, Math.abs(targetY - y));
                int nudgeY = y + (targetY > y ? step : -step);
                if (!map.checkCollision(x, nudgeY, width, height)
                        && !checkBombCollision(x, nudgeY, width, height, bombs)) {
                    y = nudgeY;
                }
                if (!map.checkCollision(x + dx, y, width, height)
                        && !checkBombCollision(x + dx, y, width, height, bombs)) {
                    x += dx;
                }
                return true;
            }
        }
        return false;
    }

    // Kiểm tra va chạm với bom (chỉ cản khi người chơi đã bước hẳn ra ngoài ô bom)
    private boolean checkBombCollision(int targetX, int targetY, int targetW, int targetH, List<Bomb> bombs) {
        if (bombs == null || bombs.isEmpty()) return false;
        Rectangle targetBox = new Rectangle(targetX, targetY, targetW, targetH);
        for (Bomb b : bombs) {
            if (b.isExploded() || b.isPlayerAllowedInside()) {
                continue;
            }
            if (targetBox.intersects(b.getBounds())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage activeSprite = switch (direction) {
            case UP -> spriteUp;
            case DOWN -> spriteDown;
            case LEFT -> spriteLeft;
            case RIGHT -> spriteRight;
        };

        // Kích thước hiển thị 44x44, vừa khít hitbox 44x44
        int spriteSize = 44;
        int drawX = x - (spriteSize - width) / 2;
        int drawY = y - (spriteSize - height) / 2;

        // Bóng mờ
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(x + 2, y + height - 4, width - 4, 6);

        if (activeSprite != null) {
            g.drawImage(activeSprite, drawX, drawY, spriteSize, spriteSize, null);
        } else {
            drawFallback(g);
        }
    }

    private void drawFallback(Graphics g) {
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(x, y, width, height, 8, 8);
        g.setColor(new Color(0, 70, 160));
        g.drawRoundRect(x, y, width, height, 8, 8);

        g.setColor(Color.WHITE);
        g.fillOval(x + 4, y + 5, 5, 5);
        g.fillOval(x + 15, y + 5, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(x + 6, y + 6, 2, 2);
        g.fillOval(x + 17, y + 6, 2, 2);
    }

    public void handleKeyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
            isMovingUp = true;
            lastDirection = Direction.UP;
        }
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
            isMovingDown = true;
            lastDirection = Direction.DOWN;
        }
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
            isMovingLeft = true;
            lastDirection = Direction.LEFT;
        }
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
            isMovingRight = true;
            lastDirection = Direction.RIGHT;
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) isMovingUp = false;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) isMovingDown = false;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) isMovingLeft = false;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) isMovingRight = false;
    }

    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        stopMoving();
        this.direction = Direction.DOWN;
        this.lastDirection = Direction.DOWN;
    }

    public void stopMoving() {
        this.isMovingUp = false;
        this.isMovingDown = false;
        this.isMovingLeft = false;
        this.isMovingRight = false;
    }

    public BufferedImage getSpriteDown() { return spriteDown; }
    public Direction getDirection() { return direction; }

    public void increaseSpeed() {
        if (this.speed < 7) { // Giới hạn tốc độ tối đa để không xuyên tường
            this.speed += 1;
        }
    }

    public void resetSpeed() {
        this.speed = Math.max(3, GameMap.TILE_SIZE / 12);
    }

    public int getSpeed() {
        return speed;
    }
}

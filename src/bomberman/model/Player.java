package bomberman.model;

import bomberman.component.Entity;
import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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

    // Animation cúi nhún người đặt bom (Squash & Stretch)
    private int placeBombAnimTick = 0;
    private static final int PLACE_BOMB_ANIM_DURATION = 9; // ~0.15s (9 frame nhún cúi người đặt bom mượt mà)

    // Mảng lưu trữ các frame hoạt ảnh bước chân cho từng hướng (mỗi hướng 3 frame: chân trái, đứng yên, chân phải)
    private BufferedImage[] animDown;
    private BufferedImage[] animUp;
    private BufferedImage[] animLeft;
    private BufferedImage[] animRight;

    // Biến đếm nhịp bước chân khi di chuyển
    private int walkTick = 0;
    private boolean isMoving = false;

    // Sprite hình ảnh 4 hướng mặc định (dùng dự phòng)
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
        animDown = loadFrames("player_down", "player.png");
        animUp = loadFrames("player_up", "player.png");
        animLeft = loadFrames("player_left", "player.png");
        animRight = loadFrames("player_right", "player.png");

        // Giữ lại sprite mặc định (frame đứng yên số 2) phòng khi cần dùng trực tiếp
        spriteDown = animDown[1];
        spriteUp = animUp[1];
        spriteLeft = animLeft[1];
        spriteRight = animRight[1];
    }

    /**
     * Nạp bộ 3 frame bước chân cho một hướng (1: chân trái, 2: đứng yên, 3: chân phải).
     */
    private BufferedImage[] loadFrames(String prefix, String fallbackName) {
        BufferedImage[] frames = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            frames[i] = ResourceLoader.loadImage(prefix + "_" + (i + 1) + ".png");
        }
        BufferedImage base = ResourceLoader.loadImage(prefix + ".png");
        if (base == null) {
            base = ResourceLoader.loadImage(fallbackName);
        }
        for (int i = 0; i < 3; i++) {
            if (frames[i] == null) {
                frames[i] = base;
            }
        }
        return frames;
    }

    @Override
    public void update() {
        if (placeBombAnimTick > 0) {
            placeBombAnimTick--;
        }

        // Cập nhật nhịp bước chân: khi đang di chuyển thì tăng tick, đứng yên thì trả về 0
        if (isMoving) {
            walkTick++;
        } else {
            walkTick = 0;
        }
    }

    /**
     * Kích hoạt hiệu ứng hoạt hình cúi nhún người đặt bom (Squash & Stretch).
     */
    public void triggerPlaceBombAnimation() {
        this.placeBombAnimTick = PLACE_BOMB_ANIM_DURATION;
    }

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

        if (primaryDir == null) {
            this.isMoving = false;
            return;
        }

        direction = primaryDir;

        // Thử đi theo hướng ưu tiên, nếu bị kẹt tường thì chuyển sang hướng phụ còn lại
        boolean moved = tryMove(primaryDir, map, bombs);
        if (!moved && secondaryDir != null) {
            if (tryMove(secondaryDir, map, bombs)) {
                direction = secondaryDir;
                moved = true;
            }
        }
        this.isMoving = moved;
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
        BufferedImage[] activeFrames = switch (direction) {
            case UP -> animUp;
            case DOWN -> animDown;
            case LEFT -> animLeft;
            case RIGHT -> animRight;
        };

        // Chu kỳ bước chân: 0 (trái), 1 (đứng), 2 (phải), 3 (đứng)
        int currentFrame = 1;
        if (isMoving && activeFrames != null) {
            int step = (walkTick / 6) % 4;
            currentFrame = (step == 3) ? 1 : step;
        }

        BufferedImage activeSprite = (activeFrames != null && activeFrames[currentFrame] != null)
                ? activeFrames[currentFrame]
                : spriteDown;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hiệu ứng nhún người đặt bom
        int widthExpansion = 0;
        int heightCompression = 0;
        if (placeBombAnimTick > 0) {
            float progress = 1.0f - (placeBombAnimTick / (float) PLACE_BOMB_ANIM_DURATION);
            float squash = (float) Math.sin(progress * Math.PI);
            heightCompression = (int) (squash * 10.0f);
            widthExpansion = (int) (squash * 6.0f);
        }

        int drawW = width + widthExpansion;
        int drawH = height - heightCompression;

        int drawX = x - widthExpansion / 2;
        int drawY = (y + height) - drawH;

        // Bóng dưới chân
        int shadowPad = widthExpansion / 2;
        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillOval(x + 2 - shadowPad, y + height - 4, width - 4 + shadowPad * 2, 6);

        if (activeSprite != null) {
            g2d.drawImage(activeSprite, drawX, drawY, drawW, drawH, null);
        } else {
            drawFallback(g2d, drawX, drawY, drawW, drawH);
        }

        g2d.dispose();
    }

    private void drawFallback(Graphics g, int drawX, int drawY, int drawW, int drawH) {
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(drawX, drawY, drawW, drawH, 8, 8);
        g.setColor(new Color(0, 70, 160));
        g.drawRoundRect(drawX, drawY, drawW, drawH, 8, 8);

        g.setColor(Color.WHITE);
        g.fillOval(drawX + 4, drawY + 5, 5, 5);
        g.fillOval(drawX + 15, drawY + 5, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(drawX + 6, drawY + 6, 2, 2);
        g.fillOval(drawX + 17, drawY + 6, 2, 2);
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
        this.placeBombAnimTick = 0;
        this.walkTick = 0;
        this.isMoving = false;
    }

    public void stopMoving() {
        this.isMovingUp = false;
        this.isMovingDown = false;
        this.isMovingLeft = false;
        this.isMovingRight = false;
        this.isMoving = false;
        this.walkTick = 0;
    }

    public BufferedImage getSpriteDown() {
        return (animDown != null && animDown[1] != null) ? animDown[1] : spriteDown;
    }
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

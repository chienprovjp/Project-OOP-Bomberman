package bomberman.entities;

import bomberman.gui.GameMap;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Lớp đại diện cho Người chơi (Player) trong game Bomberman.
 * Kế thừa từ Entity để có vị trí (x, y) và khung va chạm (hitbox).
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

    // Sprite ảnh nhân vật cho 4 hướng
    private BufferedImage spriteDown;
    private BufferedImage spriteUp;
    private BufferedImage spriteLeft;
    private BufferedImage spriteRight;

    public Player(int startX, int startY) {
        super(startX, startY, (int) (GameMap.TILE_SIZE * 0.72), (int) (GameMap.TILE_SIZE * 0.72));
        loadSprites();
    }

    private void loadSprites() {
        spriteDown = loadSingleSprite("player_down.png");
        spriteUp = loadSingleSprite("player_up.png");
        spriteLeft = loadSingleSprite("player_left.png");
        spriteRight = loadSingleSprite("player_right.png");

        // Fallback nếu không có sprite hướng: dùng ảnh player.png hoặc player1.png
        BufferedImage defaultSprite = loadSingleSprite("player.png");
        if (defaultSprite == null) {
            defaultSprite = loadSingleSprite("player1.png");
        }

        if (spriteDown == null) spriteDown = defaultSprite;
        if (spriteUp == null) spriteUp = defaultSprite;
        if (spriteLeft == null) spriteLeft = defaultSprite;
        if (spriteRight == null) spriteRight = defaultSprite;
    }

    private BufferedImage loadSingleSprite(String fileName) {
        try {
            var url = getClass().getResource("/assets/images/" + fileName);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception ignored) {
        }

        try {
            File f = new File("assets/images/" + fileName);
            if (f.exists()) {
                return ImageIO.read(f);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public void update() {
        // Cập nhật logic Player nếu cần
    }

    public void move(GameMap map) {
        move(map, null);
    }

    /**
     * Di chuyển Player và xử lý va chạm với tường/gạch và các quả bom đang đặt.
     */
    public void move(GameMap map, List<Bomb> bombs) {
        int dx = 0;
        int dy = 0;

        if (isMovingUp)    dy -= speed;
        if (isMovingDown)  dy += speed;
        if (isMovingLeft)  dx -= speed;
        if (isMovingRight) dx += speed;

        // Cập nhật hướng nhìn theo phím đang bấm
        if (isMovingUp) direction = Direction.UP;
        else if (isMovingDown) direction = Direction.DOWN;
        else if (isMovingLeft) direction = Direction.LEFT;
        else if (isMovingRight) direction = Direction.RIGHT;

        // Tách riêng trục X và Y để nhân vật trượt mượt mà khi đi chéo hoặc sát tường
        if (dx != 0 && !map.checkCollision(x + dx, y, width, height)
                && !checkBombCollision(x + dx, y, width, height, bombs)) {
            x += dx;
        }
        if (dy != 0 && !map.checkCollision(x, y + dy, width, height)
                && !checkBombCollision(x, y + dy, width, height, bombs)) {
            y += dy;
        }
    }

    /**
     * Kiểm tra va chạm với bom:
     * Nếu Player vừa đặt bom tại ô đó và chưa bước ra ngoài thì vẫn được phép đi ra.
     * Khi đã ra ngoài rồi thì quả bom là vật cản cứng.
     */
    private boolean checkBombCollision(int targetX, int targetY, int targetW, int targetH, List<Bomb> bombs) {
        if (bombs == null || bombs.isEmpty()) {
            return false;
        }
        Rectangle targetBox = new Rectangle(targetX, targetY, targetW, targetH);
        for (Bomb b : bombs) {
            if (b.isExploded()) {
                continue;
            }
            if (b.isPlayerAllowedInside()) {
                // Player vừa đặt quả bom này nên vẫn được phép bước ra ngoài
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

        // Căn giữa ảnh vẽ so với hitbox va chạm
        int drawX = x - (GameMap.TILE_SIZE - width) / 2;
        int drawY = y - (GameMap.TILE_SIZE - height) / 2;

        // 1. Bóng mờ dưới chân
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(x + 2, y + height - 4, width - 4, 6);

        // 2. Vẽ sprite ảnh nếu đã nạp thành công
        if (activeSprite != null) {
            g.drawImage(activeSprite, drawX, drawY, GameMap.TILE_SIZE, GameMap.TILE_SIZE, null);
        } else {
            drawFallback(g);
        }
    }

    private void drawFallback(Graphics g) {
        // Thân nhân vật
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(x, y, width, height, 8, 8);
        g.setColor(new Color(0, 70, 160));
        g.drawRoundRect(x, y, width, height, 8, 8);

        // Mắt
        g.setColor(Color.WHITE);
        g.fillOval(x + 4, y + 5, 5, 5);
        g.fillOval(x + 15, y + 5, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(x + 6, y + 6, 2, 2);
        g.fillOval(x + 17, y + 6, 2, 2);
    }

    public void handleKeyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP)
            isMovingUp = true;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)
            isMovingDown = true;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)
            isMovingLeft = true;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT)
            isMovingRight = true;
    }

    public void handleKeyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP)
            isMovingUp = false;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)
            isMovingDown = false;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)
            isMovingLeft = false;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT)
            isMovingRight = false;
    }

    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.isMovingUp = false;
        this.isMovingDown = false;
        this.isMovingLeft = false;
        this.isMovingRight = false;
        this.direction = Direction.DOWN;
    }

    public void stopMoving() {
        this.isMovingUp = false;
        this.isMovingDown = false;
        this.isMovingLeft = false;
        this.isMovingRight = false;
    }

    public BufferedImage getSpriteDown() {
        return spriteDown;
    }

    public Direction getDirection() {
        return direction;
    }
}

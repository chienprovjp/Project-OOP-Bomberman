package bomberman.entities;

import bomberman.gui.GameMap;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class Player {
    private int x;
    private int y;
    private int width = 24;
    private int height = 24;
    private int speed = 3;

    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    private Direction direction = Direction.DOWN;

    // Sprite ảnh nhân vật
    private BufferedImage spriteDown;
    private BufferedImage spriteUp;
    private BufferedImage spriteLeft;
    private BufferedImage spriteRight;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        loadSprites();
    }

    private void loadSprites() {
        spriteDown  = loadSingleSprite("player_down.png");
        spriteUp    = loadSingleSprite("player_up.png");
        spriteLeft  = loadSingleSprite("player_left.png");
        spriteRight = loadSingleSprite("player_right.png");

        // Fallback nếu không có sprite hướng: dùng ảnh player.png hoặc player1.png
        BufferedImage defaultSprite = loadSingleSprite("player.png");
        if (defaultSprite == null) {
            defaultSprite = loadSingleSprite("player1.png");
        }

        if (spriteDown == null)  spriteDown  = defaultSprite;
        if (spriteUp == null)    spriteUp    = defaultSprite;
        if (spriteLeft == null)  spriteLeft  = defaultSprite;
        if (spriteRight == null) spriteRight = defaultSprite;
    }

    private BufferedImage loadSingleSprite(String fileName) {
        try {
            var url = getClass().getResource("/assets/images/" + fileName);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception ignored) {}

        try {
            File f = new File("assets/images/" + fileName);
            if (f.exists()) {
                return ImageIO.read(f);
            }
        } catch (Exception ignored) {}

        return null;
    }

    public void move(GameMap map) {
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
        if (dx != 0 && !map.checkCollision(x + dx, y, width, height)) {
            x += dx;
        }
        if (dy != 0 && !map.checkCollision(x, y + dy, width, height)) {
            y += dy;
        }
    }

    public void draw(Graphics g) {
        BufferedImage activeSprite = switch (direction) {
            case UP    -> spriteUp;
            case DOWN  -> spriteDown;
            case LEFT  -> spriteLeft;
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
            // Fallback vẽ hình khối 2D của Khang nếu chưa có ảnh
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
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP)    isMovingUp = true;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)  isMovingDown = true;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)  isMovingLeft = true;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) isMovingRight = true;
    }

    public void handleKeyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP)    isMovingUp = false;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)  isMovingDown = false;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)  isMovingLeft = false;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) isMovingRight = false;
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

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Direction getDirection() { return direction; }
}

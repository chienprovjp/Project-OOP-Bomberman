package src.Entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import src.Map.GameMap;
import src.Map.MapData;

import java.io.File;

public class Player {
    private final int id;
    private final String name;

    // Vị trí spawn ban đầu để reset khi cần
    private final double startX;
    private final double startY;

    // Tọa độ hiện tại (pixel)
    private double x;
    private double y;

    // Kích thước hitbox va chạm
    private final double width;
    private final double height;
    private double speed = 3.2;

    // Trạng thái nhấn phím di chuyển
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // Hướng nhìn của nhân vật
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    private Direction direction = Direction.DOWN;

    // Phím điều khiển
    private final KeyCode upKey;
    private final KeyCode downKey;
    private final KeyCode leftKey;
    private final KeyCode rightKey;
    private final KeyCode bombKey;

    // Màu sắc nhận diện và sprite
    private final Color themeColor;
    private Image spriteDown;
    private Image spriteUp;
    private Image spriteLeft;
    private Image spriteRight;

    public Player(int id, String name, double startX, double startY,
                  KeyCode upKey, KeyCode downKey, KeyCode leftKey, KeyCode rightKey, KeyCode bombKey,
                  Color themeColor) {
        this.id = id;
        this.name = name;
        this.startX = startX;
        this.startY = startY;
        this.x = startX;
        this.y = startY;

        this.upKey = upKey;
        this.downKey = downKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.bombKey = bombKey;
        this.themeColor = themeColor;

        // Hitbox chiếm ~72% kích thước ô gạch để đi qua các khe hẹp mượt mà
        this.width = Math.round(GameMap.TILE_SIZE * 0.72);
        this.height = Math.round(GameMap.TILE_SIZE * 0.72);

        loadSprites();
    }

    private void loadSprites() {
        spriteDown = loadSingleSprite("player" + id + "_down.png");
        spriteUp = loadSingleSprite("player" + id + "_up.png");
        spriteLeft = loadSingleSprite("player" + id + "_left.png");
        spriteRight = loadSingleSprite("player" + id + "_right.png");

        // Fallback nếu không có sprite hướng: dùng sprite chung player{id}.png
        Image defaultSprite = loadSingleSprite("player" + id + ".png");
        if (spriteDown == null) spriteDown = defaultSprite;
        if (spriteUp == null) spriteUp = defaultSprite;
        if (spriteLeft == null) spriteLeft = defaultSprite;
        if (spriteRight == null) spriteRight = defaultSprite;
    }

    private Image loadSingleSprite(String fileName) {
        try {
            var url = getClass().getResource("/assets/images/" + fileName);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        } catch (Exception ignored) {}

        File f = new File("assets/images/" + fileName);
        if (f.exists()) {
            return new Image(f.toURI().toString());
        }
        return null;
    }

    /**
     * Cập nhật vị trí nhân vật (áp dụng thuật toán trượt tường từ nhánh dev).
     */
    public void move() {
        double dx = 0;
        double dy = 0;

        if (movingUp)    dy -= speed;
        if (movingDown)  dy += speed;
        if (movingLeft)  dx -= speed;
        if (movingRight) dx += speed;

        // Cập nhật hướng quay mặt
        if (movingUp) direction = Direction.UP;
        else if (movingDown) direction = Direction.DOWN;
        else if (movingLeft) direction = Direction.LEFT;
        else if (movingRight) direction = Direction.RIGHT;

        // Tách riêng trục X và Y để nhân vật trượt mượt mà khi đi chéo hoặc sát góc tường
        if (dx != 0 && !checkCollision(x + dx, y)) {
            x += dx;
        }
        if (dy != 0 && !checkCollision(x, y + dy)) {
            y += dy;
        }
    }

    /**
     * Kiểm tra va chạm với tường (WALL) hoặc gạch (BRICK) trên bản đồ.
     */
    public boolean checkCollision(double targetX, double targetY) {
        int leftCol   = (int) (targetX / GameMap.TILE_SIZE);
        int rightCol  = (int) ((targetX + width - 1) / GameMap.TILE_SIZE);
        int topRow    = (int) (targetY / GameMap.TILE_SIZE);
        int bottomRow = (int) ((targetY + height - 1) / GameMap.TILE_SIZE);

        // Vượt ra ngoài biên bản đồ
        if (leftCol < 0 || rightCol >= MapData.COLS || topRow < 0 || bottomRow >= MapData.ROWS) {
            return true;
        }

        // Kiểm tra tất cả các ô mà hitbox đang chiếm
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                int tile = MapData.getTile(row, col);
                if (tile == MapData.WALL || tile == MapData.BRICK) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Vẽ nhân vật lên Canvas.
     */
    public void draw(GraphicsContext gc) {
        // Căn chỉnh ảnh vẽ ở giữa ô / bao quanh hitbox
        double drawX = x - (GameMap.TILE_SIZE - width) / 2.0;
        double drawY = y - (GameMap.TILE_SIZE - height) / 2.0;

        // 1. Bóng dưới chân nhân vật
        gc.setFill(Color.rgb(0, 0, 0, 0.28));
        gc.fillOval(x + 2, y + height - 5, width - 4, 8);

        // 2. Vẽ Sprite nhân vật theo hướng
        Image currentSprite = getCurrentSprite();
        if (currentSprite != null) {
            gc.drawImage(currentSprite, drawX, drawY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
        } else {
            drawFallback(gc, drawX, drawY);
        }

        // 3. Huy hiệu P1 / P2 / P3 trên đầu nhân vật
        drawBadge(gc, drawX, drawY);
    }

    private Image getCurrentSprite() {
        return switch (direction) {
            case UP -> spriteUp;
            case DOWN -> spriteDown;
            case LEFT -> spriteLeft;
            case RIGHT -> spriteRight;
        };
    }

    private void drawBadge(GraphicsContext gc, double drawX, double drawY) {
        double badgeW = 22;
        double badgeH = 12;
        double badgeX = drawX + (GameMap.TILE_SIZE - badgeW) / 2.0;
        double badgeY = drawY - 9;

        // Nền huy hiệu có viền
        gc.setFill(themeColor);
        gc.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);

        // Chữ P1, P2, P3
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        gc.fillText("P" + id, badgeX + 4, badgeY + 9);
    }

    private void drawFallback(GraphicsContext gc, double drawX, double drawY) {
        // Thân
        gc.setFill(themeColor);
        gc.fillRoundRect(x, y + height * 0.4, width, height * 0.6, 6, 6);

        // Đầu
        gc.setFill(Color.WHITE);
        gc.fillOval(x + 2, y, width - 4, height * 0.55);
        gc.setStroke(Color.rgb(30, 30, 40));
        gc.setLineWidth(1.5);
        gc.strokeOval(x + 2, y, width - 4, height * 0.55);

        // Mặt / Visor
        gc.setFill(Color.rgb(255, 220, 185));
        gc.fillOval(x + 5, y + 4, width - 10, height * 0.35);

        // Mắt
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillOval(x + 8, y + 7, 3, 5);
        gc.fillOval(x + width - 11, y + 7, 3, 5);
    }

    public void handleKeyPressed(KeyCode code) {
        if (code == upKey)    movingUp = true;
        if (code == downKey)  movingDown = true;
        if (code == leftKey)  movingLeft = true;
        if (code == rightKey) movingRight = true;
    }

    public void handleKeyReleased(KeyCode code) {
        if (code == upKey)    movingUp = false;
        if (code == downKey)  movingDown = false;
        if (code == leftKey)  movingLeft = false;
        if (code == rightKey) movingRight = false;
    }

    public void reset() {
        this.x = startX;
        this.y = startY;
        this.movingUp = false;
        this.movingDown = false;
        this.movingLeft = false;
        this.movingRight = false;
        this.direction = Direction.DOWN;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public Color getThemeColor() { return themeColor; }
    public Direction getDirection() { return direction; }
    public KeyCode getBombKey() { return bombKey; }
    public boolean isMoving() { return movingUp || movingDown || movingLeft || movingRight; }
}

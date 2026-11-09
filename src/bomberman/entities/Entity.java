package bomberman.entities;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Lớp cơ sở (Base Class) cho mọi thực thể: quản lý vị trí, kích thước và va chạm.
 */
public abstract class Entity {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public Entity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update();
    public abstract void draw(Graphics g);

    // Lấy khung chữ nhật va chạm (Hitbox)
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Kiểm tra va chạm với thực thể khác
    public boolean intersects(Entity other) {
        if (other == null) return false;
        return this.getBounds().intersects(other.getBounds());
    }

    /**
     * Căn chỉnh tọa độ vào tâm ô gạch (tim đường) theo trục vuông góc.
     * Giúp thực thể trượt mượt qua góc cua và loại bỏ hiện tượng lắc lư tự do trong ô.
     */
    protected int alignToCenter(int currentPos, int currentSize, int tileSize, int stepSpeed) {
        int tile = (currentPos + currentSize / 2) / tileSize;
        int targetCenter = tile * tileSize + (tileSize - currentSize) / 2;
        int diff = targetCenter - currentPos;

        if (Math.abs(diff) <= stepSpeed) {
            return targetCenter;
        } else if (diff > 0) {
            return currentPos + stepSpeed;
        } else {
            return currentPos - stepSpeed;
        }
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}

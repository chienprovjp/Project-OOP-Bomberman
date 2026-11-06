package bomberman.component;

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

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}

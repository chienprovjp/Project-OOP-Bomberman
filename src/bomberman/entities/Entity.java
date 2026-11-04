package bomberman.entities;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Lớp trừu tượng cơ sở cho mọi thực thể trong game (Player, Enemy, Bomb, Explosion).
 * Cung cấp các thuộc tính vị trí, kích thước và kiểm tra va chạm cơ bản.
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

    /**
     * Cập nhật trạng thái logic của thực thể ở mỗi khung hình.
     */
    public abstract void update();

    /**
     * Vẽ hình ảnh của thực thể lên màn hình.
     */
    public abstract void draw(Graphics g);

    /**
     * Lấy hình chữ nhật bao quanh (hitbox) để phục vụ kiểm tra va chạm.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Kiểm tra xem thực thể này có va chạm (giao nhau) với thực thể khác hay không.
     */
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

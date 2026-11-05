package bomberman.entities;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Lớp trừu tượng cơ sở (Base Class) cho mọi thực thể trong game Bomberman
 * (Người chơi Player, Kẻ địch Enemy, Quả bom Bomb, Vụ nổ Explosion).
 *
 * Áp dụng tính Kế thừa (Inheritance) trong OOP:
 * Gom các thuộc tính chung (tọa độ x, y, chiều rộng, chiều cao) và
 * cơ chế kiểm tra va chạm (hitbox Rectangle) về một nơi duy nhất.
 */
public abstract class Entity {
    // Tọa độ vị trí tính theo pixel
    protected int x;
    protected int y;

    // Kích thước của thực thể tính theo pixel
    protected int width;
    protected int height;

    public Entity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Cập nhật trạng thái logic ở mỗi khung hình.
     * Mỗi lớp con (Player, Enemy, Bomb...) sẽ tự định nghĩa hành vi riêng (Tính Đa hình).
     */
    public abstract void update();

    /**
     * Vẽ hình ảnh của thực thể lên màn hình.
     */
    public abstract void draw(Graphics g);

    /**
     * Lấy khung chữ nhật va chạm (Hitbox).
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Kiểm tra va chạm: trả về true nếu thực thể này chạm vào thực thể khác.
     */
    public boolean intersects(Entity other) {
        if (other == null) return false;
        return this.getBounds().intersects(other.getBounds());
    }

    // Các hàm Getter & Setter cơ bản
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}

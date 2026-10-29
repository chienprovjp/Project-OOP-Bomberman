package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

/**
 * Lớp Player: Đại diện cho nhân vật người chơi (Bomber).
 * Nhiệm vụ của lớp này:
 * 1. Nhận tín hiệu phím bấm từ người chơi (W, A, S, D hoặc mũi tên).
 * 2. Tính toán vị trí mới và kiểm tra va chạm với GameMap.
 * 3. Tự vẽ hình ảnh của nhân vật lên màn hình.
 */
public class Player {
    // Tọa độ hiện tại của nhân vật trên màn hình (đơn vị: pixel)
    private int x;
    private int y;

    // Kích thước của nhân vật (26x26 pixel, nhỏ hơn ô map 32x32 một chút để dễ đi qua khe hẹp)
    private int width = 26;
    private int height = 26;

    // Tốc độ di chuyển: Mỗi khung hình nhân vật bước được bao nhiêu pixel
    private int speed = 3;

    // Biến lưu trạng thái các phím đang được nhấn (true) hay thả ra (false)
    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    /**
     * Khởi tạo nhân vật tại vị trí ban đầu (startX, startY).
     */
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    /**
     * Cập nhật vị trí nhân vật (được gọi liên tục mỗi khung hình).
     * @param map Bản đồ game để kiểm tra va chạm
     */
    public void move(GameMap map) {
        int dx = 0; // Độ dịch chuyển theo phương ngang
        int dy = 0; // Độ dịch chuyển theo phương dọc

        // 1. Tính toán hướng người chơi muốn đi
        if (isMovingUp)    dy -= speed;
        if (isMovingDown)  dy += speed;
        if (isMovingLeft)  dx -= speed;
        if (isMovingRight) dx += speed;

        // 2. Xử lý va chạm thông minh (tách riêng trục X và trục Y):
        // - Thử đi ngang (trục X) trước: Nếu vị trí mới không va chạm thì cập nhật tọa độ X
        if (dx != 0 && !map.checkCollision(x + dx, y, width, height)) {
            x += dx;
        }

        // - Thử đi dọc (trục Y) tiếp theo: Nếu vị trí mới không va chạm thì cập nhật tọa độ Y
        if (dy != 0 && !map.checkCollision(x, y + dy, width, height)) {
            y += dy;
        }
        // Nhờ tách riêng 2 trục, nhân vật khi đi chéo vào góc tường vẫn có thể trượt mượt mà
    }

    /**
     * Vẽ nhân vật lên màn hình (sử dụng các hình khối đơn giản).
     */
    public void draw(Graphics g) {
        // 1. Vẽ thân nhân vật hình hộp bo góc (màu xanh dương)
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(x, y, width, height, 8, 8);

        // 2. Vẽ viền ngoài cho thân nổi bật
        g.setColor(new Color(0, 70, 160));
        g.drawRoundRect(x, y, width, height, 8, 8);

        // 3. Vẽ mắt đơn giản để nhận diện nhân vật
        g.setColor(Color.WHITE);
        g.fillOval(x + 5, y + 6, 5, 5);
        g.fillOval(x + 16, y + 6, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(x + 7, y + 7, 2, 2);
        g.fillOval(x + 18, y + 7, 2, 2);
    }

    /**
     * Xử lý sự kiện khi một phím được NHẤN XUỐNG.
     */
    public void handleKeyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP)    isMovingUp = true;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)  isMovingDown = true;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)  isMovingLeft = true;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) isMovingRight = true;
    }

    /**
     * Xử lý sự kiện khi một phím được THẢ RA (ngừng di chuyển hướng đó).
     */
    public void handleKeyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP)    isMovingUp = false;
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN)  isMovingDown = false;
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT)  isMovingLeft = false;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) isMovingRight = false;
    }

    // Các hàm lấy thông tin vị trí (Getter)
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}


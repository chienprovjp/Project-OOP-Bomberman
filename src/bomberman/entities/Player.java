package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

/**
 * Nhân vật Player sơ cấp (Người thứ 3 xây dựng).
 * Đảm nhiệm: nhận phím điều khiển, tính toán di chuyển và va chạm với GameMap.
 */
public class Player {
    private int x;
    private int y;
    private int width = 26;   // Nhỏ hơn TILE_SIZE (32) một chút để di chuyển mượt mà qua các khe hẹp
    private int height = 26;
    private int speed = 3;

    // Trạng thái nhấn phím
    private boolean up, down, left, right;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    /**
     * Cập nhật vị trí nhân vật dựa trên phím bấm và kiểm tra va chạm với map.
     */
    public void update(GameMap map) {
        int dx = 0;
        int dy = 0;

        if (up) dy -= speed;
        if (down) dy += speed;
        if (left) dx -= speed;
        if (right) dx += speed;

        // Xử lý va chạm tách riêng trục X và Y để khi đi sát tường hoặc đi chéo không bị kẹt
        if (dx != 0 && !map.checkCollision(x + dx, y, width, height)) {
            x += dx;
        }
        if (dy != 0 && !map.checkCollision(x, y + dy, width, height)) {
            y += dy;
        }
    }

    /**
     * Vẽ nhân vật sơ bộ (hình tròn/bo góc có mắt để biết hướng).
     */
    public void render(Graphics g) {
        // Thân nhân vật
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(x, y, width, height, 8, 8);

        // Viền thân
        g.setColor(new Color(0, 70, 160));
        g.drawRoundRect(x, y, width, height, 8, 8);

        // Mắt đơn giản
        g.setColor(Color.WHITE);
        g.fillOval(x + 5, y + 6, 5, 5);
        g.fillOval(x + 16, y + 6, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(x + 7, y + 7, 2, 2);
        g.fillOval(x + 18, y + 7, 2, 2);
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) up = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) down = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) left = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) right = true;
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) up = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) down = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) left = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) right = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

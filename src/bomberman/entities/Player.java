package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class Player {
    private int x;
    private int y;
    private int width = 26;
    private int height = 26;
    private int speed = 3;

    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void move(GameMap map) {
        int dx = 0;
        int dy = 0;

        if (isMovingUp)    dy -= speed;
        if (isMovingDown)  dy += speed;
        if (isMovingLeft)  dx -= speed;
        if (isMovingRight) dx += speed;

        // Tách riêng trục X và Y để nhân vật trượt mượt mà khi đi chéo hoặc sát tường
        if (dx != 0 && !map.checkCollision(x + dx, y, width, height)) {
            x += dx;
        }
        if (dy != 0 && !map.checkCollision(x, y + dy, width, height)) {
            y += dy;
        }
    }

    public void draw(Graphics g) {
        // Thân nhân vật
        g.setColor(new Color(30, 144, 255));
        g.fillRoundRect(x, y, width, height, 8, 8);
        g.setColor(new Color(0, 70, 160));
        g.drawRoundRect(x, y, width, height, 8, 8);

        // Mắt
        g.setColor(Color.WHITE);
        g.fillOval(x + 5, y + 6, 5, 5);
        g.fillOval(x + 16, y + 6, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(x + 7, y + 7, 2, 2);
        g.fillOval(x + 18, y + 7, 2, 2);
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

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}


package bomberman.gui;

import java.awt.Color;
import java.awt.Graphics;

public class GameMap {
    public static final int TILE_SIZE = 32;
    public static final int COLS = 21;
    public static final int ROWS = 13;

    // 0: Cỏ (đi được), 1: Tường (chặn)
    private int[][] tiles;

    public GameMap() {
        initDefaultMap();
    }

    private void initDefaultMap() {
        tiles = new int[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                // Tường bao quanh viền ngoài
                if (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) {
                    tiles[row][col] = 1;
                }
                // Các cột trụ cố định
                else if (row % 2 == 0 && col % 2 == 0) {
                    tiles[row][col] = 1;
                }
                // Ô cỏ trống
                else {
                    tiles[row][col] = 0;
                }
            }
        }
    }

    /**
     * Kiểm tra vị trí (x, y) có va chạm với tường hoặc mép bản đồ hay không.
     */
    public boolean checkCollision(int x, int y, int width, int height) {
        int leftCol = x / TILE_SIZE;
        int rightCol = (x + width - 1) / TILE_SIZE;
        int topRow = y / TILE_SIZE;
        int bottomRow = (y + height - 1) / TILE_SIZE;

        // Vượt ra ngoài biên bản đồ
        if (leftCol < 0 || rightCol >= COLS || topRow < 0 || bottomRow >= ROWS) {
            return true;
        }

        // Kiểm tra các ô nhân vật đang chạm vào
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (tiles[row][col] == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public void draw(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int pixelX = col * TILE_SIZE;
                int pixelY = row * TILE_SIZE;

                if (tiles[row][col] == 1) {
                    // Tường màu xám
                    g.setColor(new Color(110, 110, 110));
                    g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(60, 60, 60));
                    g.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                } else {
                    // Cỏ màu xanh lá
                    g.setColor(new Color(160, 230, 160));
                    g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(145, 215, 145));
                    g.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public int getWidthInPixels() {
        return COLS * TILE_SIZE;
    }

    public int getHeightInPixels() {
        return ROWS * TILE_SIZE;
    }
}


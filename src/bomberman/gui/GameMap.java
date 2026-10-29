package bomberman.gui;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Bản đồ game sơ bộ.
 * Người thứ 2 có thể mở rộng class này (nạp map từ file txt, thêm Brick, Item,...).
 */
public class GameMap {
    public static final int TILE_SIZE = 32;
    public static final int COLS = 21;
    public static final int ROWS = 13;

    // 0: Đường đi (trống), 1: Tường cứng (chặn)
    // Người thứ 2 có thể thay thế ma trận này bằng MapLoader đọc từ file
    private int[][] tiles;

    public GameMap() {
        initDefaultMap();
    }

    private void initDefaultMap() {
        tiles = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // Tường bao quanh viền
                if (r == 0 || r == ROWS - 1 || c == 0 || c == COLS - 1) {
                    tiles[r][c] = 1;
                } else if (r % 2 == 0 && c % 2 == 0) {
                    // Cột trụ cứng đặc trưng của Bomberman
                    tiles[r][c] = 1;
                } else {
                    tiles[r][c] = 0;
                }
            }
        }
    }

    /**
     * Kiểm tra vị trí (x, y) với kích thước (width, height) có bị va chạm với địa hình không.
     * Người thứ 2 khi thêm Brick/Wall chỉ cần giữ hàm này để Player kiểm tra.
     */
    public boolean checkCollision(int x, int y, int width, int height) {
        int leftTile = x / TILE_SIZE;
        int rightTile = (x + width - 1) / TILE_SIZE;
        int topTile = y / TILE_SIZE;
        int bottomTile = (y + height - 1) / TILE_SIZE;

        // Vượt ra ngoài phạm vi map
        if (leftTile < 0 || rightTile >= COLS || topTile < 0 || bottomTile >= ROWS) {
            return true;
        }

        // Kiểm tra các ô mà thực thể chạm vào
        for (int r = topTile; r <= bottomTile; r++) {
            for (int c = leftTile; c <= rightTile; c++) {
                if (tiles[r][c] == 1) { // 1 là tường cản
                    return true;
                }
            }
        }
        return false;
    }

    public void render(Graphics g) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int px = c * TILE_SIZE;
                int py = r * TILE_SIZE;

                if (tiles[r][c] == 1) {
                    // Vẽ tường (màu xám)
                    g.setColor(new Color(110, 110, 110));
                    g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(70, 70, 70));
                    g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                } else {
                    // Vẽ nền cỏ (màu xanh lá)
                    g.setColor(new Color(160, 230, 160));
                    g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(145, 215, 145));
                    g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public int getWidth() {
        return COLS * TILE_SIZE;
    }

    public int getHeight() {
        return ROWS * TILE_SIZE;
    }
}

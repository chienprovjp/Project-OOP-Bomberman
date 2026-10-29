package bomberman.gui;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

public class GameMap {
    // Kích thước ô tile chuẩn: 48px giúp game hiển thị to rõ, đẹp mắt trên mọi màn hình
    public static final int TILE_SIZE = 48;
    public static final int COLS = 21;
    public static final int ROWS = 13;

    // Định nghĩa các loại ô trên bản đồ
    public static final int GRASS = 0; // Cỏ - có thể đi qua
    public static final int WALL  = 1; // Tường cố định - không đi qua được, không phá được
    public static final int BRICK = 2; // Tường gạch - không đi qua được, có thể phá hủy

    private int[][] tiles;

    // Sprite hình ảnh môi trường
    private BufferedImage grassImage;
    private BufferedImage wallImage;
    private BufferedImage brickImage;

    public GameMap() {
        loadImages();
        initDefaultMap();
    }

    private void loadImages() {
        grassImage = loadImage("/assets/images/grass.png");
        wallImage  = loadImage("/assets/images/wall.png");
        brickImage = loadImage("/assets/images/brick.png");
    }

    private BufferedImage loadImage(String path) {
        try {
            var url = getClass().getResource(path);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception ignored) {}

        try {
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;
            File file = new File(cleanPath);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            File fallback = new File("assets/images/" + new File(path).getName());
            if (fallback.exists()) {
                return ImageIO.read(fallback);
            }
        } catch (Exception ignored) {}

        return null;
    }

    private void initDefaultMap() {
        tiles = new int[ROWS][COLS];

        // Khởi tạo layout bản đồ chuẩn Bomberman
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                // Tường bao quanh viền ngoài
                if (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) {
                    tiles[row][col] = WALL;
                }
                // Các cột trụ cố định
                else if (row % 2 == 0 && col % 2 == 0) {
                    tiles[row][col] = WALL;
                }
                // Các ô còn lại
                else {
                    tiles[row][col] = GRASS;
                }
            }
        }

        // Đặt một số tường gạch mẫu (BRICK) trên bản đồ
        int[][] sampleBricks = {
            {1, 3}, {1, 4}, {1, 7}, {1, 8}, {1, 11}, {1, 14}, {1, 17},
            {2, 3}, {2, 5}, {2, 7}, {2, 9}, {2, 13}, {2, 17},
            {3, 1}, {3, 2}, {3, 4}, {3, 6}, {3, 8}, {3, 12}, {3, 15}, {3, 18},
            {4, 3}, {4, 7}, {4, 11}, {4, 15}, {4, 17},
            {5, 1}, {5, 4}, {5, 6}, {5, 8}, {5, 10}, {5, 13}, {5, 16}, {5, 19},
            {6, 3}, {6, 7}, {6, 9}, {6, 11}, {6, 15},
            {7, 2}, {7, 4}, {7, 6}, {7, 8}, {7, 12}, {7, 14}, {7, 18},
            {8, 5}, {8, 7}, {8, 11}, {8, 13}, {8, 17},
            {9, 1}, {9, 3}, {9, 6}, {9, 8}, {9, 12}, {9, 15}, {9, 19},
            {10, 3}, {10, 7}, {10, 9}, {10, 13}, {10, 17},
            {11, 4}, {11, 7}, {11, 10}, {11, 13}, {11, 16}
        };

        for (int[] pos : sampleBricks) {
            int r = pos[0];
            int c = pos[1];
            if (r > 0 && r < ROWS - 1 && c > 0 && c < COLS - 1) {
                // Không đặt gạch lên cột trụ tường cố định
                if (tiles[r][c] != WALL) {
                    tiles[r][c] = BRICK;
                }
            }
        }

        // Khu vực xuất phát của Player (góc trên bên trái) luôn là cỏ trống
        tiles[1][1] = GRASS;
        tiles[1][2] = GRASS;
        tiles[2][1] = GRASS;
    }

    /**
     * Kiểm tra vị trí (x, y) có va chạm với tường tĩnh, gạch hoặc mép bản đồ hay không.
     */
    public boolean checkCollision(int x, int y, int width, int height) {
        int leftCol   = x / TILE_SIZE;
        int rightCol  = (x + width - 1) / TILE_SIZE;
        int topRow    = y / TILE_SIZE;
        int bottomRow = (y + height - 1) / TILE_SIZE;

        // Vượt ra ngoài biên bản đồ
        if (leftCol < 0 || rightCol >= COLS || topRow < 0 || bottomRow >= ROWS) {
            return true;
        }

        // Kiểm tra các ô nhân vật đang chiếm chỗ
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (tiles[row][col] == WALL || tiles[row][col] == BRICK) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Phá hủy tường gạch tại vị trí hàng và cột.
     */
    public boolean destroyBrick(int row, int col) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS && tiles[row][col] == BRICK) {
            tiles[row][col] = GRASS;
            return true;
        }
        return false;
    }

    public int getTile(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return WALL;
        return tiles[row][col];
    }

    public void draw(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int pixelX = col * TILE_SIZE;
                int pixelY = row * TILE_SIZE;

                // 1. Luôn vẽ nền cỏ trước
                if (grassImage != null) {
                    g.drawImage(grassImage, pixelX, pixelY, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(new Color(160, 230, 160));
                    g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                }

                // 2. Vẽ tường cố định (WALL)
                if (tiles[row][col] == WALL) {
                    if (wallImage != null) {
                        g.drawImage(wallImage, pixelX, pixelY, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g.setColor(new Color(110, 110, 110));
                        g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                        g.setColor(new Color(60, 60, 60));
                        g.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                    }
                }
                // 3. Vẽ tường gạch phá được (BRICK)
                else if (tiles[row][col] == BRICK) {
                    if (brickImage != null) {
                        g.drawImage(brickImage, pixelX, pixelY, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g.setColor(new Color(210, 105, 30));
                        g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                        g.setColor(new Color(139, 69, 19));
                        g.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                    }
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

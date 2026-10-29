package bomberman.gui;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Lớp GameMap: Đại diện cho bản đồ của game.
 * Sử dụng một ma trận 2 chiều đơn giản:
 * - Số 0: Đại diện cho cỏ (đường đi thông thoáng, nhân vật đi qua được).
 * - Số 1: Đại diện cho tường cứng (chặn lại, nhân vật không đi qua được).
 *
 * [Dành cho Người thứ 2]: Sau này bạn có thể thay thế ma trận khởi tạo mặc định này
 * bằng cách đọc file map (.txt) và thêm các số khác (ví dụ 2 là gạch, 3 là vật phẩm).
 */
public class GameMap {
    // Kích thước của mỗi ô vuông trên map (tính theo pixel)
    public static final int TILE_SIZE = 32;

    // Số cột (chiều ngang) và số hàng (chiều dọc) của bản đồ
    public static final int COLS = 21;
    public static final int ROWS = 13;

    // Ma trận 2D chứa dữ liệu bản đồ
    private int[][] tiles;

    public GameMap() {
        initDefaultMap();
    }

    /**
     * Khởi tạo bản đồ mẫu ban đầu để Người 3 có thể test di chuyển.
     * Tạo một đường viền bao quanh bản đồ và các cột trụ đặc trưng của Bomberman.
     */
    private void initDefaultMap() {
        tiles = new int[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                // Nếu là ô ở 4 cạnh viền ngoài cùng -> Đặt làm tường cứng (1)
                if (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) {
                    tiles[row][col] = 1;
                }
                // Nếu cả hàng chẵn và cột chẵn -> Đặt làm cột trụ (1)
                else if (row % 2 == 0 && col % 2 == 0) {
                    tiles[row][col] = 1;
                }
                // Còn lại là bãi cỏ trống để nhân vật đi lại (0)
                else {
                    tiles[row][col] = 0;
                }
            }
        }
    }

    /**
     * Kiểm tra xem một vùng chữ nhật (nhân vật) có bị chạm vào tường hay không.
     *
     * @param x      Tọa độ X tính bằng pixel
     * @param y      Tọa độ Y tính bằng pixel
     * @param width  Chiều rộng của nhân vật
     * @param height Chiều cao của nhân vật
     * @return true nếu chạm vào tường hoặc ra ngoài map, false nếu đường thông thoáng
     */
    public boolean checkCollision(int x, int y, int width, int height) {
        // Đổi từ tọa độ pixel sang tọa độ ô trong ma trận (chia cho TILE_SIZE)
        int leftCol = x / TILE_SIZE;
        int rightCol = (x + width - 1) / TILE_SIZE;
        int topRow = y / TILE_SIZE;
        int bottomRow = (y + height - 1) / TILE_SIZE;

        // Nếu nhân vật đi vượt quá biên bản đồ -> Chặn lại
        if (leftCol < 0 || rightCol >= COLS || topRow < 0 || bottomRow >= ROWS) {
            return true;
        }

        // Duyệt qua các ô mà nhân vật đang chiếm chỗ
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                // Nếu ô đó là tường (giá trị 1) -> Có va chạm!
                if (tiles[row][col] == 1) {
                    return true;
                }
            }
        }

        // Không chạm ô tường nào -> Đi được
        return false;
    }

    /**
     * Vẽ toàn bộ bản đồ lên màn hình.
     */
    public void draw(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                // Tọa độ vẽ tính theo pixel
                int pixelX = col * TILE_SIZE;
                int pixelY = row * TILE_SIZE;

                if (tiles[row][col] == 1) {
                    // Ô TƯỜNG: Vẽ hình vuông màu xám có viền đậm
                    g.setColor(new Color(110, 110, 110));
                    g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(60, 60, 60));
                    g.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                } else {
                    // Ô CỎ: Vẽ hình vuông màu xanh lá nhạt
                    g.setColor(new Color(160, 230, 160));
                    g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(145, 215, 145));
                    g.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    // Lấy tổng chiều rộng map tính theo pixel (để set kích thước cửa sổ)
    public int getWidthInPixels() {
        return COLS * TILE_SIZE;
    }

    // Lấy tổng chiều cao map tính theo pixel
    public int getHeightInPixels() {
        return ROWS * TILE_SIZE;
    }
}


package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Lớp đại diện cho Quả bom (Bomb) trong game Bomberman.
 * Kế thừa từ Entity để tái sử dụng tọa độ (x, y) và khung va chạm (hitbox).
 */
public class Bomb extends Entity {
    // Thời gian đếm ngược từ lúc đặt đến lúc nổ (150 khung hình ~ 2.5 giây ở 60 FPS)
    public static final int FUSE_TIME = 150;
    private int countdown = FUSE_TIME;

    // Bán kính nổ (mặc định lan ra 1 ô mỗi hướng)
    private int radius = 1;

    // Cờ đánh dấu bom đã hết giờ và sẵn sàng phát nổ
    private boolean exploded = false;

    // Khi người chơi vừa bấm phím đặt bom, họ đang đứng bên trong ô bom nên được phép đi ra.
    // Ngay khi người chơi đã bước ra ngoài hoàn toàn, quả bom sẽ trở thành vật cản rắn.
    private boolean playerAllowedInside = true;

    // Vị trí quả bom tính theo đơn vị ô lưới (cột, hàng) trên bản đồ
    private int tileCol;
    private int tileRow;

    // Biến đếm khung hình để tạo hiệu ứng phập phồng và tia lửa ngòi nổ
    private int tick = 0;

    public Bomb(int tileCol, int tileRow, int radius) {
        // Căn hitbox quả bom hơi nhỏ hơn ô gạch một chút để người chơi dễ đi lại gần
        super(tileCol * GameMap.TILE_SIZE + 4,
              tileRow * GameMap.TILE_SIZE + 4,
              GameMap.TILE_SIZE - 8,
              GameMap.TILE_SIZE - 8);

        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.radius = radius;
    }

    /**
     * Cập nhật logic quả bom ở mỗi khung hình:
     * - Giảm thời gian đếm ngược.
     * - Khi đếm ngược về 0, chuyển trạng thái sang đã nổ.
     */
    @Override
    public void update() {
        countdown--;
        tick++;

        if (countdown <= 0) {
            exploded = true;
        }
    }

    /**
     * Kiểm tra xem Player đã bước ra khỏi quả bom hay chưa.
     * Nếu Player đã rời khỏi vùng va chạm, khóa lại để không cho đi xuyên vào nữa.
     */
    public void checkPlayerExit(Player player) {
        if (playerAllowedInside && !intersects(player)) {
            playerAllowedInside = false;
        }
    }

    /**
     * Vẽ quả bom lên màn hình với đầy đủ chi tiết:
     * 1. Bóng mờ dưới chân
     * 2. Thân bom hình cầu đen và vệt sáng phản chiếu
     * 3. Cổ ngòi nổ và tia lửa nhấp nháy
     */
    @Override
    public void draw(Graphics g) {
        int cx = tileCol * GameMap.TILE_SIZE;
        int cy = tileRow * GameMap.TILE_SIZE;

        // Hiệu ứng phập phồng đơn giản: xen kẽ kích thước qua số tick
        int pulse = (tick / 12) % 2 == 0 ? 1 : -1;
        int bombSize = (int) (GameMap.TILE_SIZE * 0.76) + pulse;
        int bombX = cx + (GameMap.TILE_SIZE - bombSize) / 2;
        int bombY = cy + (GameMap.TILE_SIZE - bombSize) / 2 + 2;

        // Bước 1: Bóng mờ dưới chân bom
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(cx + 8, cy + GameMap.TILE_SIZE - 10, GameMap.TILE_SIZE - 16, 7);

        // Bước 2: Thân quả bom (hình cầu màu xám đen)
        g.setColor(new Color(30, 41, 59));
        g.fillOval(bombX, bombY, bombSize, bombSize);

        // Viền quả bom
        g.setColor(new Color(15, 23, 42));
        g.drawOval(bombX, bombY, bombSize, bombSize);

        // Vệt sáng phản chiếu ánh sáng trên thân bom
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(bombX + bombSize / 4, bombY + bombSize / 5, bombSize / 4, bombSize / 5);

        // Bước 3: Cổ quả bom
        int neckW = 8;
        int neckH = 5;
        int neckX = cx + (GameMap.TILE_SIZE - neckW) / 2;
        int neckY = bombY - 3;
        g.setColor(new Color(100, 116, 139));
        g.fillRect(neckX, neckY, neckW, neckH);

        // Bước 4: Tia lửa ngòi nổ nhấp nháy
        if ((tick / 6) % 2 == 0) {
            g.setColor(new Color(245, 158, 11)); // Màu vàng cam
            g.fillOval(neckX + 1, neckY - 5, 6, 6);
            g.setColor(new Color(239, 68, 68)); // Lõi đỏ
            g.fillOval(neckX + 2, neckY - 4, 4, 4);
        } else {
            g.setColor(new Color(239, 68, 68)); // Đỏ
            g.fillOval(neckX + 2, neckY - 4, 5, 5);
            g.setColor(Color.WHITE);             // Tâm trắng
            g.fillOval(neckX + 3, neckY - 3, 3, 3);
        }
    }

    // --- Các hàm Getter ---
    public boolean isExploded() {
        return exploded;
    }

    public boolean isPlayerAllowedInside() {
        return playerAllowedInside;
    }

    public int getTileCol() {
        return tileCol;
    }

    public int getTileRow() {
        return tileRow;
    }

    public int getRadius() {
        return radius;
    }
}

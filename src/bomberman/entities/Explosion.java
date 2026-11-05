package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho Vụ nổ (Explosion) và các tia lửa lan tỏa theo 4 hướng.
 * Kế thừa từ Entity để có thể tương tác và kiểm tra va chạm với các nhân vật khác.
 */
public class Explosion extends Entity {

    /**
     * Lớp con đại diện cho một mẩu tia lửa tại một ô (col, row) cụ thể.
     */
    public static class FlameSegment {
        public int col;
        public int row;

        public FlameSegment(int col, int row) {
            this.col = col;
            this.row = row;
        }

        /**
         * Lấy khung va chạm (hitbox) của mẩu lửa này để thiêu rụi kẻ địch hoặc người chơi.
         */
        public Rectangle getBounds() {
            int padding = 4;
            return new Rectangle(
                col * GameMap.TILE_SIZE + padding,
                row * GameMap.TILE_SIZE + padding,
                GameMap.TILE_SIZE - padding * 2,
                GameMap.TILE_SIZE - padding * 2
            );
        }
    }

    // Danh sách tất cả các mẩu lửa thuộc vụ nổ này (tâm và các nhánh lan ra)
    private List<FlameSegment> segments = new ArrayList<>();

    // Thời gian tồn tại của ngọn lửa (~28 khung hình, khoảng 0.45 giây)
    private int duration = 28;
    private int tick = 0;

    public Explosion(int centerCol, int centerRow, int radius, GameMap map) {
        super(centerCol * GameMap.TILE_SIZE,
              centerRow * GameMap.TILE_SIZE,
              GameMap.TILE_SIZE,
              GameMap.TILE_SIZE);

        // Bước 1: Thêm mẩu lửa tại chính ô tâm của quả bom
        segments.add(new FlameSegment(centerCol, centerRow));

        // Bước 2: Lan tỏa tia lửa theo 4 hướng: Lên (UP), Xuống (DOWN), Trái (LEFT), Phải (RIGHT)
        int[][] directions = {
            {0, -1}, // Lên trên (dy = -1)
            {0, 1},  // Xuống dưới (dy = 1)
            {-1, 0}, // Sang trái (dx = -1)
            {1, 0}   // Sang phải (dx = 1)
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            // Duyệt từng ô theo bán kính nổ (radius)
            for (int step = 1; step <= radius; step++) {
                int c = centerCol + dx * step;
                int r = centerRow + dy * step;

                int tile = map.getTile(r, c);

                if (tile == GameMap.WALL) {
                    // Tường đá không thể phá hủy -> tia lửa bị chặn đứng ngay lập tức
                    break;
                } else if (tile == GameMap.BRICK) {
                    // Tường gạch bị nổ tung -> phá hủy gạch, tạo tia lửa tại đây rồi dừng lại
                    map.destroyBrick(r, c);
                    segments.add(new FlameSegment(c, r));
                    break;
                } else {
                    // Ô cỏ trống -> tia lửa lan qua bình thường và tiếp tục bước tiếp theo
                    segments.add(new FlameSegment(c, r));
                }
            }
        }
    }

    /**
     * Giảm thời gian tồn tại của ngọn lửa ở mỗi khung hình.
     */
    @Override
    public void update() {
        duration--;
        tick++;
    }

    /**
     * Kiểm tra xem vụ nổ đã tắt hoàn toàn hay chưa.
     */
    public boolean isFinished() {
        return duration <= 0;
    }

    /**
     * Kiểm tra xem một thực thể (Player hoặc Enemy) có bị tia lửa thiêu trúng hay không.
     */
    public boolean hitsEntity(Entity entity) {
        if (entity == null) return false;
        Rectangle entityBounds = entity.getBounds();

        for (FlameSegment segment : segments) {
            if (segment.getBounds().intersects(entityBounds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vẽ hiệu ứng ngọn lửa 3 tầng màu rực rỡ lên màn hình.
     */
    @Override
    public void draw(Graphics g) {
        for (FlameSegment seg : segments) {
            int px = seg.col * GameMap.TILE_SIZE;
            int py = seg.row * GameMap.TILE_SIZE;

            // Hiệu ứng tia lửa bập bùng
            int shrink = (tick % 4);

            // Tầng 1 (ngoài cùng): Đỏ cam rực
            g.setColor(new Color(239, 68, 68, 220));
            g.fillRoundRect(px + shrink, py + shrink,
                    GameMap.TILE_SIZE - shrink * 2, GameMap.TILE_SIZE - shrink * 2, 8, 8);

            // Tầng 2 (ở giữa): Vàng cam sáng
            g.setColor(new Color(249, 115, 22, 230));
            int midPad = 6 + shrink;
            g.fillRoundRect(px + midPad, py + midPad,
                    GameMap.TILE_SIZE - midPad * 2, GameMap.TILE_SIZE - midPad * 2, 6, 6);

            // Tầng 3 (lõi tâm lửa): Vàng trắng rực rỡ
            g.setColor(new Color(254, 240, 138, 240));
            int corePad = 12 + shrink;
            g.fillOval(px + corePad, py + corePad,
                    GameMap.TILE_SIZE - corePad * 2, GameMap.TILE_SIZE - corePad * 2);
        }
    }

    public List<FlameSegment> getSegments() {
        return segments;
    }
}

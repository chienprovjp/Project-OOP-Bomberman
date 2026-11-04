package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp quản lý vụ nổ và các tia lửa lan tỏa theo 4 hướng.
 */
public class Explosion extends Entity {
    public static class FlameSegment {
        public int col;
        public int row;

        public FlameSegment(int col, int row) {
            this.col = col;
            this.row = row;
        }

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

    private List<FlameSegment> segments = new ArrayList<>();
    private int duration = 28; // Tồn tại khoảng ~0.45 giây
    private int tick = 0;

    public Explosion(int centerCol, int centerRow, int radius, GameMap map) {
        super(centerCol * GameMap.TILE_SIZE, centerRow * GameMap.TILE_SIZE, GameMap.TILE_SIZE, GameMap.TILE_SIZE);

        // 1. Ô tâm vụ nổ
        segments.add(new FlameSegment(centerCol, centerRow));

        // 2. Lan tỏa 4 hướng: Lên, Xuống, Trái, Phải
        int[][] directions = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            for (int step = 1; step <= radius; step++) {
                int c = centerCol + dx * step;
                int r = centerRow + dy * step;

                int tile = map.getTile(r, c);

                if (tile == GameMap.WALL) {
                    // Tường đá chặn hoàn toàn tia lửa
                    break;
                } else if (tile == GameMap.BRICK) {
                    // Phá hủy tường gạch và tạo tia lửa tại ô này, sau đó dừng lan
                    map.destroyBrick(r, c);
                    segments.add(new FlameSegment(c, r));
                    break;
                } else {
                    // Cỏ thông thoáng: tia lửa tiếp tục lan tỏa
                    segments.add(new FlameSegment(c, r));
                }
            }
        }
    }

    @Override
    public void update() {
        duration--;
        tick++;
    }

    public boolean isFinished() {
        return duration <= 0;
    }

    /**
     * Kiểm tra xem một thực thể có bị tia lửa thiêu trúng hay không.
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

    @Override
    public void draw(Graphics g) {
        for (FlameSegment seg : segments) {
            int px = seg.col * GameMap.TILE_SIZE;
            int py = seg.row * GameMap.TILE_SIZE;

            // Hiệu ứng tia lửa nổ 3 tầng màu rực rỡ
            int shrink = (tick % 4);

            // Tầng ngoài: Đỏ cam
            g.setColor(new Color(239, 68, 68, 220));
            g.fillRoundRect(px + shrink, py + shrink, GameMap.TILE_SIZE - shrink * 2, GameMap.TILE_SIZE - shrink * 2, 8, 8);

            // Tầng giữa: Vàng cam
            g.setColor(new Color(249, 115, 22, 230));
            int midPad = 6 + shrink;
            g.fillRoundRect(px + midPad, py + midPad, GameMap.TILE_SIZE - midPad * 2, GameMap.TILE_SIZE - midPad * 2, 6, 6);

            // Lõi tâm: Vàng tươi sáng
            g.setColor(new Color(254, 240, 138, 240));
            int corePad = 12 + shrink;
            g.fillOval(px + corePad, py + corePad, GameMap.TILE_SIZE - corePad * 2, GameMap.TILE_SIZE - corePad * 2);
        }
    }

    public List<FlameSegment> getSegments() {
        return segments;
    }
}

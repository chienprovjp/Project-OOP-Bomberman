package bomberman.model;

import bomberman.component.Entity;
import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp vụ nổ: lan tỏa tia lửa 4 hướng, phá gạch và nạp ảnh explosion.png.
 */
public class Explosion extends Entity {

    // Mỗi mẩu tia lửa tại một ô (col, row)
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
    private int duration = 28; // Tồn tại ~0.45s
    private int tick = 0;

    // Ảnh tia lửa vụ nổ
    private static BufferedImage sprite = ResourceLoader.loadImage("explosion.png");

    public Explosion(int centerCol, int centerRow, int radius, GameMap map) {
        this(centerCol, centerRow, radius, map, null);
    }

    public Explosion(int centerCol, int centerRow, int radius, GameMap map, List<Item> items) {
        super(centerCol * GameMap.TILE_SIZE,
              centerRow * GameMap.TILE_SIZE,
              GameMap.TILE_SIZE,
              GameMap.TILE_SIZE);

        // Ô tâm vụ nổ
        segments.add(new FlameSegment(centerCol, centerRow));

        // Lan tỏa 4 hướng: Lên, Xuống, Trái, Phải
        int[][] directions = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            for (int step = 1; step <= radius; step++) {
                int c = centerCol + dx * step;
                int r = centerRow + dy * step;
                int tile = map.getTile(r, c);

                if (tile == GameMap.WALL) {
                    break; // Bị tường đá chặn
                } else if (tile == GameMap.BRICK) {
                    Item dropped = map.destroyBrick(r, c); // Phá gạch (Cơ chế 1: Rơi Item)
                    if (dropped != null && items != null) {
                        items.add(dropped);
                    }
                    segments.add(new FlameSegment(c, r));
                    break;
                } else {
                    segments.add(new FlameSegment(c, r)); // Lan tiếp trên cỏ
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

    // Kiểm tra tia lửa thiêu trúng thực thể (Player hoặc Enemy)
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
            int shrink = (tick % 4);

            if (sprite != null) {
                // Vẽ ảnh explosion.png
                g.drawImage(sprite, px + shrink, py + shrink,
                        GameMap.TILE_SIZE - shrink * 2, GameMap.TILE_SIZE - shrink * 2, null);
            } else {
                // Vẽ dự phòng nếu thiếu ảnh
                g.setColor(new Color(239, 68, 68, 220));
                g.fillRoundRect(px + shrink, py + shrink,
                        GameMap.TILE_SIZE - shrink * 2, GameMap.TILE_SIZE - shrink * 2, 8, 8);

                g.setColor(new Color(249, 115, 22, 230));
                int midPad = 6 + shrink;
                g.fillRoundRect(px + midPad, py + midPad,
                        GameMap.TILE_SIZE - midPad * 2, GameMap.TILE_SIZE - midPad * 2, 6, 6);

                g.setColor(new Color(254, 240, 138, 240));
                int corePad = 12 + shrink;
                g.fillOval(px + corePad, py + corePad,
                        GameMap.TILE_SIZE - corePad * 2, GameMap.TILE_SIZE - corePad * 2);
            }
        }
    }

    public List<FlameSegment> getSegments() {
        return segments;
    }
}

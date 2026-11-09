package bomberman.model;

import bomberman.component.Entity;
import bomberman.util.ResourceLoader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
    private List<BrickDebris> brickDebrisList = new ArrayList<>();
    private int duration = 30; // Tồn tại ~0.5s với 3 giai đoạn animation
    private int tick = 0;

    // Ảnh tia lửa vụ nổ
    private static BufferedImage sprite = ResourceLoader.loadImage("explosion.png");

    public Explosion(int centerCol, int centerRow, int radius, GameMap map) {
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
                    map.destroyBrick(r, c); // Phá gạch rồi dừng
                    segments.add(new FlameSegment(c, r));
                    brickDebrisList.add(new BrickDebris(c, r)); // Hiệu ứng vỡ vụn
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
        for (BrickDebris bd : brickDebrisList) {
            bd.update();
        }
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
        Graphics2D g2d = (Graphics2D) g.create();
        int tileSize = GameMap.TILE_SIZE;

        // Vụ nổ 3 giai đoạn: Nở bung (0-6) -> Cháy đỉnh điểm (7-19) -> Tàn lụi & khói (20-30)
        for (FlameSegment seg : segments) {
            int px = seg.col * tileSize;
            int py = seg.row * tileSize;

            float alpha = 1.0f;
            int currentSize;

            if (tick < 7) {
                // Giai đoạn 1: Bùng nổ (0 -> 6 frame) - Nở nhanh từ 60% lên 100%
                float progress = tick / 6.0f;
                currentSize = (int) (tileSize * (0.60f + 0.40f * progress));
            } else if (tick < 20) {
                // Giai đoạn 2: Cháy cực đại (7 -> 19 frame) - 100% kích thước với gợn nhiệt
                int ripple = (tick % 4 < 2) ? 1 : -1;
                currentSize = tileSize + ripple;
            } else {
                // Giai đoạn 3: Tàn lụi & khói (20 -> 30 frame) - Co nhỏ dần và mờ tan biến
                float progress = (tick - 19) / 11.0f;
                currentSize = (int) (tileSize * (1.0f - 0.35f * progress));
                alpha = Math.max(0.0f, 1.0f - progress);
            }

            int drawX = px + (tileSize - currentSize) / 2;
            int drawY = py + (tileSize - currentSize) / 2;

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            if (sprite != null) {
                g2d.drawImage(sprite, drawX, drawY, currentSize, currentSize, null);

                // Ở giai đoạn 1: Thêm lõi sáng chói vàng/trắng tăng độ bộc phá
                if (tick < 7) {
                    int coreSize = currentSize / 2;
                    g2d.setColor(new Color(254, 240, 138, 160));
                    g2d.fillOval(px + (tileSize - coreSize) / 2, py + (tileSize - coreSize) / 2, coreSize, coreSize);
                }
            } else {
                // Vẽ dự phòng nếu thiếu ảnh
                if (tick < 20) {
                    g2d.setColor(new Color(239, 68, 68, 220));
                    g2d.fillRoundRect(drawX, drawY, currentSize, currentSize, 8, 8);

                    g2d.setColor(new Color(249, 115, 22, 230));
                    int midPad = currentSize / 6;
                    g2d.fillRoundRect(drawX + midPad, drawY + midPad, currentSize - midPad * 2, currentSize - midPad * 2, 6, 6);

                    g2d.setColor(new Color(254, 240, 138, 240));
                    int corePad = currentSize / 3;
                    g2d.fillOval(drawX + corePad, drawY + corePad, currentSize - corePad * 2, currentSize - corePad * 2);
                } else {
                    // Tro tàn và khói xám ở giai đoạn 3
                    g2d.setColor(new Color(100, 116, 139, (int) (alpha * 180)));
                    g2d.fillOval(drawX, drawY, currentSize, currentSize);
                }
            }
        }

        // Vẽ các mảnh gạch vỡ vụn (Brick Debris)
        for (BrickDebris bd : brickDebrisList) {
            bd.draw(g2d);
        }

        g2d.dispose();
    }

    public List<FlameSegment> getSegments() {
        return segments;
    }
}

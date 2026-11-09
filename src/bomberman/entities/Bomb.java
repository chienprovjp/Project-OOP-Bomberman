package bomberman.entities;

import bomberman.gui.GameMap;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Lớp quả bom: đếm ngược phát nổ, cản đường và nạp ảnh bomb.png.
 */
public class Bomb extends Entity {
    public static final int FUSE_TIME = 150; // ~2.5 giây đếm ngược
    private int countdown = FUSE_TIME;
    private int radius = 1; // Bán kính nổ (số ô)
    private boolean exploded = false;

    // Cho phép người chơi bước ra ngoài sau khi vừa đặt bom
    private boolean playerAllowedInside = true;

    private int tileCol;
    private int tileRow;
    private int tick = 0;

    // Hạt tàn lửa bắn ra từ ngòi nổ
    private static class Spark {
        float x, y;
        float vx, vy;
        int life;
        int maxLife;
        Color color;

        public Spark(float x, float y, float vx, float vy, int maxLife, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = maxLife;
            this.maxLife = maxLife;
            this.color = color;
        }
    }

    private List<Spark> sparks = new ArrayList<>();
    private static final Random random = new Random();

    // Ảnh quả bom
    private static BufferedImage sprite = loadBombSprite();

    public Bomb(int tileCol, int tileRow, int radius) {
        super(tileCol * GameMap.TILE_SIZE + 4,
              tileRow * GameMap.TILE_SIZE + 4,
              GameMap.TILE_SIZE - 8,
              GameMap.TILE_SIZE - 8);
        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.radius = radius;
    }

    private static BufferedImage loadBombSprite() {
        String fileName = "bomb.png";
        try {
            var url = Bomb.class.getResource("/assets/images/" + fileName);
            if (url != null) return ImageIO.read(url);
        } catch (Exception ignored) {}

        String[] paths = {
            "assets/images/" + fileName,
            "BTL/Project-OOP-Bomberman/assets/images/" + fileName,
            "Project-OOP-Bomberman/assets/images/" + fileName
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try {
                    return ImageIO.read(f);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    @Override
    public void update() {
        countdown--;
        tick++;
        if (countdown <= 0) {
            exploded = true;
        }

        // Tần suất sinh tàn lửa từ ngòi nổ (gần nổ thì bắn tàn lửa nhiều hơn)
        int sparkInterval = (countdown < 40) ? 2 : 4;
        if (tick % sparkInterval == 0) {
            int cx = tileCol * GameMap.TILE_SIZE;
            int cy = tileRow * GameMap.TILE_SIZE;
            float fuseX = cx + GameMap.TILE_SIZE / 2.0f;
            float fuseY = cy + GameMap.TILE_SIZE * 0.18f;

            float vx = (random.nextFloat() - 0.5f) * 1.8f;
            float vy = -random.nextFloat() * 1.5f - 0.6f;
            int maxLife = 10 + random.nextInt(8);
            Color sparkCol = switch (random.nextInt(3)) {
                case 0 -> new Color(254, 240, 138); // Vàng sáng
                case 1 -> new Color(249, 115, 22);  // Cam lửa
                default -> Color.WHITE;
            };
            sparks.add(new Spark(fuseX, fuseY, vx, vy, maxLife, sparkCol));
        }

        // Cập nhật các hạt tàn lửa
        Iterator<Spark> it = sparks.iterator();
        while (it.hasNext()) {
            Spark s = it.next();
            s.x += s.vx;
            s.y += s.vy;
            s.life--;
            if (s.life <= 0) {
                it.remove();
            }
        }
    }

    // Khóa không cho đi xuyên sau khi người chơi đã bước ra khỏi ô bom
    public void checkPlayerExit(Player player) {
        if (playerAllowedInside && !intersects(player)) {
            playerAllowedInside = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        int cx = tileCol * GameMap.TILE_SIZE;
        int cy = tileRow * GameMap.TILE_SIZE;

        // 1. Nhịp thở biến thiên theo thời gian (Ticking Urgency)
        int pulseInterval;
        int pulseAmp;
        if (countdown < 30) {
            // Giai đoạn 3: Sắp nổ (< 0.5s) - đập cực nhanh, co giãn mạnh
            pulseInterval = 3;
            pulseAmp = 3;
        } else if (countdown < 75) {
            // Giai đoạn 2: Cảnh báo (< 1.25s) - đập nhanh gấp đôi
            pulseInterval = 6;
            pulseAmp = 2;
        } else {
            // Giai đoạn 1: Bình thường - thở êm dịu
            pulseInterval = 12;
            pulseAmp = 1;
        }

        int pulse = ((tick / pulseInterval) % 2 == 0) ? pulseAmp : -pulseAmp;
        int bombSize = (int) (GameMap.TILE_SIZE * 0.76) + pulse;

        // Rung lắc nhẹ khi cận kề thời điểm nổ
        int shakeX = 0;
        int shakeY = 0;
        if (countdown < 25) {
            shakeX = (tick % 2 == 0) ? 1 : -1;
            shakeY = ((tick / 2) % 2 == 0) ? 1 : -1;
        }

        int bombX = cx + (GameMap.TILE_SIZE - bombSize) / 2 + shakeX;
        int bombY = cy + (GameMap.TILE_SIZE - bombSize) / 2 + 2 + shakeY;

        // 2. Chớp đỏ cảnh báo nổ (Warning Glow)
        if (countdown < 35 && (tick / 3) % 2 == 0) {
            g.setColor(new Color(239, 68, 68, 110));
            g.fillOval(bombX - 3, bombY - 3, bombSize + 6, bombSize + 6);
        }

        // Bóng mờ dưới đất
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(cx + 8, cy + GameMap.TILE_SIZE - 9, GameMap.TILE_SIZE - 16, 7);

        // 3. Vẽ hình ảnh quả bom
        if (sprite != null) {
            g.drawImage(sprite, bombX, bombY, bombSize, bombSize, null);

            // Phủ ánh đỏ nhẹ khi chớp cảnh báo
            if (countdown < 35 && (tick / 3) % 2 == 0) {
                g.setColor(new Color(255, 60, 60, 80));
                g.fillOval(bombX, bombY, bombSize, bombSize);
            }
        } else {
            // Vẽ dự phòng bằng hình khối nếu thiếu ảnh
            g.setColor(new Color(30, 41, 59));
            g.fillOval(bombX, bombY, bombSize, bombSize);
            g.setColor(new Color(15, 23, 42));
            g.drawOval(bombX, bombY, bombSize, bombSize);

            g.setColor(new Color(255, 255, 255, 120));
            g.fillOval(bombX + bombSize / 4, bombY + bombSize / 5, bombSize / 4, bombSize / 5);

            int neckW = 8;
            int neckH = 5;
            int neckX = cx + (GameMap.TILE_SIZE - neckW) / 2;
            int neckY = bombY - 3;
            g.setColor(new Color(100, 116, 139));
            g.fillRect(neckX, neckY, neckW, neckH);
        }

        // 4. Đốm lửa ngòi nổ lập lòe (Sparkling Fuse Animation)
        int fuseX = cx + GameMap.TILE_SIZE / 2 + shakeX;
        int fuseY = bombY - 2;
        int sparkPhase = (tick / 3) % 3;
        Color sparkColor = switch (sparkPhase) {
            case 0 -> new Color(254, 240, 138); // Vàng sáng
            case 1 -> new Color(249, 115, 22);  // Cam
            default -> Color.WHITE;
        };
        g.setColor(sparkColor);
        int flameSize = (sparkPhase == 0) ? 5 : 4;
        g.fillOval(fuseX - flameSize / 2, fuseY - flameSize / 2, flameSize, flameSize);

        // 5. Vẽ các hạt tàn lửa bay ra từ ngòi nổ
        for (Spark s : sparks) {
            float alpha = s.life / (float) s.maxLife;
            g.setColor(new Color(
                s.color.getRed(),
                s.color.getGreen(),
                s.color.getBlue(),
                Math.max(0, (int) (alpha * 240))
            ));
            g.fillRect((int) s.x, (int) s.y, 2, 2);
        }
    }

    public boolean isExploded() { return exploded; }
    public boolean isPlayerAllowedInside() { return playerAllowedInside; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
    public int getRadius() { return radius; }
}

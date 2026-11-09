package bomberman.entities;

import bomberman.gui.GameMap;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Hiệu ứng vỡ vụn khi ô gạch (BRICK) bị phá hủy bởi bom.
 * Chia viên gạch thành 4 mảnh bay theo 4 góc kèm gia tốc trọng lực.
 */
public class BrickDebris {

    private static class Fragment {
        float x, y;
        float vx, vy;
        int width, height;
        BufferedImage subImage;

        public Fragment(float x, float y, float vx, float vy, int w, int h, BufferedImage sub) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.width = w;
            this.height = h;
            this.subImage = sub;
        }

        public void update() {
            x += vx;
            y += vy;
            vy += 0.35f; // Trọng lực nhẹ kéo rơi xuống
        }
    }

    private List<Fragment> fragments = new ArrayList<>();
    private int maxLife = 22; // Tồn tại ~0.35s
    private int life = maxLife;

    private static BufferedImage brickSprite = loadBrickSprite();

    public BrickDebris(int tileCol, int tileRow) {
        int tileSize = GameMap.TILE_SIZE;
        int bx = tileCol * tileSize;
        int by = tileRow * tileSize;
        int halfTile = tileSize / 2;

        BufferedImage qTL = null, qTR = null, qBL = null, qBR = null;
        if (brickSprite != null) {
            int sw = brickSprite.getWidth();
            int sh = brickSprite.getHeight();
            try {
                qTL = brickSprite.getSubimage(0, 0, sw / 2, sh / 2);
                qTR = brickSprite.getSubimage(sw / 2, 0, sw / 2, sh / 2);
                qBL = brickSprite.getSubimage(0, sh / 2, sw / 2, sh / 2);
                qBR = brickSprite.getSubimage(sw / 2, sh / 2, sw / 2, sh / 2);
            } catch (Exception ignored) {}
        }

        // 4 mảnh góc văng ra 4 hướng
        fragments.add(new Fragment(bx, by, -2.2f, -3.2f, halfTile, halfTile, qTL));
        fragments.add(new Fragment(bx + halfTile, by, 2.2f, -3.2f, halfTile, halfTile, qTR));
        fragments.add(new Fragment(bx, by + halfTile, -1.8f, -1.5f, halfTile, halfTile, qBL));
        fragments.add(new Fragment(bx + halfTile, by + halfTile, 1.8f, -1.5f, halfTile, halfTile, qBR));
    }

    private static BufferedImage loadBrickSprite() {
        String fileName = "brick.png";
        try {
            var url = BrickDebris.class.getResource("/assets/images/" + fileName);
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

    public void update() {
        life--;
        for (Fragment f : fragments) {
            f.update();
        }
    }

    public boolean isFinished() {
        return life <= 0;
    }

    public void draw(Graphics g) {
        if (life <= 0) return;

        Graphics2D g2d = (Graphics2D) g.create();
        float alpha = Math.max(0.0f, Math.min(1.0f, life / (float) maxLife));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        for (Fragment f : fragments) {
            if (f.subImage != null) {
                g2d.drawImage(f.subImage, (int) f.x, (int) f.y, f.width, f.height, null);
            } else {
                g2d.setColor(new Color(180, 83, 9));
                g2d.fillRoundRect((int) f.x, (int) f.y, f.width, f.height, 4, 4);
                g2d.setColor(new Color(120, 53, 15));
                g2d.drawRoundRect((int) f.x, (int) f.y, f.width, f.height, 4, 4);
            }
        }
        g2d.dispose();
    }
}

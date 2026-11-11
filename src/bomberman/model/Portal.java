package bomberman.model;

import bomberman.component.Entity;
import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Cổng dịch chuyển qua màn (Portal): Xuất hiện sau khi người chơi tiêu diệt hết kẻ địch.
 * Bước vào Cổng sẽ hoàn thành màn chơi và chuyển sang màn kế tiếp.
 */
public class Portal extends Entity {
    private static BufferedImage sprite = ResourceLoader.loadImage("portal.png");
    private int tick = 0;

    public Portal(int x, int y) {
        super(x, y, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
    }

    @Override
    public void update() {
        tick++;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Vòng xoáy hào quang ánh sáng xoay vòng
        int pulse = (int) (Math.sin(tick * 0.15) * 4);
        g2d.setColor(new Color(56, 189, 248, 70));
        g2d.fillOval(x - 2 - pulse / 2, y - 2 - pulse / 2, width + 4 + pulse, height + 4 + pulse);

        g2d.setColor(new Color(168, 85, 247, 90));
        g2d.fillOval(x + 2, y + 2, width - 4, height - 4);

        if (sprite != null) {
            g2d.drawImage(sprite, x, y, width, height, null);
        } else {
            // Vẽ vòng xoáy dự phòng
            g2d.setColor(new Color(147, 51, 234));
            g2d.fillOval(x + 4, y + 4, width - 8, height - 8);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(x + 8, y + 8, width - 16, height - 16);
        }
    }
}


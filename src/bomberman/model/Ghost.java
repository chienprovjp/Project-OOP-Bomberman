package bomberman.model;

import bomberman.ai.BFSStrategy;
import bomberman.util.ResourceLoader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Quái Ghost: Có khả năng bay xuyên qua Tường gạch (BRICK), chỉ bị cản bởi
 * Tường đá (WALL), Rào cản (BARRIER) và Bom.
 * Hỗ trợ Strategy Pattern (BFS / A* xuyên gạch). Điểm thưởng: 300.
 */
public class Ghost extends Enemy {

    private int animationTick = 0;

    private static BufferedImage sprite = ResourceLoader.loadImage("ghost.png");

    public Ghost(int startX, int startY) {
        super(startX, startY, 44, 44, 2);
        this.scoreReward = 300;
        this.canPassBrick = true;
        this.moveStrategy = new BFSStrategy(5);
    }

    @Override
    public void update() {
        super.update();
        if (!alive) return;
        animationTick++;
    }

    @Override
    public void move(GameMap map, List<Bomb> bombs) {
        moveAlongGrid(map, bombs, null);
    }

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            drawDeathEffect(g, new Color(168, 85, 247));
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hiệu ứng bay bồng bềnh hình sin
        int floatOffsetY = (int) (Math.sin(animationTick * 0.12) * 3);
        int drawX = x;
        int drawY = y + floatOffsetY;

        // Bóng mờ dưới chân
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(x + 4, y + height - 2, width - 8, 5);

        // Hào quang tím bao quanh
        g2d.setColor(new Color(168, 85, 247, 45));
        g2d.fillOval(drawX - 4, drawY - 4, width + 8, height + 8);

        // Thân hình bán trong suốt (Alpha = 0.85)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));

        if (sprite != null) {
            if (direction == Player.Direction.LEFT) {
                g2d.drawImage(sprite, drawX + width, drawY, -width, height, null);
            } else {
                g2d.drawImage(sprite, drawX, drawY, width, height, null);
            }
        } else {
            drawFallback(g2d, drawX, drawY);
        }

        g2d.dispose();
    }

    private void drawFallback(Graphics2D g, int drawX, int drawY) {
        g.setColor(new Color(168, 85, 247));
        g.fillRoundRect(drawX + 4, drawY + 2, width - 8, height - 6, 20, 20);

        g.setColor(Color.RED);
        int eyeOffset = (direction == Player.Direction.LEFT) ? -4 : (direction == Player.Direction.RIGHT ? 4 : 0);
        g.fillOval(drawX + 12 + eyeOffset, drawY + 12, 6, 8);
        g.fillOval(drawX + 26 + eyeOffset, drawY + 12, 6, 8);
    }
}

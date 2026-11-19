package bomberman.model;

import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Kẻ địch Ballom: quái vật quả bóng cam di chuyển bám tim đường theo MoveStrategy.
 */
public class Ballom extends Enemy {
    private int animationTick = 0;

    // Ảnh kẻ địch Ballom
    private static BufferedImage sprite = ResourceLoader.loadImage("ballom.png");

    public Ballom(int startX, int startY) {
        super(startX, startY, 44, 44, 2);
        this.scoreReward = 100;
    }

    @Override
    public void update() {
        super.update(); // Cập nhật đếm nhịp chết ở lớp cha Enemy
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
            drawDeathEffect(g, new Color(249, 115, 22));
            return;
        }

        // Nhịp phập phồng khi bay
        int step = (animationTick / 6) % 4;
        int pulseW = (step == 1) ? 2 : (step == 3 ? -1 : 0);
        int pulseH = (step == 1) ? -2 : (step == 3 ? 1 : 0);
        int spriteW = width + pulseW;
        int spriteH = height + pulseH;
        int drawX = x - (spriteW - width) / 2;
        int drawY = y - (spriteH - height) / 2;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bóng dưới chân
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(x + 2, y + height - 3, width - 4, 6);

        if (sprite != null) {
            if (direction == Player.Direction.LEFT) {
                g2d.drawImage(sprite, drawX + spriteW, drawY, -spriteW, spriteH, null);
            } else {
                g2d.drawImage(sprite, drawX, drawY, spriteW, spriteH, null);
            }
        } else {
            drawFallback(g2d, drawX, drawY, spriteW, spriteH);
        }

        g2d.dispose();
    }

    private void drawFallback(Graphics2D g, int drawX, int drawY, int spriteW, int spriteH) {
        g.setColor(new Color(249, 115, 22));
        g.fillOval(drawX, drawY, spriteW, spriteH);
        g.setColor(new Color(194, 65, 12));
        g.drawOval(drawX, drawY, spriteW, spriteH);

        g.setColor(new Color(255, 255, 255, 140));
        g.fillOval(drawX + spriteW / 4, drawY + 4, spriteW / 3, spriteH / 4);

        int eyeOffsetX = switch (direction) {
            case LEFT  -> -3;
            case RIGHT -> 3;
            default    -> 0;
        };
        int eyeOffsetY = switch (direction) {
            case UP    -> -2;
            case DOWN  -> 2;
            default    -> 0;
        };

        int eye1X = drawX + spriteW / 2 - 8 + eyeOffsetX;
        int eye2X = drawX + spriteW / 2 + 2 + eyeOffsetX;
        int eyeY  = drawY + spriteH / 3 + eyeOffsetY;

        g.setColor(Color.WHITE);
        g.fillOval(eye1X, eyeY, 6, 8);
        g.fillOval(eye2X, eyeY, 6, 8);

        g.setColor(Color.BLACK);
        g.fillOval(eye1X + 2 + eyeOffsetX / 2, eyeY + 2 + eyeOffsetY / 2, 3, 4);
        g.fillOval(eye2X + 2 + eyeOffsetX / 2, eyeY + 2 + eyeOffsetY / 2, 3, 4);

        g.setColor(new Color(124, 45, 18));
        g.drawArc(drawX + spriteW / 2 - 4 + eyeOffsetX, drawY + spriteH / 2 + 4, 8, 5, 0, -180);
    }
}

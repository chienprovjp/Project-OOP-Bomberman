package bomberman.model;

import bomberman.component.Entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Random;

/**
 * Lớp trừu tượng định nghĩa các thuộc tính và hành vi chung của Kẻ địch (Enemy).
 */
public abstract class Enemy extends Entity {
    protected int speed = 2;
    protected Player.Direction direction = Player.Direction.RIGHT;
    protected boolean alive = true;
    protected boolean deathFinished = false;
    protected int deathAnimationTick = 0;
    protected static final int DEATH_ANIM_DURATION = 24; // ~0.4 giây diễn hoạt khi chết
    protected int scoreReward = 100;
    protected static final Random random = new Random();

    public Enemy(int startX, int startY, int width, int height, int speed) {
        super(startX, startY, width, height);
        this.speed = speed;
        changeRandomDirection();
    }

    // Từng loại quái vật con sẽ tự cài đặt logic di chuyển riêng
    public abstract void move(GameMap map, List<Bomb> bombs);

    // Nạp chồng hỗ trợ truyền Player (phục vụ quái rượt đuổi Oneal)
    public void move(GameMap map, List<Bomb> bombs, Player player) {
        move(map, bombs);
    }

    @Override
    public void update() {
        if (!alive) {
            deathAnimationTick++;
            if (deathAnimationTick >= DEATH_ANIM_DURATION) {
                deathFinished = true;
            }
        }
    }

    // Đổi sang một hướng ngẫu nhiên
    protected void changeRandomDirection() {
        Player.Direction[] dirs = Player.Direction.values();
        this.direction = dirs[random.nextInt(dirs.length)];
    }

    // Kiểm tra va chạm với các quả bom đang hoạt động
    protected boolean checkBombCollision(int targetX, int targetY, List<Bomb> bombs) {
        if (bombs == null || bombs.isEmpty()) {
            return false;
        }
        Rectangle targetBox = new Rectangle(targetX, targetY, width, height);
        for (Bomb b : bombs) {
            if (!b.isExploded() && targetBox.intersects(b.getBounds())) {
                return true;
            }
        }
        return false;
    }

    // Hiệu ứng khi chết: chớp sáng rồi tan biến thành hạt linh hồn
    protected void drawDeathEffect(Graphics g, Color primaryColor) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = x + width / 2;
        int cy = y + height / 2;

        if (deathAnimationTick < 7) {
            // Chớp sáng & rung giật nhẹ
            int shake = (deathAnimationTick % 2 == 0) ? 2 : -2;
            Color flashCol = (deathAnimationTick % 2 == 0) ? new Color(255, 255, 255, 230) : primaryColor;
            g2d.setColor(flashCol);
            g2d.fillOval(x + shake, y, width, height);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x + shake, y, width, height);
        } else {
            // Hạt linh hồn bay lên và mờ dần
            float progress = (deathAnimationTick - 7) / (float) (DEATH_ANIM_DURATION - 7);
            float alpha = Math.max(0.0f, 1.0f - progress);
            int spread = (int) (progress * 22);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int ringSize = (int) (width * (0.8f + progress * 0.6f));
            g2d.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), (int) (alpha * 120)));
            g2d.fillOval(cx - ringSize / 2, cy - ringSize / 2, ringSize, ringSize);

            int pCount = 6;
            for (int i = 0; i < pCount; i++) {
                double angle = i * (Math.PI * 2 / pCount);
                int px = cx + (int) (Math.cos(angle) * spread);
                int py = cy + (int) (Math.sin(angle) * spread) - (int) (progress * 14);
                int pSize = Math.max(2, (int) (6 * (1.0f - progress * 0.5f)));

                g2d.setColor(Color.WHITE);
                g2d.fillOval(px - pSize / 2, py - pSize / 2, pSize, pSize);
                g2d.setColor(primaryColor);
                g2d.drawOval(px - pSize / 2, py - pSize / 2, pSize, pSize);
            }
        }
        g2d.dispose();
    }

    public boolean isAlive() { return alive; }
    public boolean isDeathFinished() { return deathFinished; }
    public void kill() {
        if (!alive) return;
        this.alive = false;
        this.deathAnimationTick = 0;
    }
    public int getScoreReward() { return scoreReward; }
    public Player.Direction getDirection() { return direction; }
}

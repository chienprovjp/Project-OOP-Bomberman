package bomberman.model;

import bomberman.ai.AStarStrategy;
import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Quái Oneal (Cấp cao):
 * - Kết hợp đồ họa & cơ chế tăng tốc khi phát hiện người chơi (speed: 2 -> 3) của Khang.
 * - Kết hợp Strategy Pattern (mặc định A* / BFS) để tìm đường tối ưu né tường và né bom.
 * - Điểm thưởng: 200.
 */
public class Oneal extends Enemy {

    private static final int NORMAL_SPEED = 2; // Tốc độ tuần tra
    private static final int CHASE_SPEED = 3;  // Tốc độ rượt đuổi
    private static final int DETECT_RANGE = 8; // Bán kính phát hiện (8 ô)

    private boolean chasing = false;
    private int animationTick = 0;

    private static BufferedImage sprite = ResourceLoader.loadImage("oneal.png");

    static {
        if (sprite == null) {
            sprite = ResourceLoader.loadImage("ballom_blue.png");
        }
    }

    public Oneal(int startX, int startY) {
        this(startX, startY, CHASE_SPEED);
    }

    public Oneal(int startX, int startY, int chaseSpeed) {
        super(startX, startY, 44, 44, NORMAL_SPEED);
        this.scoreReward = 200;
        this.moveStrategy = new AStarStrategy(DETECT_RANGE + 1, true);
    }

    @Override
    public void update() {
        super.update();
        if (!alive) return;
        animationTick++;
    }

    @Override
    public void move(GameMap map, List<Bomb> bombs) {
        move(map, bombs, null);
    }

    @Override
    public void move(GameMap map, List<Bomb> bombs, Player player) {
        if (!alive) return;

        updateChasingState(map, player);
        moveAlongGrid(map, bombs, player);
    }

    private void updateChasingState(GameMap map, Player player) {
        if (player == null) {
            this.chasing = false;
            this.speed = NORMAL_SPEED;
            return;
        }

        int tileSize = GameMap.TILE_SIZE;
        int myCol = (x + width / 2) / tileSize;
        int myRow = (y + height / 2) / tileSize;
        int pCol = (player.getX() + player.getWidth() / 2) / tileSize;
        int pRow = (player.getY() + player.getHeight() / 2) / tileSize;

        int dist = Math.abs(myCol - pCol) + Math.abs(myRow - pRow);
        boolean hasLOS = hasClearLineOfSight(map, myCol, myRow, pCol, pRow);

        if (dist <= DETECT_RANGE || hasLOS) {
            this.chasing = true;
            this.speed = CHASE_SPEED;
        } else {
            this.chasing = false;
            this.speed = NORMAL_SPEED;
        }
    }

    // Kiểm tra đường thẳng tầm nhìn (Line of Sight) tới người chơi
    private boolean hasClearLineOfSight(GameMap map, int c1, int r1, int c2, int r2) {
        if (c1 == c2) {
            int startRow = Math.min(r1, r2) + 1;
            int endRow = Math.max(r1, r2) - 1;
            for (int r = startRow; r <= endRow; r++) {
                int tile = map.getTile(r, c1);
                if (tile != GameMap.GRASS) {
                    return false;
                }
            }
            return true;
        } else if (r1 == r2) {
            int startCol = Math.min(c1, c2) + 1;
            int endCol = Math.max(c1, c2) - 1;
            for (int c = startCol; c <= endCol; c++) {
                int tile = map.getTile(r1, c);
                if (tile != GameMap.GRASS) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void draw(Graphics g) {
        if (!alive) {
            drawDeathEffect(g, new Color(56, 189, 248));
            return;
        }

        // Nhịp lơ lửng phập phồng (nhanh hơn khi đuổi)
        int animRate = chasing ? 4 : 6;
        int step = (animationTick / animRate) % 4;
        int pulseW = (step == 1) ? 2 : (step == 3 ? -1 : 0);
        int pulseH = (step == 1) ? -2 : (step == 3 ? 1 : 0);
        int spriteW = width + pulseW;
        int spriteH = height + pulseH;
        int drawX = x - (spriteW - width) / 2;
        int drawY = y - (spriteH - height) / 2;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Hào quang xanh cảnh báo khi rượt đuổi
        if (chasing) {
            g2d.setColor(new Color(56, 189, 248, 80));
            g2d.fillOval(drawX - 4, drawY - 4, spriteW + 8, spriteH + 8);
        }

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
            g2d.setColor(new Color(56, 189, 248));
            g2d.fillOval(drawX, drawY, spriteW, spriteH);
            g2d.setColor(new Color(2, 132, 199));
            g2d.drawOval(drawX, drawY, spriteW, spriteH);
        }

        g2d.dispose();
    }

    public boolean isChasing() {
        return chasing;
    }
}

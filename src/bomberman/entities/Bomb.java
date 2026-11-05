package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Lớp quả bom: đếm ngược phát nổ, cản đường và vẽ hiệu ứng ngòi nổ.
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

    public Bomb(int tileCol, int tileRow, int radius) {
        super(tileCol * GameMap.TILE_SIZE + 4,
              tileRow * GameMap.TILE_SIZE + 4,
              GameMap.TILE_SIZE - 8,
              GameMap.TILE_SIZE - 8);
        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.radius = radius;
    }

    @Override
    public void update() {
        countdown--;
        tick++;
        if (countdown <= 0) {
            exploded = true;
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

        int pulse = (tick / 12) % 2 == 0 ? 1 : -1;
        int bombSize = (int) (GameMap.TILE_SIZE * 0.76) + pulse;
        int bombX = cx + (GameMap.TILE_SIZE - bombSize) / 2;
        int bombY = cy + (GameMap.TILE_SIZE - bombSize) / 2 + 2;

        // Bóng mờ
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(cx + 8, cy + GameMap.TILE_SIZE - 10, GameMap.TILE_SIZE - 16, 7);

        // Thân quả bom
        g.setColor(new Color(30, 41, 59));
        g.fillOval(bombX, bombY, bombSize, bombSize);
        g.setColor(new Color(15, 23, 42));
        g.drawOval(bombX, bombY, bombSize, bombSize);

        // Vệt sáng phản chiếu
        g.setColor(new Color(255, 255, 255, 120));
        g.fillOval(bombX + bombSize / 4, bombY + bombSize / 5, bombSize / 4, bombSize / 5);

        // Cổ bom & ngòi nổ
        int neckW = 8;
        int neckH = 5;
        int neckX = cx + (GameMap.TILE_SIZE - neckW) / 2;
        int neckY = bombY - 3;
        g.setColor(new Color(100, 116, 139));
        g.fillRect(neckX, neckY, neckW, neckH);

        // Tia lửa ngòi nổ
        if ((tick / 6) % 2 == 0) {
            g.setColor(new Color(245, 158, 11));
            g.fillOval(neckX + 1, neckY - 5, 6, 6);
            g.setColor(new Color(239, 68, 68));
            g.fillOval(neckX + 2, neckY - 4, 4, 4);
        } else {
            g.setColor(new Color(239, 68, 68));
            g.fillOval(neckX + 2, neckY - 4, 5, 5);
            g.setColor(Color.WHITE);
            g.fillOval(neckX + 3, neckY - 3, 3, 3);
        }
    }

    public boolean isExploded() { return exploded; }
    public boolean isPlayerAllowedInside() { return playerAllowedInside; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
    public int getRadius() { return radius; }
}

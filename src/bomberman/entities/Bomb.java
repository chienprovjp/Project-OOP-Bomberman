package bomberman.entities;

import bomberman.gui.GameMap;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

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

        if (sprite != null) {
            // Vẽ ảnh bomb.png phập phồng nhẹ theo nhịp
            g.drawImage(sprite, bombX, bombY, bombSize, bombSize, null);
        } else {
            // Vẽ dự phòng bằng hình khối nếu thiếu ảnh
            g.setColor(new Color(0, 0, 0, 60));
            g.fillOval(cx + 8, cy + GameMap.TILE_SIZE - 10, GameMap.TILE_SIZE - 16, 7);

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
    }

    public boolean isExploded() { return exploded; }
    public boolean isPlayerAllowedInside() { return playerAllowedInside; }
    public int getTileCol() { return tileCol; }
    public int getTileRow() { return tileRow; }
    public int getRadius() { return radius; }
}

package bomberman.model;

import bomberman.component.Entity;
import bomberman.ui.GamePanel;
import bomberman.util.ResourceLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Lớp trừu tượng đại diện cho một vật phẩm (Item) hỗ trợ người chơi.
 * Kế thừa từ Entity (component) để quản lý tọa độ và va chạm.
 */
public abstract class Item extends Entity {
    protected BufferedImage sprite;
    protected boolean collected = false;
    protected int tick = 0;
    // Miễn nhiễm với vụ nổ vừa tạo ra nó trong ~0.65 giây (đủ để tia lửa phá gạch tan biến)
    protected int immuneTimer = 40;
    protected static final Random random = new Random();

    public Item(int x, int y, String spriteName) {
        super(x + (int) (GameMap.TILE_SIZE * 0.12),
              y + (int) (GameMap.TILE_SIZE * 0.12),
              (int) (GameMap.TILE_SIZE * 0.76),
              (int) (GameMap.TILE_SIZE * 0.76));
        if (spriteName != null) {
            this.sprite = ResourceLoader.loadImage(spriteName);
        }
    }

    @Override
    public void update() {
        tick++;
        if (immuneTimer > 0) {
            immuneTimer--;
        }
    }

    public boolean canBeBurned() {
        return immuneTimer <= 0;
    }

    /**
     * Phương thức trừu tượng: Áp dụng hiệu ứng đặc thù của Item lên Player hoặc Game.
     * Minh chứng cho Tính đa hình (Polymorphism) trong OOP.
     */
    public abstract void applyEffect(Player player, GamePanel panel);

    /**
     * Tên mô tả của Item khi người chơi nhặt được.
     */
    public abstract String getItemName();

    @Override
    public void draw(Graphics g) {
        if (collected) return;

        // Hiệu ứng nhấp nhô nhẹ (Floating animation) tạo cảm giác sống động
        int bounce = (int) (Math.sin(tick * 0.18) * 3);
        int drawX = x;
        int drawY = y + bounce;

        // Vòng tròn hào quang phát sáng nhẹ bên dưới item
        g.setColor(new Color(255, 255, 255, 45));
        g.fillOval(drawX - 2, drawY + height - 6, width + 4, 8);

        if (sprite != null) {
            g.drawImage(sprite, drawX, drawY, width, height, null);
        } else {
            // Vẽ biểu tượng dự phòng nếu thiếu file ảnh
            g.setColor(new Color(251, 191, 36));
            g.fillRoundRect(drawX, drawY, width, height, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString("?", drawX + width / 3, drawY + height * 2 / 3);
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
    }

    /**
     * Tạo ngẫu nhiên một trong các loại Item (Bomb, Flame, Speed, Life).
     */
    public static Item createRandomItem(int x, int y) {
        int r = random.nextInt(4);
        return switch (r) {
            case 0 -> new BombItem(x, y);
            case 1 -> new FlameItem(x, y);
            case 2 -> new SpeedItem(x, y);
            default -> new LifeItem(x, y);
        };
    }
}


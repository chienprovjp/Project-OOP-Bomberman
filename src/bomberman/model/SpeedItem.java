package bomberman.model;

import bomberman.ui.GamePanel;

/**
 * Vật phẩm tăng tốc độ di chuyển của người chơi.
 */
public class SpeedItem extends Item {
    public SpeedItem(int x, int y) {
        super(x, y, "item_speed.png");
    }

    @Override
    public void applyEffect(Player player, GamePanel panel) {
        player.increaseSpeed();
    }

    @Override
    public String getItemName() {
        return "+1 TỐC ĐỘ DI CHUYỂN";
    }
}


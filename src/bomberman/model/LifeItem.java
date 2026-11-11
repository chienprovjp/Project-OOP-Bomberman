package bomberman.model;

import bomberman.ui.GamePanel;

/**
 * Vật phẩm tăng thêm 1 mạng cho người chơi.
 */
public class LifeItem extends Item {
    public LifeItem(int x, int y) {
        super(x, y, "item_life.png");
    }

    @Override
    public void applyEffect(Player player, GamePanel panel) {
        panel.increaseLives();
    }

    @Override
    public String getItemName() {
        return "+1 MẠNG";
    }
}


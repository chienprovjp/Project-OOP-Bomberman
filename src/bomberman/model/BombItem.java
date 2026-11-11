package bomberman.model;

import bomberman.ui.GamePanel;

/**
 * Vật phẩm tăng số lượng bom tối đa mà người chơi có thể đặt cùng lúc.
 */
public class BombItem extends Item {
    public BombItem(int x, int y) {
        super(x, y, "item_bomb.png");
    }

    @Override
    public void applyEffect(Player player, GamePanel panel) {
        panel.increaseMaxBombs();
    }

    @Override
    public String getItemName() {
        return "+1 BOM TỐI ĐA";
    }
}


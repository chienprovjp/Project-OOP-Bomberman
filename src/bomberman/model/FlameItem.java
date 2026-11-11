package bomberman.model;

import bomberman.ui.GamePanel;

/**
 * Vật phẩm tăng bán kính nổ (độ dài tia lửa) của bom.
 */
public class FlameItem extends Item {
    public FlameItem(int x, int y) {
        super(x, y, "item_flame.png");
    }

    @Override
    public void applyEffect(Player player, GamePanel panel) {
        panel.increaseBombRadius();
    }

    @Override
    public String getItemName() {
        return "+1 ĐỘ DÀI TIA LỬA";
    }
}


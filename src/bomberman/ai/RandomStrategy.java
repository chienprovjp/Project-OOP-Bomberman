package bomberman.ai;

import bomberman.model.Bomb;
import bomberman.model.Enemy;
import bomberman.model.GameMap;
import bomberman.model.Player;

import java.util.List;

/**
 * Chiến lược di chuyển ngẫu nhiên (Random Patrol).
 * Dùng cho độ khó DỄ hoặc quái cơ bản khi không đuổi theo người chơi.
 */
public class RandomStrategy implements MoveStrategy {

    @Override
    public Player.Direction chooseDirection(Enemy enemy, GameMap map, List<Bomb> bombs, Player target) {
        return GridPathHelper.pickSafeRandomDirection(enemy, map, bombs, enemy.canPassBrick());
    }

    @Override
    public String getStrategyName() {
        return "Random Patrol";
    }
}

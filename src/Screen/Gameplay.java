package src.Screen;

import src.Map.GameMap;
import src.Map.MapData;
import src.Window.Window;

public class Gameplay extends Screen {

    private static Gameplay instance;
    private final GameMap gameMap;

    private Gameplay() {
        super();

        gameMap = new GameMap();

        gameMap.setLayoutX(
            (Window.getInstance().getWidth() - gameMap.getWidth()) / 2
        );

        gameMap.setLayoutY(
            (Window.getInstance().getHeight() - gameMap.getHeight()) / 2
        );

        root.getChildren().add(gameMap);
    }

    public static Gameplay getInstance() {
        if (instance == null) {
            instance = new Gameplay();
        }

        return instance;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void startLevel(int level) {
        MapData.loadLevel(level);
        gameMap.reload();
        centerMap();
    }

    private void centerMap() {
        gameMap.setLayoutX(
            (Window.getInstance().getWidth() - gameMap.getWidth()) / 2
        );
        gameMap.setLayoutY(
            (Window.getInstance().getHeight() - gameMap.getHeight()) / 2
        );
    }
}
package src.Screen;

import javafx.scene.control.Button;
import src.Main.StateMachine;
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

        Button backButton = new Button("Quay lai");
        backButton.setLayoutX(24);
        backButton.setLayoutY(24);
        backButton.setStyle(
            "-fx-background-color: rgba(35, 54, 35, 0.88);"
                + "-fx-background-radius: 18px;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 9px 16px;"
                + "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> {
            StateMachine.getInstance().setState(StateMachine.State.LEVEL_SELECT);
            Window.getInstance().changeScreen();
        });
        root.getChildren().add(backButton);
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
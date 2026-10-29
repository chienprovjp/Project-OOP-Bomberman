package src.Screen;

import src.Map.GameMap;
import javafx.scene.layout.StackPane;

public class Gameplay extends Screen {
    private static Gameplay instance;

    private Gameplay() {
        super();
        StackPane gameplayRoot = new StackPane();
        gameplayRoot.setPrefSize(1280, 720);
        root = gameplayRoot;
        root.getChildren().add(new GameMap());
    }

    public static Gameplay getInstance() {
        if (instance == null) {
            instance = new Gameplay();
        }
        return instance;
    }
}
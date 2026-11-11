package src.Screen;

import java.io.File;

import javafx.scene.control.Button;
import javafx.scene.shape.Circle;
import src.Main.StateMachine;
import src.Map.MapData;
import src.Window.Window;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class LevelSelect extends Screen {

    private static LevelSelect instance;

    private LevelSelect() {
        super();
        this.root.setPrefSize(1280, 720);
        Image background = new Image(
            new File("assets/backgroundlevel.png").toURI().toString()
        );
        ImageView backgroundImageView = new ImageView(background);
        backgroundImageView.setFitWidth(1280);
        backgroundImageView.setFitHeight(720);
        getRoot().getChildren().add(backgroundImageView);

        double[][] levelPositions = {
            {160, 198},
            {159, 528},
            {505, 389},
            {640, 210},
            {790, 350},
            {740, 550},
            {1120, 200},
            {1120, 330},
            {1120, 520}
        };

        for (int i = 1; i <= MapData.LEVEL_COUNT; i++) {
            int level = i;

            Button levelButton = new Button("LEVEL" + level);
            levelButton.setAccessibleText("Man " + level);
            levelButton.setShape(new Circle(40));
            levelButton.setMinSize(60, 60);
            levelButton.setPrefSize(60, 60);
            levelButton.setMaxSize(60, 60);
            levelButton.setStyle(
                "-fx-background-color: #f4c542;"
                    + "-fx-background-radius: 40px;"
                    + "-fx-border-color: #8d4e24;"
                    + "-fx-border-width: 4px;"
                    + "-fx-border-radius: 40px;"
                    + "-fx-text-fill: #422516;"
                    + "-fx-font-size: 12px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-cursor: hand;"
            );
            levelButton.setLayoutX(levelPositions[level - 1][0] - 30);
            levelButton.setLayoutY(levelPositions[level - 1][1] - 30);
            levelButton.setOnAction(e -> {
                Gameplay.getInstance().startLevel(level);
                StateMachine.getInstance().setState(StateMachine.State.GAMEPLAY);
                Window.getInstance().changeScreen();
            });

            getRoot().getChildren().add(levelButton);
        }

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
            StateMachine.getInstance().setState(StateMachine.State.MAIN_MENU);
            Window.getInstance().changeScreen();
        });
        getRoot().getChildren().add(backButton);
    }

    public static LevelSelect getInstance() {
        if (instance == null) {
            instance = new LevelSelect();
        }

        return instance;
    }
}

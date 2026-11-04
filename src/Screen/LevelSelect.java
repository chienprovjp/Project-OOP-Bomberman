package src.Screen;

import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
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

        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(
            Window.getInstance().getWidth(),
            Window.getInstance().getHeight()
        );

        for (int i = 1; i <= MapData.LEVEL_COUNT; i++) {
            int level = i;

            Button levelButton = new Button("Man " + level);
            levelButton.setOnAction(e -> {
                Gameplay.getInstance().startLevel(level);
                StateMachine.getInstance().setState(StateMachine.State.GAMEPLAY);
                Window.getInstance().changeScreen();
            });

            box.getChildren().add(levelButton);
        }

        Button backButton = new Button("Quay lai");
        backButton.setOnAction(e -> {
            StateMachine.getInstance().setState(StateMachine.State.MAIN_MENU);
            Window.getInstance().changeScreen();
        });
        box.getChildren().add(backButton);

        root.getChildren().add(box);
    }

    public static LevelSelect getInstance() {
        if (instance == null) {
            instance = new LevelSelect();
        }

        return instance;
    }
}

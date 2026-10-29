package src.Screen;

import java.io.File;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import src.Main.StateMachine;
import src.Window.Window;

public class MainMenu extends Screen {
    private static MainMenu instance;
    private ImageView backgroundImageView;
    private ImageView playButtonImageView;


    private MainMenu() {
        super();
        this.root.setPrefSize(1280, 720);
        Image background = new Image(new File("assets/background.png").toURI().toString());
        backgroundImageView = new ImageView(background);
        backgroundImageView.setFitWidth(1280);
        backgroundImageView.setFitHeight(720);

        Image playButtonImage = new Image(new File("assets/Buttons/Play.png").toURI().toString());
        playButtonImageView = new ImageView(playButtonImage);
        playButtonImageView.setPreserveRatio(true);
        playButtonImageView.setFitWidth(235);
        playButtonImageView.setFitHeight(60);
        playButtonImageView.setLayoutX(200);
        playButtonImageView.setLayoutY(400);
        playButtonImageView.setOnMouseClicked(change -> {
            StateMachine.getInstance().setState(StateMachine.State.GAMEPLAY);
            Window.getInstance().changeScreen();
        });

        root.getChildren().addAll(backgroundImageView, playButtonImageView);


    }
 
    public static MainMenu getInstance() {
        if (instance == null) {
            instance = new MainMenu();
        }
        return instance;
    }
    
}

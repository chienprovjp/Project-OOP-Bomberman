package src.Window;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.VBox;

import src.Screen.*;

import src.Main.StateMachine;

public class Window {
    private int width = 1280;
    private int height = 720;

    private static Window instance;
    private static VBox root;
    private static Scene scene;

    private static Screen screen;

    private Window(int width, int height) {
        this.width = width;
        this.height = height;

       
    
        root = new VBox(15);
        root.setAlignment(Pos.TOP_LEFT);
        scene = new Scene(root, width, height);

        changeScreen();


    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static Window getInstance() {
        if (instance == null) {
            instance = new Window(1280, 720);
        }
        return instance;
    }

    public VBox getRoot() {
        return root;
    }

    public Scene getScene() {
        return scene;
    }

    public void changeScreen() {
        StateMachine stateMachine = StateMachine.getInstance();
        switch (stateMachine.getCurrentState()) {
            case MAIN_MENU:
                screen = MainMenu.getInstance();
                break;
            case GAMEPLAY:
                screen = Gameplay.getInstance();
                break;
            case PAUSE_MENU:
                // Implement PauseMenu screen
                break;
            case GAME_OVER:
                // Implement GameOver screen
                break;
        }
        root.getChildren().setAll(screen.getRoot());

    }
}

package src.Window;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import src.Main.StateMachine;
import src.Screen.Gameplay;
import src.Screen.MainMenu;
import src.Screen.Screen;

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

        root = new VBox();
        root.setAlignment(Pos.TOP_LEFT);
        scene = new Scene(root, width, height);

        // Bắt sự kiện bàn phím từ Scene và chuyển tiếp cho Gameplay
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (StateMachine.getInstance().getCurrentState() == StateMachine.State.GAMEPLAY) {
                    Gameplay.getInstance().stopGameLoop();
                    StateMachine.getInstance().setState(StateMachine.State.MAIN_MENU);
                    changeScreen();
                    return;
                }
            }
            if (StateMachine.getInstance().getCurrentState() == StateMachine.State.GAMEPLAY) {
                Gameplay.getInstance().handleKeyPressed(e.getCode());
            }
        });

        scene.setOnKeyReleased(e -> {
            if (StateMachine.getInstance().getCurrentState() == StateMachine.State.GAMEPLAY) {
                Gameplay.getInstance().handleKeyReleased(e.getCode());
            }
        });

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
                if (screen instanceof Gameplay) {
                    Gameplay.getInstance().stopGameLoop();
                }
                screen = MainMenu.getInstance();
                break;
            case GAMEPLAY:
                screen = Gameplay.getInstance();
                Gameplay.getInstance().startGameLoop();
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

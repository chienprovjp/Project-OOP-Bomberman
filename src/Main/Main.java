package src.Main;

import javafx.application.Application;
import javafx.stage.Stage;

import src.Window.Window;

public class Main extends Application {
    private static Window window;

    @Override
    public void start(Stage stage) {
        window = Window.getInstance();
        // Bố cục chính
        stage.setTitle("Bomberman v3.14.15.9.26");
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setScene(window.getScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package src.Screen;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import src.Map.GameMap;

public class Gameplay extends Screen {
    private static Gameplay instance;

    private final GameMap gameMap;
    private AnimationTimer gameLoop;
    private boolean isRunning = false;

    private Gameplay() {
        super();
        this.gameMap = new GameMap();

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPrefSize(1280, 720);
        mainLayout.setStyle("-fx-background-color: #12131c;");

        // Bảng điều khiển HUD bên trái
        VBox leftHud = createHudSidebar();
        mainLayout.setLeft(leftHud);

        // Vùng bản đồ game ở giữa
        StackPane mapContainer = new StackPane(gameMap);
        mapContainer.setAlignment(Pos.CENTER);
        mapContainer.setPadding(new Insets(20));
        mapContainer.setStyle(
            "-fx-background-color: #1a1b26; " +
            "-fx-border-color: #2e3046; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 8);"
        );

        StackPane centerWrapper = new StackPane(mapContainer);
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setPadding(new Insets(20, 40, 20, 20));
        mainLayout.setCenter(centerWrapper);

        root = mainLayout;
        initGameLoop();
    }

    private VBox createHudSidebar() {
        VBox hud = new VBox(14);
        hud.setPrefWidth(300);
        hud.setPadding(new Insets(25, 20, 25, 25));
        hud.setStyle("-fx-background-color: #161722; -fx-border-color: #232536; -fx-border-width: 0 1 0 0;");

        // Tiêu đề game
        Label titleLabel = new Label("BOMBERMAN");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        titleLabel.setTextFill(Color.rgb(255, 190, 40));

        Label subtitleLabel = new Label("3 PLAYERS BATTLE");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        subtitleLabel.setTextFill(Color.rgb(160, 165, 185));

        VBox titleBox = new VBox(2, titleLabel, subtitleLabel);
        titleBox.setPadding(new Insets(0, 0, 10, 0));

        // Thẻ Player 1 (Blue)
        VBox p1Card = createPlayerCard("Player 1", "W, S, A, D", "SPACE", Color.rgb(30, 144, 255), "#1c2738");

        // Thẻ Player 2 (Red)
        VBox p2Card = createPlayerCard("Player 2", "↑, ↓, ←, →", "ENTER", Color.rgb(235, 59, 90), "#381c24");

        // Thẻ Player 3 (Green)
        VBox p3Card = createPlayerCard("Player 3", "I, K, J, L", "O", Color.rgb(38, 222, 129), "#1c3826");

        // Phím tắt phụ
        VBox tipsCard = new VBox(6);
        tipsCard.setPadding(new Insets(10, 12, 10, 12));
        tipsCard.setStyle("-fx-background-color: #202230; -fx-background-radius: 8;");

        Label tip1 = new Label("[R] : Đặt lại vị trí (Reset)");
        tip1.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        tip1.setTextFill(Color.rgb(180, 185, 205));

        Label tip2 = new Label("[ESC] : Quay về Menu chính");
        tip2.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 11));
        tip2.setTextFill(Color.rgb(180, 185, 205));

        tipsCard.getChildren().addAll(tip1, tip2);

        hud.getChildren().addAll(titleBox, p1Card, p2Card, p3Card, tipsCard);
        return hud;
    }

    private VBox createPlayerCard(String name, String moveKeys, String bombKey, Color color, String bgHex) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle(
            "-fx-background-color: " + bgHex + "; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: rgba(255,255,255,0.08); " +
            "-fx-border-radius: 8; -fx-border-width: 1;"
        );

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setTextFill(color);

        Label moveLabel = new Label("Di chuyển: " + moveKeys);
        moveLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        moveLabel.setTextFill(Color.rgb(215, 220, 235));

        Label bombLabel = new Label("Đặt bom:   [" + bombKey + "]");
        bombLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        bombLabel.setTextFill(Color.rgb(255, 215, 0));

        card.getChildren().addAll(nameLabel, moveLabel, bombLabel);
        return card;
    }

    private void initGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameMap.update();
                gameMap.render();
            }
        };
    }

    public void startGameLoop() {
        if (!isRunning) {
            gameLoop.start();
            isRunning = true;
        }
    }

    public void stopGameLoop() {
        if (isRunning) {
            gameLoop.stop();
            isRunning = false;
        }
    }

    public void handleKeyPressed(KeyCode code) {
        gameMap.handleKeyPressed(code);
    }

    public void handleKeyReleased(KeyCode code) {
        gameMap.handleKeyReleased(code);
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public static Gameplay getInstance() {
        if (instance == null) {
            instance = new Gameplay();
        }
        return instance;
    }
}
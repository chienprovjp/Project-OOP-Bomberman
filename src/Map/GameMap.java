package src.Map;

import java.net.URL;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameMap extends Canvas {

    public static final int TILE_SIZE = 32;

    private final GraphicsContext gc;
    private Image grassImage;
    private Image wallImage;
    private Image brickImage;

    public GameMap() {
        super(
            MapData.COLS * TILE_SIZE,
            MapData.ROWS * TILE_SIZE
        );

        gc = getGraphicsContext2D();
        gc.setImageSmoothing(false);

        loadLevelImages(MapData.getCurrentLevel());

        drawMap();
    }

    public void reload() {
        setWidth(MapData.COLS * TILE_SIZE);
        setHeight(MapData.ROWS * TILE_SIZE);
        loadLevelImages(MapData.getCurrentLevel());
        drawMap();
    }

    private void loadLevelImages(int level) {
        grassImage = loadImage("/assets/images/grass" + level + ".png");
        wallImage = loadImage("/assets/images/wall" + level + ".png");
        brickImage = loadImage("/assets/images/brick" + level + ".png");
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);

        if (url == null) {
            throw new IllegalArgumentException(
                "Khong tim thay hinh anh: " + path
            );
        }

        Image image = new Image(url.toExternalForm());

        if (image.isError()) {
            throw new IllegalArgumentException(
                "Khong the doc hinh anh: " + path,
                image.getException()
            );
        }

        return image;
    }

    public void drawMap() {
        gc.clearRect(0, 0, getWidth(), getHeight());

        for (int row = 0; row < MapData.ROWS; row++) {
            for (int col = 0; col < MapData.COLS; col++) {
                int tile = MapData.getTile(row, col);
                Image image;

                if (tile == MapData.GRASS) {
                    image = grassImage;
                } else if (tile == MapData.WALL) {
                    image = wallImage;
                } else {
                    image = brickImage;
                }

                gc.drawImage(
                    image,
                    col * TILE_SIZE,
                    row * TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE
                );
            }
        }
    }

    public void updateMap() {
        drawMap();
    }
}
package src.Screen;

import javafx.scene.layout.*;

public abstract class Screen {
    protected Pane root;

    protected Screen() {
        root = new Pane();
    }
    public Pane getRoot() {
        return root;
    }


}

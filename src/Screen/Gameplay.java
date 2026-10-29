package src.Screen;

public class Gameplay extends Screen {
    private static Gameplay instance;

    private Gameplay() {
        super();
    }

    public static Gameplay getInstance() {
        if (instance == null) {
            instance = new Gameplay();
        }
        return instance;
    }
}


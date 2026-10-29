package src.Main;

public class StateMachine {
    public enum State {
        MAIN_MENU,
        GAMEPLAY,
        PAUSE_MENU,
        GAME_OVER
    }

    private static StateMachine instance;
    private static State currentState;

    private StateMachine() {
        currentState = State.MAIN_MENU;
    }

    public static StateMachine getInstance() {
        if (instance == null) {
            instance = new StateMachine();
        }
        return instance;
    }

    public State getCurrentState() {
        return currentState;
    }
    
    public void setState(State state) {
        currentState = state;
    }
}
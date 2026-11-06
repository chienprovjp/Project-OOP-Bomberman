package bomberman.util;

public class GameSaveData {
    private int level;
    private int score;
    private int lives;
    private int highScore;
    private boolean hasOngoingGame;

    public GameSaveData(int level, int score, int lives, int highScore) {
        this(level, score, lives, highScore, false);
    }

    public GameSaveData(int level, int score, int lives, int highScore, boolean hasOngoingGame) {
        this.level = level;
        this.score = score;
        this.lives = lives;
        this.highScore = highScore;
        this.hasOngoingGame = hasOngoingGame;
    }

    // Getters
    public int getLevel() { return level; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getHighScore() { return highScore; }
    public boolean hasOngoingGame() { return hasOngoingGame; }
}

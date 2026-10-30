package bomberman.data;

public class GameSaveData {
    private int level;
    private int score;
    private int lives;
    private int highScore;

    public GameSaveData(int level, int score, int lives, int highScore) {
        this.level = level;
        this.score = score;
        this.lives = lives;
        this.highScore = highScore;
    }

    // Getters và Setters
    public int getLevel() { return level; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getHighScore() { return highScore; }
}
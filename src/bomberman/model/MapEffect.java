package bomberman.model;
import bomberman.util.ResourceLoader;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapEffect {
    private BufferedImage particleimage;
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private int PARTICLE_COUNT = 4 + random.nextInt(5);;
    public void loadThemeForLevel(int level) {
        int theme = 1;
        if (level >= 1 && level <= 3) {
                theme = 1;
        } else if (level >= 4 && level <= 6) {
                theme = 2;
        } else if (level >= 7 && level <= 9) {
                theme = 3;
        }
        this.particleimage = ResourceLoader.loadImage("particle" + theme + ".png");
    }
    private static class Particle {
    double x;
    double y;
    double speedX;
    double speedY;
    float size;
    

    public Particle(double x, double y, double speedX, double speedY,
        float size) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.size = size;
        }
    }
    public void initializeParticles(int width, int height) {
    particles.clear();

    for (int i = 0; i < PARTICLE_COUNT; i++) {
        double x = random.nextDouble() * width;
        double y = random.nextDouble() * height;
        double speedX = (random.nextDouble() - 0.5) * 2;
        double speedY = 0.5 + random.nextDouble() * 1.5;
        float size = 8 + random.nextFloat() * 8;

        particles.add(new Particle(x, y, speedX, speedY, size));
        }
    }
    public void updateParticles(int width, int height) {
    for (Particle particle : particles) {
        particle.x += particle.speedX;
        particle.y += particle.speedY;

        if (particle.y > height) {
            particle.y = -particle.size;
            particle.x = random.nextDouble() * width;
        }

        if (particle.x > width) {
            particle.x = -particle.size;
        } else if (particle.x < -particle.size) {
            particle.x = width;
        }
    }
}
    public void drawParticles(Graphics2D g2d) {
    for (Particle particle : particles) {
        g2d.drawImage(particleimage, (int) particle.x, (int) particle.y,
            (int) particle.size, (int) particle.size, null);
        }
    }

}

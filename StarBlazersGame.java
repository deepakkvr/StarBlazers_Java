import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class StarBlazersGame extends JFrame implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int SPACESHIP_WIDTH = 60;
    private static final int SPACESHIP_HEIGHT = 40;
    private static final int ENEMY_WIDTH = 30;
    private static final int ENEMY_HEIGHT = 30;
    private static final int BULLET_WIDTH = 5;
    private static final int BULLET_HEIGHT = 10;

    private int spaceshipX = WIDTH / 2 - SPACESHIP_WIDTH / 2;
    private final int spaceshipY = HEIGHT - 50;
    private final int spaceshipSpeed = 20;

    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();

    private int score = 0;
    private int level = 1;
    private int enemiesToDestroy = 5;
    private int lives = 3;

    private boolean inGame = false;
    private boolean isPaused = false;

    private StartMenu startMenu;
    private PauseMenu pauseMenu;

    public StarBlazersGame() {
        setSize(WIDTH, HEIGHT);
        setTitle("StarBlazers Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);

        Timer timer = new Timer(20, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        startMenu = new StartMenu();
        startMenu.setVisible(true);
        add(startMenu);

        pauseMenu = new PauseMenu();
        pauseMenu.setVisible(false);
        add(pauseMenu);

        initializeEnemies();
    }

    private void initializeEnemies() {
        Random random = new Random();
        for (int i = 0; i < enemiesToDestroy; i++) {
            int x = random.nextInt(WIDTH - ENEMY_WIDTH);
            int y = random.nextInt(200);
            enemies.add(new Enemy(x, y));
        }
    }

    private void toggleStartMenu() {
        startMenu.setVisible(!startMenu.isVisible());
        inGame = true;
    }

    private void drawStartMenu(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("StarBlazers Game", WIDTH / 2 - 200, HEIGHT / 2 - 60);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press S to start", WIDTH / 2 - 100, HEIGHT / 2);
        g.drawString("Press Q to quit", WIDTH / 2 - 100, HEIGHT / 2 + 30);
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
        }

        repaint();
    }

    private void drawPauseMenu(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Game Paused", WIDTH / 2 - 120, HEIGHT / 2 - 30);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, WIDTH / 2 - 100, HEIGHT / 2 + 20);
        g.drawString("Press R to restart", WIDTH / 2 - 100, HEIGHT / 2 + 50);
        g.drawString("Press Q to quit", WIDTH / 2 - 100, HEIGHT / 2 + 80);
        g.drawString("Press E to resume", WIDTH / 2 - 120, HEIGHT / 2 + 110);
    }

    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        if (startMenu.isVisible()) {
            drawStartMenu(g);
        } else if (isPaused) {
            drawPauseMenu(g);
        } else if (inGame) {
            g.setColor(Color.white);
            g.fillRect(spaceshipX, spaceshipY, SPACESHIP_WIDTH, SPACESHIP_HEIGHT);

            for (Enemy enemy : enemies) {
                enemy.draw(g); // Draw enemies with the customized appearance
            }

            for (Bullet bullet : bullets) {
                g.setColor(Color.blue);
                g.fillRect(bullet.getX(), bullet.getY(), BULLET_WIDTH, BULLET_HEIGHT);
            }

            g.setColor(Color.white);
            g.drawString("Score: " + score, 10, 20);
            g.drawString("Level: " + level, 10, 40);
            g.drawString("Lives: " + lives, 10, 60);
            g.drawString("Enemies Left: " + enemies.size(), 10, 80);
        } else {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over", WIDTH / 2 - 100, HEIGHT / 2 - 30);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Final Score: " + score, WIDTH / 2 - 100, HEIGHT / 2 + 20);
            g.drawString("Press R to restart", WIDTH / 2 - 100, HEIGHT / 2 + 50);
            g.drawString("Press Q to quit", WIDTH / 2 - 100, HEIGHT / 2 + 80);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (inGame && !isPaused) {
            moveSpaceship();
            moveEnemies();
            moveBullets();
            checkCollisions();
            checkLevelCompletion();
        }

        repaint();
    }

    private void moveSpaceship() {
        if (spaceshipX < 0) {
            spaceshipX = 0;
        } else if (spaceshipX > WIDTH - SPACESHIP_WIDTH) {
            spaceshipX = WIDTH - SPACESHIP_WIDTH;
        }
    }

    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            enemy.move();
        }
    }

    private void moveBullets() {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.move();
            if (bullet.getY() < 0) {
                iterator.remove();
            }
        }
    }

    private void checkCollisions() {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            for (Enemy enemy : new ArrayList<>(enemies)) {
                if (bullet.intersects(enemy)) {
                    bullets.remove(bullet);
                    enemies.remove(enemy);
                    score += 10;
                }
            }
        }

        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (spaceshipIntersects(enemy)) {
                lives--;
                if (lives <= 0) {
                    inGame = false;
                } else {
                    resetGame();
                }
            }
        }
    }

    private void checkLevelCompletion() {
        if (enemies.isEmpty()) {
            level++;
            enemiesToDestroy += 2;
            initializeEnemies();
        }
    }

    private boolean spaceshipIntersects(Enemy enemy) {
        return spaceshipX < enemy.getX() + ENEMY_WIDTH &&
                spaceshipX + SPACESHIP_WIDTH > enemy.getX() &&
                spaceshipY < enemy.getY() + ENEMY_HEIGHT &&
                spaceshipY + SPACESHIP_HEIGHT > enemy.getY();
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (startMenu.isVisible()) {
            if (key == KeyEvent.VK_S) {
                toggleStartMenu(); // Start the game
            } else if (key == KeyEvent.VK_Q) {
                System.exit(0);
            }
        } else if (inGame && !isPaused) {
            if (key == KeyEvent.VK_LEFT) {
                spaceshipX -= spaceshipSpeed;
            } else if (key == KeyEvent.VK_RIGHT) {
                spaceshipX += spaceshipSpeed;
            } else if (key == KeyEvent.VK_SPACE) {
                bullets.add(new Bullet(spaceshipX + SPACESHIP_WIDTH / 2 - BULLET_WIDTH / 2, spaceshipY));
            } else if (key == KeyEvent.VK_ESCAPE) {
                togglePause();
            }
        } else {
            if (key == KeyEvent.VK_R) {

            } else if (key == KeyEvent.VK_Q) {
                System.exit(0);
            } else if (key == KeyEvent.VK_E && isPaused) {
                togglePause(); // Resume the game
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used in this example
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Handle key released events if needed
    }

    private void resetGame() {
        spaceshipX = WIDTH / 2 - SPACESHIP_WIDTH / 2;
        enemies.clear();
        bullets.clear();
        initializeEnemies();
    }

    private void restartGame() {
        inGame = true;
        level = 1;
        score = 0;
        lives = 3;
        resetGame();
        togglePause();
    }

    private class Enemy {
        private int x;
        private int y;
        private int speed = 2;

        public Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void move() {
            y += speed;
            if (y > HEIGHT) {
                y = 0;
                x = new Random().nextInt(WIDTH - ENEMY_WIDTH);
            }
        }

        public void draw(Graphics g) {
            // Customize the appearance of the enemies
            // Example: Drawing an oval-shaped alien with antennas
            
            // Alien body
            g.setColor(Color.green); // Change color as desired
            g.fillOval(x, y, ENEMY_WIDTH, ENEMY_HEIGHT);

            // Antennas
            g.setColor(Color.yellow); // Change color as desired
            g.fillRect(x + ENEMY_WIDTH / 2 - 1, y - 5, 2, 5);
            g.fillRect(x + ENEMY_WIDTH / 2 - 5, y - 10, 10, 5);
        }
    }

    private class Bullet {
        private int x;
        private int y;
        private int speed = 5;

        public Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void move() {
            y -= speed;
        }

        public boolean intersects(Enemy enemy) {
            return x < enemy.getX() + ENEMY_WIDTH &&
                    x + BULLET_WIDTH > enemy.getX() &&
                    y < enemy.getY() + ENEMY_HEIGHT &&
                    y + BULLET_HEIGHT > enemy.getY();
        }
    }

    private class StartMenu extends JPanel {
        public StartMenu() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
    }

    private class PauseMenu extends JPanel {
        private JButton resumeButton;
        private JButton restartButton;
        private JButton quitButton;

        public PauseMenu() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            resumeButton = new JButton("Resume");
            restartButton = new JButton("Restart");
            quitButton = new JButton("Quit");

            resumeButton.addActionListener(e -> {
                togglePause();
            });

            restartButton.addActionListener(e -> {
                restartGame();
            });

            quitButton.addActionListener(e -> {
                System.exit(0);
            });

            add(resumeButton);
            add(restartButton);
            add(quitButton);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StarBlazersGame game = new StarBlazersGame();
            game.setVisible(true);
        });
    }
}

package src;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


public class PacMan extends JPanel implements ActionListener {
  private final String[] tileMap = { 

            "XXXXXXXXXXXXXXXXXXX", 

            "X        X        X", 

            "X XX XXX X XXX XXbX", 

            "X                 X", 

            "X XX X XXXXX X XX X", 

            "X    X       X    X", 

            "XXXX XXXX XXXX XXXX", 

            "OOOX X       X XOOO", 

            "XXXX X XXrXX X XXXX", 

            "O        o        O", 

            "XXXX X XXXXX X XXXX", 

            "OOOX X       X XOOO", 

            "XXXX X XXXXX X XXXX", 

            "X        X        X", 

            "X XX XXX X XXX XX X", 

            "X  X     P     X  X", 

            "XX X X XXXXX X X XX", 

            "X    X   X   X    X", 

            "X XXXXXX X XXXXXX X", 

            "X         p       X", 

            "XXXXXXXXXXXXXXXXXXX" 

    }; 

private final List<GhostInfo> previousGhosts = new ArrayList<>(); 

private final int rowCount = tileMap.length; 

private final int columnCount = tileMap[0].length(); 

private final int tileSize = 32; 

private final int boardWidth = columnCount * tileSize; 

private final int boardHeight = rowCount * tileSize;

private final List<Block> walls = new ArrayList<>(); 
private final List<Block> foods = new ArrayList<>(); 

private final List<Ghost> ghosts = new ArrayList<>(); 
private final Timer gameLoop;
            
private enum GameState { MENU, PLAYING, PAUSED, GAMEOVER }
private GameState gameState = GameState.MENU;
private final JPanel menuPanel = new JPanel();
private final JButton startBtn = new JButton("Start (New Game)");
private final JButton continueBtn = new JButton("Continue");
private final JButton exitBtn = new JButton("Exit");

private Cherry cherry = null;
private long cherrySpawnTime = 0;
private int cherriesEatenThisLife = 0;
private boolean ghostsScared = false;
private long scaredStartTime = 0;
private Image cherryImage; // Load in loadImages()

private PacmanBlock pacman; 

    private static final int TILE_SIZE = 32;
    private static final int SPEED = TILE_SIZE / 6;

cherryImage = new ImageIcon(getClass().getResource("/Resource/cherry.png")).getImage();

    private PacmanBlock pacman;

    private void loadMap() { 

    walls.clear(); 

    foods.clear(); 

    ghosts.clear(); 

    pacman = null; 

 

    for (int r = 0; r < rowCount; r++) { 

        for (int c = 0; c < columnCount; c++) { 

            char ch = tileMap[r].charAt(c); 

            int x = c * tileSize; 

            int y = r * tileSize; 

 

            if (ch == 'X') { 

                walls.add(new Block(wallImage, x, y, tileSize, tileSize)); 

            } 

            else if (ch == 'P') { 

                pacman = new PacmanBlock(pacmanRightImage, x, y, tileSize, tileSize); 

            } 

            else if (ch == ' ') { 

                foods.add(new Block(null, 

                        x + tileSize / 2 - 2, 

                        y + tileSize / 2 - 2, 

                        4, 4)); 

            } 

           else if (ch == 'b' || ch == 'o' || ch == 'p' || ch == 'r') { 

                    Image img = blueGhostImage; 

                    String type = "blue"; 

                    if (ch == 'o') { 

                        img = orangeGhostImage; 

                        type = "orange"; 

                    } else if (ch == 'p') { 

                        img = pinkGhostImage; 

                        type = "pink"; 

                    } else if (ch == 'r') { 

                        img = redGhostImage; 

                        type = "red"; 

                    } 

                    Ghost g = new Ghost(img, x, y, tileSize, tileSize, type, x, y); 

                    ghosts.add(g); 

 

                    previousGhosts.add(new GhostInfo(type, x, y)); 

                } 

        } 

    } 

} 
@Override
public void actionPerformed(ActionEvent e) {
    if (gameState == GameState.PLAYING) {
        step();
        repaint();
    }
}

private void step() {
        if (pacman != null) pacman.move();

        if (pacman != null) {
            if (pacman.x < -pacman.width) pacman.x = boardWidth;
            else if (pacman.x > boardWidth) pacman.x = -pacman.width;
        }

        for (Ghost g : ghosts) g.move();

        for (Ghost g : ghosts) {
            if (g.x < 0) {
                g.x = 0;
                g.reverseDirection();
            }
            if (g.x + g.width > boardWidth) {
                g.x = boardWidth - g.width;
                g.reverseDirection();
            }
        }

        Iterator<Block> fit = foods.iterator();
        while (fit.hasNext()) {
            Block f = fit.next();
            if (collision(pacman, f)) {
                fit.remove();
                score += 10;
            }
        }

        spawnCherryIfNeeded();
        if (cherry != null && collision(pacman, cherry)) {
            cherry = null;
            cherriesEatenThisLife++;
            ghostsScared = true;
            scaredStartTime = System.currentTimeMillis();
            for (Ghost g : ghosts) g.setScared(true);
        }

        if (ghostsScared) {
            long now = System.currentTimeMillis();
            List<Ghost> eatenThisFrame = new ArrayList<>();
            for (Ghost g : ghosts) {
                if (collision(pacman, g)) {
                    eatenThisFrame.add(g);
                    score += 200;
                    removedGhostStarts.add(new Point(g.startX, g.startY));
                }
            }
            if (!eatenThisFrame.isEmpty()) ghosts.removeAll(eatenThisFrame);

            if (now - scaredStartTime >= 15000) {
                ghostsScared = false;
                for (Ghost g : ghosts) g.setScared(false);
            }
        } else {
            for (Ghost g : ghosts) {
                if (collision(pacman, g)) {
                    lives--;
                    if (lives <= 0) {
                        triggerGameOver();
                        return;
                    } else {
                        restoreAllGhostsAfterLifeLoss();
                        resetPositions();
                        return;
                    }
                }
            }
        }

        if (foods.isEmpty()) {
            restoreAllGhostsAfterLevel();
            loadMap();
            resetPositions();
        }
    }

private void spawnCherryIfNeeded() {
    long now = System.currentTimeMillis();
    if (cherry == null && cherriesEatenThisLife >= 2) {
        if (cherriesEatenThisLife == 2) placeNewCherry();
        cherrySpawnTime = now;
    } else if (now - cherrySpawnTime > 20000) {
        placeNewCherry();
        cherrySpawnTime = now;
    }
}

private void placeNewCherry() {
    // Random safe position logic (check !wall, !pacman, !ghost collision)
    // cherry = new Cherry(cherryImage, x, y, tileSize, tileSize);
}

    private class PacmanBlock {
        Image image;
        int x, y, width, height;
        char direction = 'R';

        PacmanBlock(Image img, int x, int y, int w, int h) {
            this.image = img;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        void setDirection(char d) {
            direction = d;
        }

        void setImage(Image img) {
            image = img;
        }

        Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        void move() {
            int px = x, py = y;
            if (direction == 'U')
                y -= SPEED;
            else if (direction == 'D')
                y += SPEED;
            else if (direction == 'L')
                x -= SPEED;
            else if (direction == 'R')
                x += SPEED;
        }

        private boolean softCollision(Block a, Block b) {
            Rectangle r1 = a.getBounds();
            Rectangle r2 = b.getBounds();
            r2.grow(-3, -3);
            return r1.intersects(r2);
        }

        private void setupKeyBindings() {
            InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke("released UP"), "up");
            am.put("up", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gameState == GameState.PLAYING && pacman != null) {
                        pacman.setDirection('U');
                        pacman.setImage(pacmanUpImage);
                    }
                }
            });

            im.put(KeyStroke.getKeyStroke("released DOWN"), "down");
            am.put("down", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gameState == GameState.PLAYING && pacman != null) {
                        pacman.setDirection('D');
                        pacman.setImage(pacmanDownImage);
                    }
                }
            });

            im.put(KeyStroke.getKeyStroke("released LEFT"), "left");
            am.put("left", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gameState == GameState.PLAYING && pacman != null) {
                        pacman.setDirection('L');
                        pacman.setImage(pacmanLeftImage);
                    }
                }
            });

            im.put(KeyStroke.getKeyStroke("released RIGHT"), "right");
            am.put("right", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gameState == GameState.PLAYING && pacman != null) {
                        pacman.setDirection('R');
                        pacman.setImage(pacmanRightImage);
                    }
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "esc");
            am.put("esc", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gameState == GameState.PLAYING)
                        pauseGame();
                    else if (gameState == GameState.PAUSED)
                        resumeGame();
                    else if (gameState == GameState.MENU)
                        System.exit(0);
                    else if (gameState == GameState.GAMEOVER)
                        goToMenu();
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "enter");
            am.put("enter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (gameState == GameState.MENU || gameState == GameState.GAMEOVER)
                        startNewGame();
                    else if (gameState == GameState.PAUSED)
                        resumeGame();
                }
            });
        }
    }
    private class Ghost extends Block { 

 

    int velX = 0; 

    int velY = 0; 

    String type; 

 

    Ghost(Image img, int x, int y, int w, int h, String type) { 

        super(img, x, y, w, h); 

        this.type = type; 

        setRandomDirection(); 

    } 

 

    void setRandomDirection() { 

        int r = random.nextInt(4); 

        int s = tileSize / 8; 

 

        if (r == 0) { velX = 0; velY = -s; } 

        else if (r == 1) { velX = 0; velY = s; } 

        else if (r == 2) { velX = -s; velY = 0; } 

        else { velX = s; velY = 0; } 

    } 

 

    void reverseDirection() { 

        velX = -velX; 

        velY = -velY; 

    } 

 

    void move() { 

        int gx = x; 

        int gy = y; 

 

        x += velX; 

        y += velY; 

 

        for (Block w : walls) { 

            if (collision(this, w)) { 

                x = gx; 

                y = gy; 

               reverseDirection(); 

                break; 

            } 

        } 

    } 

} 
    private static class GhostInfo { 

        String type; 

        int startX, startY; 

        GhostInfo(String type, int startX, int startY) { 

            this.type = type; 

            this.startX = startX; 

            this.startY = startY; 

        } 

    } 
private void restoreAllGhostsAfterLifeLoss() { 

        ghosts.clear(); 

        for (GhostInfo info : previousGhosts) { 

            Image img = blueGhostImage; 

            if ("orange".equals(info.type)) img = orangeGhostImage; 

            else if ("pink".equals(info.type)) img = pinkGhostImage; 

            else if ("red".equals(info.type)) img = redGhostImage; 

            Ghost g = new Ghost(img, info.startX, info.startY, tileSize, tileSize, info.type, info.startX, info.startY); 

            g.setRandomDirection(); 

            ghosts.add(g); 

        } 

        removedGhostStarts.clear(); 

        ghostsScared = false; 

    } 

 

    private void restoreAllGhostsAfterLevel() { 

        restoreAllGhostsAfterLifeLoss(); 

    } 

 

    private void resetPositions() { 

        for (int r = 0; r < rowCount; r++) { 

            String row = tileMap[r]; 

            for (int c = 0; c < columnCount; c++) { 

                if (row.charAt(c) == 'P') { 

                    int sx = c * tileSize; 

                    int sy = r * tileSize; 

                    if (pacman != null) { 

                        pacman.x = sx; 

                        pacman.y = sy; 

                        pacman.setDirection('R'); 

                        pacman.setImage(pacmanRightImage); 

                    } 

                    return; 

                } 
@Override
protected void paintComponent(Graphics gg) {
    super.paintComponent(gg);
    Graphics2D g = (Graphics2D) gg;

    // Walls
    for (Block w : walls) {
        if (w.image != null)
            g.drawImage(w.image, w.x, w.y, w.width, w.height, null);
        else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(w.x, w.y, w.width, w.height);
        }
    }

    // Foods
    g.setColor(Color.WHITE);
    for (Block f : foods)
        g.fillRect(f.x, f.y, f.width, f.height);

    // Cherry
    if (cherry != null && cherry.image != null) {
                g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
    }

    // Ghosts
    for (Ghost gr : ghosts) {
        if (gr.image != null)
            g.drawImage(gr.image, gr.x, gr.y, gr.width, gr.height, null);
        else {
            g.setColor(Color.MAGENTA);
            g.fillRect(gr.x, gr.y, gr.width, gr.height);
        }
    }

    // Pacman
    if (pacman != null && pacman.image != null) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
    }

    // Score & Lives
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.PLAIN, 18));
    g.drawString("Score: " + score, tileSize / 2, tileSize / 2);

    String livesText = "Lives: " + lives;
    int w = g.getFontMetrics().stringWidth(livesText);
    g.drawString(livesText, boardWidth - w - 8, tileSize / 2);
    // Draw PAUSED title above menu panel with space
        if (gameState == GameState.PAUSED) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String s = "PAUSED";
            int sw = g.getFontMetrics().stringWidth(s);
            g.drawString(s, (boardWidth - sw) / 2, (boardHeight - 250) / 2); // draw it 30 pixels above menu panel Y
        } else if (gameState == GameState.GAMEOVER) {
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String s = "GAME OVER";
            int sw = g.getFontMetrics().stringWidth(s);
            g.drawString(s, (boardWidth - sw) / 2, (boardHeight - 250) / 2);
        }


}




            } 

        } 

    } 
}

package src;


import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PacMan extends JPanel implements ActionListener {

    // ------------------- Game state -------------------
    private enum GameState { MENU, PLAYING, PAUSED, GAMEOVER }
    private GameState gameState = GameState.MENU;

    // ------------------- Map and tile config -------------------
    private final String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XXbX",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O        o        O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X         p       X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    private final int rowCount = tileMap.length;
    private final int columnCount = tileMap[0].length();
    private final int tileSize = 32;
    private final int boardWidth = columnCount * tileSize;
    private final int boardHeight = rowCount * tileSize;

    // ------------------- Images -------------------
    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;
    private Image cherryImage;
    private Image scaredGhostImage;

    // ------------------- Game objects -------------------
    private final List<Block> walls = new ArrayList<>();
    private final List<Block> foods = new ArrayList<>();
    private final List<Ghost> ghosts = new ArrayList<>();
    private final List<GhostInfo> previousGhosts = new ArrayList<>();
    private final List<Point> removedGhostStarts = new ArrayList<>();
    private PacmanBlock pacman;
    private Cherry cherry = null;

    // Cherry & scared state
    private long cherrySpawnTime = 0;
    private int cherriesEatenThisLife = 0;
    private boolean ghostsScared = false;
    private long scaredStartTime = 0;

    // Score / lives
    private int score = 0;
    private int lives = 3;

    // Game loop
    private final Timer gameLoop;

    // Menu UI
    private final JPanel menuPanel = new JPanel();
    private final JButton startBtn = new JButton("Start (New Game)");
    private final JButton continueBtn = new JButton("Continue");
    private final JButton exitBtn = new JButton("Exit");

    private final Random random = new Random();

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setLayout(null);

        loadImages();
        buildMenu();
        loadMap();
        gameLoop = new Timer(50, this);

        setupKeyBindings();
    }

    private void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("/src/Resource/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/src/Resource/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/src/Resource/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/src/Resource/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/src/Resource/redGhost.png")).getImage();
        pacmanUpImage = new ImageIcon(getClass().getResource("/src/Resource/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/src/Resource/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/src/Resource/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/src/Resource/pacmanRight.png")).getImage();
        cherryImage = new ImageIcon(getClass().getResource("/src/Resource/cherry.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("/src/Resource/scaredGhost.png")).getImage();
    }

    private void buildMenu() {
        menuPanel.setLayout(new GridBagLayout());
        menuPanel.setOpaque(false);
        int panelW = 360, panelH = 250;

        // Shift down panel Y by 30px to create space below PAUSED title
        menuPanel.setBounds((boardWidth - panelW) / 2, (boardHeight - panelH) / 2 + 30, panelW, panelH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("PAC-MAN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        menuPanel.add(title, gbc);

        startBtn.setFocusable(false);
        continueBtn.setFocusable(false);
        exitBtn.setFocusable(false);

        gbc.gridy = 1;
        menuPanel.add(startBtn, gbc);
        gbc.gridy = 2;
        menuPanel.add(continueBtn, gbc);
        gbc.gridy = 3;
        menuPanel.add(exitBtn, gbc);

        startBtn.addActionListener(e -> startNewGame());
        continueBtn.addActionListener(e -> resumeGame());
        exitBtn.addActionListener(e -> System.exit(0));

        add(menuPanel);
        updateMenuVisibility();
    }

    private void updateMenuVisibility() {
        switch (gameState) {
            case MENU:
                menuPanel.setVisible(true);
                continueBtn.setVisible(false);
                startBtn.setText("Start (New Game)");
                break;
            case PAUSED:
                menuPanel.setVisible(true);
                continueBtn.setVisible(true);
                startBtn.setText("Restart (New Game)");
                break;
            case GAMEOVER:
                menuPanel.setVisible(true);
                continueBtn.setVisible(false);
                startBtn.setText("Restart (New Game)");
                break;
            case PLAYING:
                menuPanel.setVisible(false);
                break;
        }
        repaint();
    }

    private void loadMap() {
        walls.clear();
        foods.clear();
        ghosts.clear();
        previousGhosts.clear();
        removedGhostStarts.clear();
        pacman = null;
        cherry = null;
        cherriesEatenThisLife = 0;
        ghostsScared = false;

        for (int r = 0; r < rowCount; r++) {
            String row = tileMap[r];
            for (int c = 0; c < columnCount; c++) {
                char ch = row.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;
                if (ch == 'X') {
                    walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                } else if (ch == 'P') {
                    pacman = new PacmanBlock(pacmanRightImage, x, y, tileSize, tileSize);
                } else if (ch == 'b' || ch == 'o' || ch == 'p' || ch == 'r') {
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
                } else if (ch == ' ') {
                    foods.add(new Block(null, x + tileSize / 2 - 2, y + tileSize / 2 - 2, 4, 4));
                }
            }
        }

        for (Ghost g : ghosts) g.setRandomDirection();
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
                if (gameState == GameState.PLAYING) pauseGame();
                else if (gameState == GameState.PAUSED) resumeGame();
                else if (gameState == GameState.MENU) System.exit(0);
                else if (gameState == GameState.GAMEOVER) goToMenu();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "enter");
        am.put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameState == GameState.MENU || gameState == GameState.GAMEOVER) startNewGame();
                else if (gameState == GameState.PAUSED) resumeGame();
            }
        });
    }

    private void startNewGame() {
        loadMap();
        score = 0;
        lives = 3;
        cherriesEatenThisLife = 0;
        cherry = null;
        ghostsScared = false;
        gameState = GameState.PLAYING;
        updateMenuVisibility();
        gameLoop.start();
        requestFocusInWindow();
    }

    private void resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            menuPanel.setVisible(false);
            gameLoop.start();
            requestFocusInWindow();
        }
    }

    private void pauseGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            gameLoop.stop();
            updateMenuVisibility();
        }
    }

    private void goToMenu() {
        gameState = GameState.MENU;
        gameLoop.stop();
        updateMenuVisibility();
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

    private void triggerGameOver() {
        gameState = GameState.GAMEOVER;
        gameLoop.stop();
        updateMenuVisibility();
    }

    private void spawnCherryIfNeeded() {
        long now = System.currentTimeMillis();
        if (cherry == null && cherriesEatenThisLife < 2) {
            if (cherriesEatenThisLife == 0) {
                placeNewCherry();
                cherrySpawnTime = now;
            } else {
                if (now - cherrySpawnTime >= 20000) {
                    placeNewCherry();
                    cherrySpawnTime = now;
                }
            }
        }
    }

    private void placeNewCherry() {
        int attempts = 0;
        while (attempts++ < 1000) {
            int r = random.nextInt(rowCount);
            int c = random.nextInt(columnCount);
            char mapch = tileMap[r].charAt(c);
            if (mapch == 'X') continue;
            int x = c * tileSize;
            int y = r * tileSize;
            Rectangle candidate = new Rectangle(x, y, tileSize, tileSize);
            boolean conflict = false;
            if (pacman != null && candidate.intersects(pacman.getBounds())) conflict = true;
            for (Ghost g : ghosts) if (candidate.intersects(g.getBounds())) { conflict = true; break; }
            if (conflict) continue;
            cherry = new Cherry(cherryImage, x, y, tileSize, tileSize);
            return;
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
            }
        }
    }

    private boolean collision(Block a, Block b) {
        if (a == null || b == null) return false;
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;

        for (Block w : walls) {
            if (w.image != null) g.drawImage(w.image, w.x, w.y, w.width, w.height, null);
            else { g.setColor(Color.DARK_GRAY); g.fillRect(w.x, w.y, w.width, w.height); }
        }

        g.setColor(Color.WHITE);
        for (Block f : foods) g.fillRect(f.x, f.y, f.width, f.height);

        if (cherry != null && cherry.image != null) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }

        for (Ghost gr : ghosts) {
            if (gr.image != null) g.drawImage(gr.image, gr.x, gr.y, gr.width, gr.height, null);
            else { g.setColor(Color.MAGENTA); g.fillRect(gr.x, gr.y, gr.width, gr.height); }
        }

        if (pacman != null && pacman.image != null) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }

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

    // ------------------- Inner classes -------------------
    private class Block {
        Image image;
        int x, y, width, height;
        Block(Image image, int x, int y, int width, int height) {
            this.image = image; this.x = x; this.y = y; this.width = width; this.height = height;
        }
        Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    }

    private class PacmanBlock extends Block {
        char direction = 'R';
        int speed = tileSize / 6;
        private static final int MOVE_TOLERANCE = 3;

        PacmanBlock(Image image, int x, int y, int w, int h) {
            super(image, x, y, w, h);
        }

        void setDirection(char d) { direction = d; }
        void setImage(Image img) { image = img; }
        Rectangle getBounds() { return new Rectangle(x, y, width, height); }

        void move() {
            int px = x, py = y;
            if (direction == 'U') y -= speed;
            else if (direction == 'D') y += speed;
            else if (direction == 'L') x -= speed;
            else if (direction == 'R') x += speed;

            for (Block w : walls) {
                if (softCollision(this, w)) {
                    x = px;
                    y = py;
                    break;
                }
            }
        }

        private boolean softCollision(Block a, Block b) {
            Rectangle r1 = a.getBounds();
            Rectangle r2 = b.getBounds();
            r2.grow(-MOVE_TOLERANCE, -MOVE_TOLERANCE);
            return r1.intersects(r2);
        }
    }

    private class Ghost extends Block {
        String type;
        int startX, startY;
        int velX = 0, velY = 0;
        Image originalImage;
        boolean scared = false;

        Ghost(Image image, int x, int y, int w, int h, String type, int startX, int startY) {
            super(image, x, y, w, h);
            this.type = type;
            this.startX = startX;
            this.startY = startY;
            this.originalImage = image;
            setRandomDirection();
        }

        void setRandomDirection() {
            int r = random.nextInt(4);
            int s = tileSize / 8;
            if (r == 0) {
                velX = 0; velY = -s;
            } else if (r == 1) {
                velX = 0; velY = s;
            } else if (r == 2) {
                velX = -s; velY = 0;
            } else {
                velX = s; velY = 0;
            }
        }

        void setScared(boolean s) {
            scared = s;
            image = s ? scaredGhostImage : originalImage;
        }

        void reverseDirection() {
            velX = -velX;
            velY = -velY;
        }

        Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        void move() {
            int gx = x, gy = y;
            x += velX; y += velY;

            for (Block w : walls) {
                if (collision(this, w)) {
                    x = gx; y = gy;
                    for (int i = 0; i < 6; i++) {
                        setRandomDirection();
                        int nx = x + velX, ny = y + velY;
                        Rectangle next = new Rectangle(nx, ny, width, height);
                        boolean coll = false;
                        for (Block ww : walls) {
                            if (next.intersects(new Rectangle(ww.x, ww.y, ww.width, ww.height))) {
                                coll = true;
                                break;
                            }
                        }
                        if (!coll) break;
                    }
                    break;
                }
            }

            if (y < 0) y = 0;
            if (y + height > boardHeight) y = boardHeight - height;
        }
    }

    private class Cherry extends Block {
        Cherry(Image img, int x, int y, int w, int h) {
            super(img, x, y, w, h);
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
}

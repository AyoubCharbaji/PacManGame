package Resource;

import java.awt.*;
import javax.swing.*;

public class PacMan extends JPanel {

    private static final int TILE_SIZE = 32;
    private static final int SPEED = TILE_SIZE / 6;

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

private PacmanBlock pacman;
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
}

private PacmanBlock pacman;
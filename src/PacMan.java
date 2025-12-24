package Resource;

import java.awt.*;
import javax.swing.*;

public class PacMan extends JPanel {
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
}

private PacmanBlock pacman;
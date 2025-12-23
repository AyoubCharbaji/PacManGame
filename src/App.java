import javax.swing.*;
import Resource.PacMan;
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pac-Man");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            PacMan game = new PacMan();
            frame.add(game);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}

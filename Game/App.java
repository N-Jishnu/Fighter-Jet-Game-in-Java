import javax.swing.*;
import java.awt.Dimension;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 1000;
        int boardHeight = 700;

        JFrame frame = new JFrame("Fighter Game");
        // frame.setVisible(true);
		frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a custom JPanel for the input dialog
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 100));
        JTextField textField = new JTextField(20);
        panel.add(new JLabel("Username:"));
        panel.add(textField);

        int result = JOptionPane.showConfirmDialog(frame, panel, 
            "Enter Username", JOptionPane.OK_CANCEL_OPTION);
            
        String playerName = (result == JOptionPane.OK_OPTION) ? 
            textField.getText() : "Player";
        if (playerName.trim().isEmpty()) {
            playerName = "Player";
        }

        Fighter_Game F = new Fighter_Game(playerName);
        frame.add(F);
        frame.pack();
        F.requestFocus();
        frame.setVisible(true);
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ood.assignment;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Dimension;

/**
 *
 * @author Edmund Yap
 */
public class OODAssignment {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Users userSystem = new Users();
            
            JFrame frame = new JFrame("Logistics Handling Program");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());
            
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            JLabel titleLabel = new JLabel("Logistics Handling Program", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            
            JButton signUpButton = new JButton("Sign Up");
            signUpButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
            
            JButton loginButton = new JButton("Log In");
            loginButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
            
            JButton exitButton = new JButton("Exit");
            exitButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
            
            // Add rigid areas for spacing
            panel.add(Box.createVerticalGlue());
            panel.add(titleLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(signUpButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(loginButton);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(exitButton);
            panel.add(Box.createVerticalGlue());
            
            frame.add(panel, BorderLayout.CENTER);
            
            // Add action listeners
            signUpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    userSystem.signup();
                }
            });
            
            loginButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    userSystem.login();
                }
            });
            
            exitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            
            frame.setLocationRelativeTo(null); // Center the window
            frame.setVisible(true);
        });
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ood.assignment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Users {
    private String email;
    private String username;
    private String password;
    private String role;
    private String approve = null;
    private JFrame currentFrame;
    
    public Users() {}
    
    public Users(String email, String username, String password, String role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public class SystemAdmin extends Users {
    public SystemAdmin(String email, String username, String password, String role) {
        super(email, username, password, role);
    }
    
    @Override
    public void userMenu() {
        currentFrame = new JFrame("System Admin Menu");
        currentFrame.setSize(500, 400);
        currentFrame.setLayout(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("Welcome " + getUsername() + " (System Admin)", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        
        JButton promoteButton = new JButton("Promote to Admin");
        JButton revokeButton = new JButton("Revoke Admin Privileges");
        JButton lockButton = new JButton("Lock User Account");
        JButton viewEncryptedButton = new JButton("View Encrypted Data");
        JButton deleteFeedbackButton = new JButton("Delete Any Feedback");
        JButton logoutButton = new JButton("Logout");
        
        buttonPanel.add(promoteButton);
        buttonPanel.add(revokeButton);
        buttonPanel.add(lockButton);
        buttonPanel.add(viewEncryptedButton);
        buttonPanel.add(deleteFeedbackButton);
        buttonPanel.add(logoutButton);
        
        currentFrame.add(welcomeLabel, BorderLayout.NORTH);
        currentFrame.add(buttonPanel, BorderLayout.CENTER);
        
        promoteButton.addActionListener(e -> promoteToAdmin());
        revokeButton.addActionListener(e -> revokeAdminPrivileges());
        lockButton.addActionListener(e -> lockUserAccount());
        viewEncryptedButton.addActionListener(e -> viewEncryptedData());
        deleteFeedbackButton.addActionListener(e -> deleteAnyFeedback());
        logoutButton.addActionListener(e -> {
            logout();
            currentFrame.dispose();
        });
        
        currentFrame.setVisible(true);
    }
    
        private void promoteToAdmin() {
            JFrame promoteFrame = new JFrame("Promote User to Admin");
            promoteFrame.setSize(500, 500);
            promoteFrame.setLayout(new BorderLayout());

            JTextArea userTextArea = new JTextArea();
            userTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(userTextArea);

            JPanel inputPanel = new JPanel();
            JTextField usernameField = new JTextField(15);
            JButton promoteButton = new JButton("Promote User");

            inputPanel.add(new JLabel("Username to promote:"));
            inputPanel.add(usernameField);
            inputPanel.add(promoteButton);

            try {
                List<String> userLines = Files.readAllLines(Paths.get("credentials.txt"));
                userTextArea.append(String.format("%-20s %-15s %-10s%n", "Email", "Username", "Role"));
                userTextArea.append("---------------------------------------------\n");

                for (String line : userLines) {
                    String[] credentials = line.split(", ");
                    if (credentials.length >= 4 && !credentials[3].equalsIgnoreCase("admin")) {
                        userTextArea.append(String.format("%-20s %-15s %-10s%n", 
                            credentials[0], credentials[1], credentials[3]));
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(promoteFrame, "Error reading user data");
            }

            promoteFrame.add(scrollPane, BorderLayout.CENTER);
            promoteFrame.add(inputPanel, BorderLayout.SOUTH);

            promoteButton.addActionListener(e -> {
                String username = usernameField.getText();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(promoteFrame, "Please enter a username");
                    return;
                }

                File credentialsFile = new File("credentials.txt");
                File tempFile = new File("temp_credentials.txt");

                try {
                    List<String> lines = Files.readAllLines(credentialsFile.toPath());
                    boolean userFound = false;

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                        for (String line : lines) {
                            String[] parts = line.split(", ");
                            if (parts.length >= 4 && parts[1].equals(username)) {
                                if (parts[3].equalsIgnoreCase("admin")) {
                                    JOptionPane.showMessageDialog(promoteFrame, "User is already an admin");
                                    return;
                                }

                                userFound = true;
                                parts[3] = "admin";
                                line = String.join(", ", parts);
                            }
                            writer.write(line);
                            writer.newLine();
                        }
                    }

                    if (userFound) {
                        Files.move(tempFile.toPath(), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(promoteFrame, "User promoted to admin successfully");
                        promoteFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(promoteFrame, "User not found");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(promoteFrame, "Error processing files: " + ex.getMessage());
                }
            });

            promoteFrame.setVisible(true);
        }

        private void revokeAdminPrivileges() {
            JFrame revokeFrame = new JFrame("Revoke Admin Privileges");
            revokeFrame.setSize(600, 600);
            revokeFrame.setLayout(new BorderLayout());

            JTextArea adminTextArea = new JTextArea();
            adminTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(adminTextArea);

            JPanel inputPanel = new JPanel();
            JTextField usernameField = new JTextField(15);
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"staff", "salesman"});
            JButton revokeButton = new JButton("Revoke Admin");

            inputPanel.add(new JLabel("Admin username:"));
            inputPanel.add(usernameField);
            inputPanel.add(new JLabel("New role:"));
            inputPanel.add(roleCombo);
            inputPanel.add(revokeButton);

            try {
                List<String> userLines = Files.readAllLines(Paths.get("credentials.txt"));
                adminTextArea.append(String.format("%-20s %-15s %-10s%n", "Email", "Username", "Role"));
                adminTextArea.append("---------------------------------------------\n");

                for (String line : userLines) {
                    String[] credentials = line.split(", ");
                    if (credentials.length >= 4 && credentials[3].equalsIgnoreCase("admin")) {
                        adminTextArea.append(String.format("%-20s %-15s %-10s%n", 
                            credentials[0], credentials[1], credentials[3]));
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(revokeFrame, "Error reading user data");
            }

            revokeFrame.add(scrollPane, BorderLayout.CENTER);
            revokeFrame.add(inputPanel, BorderLayout.SOUTH);

            revokeButton.addActionListener(e -> {
                String username = usernameField.getText();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(revokeFrame, "Please enter a username");
                    return;
                }

                String newRole = (String) roleCombo.getSelectedItem();
                File credentialsFile = new File("credentials.txt");
                File tempFile = new File("temp_credentials.txt");

                try {
                    List<String> lines = Files.readAllLines(credentialsFile.toPath());
                    boolean adminFound = false;

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                        for (String line : lines) {
                            String[] parts = line.split(", ");
                            if (parts.length >= 4 && parts[1].equals(username)) {
                                if (!parts[3].equalsIgnoreCase("admin")) {
                                    JOptionPane.showMessageDialog(revokeFrame, "User is not an admin");
                                    return;
                                }

                                adminFound = true;
                                parts[3] = newRole;
                                line = String.join(", ", parts);
                            }
                            writer.write(line);
                            writer.newLine();
                        }
                    }

                    if (adminFound) {
                        Files.move(tempFile.toPath(), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(revokeFrame, "Admin privileges revoked successfully");
                        revokeFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(revokeFrame, "Admin user not found");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(revokeFrame, "Error processing files: " + ex.getMessage());
                }
            });

            revokeFrame.setVisible(true);
        }
        
        private void lockUserAccount() {
            JFrame lockFrame = new JFrame("Lock User Account");
            lockFrame.setSize(500, 400);
            lockFrame.setLayout(new BorderLayout());

            JTextArea userTextArea = new JTextArea();
            userTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(userTextArea);
        
            JPanel inputPanel = new JPanel();
            JTextField usernameField = new JTextField(15);
            JButton lockButton = new JButton("Lock Account");
            JButton unlockButton = new JButton("Unlock Account");
        
            inputPanel.add(new JLabel("Username:"));
            inputPanel.add(usernameField);
            inputPanel.add(lockButton);
            inputPanel.add(unlockButton);
        
            try {
                List<String> userLines = Files.readAllLines(Paths.get("credentials.txt"));
                userTextArea.append(String.format("%-20s %-15s %-10s %-10s%n", "Email", "Username", "Role", "Status"));
                userTextArea.append("-------------------------------------------------\n");
            
                for (String line : userLines) {
                    String[] credentials = line.split(", ");
                    if (credentials.length >= 4) {
                        String status = (credentials.length > 4 && credentials[4].equals("locked")) ? "locked" : "active";
                        userTextArea.append(String.format("%-20s %-15s %-10s %-10s%n", 
                            credentials[0], credentials[1], credentials[3], status));
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(lockFrame, "Error reading user data");
            }
        
            lockFrame.add(scrollPane, BorderLayout.CENTER);
            lockFrame.add(inputPanel, BorderLayout.SOUTH);
        
            lockButton.addActionListener(e -> toggleAccountLock(usernameField.getText(), true, lockFrame));
            unlockButton.addActionListener(e -> toggleAccountLock(usernameField.getText(), false, lockFrame));
        
            lockFrame.setVisible(true);
        }
    
        private void toggleAccountLock(String username, boolean lock, JFrame parentFrame) {
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Please enter a username");
                return;
            }
        
            File credentialsFile = new File("credentials.txt");
            File tempFile = new File("temp_credentials.txt");
        
            try {
                List<String> lines = Files.readAllLines(credentialsFile.toPath());
                boolean userFound = false;
            
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    for (String line : lines) {
                        String[] parts = line.split(", ");
                        if (parts.length >= 4 && parts[1].equals(username)) {
                            userFound = true;
                        
                            if (lock) {
                                if (parts.length > 4 && parts[4].equals("locked")) {
                                    JOptionPane.showMessageDialog(parentFrame, "Account is already locked");
                                    return;
                                }
                                line = line + ", locked";
                            } else {
                                if (parts.length <= 4 || !parts[4].equals("locked")) {
                                    JOptionPane.showMessageDialog(parentFrame, "Account is not locked");
                                    return;
                                }
                                line = parts[0] + ", " + parts[1] + ", " + parts[2] + ", " + parts[3];
                                if (parts.length > 5) {
                                    for (int i = 5; i < parts.length; i++) {
                                        line += ", " + parts[i];
                                    }
                                }
                            }
                        }
                        writer.write(line);
                        writer.newLine();
                    }
                }
            
                if (userFound) {
                    Files.move(tempFile.toPath(), credentialsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    JOptionPane.showMessageDialog(parentFrame, 
                        lock ? "Account locked successfully" : "Account unlocked successfully");
                    parentFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "User not found");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Error processing files: " + ex.getMessage());
            }
        }
    
        private void viewEncryptedData() {
            JFrame encryptedFrame = new JFrame("View Encrypted Data");
            encryptedFrame.setSize(600, 500);
            encryptedFrame.setLayout(new BorderLayout());
        
            JTextArea dataTextArea = new JTextArea();
            dataTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(dataTextArea);
        
            JButton closeButton = new JButton("Close");
        
            try {
                List<String> credentials = Files.readAllLines(Paths.get("credentials.txt"));
                dataTextArea.append("=== Encrypted User Credentials ===\n");
                for (String line : credentials) {
                    dataTextArea.append(line + "\n");
                }
            
                dataTextArea.append("\n=== Car Status Data ===\n");
                List<String> carStatus = Files.readAllLines(Paths.get("carStatus.txt"));
                for (String line : carStatus) {
                    dataTextArea.append(line + "\n");
                }
            
                dataTextArea.append("\n=== Payment Log ===\n");
                if (Files.exists(Paths.get("paymentLog.txt"))) {
                    List<String> paymentLog = Files.readAllLines(Paths.get("paymentLog.txt"));
                    for (String line : paymentLog) {
                        dataTextArea.append(line + "\n");
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(encryptedFrame, "Error reading data files: " + e.getMessage());
            }
        
            encryptedFrame.add(scrollPane, BorderLayout.CENTER);
            encryptedFrame.add(closeButton, BorderLayout.SOUTH);
        
            closeButton.addActionListener(e -> encryptedFrame.dispose());
            encryptedFrame.setVisible(true);
        }
    
        private void deleteAnyFeedback() {
            JFrame feedbackFrame = new JFrame("Delete Feedback");
            feedbackFrame.setSize(600, 500);
            feedbackFrame.setLayout(new BorderLayout());
        
            JTextArea feedbackTextArea = new JTextArea();
            feedbackTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(feedbackTextArea);
        
            JPanel inputPanel = new JPanel();
            JTextField searchField = new JTextField(15);
            JButton searchButton = new JButton("Search");
            JButton deleteButton = new JButton("Delete Selected");
            JButton refreshButton = new JButton("Refresh");
        
            inputPanel.add(new JLabel("Search feedback:"));
            inputPanel.add(searchField);
            inputPanel.add(searchButton);
            inputPanel.add(deleteButton);
            inputPanel.add(refreshButton);
        
            File feedbackFile = new File("feedbackLog.txt");
            if (!feedbackFile.exists()) {
                JOptionPane.showMessageDialog(feedbackFrame, "No feedback available to delete");
                feedbackFrame.dispose();
                return;
            }
        
            // Display all feedback initially
            displayAllFeedback(feedbackTextArea, feedbackFile);
        
            feedbackFrame.add(scrollPane, BorderLayout.CENTER);
            feedbackFrame.add(inputPanel, BorderLayout.SOUTH);
        
            searchButton.addActionListener(e -> {
                String searchTerm = searchField.getText();
                if (searchTerm.isEmpty()) {
                    displayAllFeedback(feedbackTextArea, feedbackFile);
                    return;
                }
            
                try {
                    List<String> feedbackLines = Files.readAllLines(feedbackFile.toPath());
                    feedbackTextArea.setText("");
                
                    for (String line : feedbackLines) {
                        if (line.toLowerCase().contains(searchTerm.toLowerCase())) {
                            feedbackTextArea.append(line + "\n");
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(feedbackFrame, "Error searching feedback: " + ex.getMessage());
                }
            });
        
            deleteButton.addActionListener(e -> {
                String selectedText = feedbackTextArea.getSelectedText();
                if (selectedText == null || selectedText.isEmpty()) {
                    JOptionPane.showMessageDialog(feedbackFrame, "Please select feedback to delete");
                    return;
                }
            
                int confirm = JOptionPane.showConfirmDialog(feedbackFrame, 
                    "Are you sure you want to delete the selected feedback?", 
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        List<String> feedbackLines = Files.readAllLines(feedbackFile.toPath());
                        File tempFile = new File("temp_feedback.txt");
                    
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                            for (String line : feedbackLines) {
                                if (!line.equals(selectedText.trim())) {
                                    writer.write(line);
                                    writer.newLine();
                                }
                            }
                        }
                    
                        Files.move(tempFile.toPath(), feedbackFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(feedbackFrame, "Feedback deleted successfully");
                        displayAllFeedback(feedbackTextArea, feedbackFile);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(feedbackFrame, "Error deleting feedback: " + ex.getMessage());
                    }
                }
            });
        
            refreshButton.addActionListener(e -> displayAllFeedback(feedbackTextArea, feedbackFile));
        
            feedbackFrame.setVisible(true);
        }
    
        private void displayAllFeedback(JTextArea textArea, File feedbackFile) {
            try {
                List<String> feedbackLines = Files.readAllLines(feedbackFile.toPath());
                textArea.setText("");
            
                for (String line : feedbackLines) {
                    textArea.append(line + "\n");
                }
            } catch (IOException e) {
                textArea.setText("Error reading feedback file");
            }
        }
    }
    
    public class ManagingStaff extends Users {
        public ManagingStaff(String email, String username, String password, String role) {
            super(email, username, password, role);
        }

        @Override
        public void userMenu() {
            currentFrame = new JFrame("Managing Staff Menu");
            currentFrame.setSize(500, 400);
            currentFrame.setLayout(new BorderLayout());
            
            JLabel welcomeLabel = new JLabel("Welcome " + getUsername() + " (Managing Staff)", JLabel.CENTER);
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            
            JButton userManagementButton = new JButton("User Management");
            JButton dataAnalysisButton = new JButton("Data Analysis");
            JButton updateProfileButton = new JButton("Update Profile");
            JButton logoutButton = new JButton("Logout");
            
            buttonPanel.add(userManagementButton);
            buttonPanel.add(dataAnalysisButton);
            buttonPanel.add(updateProfileButton);
            buttonPanel.add(logoutButton);
            
            currentFrame.add(welcomeLabel, BorderLayout.NORTH);
            currentFrame.add(buttonPanel, BorderLayout.CENTER);
            
            userManagementButton.addActionListener(e -> manageUsers());
            dataAnalysisButton.addActionListener(e -> analyzeData());
            updateProfileButton.addActionListener(e -> super.updateProfile());
            logoutButton.addActionListener(e -> {
                super.logout();
                currentFrame.dispose();
            });
            
            currentFrame.setVisible(true);
        }

        private void manageUsers() {
            JFrame userManagementFrame = new JFrame("User Management");
            userManagementFrame.setSize(400, 300);
            userManagementFrame.setLayout(new BorderLayout());
            
            JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            
            JButton deleteUserButton = new JButton("Delete User");
            JButton updateUserButton = new JButton("Update User");
            JButton searchUserButton = new JButton("Search User");
            JButton backButton = new JButton("Back to main menu");
            
            buttonPanel.add(deleteUserButton);
            buttonPanel.add(updateUserButton);
            buttonPanel.add(searchUserButton);
            buttonPanel.add(backButton);
            
            userManagementFrame.add(buttonPanel, BorderLayout.CENTER);
            
            deleteUserButton.addActionListener(e -> deleteUser());
            updateUserButton.addActionListener(e -> updateUser());
            searchUserButton.addActionListener(e -> searchUser());
            backButton.addActionListener(e -> userManagementFrame.dispose());
            
            userManagementFrame.setVisible(true);
        }
        
        private void deleteUser() {
            JFrame deleteFrame = new JFrame("Delete User");
            deleteFrame.setSize(600, 400);
            deleteFrame.setLayout(new BorderLayout());
            
            JTextArea userTextArea = new JTextArea();
            userTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(userTextArea);
            
            JPanel inputPanel = new JPanel();
            JTextField usernameField = new JTextField(15);
            JButton deleteButton = new JButton("Delete User");
            
            inputPanel.add(new JLabel("Username to delete:"));
            inputPanel.add(usernameField);
            inputPanel.add(deleteButton);
            
            try {
                List<String> userLines = Files.readAllLines(Paths.get("credentials.txt"));
                userTextArea.append(String.format("%-20s %-15s %-10s%n", "Email", "Username", "Role"));
                userTextArea.append("---------------------------------------------\n");
                
                for (String line : userLines) {
                    String[] credentials = line.split(", ");
                    if (credentials.length >= 4 && !credentials[3].equalsIgnoreCase("staff")) {
                        userTextArea.append(String.format("%-20s %-15s %-10s%n", 
                            credentials[0], credentials[1], credentials[3]));
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(deleteFrame, "Error reading user data");
            }
            
            deleteFrame.add(scrollPane, BorderLayout.CENTER);
            deleteFrame.add(inputPanel, BorderLayout.SOUTH);
            
            deleteButton.addActionListener(e -> {
                String usernameToDelete = usernameField.getText();
                if (usernameToDelete.isEmpty()) {
                    JOptionPane.showMessageDialog(deleteFrame, "Please enter a username");
                    return;
                }
                
                File fileCredentials = new File("credentials.txt");
                try {
                    List<String> userLines = Files.readAllLines(fileCredentials.toPath());
                    List<String> updatedLines = new ArrayList<>();
                    boolean userFound = false;
                    
                    for (String line : userLines) {
                        String[] credentials = line.split(", ");
                        if (credentials.length >= 4 && credentials[1].equalsIgnoreCase(usernameToDelete)) {
                            if (credentials[3].equalsIgnoreCase("staff")) {
                                JOptionPane.showMessageDialog(deleteFrame, "Cannot delete staff members.");
                                return;
                            }
                            
                            userFound = true;
                            int confirm = JOptionPane.showConfirmDialog(deleteFrame, 
                                "Are you sure you want to delete user: " + credentials[1] + " with role: " + credentials[3] + "?", 
                                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                            
                            if (confirm == JOptionPane.YES_OPTION) {
                                continue; // Skip adding this line (effectively deletes it)
                            }
                        }
                        updatedLines.add(line);
                    }
                    
                    if (userFound) {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileCredentials))) {
                            for (String line : updatedLines) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                        JOptionPane.showMessageDialog(deleteFrame, "User deleted successfully");
                        deleteFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(deleteFrame, "User not found or user is a staff member.");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(deleteFrame, "Error deleting user: " + ex.getMessage());
                }
            });
            
            deleteFrame.setVisible(true);
        }
        
        private void updateUser() {
            JFrame updateFrame = new JFrame("Update User");
            updateFrame.setSize(600, 400);
            updateFrame.setLayout(new BorderLayout());
            
            JTextArea userTextArea = new JTextArea();
            userTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(userTextArea);
            
            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField usernameField = new JTextField(15);
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"customer", "salesman"});
            JButton updateButton = new JButton("Update Role");
            
            inputPanel.add(new JLabel("Username to update:"));
            inputPanel.add(usernameField);
            inputPanel.add(new JLabel("New Role:"));
            inputPanel.add(roleCombo);
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(updateButton);
            
            try {
                List<String> userLines = Files.readAllLines(Paths.get("credentials.txt"));
                userTextArea.append(String.format("%-20s %-15s %-10s%n", "Email", "Username", "Role"));
                userTextArea.append("---------------------------------------------\n");
                
                for (String line : userLines) {
                    String[] credentials = line.split(", ");
                    if (credentials.length >= 4 && !credentials[3].equalsIgnoreCase("staff")) {
                        userTextArea.append(String.format("%-20s %-15s %-10s%n", 
                            credentials[0], credentials[1], credentials[3]));
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(updateFrame, "Error reading user data");
            }
            
            updateFrame.add(scrollPane, BorderLayout.CENTER);
            updateFrame.add(inputPanel, BorderLayout.NORTH);
            updateFrame.add(buttonPanel, BorderLayout.SOUTH);
            
            updateButton.addActionListener(e -> {
                String usernameToUpdate = usernameField.getText();
                if (usernameToUpdate.isEmpty()) {
                    JOptionPane.showMessageDialog(updateFrame, "Please enter a username");
                    return;
                }
                
                String newRole = (String) roleCombo.getSelectedItem();
                File fileCredentials = new File("credentials.txt");
                
                try {
                    List<String> userLines = Files.readAllLines(fileCredentials.toPath());
                    List<String> updatedLines = new ArrayList<>();
                    boolean userFound = false;
                    
                    for (String line : userLines) {
                        String[] credentials = line.split(", ");
                        if (credentials.length >= 4 && credentials[1].equalsIgnoreCase(usernameToUpdate)) {
                            if (credentials[3].equalsIgnoreCase("staff")) {
                                JOptionPane.showMessageDialog(updateFrame, "Cannot update staff members.");
                                return;
                            }
                            
                            userFound = true;
                            credentials[3] = newRole;
                            line = String.join(", ", credentials);
                        }
                        updatedLines.add(line);
                    }
                    
                    if (userFound) {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileCredentials))) {
                            for (String line : updatedLines) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                        JOptionPane.showMessageDialog(updateFrame, "Role updated successfully");
                        updateFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(updateFrame, "User not found or user is a staff member.");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(updateFrame, "Error updating user: " + ex.getMessage());
                }
            });
            
            updateFrame.setVisible(true);
        }
        
        private void searchUser() {
            JFrame searchFrame = new JFrame("Search User");
            searchFrame.setSize(500, 400);
            searchFrame.setLayout(new BorderLayout());
            
            JPanel searchPanel = new JPanel();
            JTextField searchField = new JTextField(20);
            JButton searchButton = new JButton("Search");
            
            searchPanel.add(new JLabel("Search Username:"));
            searchPanel.add(searchField);
            searchPanel.add(searchButton);
            
            JTextArea resultArea = new JTextArea();
            resultArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(resultArea);
            
            searchFrame.add(searchPanel, BorderLayout.NORTH);
            searchFrame.add(scrollPane, BorderLayout.CENTER);
            
            searchButton.addActionListener(e -> {
                String searchTerm = searchField.getText();
                if (searchTerm.isEmpty()) {
                    JOptionPane.showMessageDialog(searchFrame, "Please enter a search term");
                    return;
                }
                
                resultArea.setText("");
                try {
                    List<String> userLines = Files.readAllLines(Paths.get("credentials.txt"));
                    boolean found = false;
                    
                    resultArea.append(String.format("%-20s %-15s %-10s%n", "Email", "Username", "Role"));
                    resultArea.append("---------------------------------------------\n");
                    
                    for (String line : userLines) {
                        String[] credentials = line.split(", ");
                        if (credentials.length >= 4 && credentials[1].toLowerCase().contains(searchTerm.toLowerCase())) {
                            resultArea.append(String.format("%-20s %-15s %-10s%n", 
                                credentials[0], credentials[1], credentials[3]));
                            found = true;
                        }
                    }
                    
                    if (!found) {
                        resultArea.append("No users found matching your search.\n");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(searchFrame, "Error searching users: " + ex.getMessage());
                }
            });
            
            searchFrame.setVisible(true);
        }

        private void analyzeData() {
            JFrame analysisFrame = new JFrame("Data Analysis");
            analysisFrame.setSize(400, 300);
            analysisFrame.setLayout(new BorderLayout());
            
            JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            
            JButton paymentAnalysisButton = new JButton("Payment Analysis");
            JButton feedbackAnalysisButton = new JButton("Feedback Analysis");
            JButton generateReportsButton = new JButton("Generate Reports");
            JButton backButton = new JButton("Back to main menu");
            
            buttonPanel.add(paymentAnalysisButton);
            buttonPanel.add(feedbackAnalysisButton);
            buttonPanel.add(generateReportsButton);
            buttonPanel.add(backButton);
            
            analysisFrame.add(buttonPanel, BorderLayout.CENTER);
            
            paymentAnalysisButton.addActionListener(e -> paymentAnalysis());
            feedbackAnalysisButton.addActionListener(e -> feedbackAnalysis());
            generateReportsButton.addActionListener(e -> generateReports());
            backButton.addActionListener(e -> analysisFrame.dispose());
            
            analysisFrame.setVisible(true);
        }

        private void paymentAnalysis() {
            JFrame paymentFrame = new JFrame("Payment Analysis");
            paymentFrame.setSize(600, 400);
            paymentFrame.setLayout(new BorderLayout());
            
            JTextArea paymentTextArea = new JTextArea();
            paymentTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(paymentTextArea);
            
            File carStatusFile = new File("carStatus.txt");
            File paymentLog = new File("paymentLog.txt");
            
            try {
                if (!paymentLog.exists()) {
                    paymentLog.createNewFile();
                }
                
                // Write contents from carStatus.txt to paymentLog.txt
                try (BufferedReader reader = new BufferedReader(new FileReader(carStatusFile));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(paymentLog))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] carStatus = line.split(", ");
                        if (carStatus.length >= 7 && carStatus[4].equalsIgnoreCase("paid") && carStatus[6].equalsIgnoreCase("approved")) {
                            writer.write(String.format("%s, %s, %s", carStatus[0], carStatus[1], carStatus[5]));
                            writer.newLine();
                        }
                    }
                }
                
                // Read from paymentLog.txt to display
                try (BufferedReader reader = new BufferedReader(new FileReader(paymentLog))) {
                    String line;
                    paymentTextArea.append(String.format("%-10s %-20s %-20s%n", "CarID", "Model", "Seller"));
                    paymentTextArea.append("-------------------------------------------------------\n");
                    
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(", ");
                        if (parts.length >= 3) {
                            // Format each line with fixed-width columns
                            paymentTextArea.append(String.format("%-10s %-20s %-20s%n", 
                                parts[0].trim(), parts[1].trim(), parts[2].trim()));
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(paymentFrame, "Error processing payment data: " + e.getMessage());
            }
            
            paymentFrame.add(scrollPane, BorderLayout.CENTER);
            paymentFrame.setVisible(true);
        }
        
        private void feedbackAnalysis() {
            JFrame feedbackFrame = new JFrame("Feedback Analysis");
            feedbackFrame.setSize(600, 400);
            feedbackFrame.setLayout(new BorderLayout());
            
            JTextArea feedbackTextArea = new JTextArea();
            feedbackTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(feedbackTextArea);
            
            File feedbackLog = new File("feedbackLog.txt");
            if (!feedbackLog.exists()) {
                JOptionPane.showMessageDialog(feedbackFrame, "No feedback available");
                feedbackFrame.dispose();
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(feedbackLog))) {
                String line;
                feedbackTextArea.append("Customer Feedback:\n");
                feedbackTextArea.append("-------------------------------------------------------\n");
                while ((line = reader.readLine()) != null) {
                    feedbackTextArea.append(line + "\n");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(feedbackFrame, "Error reading feedback: " + e.getMessage());
            }
            
            feedbackFrame.add(scrollPane, BorderLayout.CENTER);
            feedbackFrame.setVisible(true);
        }
        
        private void generateReports() {
            System.out.println("DEBUG: generateReports() called");
            JFrame reportFrame = new JFrame("Generate Reports");
            reportFrame.setSize(500, 300);
            reportFrame.setLayout(new BorderLayout());

            JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));

            JButton salesReportButton = new JButton("Sales Report");
            JButton inventoryReportButton = new JButton("Inventory Report");
            JButton userReportButton = new JButton("User Activity Report");
            JButton feedbackReportButton = new JButton("Feedback Summary Report");

            buttonPanel.add(salesReportButton);
            buttonPanel.add(inventoryReportButton);
            buttonPanel.add(userReportButton);
            buttonPanel.add(feedbackReportButton);

            reportFrame.add(new JLabel("Select Report Type:", JLabel.CENTER), BorderLayout.NORTH);
            reportFrame.add(buttonPanel, BorderLayout.CENTER);

            salesReportButton.addActionListener(e -> generateSalesReport());
            inventoryReportButton.addActionListener(e -> generateInventoryReport());
            userReportButton.addActionListener(e -> generateUserActivityReport());
            feedbackReportButton.addActionListener(e -> generateFeedbackReport());

            reportFrame.setVisible(true);
        }

        private void generateSalesReport() {
            JFrame reportFrame = new JFrame("Sales Report");
            reportFrame.setSize(600, 400);
            reportFrame.setLayout(new BorderLayout());

            JTextArea reportArea = new JTextArea();
            reportArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(reportArea);

            try {
                List<String> paymentLines = Files.readAllLines(Paths.get("paymentLog.txt"));
                List<String> carLines = Files.readAllLines(Paths.get("carStatus.txt"));

                reportArea.append("=== SALES REPORT ===\n");
                reportArea.append(String.format("%-10s %-15s %-15s %-10s %-10s%n", 
                    "CarID", "Model", "Seller", "Price", "Status"));
                reportArea.append("------------------------------------------------------------\n");

                double totalRevenue = 0;
                int totalSales = 0;

                for (String paymentLine : paymentLines) {
                    String[] paymentData = paymentLine.split(", ");
                    if (paymentData.length >= 3) {
                        String carId = paymentData[0];
                        String model = paymentData[1];
                        String seller = paymentData[2];

                        // Find the car details to get price
                        for (String carLine : carLines) {
                            String[] carData = carLine.split(", ");
                            if (carData.length >= 7 && carData[0].equals(carId)) {
                                String price = carData[3];
                                String status = carData[4];

                                reportArea.append(String.format("%-10s %-15s %-15s %-10s %-10s%n", 
                                    carId, model, seller, price, status));

                                try {
                                    totalRevenue += Double.parseDouble(price);
                                    totalSales++;
                                } catch (NumberFormatException e) {
                                    // Skip if price is not a valid number
                                }
                                break;
                            }
                        }
                    }
                }

                reportArea.append("\n=== SUMMARY ===\n");
                reportArea.append(String.format("Total Sales: %d%n", totalSales));
                reportArea.append(String.format("Total Revenue: RM%.2f%n", totalRevenue));

            } catch (IOException e) {
                JOptionPane.showMessageDialog(reportFrame, "Error generating sales report: " + e.getMessage());
            }

            JButton saveButton = new JButton("Save Report");
            saveButton.addActionListener(e -> saveReportToFile(reportArea.getText(), "sales_report.txt"));

            reportFrame.add(scrollPane, BorderLayout.CENTER);
            reportFrame.add(saveButton, BorderLayout.SOUTH);
            reportFrame.setVisible(true);
        }

        private void generateInventoryReport() {
            JFrame reportFrame = new JFrame("Inventory Report");
            reportFrame.setSize(600, 400);
            reportFrame.setLayout(new BorderLayout());

            JTextArea reportArea = new JTextArea();
            reportArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(reportArea);

            try {
                List<String> carLines = Files.readAllLines(Paths.get("carStatus.txt"));

                reportArea.append("=== INVENTORY REPORT ===\n");
                reportArea.append(String.format("%-10s %-15s %-10s %-10s %-15s %-15s%n", 
                    "CarID", "Model", "Year", "Price", "Status", "Approval"));
                reportArea.append("--------------------------------------------------------------------\n");

                int availableCount = 0;
                int bookedCount = 0;
                int paidCount = 0;
                int totalCars = 0;

                for (String carLine : carLines) {
                    String[] carData = carLine.split(", ");
                    if (carData.length >= 7) {
                        reportArea.append(String.format("%-10s %-15s %-10s %-10s %-15s %-15s%n", 
                            carData[0], carData[1], carData[2], carData[3], carData[4], carData[6]));

                        totalCars++;
                        switch (carData[4].toLowerCase()) {
                            case "available":
                                availableCount++;
                                break;
                            case "booked":
                                bookedCount++;
                                break;
                            case "paid":
                                paidCount++;
                                break;
                        }
                    }
                }

                reportArea.append("\n=== SUMMARY ===\n");
                reportArea.append(String.format("Total Cars: %d%n", totalCars));
                reportArea.append(String.format("Available: %d%n", availableCount));
                reportArea.append(String.format("Booked: %d%n", bookedCount));
                reportArea.append(String.format("Paid: %d%n", paidCount));

            } catch (IOException e) {
                JOptionPane.showMessageDialog(reportFrame, "Error generating inventory report: " + e.getMessage());
            }

            JButton saveButton = new JButton("Save Report");
            saveButton.addActionListener(e -> saveReportToFile(reportArea.getText(), "inventory_report.txt"));

            reportFrame.add(scrollPane, BorderLayout.CENTER);
            reportFrame.add(saveButton, BorderLayout.SOUTH);
            reportFrame.setVisible(true);
        }

        private void generateUserActivityReport() {
            JFrame reportFrame = new JFrame("User Activity Report");
            reportFrame.setSize(600, 400);
            reportFrame.setLayout(new BorderLayout());

            JTextArea reportArea = new JTextArea();
            reportArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(reportArea);

            try {
                List<String> userLines = Files.readAllLines(Paths.get("credentials.txt"));
                List<String> ownershipLines = Files.exists(Paths.get("ownership.txt")) ? 
                    Files.readAllLines(Paths.get("ownership.txt")) : new ArrayList<>();

                reportArea.append("=== USER ACTIVITY REPORT ===\n");
                reportArea.append(String.format("%-15s %-15s %-10s %-15s %-10s%n", 
                    "Username", "Email", "Role", "Cars Owned", "Status"));
                reportArea.append("------------------------------------------------------------\n");

                int totalUsers = 0;
                int activeUsers = 0;
                int lockedUsers = 0;

                for (String userLine : userLines) {
                    String[] userData = userLine.split(", ");
                    if (userData.length >= 4) {
                        String username = userData[1];
                        String role = userData[3];
                        String status = (userData.length > 4 && userData[4].equals("locked")) ? "Locked" : "Active";

                        // Count cars owned by this user
                        int carsOwned = 0;
                        for (String ownershipLine : ownershipLines) {
                            String[] ownershipData = ownershipLine.split(", ");
                            if (ownershipData.length >= 2 && ownershipData[0].equals(username)) {
                                carsOwned++;
                            }
                        }

                        reportArea.append(String.format("%-15s %-15s %-10s %-15d %-10s%n", 
                            username, userData[0], role, carsOwned, status));

                        totalUsers++;
                        if (status.equals("Active")) {
                            activeUsers++;
                        } else {
                            lockedUsers++;
                        }
                    }
                }

                reportArea.append("\n=== SUMMARY ===\n");
                reportArea.append(String.format("Total Users: %d%n", totalUsers));
                reportArea.append(String.format("Active Users: %d%n", activeUsers));
                reportArea.append(String.format("Locked Users: %d%n", lockedUsers));

            } catch (IOException e) {
                JOptionPane.showMessageDialog(reportFrame, "Error generating user activity report: " + e.getMessage());
            }

            JButton saveButton = new JButton("Save Report");
            saveButton.addActionListener(e -> saveReportToFile(reportArea.getText(), "user_activity_report.txt"));

            reportFrame.add(scrollPane, BorderLayout.CENTER);
            reportFrame.add(saveButton, BorderLayout.SOUTH);
            reportFrame.setVisible(true);
        }

        private void generateFeedbackReport() {
            JFrame reportFrame = new JFrame("Feedback Summary Report");
            reportFrame.setSize(600, 400);
            reportFrame.setLayout(new BorderLayout());

            JTextArea reportArea = new JTextArea();
            reportArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(reportArea);

            File feedbackFile = new File("feedbackLog.txt");
            if (!feedbackFile.exists()) {
                JOptionPane.showMessageDialog(reportFrame, "No feedback available to generate report");
                reportFrame.dispose();
                return;
            }

            try {
                List<String> feedbackLines = Files.readAllLines(feedbackFile.toPath());

                reportArea.append("=== FEEDBACK SUMMARY REPORT ===\n");
                reportArea.append(String.format("%-10s %-15s %-20s %-30s%n", 
                    "CarID", "Username", "Feedback Excerpt", "Date"));
                reportArea.append("--------------------------------------------------------------------\n");

                int totalFeedback = 0;

                for (String line : feedbackLines) {
                    // Assuming format: "CarID: Username - Feedback text - Date"
                    String[] parts = line.split(" - ");
                    if (parts.length >= 3) {
                        String carUserPart = parts[0];
                        String feedbackText = parts[1];
                        String date = parts[2];

                        String[] carUser = carUserPart.split(": ");
                        String carId = carUser.length > 0 ? carUser[0] : "N/A";
                        String username = carUser.length > 1 ? carUser[1] : "N/A";

                        // Shorten feedback text for display
                        String excerpt = feedbackText.length() > 25 ? 
                            feedbackText.substring(0, 25) + "..." : feedbackText;

                        reportArea.append(String.format("%-10s %-15s %-20s %-30s%n", 
                            carId, username, excerpt, date));

                        totalFeedback++;
                    }
                }

                reportArea.append("\n=== SUMMARY ===\n");
                reportArea.append(String.format("Total Feedback Entries: %d%n", totalFeedback));

            } catch (IOException e) {
                JOptionPane.showMessageDialog(reportFrame, "Error generating feedback report: " + e.getMessage());
            }

            JButton saveButton = new JButton("Save Report");
            saveButton.addActionListener(e -> saveReportToFile(reportArea.getText(), "feedback_report.txt"));

            reportFrame.add(scrollPane, BorderLayout.CENTER);
            reportFrame.add(saveButton, BorderLayout.SOUTH);
            reportFrame.setVisible(true);
        }

        private void saveReportToFile(String reportContent, String fileName) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    Files.write(fileToSave.toPath(), reportContent.getBytes());
                    JOptionPane.showMessageDialog(null, "Report saved successfully to: " + fileToSave.getAbsolutePath());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving report: " + e.getMessage());
                }
            }
        }
    }
    
    public class Salesman extends Users {
        public Salesman(String email, String username, String password, String role) {
            super(email, username, password, role);
        }

        @Override
        public void userMenu() {
            // Set the current user in CarSystem before showing the menu

            currentFrame = new JFrame("Salesman Menu");
            currentFrame.setSize(500, 400);
            currentFrame.setLayout(new BorderLayout());

            JLabel welcomeLabel = new JLabel("Welcome " + getUsername() + " (Salesman)", JLabel.CENTER);
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));

            JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));

            JButton viewSalesButton = new JButton("View Sales Records");
            JButton updateProfileButton = new JButton("Update Profile");
            JButton logoutButton = new JButton("Logout");

            buttonPanel.add(viewSalesButton);
            buttonPanel.add(updateProfileButton);
            buttonPanel.add(logoutButton);

            currentFrame.add(welcomeLabel, BorderLayout.NORTH);
            currentFrame.add(buttonPanel, BorderLayout.CENTER);

            viewSalesButton.addActionListener(e -> paymentAnalysis());
            updateProfileButton.addActionListener(e -> super.updateProfile());
            logoutButton.addActionListener(e -> {
                super.logout();
                currentFrame.dispose();
            });

            currentFrame.setVisible(true);
        }

        private void paymentAnalysis() {
            JFrame paymentFrame = new JFrame("Payment Analysis");
            paymentFrame.setSize(600, 400);
            paymentFrame.setLayout(new BorderLayout());
            
            JTextArea paymentTextArea = new JTextArea();
            paymentTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(paymentTextArea);
            
            File carStatusFile = new File("carStatus.txt");
            File paymentLog = new File("paymentLog.txt");
            
            try {
                if (!paymentLog.exists()) {
                    paymentLog.createNewFile();
                }
                
                // Write contents from carStatus.txt to paymentLog.txt
                try (BufferedReader reader = new BufferedReader(new FileReader(carStatusFile));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(paymentLog))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] carStatus = line.split(", ");
                        if (carStatus.length >= 7 && carStatus[4].equalsIgnoreCase("paid") && carStatus[6].equalsIgnoreCase("approved")) {
                            writer.write(String.format("%s, %s, %s", carStatus[0], carStatus[1], carStatus[5]));
                            writer.newLine();
                        }
                    }
                }
                
                // Read from paymentLog.txt to display
                try (BufferedReader reader = new BufferedReader(new FileReader(paymentLog))) {
                    String line;
                    paymentTextArea.append(String.format("%-10s %-20s %-20s%n", "CarID", "Model", "Seller"));
                    paymentTextArea.append("-------------------------------------------------------\n");
                    
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(", ");
                        if (parts.length >= 3) {
                            // Format each line with fixed-width columns
                            paymentTextArea.append(String.format("%-10s %-20s %-20s%n", 
                                parts[0].trim(), parts[1].trim(), parts[2].trim()));
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(paymentFrame, "Error processing payment data: " + e.getMessage());
            }
            
            paymentFrame.add(scrollPane, BorderLayout.CENTER);
            paymentFrame.setVisible(true);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void signup() {
        JFrame signupFrame = new JFrame("Sign Up");
        signupFrame.setSize(400, 300);
        signupFrame.setLayout(new GridLayout(5, 2, 10, 10));

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        signupFrame.add(emailLabel);
        signupFrame.add(emailField);
        signupFrame.add(usernameLabel);
        signupFrame.add(usernameField);
        signupFrame.add(passwordLabel);
        signupFrame.add(passwordField);
        signupFrame.add(submitButton);
        signupFrame.add(cancelButton);

        submitButton.addActionListener(e -> {
            String email = emailField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = "customer"; // default role

            // Check for special role prefixes
            if (email.startsWith("/admin") && username.startsWith("/admin") && password.startsWith("/admin")) {
                role = "admin";
                email = email.substring(6); // Remove the prefix
                username = username.substring(6);
                password = password.substring(6);
            } 
            else if (email.startsWith("/sm") && username.startsWith("/sm") && password.startsWith("/sm")) {
                role = "salesman";
                email = email.substring(3);
                username = username.substring(3);
                password = password.substring(3);
            }
            else if (email.startsWith("/sf") && username.startsWith("/sf") && password.startsWith("/sf")) {
                role = "staff";
                email = email.substring(3);
                username = username.substring(3);
                password = password.substring(3);
            }

            // Validate the remaining credentials after removing prefixes
            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(signupFrame, "All fields are required after the prefix!");
                return;
            }

            // Check if username already exists
            String credentialsFile = "credentials.txt";
            try {
                // Read existing credentials to check for duplicate username
                File file = new File(credentialsFile);
                if (file.exists()) {
                    Scanner scanner = new Scanner(file);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] parts = line.split(", ");
                        if (parts.length >= 2 && parts[1].equalsIgnoreCase(username)) {
                            JOptionPane.showMessageDialog(signupFrame, "Username already exists! Please choose a different username.");
                            scanner.close();
                            return;
                        }
                    }
                    scanner.close();
                }

                FileWriter fileWriter = new FileWriter(credentialsFile, true);
                fileWriter.close();
                JOptionPane.showMessageDialog(signupFrame, "Account created successfully!");
                signupFrame.dispose();
                login();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(signupFrame, "An error occurred during sign up: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> signupFrame.dispose());
        signupFrame.setVisible(true);
    }
    
    public boolean login() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 200);
        loginFrame.setLayout(new GridLayout(3, 2, 10, 10));
        
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");
        
        loginFrame.add(usernameLabel);
        loginFrame.add(usernameField);
        loginFrame.add(passwordLabel);
        loginFrame.add(passwordField);
        loginFrame.add(loginButton);
        loginFrame.add(cancelButton);
        
        loginButton.addActionListener(e -> {
            String usernameInput = usernameField.getText();
            String passwordInput = new String(passwordField.getPassword());
            String credentialsFile = "credentials.txt";

            try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFile))) {
                if (!new File(credentialsFile).exists()) {
                    JOptionPane.showMessageDialog(loginFrame, "No account exists. Please sign up first.");
                    signup();
                    loginFrame.dispose();
                    return;
                }

                boolean loginSuccess = false;
                boolean accountLocked = false;
                String line;
                String[] credentials = null;

                while ((line = reader.readLine()) != null) {
                    credentials = line.split(", ");
                    if (credentials.length >= 4 && credentials[1].equals(usernameInput)) {
                        // Check if account is locked
                        if (credentials.length > 4 && Arrays.asList(credentials).contains("locked")) {
                            accountLocked = true;
                            break;
                        }

                        if (credentials[2].equals(passwordInput)) {
                            this.email = credentials[0];
                            this.username = credentials[1];
                            this.password = credentials[2];
                            this.role = credentials[3];
                            this.approve = (credentials.length > 4) ? credentials[4] : null;
                            loginSuccess = true;
                            break;
                        }
                    }
                }

                if (accountLocked) {
                    JOptionPane.showMessageDialog(loginFrame, "Your account is locked. Please contact an administrator.");
                    return;
                }

                if (loginSuccess) {
                    JOptionPane.showMessageDialog(loginFrame, "Login successful. Welcome!");
                    loginFrame.dispose();
                    if (credentials[3].equalsIgnoreCase("staff")) {
                        ManagingStaff staff = new ManagingStaff(credentials[0], credentials[1], credentials[2], credentials[3]);
                        staff.userMenu();
                    } else if (credentials[3].equalsIgnoreCase("customer")) {
                        Users user = new Users(credentials[0], credentials[1], credentials[2], credentials[3]);
                        user.userMenu();
                    } else if (credentials[3].equalsIgnoreCase("salesman")) {
                        Salesman salesman = new Salesman(credentials[0], credentials[1], credentials[2], credentials[3]);
                        salesman.userMenu();
                    }
                    else if (credentials[3].equalsIgnoreCase("admin")) {
                        SystemAdmin admin = new SystemAdmin(credentials[0], credentials[1], credentials[2], credentials[3]);
                        admin.userMenu();
                    }
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid username and/or password.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(loginFrame, "An error occurred during login: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> loginFrame.dispose());
        loginFrame.setVisible(true);
        return false;
    }
    
    public void userMenu() {
        currentFrame = new JFrame("Student Menu");
        currentFrame.setSize(500, 400);  // Increased height to accommodate new buttons
        currentFrame.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome " + getUsername() + " (" + getRole() + ")", JLabel.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));  // Changed to 5 rows
        JButton classManagementButton = new JButton("Class Management");
        JButton paymentManagementButton = new JButton("Payment Management");
        JButton instructorRatingButton = new JButton("Instructor Ratings");
        JButton updateProfileButton = new JButton("Update Profile");
        JButton logoutButton = new JButton("Logout");

        buttonPanel.add(classManagementButton);
        buttonPanel.add(paymentManagementButton);
        buttonPanel.add(instructorRatingButton);
        buttonPanel.add(updateProfileButton);
        buttonPanel.add(logoutButton);

        currentFrame.add(welcomeLabel, BorderLayout.NORTH);
        currentFrame.add(buttonPanel, BorderLayout.CENTER);

        classManagementButton.addActionListener(e -> manageClass());
        paymentManagementButton.addActionListener(e -> managePayment());
        instructorRatingButton.addActionListener(e -> instructorRatings());
        updateProfileButton.addActionListener(e -> updateProfile());
        logoutButton.addActionListener(e -> {
            logout();
            currentFrame.dispose();
        });

        currentFrame.setVisible(true);
    }
    
    private void manageClass() {
            JFrame classManagementFrame = new JFrame("User Management");
            classManagementFrame.setSize(400, 300);
            classManagementFrame.setLayout(new BorderLayout());
            
            JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));
            
            JButton bookClassButton = new JButton("Book Classes");
            JButton viewClassButton = new JButton("View Classes");
            JButton searchClassButton = new JButton("Search Class");
            JButton rescheduleClassButton = new JButton("Reschedule Class");
            JButton cancelClassButton = new JButton("Cancel Class");
            JButton backButton = new JButton("Back to main menu");
            
            buttonPanel.add(bookClassButton);
            buttonPanel.add(viewClassButton);
            buttonPanel.add(searchClassButton);
            buttonPanel.add(rescheduleClassButton);
            buttonPanel.add(cancelClassButton);
            buttonPanel.add(backButton);
            
            classManagementFrame.add(buttonPanel, BorderLayout.CENTER);
            
            bookClassButton.addActionListener(e -> bookClass());
            viewClassButton.addActionListener(e -> viewClass());
            searchClassButton.addActionListener(e -> searchClass());
            rescheduleClassButton.addActionListener(e -> rescheduleClass());
            cancelClassButton.addActionListener(e -> cancelClass());
            backButton.addActionListener(e -> classManagementFrame.dispose());
            
            classManagementFrame.setVisible(true);
        }
    
    private void managePayment() {
            JFrame paymentManagementFrame = new JFrame("User Management");
            paymentManagementFrame.setSize(400, 300);
            paymentManagementFrame.setLayout(new BorderLayout());
            
            JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            
            JButton makePaymentButton = new JButton("Make Payments");
            JButton searchPaymentButton = new JButton("Search Payment");
            JButton paymentHistoryButton = new JButton("View Payment History");
            JButton backButton = new JButton("Back to main menu");
            
            buttonPanel.add(makePaymentButton);
            buttonPanel.add(searchPaymentButton);
            buttonPanel.add(paymentHistoryButton);
            buttonPanel.add(backButton);
            
            paymentManagementFrame.add(buttonPanel, BorderLayout.CENTER);
            
            makePaymentButton.addActionListener(e -> makePayment());
            searchPaymentButton.addActionListener(e -> searchPayment());
            paymentHistoryButton.addActionListener(e -> paymentHistory());
            backButton.addActionListener(e -> paymentManagementFrame.dispose());
            
            paymentManagementFrame.setVisible(true);
        }
    
    private void instructorRatings() {
            JFrame instructorRatingsFrame = new JFrame("User Management");
            instructorRatingsFrame.setSize(400, 300);
            instructorRatingsFrame.setLayout(new BorderLayout());
            
            JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
            
            JButton addRatingButton = new JButton("Add Instructor Ratings");
            JButton viewRatingButton = new JButton("View Instructor Ratings");
            JButton backButton = new JButton("Back to main menu");
            
            buttonPanel.add(addRatingButton);
            buttonPanel.add(viewRatingButton);
            buttonPanel.add(backButton);
            
            instructorRatingsFrame.add(buttonPanel, BorderLayout.CENTER);
            
            addRatingButton.addActionListener(e -> addRating());
            viewRatingButton.addActionListener(e -> viewRating());
            backButton.addActionListener(e -> instructorRatingsFrame.dispose());
            
            instructorRatingsFrame.setVisible(true);
        }
    
    private void bookClass(){
        JFrame bookClassFrame = new JFrame("Book a Class");
        bookClassFrame.setSize(650, 500);
        bookClassFrame.setLayout(new BorderLayout());
        
        JTextArea classBookingArea = new JTextArea();
        classBookingArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(classBookingArea);
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        JTextField classIdField = new JTextField(10);
        JButton bookButton = new JButton("Book");
        
        inputPanel.add(new JLabel("Class ID to book: "));
        inputPanel.add(classIdField);
        inputPanel.add(bookButton);
        
        //Display all available classes
        
        
    }
    
    private void viewClass(){
        JFrame viewClassFrame = new JFrame("My Classes");
        viewClassFrame.setSize(650, 450);
        viewClassFrame.setLayout(new BorderLayout());
        
        JTextArea classViewArea = new JTextArea();
        classViewArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(classViewArea);
        
        classViewArea.append("=== YOUR ENROLLED CLASSES ===\n");
        classViewArea.append(String.format("%-8s %-20s %-15s %-20s %-8s%n",
            "ClassID", "Class Name", "Instructor", "Schedule", "Status"));
        classViewArea.append("--------------------------------------------------------------------------\n");
    }
    
    private void searchClass(){
        JFrame searchClassFrame = new JFrame("Search for Classes");
        searchClassFrame.setSize(650, 450);
        searchClassFrame.setLayout(new BorderLayout());
        
        JPanel top = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        top.add(new JLabel("Search by class name / instructor:"));
        top.add(searchField);
        top.add(searchButton);
        
        JTextArea classSearchArea = new JTextArea();
        classSearchArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(classSearchArea);
        
        searchClassFrame.add(top, BorderLayout.NORTH);
        searchClassFrame.add(scroll, BorderLayout.CENTER);
    }
    
    private void rescheduleClass(){
        JFrame rescheduleClassFrame = new JFrame("Reschedule Your Classes");
        rescheduleClassFrame.setSize(500, 250);
        rescheduleClassFrame.setLayout(new GridLayout(3, 2, 10, 10));
        
        JTextField oldIdField = new JTextField();
        JTextField newIdField = new JTextField();
        JButton confirm = new JButton("Reschedule");
        JButton cancel = new JButton ("Cancel");
        
        rescheduleClassFrame.add(new JLabel("Current Class ID:"));
        rescheduleClassFrame.add(oldIdField);
        rescheduleClassFrame.add(new JLabel("New Class ID:"));
        rescheduleClassFrame.add(newIdField);
        rescheduleClassFrame.add(confirm);
        rescheduleClassFrame.add(cancel);
    }
    
    private void cancelClass(){
        JFrame cancelClassFrame = new JFrame("Cancel Class");
        cancelClassFrame.setSize(500, 200);
        cancelClassFrame.setLayout(new GridLayout(2, 2, 10, 10));
        
        JTextField classIdField = new JTextField();
        JButton cancelButton = new JButton("Cancel Enrollment");
        
        cancelClassFrame.add(new JLabel("Class ID to cancel:"));
        cancelClassFrame.add(classIdField);

    }
    
    private void makePayment(){
        JFrame makePaymentFrame = new JFrame("Make Payment");
        makePaymentFrame.setSize(500, 300);
        makePaymentFrame.setLayout(new BorderLayout());
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        JTextField classIdField = new JTextField();
        JTextField amountField = new JTextField();
        JButton payButton = new JButton("Pay");
        
        inputPanel.add(new JLabel("Class ID:"));
        inputPanel.add(classIdField);
        inputPanel.add(new JLabel("Amount (RM):"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(payButton);
        
        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.append("Your active enrollments:\n");
    }
    
    private void searchPayment(){
        JFrame frame = new JFrame("Search Payment");
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        
        JPanel top = new JPanel();
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        top.add(new JLabel("Payment ID or Class ID:"));
        top.add(searchField);
        
        JTextArea paymentSearchArea = new JTextArea();
        paymentSearchArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(paymentSearchArea);
        
        frame.add(top, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
    }
    
    private void paymentHistory(){
        JFrame paymentHistoryFrame = new JFrame("Your Payment History");
        paymentHistoryFrame.setSize(650, 400);
        paymentHistoryFrame.setLayout(new BorderLayout());

        JTextArea paymentHistoryArea = new JTextArea();
        paymentHistoryArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(paymentHistoryArea);
        
        paymentHistoryArea.append("=== YOUR PAYMENT HISTORY ===\n");
        paymentHistoryArea.append(String.format("%-10s %-10s %-10s %-12s %-8s%n", "PayID", "ClassID", "Amount", "Date", "Status"));
        paymentHistoryArea.append("-----------------------------------------------------\\n");

        
    }
    
    private void addRating(){
        JFrame addRatingFrame = new JFrame("Rate an Instructor");
        addRatingFrame.setSize(500, 400);
        addRatingFrame.setLayout(new BorderLayout());
        
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        
        JTextField classIdField = new JTextField();
        JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextArea commentArea = new JTextArea(4, 20);
        JButton submit = new JButton("Submit Rating");
        
        form.add(new JLabel("Class ID (from your enrollments): "));
        form.add(classIdField);
        form.add(new JLabel("Rating (1-5):"));
        form.add(ratingCombo);
        form.add(new JLabel("Comment:"));
        form.add(new JScrollPane(commentArea));
        form.add(new JLabel(""));
        form.add(submit);
        
        addRatingFrame.add(form, BorderLayout.CENTER);
        
        
    }
    
    private void viewRating(){
        JFrame viewRatingFrame = new JFrame("Your Feedback History");
        viewRatingFrame.setSize(700, 400);
        viewRatingFrame.setLayout(new BorderLayout());

        JTextArea viewRatingArea = new JTextArea();
        viewRatingArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(viewRatingArea);

        viewRatingArea.append("=== YOUR SUBMITTED RATINGS ===\n");
        viewRatingArea.append(String.format("%-8s %-10s %-15s %-10s %-8s %-25s %-12s%n", "RateID", "ClassID", "Instructor", "Rating", "/5", "Comment", "Date"));
    }
    
    private void leaveFeedback(String carId) {
        JFrame feedbackFrame = new JFrame("Leave Feedback for Car " + carId);
        feedbackFrame.setSize(400, 300);
        feedbackFrame.setLayout(new BorderLayout());
        
        JTextArea feedbackArea = new JTextArea(10, 30);
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        JButton submitButton = new JButton("Submit Feedback");
        
        feedbackFrame.add(new JLabel("Please leave your feedback for car " + carId + ":"), BorderLayout.NORTH);
        feedbackFrame.add(scrollPane, BorderLayout.CENTER);
        feedbackFrame.add(submitButton, BorderLayout.SOUTH);
        
        submitButton.addActionListener(e -> {
            String review = feedbackArea.getText();
            if (review.isEmpty()) {
                JOptionPane.showMessageDialog(feedbackFrame, "Please enter some feedback");
                return;
            }
            
            File feedbackLog = new File("feedbackLog.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(feedbackLog, true))) {
                writer.write(carId + ": " + getUsername() + " - " + review);
                writer.newLine();
                JOptionPane.showMessageDialog(feedbackFrame, "Thank you for your feedback!");
                feedbackFrame.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(feedbackFrame, "Error saving feedback: " + ex.getMessage());
            }
        });
        
        feedbackFrame.setVisible(true);
    }
    
    public void logout() {
        int choice = JOptionPane.showConfirmDialog(null, 
            "Would you like to logout " + getUsername() + "?", 
            "Logout", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            this.email = null;
            this.username = null;
            this.password = null;
            this.role = null;
            if (currentFrame != null) {
                currentFrame.dispose();
            }
        }
    }
    
    public void updateProfile() {
        JFrame updateFrame = new JFrame("Update Profile");
        updateFrame.setSize(400, 200);
        updateFrame.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel fieldLabel = new JLabel("Select field to update:");
        JComboBox<String> fieldCombo = new JComboBox<>(new String[]{"Email", "Username", "Password"});
        JLabel verifyUserLabel = new JLabel("Verify Username:");
        JTextField verifyUserField = new JTextField();
        JLabel verifyPassLabel = new JLabel("Verify Password:");
        JPasswordField verifyPassField = new JPasswordField();
        JButton proceedButton = new JButton("Proceed");
        JButton cancelButton = new JButton("Cancel");

        updateFrame.add(fieldLabel);
        updateFrame.add(fieldCombo);
        updateFrame.add(verifyUserLabel);
        updateFrame.add(verifyUserField);
        updateFrame.add(verifyPassLabel);
        updateFrame.add(verifyPassField);
        updateFrame.add(proceedButton);
        updateFrame.add(cancelButton);

        proceedButton.addActionListener(e -> {
            String verifyUsername = verifyUserField.getText();
            String verifyPassword = new String(verifyPassField.getPassword());
            String selectedField = (String) fieldCombo.getSelectedItem();

            if (verifyUsername.isEmpty() || verifyPassword.isEmpty()) {
                JOptionPane.showMessageDialog(updateFrame, "Please enter both username and password");
                return;
            }

            String credentialsFile = "credentials.txt";
            try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFile))) {
                // Read all lines into a list
                List<String> lines = Files.readAllLines(Paths.get(credentialsFile));
                boolean found = false;

                for (int i = 0; i < lines.size(); i++) {
                    String[] credentials = lines.get(i).split(", ");
                    if (credentials.length >= 4 && credentials[1].equals(verifyUsername) && credentials[2].equals(verifyPassword)) {
                        found = true;
                        // Open update dialog and get updated line
                        String updatedLine = openUpdateDialog(updateFrame, selectedField, credentials);
                        if (updatedLine != null) {
                            lines.set(i, updatedLine);
                        }
                        break;
                    }
                }

                if (found) {
                    // Write all lines back to file, preserving all users
                    Files.write(Paths.get(credentialsFile), lines);
                    JOptionPane.showMessageDialog(updateFrame, "Profile updated successfully.");
                    updateFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(updateFrame, "Incorrect username/password.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(updateFrame, "Error reading credentials: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> updateFrame.dispose());
        updateFrame.setVisible(true);
    }

    // Helper method to handle the update logic
    private String openUpdateDialog(JFrame parentFrame, String selectedField, String[] credentials) {
        final String[] updatedLine = {null};

        JDialog dialog = new JDialog(parentFrame, "Update " + selectedField, true);
        dialog.setSize(350, 150);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(parentFrame);

        JPanel inputPanel = new JPanel();
        JLabel newValueLabel = new JLabel("New " + selectedField + ":");
        JTextField newValueField = selectedField.equals("Password") ? new JPasswordField() : new JTextField();

        inputPanel.add(newValueLabel);
        inputPanel.add(newValueField);

        JButton updateButton = new JButton("Update");

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(updateButton, BorderLayout.SOUTH);

        updateButton.addActionListener(ev -> {
            String newValue = newValueField.getText().trim();
            if (newValue.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a new value");
                return;
            }

            // Check if updating username and if new username already exists
            if ("username".equalsIgnoreCase(selectedField)) {
                String credentialsFile = "credentials.txt";
                try {
                    List<String> lines = Files.readAllLines(Paths.get(credentialsFile));
                    for (String line : lines) {
                        String[] parts = line.split(", ");
                        if (parts.length >= 4) {
                            String existingUsername = parts[1];
                            // If username matches the new one and is not the current user's username
                            if (existingUsername.equals(newValue) && !existingUsername.equals(credentials[1])) {
                                JOptionPane.showMessageDialog(dialog, "Username already exists. Please choose a different username.");
                                return; // stop here, don't update or close dialog
                            }
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error reading credentials: " + ex.getMessage());
                    return; // stop here, don't update or close dialog
                }
            }

            String newLine = switch(selectedField.toLowerCase()) {
                case "email" -> newValue + ", " + credentials[1] + ", " + credentials[2] + ", " + credentials[3];
                case "username" -> credentials[0] + ", " + newValue + ", " + credentials[2] + ", " + credentials[3];
                case "password" -> credentials[0] + ", " + credentials[1] + ", " + newValue + ", " + credentials[3];
                default -> String.join(", ", credentials);
            };

            if (credentials.length > 4) {
                newLine += ", " + credentials[4];
            }

            updatedLine[0] = newLine;

            JOptionPane.showMessageDialog(dialog, selectedField + " updated successfully!");
            dialog.dispose();
        });

        dialog.setVisible(true);

        return updatedLine[0];
    }

    private void viewFeedbackHistory() {
        JFrame feedbackFrame = new JFrame("Your Feedback History");
        feedbackFrame.setSize(600, 400);
        feedbackFrame.setLayout(new BorderLayout());

        JTextArea feedbackTextArea = new JTextArea();
        feedbackTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(feedbackTextArea);

        File feedbackFile = new File("feedbackLog.txt");
        if (!feedbackFile.exists()) {
            JOptionPane.showMessageDialog(feedbackFrame, "You haven't submitted any feedback yet");
            feedbackFrame.dispose();
            return;
        }

        try {
            List<String> feedbackLines = Files.readAllLines(feedbackFile.toPath());
            feedbackTextArea.append("=== YOUR FEEDBACK HISTORY ===\n");
            feedbackTextArea.append("------------------------------------------------\n");

            for (String line : feedbackLines) {
                if (line.contains(getUsername())) {
                    // Extract the relevant parts of the feedback
                    String[] parts = line.split(" - ");
                    if (parts.length >= 2) {
                        String carInfo = parts[0];
                        String feedback = parts[1];
                        feedbackTextArea.append(carInfo + "\n" + feedback + "\n\n");
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(feedbackFrame, "Error reading feedback: " + e.getMessage());
        }

        feedbackFrame.add(scrollPane, BorderLayout.CENTER);
        feedbackFrame.setVisible(true);
    }
}

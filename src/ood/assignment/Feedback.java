/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ood.assignment;
import java.io.*;
import java.util.ArrayList; // import the ArrayList class
import java.util.Scanner;

public class Feedback {
    private String feedbackID;
    private String username;
    private String carID;
    private double rating;
    private String comment;
    private Scanner sc = new Scanner(System.in);
    
    public Feedback() {}
    
    public Feedback(String feedbackID, String username, String carID, double rating, String comment){
        this.feedbackID = feedbackID;
        this.username = username;
        this.carID = carID;
        this.rating = rating;
        this.comment = comment;
    }
    
    public String getFeedbackID() { return feedbackID; }
    public String getUsername() { return username; }
    public String getCarID() { return carID; }
    public double getRating() { return rating; }
    public String getComment() { return comment; }
    
    public void addComment(String username, String carID){
        File feedback = new File("feedback.txt");
        
        try {
            if (!feedback.exists()) {
                feedback.createNewFile();
                System.out.println("Created feedback.txt");
            }
        }
        catch (IOException e) {
            System.out.println("Error creating files: " + e.getMessage());
            return;
        }
        
        // Generate new feedback ID
        String newFeedbackID = generateNewFeedbackID(feedback);
        
        // Get rating from user
        double rating = 0;
        while (true) {
            System.out.println("Please enter your rating for this car (1-5):");
            try {
                rating = Double.parseDouble(sc.nextLine());
                if (rating < 1 || rating > 5) {
                    System.out.println("Rating must be between 1 and 5. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        
        // Get comment from user
        System.out.println("Please enter your comments about this car:");
        String comment = sc.nextLine();
        
         // Create new feedback entry
        Feedback newFeedback = new Feedback(newFeedbackID, username, carID, rating, comment);
        
        // Write to feedback file
        try (FileWriter fileWriter = new FileWriter(feedback, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                PrintWriter out = new PrintWriter(bufferedWriter)) {
            
            out.println(newFeedbackID + ", " + username + ", " + carID + ", " + rating + ", " + comment);
            System.out.println("Thank you for your feedback!");
        }
        catch (IOException e){
            System.out.println("An error occurred while saving your feedback: " + e.getMessage());
        }
    }
    
    private String generateNewFeedbackID(File feedback){
        int maxId = 0;
        
        if (feedback.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader (feedback))){
                String line;
                while ((line = reader.readLine()) != null){
                    String[] parts = line.split(", ");
                    if (parts.length >0) {
                        String feedbackId = parts[0];
                        if (feedbackId.startsWith("F")) {
                            try{
                                int num = Integer.parseInt(feedbackID.substring(1));
                                if (num > maxId){
                                    maxId = num;
                                }
                            }
                            catch (NumberFormatException e) {
                                System.out.println("Invalid FeedbackID format: " + feedbackId);
                            }
                        }
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Error reading feedback file: " + e.getMessage());
            }
        }
        return String.format("F%02d", maxId + 1);
    }
}

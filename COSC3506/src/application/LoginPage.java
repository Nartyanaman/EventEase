package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginPage {
    public static Scene getScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Login to Event Management System");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            try {
                if (DatabaseHandler.authenticateUser(email, password)) {
                    String role = DatabaseHandler.getUserRole(email);
                    Main.showDashboard(role);
                } else {
                    errorLabel.setText("Invalid email or password.");
                    errorLabel.setVisible(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("An error occurred. Please try again.");
                errorLabel.setVisible(true);
            }
        });

        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> Main.showIndexPage());

        root.getChildren().addAll(titleLabel, emailField, passwordField, loginButton, backButton, errorLabel);

        return new Scene(root, 400, 300);
    }
}
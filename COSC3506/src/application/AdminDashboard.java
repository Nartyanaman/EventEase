package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import static application.UserOperations.logEvent;

import java.sql.*;

public class AdminDashboard {
	
	private static final String url = "jdbc:mysql://localhost:3306/event_management_system";
    private static final String user = "root";
    private static final String password = "";

    private Connection conn;
    private Stage primaryStage;

    public AdminDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        establishConnection();
    }

    public Scene getScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome to Admin Dashboard");
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> Main.showIndexPage());

        Button viewUsersButton = new Button("View User List");
        viewUsersButton.setOnAction(e -> fetchUsers(root));

        Button viewAnalyticsButton = new Button("View System Analytics");
        viewAnalyticsButton.setOnAction(e -> fetchSystemAnalytics(root));

        Button viewAuditLogsButton = new Button("View Audit Logs");
        viewAuditLogsButton.setOnAction(e -> fetchAuditLogs(root));
        
        Button clearAuditLogsButton = new Button("Clear Audit Logs");
        clearAuditLogsButton.setOnAction(e -> clearAuditLogs());

        Button addUserButton = new Button("Add User");
        addUserButton.setOnAction(e -> showAddUserDialog());

        Button editUserButton = new Button("Edit User");
        editUserButton.setOnAction(e -> showEditUserDialog());

        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setOnAction(e -> showDeleteUserDialog());

        root.getChildren().addAll(welcomeLabel, backButton, viewUsersButton, viewAnalyticsButton, viewAuditLogsButton, clearAuditLogsButton, addUserButton, editUserButton, deleteUserButton);

        Scene scene = new Scene(root, 600, 550);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        return scene;
    }

    private void establishConnection() {
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


private void fetchUsers(VBox root) {
    try (Connection conn = DriverManager.getConnection(url, user, password);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT user_id, name, email, role FROM Users")) {

        root.getChildren().clear();

        Label titleLabel = new Label("User List");
        root.getChildren().addAll(titleLabel);

        while (rs.next()) {
            int userId = rs.getInt("user_id");
            String name = rs.getString("name");
            String email = rs.getString("email");
            String role = rs.getString("role");
            Label userLabel = new Label("User ID: " + userId + " - " + name + " - " + email + " (" + role + ")");
            root.getChildren().add(userLabel);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(getScene()));
        root.getChildren().add(backButton);

    } catch (SQLException e) {
        System.err.println("Error fetching users: " + e.getMessage());
    }
}

private void clearAuditLogs() {
    String query = "DELETE FROM event_logs";
    try (Connection conn = DriverManager.getConnection(url, user, password);
         Statement stmt = conn.createStatement()) {

        int rowsAffected = stmt.executeUpdate(query);
        if (rowsAffected > 0) {
            showAlert(AlertType.INFORMATION, "Success", "Audit logs cleared successfully!");
        } else {
            showAlert(AlertType.ERROR, "Error", "No audit logs to clear.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert(AlertType.ERROR, "Error", "Database error: " + e.getMessage());
    }
}


private void fetchSystemAnalytics(VBox root) {
    try (Connection conn = DriverManager.getConnection(url, user, password);
         Statement stmt = conn.createStatement()) {

        root.getChildren().clear();

        ResultSet usersResult = stmt.executeQuery("SELECT role, COUNT(*) AS count FROM Users GROUP BY role");
        Label usersLabel = new Label("User Counts:");
        root.getChildren().addAll(usersLabel);
        while (usersResult.next()) {
            String role = usersResult.getString("role");
            int count = usersResult.getInt("count");
            Label countLabel = new Label(role + ": " + count);
            root.getChildren().add(countLabel);
        }

        ResultSet eventsResult = stmt.executeQuery("SELECT COUNT(*) AS count FROM Events");
        if (eventsResult.next()) {
            int eventCount = eventsResult.getInt("count");
            Label eventsLabel = new Label("Event Count: " + eventCount);
            root.getChildren().add(eventsLabel);
        }

        ResultSet allEventsResult = stmt.executeQuery("SELECT event_name, event_date FROM Events");
        Label allEventsLabel = new Label("All Events:");
        root.getChildren().add(allEventsLabel);
        while (allEventsResult.next()) {
            String eventName = allEventsResult.getString("event_name");
            String eventDate = allEventsResult.getString("event_date");
            Label eventLabel = new Label(eventName + " on " + eventDate);
            root.getChildren().add(eventLabel);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(getScene()));
        root.getChildren().add(backButton);

    } catch (SQLException e) {
        System.err.println("Error fetching system analytics: " + e.getMessage());
    }
}

private void fetchAuditLogs(VBox root) {	
    try (Connection conn = DriverManager.getConnection(url, user, password);
         Statement stmt = conn.createStatement()) {

        root.getChildren().clear();

        ResultSet rs = stmt.executeQuery("SELECT * FROM event_logs ORDER BY timestamp DESC LIMIT 10");

        Label titleLabel = new Label("Audit Logs");
        root.getChildren().addAll(titleLabel);

        while (rs.next()) {
            int logId = rs.getInt("log_id");
            int userId = rs.getInt("user_id");
            String action = rs.getString("action");
            int eventId = rs.getInt("event_id");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            Label logLabel = new Label("Log ID: " + logId + ", User ID: " + userId + ", Action: " + action + ", Event ID: " + eventId + ", Timestamp: " + timestamp);
            root.getChildren().add(logLabel);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(getScene()));
        root.getChildren().add(backButton);

    } catch (SQLException e) {
        System.err.println("Error fetching audit logs: " + e.getMessage());
    }
}

    private void showAddUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.setTitle("Add User");
        dialog.setHeaderText("Enter User Details");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        dialogVBox.getChildren().addAll(nameField, emailField, passwordField);

        dialog.getDialogPane().setContent(dialogVBox);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == addButton) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String password = passwordField.getText().trim();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    addUser(name, email, password);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Enter User Details");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);

        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "Vendor", "Client");
        roleComboBox.setPromptText("Role");

        ButtonType editButton = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButton, ButtonType.CANCEL);

        dialogVBox.getChildren().addAll(userIdField, nameField, emailField, roleComboBox);

        dialog.getDialogPane().setContent(dialogVBox);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == editButton) {
                String userIdStr = userIdField.getText().trim();
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String role = roleComboBox.getValue();

                try {
                    int userId = Integer.parseInt(userIdStr);
                    editUser(userId, name, email, role);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid User ID.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showDeleteUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.setTitle("Delete User");
        dialog.setHeaderText("Enter User ID");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);

        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");

        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButton, ButtonType.CANCEL);

        dialogVBox.getChildren().addAll(userIdField);

        dialog.getDialogPane().setContent(dialogVBox);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButton) {
                String userIdStr = userIdField.getText().trim();
                try {
                    int userId = Integer.parseInt(userIdStr);
                    deleteUser(userId);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid User ID.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
  
    private void addUser(String name, String email, String password) {
        String query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        logEvent(userId, "Admin created user", 0);
                    }
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void editUser(int userId, String name, String email, String role) {
        String query = "UPDATE users SET name = ?, email = ?, role = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, role);
            stmt.setInt(4, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logEvent(userId, "Admin updated user", 0);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void deleteUser(int userId) {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logEvent(userId, "Admin deleted user", 0);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
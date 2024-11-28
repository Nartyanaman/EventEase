package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDashboard {

    private static final String url = "jdbc:mysql://localhost:3306/event_management_system";
    private static final String user = "root";
    private static final String password = "";

    private Connection conn;
    private Stage primaryStage;
    private int userId;
    private int nextEventId;
    private ListView<String> clientEventsListView;

    public ClientDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        establishConnection();
        userId = getUserIdFromSession();
        nextEventId = fetchNextEventId();
    }

    public Scene getScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Client Dashboard");
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> Main.showIndexPage());

        Label requestServiceLabel = new Label("Request Event Services");
        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID (Admin 1, Vendor 2, Client 3)");
        TextField eventNameField = new TextField();
        eventNameField.setPromptText("Event Name");
        DatePicker eventDatePicker = new DatePicker();
        Button requestServiceButton = new Button("Request Service");
        requestServiceButton.setOnAction(e -> requestService(
                Integer.parseInt(userIdField.getText()),
                eventNameField.getText(),
                eventDatePicker.getValue().toString()
        ));

        VBox requestServiceForm = new VBox(10, userIdField, eventNameField, eventDatePicker, requestServiceButton);
        requestServiceForm.setAlignment(Pos.CENTER);

        Label myEventsLabel = new Label("My Events");
        clientEventsListView = new ListView<>();
        fetchClientEvents(clientEventsListView);

        Label allEventsLabel = new Label("All Event Names and Dates");
        ListView<String> allEventsListView = new ListView<>();
        fetchAllEvents(allEventsListView);

        Label allUsersLabel = new Label("All User IDs");
        ListView<String> allUsersListView = new ListView<>();
        fetchAllUsers(allUsersListView);

        Button editServiceButton = new Button("Edit Service");
        editServiceButton.setOnAction(e -> {
            String selectedEvent = clientEventsListView.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                new EditServiceDialog(primaryStage, selectedEvent, userId, conn, this).show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No event selected.");
            }
        });

        root.getChildren().addAll(welcomeLabel, backButton, requestServiceLabel, requestServiceForm, myEventsLabel, clientEventsListView, allEventsLabel, allEventsListView, allUsersLabel, allUsersListView, editServiceButton);

        Scene scene = new Scene(root, 600, 780);
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

    private int getUserIdFromSession() {
        return 3;
    }

    private int fetchNextEventId() {
        String query = "SELECT MAX(event_id) AS max_event_id FROM Events";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("max_event_id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void requestService(int userId, String eventName, String eventDate) {
        try {
            String addEventQuery = "INSERT INTO Events (event_id, event_name, event_date, user_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(addEventQuery)) {
                stmt.setInt(1, nextEventId);
                stmt.setString(2, eventName);
                stmt.setString(3, eventDate);
                stmt.setInt(4, userId);
                stmt.executeUpdate();
            }

            String addBookingQuery = "INSERT INTO Bookings (event_id, user_id, status) VALUES (?, ?, 'Pending')";
            try (PreparedStatement stmt = conn.prepareStatement(addBookingQuery)) {
                stmt.setInt(1, nextEventId);
                stmt.setInt(2, userId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Booking request submitted successfully!");
                    nextEventId++;
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit booking request.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    public void fetchClientEvents(ListView<String> clientEventsListView) {
        String query = "SELECT b.booking_id, b.status, e.event_name, e.event_date " +
                       "FROM Bookings b " +
                       "JOIN Events e ON b.event_id = e.event_id " +
                       "WHERE b.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            List<String> events = new ArrayList<>();
            while (rs.next()) {
                events.add(rs.getString("event_name") + " on " + rs.getString("event_date") + " (Status: " + rs.getString("status") + ")");
            }
            clientEventsListView.getItems().setAll(events);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void fetchAllEvents(ListView<String> allEventsListView) {
        String query = "SELECT event_name, event_date FROM Events";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            List<String> events = new ArrayList<>();
            while (rs.next()) {
                events.add(rs.getString("event_name") + " on " + rs.getString("event_date"));
            }
            allEventsListView.getItems().setAll(events);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void fetchAllUsers(ListView<String> allUsersListView) {
        String query = "SELECT user_id, name FROM Users";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            List<String> users = new ArrayList<>();
            while (rs.next()) {
                users.add("User ID: " + rs.getInt("user_id") + " - " + rs.getString("name"));
            }
            allUsersListView.getItems().setAll(users);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    public ListView<String> getClientEventsListView() {
        return clientEventsListView;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
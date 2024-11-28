package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendorDashboard {

    private static final String url = "jdbc:mysql://localhost:3306/event_management_system";
    private static final String user = "root";
    private static final String password = "";

    private Connection conn;
    private int vendorId;
    private int nextEventId;
    private Stage primaryStage;
    private ListView<String> eventListView;

    public VendorDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        establishConnection();
        fetchVendorId();
        nextEventId = fetchNextEventId();
    }

    public Scene getScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Vendor Dashboard");
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> Main.showIndexPage());

        Label manageEventsLabel = new Label("Manage Events");
        TextField eventNameField = new TextField();
        eventNameField.setPromptText("Event Name");
        DatePicker eventDatePicker = new DatePicker();
        Button addEventButton = new Button("Add Event");
        addEventButton.setOnAction(e -> addEvent(eventNameField.getText(), eventDatePicker.getValue().toString()));

        VBox addEventForm = new VBox(10, eventNameField, eventDatePicker, addEventButton);
        addEventForm.setAlignment(Pos.CENTER);

        Label assignedEventsLabel = new Label("Assigned Events");
        eventListView = new ListView<>();
        fetchAssignedEvents(eventListView);

        Button editEventButton = new Button("Edit Event");
        editEventButton.setOnAction(e -> {
            String selectedEvent = eventListView.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                new EditEventDialog(primaryStage, selectedEvent, vendorId, conn, this).show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No event selected.");
            }
        });

        Button removeEventButton = new Button("Remove Event");
        removeEventButton.setOnAction(e -> {
            String selectedEvent = eventListView.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                deleteEvent(selectedEvent);
                fetchAssignedEvents(eventListView);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No event selected.");
            }
        });

        VBox eventActions = new VBox(10, editEventButton, removeEventButton);
        eventActions.setAlignment(Pos.CENTER);

        Label trackBookingsLabel = new Label("Track Bookings");
        ListView<String> bookingListView = new ListView<>();
        fetchBookings(bookingListView);

        root.getChildren().addAll(welcomeLabel, backButton, manageEventsLabel, addEventForm, assignedEventsLabel, eventListView, eventActions, trackBookingsLabel, bookingListView);

        Scene scene = new Scene(root, 600, 550);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        return scene;
    }

    public ListView<String> getEventListView() {
        return eventListView;
    }

    private void establishConnection() {
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchVendorId() {
        String query = "SELECT user_id AS vendor_id FROM Users WHERE user_id = ? AND role = 'Vendor'";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, getUserIdFromSession());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                vendorId = rs.getInt("vendor_id");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No vendor found for this user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private int getUserIdFromSession() {
        return 2;
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

    private void addEvent(String eventName, String eventDate) {
        try {
            String addEventQuery = "INSERT INTO Events (event_id, event_name, event_date, user_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(addEventQuery)) {
                stmt.setInt(1, nextEventId);
                stmt.setString(2, eventName);
                stmt.setString(3, eventDate);
                stmt.setInt(4, vendorId);
                stmt.executeUpdate();
            }

            String addBookingQuery = "INSERT INTO Bookings (event_id, user_id, status) VALUES (?, ?, 'Pending')";
            try (PreparedStatement stmt = conn.prepareStatement(addBookingQuery)) {
                stmt.setInt(1, nextEventId);
                stmt.setInt(2, vendorId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event and booking added successfully!");
                    nextEventId++;
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add booking.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    public void fetchAssignedEvents(ListView<String> eventListView) {
        String query = "SELECT * FROM Events WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vendorId);
            ResultSet rs = stmt.executeQuery();

            List<String> events = new ArrayList<>();
            while (rs.next()) {
                events.add(rs.getString("event_name") + " - " + rs.getString("event_date"));
            }
            eventListView.getItems().setAll(events);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void fetchBookings(ListView<String> bookingListView) {
        String query = "SELECT b.booking_id, b.status, e.event_name, u.name AS client_name " +
                       "FROM Bookings b " +
                       "JOIN Events e ON b.event_id = e.event_id " +
                       "JOIN Users u ON b.user_id = u.user_id " +
                       "WHERE e.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vendorId);
            ResultSet rs = stmt.executeQuery();

            List<String> bookings = new ArrayList<>();
            while (rs.next()) {
                bookings.add(rs.getString("event_name") + " - " + rs.getString("client_name") + " (" + rs.getString("status") + ")");
            }
            bookingListView.getItems().setAll(bookings);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void deleteEvent(String selectedEvent) {
        String[] eventDetails = selectedEvent.split(" - ");
        String eventName = eventDetails[0];
        String eventDate = eventDetails[1];

        String query = "DELETE FROM Events WHERE event_name = ? AND event_date = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, eventName);
            stmt.setString(2, eventDate);
            stmt.setInt(3, vendorId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event.");
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
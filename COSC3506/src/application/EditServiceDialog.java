package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditServiceDialog {

    private Stage dialogStage;
    private String selectedEvent;
    private int userId;
    private Connection conn;
    private ClientDashboard clientDashboard;

    public EditServiceDialog(Stage owner, String selectedEvent, int userId, Connection conn, ClientDashboard clientDashboard) {
        this.selectedEvent = selectedEvent;
        this.userId = userId;
        this.conn = conn;
        this.clientDashboard = clientDashboard;

        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle("Edit Service");

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        if (selectedEvent == null || !selectedEvent.contains(" on ") || !selectedEvent.contains(" (Status: ")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid event format.");
            return;
        }

        String[] eventDetails = selectedEvent.split(" on ");
        String eventName = eventDetails[0];
        String eventDate = eventDetails[1].split(" \\(Status: ")[0];
        String status = eventDetails[1].split(" \\(Status: ")[1].replace(")", "");

        TextField eventNameField = new TextField(eventName);
        DatePicker eventDatePicker = new DatePicker();
        eventDatePicker.setPromptText(eventDate);
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Pending", "Confirmed");
        statusComboBox.setValue(status);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String newEventName = eventNameField.getText().trim();
            String newEventDate = eventDatePicker.getValue() != null ? eventDatePicker.getValue().toString() : eventDate;
            saveChanges(newEventName, newEventDate, statusComboBox.getValue());
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteEvent(eventName, eventDate));

        root.getChildren().addAll(new Label("Edit Event"), eventNameField, eventDatePicker, statusComboBox, saveButton, deleteButton);

        Scene scene = new Scene(root, 300, 250);
        dialogStage.setScene(scene);
    }

    public void show() {
        dialogStage.showAndWait();
    }

    private void saveChanges(String eventName, String eventDate, String status) {
        String query = "UPDATE Events e " +
                       "JOIN Bookings b ON e.event_id = b.event_id " +
                       "SET e.event_name = ?, e.event_date = ?, b.status = ? " +
                       "WHERE e.event_name = ? AND e.event_date = ? AND b.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, eventName);
            stmt.setString(2, eventDate);
            stmt.setString(3, status);
            stmt.setString(4, selectedEvent.split(" on ")[0]);
            stmt.setString(5, selectedEvent.split(" on ")[1].split(" \\(Status: ")[0]);
            stmt.setInt(6, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event details updated successfully.");
                clientDashboard.fetchClientEvents(clientDashboard.getClientEventsListView());
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event details.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }

    private void deleteEvent(String eventName, String eventDate) {
        String query = "DELETE b, e FROM Bookings b " +
                       "JOIN Events e ON b.event_id = e.event_id " +
                       "WHERE e.event_name = ? AND e.event_date = ? AND b.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, eventName);
            stmt.setString(2, eventDate);
            stmt.setInt(3, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
                clientDashboard.fetchClientEvents(clientDashboard.getClientEventsListView());
                dialogStage.close();
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
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

public class EditEventDialog {

    private Stage dialogStage;
    private String selectedEvent;
    private int vendorId;
    private Connection conn;
    private VendorDashboard vendorDashboard;

    public EditEventDialog(Stage owner, String selectedEvent, int vendorId, Connection conn, VendorDashboard vendorDashboard) {
        this.selectedEvent = selectedEvent;
        this.vendorId = vendorId;
        this.conn = conn;
        this.vendorDashboard = vendorDashboard;

        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle("Edit Event");

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        if (selectedEvent == null || !selectedEvent.contains(" - ")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid event format.");
            return;
        }

        String[] eventDetails = selectedEvent.split(" - ");
        String eventName = eventDetails[0];
        String eventDate = eventDetails[1];

        TextField userIdField = new TextField(String.valueOf(vendorId));
        TextField eventNameField = new TextField(eventName);
        DatePicker eventDatePicker = new DatePicker();
        eventDatePicker.setPromptText(eventDate);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String newUserId = userIdField.getText().trim();
            String newEventName = eventNameField.getText().trim();
            String newEventDate = eventDatePicker.getValue() != null ? eventDatePicker.getValue().toString() : eventDate;
            saveChanges(newUserId, newEventName, newEventDate);
        });

        root.getChildren().addAll(new Label("Edit Event"), new Label("User ID (Admin 1, Vendor 2, Client 3)"), userIdField, new Label("Event Name"), eventNameField, new Label("Event Date"), eventDatePicker, saveButton);

        Scene scene = new Scene(root, 300, 250);
        dialogStage.setScene(scene);
    }

    public void show() {
        dialogStage.showAndWait();
    }

    private void saveChanges(String userId, String eventName, String eventDate) {
        String query = "UPDATE Events SET user_id = ?, event_name = ?, event_date = ? WHERE event_name = ? AND event_date = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setString(2, eventName);
            stmt.setString(3, eventDate);
            stmt.setString(4, selectedEvent.split(" - ")[0]);
            stmt.setString(5, selectedEvent.split(" - ")[1]);
            stmt.setInt(6, vendorId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event details updated successfully.");
                vendorDashboard.fetchAssignedEvents(vendorDashboard.getEventListView());
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event details.");
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
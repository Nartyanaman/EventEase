package application;

import java.sql.*;

public class UserOperations {
    private static final String url = "jdbc:mysql://localhost:3306/event_management_system";
    private static final String user = "root";
    private static final String password = "";

    public static void logEvent(int userId, String action, int eventId) {
        String query = "INSERT INTO event_logs (user_id, action, event_id, timestamp) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setInt(3, eventId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging event: " + e.getMessage());
        }
    }

public static void addUser(String name, String email, String password) {
    String query = "INSERT INTO Users (name, email, password) VALUES (?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(url, user, password);
         PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.setString(3, password);

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    logEvent(userId, "Admin Created User", 0);
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("Error: " + e.getMessage());
    }
}

public static void updateUser(int userId, String name, String email) {
    String query = "UPDATE Users SET name = ?, email = ? WHERE user_id = ?";
    try (Connection conn = DriverManager.getConnection(url, user, password);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.setInt(3, userId);

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            logEvent(userId, "Admin Updated User", 0); 
        }
    } catch (SQLException e) {
        System.err.println("Error: " + e.getMessage()); 
    }
}

public static void deleteUser(int userId) {
    String query = "DELETE FROM Users WHERE user_id = ?";
    try (Connection conn = DriverManager.getConnection(url, user, password);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            logEvent(userId, "Admin Deleted User", 0);
        }
    } catch (SQLException e) {
        System.err.println("Error: " + e.getMessage());
    }
  }
}
package application;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        showIndexPage();
    }

    public static void showIndexPage() {
        primaryStage.setScene(IndexPage.getScene());
        primaryStage.getScene().getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
        primaryStage.setTitle("Event Management System - Index");
        primaryStage.show();
    }

    public static void showLoginPage() {
        primaryStage.setScene(LoginPage.getScene());
        primaryStage.getScene().getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
        primaryStage.setTitle("Event Management System - Login");
        primaryStage.show();
    }

    public static void showDashboard(String role) {
        try {
            if ("admin".equalsIgnoreCase(role)) {
                AdminDashboard adminDashboard = new AdminDashboard(primaryStage);
                primaryStage.setScene(adminDashboard.getScene());
                primaryStage.getScene().getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
                primaryStage.setTitle("Event Management System - Admin Dashboard");
            } else if ("vendor".equalsIgnoreCase(role)) {
                VendorDashboard vendorDashboard = new VendorDashboard(primaryStage);
                primaryStage.setScene(vendorDashboard.getScene());
                primaryStage.getScene().getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
                primaryStage.setTitle("Event Management System - Vendor Dashboard");
            } else if ("client".equalsIgnoreCase(role)) {
                ClientDashboard clientDashboard = new ClientDashboard(primaryStage);
                primaryStage.setScene(clientDashboard.getScene());
                primaryStage.getScene().getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
                primaryStage.setTitle("Event Management System - Client Dashboard");
            } else {
                System.err.println("Unknown role: " + role);
                showIndexPage();
            }
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
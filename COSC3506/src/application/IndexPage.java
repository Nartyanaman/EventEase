package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class IndexPage {
    public static Scene getScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Button adminButton = new Button("Admin Dashboard");
        adminButton.setOnAction(e -> Main.showLoginPage());

        Button vendorButton = new Button("Vendor Dashboard");
        vendorButton.setOnAction(e -> Main.showLoginPage());

        Button clientButton = new Button("Client Dashboard");
        clientButton.setOnAction(e -> Main.showLoginPage());               

        root.getChildren().addAll(adminButton, vendorButton, clientButton);
        
        Label creditsLabel = new Label("Credits: Cooper, Joanna, Arnav, and Nartya");
        root.getChildren().add(creditsLabel);

        return new Scene(root, 400, 300);
    }
}
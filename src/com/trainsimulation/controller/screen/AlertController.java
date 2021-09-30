package com.trainsimulation.controller.screen;

import javafx.scene.control.Alert;

public class AlertController extends ScreenController {
    // Display a simple OK-activated alert box
    public static void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
}

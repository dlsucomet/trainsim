package com.crowdsimulation.controller.controls.alert;

import com.crowdsimulation.controller.controls.ScreenController;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertController extends ScreenController {
    // Display a simple OK-activated alert box
    public static void showSimpleAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }

    // Display a simple yes or no confirmation alert box
    public static boolean showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();

        return result.get() == ButtonType.OK;
    }

    @Override
    public void setElements() {

    }

    @Override
    protected void closeAction() {

    }
}

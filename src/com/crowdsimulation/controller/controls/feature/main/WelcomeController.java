package com.crowdsimulation.controller.controls.feature.main;

import com.crowdsimulation.controller.controls.ScreenController;
import com.crowdsimulation.controller.controls.service.main.InitializeWelcomeService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

public class WelcomeController extends ScreenController {
    public static final String[] OUTPUT_KEYS = {"blank_station", "rows", "columns"};

    @FXML
    Label rowLabel;

    @FXML
    Spinner<Integer> rowSpinner;

    @FXML
    Label columnLabel;

    @FXML
    Spinner<Integer> columnSpinner;

    @FXML
    Button createBlankStationButton;

    @FXML
    Button loadStationButton;

    @Override
    public void setElements() {
        InitializeWelcomeService.initializeWelcomeService(
                rowLabel,
                rowSpinner,
                columnLabel,
                columnSpinner,
                createBlankStationButton,
                loadStationButton
        );
    }

    @FXML
    public void createBlankStationAction() {
        Stage stage = (Stage) rowLabel.getScene().getWindow();

        this.getWindowOutput().put(OUTPUT_KEYS[0], true);
        this.getWindowOutput().put(OUTPUT_KEYS[1], rowSpinner.getValue());
        this.getWindowOutput().put(OUTPUT_KEYS[2], columnSpinner.getValue());

        // Close the window
        this.setClosedWithAction(true);
        stage.close();
    }

    @FXML
    public void loadStationAction() {
        Stage stage = (Stage) rowLabel.getScene().getWindow();

        this.getWindowOutput().put(OUTPUT_KEYS[0], false);

        // Close the window
        this.setClosedWithAction(true);
        stage.close();
    }

    @Override
    protected void closeAction() {
    }
}

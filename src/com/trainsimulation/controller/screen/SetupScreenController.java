package com.trainsimulation.controller.screen;

import com.jfoenix.controls.JFXTimePicker;
import com.trainsimulation.controller.Main;
import com.trainsimulation.model.simulator.SimulationTime;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.time.LocalTime;

public class SetupScreenController extends ScreenController {
    @FXML
    private JFXTimePicker startTimePicker;

    @FXML
    private JFXTimePicker endTimePicker;

    @FXML
    private Button setupButton;

    @FXML
    public void initialize() {
        // Set spinners
        final int startHourDefault = 4;
        final int endHourDefault = 22;

        final int startMinuteDefault = 30;
        final int endMinuteDefault = 30;

        startTimePicker.set24HourView(true);
        endTimePicker.set24HourView(true);

        startTimePicker.setValue(LocalTime.of(startHourDefault, startMinuteDefault));
        endTimePicker.setValue(LocalTime.of(endHourDefault, endMinuteDefault));
    }

    @FXML
    public void setupAction() {
        Stage stage = (Stage) setupButton.getScene().getWindow();

        // Set the simulation up
        LocalTime startTime = startTimePicker.getValue();
        LocalTime endTime = endTimePicker.getValue();

        SimulationTime startSimulationTime = new SimulationTime(startTime);
        SimulationTime endSimulationTime = new SimulationTime(endTime);

        Main.simulator.setup(startSimulationTime, endSimulationTime);

        // Signal that the button is closed from the set up button
        this.setClosedWithAction(true);

        // Close the window
        stage.close();
    }
}
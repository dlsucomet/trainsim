package com.trainsimulation.controller.context;

import com.crowdsimulation.model.core.environment.station.Floor;
import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

// Used to store the basic states of a simulation window to be presented on the screen
public class SimulationContext {
    private final Tab simulationWindow;

    private final TrainSystem trainSystem;

    private final double lineScaleDownFactor;
    private final double stationScaleDownFactor;

    public int stationIndex;
    private Station currentStation;

    private Button firstStationButton;
    private Button previousStationButton;

    private Text currentStationText;

    private Button floorBelowButton;
    private Button floorAboveButton;

    private Button nextStationButton;
    private Button lastStationButton;

    private List<Floor> floors;
    private int floorIndex;

    public SimulationContext(
            Tab simulationWindow,
            TrainSystem trainSystem,
            double lineScaleDownFactor,
            double stationScaleDownFactor
    ) {
        this.simulationWindow = simulationWindow;
        this.trainSystem = trainSystem;
        this.lineScaleDownFactor = lineScaleDownFactor;
        this.stationScaleDownFactor = stationScaleDownFactor;
        this.stationIndex = 0;

        this.currentStation = this.trainSystem.getStations().get(this.stationIndex);

//        // TODO: Enable for other train systems and stations
//        if (this.currentStation.getName().equals("Recto")) {
//            this.floors = this.currentStation.getStationLayout().getFloors();
//        } else {
//            this.floors = null;
//        }

        this.floorIndex = 0;
    }

    public TrainSystem getTrainSystem() {
        return trainSystem;
    }

    public double getLineScaleDownFactor() {
        return lineScaleDownFactor;
    }

    public double getStationScaleDownFactor() {
        return stationScaleDownFactor;
    }

    public StackPane getLineViewCanvases() {
        VBox viewContainer = (VBox) ((BorderPane) this.simulationWindow.getContent()).getCenter();

        return (StackPane) viewContainer.getChildren().get(0);
    }

    public StackPane getStationViewCanvases() {
        VBox viewContainer = (VBox) ((BorderPane) this.simulationWindow.getContent()).getCenter();
        VBox stationViewContainer = (VBox) viewContainer.getChildren().get(2);

        ScrollPane scrollPane = (ScrollPane) stationViewContainer.getChildren().get(1);

        return (StackPane) scrollPane.getContent();

//        return (StackPane) ((VBox) viewContainer.getChildren().get(2)).getChildren().get(1);
    }

    public Station getCurrentStation() {
        return this.currentStation;
    }

    public void setFirstStationButton(Button firstStationButton) {
        this.firstStationButton = firstStationButton;
    }

    public void setPreviousStationButton(Button previousStationButton) {
        this.previousStationButton = previousStationButton;
    }

    public Text getCurrentStationText() {
        return currentStationText;
    }

    public void setCurrentStationText(Text currentStationText) {
        this.currentStationText = currentStationText;

        currentStationText.setText(this.currentStation.getName());
    }

    public void setFloorBelowButton(Button floorBelowButton) {
        this.floorBelowButton = floorBelowButton;
    }

    public void setFloorAboveButton(Button floorAboveButton) {
        this.floorAboveButton = floorAboveButton;
    }

    public void setNextStationButton(Button nextStationButton) {
        this.nextStationButton = nextStationButton;
    }

    public void setLastStationButton(Button lastStationButton) {
        this.lastStationButton = lastStationButton;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public Floor getFloor() {
        return floors.get(this.floorIndex);
    }

    public void configureStationButtonsDisabled() {
        // Current station is first station
        if (this.stationIndex == 0) {
            this.previousStationButton.setDisable(true);
            this.nextStationButton.setDisable(false);
        } else if (this.stationIndex == this.trainSystem.getStations().size() - 1) {
            // Current station is the last station
            this.previousStationButton.setDisable(false);
            this.nextStationButton.setDisable(true);
        } else {
            // Current station is somewhere between the first and last stations
            this.previousStationButton.setDisable(false);
            this.nextStationButton.setDisable(false);
        }

        // Current floor is first floor
        if (this.floorIndex == 0) {
            this.floorBelowButton.setDisable(true);
            this.floorAboveButton.setDisable(false);
        } else if (this.floorIndex == this.floors.size() - 1) {
            // Current floor is last floor
            this.floorBelowButton.setDisable(false);
            this.floorAboveButton.setDisable(true);
        } else {
            // Current floor is somewhere between the first and last floors
            this.floorBelowButton.setDisable(false);
            this.floorAboveButton.setDisable(false);
        }
    }

    public void moveToFirstStation() {
        this.stationIndex = 0;
        this.currentStation = this.trainSystem.getStations().get(this.stationIndex);

        // Change floors
        // TODO: Enable for other train systems and stations
//        if (this.currentStation.getTrainSystem().getTrainSystemInformation().getName().equals("MRT-3")) {
        this.floors = this.currentStation.getStationLayout().getFloors();
        this.floorIndex = this.floors.size() - 1;
//        } else {
//            this.floors = null;
//            this.floorIndex = 0;
//        }

        // Change labels
        this.currentStationText.setText(this.currentStation.getName());
        this.configureStationButtonsDisabled();
    }

    public void moveToPreviousStation() {
        // Change the station
        this.stationIndex--;
        this.currentStation = this.trainSystem.getStations().get(this.stationIndex);

        // Change floors
//        // TODO: Enable for other train systems and stations
//        if (this.currentStation.getTrainSystem().getTrainSystemInformation().getName().equals("MRT-3")) {
        this.floors = this.currentStation.getStationLayout().getFloors();
        this.floorIndex = this.floors.size() - 1;
//        } else {
//            this.floors = null;
//            this.floorIndex = 0;
//        }

        // Change labels
        this.currentStationText.setText(this.currentStation.getName());
        this.configureStationButtonsDisabled();
    }

    public void moveToFloorBelow() {
        // Change the floor
        this.floorIndex--;

        // Change labels
        this.configureStationButtonsDisabled();
    }

    public void moveToFloorAbove() {
        // Change the floor
        this.floorIndex++;

        // Change labels
        this.configureStationButtonsDisabled();
    }

    public void moveToNextStation() {
        // Change the station
        this.stationIndex++;
        this.currentStation = this.trainSystem.getStations().get(this.stationIndex);

        // TODO: Enable for other train systems and stations
//        if (this.currentStation.getTrainSystem().getTrainSystemInformation().getName().equals("MRT-3")) {
        this.floors = this.currentStation.getStationLayout().getFloors();
        this.floorIndex = this.floors.size() - 1;
//        } else {
//            this.floors = null;
//            this.floorIndex = 0;
//        }

        // Change labels
        this.currentStationText.setText(this.currentStation.getName());
        this.configureStationButtonsDisabled();
    }

    public void moveToLastStation() {
        this.stationIndex = this.trainSystem.getStations().size() - 1;
        this.currentStation = this.trainSystem.getStations().get(this.stationIndex);

        // Change floors
        // TODO: Enable for other train systems and stations
//        if (this.currentStation.getTrainSystem().getTrainSystemInformation().getName().equals("MRT-3")) {
        this.floors = this.currentStation.getStationLayout().getFloors();
        this.floorIndex = this.floors.size() - 1;
//        } else {
//            this.floors = null;
//            this.floorIndex = 0;
//        }

        // Change labels
        this.currentStationText.setText(this.currentStation.getName());
        this.configureStationButtonsDisabled();
    }
}

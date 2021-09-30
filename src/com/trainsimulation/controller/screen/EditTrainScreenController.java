package com.trainsimulation.controller.screen;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.property.PassengerServiceProperty;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.property.StationProperty;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EditTrainScreenController extends ScreenController {
    public static final String[] INPUT_KEYS = {"train", "stations"};
    public static final String OUTPUT_KEY = "train";

    @FXML
    public JFXListView<StationProperty> stationsList;

    @FXML
    public JFXButton selectAllButton;

    @FXML
    public JFXButton clearAllButton;

    @FXML
    private JFXButton applyChangesButton;

    @FXML
    private JFXButton removeTrainButton;

    @FXML
    private Text routeText;

    @FXML
    private Text removeText;

    // Take note of the train to be edited
    private Train train;

    // Take note of the stations to be displayed
    private List<Station> stationChoices;

    public void setElements() {
        // Set the choice variables first
        this.train = (Train) this.getWindowInput().get(EditTrainScreenController.INPUT_KEYS[0]);
        this.stationChoices = (List<Station>) this.getWindowInput().get(EditTrainScreenController.INPUT_KEYS[1]);

        // Enable all elements
        stationsList.setDisable(false);
        selectAllButton.setDisable(false);
        clearAllButton.setDisable(false);
        removeTrainButton.setDisable(false);

        // Set their necessary content
        final String routePrompt
                = "Select at least two stations which should be serviced by this train. Use the Shift and Ctrl keys to" +
                " select multiple stations at once.";
        routeText.setText(routePrompt);

        final String removePrompt
                = "Removing this train will make it stop at the next station (if it isn't currently in one) to allow" +
                " all of its passengers to disembark. Afterwards, it shall return to its depot.";
        removeText.setText(removePrompt);

        // Prepare the station list
        List<StationProperty> stationChoiceList = new ArrayList<>();

        for (Station station : this.stationChoices) {
            stationChoiceList.add(station.getStationProperty());
        }

        ObservableList<StationProperty> stationProperties
                = PassengerServiceProperty.toObservablePropertyList(stationChoiceList);

        stationsList.setItems(stationProperties);

        stationsList.setCellFactory(param -> new JFXListCell<StationProperty>() {
            @Override
            protected void updateItem(StationProperty item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getStationName() == null) {
                    setText(null);
                } else {
                    setText(item.getStationName());
                }
            }
        });

        stationsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Prepare the bindings
        final int minimumSelectedStationsSize = 2;

        IntegerBinding selectedItemsSizeProperty = Bindings.size(stationsList.getSelectionModel().getSelectedItems());
        BooleanBinding selectedItemsNotEnoughProperty = selectedItemsSizeProperty.lessThan(minimumSelectedStationsSize);

        applyChangesButton.disableProperty().bind(selectedItemsNotEnoughProperty);
    }

    @FXML
    public void applyChangesAction() {
        Stage stage = (Stage) applyChangesButton.getScene().getWindow();

        // Get the selected values
        List<StationProperty> selectedStations = stationsList.getSelectionModel().getSelectedItems();

        List<Station> chosenStations = new ArrayList<>();

        for (StationProperty stationProperty : selectedStations) {
            chosenStations.add((Station) stationProperty.getOwner());
        }

        // Check if the train is allowed to be edited at this point
        if (this.train.getTrainMovement().isEditable()) {
            this.train.getTrainMovement().getStationQueue().setNewStations(
                    chosenStations,
                    !this.train.getTrainMovement().isTowardsNearEnd()
            );

            this.train.getTrainMovement().setStationListEdited(true);

            // Set the return value of this controller - the train selected with its chosen stations
            this.getWindowOutput().put(OUTPUT_KEY, this.train);

            // Signal that the button is be closed from the apply button
            this.setClosedWithAction(true);

            // Close the window
            stage.close();
        } else {
            AlertController.showAlert(
                    "Unable to set route",
                    "Unable to set route",
                    "Train routes may only be set at the ends of their routes while they are preparing to" +
                            " switch directions.",
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    public void removeTrainAction() {
        Stage stage = (Stage) removeTrainButton.getScene().getWindow();

        // Check if the train is allowed to deactivate at this point
        if (this.train.getTrainMovement().isEditable()) {
            this.train.getTrainMovement().setActive(false);

            // Set the return value of this controller - the train deactivated
            this.getWindowOutput().put(OUTPUT_KEY, this.train);

            // Signal that the button is be closed from the remove button
            this.setClosedWithAction(true);

            // Close the window
            stage.close();
        } else {
            AlertController.showAlert(
                    "Unable to remove train",
                    "Unable to remove train",
                    "Trains may only be removed at the ends of their routes while they are preparing to switch" +
                            " directions.",
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    public void selectAllAction() {
        stationsList.getSelectionModel().selectAll();
    }

    @FXML
    public void clearAllAction() {
        stationsList.getSelectionModel().clearSelection();
    }
}

package com.trainsimulation.controller.screen;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.property.PassengerServiceProperty;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.property.StationProperty;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.property.TrainProperty;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class InsertTrainScreenController extends ScreenController {
    public static final String[] INPUT_KEYS = {"trains", "stations"};
    public static final String OUTPUT_KEY = "train";

    @FXML
    public JFXButton insertTrainButton;

    @FXML
    public TableView<TrainProperty> inactiveTrainsTable;

    @FXML
    public TableColumn<TrainProperty, String> trainNumberColumn;

    @FXML
    public TableColumn<TrainProperty, String> numberOfCarriagesColumn;

    @FXML
    public TableColumn<TrainProperty, String> carriageClassNameColumn;

    @FXML
    public TableColumn<TrainProperty, String> totalPassengerCapacityColumn;

    @FXML
    public JFXListView<StationProperty> stationsList;

    @FXML
    public JFXButton selectAllButton;

    @FXML
    public JFXButton clearAllButton;

    // Take note of the inactive trains to be displayed
    private List<Train> trainChoices;

    // Take note of the stations to be displayed
    private List<Station> stationChoices;

    public void updateTrainChoices() {
        this.trainChoices = (List<Train>) this.getWindowInput().get(InsertTrainScreenController.INPUT_KEYS[0]);
    }

    public void setElements() {
        // Set the choice variables first
        updateTrainChoices();

        this.stationChoices = (List<Station>) this.getWindowInput().get(InsertTrainScreenController.INPUT_KEYS[1]);

        // Prepare the train table
        List<TrainProperty> inactiveTrainPropertyList = new ArrayList<>();

        for (Train train : this.trainChoices) {
            inactiveTrainPropertyList.add(train.getTrainProperty());
        }

        inactiveTrainsTable.setItems(PassengerServiceProperty.toObservablePropertyList(inactiveTrainPropertyList));

        // Prepare the table columns
        trainNumberColumn.setCellValueFactory(trainNumber -> trainNumber.getValue().trainNumberProperty());
        numberOfCarriagesColumn.setCellValueFactory(numberOfCarriages -> numberOfCarriages.getValue()
                .numberOfCarriagesProperty());
        carriageClassNameColumn.setCellValueFactory(carriageClassName -> carriageClassName.getValue()
                .carriageClassNameProperty());
        totalPassengerCapacityColumn.setCellValueFactory(totalPassengerCapacity -> totalPassengerCapacity.getValue()
                .totalPassengerCapacity());

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

        insertTrainButton.disableProperty().bind(
                selectedItemsNotEnoughProperty.or(
                        Bindings.isEmpty(inactiveTrainsTable.getSelectionModel().getSelectedItems())
                )
        );
    }

    @FXML
    public void insertTrainAction() {
        Stage stage = (Stage) insertTrainButton.getScene().getWindow();

        // Get the selected values
        Train selectedTrain = (Train) inactiveTrainsTable.getSelectionModel().getSelectedItem().getOwner();
        List<StationProperty> selectedStations = stationsList.getSelectionModel().getSelectedItems();

        List<Station> chosenStations = new ArrayList<>();

        for (StationProperty stationProperty : selectedStations) {
            chosenStations.add((Station) stationProperty.getOwner());
        }

        selectedTrain.getTrainMovement().getStationQueue().setNewStations(
                chosenStations,
                selectedTrain.getTrainMovement().isTowardsNearEnd()
        );

        // Set the return value of this controller - the train selected with its chosen stations
        this.getWindowOutput().put(OUTPUT_KEY, selectedTrain);

        // Signal that the button is be closed from the insert button
        this.setClosedWithAction(true);

        // Close the window
        stage.close();
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

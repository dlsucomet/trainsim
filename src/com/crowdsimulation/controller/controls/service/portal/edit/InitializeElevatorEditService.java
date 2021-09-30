package com.crowdsimulation.controller.controls.service.portal.edit;

import com.crowdsimulation.controller.controls.service.InitializeScreenService;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorShaft;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class InitializeElevatorEditService extends InitializeScreenService {
    public static void initializeElevatorEdit(
            Text promptText,
            CheckBox elevatorEnableCheckbox,
            Label elevatorDelayLabel,
            Spinner<Integer> elevatorDelaySpinner,
            Label elevatorOpenLabel,
            Spinner<Integer> elevatorOpenSpinner,
            Label elevatorMoveLabel,
            Spinner<Integer> elevatorMoveSpinner,
            Label elevatorDirectionLabel,
            ChoiceBox<ElevatorShaft.ElevatorDirection> elevatorDirectionChoiceBox,
            Label elevatorCapacityLabel,
            Spinner<Integer> elevatorCapacitySpinner,
            Button proceedButton
    ) {
        // Set elements
        elevatorDelayLabel.setLabelFor(elevatorDelaySpinner);
        elevatorDelaySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        5
                )
        );

        elevatorOpenLabel.setLabelFor(elevatorOpenSpinner);
        elevatorOpenSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        5,
                        30
                )
        );

        elevatorMoveLabel.setLabelFor(elevatorMoveSpinner);
        elevatorMoveSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        5,
                        30
                )
        );

        elevatorDirectionLabel.setLabelFor(elevatorDirectionChoiceBox);

        elevatorDirectionChoiceBox.setItems(FXCollections.observableArrayList(
                ElevatorShaft.ElevatorDirection.UP,
                ElevatorShaft.ElevatorDirection.DOWN
        ));
        elevatorDirectionChoiceBox.getSelectionModel().select(0);

        elevatorCapacityLabel.setLabelFor(elevatorCapacitySpinner);
        elevatorCapacitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        5,
                        30
                )
        );
    }
}

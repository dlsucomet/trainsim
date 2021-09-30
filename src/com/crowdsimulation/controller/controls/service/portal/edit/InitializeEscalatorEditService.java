package com.crowdsimulation.controller.controls.service.portal.edit;

import com.crowdsimulation.controller.controls.service.InitializeScreenService;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorShaft;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class InitializeEscalatorEditService extends InitializeScreenService {
    public static void initializeEscalatorEdit(
            Text promptText,
            CheckBox escalatorEnableCheckbox,
            Label escalatorMoveLabel,
            Spinner<Integer> escalatorMoveSpinner,
            Label escalatorDirectionLabel,
            ChoiceBox<EscalatorShaft.EscalatorDirection> escalatorDirectionChoiceBox,
            Label escalatorCapacityLabel,
            Spinner<Integer> escalatorCapacitySpinner,
            Button proceedButton
    ) {
        // Set elements
        escalatorMoveLabel.setLabelFor(escalatorMoveSpinner);
        escalatorMoveSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        10,
                        60
                )
        );

        escalatorDirectionLabel.setLabelFor(escalatorDirectionChoiceBox);

        escalatorDirectionChoiceBox.setItems(FXCollections.observableArrayList(
                EscalatorShaft.EscalatorDirection.UP,
                EscalatorShaft.EscalatorDirection.DOWN
        ));
        escalatorDirectionChoiceBox.getSelectionModel().select(0);

        escalatorCapacityLabel.setLabelFor(escalatorCapacitySpinner);
        escalatorCapacitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        10,
                        200
                )
        );
    }
}

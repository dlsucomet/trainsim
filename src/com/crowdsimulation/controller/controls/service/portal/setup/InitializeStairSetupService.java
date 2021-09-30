package com.crowdsimulation.controller.controls.service.portal.setup;

import com.crowdsimulation.controller.controls.service.InitializeScreenService;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class InitializeStairSetupService extends InitializeScreenService {
    public static void initializeStairSetup(
            Text promptText,
            CheckBox stairEnableCheckBox,
            Label stairMoveLabel,
            Spinner<Integer> stairMoveSpinner,
            Label stairCapacityLabel,
            Spinner<Integer> stairCapacitySpinner,
            Button proceedButton
    ) {
        // Set elements
        stairMoveLabel.setLabelFor(stairMoveSpinner);
        stairMoveSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        10,
                        60
                )
        );

        stairCapacityLabel.setLabelFor(stairCapacitySpinner);
        stairCapacitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        10,
                        200
                )
        );
    }
}

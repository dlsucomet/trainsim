package com.crowdsimulation.controller.controls.feature.portal.edit;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.controls.service.portal.edit.InitializeElevatorEditService;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorShaft;
import com.crowdsimulation.model.simulator.Simulator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ElevatorEditController extends PortalEditController {
    public static final String OUTPUT_KEY = "elevator_shaft";

    @FXML
    private Text promptText;

    @FXML
    private CheckBox elevatorEnableCheckbox;

    @FXML
    private Label elevatorDelayLabel;

    @FXML
    private Spinner<Integer> elevatorDelaySpinner;

    @FXML
    private Label elevatorOpenLabel;

    @FXML
    private Spinner<Integer> elevatorOpenSpinner;

    @FXML
    private Label elevatorMoveLabel;

    @FXML
    private Spinner<Integer> elevatorMoveSpinner;

    @FXML
    private Label elevatorDirectionLabel;

    @FXML
    private ChoiceBox<ElevatorShaft.ElevatorDirection> elevatorDirectionChoiceBox;

    @FXML
    private Label elevatorCapacityLabel;

    @FXML
    private Spinner<Integer> elevatorCapacitySpinner;

    @FXML
    private Button proceedButton;

    private ElevatorShaft elevatorShaft;

    @FXML
    public void proceedAction() {
        Stage stage = (Stage) proceedButton.getScene().getWindow();

        // Take note of the values in the form
        boolean enabled = elevatorEnableCheckbox.isSelected();
        int delayTime = elevatorDelaySpinner.getValue();
        int openTime = elevatorOpenSpinner.getValue();
        int moveTime = elevatorMoveSpinner.getValue();
        ElevatorShaft.ElevatorDirection elevatorDirection = elevatorDirectionChoiceBox.getValue();
        int capacity = elevatorCapacitySpinner.getValue();

        // Modify its values
        ElevatorShaft.elevatorEditor.edit(
                elevatorShaft,
                enabled,
                delayTime,
                openTime,
                moveTime,
                elevatorDirection,
                capacity
        );

        this.getWindowOutput().put(OUTPUT_KEY, elevatorShaft);

        // Close the window
        this.setClosedWithAction(true);
        stage.close();
    }

    @Override
    public void setElements() {
        InitializeElevatorEditService.initializeElevatorEdit(
                promptText,
                elevatorEnableCheckbox,
                elevatorDelayLabel,
                elevatorDelaySpinner,
                elevatorOpenLabel,
                elevatorOpenSpinner,
                elevatorMoveLabel,
                elevatorMoveSpinner,
                elevatorDirectionLabel,
                elevatorDirectionChoiceBox,
                elevatorCapacityLabel,
                elevatorCapacitySpinner,
                proceedButton
        );

        boolean editingOne = Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE;

        // Set the proper prompt text
        String promptString = promptText.getText();
        promptString = promptString.replace(
                "%s",
                editingOne ? "" : "s"
        );
        promptText.setText(promptString);

        // If editing one, use the selected elevator as a reference
        // If editing all, create a temporary reference portal shaft
        if (editingOne) {
            // Extract the elevator shaft from the selected elevator portal
            this.elevatorShaft
                    = ((ElevatorPortal) Main.simulator.getCurrentAmenity()).getElevatorShaft();

            elevatorEnableCheckbox.setSelected(this.elevatorShaft.isEnabled());
            elevatorDelaySpinner.getValueFactory().setValue(this.elevatorShaft.getOpenDelayTime());
            elevatorOpenSpinner.getValueFactory().setValue(this.elevatorShaft.getDoorOpenTime());
            elevatorMoveSpinner.getValueFactory().setValue(this.elevatorShaft.getMoveTime());
            elevatorDirectionChoiceBox.setValue(this.elevatorShaft.getElevatorDirection());
            elevatorCapacitySpinner.getValueFactory().setValue(this.elevatorShaft.getCapacity());
        } else {
            // Create a dummy elevator shaft
            ElevatorShaft.ElevatorShaftFactory elevatorShaftFactory = new ElevatorShaft.ElevatorShaftFactory();

            this.elevatorShaft = elevatorShaftFactory.create(
                    false,
                    -1,
                    -1,
                    -1,
                    null,
                    -1
            );
        }
    }

    @Override
    protected void closeAction() {

    }
}

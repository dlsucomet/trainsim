package com.crowdsimulation.controller.controls.feature.portal.edit;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.controls.service.portal.edit.InitializeStairEditService;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairShaft;
import com.crowdsimulation.model.simulator.Simulator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class StairEditController extends PortalEditController {
    public static final String OUTPUT_KEY = "stair_shaft";

    @FXML
    private Text promptText;

    @FXML
    private CheckBox stairEnableCheckBox;

    @FXML
    private Label stairMoveLabel;

    @FXML
    private Spinner<Integer> stairMoveSpinner;

    @FXML
    private Label stairCapacityLabel;

    @FXML
    private Spinner<Integer> stairCapacitySpinner;

    @FXML
    private Button proceedButton;

    private StairShaft stairShaft;

    @FXML
    public void proceedAction() {
        Stage stage = (Stage) proceedButton.getScene().getWindow();

        // Take note of the values in the form
        boolean enabled = stairEnableCheckBox.isSelected();
        int moveTime = stairMoveSpinner.getValue();
        int capacity = stairCapacitySpinner.getValue();

        // Modify its values
        StairShaft.stairEditor.edit(
                stairShaft,
                enabled,
                moveTime,
                capacity
        );

        this.getWindowOutput().put(OUTPUT_KEY, stairShaft);

        // Close the window
        this.setClosedWithAction(true);
        stage.close();
    }

    @Override
    public void setElements() {
        InitializeStairEditService.initializeStairEdit(
                promptText,
                stairEnableCheckBox,
                stairMoveLabel,
                stairMoveSpinner,
                stairCapacityLabel,
                stairCapacitySpinner,
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

        // If editing one, use the selected stair as a reference
        // If editing all, create a temporary reference portal shaft
        if (editingOne) {
            // Extract the stair shaft from the selected stair portal
            this.stairShaft
                    = ((StairPortal) Main.simulator.getCurrentAmenity()).getStairShaft();

            stairEnableCheckBox.setSelected(this.stairShaft.isEnabled());
            stairMoveSpinner.getValueFactory().setValue(this.stairShaft.getMoveTime());
            stairCapacitySpinner.getValueFactory().setValue(this.stairShaft.getCapacity());
        } else {
            // Create a dummy stair shaft
            StairShaft.StairShaftFactory stairShaftFactory = new StairShaft.StairShaftFactory();

            this.stairShaft = stairShaftFactory.create(
                    false,
                    -1,
                    -1
            );
        }
    }

    @Override
    protected void closeAction() {

    }
}

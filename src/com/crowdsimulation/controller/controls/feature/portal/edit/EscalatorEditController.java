package com.crowdsimulation.controller.controls.feature.portal.edit;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.controls.service.portal.edit.InitializeEscalatorEditService;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorShaft;
import com.crowdsimulation.model.simulator.Simulator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EscalatorEditController extends PortalEditController {
    public static final String OUTPUT_KEY = "escalator_shaft";

    @FXML
    private Text promptText;

    @FXML
    private CheckBox escalatorEnableCheckbox;

    @FXML
    private Label escalatorMoveLabel;

    @FXML
    private Spinner<Integer> escalatorMoveSpinner;

    @FXML
    private Label escalatorDirectionLabel;

    @FXML
    private ChoiceBox<EscalatorShaft.EscalatorDirection> escalatorDirectionChoiceBox;

    @FXML
    private Label escalatorCapacityLabel;

    @FXML
    private Spinner<Integer> escalatorCapacitySpinner;

    @FXML
    private Button proceedButton;

    private EscalatorShaft escalatorShaft;

    @FXML
    public void proceedAction() {
        Stage stage = (Stage) proceedButton.getScene().getWindow();

        // Take note of the values in the form
        boolean enabled = escalatorEnableCheckbox.isSelected();
        int moveTime = escalatorMoveSpinner.getValue();
        EscalatorShaft.EscalatorDirection escalatorDirection = escalatorDirectionChoiceBox.getValue();
        int capacity = escalatorCapacitySpinner.getValue();

        // Modify its values
        EscalatorShaft.escalatorEditor.edit(
                escalatorShaft,
                enabled,
                moveTime,
                escalatorDirection,
                capacity
        );

        this.getWindowOutput().put(OUTPUT_KEY, escalatorShaft);

        // Close the window
        this.setClosedWithAction(true);
        stage.close();
    }

    @Override
    public void setElements() {
        InitializeEscalatorEditService.initializeEscalatorEdit(
                promptText,
                escalatorEnableCheckbox,
                escalatorMoveLabel,
                escalatorMoveSpinner,
                escalatorDirectionLabel,
                escalatorDirectionChoiceBox,
                escalatorCapacityLabel,
                escalatorCapacitySpinner,
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

        // If editing one, use the selected escalator as a reference
        // If editing all, create a temporary reference portal shaft
        if (editingOne) {
            // Extract the escalator shaft from the selected escalator portal
            this.escalatorShaft
                    = ((EscalatorPortal) Main.simulator.getCurrentAmenity()).getEscalatorShaft();

            escalatorEnableCheckbox.setSelected(this.escalatorShaft.isEnabled());
            escalatorMoveSpinner.getValueFactory().setValue(this.escalatorShaft.getMoveTime());
            escalatorDirectionChoiceBox.setValue(this.escalatorShaft.getEscalatorDirection());
            escalatorCapacitySpinner.getValueFactory().setValue(this.escalatorShaft.getCapacity());
        } else {
            // Create a dummy escalator shaft
            EscalatorShaft.EscalatorShaftFactory escalatorShaftFactory = new EscalatorShaft.EscalatorShaftFactory();

            this.escalatorShaft = escalatorShaftFactory.create(
                    false,
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

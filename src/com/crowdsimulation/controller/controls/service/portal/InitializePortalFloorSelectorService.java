package com.crowdsimulation.controller.controls.service.portal;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.controls.service.InitializeScreenService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.text.Text;

public class InitializePortalFloorSelectorService extends InitializeScreenService {
    public static void initializePortalFloorSelector(
            Text promptText,
            Spinner<Integer> floorSpinner,
            Button proceedButton
    ) {
        // Set the proper amenity (elevator, escalator, or stairs) to display in the prompt text
        String originalText = promptText.getText();

        String updatedText = originalText.replace(
                "%s",
                Main.simulator.getBuildSubcategory().toString().toLowerCase()
        );

        promptText.setText(updatedText);

        // First, get the number of floors
        int numFloors = Main.simulator.getStation().getFloors().size();
        ObservableList<Integer> availableFloorIndices = FXCollections.observableArrayList();

        // Then exclude the index of the current floor
        for (int index = 0; index < numFloors; index++) {
            if (index != Main.simulator.getCurrentFloorIndex()) {
                availableFloorIndices.add(index + 1);
            }
        }

        floorSpinner.setValueFactory(
                new SpinnerValueFactory.ListSpinnerValueFactory<>(availableFloorIndices)
        );
    }
}

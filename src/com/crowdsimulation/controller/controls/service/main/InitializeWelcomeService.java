package com.crowdsimulation.controller.controls.service.main;

import com.crowdsimulation.controller.controls.service.InitializeScreenService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class InitializeWelcomeService extends InitializeScreenService {
    public static void initializeWelcomeService(
            Label rowLabel,
            Spinner<Integer> rowSpinner,
            Label columnLabel,
            Spinner<Integer> columnSpinner,
            Button createBlankStationButton,
            Button loadStationButton
    ) {
        final int minimumRows = 25;
        final int maximumRows = 100;

        final int minimumColumns = 106;
        final int maximumColumns = 220;

        final int defaultRows = 60;
        final int defaultColumns = minimumColumns;

        rowLabel.setLabelFor(rowSpinner);
        rowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                minimumRows, maximumRows
        ));

        rowSpinner.setEditable(true);
        rowSpinner.getValueFactory().setValue(defaultRows);

        // A hacky way to commit the typed value when focus is lost from the spinner
        rowSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                rowSpinner.increment(0);
            }
        });

        columnLabel.setLabelFor(columnSpinner);
        columnSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                minimumColumns, maximumColumns
        ));

        columnSpinner.setEditable(true);
        columnSpinner.getValueFactory().setValue(defaultColumns);

        // A hacky way to commit the typed value when focus is lost from the spinner
        columnSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                columnSpinner.increment(0);
            }
        });
    }
}

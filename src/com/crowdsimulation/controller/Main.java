package com.crowdsimulation.controller;

import com.crowdsimulation.controller.controls.ScreenController;
import com.crowdsimulation.controller.controls.feature.main.MainScreenController;
import com.crowdsimulation.controller.controls.feature.main.WelcomeController;
import com.crowdsimulation.model.simulator.Simulator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
    // Stores the simulator object in charge of all the simulation processes
    public static Simulator simulator = null;

    // Keep a reference to the main controller
    public static MainScreenController mainScreenController;

    // Denotes whether a choice was made by the user
    public static boolean hasMadeChoice = false;

    // Denotes whether a station has been loaded
    public static boolean stationLoaded = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set the simulator up
        initializeSimulator();

        // Set the interface up
        // The reason we set the simulator up before the interface is because connecting to the database takes some
        // time so it's better to bring the interface up after the database connection has been fulfilled (otherwise
        // there will be a few seconds of unresponsiveness from the interface until the database connection is
        // fulfilled
        FXMLLoader mainInterfaceLoader = ScreenController.getLoader(
                getClass(),
                "/com/crowdsimulation/view/MainInterface.fxml");
        Parent mainRoot = mainInterfaceLoader.load();

        MainScreenController mainController = mainInterfaceLoader.getController();
        Main.mainScreenController = mainController;

        // Make the user choose whether to start from a blank station or to load an existing station
        FXMLLoader welcomeInterfaceLoader = ScreenController.getLoader(
                getClass(),
                "/com/crowdsimulation/view/WelcomeInterface.fxml");
        Parent welcomeRoot = welcomeInterfaceLoader.load();

        WelcomeController welcomeController = welcomeInterfaceLoader.getController();
        welcomeController.setElements();

        while (true) {
            // Reset all necessary variables
            welcomeController.setClosedWithAction(false);

            Main.hasMadeChoice = false;
            Main.stationLoaded = false;

            // Show the action-choosing window
            welcomeController.showWindow(
                    welcomeRoot,
                    "Choose an action",
                    true,
                    false
            );

            // Determine whether to open a blank station with the specified rows and columns, or to load an existing
            // station
            if (welcomeController.isClosedWithAction()) {
                boolean isBlankStation
                        = (boolean) welcomeController.getWindowOutput().get(WelcomeController.OUTPUT_KEYS[0]);

                mainController.getWindowInput().put(MainScreenController.INPUT_KEYS[0], isBlankStation);

                if (isBlankStation) {
                    int rows = (int) welcomeController.getWindowOutput().get(WelcomeController.OUTPUT_KEYS[1]);
                    int columns = (int) welcomeController.getWindowOutput().get(WelcomeController.OUTPUT_KEYS[2]);

                    mainController.getWindowInput().put(MainScreenController.INPUT_KEYS[1], rows);
                    mainController.getWindowInput().put(MainScreenController.INPUT_KEYS[2], columns);
                }
            } else if (!welcomeController.isClosedWithAction()) {
                // No choice was made, end the program
                break;
            }

            // Prepare the elements needed for showing the main window
            mainController.performChoice();

            // If, at this point, no choice has been made yet (the file dialog has been closed), end the program
            if (!Main.hasMadeChoice) {
                break;
            }

            // Then adjust the canvas size accordingly
            mainController.setElements();

            // Finally, show the window
            mainController.showWindow(
                    mainRoot,
                    "Station editor",
                    true,
                    false);
        }
    }

    // Initializes the simulator
    private void initializeSimulator() {
        simulator = new Simulator();
    }
}

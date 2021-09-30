package com.trainsimulation.controller;

import com.trainsimulation.controller.screen.MainScreenController;
import com.trainsimulation.controller.screen.ScreenController;
import com.trainsimulation.model.simulator.Simulator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {
    // Stores the simulator object in charge of all the simulation processes
    public static Simulator simulator = null;

    // Keep a reference to the main controller
    public static MainScreenController mainScreenController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Set the simulator up
            initializeSimulator();

            // Set the interface up
            // The reason we set the simulator up before the interface is because connecting to the database takes some
            // time so it's better to bring the interface up after the database connection has been fulfilled (otherwise
            // there will be a few seconds of unresponsiveness from the interface until the database connection is
            // fulfilled
            FXMLLoader loader = ScreenController.getLoader(
                    getClass(),
                    "/com/trainsimulation/view/MainInterface.fxml");
            Parent root = loader.load();

            MainScreenController mainController = loader.getController();
            mainController.showWindow(
                    root,
                    "Train simulation",
                    false);

            Main.mainScreenController = mainController;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    // Initializes the simulator
    private void initializeSimulator() throws Throwable {
        simulator = new Simulator();
    }
}

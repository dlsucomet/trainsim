package com.trainsimulation.controller.screen;

import com.trainsimulation.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public abstract class ScreenController extends Controller {
    // Represents the inputs to this window
    private final HashMap<String, Object> windowInput;

    // Represents the outputs of this window
    private final HashMap<String, Object> windowOutput;

    // Used to pass values around windows, mostly to determine how a window was closed
    private boolean closedWithAction;

    public ScreenController() {
        this.closedWithAction = false;

        this.windowInput = new HashMap<>();
        this.windowOutput = new HashMap<>();
    }

    public static FXMLLoader getLoader(Class<?> classType, String interfaceLocation) {
        return new FXMLLoader(classType.getResource(
                interfaceLocation));
    }

    public void showWindow(Parent loadedRoot, String title, boolean isDialog) throws IOException {
        Scene scene = new Scene(loadedRoot);
        Stage stage = new Stage();

        stage.setTitle(title);
        stage.setResizable(false);
        stage.setScene(scene);

        if (isDialog) {
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } else {
            stage.show();
        }
    }

    public boolean isClosedWithAction() {
        return this.closedWithAction;
    }

    public void setClosedWithAction(boolean closedWithAction) {
        this.closedWithAction = closedWithAction;
    }

    public HashMap<String, Object> getWindowInput() {
        return windowInput;
    }

    public HashMap<String, Object> getWindowOutput() {
        return windowOutput;
    }
}

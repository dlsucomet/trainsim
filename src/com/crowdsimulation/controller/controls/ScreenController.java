package com.crowdsimulation.controller.controls;

import com.crowdsimulation.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;

public abstract class ScreenController extends Controller {
    // Represents the inputs to this window
    private final HashMap<String, Object> windowInput;

    // Represents the outputs of this window
    private final HashMap<String, Object> windowOutput;

    // Preferred x and y values where the window would pop up
    private Double x;
    private Double y;

    // Used to pass values around windows, mostly to determine how a window was closed
    private boolean closedWithAction;

    // Take note of the stage
    protected Stage stage;

    public ScreenController() {
        this.closedWithAction = false;

        this.windowInput = new HashMap<>();
        this.windowOutput = new HashMap<>();

        this.x = null;
        this.y = null;

        this.stage = null;
    }

    public Stage getStage() {
        return this.stage;
    }

    public static FXMLLoader getLoader(Class<?> classType, String interfaceLocation) {
        return new FXMLLoader(classType.getResource(
                interfaceLocation));
    }

    public void showWindow(Parent loadedRoot, String title, boolean showAndWait, boolean isAlwaysOnTop) {
        Scene scene = loadedRoot.getScene();

        if (scene == null) {
            scene = new Scene(loadedRoot);
        } else {
            scene.setRoot(loadedRoot);
        }

        Stage stage = new Stage();

        stage.setTitle(title);
        stage.setResizable(false);
        stage.setScene(scene);

        this.stage = stage;

        stage.setOnCloseRequest(event -> {
            closeAction();
        });

        if (this.x != null) {
            stage.setX(this.x);
        }

        if (this.y != null) {
            stage.setY(this.y);
        }

        this.stage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeAction();
            }
        });

        if (showAndWait) {
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } else {
            stage.setAlwaysOnTop(isAlwaysOnTop);
            stage.show();
        }
    }

    public void closeWindow() {
        this.stage.close();
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

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public abstract void setElements();

    // For what stages need to do when the window is closed
    protected abstract void closeAction();
}

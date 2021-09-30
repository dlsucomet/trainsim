package com.crowdsimulation.controller.controls.feature.main;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.controls.ScreenController;
import com.crowdsimulation.controller.controls.alert.AlertController;
import com.crowdsimulation.controller.controls.feature.floorfield.NormalFloorFieldController;
import com.crowdsimulation.controller.controls.feature.portal.PortalFloorSelectorController;
import com.crowdsimulation.controller.controls.feature.portal.edit.ElevatorEditController;
import com.crowdsimulation.controller.controls.feature.portal.edit.EscalatorEditController;
import com.crowdsimulation.controller.controls.feature.portal.edit.StairEditController;
import com.crowdsimulation.controller.controls.feature.portal.setup.ElevatorSetupController;
import com.crowdsimulation.controller.controls.feature.portal.setup.EscalatorSetupController;
import com.crowdsimulation.controller.controls.feature.portal.setup.PortalSetupController;
import com.crowdsimulation.controller.controls.feature.portal.setup.StairSetupController;
import com.crowdsimulation.controller.controls.service.main.InitializeMainScreenService;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.*;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.PlatformFloorField;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.TurnstileFloorField;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.template.*;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Drawable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Obstacle;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Track;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Wall;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.PortalShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;
import com.crowdsimulation.model.simulator.Simulator;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

import java.io.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainScreenController extends ScreenController {
    public static final String[] INPUT_KEYS = {"blank_station", "rows", "columns"};

    // Controller for functions related to adding and editing
    public static NormalFloorFieldController normalFloorFieldController;

    // Operational variables
    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private TabPane sidebar;

    @FXML
    private TabPane buildTabPane;

    @FXML
    private StackPane interfaceStackPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ImageView deleteImageView;

    // Canvas variables
    @FXML
    private Canvas backgroundCanvas;

    @FXML
    private Canvas foregroundCanvas;

    @FXML
    private Canvas markingsCanvas;

    @FXML
    private Group canvasGroup;

    // Build tab variables
    @FXML
    private ChoiceBox<Simulator.BuildState> buildModeChoiceBox;

    @FXML
    private Label buildModeLabel;

    @FXML
    private Button validateButton;

    // Entrances and exits
    // Station entrance/exit
    @FXML
    private CheckBox stationGateEnableCheckBox;

    @FXML
    private Label stationGateModeLabel;

    @FXML
    private ChoiceBox<StationGate.StationGateMode> stationGateModeChoiceBox;

    @FXML
    private Label stationGateDirectionLabel;

    @FXML
    private ListView<PassengerMovement.TravelDirection> stationGateDirectionListView;

    @FXML
    private Label stationGateSpawnLabel;

    @FXML
    private Spinner<Integer> stationGateSpawnSpinner;

    @FXML
    private Button saveStationGateButton;

    @FXML
    private Button deleteStationGateButton;

    // Security
    @FXML
    private CheckBox securityEnableCheckBox;

    @FXML
    private CheckBox securityBlockPassengerCheckBox;

    @FXML
    private Label securityIntervalLabel;

    @FXML
    private Spinner<Integer> securityIntervalSpinner;

    @FXML
    private Button saveSecurityButton;

    @FXML
    private Button deleteSecurityButton;

    @FXML
    private Button addFloorFieldsSecurityButton;

    @FXML
    private Button flipSecurityButton;

    // Stairs and elevators
    // Stairs
    @FXML
    private Button addStairButton;

    @FXML
    private Button editStairButton;

    @FXML
    private Button deleteStairButton;

    // Escalator
    @FXML
    private Button addEscalatorButton;

    @FXML
    private Button editEscalatorButton;

    @FXML
    private Button deleteEscalatorButton;

    // Elevator
    @FXML
    private Button addElevatorButton;

    @FXML
    private Button editElevatorButton;

    @FXML
    private Button deleteElevatorButton;

    @FXML
    private Button addFloorFieldsElevatorButton;

    // Concourse amenities
    // Ticket booth
    @FXML
    private CheckBox ticketBoothEnableCheckBox;

    @FXML
    private Label ticketBoothModeLabel;

    @FXML
    private ChoiceBox<TicketBooth.TicketType> ticketBoothModeChoiceBox;

    @FXML
    private Label ticketBoothIntervalLabel;

    @FXML
    private Spinner<Integer> ticketBoothIntervalSpinner;

    @FXML
    private Button saveTicketBoothButton;

    @FXML
    private Button deleteTicketBoothButton;

    @FXML
    private Button addFloorFieldsTicketBoothButton;

    @FXML
    private Button flipTicketBoothButton;

    // Turnstile
    @FXML
    private CheckBox turnstileEnableCheckBox;

    @FXML
    private CheckBox turnstileBlockPassengerCheckBox;

    @FXML
    private Label turnstileModeLabel;

    @FXML
    private ChoiceBox<Turnstile.TurnstileMode> turnstileModeChoiceBox;

    @FXML
    private Label turnstileDirectionLabel;

    @FXML
    private ListView<PassengerMovement.TravelDirection> turnstileDirectionListView;

    @FXML
    private Label turnstileIntervalLabel;

    @FXML
    private Spinner<Integer> turnstileIntervalSpinner;

    @FXML
    private Button saveTurnstileButton;

    @FXML
    private Button deleteTurnstileButton;

    @FXML
    private Button addFloorFieldsTurnstileButton;

    // Platform amenities
    // Train boarding area
    @FXML
    private CheckBox trainDoorEnableCheckBox;

    @FXML
    private Label trainDoorDirectionLabel;

    @FXML
    private ChoiceBox<PassengerMovement.TravelDirection> trainDoorDirectionChoiceBox;

    @FXML
    private Label trainDoorCarriageLabel;

    @FXML
    // TODO: Retrieve carriages from database
    private ListView<TrainDoor.TrainDoorCarriage> trainDoorCarriageListView;

    @FXML
    private Label trainDoorOrientationLabel;

    @FXML
    private ChoiceBox<Station.StationOrientation> trainDoorOrientationChoiceBox;

    @FXML
    private CheckBox trainDoorFemalesOnlyCheckBox;

    @FXML
    private Button saveTrainDoorButton;

    @FXML
    private Button deleteTrainDoorButton;

    @FXML
    private Button addFloorFieldsTrainDoorButton;

    // Train tracks
    @FXML
    private Label trackDirectionLabel;

    @FXML
    private ChoiceBox<PassengerMovement.TravelDirection> trackDirectionChoiceBox;

    @FXML
    private Button saveTrackButton;

    @FXML
    private Button deleteTrackButton;

    // Obstacles
    // Wall
    @FXML
    private Label wallTypeLabel;

    @FXML
    private ChoiceBox<Wall.WallType> wallTypeChoiceBox;

    @FXML
    private Button saveWallButton;

    @FXML
    private Button deleteWallButton;

    @FXML
    private Button flipWallButton;

    // Test tab variables
    // Simulation controls
    @FXML
    private Text elapsedTimeText;

    @FXML
    private ToggleButton playButton;

    @FXML
    private Button resetButton;

    @FXML
    private Label simulationSpeedLabel;

    @FXML
    private Slider simulationSpeedSlider;

    // Passenger controls
    @FXML
    private Text passengerCountStationText;

    @FXML
    private Text passengerCountFloorText;

    // Platform controls
    @FXML
    private Label platformDirectionLabel;

    @FXML
    private ChoiceBox<PassengerMovement.TravelDirection> platformDirectionChoiceBox;

    @FXML
    private Label platformCarriagesLabel;

    @FXML
    private ChoiceBox<TrainDoor.TrainDoorCarriage> platformCarriagesChoiceBox;

    @FXML
    private ToggleButton openTrainDoorsButton;

    // Top bar
    // Top bar text prompt
    @FXML
    private Text stationNameText;

    @FXML
    private Button addFloorBelowButton;

    @FXML
    private Button floorBelowButton;

    @FXML
    private Text floorNumberText;

    @FXML
    private Button deleteFloorButton;

    @FXML
    private Button floorAboveButton;

    @FXML
    private Button addFloorAboveButton;

    @FXML
    private ToggleButton peekFloorsButton;

    @FXML
    private Text promptText;

    // The file chooser for saving and loading
    private FileChooser fileChooser;

    public MainScreenController() {
        try {
            FXMLLoader loader;

            loader = ScreenController.getLoader(
                    getClass(),
                    "/com/crowdsimulation/view/NormalFloorFieldInterface.fxml");
            Parent normalFloorFieldControllerRoot = loader.load();

            MainScreenController.normalFloorFieldController = loader.getController();
            MainScreenController.normalFloorFieldController.setElements();
            MainScreenController.normalFloorFieldController.setRoot(normalFloorFieldControllerRoot);

            // Set the position where the floor field would appear
            MainScreenController.normalFloorFieldController.setX(Screen.getPrimary().getBounds().getWidth() * 0.75);
            MainScreenController.normalFloorFieldController.setY(Screen.getPrimary().getBounds().getHeight() * 0.25);

            // Initialize the station file chooser
            this.fileChooser = new FileChooser();

            // Set the extension for this station file
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
                    "Station file (*" + Station.STATION_LAYOUT_FILE_EXTENSION + ")",
                    "*" + Station.STATION_LAYOUT_FILE_EXTENSION + ""
            );

            this.fileChooser.getExtensionFilters().add(extensionFilter);
        } catch (IOException ex) {
            MainScreenController.normalFloorFieldController = null;

            ex.printStackTrace();
        }
    }

    public StackPane getInterfaceStackPane() {
        return interfaceStackPane;
    }

    // Switch the build mode
    public static void switchBuildMode(Simulator.BuildState newBuildState) {
        if (newBuildState != Main.simulator.getBuildState()) {
            Main.simulator.setBuildState(newBuildState);

            Main.mainScreenController.updatePromptText();
        }
    }

    public static Simulator.BuildCategory getBuildCategory(SingleSelectionModel<Tab> currentTabSelectionModel) {
        Simulator.BuildCategory buildCategory = null;

        switch (currentTabSelectionModel.getSelectedIndex()) {
            case 0:
                buildCategory = Simulator.BuildCategory.ENTRANCES_AND_EXITS;

                break;
            case 1:
                buildCategory = Simulator.BuildCategory.STAIRS_AND_ELEVATORS;

                break;
            case 2:
                buildCategory = Simulator.BuildCategory.CONCOURSE_AMENITIES;

                break;
            case 3:
                buildCategory = Simulator.BuildCategory.PLATFORM_AMENITIES;

                break;
            case 4:
                buildCategory = Simulator.BuildCategory.MISCELLANEOUS;

                break;
        }

        return buildCategory;
    }

    public static Simulator.BuildSubcategory getBuildSubcategory(
            SingleSelectionModel<Tab> currentTabSelectionModel) {
        // Get the build subcategory
        Simulator.BuildSubcategory buildSubcategory = null;

        Accordion currentAccordion = (Accordion) currentTabSelectionModel.getSelectedItem().getContent();
        TitledPane currentTitledPane = currentAccordion.getExpandedPane();

        // If there is no expanded subcategory, the current subcategory is none
        if (currentTitledPane == null) {
            buildSubcategory = Simulator.BuildSubcategory.NONE;
        } else {
            String titledPaneTitle = currentAccordion.getExpandedPane().getText();

            switch (titledPaneTitle) {
                case "Station entrance/exit":
                    buildSubcategory = Simulator.BuildSubcategory.STATION_ENTRANCE_EXIT;

                    break;
                case "Security":
                    buildSubcategory = Simulator.BuildSubcategory.SECURITY;

                    break;
                case "Stairs":
                    buildSubcategory = Simulator.BuildSubcategory.STAIRS;

                    break;
                case "Escalator":
                    buildSubcategory = Simulator.BuildSubcategory.ESCALATOR;

                    break;
                case "Elevator":
                    buildSubcategory = Simulator.BuildSubcategory.ELEVATOR;

                    break;
                case "Ticket booth":
                    buildSubcategory = Simulator.BuildSubcategory.TICKET_BOOTH;

                    break;
                case "Turnstile":
                    buildSubcategory = Simulator.BuildSubcategory.TURNSTILE;

                    break;
                case "Train boarding area":
                    buildSubcategory = Simulator.BuildSubcategory.TRAIN_BOARDING_AREA;

                    break;
                case "Train track":
                    buildSubcategory = Simulator.BuildSubcategory.TRAIN_TRACK;

                    break;
                case "Obstacle":
                    buildSubcategory = Simulator.BuildSubcategory.OBSTACLE;

                    break;
            }
        }

        return buildSubcategory;
    }

    @Override
    protected void closeAction() {
    }

    @Override
    public void setElements() {
        // Return to normal zoom size
        interfaceStackPane.setScaleX(1.0);
        interfaceStackPane.setScaleY(1.0);

        // Set the canvas size, depending on the given row and column size
        double rowsScaled = Main.simulator.getStation().getRows() * GraphicsController.tileSize;
        double columnsScaled = Main.simulator.getStation().getColumns() * GraphicsController.tileSize;

        interfaceStackPane.setPrefWidth(columnsScaled);
        interfaceStackPane.setPrefHeight(rowsScaled);

        backgroundCanvas.setWidth(columnsScaled);
        backgroundCanvas.setHeight(rowsScaled);

        foregroundCanvas.setWidth(columnsScaled);
        foregroundCanvas.setHeight(rowsScaled);

        markingsCanvas.setWidth(columnsScaled);
        markingsCanvas.setHeight(rowsScaled);
    }

    @FXML
    public void initialize() {
        // Initialize all UI elements and label references
        InitializeMainScreenService.initializeTopBar(
                addFloorBelowButton,
                floorBelowButton,
                deleteFloorButton,
                floorAboveButton,
                addFloorAboveButton,
                peekFloorsButton
        );

        InitializeMainScreenService.initializeSidebar(
                sidebar
        );

        InitializeMainScreenService.initializeBuildTab(
                validateButton,
                buildModeLabel,
                buildModeChoiceBox,
                // Entrances/exits
                // Station gate
                stationGateEnableCheckBox,
                stationGateModeLabel,
                stationGateModeChoiceBox,
                stationGateDirectionLabel,
                stationGateDirectionListView,
                stationGateSpawnLabel,
                stationGateSpawnSpinner,
                saveStationGateButton,
                deleteStationGateButton,
                // Security
                securityEnableCheckBox,
                securityBlockPassengerCheckBox,
                securityIntervalLabel,
                securityIntervalSpinner,
                saveSecurityButton,
                deleteSecurityButton,
                addFloorFieldsSecurityButton,
                flipSecurityButton,
                // Stairs and elevators
                // Stairs
                addStairButton,
                editStairButton,
                deleteStairButton,
                // Escalator
                addEscalatorButton,
                editEscalatorButton,
                deleteEscalatorButton,
                // Elevator
                addElevatorButton,
                editElevatorButton,
                deleteElevatorButton,
                addFloorFieldsElevatorButton,
                // Concourse amenities
                // Ticket booth
                ticketBoothEnableCheckBox,
                ticketBoothModeLabel,
                ticketBoothModeChoiceBox,
                ticketBoothIntervalLabel,
                ticketBoothIntervalSpinner,
                saveTicketBoothButton,
                deleteTicketBoothButton,
                addFloorFieldsTicketBoothButton,
                flipTicketBoothButton,
                // Turnstile
                turnstileEnableCheckBox,
                turnstileBlockPassengerCheckBox,
                turnstileModeLabel,
                turnstileModeChoiceBox,
                turnstileDirectionLabel,
                turnstileDirectionListView,
                turnstileIntervalLabel,
                turnstileIntervalSpinner,
                saveTurnstileButton,
                deleteTurnstileButton,
                addFloorFieldsTurnstileButton,
                // Platform amenities
                // Train doors
                trainDoorEnableCheckBox,
                trainDoorDirectionLabel,
                trainDoorDirectionChoiceBox,
                trainDoorCarriageLabel,
                trainDoorCarriageListView,
                trainDoorOrientationLabel,
                trainDoorOrientationChoiceBox,
                trainDoorFemalesOnlyCheckBox,
                saveTrainDoorButton,
                deleteTrainDoorButton,
                addFloorFieldsTrainDoorButton,
                // Train tracks
                trackDirectionLabel,
                trackDirectionChoiceBox,
                saveTrackButton,
                deleteTrackButton,
                // Miscellaneous
                // Obstacle
                wallTypeLabel,
                wallTypeChoiceBox,
                saveWallButton,
                deleteWallButton,
                flipWallButton,
                // Build tab
                buildTabPane
        );

        InitializeMainScreenService.initializeTestTab(
                // Simulation controls
                elapsedTimeText,
                playButton,
                resetButton,
                simulationSpeedLabel,
                simulationSpeedSlider,
                // Passenger controls
                passengerCountStationText,
                passengerCountFloorText,
                // Platform controls
                platformDirectionLabel,
                platformDirectionChoiceBox,
                platformCarriagesLabel,
                platformCarriagesChoiceBox,
                openTrainDoorsButton
        );

        InitializeMainScreenService.initializeScrollPane(
                scrollPane,
                interfaceStackPane
        );
    }

    @FXML
    public void loadStationAction() {
        fileChooser.setTitle("Load a pre-built station from a file");

        File stationFile = fileChooser.showOpenDialog(this.getStage());

        if (stationFile != null) {
            // When a station is already loaded, load the station concurrently to avoid choking the UI
            if (Main.stationLoaded) {
                // Load the station from a file
                GraphicsController.beginWaitCursor(borderPane);

                Task<Void> loadStationTask = new Task<Void>() {
                    @Override
                    public Void call() {
                        loadAndInitializeStation(stationFile);

                        return null;
                    }
                };

                loadStationTask.setOnSucceeded(e -> {
                    GraphicsController.endWaitCursor(borderPane);

                    // Finally, update the top bar
                    updateTopBar();
                });

                Thread loadStationThread = new Thread(loadStationTask);

                loadStationThread.start();
            } else {
                // The main window is closed, and hence no UIs to choke yet
                loadAndInitializeStation(stationFile);

                // Finally, update the top bar
                updateTopBar();

            }
        }
    }

    private void loadAndInitializeStation(File stationFile) {
        try {
            Station station = MainScreenController.loadStation(stationFile);

            // Load the station to the simulator
            initializeStation(station, !GraphicsController.listenersDrawn);

            // Regardless, the first choice has already been made
            Main.hasMadeChoice = true;
            Main.stationLoaded = true;

            // Listeners have already been drawn
            if (!GraphicsController.listenersDrawn) {
                GraphicsController.listenersDrawn = true;
            }
        } catch (IOException | ClassNotFoundException e) {
            AlertController.showSimpleAlert(
                    "File opening failed",
                    "Failed to load station",
                    "Failed to load the station from the selected file.",
                    Alert.AlertType.ERROR
            );

            e.printStackTrace();
        }
    }

    @FXML
    // Save the station to a file
    public void saveStationAction() {
        fileChooser.setTitle("Save this station to a file");

        File stationFile = fileChooser.showSaveDialog(this.getStage());

        if (stationFile != null) {
            Station station = Main.simulator.getStation();

            // Set the name of the station in the interface
            String filenameWithoutExtension = stationFile.getName().replaceFirst("[.][^.]+$", "");
            station.setName(filenameWithoutExtension);

            // Save the station to a file
            GraphicsController.beginWaitCursor(borderPane);

            Task<Void> saveStationTask = new Task<Void>() {
                @Override
                public Void call() {
                    Platform.runLater(() -> {
                        try {
                            saveStation(station, stationFile, false);

//                            List<String> stations = new ArrayList<>();

//                            stations.add("North Avenue");
//                            stations.add("Quezon Avenue");
//                            stations.add("GMA-Kamuning");
//                            stations.add("Cubao");
//                            stations.add("Santolan-Annapolis");
//                            stations.add("Ortigas");
//                            stations.add("Shaw Boulevard");
//                            stations.add("Boni Avenue");
//                            stations.add("Guadalupe");
//                            stations.add("Buendia");
//                            stations.add("Ayala");
//                            stations.add("Magallanes");
//                            stations.add("Taft Avenue");

//                            stations.add("Roosevelt");
//                            stations.add("Balintawak");
//                            stations.add("5th Avenue");
//                            stations.add("R. Papa");
//                            stations.add("Abad Santos");
//                            stations.add("Blumentritt");
//                            stations.add("Tayuman");
//                            stations.add("Bambang");
//                            stations.add("D. Jose");
//                            stations.add("Carriedo");
//                            stations.add("Central Terminal");
//                            stations.add("U.N. Avenue");
//                            stations.add("Pedro Gil");
//                            stations.add("Quirino");
//                            stations.add("Vito Cruz");
//                            stations.add("Gil Puyat");
//                            stations.add("Libertad");
//                            stations.add("EDSA");
//                            stations.add("Baclaran");

//                            // TODO: Do not save station as build, then run-only without reloading the file
//                            for (String stationName : stations) {
//                                System.out.println("Saving to " + stationName);
//
////                                station.setName(stationName + " (Build)");
////                                saveStation(station, new File("C:\\Users\\ERDT\\Desktop\\train-simulation\\train-systems\\MRT-3\\stations\\" + stationName + "\\build\\" + stationName + " (Build).stn"), false);
//
//                                station.setName(stationName);
//                                saveStation(station, new File("C:\\Users\\ERDT\\Desktop\\train-simulation\\train-systems\\LRT-1\\stations\\" + stationName + "\\run\\" + stationName + ".stn"), true);
//
//                                System.out.println(stationName + " done");
//                            }
                        } catch (Exception e) {
                            AlertController.showSimpleAlert(
                                    "File saving failed",
                                    "Failed to save this station to a file",
                                    "There was an error in saving the station into a file.",
                                    Alert.AlertType.ERROR
                            );

                            e.printStackTrace();
                        }
                    });

                    return null;
                }
            };

            saveStationTask.setOnSucceeded(e -> {
                GraphicsController.endWaitCursor(borderPane);

                // Finally, update the top bar
                updateTopBar();

                // Enable build mode
                disableBuilding(false);
            });

            saveStationTask.setOnFailed(e -> {
                System.out.println("oop");
            });

            new Thread(saveStationTask).start();
        }

    }

    @FXML
    // Save the station as run-only to a file
    public void saveStationRunOnlyAction() {
        fileChooser.setTitle("Save this station as a run-only to a file");

        File stationFile = fileChooser.showSaveDialog(this.getStage());

        if (stationFile != null) {
            Station station = Main.simulator.getStation();

            // Set the name of the station in the interface
            String filenameWithoutExtension = stationFile.getName().replaceFirst("[.][^.]+$", "");
            station.setName(filenameWithoutExtension);

            // Save the station to a file
            GraphicsController.beginWaitCursor(borderPane);

            Task<Void> saveStationTask = new Task<Void>() {
                @Override
                public Void call() {
                    try {
                        saveStation(station, stationFile, true);
                    } catch (IOException e) {
                        AlertController.showSimpleAlert(
                                "File saving failed",
                                "Failed to save this station to a file",
                                "There was an error in saving the station into a file.",
                                Alert.AlertType.ERROR
                        );

                        e.printStackTrace();
                    }

                    return null;
                }
            };

            saveStationTask.setOnSucceeded(e -> {
                GraphicsController.endWaitCursor(borderPane);

                Main.simulator.setStationRunOnly(true);

                // Finally, update the top bar
                updateTopBar();

                // Disable build mode
                disableBuilding(true);
            });

            new Thread(saveStationTask).start();
        }
    }

    @FXML
    // Validate the station layout
    public void validateStationAction() {
        AtomicBoolean isStationValid = new AtomicBoolean(false);

        GraphicsController.beginWaitCursor(this.borderPane);

        // Run deep station validation on another thread
        Task<Station.StationValidationResult> deepStationValidationTask = new Task<Station.StationValidationResult>() {
            @Override
            public Station.StationValidationResult call() {
                try {
                    clearStation(Main.simulator.getStation());

                    return Station.validateStationLayoutDeeply(Main.simulator.getStation());
                } catch (Exception ex) {
                    ex.printStackTrace();

                    return null;
                }
            }
        };

        deepStationValidationTask.setOnSucceeded(e1 -> {
            // Also run floor field validation on another thread
            Task<Boolean> stationFloorFieldValidationTask = new Task<Boolean>() {
                @Override
                public Boolean call() {
                    return Station.validateFloorFieldsInStation(Main.simulator.getStation());
                }
            };

            stationFloorFieldValidationTask.setOnSucceeded(
                    e2 -> {
                        Station.StationValidationResult stationValidationResult;
                        boolean areFloorFieldsValid;

                        try {
                            stationValidationResult = deepStationValidationTask.get();
                            areFloorFieldsValid = stationFloorFieldValidationTask.get();

                            GraphicsController.endWaitCursor(this.borderPane);

                            if (
                                    stationValidationResult.getStationValidationResultType()
                                            == Station.StationValidationResult.StationValidationResultType.NO_ERROR
                                            && areFloorFieldsValid
                            ) {
                                // Only show the success dialog box when the validation was performed directly from the
                                // validate station button
                                if (!Main.simulator.getValidatingFromRunning()) {
                                    AlertController.showSimpleAlert(
                                            "Valid station",
                                            "Valid station",
                                            "This station contains all the necessary amenities for passenger"
                                                    + " traversal, and is ready for testing.",
                                            Alert.AlertType.INFORMATION
                                    );
                                }

                                isStationValid.set(true);
                            } else {
                                if (
                                        stationValidationResult.getStationValidationResultType()
                                                != Station.StationValidationResult.StationValidationResultType.NO_ERROR
                                ) {
                                    AlertController.showSimpleAlert(
                                            "Invalid station layout",
                                            "Invalid station layout",
                                            "The layout of this station is invalid and not ready for testing.\n"
                                                    + "\n" + stationValidationResult,
                                            Alert.AlertType.ERROR
                                    );
                                } else {
                                    AlertController.showSimpleAlert(
                                            "Floor fields incomplete",
                                            "Floor fields incomplete",
                                            "Some amenities of this station do not have complete floor fields.",
                                            Alert.AlertType.ERROR
                                    );
                                }
                            }

                            // Set the validity flag of the station of the simulator
                            // No runs will be performed if the station is not valid
                            Main.simulator.setStationValid(isStationValid.get());
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                        }
                    }
            );

            new Thread(stationFloorFieldValidationTask).start();
        });

        deepStationValidationTask.setOnFailed(
                event -> {
                    System.out.println("oops");
                }
        );

        new Thread(deepStationValidationTask).start();
    }

    @FXML
    // Remove amenities
    public void deleteAmenityAction() {
        deleteAmenityInFloor(
                Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE
                        || Main.simulator.getBuildState() == Simulator.BuildState.DRAWING
        );

        // Hence, the simulator won't have this amenity anymore
        Main.simulator.setCurrentAmenity(null);
        Main.simulator.setCurrentClass(null);

        // Redraw the interface
        drawInterface(false);
    }

    @FXML
    // Save amenities
    public void saveAmenityAction() {
        saveAmenityInFloor(Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE);

        // Reset the current amenity and class to nulls to disable the save and delete buttons
        Main.simulator.setCurrentAmenity(null);
        Main.simulator.setCurrentClass(null);

        // Redraw the interface
        drawInterface(false);
    }

    @FXML
    // Add a portal
    public void addPortalAction() throws IOException {
        switch (Main.simulator.getBuildSubcategory()) {
            case STAIRS:
                // Only add stairs when there are multiple floors
                if (Main.simulator.getStation().getFloors().size() > 1) {
                    // Display the stair setup prompt
                    FXMLLoader loader = ScreenController.getLoader(
                            getClass(),
                            "/com/crowdsimulation/view/StairSetupInterface.fxml");
                    Parent root = loader.load();

                    StairSetupController stairSetupController = loader.getController();
                    stairSetupController.setElements();

                    // Show the window
                    stairSetupController.showWindow(
                            root,
                            "Stair setup",
                            true,
                            false
                    );

                    // Only proceed when this window is closed through the proceed action
                    if (stairSetupController.isClosedWithAction()) {
                        // Prompt the user that it is now time to draw the first stair
                        AlertController.showSimpleAlert(
                                "Add first stair landing",
                                "Draw the first stair landing",
                                "After closing this window, please draw the first stair landing on this floor." +
                                        " Click X to cancel this operation.",
                                Alert.AlertType.INFORMATION
                        );

                        beginPortalDrawing(stairSetupController);
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Stairs addition failed",
                            "Unable to add stairs",
                            "You may only add stairs when there are more than one floors in the station.",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case ESCALATOR:
                // Only add an escalator when there are multiple floors
                if (Main.simulator.getStation().getFloors().size() > 1) {
                    // Display the escalator setup prompt
                    FXMLLoader loader = ScreenController.getLoader(
                            getClass(),
                            "/com/crowdsimulation/view/EscalatorSetupInterface.fxml");
                    Parent root = loader.load();

                    EscalatorSetupController escalatorSetupController = loader.getController();
                    escalatorSetupController.setElements();

                    // Show the window
                    escalatorSetupController.showWindow(
                            root,
                            "Escalator setup",
                            true,
                            false
                    );

                    // Only proceed when this window is closed through the proceed action
                    if (escalatorSetupController.isClosedWithAction()) {
                        // Prompt the user that it is now time to draw the first escalator
                        AlertController.showSimpleAlert(
                                "Add first escalator",
                                "Draw the first escalator",
                                "After closing this window, please draw the first escalator on this floor." +
                                        " Click X to cancel this operation.",
                                Alert.AlertType.INFORMATION
                        );

                        beginPortalDrawing(escalatorSetupController);
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Escalator addition failed",
                            "Unable to add escalator",
                            "You may only add escalators when there are more than one floors in the station.",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case ELEVATOR:
                // Only add an elevator when there are multiple floors
                if (Main.simulator.getStation().getFloors().size() > 1) {
                    // Display the elevator setup prompt
                    FXMLLoader loader = ScreenController.getLoader(
                            getClass(),
                            "/com/crowdsimulation/view/ElevatorSetupInterface.fxml");
                    Parent root = loader.load();

                    ElevatorSetupController elevatorSetupController = loader.getController();
                    elevatorSetupController.setElements();

                    // Show the window
                    elevatorSetupController.showWindow(
                            root,
                            "Elevator setup",
                            true,
                            false
                    );

                    // Only proceed when this window is closed through the proceed action
                    if (elevatorSetupController.isClosedWithAction()) {
                        // Prompt the user that it is now time to draw the first elevator
                        AlertController.showSimpleAlert(
                                "Add first elevator",
                                "Draw the first elevator",
                                "After closing this window, please draw the first elevator on this floor." +
                                        " Click X to cancel this operation.",
                                Alert.AlertType.INFORMATION
                        );

                        beginPortalDrawing(elevatorSetupController);
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Elevator addition failed",
                            "Unable to add elevator",
                            "You may only add elevators when there are more than one floors in the station.",
                            Alert.AlertType.ERROR
                    );
                }

                break;
        }
    }

    @FXML
    // Edit a portal
    public void editPortalAction() throws IOException {
        FXMLLoader loader;
        Parent root;

        switch (Main.simulator.getBuildSubcategory()) {
            case STAIRS:
                // Display the stairs edit prompt
                loader = ScreenController.getLoader(
                        getClass(),
                        "/com/crowdsimulation/view/StairEditInterface.fxml");
                root = loader.load();

                StairEditController stairEditController = loader.getController();
                stairEditController.setElements();

                // Show the window
                stairEditController.showWindow(
                        root,
                        (Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE)
                                ? "Edit a staircase" : "Edit all staircases",
                        true,
                        false
                );

                // Only proceed when this window is closed through the proceed action
                if (stairEditController.isClosedWithAction()) {
                    // Extract the modified stair shaft from the window
                    StairShaft stairShaft = (StairShaft) stairEditController.getWindowOutput().get(
                            StairEditController.OUTPUT_KEY
                    );

                    // Determine whether we need to edit one or all
                    boolean editingOne = Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE;

                    if (editingOne) {
                        // Apply the changes to the stair shaft to its component elevators
                        saveSingleAmenityInFloor(stairShaft);
                    } else {
                        // Apply the changes to the stair shaft to all elevator shafts and all their component elevators
                        saveAllAmenitiesInFloor(stairShaft);
                    }

                    // Prompt the user that the stair has been successfully edited
                    if (editingOne) {
                        AlertController.showSimpleAlert(
                                "Staircase edited",
                                "Staircase successfully edited",
                                "The staircase has been successfully edited.",
                                Alert.AlertType.INFORMATION
                        );
                    } else {
                        AlertController.showSimpleAlert(
                                "Staircases edited",
                                "Staircases successfully edited",
                                "The staircases have been successfully edited.",
                                Alert.AlertType.INFORMATION
                        );
                    }
                }

                break;
            case ESCALATOR:
                // Display the escalator edit prompt
                loader = ScreenController.getLoader(
                        getClass(),
                        "/com/crowdsimulation/view/EscalatorEditInterface.fxml");
                root = loader.load();

                EscalatorEditController escalatorEditController = loader.getController();
                escalatorEditController.setElements();

                // Show the window
                escalatorEditController.showWindow(
                        root,
                        (Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE)
                                ? "Edit an escalator" : "Edit all escalators",
                        true,
                        false
                );

                // Only proceed when this window is closed through the proceed action
                if (escalatorEditController.isClosedWithAction()) {
                    // Extract the modified escalator shaft from the window
                    EscalatorShaft escalatorShaft = (EscalatorShaft) escalatorEditController.getWindowOutput().get(
                            EscalatorEditController.OUTPUT_KEY
                    );

                    // Determine whether we need to edit one or all
                    boolean editingOne = Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE;

                    if (editingOne) {
                        // Apply the changes to the escalator shaft to its component elevators
                        saveSingleAmenityInFloor(escalatorShaft);
                    } else {
                        // Apply the changes to the escalator shaft to all elevator shafts and all their component elevators
                        saveAllAmenitiesInFloor(escalatorShaft);
                    }

                    // Prompt the user that the escalator has been successfully edited
                    if (editingOne) {
                        AlertController.showSimpleAlert(
                                "Escalator edited",
                                "Escalator successfully edited",
                                "The escalator has been successfully edited.",
                                Alert.AlertType.INFORMATION
                        );
                    } else {
                        AlertController.showSimpleAlert(
                                "Escalators edited",
                                "Escalators successfully edited",
                                "The escalators have been successfully edited.",
                                Alert.AlertType.INFORMATION
                        );
                    }
                }

                break;
            case ELEVATOR:
                // Display the elevator edit prompt
                loader = ScreenController.getLoader(
                        getClass(),
                        "/com/crowdsimulation/view/ElevatorEditInterface.fxml");
                root = loader.load();

                ElevatorEditController elevatorEditController = loader.getController();
                elevatorEditController.setElements();

                // Show the window
                elevatorEditController.showWindow(
                        root,
                        (Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE)
                                ? "Edit an elevator" : "Edit all elevators",
                        true,
                        false
                );

                // Only proceed when this window is closed through the proceed action
                if (elevatorEditController.isClosedWithAction()) {
                    // Extract the modified elevator shaft from the window
                    ElevatorShaft elevatorShaft = (ElevatorShaft) elevatorEditController.getWindowOutput().get(
                            ElevatorEditController.OUTPUT_KEY
                    );

                    // Determine whether we need to edit one or all
                    boolean editingOne = Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE;

                    if (editingOne) {
                        // Apply the changes to the elevator shaft to its component elevators
                        saveSingleAmenityInFloor(elevatorShaft);
                    } else {
                        // Apply the changes to the elevator shaft to all elevator shafts and all their component elevators
                        saveAllAmenitiesInFloor(elevatorShaft);
                    }

                    // Prompt the user that the elevator has been successfully edited
                    if (editingOne) {
                        AlertController.showSimpleAlert(
                                "Elevator edited",
                                "Elevator successfully edited",
                                "The elevator has been successfully edited.",
                                Alert.AlertType.INFORMATION
                        );
                    } else {
                        AlertController.showSimpleAlert(
                                "Elevators edited",
                                "Elevators successfully edited",
                                "The elevators have been successfully edited.",
                                Alert.AlertType.INFORMATION
                        );
                    }
                }

                break;
        }

        // Redraw the interface
        drawInterface(false);
    }

    @FXML
    // Delete a portal
    public void deletePortalAction() {
        boolean editingOne = Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE;
        boolean confirm;

        Simulator.BuildSubcategory buildSubcategory = Main.simulator.getBuildSubcategory();

        switch (buildSubcategory) {
            case STAIRS:
                // Show a dialog to confirm floor deletion
                if (editingOne) {
                    confirm = AlertController.showConfirmationAlert(
                            "Are you sure?",
                            "Are you sure you want to delete this staircase?",
                            "This will remove this staircase from all its serviced floors. This operation cannot be undone."
                    );
                } else {
                    confirm = AlertController.showConfirmationAlert(
                            "Are you sure?",
                            "Are you sure you want to delete all staircases?",
                            "This will remove all staircases from their serviced floors. This operation cannot be undone."
                    );
                }

                // Determine whether we need to edit one or all
                if (confirm) {
                    if (editingOne) {
                        // Delete this staircase
                        deleteSingleAmenityInFloor(
                                ((StairPortal) Main.simulator.getCurrentAmenity()).getStairShaft()
                        );
                    } else {
                        // Delete all staircases
                        deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.STAIRS);
                    }

                    // Prompt the user that the staircase has been successfully edited
                    if (editingOne) {
                        AlertController.showSimpleAlert(
                                "Staircase deleted",
                                "Staircase successfully deleted",
                                "The staircase has been successfully deleted.",
                                Alert.AlertType.INFORMATION
                        );
                    } else {
                        AlertController.showSimpleAlert(
                                "All staircases deleted",
                                "All staircases successfully deleted",
                                "All staircases have been successfully deleted.",
                                Alert.AlertType.INFORMATION
                        );
                    }

                    // Redraw the interface
                    drawInterface(false);
                }

                break;
            case ESCALATOR:
                // Show a dialog to confirm floor deletion
                if (editingOne) {
                    confirm = AlertController.showConfirmationAlert(
                            "Are you sure?",
                            "Are you sure you want to delete this escalator?",
                            "This will remove this escalator from all its serviced floors. This operation cannot be undone."
                    );
                } else {
                    confirm = AlertController.showConfirmationAlert(
                            "Are you sure?",
                            "Are you sure you want to delete all escalators?",
                            "This will remove all escalators from their serviced floors. This operation cannot be undone."
                    );
                }

                // Determine whether we need to edit one or all
                if (confirm) {
                    if (editingOne) {
                        // Delete this escalator
                        deleteSingleAmenityInFloor(
                                ((EscalatorPortal) Main.simulator.getCurrentAmenity()).getEscalatorShaft()
                        );
                    } else {
                        // Delete all escalators
                        deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.ESCALATOR);
                    }

                    // Prompt the user that the escalator has been successfully edited
                    if (editingOne) {
                        AlertController.showSimpleAlert(
                                "Escalator deleted",
                                "Escalator successfully deleted",
                                "The escalator has been successfully deleted.",
                                Alert.AlertType.INFORMATION
                        );
                    } else {
                        AlertController.showSimpleAlert(
                                "All escalators deleted",
                                "All escalators successfully deleted",
                                "All escalators have been successfully deleted.",
                                Alert.AlertType.INFORMATION
                        );
                    }

                    // Redraw the interface
                    drawInterface(false);
                }

                break;
            case ELEVATOR:
                // Show a dialog to confirm floor deletion
                if (editingOne) {
                    confirm = AlertController.showConfirmationAlert(
                            "Are you sure?",
                            "Are you sure you want to delete this elevator?",
                            "This will remove this elevator from all its serviced floors. This operation cannot be undone."
                    );
                } else {
                    confirm = AlertController.showConfirmationAlert(
                            "Are you sure?",
                            "Are you sure you want to delete all elevators?",
                            "This will remove all elevators from their serviced floors. This operation cannot be undone."
                    );
                }

                // Determine whether we need to edit one or all
                if (confirm) {
                    if (editingOne) {
                        // Delete this elevator
                        deleteSingleAmenityInFloor(
                                ((ElevatorPortal) Main.simulator.getCurrentAmenity()).getElevatorShaft()
                        );
                    } else {
                        // Delete all elevators
                        deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.ELEVATOR);
                    }

                    // Prompt the user that the elevator has been successfully edited
                    if (editingOne) {
                        AlertController.showSimpleAlert(
                                "Elevator deleted",
                                "Elevator successfully deleted",
                                "The elevator has been successfully deleted.",
                                Alert.AlertType.INFORMATION
                        );
                    } else {
                        AlertController.showSimpleAlert(
                                "All elevators deleted",
                                "All elevators successfully deleted",
                                "All elevators have been successfully deleted.",
                                Alert.AlertType.INFORMATION
                        );
                    }

                    // Redraw the interface
                    drawInterface(false);
                }

                break;
        }

        // Hence, the simulator won't have this amenity anymore
        Main.simulator.setCurrentAmenity(null);
        Main.simulator.setCurrentClass(null);
    }

    @FXML
    // Add a floor below this current floor
    public void addFloorBelowAction() {
        // Retrieve the floors of the current station
        Floor currentFloor = Main.simulator.getCurrentFloor();

        // Add the floor below
        Floor newFloor = Floor.addFloorAboveOrBelow(
                Main.simulator.getStation(),
                currentFloor,
                false,
                currentFloor.getRows(),
                currentFloor.getColumns()
        );

        // Switch to that new floor
        switchFloor(newFloor);

        // Show a dialog box to confirm that a floor was added
        AlertController.showSimpleAlert(
                "Floor added",
                "Floor added successfully",
                "A new floor has been added successfully. The view has been switched to that new floor.",
                Alert.AlertType.INFORMATION
        );
    }

    @FXML
    // Add a floor above this current floor
    public void addFloorAboveAction() {
        // Retrieve the floors of the current station
        Floor currentFloor = Main.simulator.getCurrentFloor();

        // Add the floor below
        Floor newFloor = Floor.addFloorAboveOrBelow(
                Main.simulator.getStation(),
                currentFloor,
                true,
                currentFloor.getRows(),
                currentFloor.getColumns()
        );

        // Switch to that new floor
        switchFloor(newFloor);

        // Show a dialog box to confirm that a floor was added
        AlertController.showSimpleAlert(
                "Floor added",
                "Floor added successfully",
                "A new floor has been added successfully. The view has been switched to that new floor.",
                Alert.AlertType.INFORMATION
        );
    }

    @FXML
    // Delete current floor
    public void deleteFloorAction() {
        List<Floor> floors = Main.simulator.getStation().getFloors();

        // Only allow deletion of floors when there are more than one
        // i.e., you can't delete the only floor you're working on
        if (floors.size() > 1) {
            // Show a dialog to confirm floor deletion
            boolean confirm = AlertController.showConfirmationAlert(
                    "Are you sure?",
                    "Are you sure you want to delete this floor?",
                    "All amenities within this floor will be deleted. Stairs, escalators, and elevators" +
                            " going to or coming from this floor will also be deleted from other floors. This" +
                            " operation cannot be undone."
            );

            if (confirm) {
                Station station = Main.simulator.getStation();
                Floor floorToBeRemoved = Main.simulator.getCurrentFloor();
                Floor floorToSwitchTo;

                // Delete the current floor
                Floor.deleteFloor(
                        station,
                        floorToBeRemoved
                );

                // Switch to the floor below the floor to be deleted, if any
                if (Main.simulator.getCurrentFloorIndex() > 0) {
                    floorToSwitchTo = floors.get(
                            Main.simulator.getCurrentFloorIndex() - 1
                    );

                    // Show a dialog box to confirm that a floor was added
                    AlertController.showSimpleAlert(
                            "Floor deleted",
                            "Floor deleted successfully",
                            "The floor has been deleted successfully. The view has been switched to the floor below" +
                                    " the deleted floor.",
                            Alert.AlertType.INFORMATION
                    );
                } else {
                    // If there is no floor below it, switch to the floor above it
                    floorToSwitchTo = floors.get(0);

                    // Show a dialog box to confirm that a floor was added
                    AlertController.showSimpleAlert(
                            "Floor deleted",
                            "Floor deleted successfully",
                            "The floor has been deleted successfully. The view has been switched to the floor above" +
                                    " the deleted floor.",
                            Alert.AlertType.INFORMATION
                    );
                }

                // Switch to the floor below
                switchFloor(floorToSwitchTo);
            }
        } else {
            // Show a dialog box to tell the user that deleting a singular floor is not allowed
            AlertController.showSimpleAlert(
                    "Floor deletion failed",
                    "Floor deletion failed",
                    "You may not delete the only floor in this station.",
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    // Switch view to the floor below
    public void switchToFloorBelowAction() {
        // Get the floor below
        Floor floorBelow = Main.simulator.getStation().getFloors().get(
                Main.simulator.getCurrentFloorIndex() - 1
        );

        // Switch to that floor
        switchFloor(floorBelow);
    }

    @FXML
    // Switch view to the floor above
    public void switchToFloorAboveAction() {
        // Get the floor above
        Floor floorAbove = Main.simulator.getStation().getFloors().get(
                Main.simulator.getCurrentFloorIndex() + 1
        );

        // Switch to that floor
        switchFloor(floorAbove);
    }

    @FXML
    public void togglePeekAction() {
        // Set the will peek variable
        GraphicsController.willPeek = peekFloorsButton.isSelected();

        // Redraw the interface
        drawInterface(false);
    }

    @FXML
    // Add floor fields
    public void addFloorFieldsAction() {
        // Turn on the floor fields drawing mode
        beginFloorFieldDrawing();

        // Commence adding the floor fields
        addFloorFields();
    }

    @FXML
    // Flip graphic
    public void flipAction() {
        // Flip the current amenity
        flipAmenityInFloor(Main.simulator.getBuildState() == Simulator.BuildState.EDITING_ONE);

        // Redraw interface
        drawInterface(false);
    }

    @FXML
    // Play the simulation
    public void playAction() {
        // Not yet running to running (play simulation)
        if (!Main.simulator.isRunning()) {
            // Only run when the station is valid
            Main.simulator.setValidatingFromRunning(true);

//            validateStationAction();

            Main.simulator.setValidatingFromRunning(false);

            if (/*Main.simulator.isStationValid()*/true) {
                // Update mode
                Main.simulator.setOperationMode(Simulator.OperationMode.TESTING);
                Main.mainScreenController.updatePromptText();

                // The simulation will now be running
                Main.simulator.setRunning(true);
                Main.simulator.getPlaySemaphore().release();

                playButton.setText("Pause");
            } else {
                playButton.setSelected(false);
            }
        } else {
            // Update mode
            Main.simulator.setOperationMode(Simulator.OperationMode.BUILDING);
            Main.mainScreenController.updatePromptText();

            // Running to not running (pause simulation)
            // The simulation will now be pausing
            Main.simulator.setRunning(false);

            playButton.setText("Play");
        }
    }

    @FXML
    // Reset the simulation
    public void resetAction() {
        Main.simulator.reset();

        // Clear all passengers
        clearStation(Main.simulator.getStation());

        // Redraw the canvas
        drawStationViewFloorForeground(Main.simulator.getCurrentFloor(), false);

        // If the simulator is running, stop it
        if (Main.simulator.isRunning()) {
            playAction();
            playButton.setSelected(false);
        }
    }

    @FXML
    // Open or close the train doors
    public void toggleTrainDoorsAction() {
        if (!toggleTrainDoors(platformDirectionChoiceBox.getValue(), platformCarriagesChoiceBox.getValue())) {
            AlertController.showSimpleAlert(
                    "No train doors opened",
                    "No train doors opened",
                    "The given platform direction and trainset supported do not match any train door" +
                            " waiting areas.",
                    Alert.AlertType.ERROR
            );

            openTrainDoorsButton.setSelected(false);
        }
    }

    public void performChoice() {
        // Depending on the mode chosen by the user in the window before this, either start with a blank station, or
        // load an already existing one
        if ((boolean) this.getWindowInput().get(MainScreenController.INPUT_KEYS[0])) {
            int width = (int) this.getWindowInput().get(MainScreenController.INPUT_KEYS[1]);
            int length = (int) this.getWindowInput().get(MainScreenController.INPUT_KEYS[2]);

            // Discretize the given width and length measurements to 0.6 x 0.6 sized patches
            // Always round up
            int rows = (int) Math.ceil(width / Patch.PATCH_SIZE_IN_SQUARE_METERS);
            int columns = (int) Math.ceil(length / Patch.PATCH_SIZE_IN_SQUARE_METERS);

            // Initialize the blank station
            Station blankStation = new Station(rows, columns);
            initializeStation(blankStation, !GraphicsController.listenersDrawn);

            // Listeners have already been drawn
            if (!GraphicsController.listenersDrawn) {
                GraphicsController.listenersDrawn = true;
            }

            Main.hasMadeChoice = true;
            Main.stationLoaded = true;
        } else {
            loadStationAction();
        }
    }

    // Clear all passengers in the station
    public static void clearStation(Station station) {
        // Clear the station cache
        station.clearCaches();

        // Clear all portals
        for (StairShaft stairShaft : station.getStairShafts()) {
            station.getPassengersInStation().removeAll(stairShaft.clearQueues());
        }

        for (EscalatorShaft escalatorShaft : station.getEscalatorShafts()) {
            station.getPassengersInStation().removeAll(escalatorShaft.clearQueues());
        }

        // Clear passengers from each floor
        for (Floor floor : station.getFloors()) {
            clearStationFloor(floor);

            // Clear queued passengers in the elevator shafts in this station
            for (ElevatorShaft elevatorShaft : station.getElevatorShafts()) {
                ((Queueable) elevatorShaft.getLowerPortal()).getQueueObject().setPassengerServiced(null);
                ((Queueable) elevatorShaft.getUpperPortal()).getQueueObject().setPassengerServiced(null);

                ((Queueable) elevatorShaft.getLowerPortal()).getQueueObject().getPassengersQueueing().clear();
                ((Queueable) elevatorShaft.getUpperPortal()).getQueueObject().getPassengersQueueing().clear();
            }

            // Close all open train doors
            for (TrainDoor trainDoor : floor.getTrainDoors()) {
                if (trainDoor.isOpen()) {
                    trainDoor.toggleTrainDoor();
                }
            }
        }
    }

    // Clear passengers in a single floor
    public static void clearStationFloor(Floor floor) {
        // Rest station gate backlogs
        for (StationGate stationGate : floor.getStationGates()) {
            stationGate.resetBacklogs();
        }

        // Reset all goal waiting times
        for (Security security : floor.getSecurities()) {
            security.resetWaitingTime();
        }

        for (TicketBooth ticketBooth : floor.getTicketBooths()) {
            ticketBooth.resetWaitingTime();
        }

        for (Turnstile turnstile : floor.getTurnstiles()) {
            turnstile.resetWaitingTime();
        }

        // Remove the relationship between the patch and the passenger
        for (Passenger passenger : floor.getPassengersInFloor()) {
            passenger.getPassengerMovement().getCurrentPatch().getPassengers().clear();
            passenger.getPassengerMovement().setCurrentPatch(null);
        }

        // Remove all the passengers found in this floor from the station's master list of passengers
        floor.getStation().getPassengersInStation().removeAll(
                floor.getPassengersInFloor()
        );

        // Clear all passengers from this floor's passenger list
        floor.getPassengersInFloor().clear();

        // Clear this floor's patch set
        floor.getPassengerPatchSet().clear();

        // Clear all serviced passengers
        List<Queueable> queueables = new ArrayList<>();

        queueables.addAll(floor.getSecurities());
        queueables.addAll(floor.getTicketBooths());
        queueables.addAll(floor.getTurnstiles());
        queueables.addAll(floor.getTrainDoors());

        for (Queueable queueable : queueables) {
            if (queueable instanceof Turnstile) {
                for (QueueObject queueObject : ((Turnstile) queueable).getQueueObjects().values()) {
                    queueObject.setPassengerServiced(null);
                    queueObject.getPassengersQueueing().clear();
                }
            } else if (queueable instanceof TrainDoor) {
                for (QueueObject queueObject : ((TrainDoor) queueable).getQueueObjects().values()) {
                    queueObject.setPassengerServiced(null);
                    queueObject.getPassengersQueueing().clear();
                }
            } else {
                queueable.getQueueObject().setPassengerServiced(null);
                queueable.getQueueObject().getPassengersQueueing().clear();
            }
        }
    }

    private boolean toggleTrainDoors(
            PassengerMovement.TravelDirection travelDirection,
            TrainDoor.TrainDoorCarriage trainDoorCarriage
    ) {
        List<TrainDoor> trainDoors = Main.simulator.getCurrentFloor().getTrainDoors();

        boolean hasTrainDoorOpened = false;

        // Open each train door given the parameters in the interface
        for (TrainDoor trainDoor : trainDoors) {
            if (
                    trainDoor.getPlatformDirection() == travelDirection
                            && trainDoor.getTrainDoorCarriagesSupported().contains(trainDoorCarriage)
            ) {
                trainDoor.toggleTrainDoor();

                hasTrainDoorOpened = true;
            }
        }

        return hasTrainDoorOpened;
    }

    public void initializeStation(Station station, boolean drawListeners) {
        // Reset the simulator to its initial settings
        Main.simulator.resetToDefaultConfiguration(station);

        // Update the top bar, reflecting the name and other details of the current station
        updateTopBar();

        // Reset the top bar
        resetTopBar();

        // Determine whether build mode should be turned on or off
        // Disable build mode
        disableBuilding(station.isRunOnly());

        // Draw the interface
        GraphicsController.tileSize = backgroundCanvas.getHeight() / Main.simulator.getCurrentFloor().getRows();

        drawInterface(drawListeners);
    }

    // Load station
    public static Station loadStation(File stationFile) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(stationFile.getAbsolutePath());
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        Station station = (Station) objectInputStream.readObject();

        objectInputStream.close();

        return station;
    }

    // Save station
    private static void saveStation(Station station, File stationFile, boolean runOnly) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(stationFile.getAbsolutePath());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        // Clear the passengers in the station first
        clearStation(station);

        // If the station is to be saved as a run-only, remove all patches which are outside the station
        if (runOnly) {
            station.removeIrrelevantPatches();
            station.setRunOnly(true);
        }

        objectOutputStream.writeObject(station);
        objectOutputStream.close();
    }

    // Add floor fields
    private void addFloorFields() {
        // Show the window to edit the parameters of the floor field
        MainScreenController.normalFloorFieldController.showWindow(
                MainScreenController.normalFloorFieldController.getRoot(),
                "Floor fields",
                false,
                true
        );
    }

    // Delete floor fields and redraw interface
    public void deleteFloorFieldAction() {
        deleteFloorField();

        drawInterface(false);
    }

    // Delete a floor field
    private void deleteFloorField() {
        // Clear the floor field of the current target given the current floor field state
        Main.simulator.getCurrentFloorFieldTarget().deleteFloorField(
                normalFloorFieldController.getFloorFieldState()
        );
    }

    // Paste a floor field and redraw interface
    public void pasteFloorFieldAction() {
        if (Main.simulator.getCurrentAmenity().getClass().equals(
                normalFloorFieldController.getQueueObjectCopiedClass()
        )
        ) {
            pasteFloorField();

            drawInterface(false);
        } else {
            AlertController.showSimpleAlert(
                    "Floor field pasting failed",
                    "Incompatible amenity type",
                    "This floor field is not applicable to this amenity.",
                    Alert.AlertType.ERROR
            );
        }
    }

    // Paste a floor field template onto the current queueable
    private void pasteFloorField() {
        // Using the template copied, apply the floor fields to this queueable
        QueueableFloorFieldTemplate queueObjectTemplate
                = MainScreenController.normalFloorFieldController.getQueueObjectTemplateCopied();

        if (queueObjectTemplate instanceof SimpleFloorFieldTemplate) {
            SimpleFloorFieldTemplate singleQueueObjectTemplate = ((SimpleFloorFieldTemplate) queueObjectTemplate);

            // Add the floor fields for each disposition-state pair
            for (
                    Map.Entry<QueueingFloorField.FloorFieldState.DispositionStatePair, QueueingFloorFieldTemplate>
                            templateEntry : singleQueueObjectTemplate.getFloorFieldsTemplate().entrySet()
            ) {
                QueueingFloorField.FloorFieldState.DispositionStatePair dispositionStatePair = templateEntry.getKey();
                QueueingFloorFieldTemplate queueingFloorFieldTemplate = templateEntry.getValue();

                // Apply all floor fields to the current queueable
                Queueable currentQueueableTarget = Main.simulator.getCurrentFloorFieldTarget();

                for (
                        QueueingFloorFieldTemplate.PatchOffsetFloorFieldValuePair patchOffsetFloorFieldValuePair
                        : queueingFloorFieldTemplate.getAssociatedPatches()
                ) {
                    // Assemble the floor field state using the given disposition and the current queueable target
                    PassengerMovement.Disposition disposition = dispositionStatePair.getDisposition();
                    PassengerMovement.State state = dispositionStatePair.getState();

                    QueueingFloorField.FloorFieldState floorFieldState
                            = new QueueingFloorField.FloorFieldState(
                            disposition,
                            state,
                            currentQueueableTarget
                    );

                    // Get the offset value
                    Patch.Offset offset = patchOffsetFloorFieldValuePair.getOffset();
                    double floorFieldValue = patchOffsetFloorFieldValuePair.getFloorFieldValue();

                    // Get the patch given when applying the offset value to the current queueable
                    QueueObject queueObject = currentQueueableTarget.getQueueObject();

                    Patch referencePatch = queueObject.getPatch();
                    Patch floorFieldPatch = Patch.Offset.getPatchFromOffset(
                            Main.simulator.getCurrentFloor(),
                            referencePatch,
                            offset
                    );

                    // If the offset patch was a valid patch, add the floor field value
                    if (floorFieldPatch != null) {
                        QueueingFloorField.addFloorFieldValue(
                                floorFieldPatch,
                                currentQueueableTarget,
                                floorFieldState,
                                floorFieldValue
                        );
                    }
                }
            }
        } else if (queueObjectTemplate instanceof TurnstileFloorFieldTemplate) {
            TurnstileFloorFieldTemplate turnstileFloorFieldTemplate
                    = ((TurnstileFloorFieldTemplate) queueObjectTemplate);

            // Add the floor fields for each disposition-state pair
            for (
                    Map.Entry<
                            PassengerMovement.Disposition,
                            HashMap<QueueingFloorField.FloorFieldState.DispositionStatePair,
                                    QueueingFloorFieldTemplate>
                            >
                            dispositionHashMapEntry : turnstileFloorFieldTemplate.getFloorFieldsTemplate().entrySet()
            ) {
                PassengerMovement.Disposition disposition = dispositionHashMapEntry.getKey();

                HashMap<
                        QueueingFloorField.FloorFieldState.DispositionStatePair,
                        QueueingFloorFieldTemplate>
                        queueingFloorFieldTemplateHashMap = dispositionHashMapEntry.getValue();

                // Add the floor fields for each disposition-state pair
                for (
                        Map.Entry<QueueingFloorField.FloorFieldState.DispositionStatePair, QueueingFloorFieldTemplate>
                                templateEntry : queueingFloorFieldTemplateHashMap.entrySet()
                ) {
                    QueueingFloorField.FloorFieldState.DispositionStatePair dispositionStatePair = templateEntry.getKey();
                    QueueingFloorFieldTemplate queueingFloorFieldTemplate = templateEntry.getValue();

                    // Apply all floor fields to the current queueable
                    Queueable currentQueueableTarget = Main.simulator.getCurrentFloorFieldTarget();
                    Turnstile turnstileTarget = ((Turnstile) currentQueueableTarget);

                    for (
                            QueueingFloorFieldTemplate.PatchOffsetFloorFieldValuePair patchOffsetFloorFieldValuePair
                            : queueingFloorFieldTemplate.getAssociatedPatches()
                    ) {
                        // Assemble the floor field state using the given disposition and the current queueable target
                        PassengerMovement.State state = dispositionStatePair.getState();

                        QueueingFloorField.FloorFieldState floorFieldState
                                = new QueueingFloorField.FloorFieldState(
                                disposition,
                                state,
                                currentQueueableTarget
                        );

                        // Get the offset value
                        Patch.Offset offset = patchOffsetFloorFieldValuePair.getOffset();
                        double floorFieldValue = patchOffsetFloorFieldValuePair.getFloorFieldValue();

                        // Get the patch given when applying the offset value to the current queueable
                        QueueObject queueObject = turnstileTarget.getQueueObjects().get(disposition);

                        Patch referencePatch = queueObject.getPatch();
                        Patch floorFieldPatch = Patch.Offset.getPatchFromOffset(
                                Main.simulator.getCurrentFloor(),
                                referencePatch,
                                offset
                        );

                        // If the offset patch was a valid patch, add the floor field value
                        if (floorFieldPatch != null) {
                            QueueingFloorField.addFloorFieldValue(
                                    floorFieldPatch,
                                    currentQueueableTarget,
                                    floorFieldState,
                                    floorFieldValue
                            );
                        }
                    }
                }
            }
        } else if (queueObjectTemplate instanceof TrainDoorFloorFieldTemplate) {
            TrainDoorFloorFieldTemplate trainDoorFloorFieldTemplate
                    = ((TrainDoorFloorFieldTemplate) queueObjectTemplate);

            // Add the floor fields for each disposition-state pair
            for (
                    Map.Entry<
                            TrainDoor.TrainDoorEntranceLocation,
                            HashMap<PlatformFloorField.PlatformFloorFieldState.DispositionStatePair,
                                    QueueingFloorFieldTemplate>
                            >
                            dispositionHashMapEntry : trainDoorFloorFieldTemplate.getFloorFieldsTemplate().entrySet()
            ) {
                TrainDoor.TrainDoorEntranceLocation trainDoorEntranceLocation = dispositionHashMapEntry.getKey();

                HashMap<
                        PlatformFloorField.PlatformFloorFieldState.DispositionStatePair,
                        QueueingFloorFieldTemplate>
                        queueingFloorFieldTemplateHashMap = dispositionHashMapEntry.getValue();

                // Add the floor fields for each disposition-state pair
                for (
                        Map.Entry<PlatformFloorField.PlatformFloorFieldState.DispositionStatePair, QueueingFloorFieldTemplate>
                                templateEntry : queueingFloorFieldTemplateHashMap.entrySet()
                ) {
                    PlatformFloorField.PlatformFloorFieldState.DispositionStatePair dispositionStatePair = templateEntry.getKey();
                    QueueingFloorFieldTemplate queueingFloorFieldTemplate = templateEntry.getValue();

                    // Apply all floor fields to the current queueable
                    Queueable currentQueueableTarget = Main.simulator.getCurrentFloorFieldTarget();
                    TrainDoor trainDoorTarget = ((TrainDoor) currentQueueableTarget);

                    for (
                            QueueingFloorFieldTemplate.PatchOffsetFloorFieldValuePair patchOffsetFloorFieldValuePair
                            : queueingFloorFieldTemplate.getAssociatedPatches()
                    ) {
                        // Assemble the floor field state using the given disposition and the current queueable target
                        PassengerMovement.State state = dispositionStatePair.getState();

                        PlatformFloorField.PlatformFloorFieldState PlatformFloorFieldState
                                = new PlatformFloorField.PlatformFloorFieldState(
                                dispositionStatePair.getDisposition(),
                                state,
                                currentQueueableTarget,
                                trainDoorEntranceLocation
                        );

                        // Get the offset value
                        Patch.Offset offset = patchOffsetFloorFieldValuePair.getOffset();
                        double floorFieldValue = patchOffsetFloorFieldValuePair.getFloorFieldValue();

                        // Get the patch given when applying the offset value to the current queueable
                        QueueObject queueObject = trainDoorTarget.getQueueObjects().get(trainDoorEntranceLocation);

                        Patch referencePatch = queueObject.getPatch();
                        Patch floorFieldPatch = Patch.Offset.getPatchFromOffset(
                                Main.simulator.getCurrentFloor(),
                                referencePatch,
                                offset
                        );

                        // If the offset patch was a valid patch, add the floor field value
                        if (floorFieldPatch != null) {
                            PlatformFloorField.addFloorFieldValue(
                                    floorFieldPatch,
                                    currentQueueableTarget,
                                    PlatformFloorFieldState,
                                    floorFieldValue
                            );
                        }
                    }
                }
            }
        }
    }

    // Enable floor fields drawing
    private void beginFloorFieldDrawing() {
        Main.simulator.setFloorFieldDrawing(true);

        // Reset the top bar
        resetTopBar();

        // Redraw interface
        drawInterface(false);
    }

    // Disable floor fields drawing
    public void endFloorFieldDrawing(boolean windowClosedAutomatically) {
        Main.simulator.setFloorFieldDrawing(false);

        // If the window will be closed manually, the current patch hasn't changed - the window is just closed
        // So don't reset the current floor field targets and states
        // Otherwise, proceed with the reset
        if (windowClosedAutomatically) {
            Main.simulator.setCurrentFloorFieldTarget(null);
            Main.simulator.setCurrentFloorFieldState(null);
        }

        // If the window is to be closed automatically, do so
        MainScreenController.normalFloorFieldController.closeWindow();

        // Reset the top bar
        resetTopBar();

        // Redraw interface
        drawInterface(false);
    }

    // Switch to a given floor
    private void switchFloor(Floor floor) {
        // Make the given floor the current floor
        Main.simulator.setCurrentFloor(floor);

        // Update the index of the current floor
        Main.simulator.setCurrentFloorIndex(
                Main.simulator.getStation().getFloors().indexOf(floor)
        );

        // Switch to that floor by redrawing the interface
        drawInterface(false);

        // Update the top bar
        updateTopBar();

        // Reset the top bar
        resetTopBar();
    }

    // Reset the top bar
    private void resetTopBar() {
        // Check if the menu bar may be enabled
        menuBar.setDisable(
                Main.simulator.isPortalDrawing()
                        || Main.simulator.isFloorFieldDrawing()
        );

        // Check if the above and below switch floor buttons may be enabled
        floorBelowButton.setDisable(
                Main.simulator.isPortalDrawing()
                        || Main.simulator.isFloorFieldDrawing()
                        || Main.simulator.getCurrentFloorIndex() == 0
        );

        floorAboveButton.setDisable(
                Main.simulator.isPortalDrawing()
                        || Main.simulator.isFloorFieldDrawing()
                        || Main.simulator.getCurrentFloorIndex() == Main.simulator.getStation().getFloors().size() - 1
        );
    }

    // TODO: Enclose all in Platform.runLater()
    // Disable build mode
    private void disableBuilding(boolean isDisabled) {
        Platform.runLater(() -> {
            if (isDisabled) {
                sidebar.getSelectionModel().select(1);
            } else {
                sidebar.getSelectionModel().select(0);
            }
        });
    }

    // Update the top bar as a while
    private void updateTopBar() {
        Platform.runLater(() -> {
            updateStationNameText();
            updateFloorNumberText();
            updatePromptText();
        });
    }

    // Set the station name text
    private void updateStationNameText() {
        stationNameText.setText(
                Main.simulator.getStation().getName()
        );
    }

    // Set the floor number text
    public void updateFloorNumberText() {
        floorNumberText.setText(
                "Floor #" + (Main.simulator.getCurrentFloorIndex() + 1)
        );
    }

    // Set the prompt text
    public void updatePromptText() {
        String operationModeText = "";
        String buildStateText = "";
        String amenityText = "";

        boolean testing = Simulator.OperationMode.TESTING.equals(Main.simulator.getOperationMode());
        boolean editingAll = Simulator.BuildState.EDITING_ALL.equals(Main.simulator.getBuildState());
        boolean noAmenity = Simulator.BuildSubcategory.NONE.equals(Main.simulator.getBuildSubcategory());

        switch (Main.simulator.getOperationMode()) {
            case BUILDING:
                operationModeText = "Building";

                break;
            case TESTING:
                operationModeText = "Testing";

                break;
        }

        switch (Main.simulator.getBuildState()) {
            case DRAWING:
                buildStateText = ((!noAmenity) ? "Drawing" : "draw");

                break;
            case EDITING_ONE:
                buildStateText = ((!noAmenity) ? "Editing" : "edit");

                break;
            case EDITING_ALL:
                buildStateText = ((!noAmenity) ? "Editing all" : "edit all");

                break;
        }

        switch (Main.simulator.getBuildSubcategory()) {
            case STATION_ENTRANCE_EXIT:
                amenityText = "station entrance/exit" + ((editingAll) ? "s" : "");

                break;
            case SECURITY:
                amenityText = "security gate" + ((editingAll) ? "s" : "");

                break;
            case STAIRS:
                amenityText = "stairs";

                break;
            case ESCALATOR:
                amenityText = "escalator" + ((editingAll) ? "s" : "");

                break;
            case ELEVATOR:
                amenityText = "elevator" + ((editingAll) ? "s" : "");

                break;
            case TICKET_BOOTH:
                amenityText = "ticket booth" + ((editingAll) ? "s" : "");

                break;
            case TURNSTILE:
                amenityText = "turnstile" + ((editingAll) ? "s" : "");

                break;
            case TRAIN_BOARDING_AREA:
                amenityText = "train boarding area" + ((editingAll) ? "s" : "");

                break;
            case TRAIN_TRACK:
                amenityText = "train track" + ((editingAll) ? "s" : "");

                break;
            case OBSTACLE:
                amenityText = "obstacle" + ((editingAll) ? "s" : "");

                break;
        }

        String finalOperationModeText = operationModeText;
        String finalBuildStateText = buildStateText;
        String finalAmenityText = amenityText;

        promptText.setText(
                finalOperationModeText + ((testing) ?
                        ""
                        :
                        ": "
                                + ((!noAmenity) ?
                                finalBuildStateText + " " + finalAmenityText
                                :
                                "Ready to " + finalBuildStateText
                        )
                )
        );
    }

    // Update the simulation time on the screen
    public void updateSimulationTime() {
        LocalTime currentTime = Main.simulator.getSimulationTime().getTime();
        long elapsedTime = Main.simulator.getSimulationTime().getStartTime().until(currentTime, ChronoUnit.SECONDS);

        String timeString;

        timeString = String.format("%02d", currentTime.getHour()) + ":"
                + String.format("%02d", currentTime.getMinute()) + ":"
                + String.format("%02d", currentTime.getSecond());

        elapsedTimeText.setText("Elapsed time: " + timeString + " (" + elapsedTime + " s)");
    }

    // Set the passenger counts
    public void updatePassengerCounts() {
        passengerCountStationText.setText(
                Main.simulator.getStation().getPassengersInStation().size() + " passengers in this station"
        );

        final double passengerCountInFloorPercentage;

        if (Main.simulator.getStation().getPassengersInStation().isEmpty()) {
            passengerCountInFloorPercentage = 0.0;
        } else {
            passengerCountInFloorPercentage = (double) Main.simulator.getCurrentFloor().getPassengersInFloor().size()
                    / Main.simulator.getStation().getPassengersInStation().size() * 100.0;
        }

        passengerCountFloorText.setText(
                Main.simulator.getCurrentFloor().getPassengersInFloor().size() + " passengers in this floor ("
                        + String.format("%.3f", passengerCountInFloorPercentage) + " %)"
        );
    }

    // Save a single amenity or all instances of an amenity in a floor
    private void saveAmenityInFloor(boolean singleAmenity) {
        // Distinguish whether only a single amenity will be saved or not
        if (singleAmenity) {
            saveSingleAmenityInFloor(Main.simulator.getCurrentAmenity());
        } else {
            saveAllAmenitiesInFloor(null);
        }
    }

    // Save the current amenity in a floor
    private void saveSingleAmenityInFloor(Amenity amenityToSave) {
        switch (Main.simulator.getBuildSubcategory()) {
            case STATION_ENTRANCE_EXIT:
                if (!stationGateDirectionListView.getSelectionModel().isEmpty()) {
                    StationGate stationGateToEdit = (StationGate) amenityToSave;

                    boolean wasEnabledPrior = stationGateToEdit.isEnabled();

                    StationGate.stationGateEditor.edit(
                            stationGateToEdit,
                            stationGateEnableCheckBox.isSelected(),
                            stationGateSpawnSpinner.getValue() / 100.0,
                            stationGateModeChoiceBox.getValue(),
                            stationGateDirectionListView.getSelectionModel().getSelectedItems()
                    );

                    // Update the graphic if needed
                    if (wasEnabledPrior != stationGateEnableCheckBox.isSelected()) {
                        ((StationGateGraphic) stationGateToEdit.getGraphicObject()).change();
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Station entrance/exit addition failed",
                            "No travel directions selected",
                            "Please select the travel directions of the passengers that may come from this" +
                                    " gate.",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case SECURITY:
                Security securityToEdit = (Security) amenityToSave;

                Security.securityEditor.edit(
                        securityToEdit,
                        securityEnableCheckBox.isSelected(),
                        securityIntervalSpinner.getValue(),
                        securityBlockPassengerCheckBox.isSelected()
                );

                break;
            case STAIRS:
                StairShaft stairShaftToEdit = (StairShaft) amenityToSave;

                // Retrieve portal components
                StairPortal lowerPortalStairs = (StairPortal) stairShaftToEdit.getLowerPortal();
                StairPortal upperPortalStairs = (StairPortal) stairShaftToEdit.getUpperPortal();

                // Apply the changes from the stair shaft to these portals
                // Only the enabled option is reflected to these portals
                lowerPortalStairs.setEnabled(stairShaftToEdit.isEnabled());
                upperPortalStairs.setEnabled(stairShaftToEdit.isEnabled());

                break;
            case ESCALATOR:
                EscalatorShaft escalatorShaftToEdit = (EscalatorShaft) amenityToSave;

                // Retrieve portal components
                EscalatorPortal lowerPortalEscalator = (EscalatorPortal) escalatorShaftToEdit.getLowerPortal();
                EscalatorPortal upperPortalEscalator = (EscalatorPortal) escalatorShaftToEdit.getUpperPortal();

                // Apply the changes from the escalator shaft to these portals
                // Only the enabled option is reflected to these portals
                lowerPortalEscalator.setEnabled(escalatorShaftToEdit.isEnabled());
                upperPortalEscalator.setEnabled(escalatorShaftToEdit.isEnabled());

                // Update the graphic if needed
                if (escalatorShaftToEdit.hasChangedDirection()) {
                    // Update the graphic along with the direction of the shaft
                    EscalatorGraphic lowerEscalatorGraphic
                            = (EscalatorGraphic) lowerPortalEscalator.getGraphicObject();

                    lowerEscalatorGraphic.change();

                    EscalatorGraphic upperEscalatorGraphic
                            = (EscalatorGraphic) upperPortalEscalator.getGraphicObject();

                    upperEscalatorGraphic.change();

                    escalatorShaftToEdit.setChangedDirection(false);
                }

                break;
            case ELEVATOR:
                ElevatorShaft elevatorShaftToEdit = (ElevatorShaft) amenityToSave;

                // Retrieve portal components
                ElevatorPortal lowerPortalElevator = (ElevatorPortal) elevatorShaftToEdit.getLowerPortal();
                ElevatorPortal upperPortalElevator = (ElevatorPortal) elevatorShaftToEdit.getUpperPortal();

                // Apply the changes from the elevator shaft to these portals
                // Only the enabled option is reflected to these portals
                lowerPortalElevator.setEnabled(elevatorShaftToEdit.isEnabled());
                upperPortalElevator.setEnabled(elevatorShaftToEdit.isEnabled());

                break;
            case TICKET_BOOTH:
                TicketBooth ticketBoothToEdit = (TicketBooth) amenityToSave;

                TicketBooth.ticketBoothEditor.edit(
                        ticketBoothToEdit,
                        ticketBoothEnableCheckBox.isSelected(),
                        ticketBoothIntervalSpinner.getValue(),
                        ticketBoothModeChoiceBox.getValue()
                );

                break;
            case TURNSTILE:
                if (!turnstileDirectionListView.getSelectionModel().isEmpty()) {
                    Turnstile turnstileToEdit = (Turnstile) amenityToSave;

                    Turnstile.turnstileEditor.edit(
                            turnstileToEdit,
                            turnstileEnableCheckBox.isSelected(),
                            turnstileIntervalSpinner.getValue(),
                            turnstileBlockPassengerCheckBox.isSelected(),
                            turnstileModeChoiceBox.getValue(),
                            turnstileDirectionListView.getSelectionModel().getSelectedItems()
                    );
                } else {
                    AlertController.showSimpleAlert(
                            "Turnstile addition failed",
                            "No travel directions selected",
                            "Please select the travel directions supported by this turnstile.",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case TRAIN_BOARDING_AREA:
                if (!trainDoorCarriageListView.getSelectionModel().isEmpty()) {
                    TrainDoor trainDoorToEdit = (TrainDoor) amenityToSave;

                    PassengerMovement.TravelDirection priorPlatform = trainDoorToEdit.getPlatformDirection();
                    Station.StationOrientation priorOrientation = trainDoorToEdit.getTrainDoorOrientation();

                    TrainDoor.trainDoorEditor.edit(
                            trainDoorToEdit,
                            trainDoorEnableCheckBox.isSelected(),
                            trainDoorDirectionChoiceBox.getValue(),
                            trainDoorCarriageListView.getSelectionModel().getSelectedItems(),
                            trainDoorOrientationChoiceBox.getValue(),
                            trainDoorFemalesOnlyCheckBox.isSelected()
                    );

                    // Update the graphic if needed
                    if (
                            priorPlatform != trainDoorDirectionChoiceBox.getValue()
                                    || priorOrientation != trainDoorOrientationChoiceBox.getValue()
                    ) {
                        ((TrainDoorGraphic) trainDoorToEdit.getGraphicObject()).change();
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Train boarding area addition failed",
                            "No carriages selected",
                            "Please select the train carriage(s) supported by this train boarding area",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case TRAIN_TRACK:
                Track trackToEdit = (Track) amenityToSave;
                PassengerMovement.TravelDirection priorTrackDirection = trackToEdit.getTrackDirection();

                Track.trackEditor.edit(
                        trackToEdit,
                        trackDirectionChoiceBox.getValue()
                );

                // Update the graphic if needed
                if (priorTrackDirection != trackToEdit.getTrackDirection()) {
                    ((TrackGraphic) trackToEdit.getGraphicObject()).change();
                }

                break;
            case OBSTACLE:
                Wall wallToEdit = (Wall) amenityToSave;

                Wall.WallType priorWallType = wallToEdit.getWallType();

                Wall.wallEditor.edit(
                        wallToEdit,
                        wallTypeChoiceBox.getValue()
                );

                // Update the graphic if needed
                if (priorWallType != wallTypeChoiceBox.getValue()) {
                    ((WallGraphic) wallToEdit.getGraphicObject()).change();
                }

                break;
        }
    }

    // Save all instances of an amenity in a floor
    private void saveAllAmenitiesInFloor(PortalShaft portalShaft) {
        switch (Main.simulator.getBuildSubcategory()) {
            case STATION_ENTRANCE_EXIT:
                // Edit all station gates
                if (!stationGateDirectionListView.getSelectionModel().isEmpty()) {
                    for (StationGate stationGateToEdit : Main.simulator.getCurrentFloor().getStationGates()) {
                        saveSingleAmenityInFloor(stationGateToEdit);
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Station entrance/exit addition failed",
                            "No travel directions selected",
                            "Please select the travel directions of the passengers that may come from this" +
                                    " gate.",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case SECURITY:
                // Edit all securities
                for (Security securityToEdit : Main.simulator.getCurrentFloor().getSecurities()) {
                    saveSingleAmenityInFloor(securityToEdit);
                }

                break;
            case STAIRS:
                // Edit all stairs
                StairShaft stairShaftReference = (StairShaft) portalShaft;

                for (StairShaft stairShaftToEdit : Main.simulator.getStation().getStairShafts()) {
                    // Retrieve portal components
                    Portal lowerPortal = stairShaftToEdit.getLowerPortal();
                    Portal upperPortal = stairShaftToEdit.getUpperPortal();

                    // Only edit stairs that are in this floor
                    if (
                            lowerPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                                    || upperPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                    ) {
                        // Mirror each stair shaft to the reference shaft
                        StairShaft.stairEditor.edit(
                                stairShaftToEdit,
                                stairShaftReference.isEnabled(),
                                stairShaftReference.getMoveTime(),
                                stairShaftReference.getCapacity()
                        );

                        // Apply the changes from the stair shaft to these portals
                        saveSingleAmenityInFloor(stairShaftToEdit);
                    }
                }

                break;
            case ESCALATOR:
                // Edit all escalators
                EscalatorShaft escalatorShaftReference = (EscalatorShaft) portalShaft;

                for (EscalatorShaft escalatorShaftToEdit : Main.simulator.getStation().getEscalatorShafts()) {
                    // Retrieve portal components
                    Portal lowerPortal = escalatorShaftToEdit.getLowerPortal();
                    Portal upperPortal = escalatorShaftToEdit.getUpperPortal();

                    // Only edit escalators that are in this floor
                    if (
                            lowerPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                                    || upperPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                    ) {
                        // Mirror each escalator shaft to the reference shaft
                        EscalatorShaft.escalatorEditor.edit(
                                escalatorShaftToEdit,
                                escalatorShaftReference.isEnabled(),
                                escalatorShaftReference.getMoveTime(),
                                escalatorShaftReference.getEscalatorDirection(),
                                escalatorShaftReference.getCapacity()
                        );

                        // Apply the changes from the escalator shaft to these portals
                        saveSingleAmenityInFloor(escalatorShaftToEdit);
                    }
                }

                break;
            case ELEVATOR:
                // Edit all elevators
                ElevatorShaft elevatorShaftReference = (ElevatorShaft) portalShaft;

                for (ElevatorShaft elevatorShaftToEdit : Main.simulator.getStation().getElevatorShafts()) {
                    // Retrieve portal components
                    Portal lowerPortal = elevatorShaftToEdit.getLowerPortal();
                    Portal upperPortal = elevatorShaftToEdit.getUpperPortal();

                    // Only edit elevators in this floor
                    if (
                            lowerPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                                    || upperPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                    ) {
                        // Mirror each elevator shaft to the reference shaft
                        ElevatorShaft.elevatorEditor.edit(
                                elevatorShaftToEdit,
                                elevatorShaftReference.isEnabled(),
                                elevatorShaftReference.getOpenDelayTime(),
                                elevatorShaftReference.getDoorOpenTime(),
                                elevatorShaftReference.getMoveTime(),
                                elevatorShaftReference.getElevatorDirection(),
                                elevatorShaftReference.getCapacity()
                        );

                        // Apply the changes from the elevator shaft to these portals
                        saveSingleAmenityInFloor(elevatorShaftToEdit);
                    }
                }

                break;
            case TICKET_BOOTH:
                // Edit all ticket booths
                for (TicketBooth ticketBoothToEdit : Main.simulator.getCurrentFloor().getTicketBooths()) {
                    saveSingleAmenityInFloor(ticketBoothToEdit);
                }

                break;
            case TURNSTILE:
                // Edit all turnstiles
                if (!turnstileDirectionListView.getSelectionModel().isEmpty()) {
                    for (Turnstile turnstileToEdit : Main.simulator.getCurrentFloor().getTurnstiles()) {
                        saveSingleAmenityInFloor(turnstileToEdit);
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Turnstile addition failed",
                            "No travel directions selected",
                            "Please select the travel directions supported by this turnstile.",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case TRAIN_BOARDING_AREA:
                // Edit all train doors
                if (!trainDoorCarriageListView.getSelectionModel().isEmpty()) {
                    for (TrainDoor trainDoorToEdit : Main.simulator.getCurrentFloor().getTrainDoors()) {
                        saveSingleAmenityInFloor(trainDoorToEdit);
                    }
                } else {
                    AlertController.showSimpleAlert(
                            "Train boarding area addition failed",
                            "No carriages selected",
                            "Please select the train carriage(s) supported by these train boarding areas",
                            Alert.AlertType.ERROR
                    );
                }

                break;
            case TRAIN_TRACK:
                // Edit all tracks
                for (Track trackToEdit : Main.simulator.getCurrentFloor().getTracks()) {
                    saveSingleAmenityInFloor(trackToEdit);
                }

                break;
            case OBSTACLE:
                // Edit all walls
                for (Wall wallToEdit : Main.simulator.getCurrentFloor().getWalls()) {
                    saveSingleAmenityInFloor(wallToEdit);
                }

                break;
        }
    }

    // Delete a single amenity or all instances of an amenity in a floor
    private void deleteAmenityInFloor(boolean singleAmenity) {
        // Distinguish whether only a single amenity will be deleted or not
        if (singleAmenity) {
            deleteSingleAmenityInFloor(Main.simulator.getCurrentAmenity());
        } else {
            deleteAllAmenitiesInFloor(Main.simulator.getBuildSubcategory());
        }
    }

    // Delete the current amenity in a floor
    public void deleteSingleAmenityInFloor(Amenity amenityToDelete) {
        // Delete this amenity from the patch that contains it
        // Portal shafts do not have a patch, so ignore if it is
        if (!(amenityToDelete instanceof PortalShaft)) {
            for (Amenity.AmenityBlock amenityBlock : amenityToDelete.getAmenityBlocks()) {
                amenityBlock.getPatch().setAmenityBlock(null);
                amenityBlock.getPatch().signalRemoveAmenityBlock();
            }
        }

        // If this amenity is a queueable, delete all floor fields associated with it
        if (amenityToDelete instanceof Queueable) {
            Queueable queueable = (Queueable) amenityToDelete;

            queueable.deleteAllFloorFields();
        }

        // If this amenity is an elevator shaft, also delete the floor fields of its individual portals
        if (amenityToDelete instanceof ElevatorShaft) {
            ElevatorShaft elevatorShaft = (ElevatorShaft) amenityToDelete;

            if (elevatorShaft.getUpperPortal() != null) {
                ((ElevatorPortal) elevatorShaft.getUpperPortal()).deleteAllFloorFields();
            }

            if (elevatorShaft.getLowerPortal() != null) {
                ((ElevatorPortal) elevatorShaft.getLowerPortal()).deleteAllFloorFields();
            }
        }

        if (amenityToDelete instanceof StationGate) {
            StationGate.stationGateEditor.delete(
                    (StationGate) amenityToDelete
            );
        } else if (amenityToDelete instanceof Security) {
            Security.securityEditor.delete(
                    (Security) amenityToDelete
            );
        } else if (amenityToDelete instanceof StairShaft) {
            StairShaft stairShaftToDelete = (StairShaft) amenityToDelete;

            StairShaft.stairEditor.delete(
                    stairShaftToDelete
            );
        } else if (amenityToDelete instanceof EscalatorShaft) {
            EscalatorShaft escalatorShaftToDelete = (EscalatorShaft) amenityToDelete;

            EscalatorShaft.escalatorEditor.delete(
                    escalatorShaftToDelete
            );
        } else if (amenityToDelete instanceof ElevatorShaft) {
            ElevatorShaft elevatorShaftToDelete = (ElevatorShaft) amenityToDelete;

            ElevatorShaft.elevatorEditor.delete(
                    elevatorShaftToDelete
            );
        } else if (amenityToDelete instanceof TicketBooth) {
            TicketBooth.ticketBoothEditor.delete(
                    (TicketBooth) amenityToDelete
            );
        } else if (amenityToDelete instanceof Turnstile) {
            Turnstile.turnstileEditor.delete(
                    (Turnstile) amenityToDelete
            );
        } else if (amenityToDelete instanceof TrainDoor) {
            TrainDoor.trainDoorEditor.delete(
                    (TrainDoor) amenityToDelete
            );
        } else if (amenityToDelete instanceof Track) {
            Track.trackEditor.delete(
                    (Track) amenityToDelete
            );
        } else if (amenityToDelete instanceof Obstacle) {
            Wall.wallEditor.delete(
                    (Wall) amenityToDelete
            );
        }

        /*switch (buildSubcategory) {
            case STATION_ENTRANCE_EXIT:
                StationGate.stationGateEditor.delete(
                        (StationGate) amenityToDelete
                );

                break;
            case SECURITY:
                Security.securityEditor.delete(
                        (Security) amenityToDelete
                );

                break;
            case STAIRS:
                StairShaft stairShaftToDelete = (StairShaft) amenityToDelete;

                StairShaft.stairEditor.delete(
                        stairShaftToDelete
                );

                break;
            case ESCALATOR:
                EscalatorShaft escalatorShaftToDelete = (EscalatorShaft) amenityToDelete;

                EscalatorShaft.escalatorEditor.delete(
                        escalatorShaftToDelete
                );

                break;
            case ELEVATOR:
                ElevatorShaft elevatorShaftToDelete = (ElevatorShaft) amenityToDelete;

                ElevatorShaft.elevatorEditor.delete(
                        elevatorShaftToDelete
                );

                break;
            case TICKET_BOOTH:
                TicketBooth.ticketBoothEditor.delete(
                        (TicketBooth) amenityToDelete
                );

                break;
            case TURNSTILE:
                Turnstile.turnstileEditor.delete(
                        (Turnstile) amenityToDelete
                );

                break;
            case TRAIN_BOARDING_AREA:
                TrainDoor.trainDoorEditor.delete(
                        (TrainDoor) amenityToDelete
                );

                break;
            case TRAIN_TRACK:
                Track.trackEditor.delete(
                        (Track) amenityToDelete
                );

                break;
            case OBSTACLE:
                Wall.wallEditor.delete(
                        (Wall) amenityToDelete
                );

                break;
        }*/
    }

    // Delete all instances of an amenity in a floor
    public void deleteAllAmenitiesInFloor(Simulator.BuildSubcategory buildSubcategory) {
        switch (buildSubcategory) {
            case STATION_ENTRANCE_EXIT:
                List<StationGate> stationGatesCopy
                        = new ArrayList<>(Main.simulator.getCurrentFloor().getStationGates());

                for (StationGate stationGate : stationGatesCopy) {
                    deleteSingleAmenityInFloor(stationGate);
                }

                break;
            case SECURITY:
                List<Security> securitiesCopy
                        = new ArrayList<>(Main.simulator.getCurrentFloor().getSecurities());

                for (Security securities : securitiesCopy) {
                    deleteSingleAmenityInFloor(securities);
                }

                break;
            case STAIRS:
                List<StairShaft> stairsCopy
                        = new ArrayList<>(Main.simulator.getStation().getStairShafts());

                for (StairShaft stairShaft : stairsCopy) {
                    // Retrieve portal components
                    Portal lowerPortal = stairShaft.getLowerPortal();
                    Portal upperPortal = stairShaft.getUpperPortal();

                    // Only delete stairs that are in this floor
                    if (
                            lowerPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                                    || upperPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                    ) {
                        // Mirror each stair shaft to the reference shaft
                        deleteSingleAmenityInFloor(stairShaft);
                    }
                }

                break;
            case ESCALATOR:
                List<EscalatorShaft> escalatorsCopy
                        = new ArrayList<>(Main.simulator.getStation().getEscalatorShafts());

                for (EscalatorShaft escalatorShaft : escalatorsCopy) {
                    // Retrieve portal components
                    Portal lowerPortal = escalatorShaft.getLowerPortal();
                    Portal upperPortal = escalatorShaft.getUpperPortal();

                    // Only delete escalators that are in this floor
                    if (
                            lowerPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                                    || upperPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                    ) {
                        // Mirror each stair shaft to the reference shaft
                        deleteSingleAmenityInFloor(escalatorShaft);
                    }
                }

                break;
            case ELEVATOR:
                List<ElevatorShaft> elevatorsCopy
                        = new ArrayList<>(Main.simulator.getStation().getElevatorShafts());

                for (ElevatorShaft elevatorShaft : elevatorsCopy) {
                    // Retrieve portal components
                    Portal lowerPortal = elevatorShaft.getLowerPortal();
                    Portal upperPortal = elevatorShaft.getUpperPortal();

                    // Only delete elevators that are in this floor
                    if (
                            lowerPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                                    || upperPortal.getFloorServed() == Main.simulator.getCurrentFloor()
                    ) {
                        // Mirror each stair shaft to the reference shaft
                        deleteSingleAmenityInFloor(elevatorShaft);
                    }
                }

                break;
            case TICKET_BOOTH:
                List<TicketBooth> ticketBoothsCopy
                        = new ArrayList<>(Main.simulator.getCurrentFloor().getTicketBooths());

                for (TicketBooth ticketBooth : ticketBoothsCopy) {
                    deleteSingleAmenityInFloor(ticketBooth);
                }

                break;
            case TURNSTILE:
                List<Turnstile> turnstilesCopy
                        = new ArrayList<>(Main.simulator.getCurrentFloor().getTurnstiles());

                for (Turnstile turnstile : turnstilesCopy) {
                    deleteSingleAmenityInFloor(turnstile);
                }

                break;
            case TRAIN_BOARDING_AREA:
                List<TrainDoor> trainDoorsCopy
                        = new ArrayList<>(Main.simulator.getCurrentFloor().getTrainDoors());

                for (TrainDoor trainDoor : trainDoorsCopy) {
                    deleteSingleAmenityInFloor(trainDoor);
                }

                break;
            case TRAIN_TRACK:
                List<Track> tracksCopy
                        = new ArrayList<>(Main.simulator.getCurrentFloor().getTracks());

                for (Track track : tracksCopy) {
                    deleteSingleAmenityInFloor(track);
                }

                break;
            case OBSTACLE:
                List<Wall> wallsCopy
                        = new ArrayList<>(Main.simulator.getCurrentFloor().getWalls());

                for (Wall wall : wallsCopy) {
                    deleteSingleAmenityInFloor(wall);
                }

                break;
        }
    }

    // Commence with adding a portal
    private void beginPortalDrawing(PortalSetupController portalSetupController) {
        if (portalSetupController instanceof ElevatorSetupController) {
            ElevatorShaft elevatorShaft
                    = (ElevatorShaft) portalSetupController.getWindowOutput().get(ElevatorSetupController.OUTPUT_KEY);

            Main.simulator.setProvisionalPortalShaft(elevatorShaft);
        } else if (portalSetupController instanceof EscalatorSetupController) {
            EscalatorShaft escalatorShaft
                    = (EscalatorShaft) portalSetupController.getWindowOutput().get(EscalatorSetupController.OUTPUT_KEY);

            Main.simulator.setProvisionalPortalShaft(escalatorShaft);
        } else if (portalSetupController instanceof StairSetupController) {
            StairShaft stairShaft
                    = (StairShaft) portalSetupController.getWindowOutput().get(StairSetupController.OUTPUT_KEY);

            Main.simulator.setProvisionalPortalShaft(stairShaft);
        }

        // Portal drawing shall now commence
        Main.simulator.setPortalDrawing(true);

        // Finally, reset the switch floor buttons
        resetTopBar();
    }

    // End adding a portal
    private void endPortalDrawing(boolean completed) {
        PortalShaft portalShaft = Main.simulator.getProvisionalPortalShaft();

        // If the portal drawing has been completed, register the portal shaft to its respective amenity list
        if (!completed) {
            // If the portal drawing sequence was not completed, discard the prematurely added
            // portals to maintain a consistent state
            deleteSingleAmenityInFloor(portalShaft);
        }

        // Discard the portal shaft
        // If the portal drawing was completed in its entirety, this portal shaft will live on in portals that have been
        // added despite having its reference removed from the simulator
        Main.simulator.setProvisionalPortalShaft(null);

        // Portal drawing is now disabled
        Main.simulator.setPortalDrawing(false);
        Main.simulator.setFirstPortalDrawn(false);
        Main.simulator.setFirstPortal(null);

        GraphicsController.floorNextPortal = null;
        GraphicsController.firstPortalAmenityBlocks = null;

        // Finally, reset the switch floor buttons
        resetTopBar();
    }

    // Update the floor field state
    public void updateFloorFieldState(QueueingFloorField.FloorFieldState floorFieldState) {
        // Update the floor field state
        Main.simulator.setCurrentFloorFieldState(floorFieldState);

        // Then redraw the interface so the graphics controller could visualize the changing floor field state
        drawInterface(false);
    }

    // Clear all amenities
    public void clearAmenities() {
        // Delete all amenities
        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.STATION_ENTRANCE_EXIT);
        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.SECURITY);

        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.STAIRS);
        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.ESCALATOR);
        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.ELEVATOR);

        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.TICKET_BOOTH);
        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.TURNSTILE);

        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.TRAIN_BOARDING_AREA);
        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.TRAIN_TRACK);

        Main.mainScreenController.deleteAllAmenitiesInFloor(Simulator.BuildSubcategory.OBSTACLE);

        // Then reset simulator variables
        Main.simulator.setCurrentAmenity(null);
        Main.simulator.setCurrentClass(null);
    }

    private void flipAmenityInFloor(boolean singleAmenity) {
        // Distinguish whether only a single amenity will be flipped or not
        if (singleAmenity) {
            flipSingleAmenityInFloor(Main.simulator.getCurrentAmenity());
        } else {
            flipAllAmenitiesInFloor();
        }
    }

    // Flip a single amenity
    private void flipSingleAmenityInFloor(Amenity amenity) {
        Drawable drawable = (Drawable) amenity;

        if (drawable instanceof Security) {
            ((SecurityGraphic) drawable.getGraphicObject()).cycle();
        } else if (drawable instanceof TicketBooth) {
            ((TicketBoothGraphic) drawable.getGraphicObject()).cycle();
        } else if (drawable instanceof Wall) {
            ((WallGraphic) drawable.getGraphicObject()).cycle();
        }
    }

    // Flip all amenities
    private void flipAllAmenitiesInFloor() {
        switch (Main.simulator.getBuildSubcategory()) {
            case SECURITY:
                for (Security security : Main.simulator.getCurrentFloor().getSecurities()) {
                    flipSingleAmenityInFloor(security);
                }

                break;
            case TICKET_BOOTH:
                for (TicketBooth ticketBooth : Main.simulator.getCurrentFloor().getTicketBooths()) {
                    flipSingleAmenityInFloor(ticketBooth);
                }

                break;
            case OBSTACLE:
                for (Wall wall : Main.simulator.getCurrentFloor().getWalls()) {
                    flipSingleAmenityInFloor(wall);
                }

                break;
        }
    }

    // Contains actions for building or editing
    public void buildOrEdit(Patch currentPatch) throws IOException, InterruptedException {
        // Get the current operation mode, category, and subcategory
        Simulator.OperationMode operationMode = Main.simulator.getOperationMode();
        Simulator.BuildSubcategory buildSubcategory = Main.simulator.getBuildSubcategory();
        Simulator.BuildState buildState = Main.simulator.getBuildState();

        // If the operation mode is building, the user either wants to draw or edit (one or all)
        if (operationMode == Simulator.OperationMode.BUILDING) {
            // Draw depending on the subcategory
            switch (buildSubcategory) {
                case STATION_ENTRANCE_EXIT:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(StationGate.class);

                                if (!stationGateDirectionListView.getSelectionModel().isEmpty()) {
                                    StationGate.stationGateEditor.draw(
                                            currentPatch,
                                            stationGateEnableCheckBox.isSelected(),
                                            stationGateSpawnSpinner.getValue() / 100.0,
                                            stationGateModeChoiceBox.getValue(),
                                            stationGateDirectionListView.getSelectionModel().getSelectedItems()
                                    );
                                } else {
                                    AlertController.showSimpleAlert(
                                            "Station entrance/exit addition failed",
                                            "No travel directions selected",
                                            "Please select the travel directions of the passengers that may" +
                                                    " come from this gate.",
                                            Alert.AlertType.ERROR
                                    );
                                }
                            } else {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // Only edit if there is already a station gate on that patch
                            if (Main.simulator.getCurrentAmenity() instanceof StationGate) {
                                Main.simulator.setCurrentClass(StationGate.class);

                                StationGate stationGateToEdit
                                        = (StationGate) Main.simulator.getCurrentAmenity();

                                stationGateEnableCheckBox.setSelected(
                                        stationGateToEdit.isEnabled()
                                );

                                stationGateSpawnSpinner.getValueFactory().setValue(
                                        (int) (stationGateToEdit.getChancePerSecond() * 100)
                                );

                                stationGateModeChoiceBox.setValue(
                                        stationGateToEdit.getStationGateMode()
                                );

                                stationGateDirectionListView.getSelectionModel().clearSelection();

                                for (
                                        PassengerMovement.TravelDirection travelDirection
                                        : stationGateToEdit.getStationGatePassengerTravelDirections()
                                ) {
                                    stationGateDirectionListView.getSelectionModel().select(travelDirection);
                                }
                            } else {
                                // If there is no amenity there, just do nothing
                                if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                    // If clicked on an existing amenity, switch to editing mode, then open that
                                    // amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case SECURITY:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(Security.class);

                                Security.securityEditor.draw(
                                        currentPatch,
                                        securityEnableCheckBox.isSelected(),
                                        securityIntervalSpinner.getValue(),
                                        securityBlockPassengerCheckBox.isSelected()
                                );
                            } else {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // When in adding floor fields mode, draw a floor field instead
                            // Otherwise, just enable the controls in the sidebar
                            if (!Main.simulator.isFloorFieldDrawing()) {
                                // Only edit if there is already a security gate on that patch
                                if (Main.simulator.getCurrentAmenity() instanceof Security) {
                                    Main.simulator.setCurrentClass(Security.class);

                                    Security securityToEdit
                                            = (Security) Main.simulator.getCurrentAmenity();

                                    // Take note of this amenity as the one that will own the floor fields once
                                    // drawn
                                    Main.simulator.setCurrentFloorFieldTarget(securityToEdit);

                                    // Update the direction choice box
                                    MainScreenController.normalFloorFieldController.updateDirectionChoiceBox();
                                    MainScreenController.normalFloorFieldController.updateLocationChoiceBox();

                                    // Also take note of the current floor field state
                                    QueueingFloorField.FloorFieldState floorFieldState
                                            = normalFloorFieldController.getFloorFieldState();

                                    Main.simulator.setCurrentFloorFieldState(floorFieldState);

                                    // Set the forms to the proper position
                                    securityEnableCheckBox.setSelected(
                                            securityToEdit.isEnabled()
                                    );

                                    securityBlockPassengerCheckBox.setSelected(
                                            securityToEdit.blockEntry()
                                    );

                                    securityIntervalSpinner.getValueFactory().setValue(
                                            securityToEdit.getWaitingTime()
                                    );
                                } else {
                                    // If there is no amenity there, just do nothing
                                    if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                        // If clicked on an existing amenity, switch to editing mode, then open that
                                        // amenity's controls
                                        goToAmenityControls(Main.simulator.getCurrentAmenity());

                                        // Then revisit this method as if that amenity was clicked
                                        buildOrEdit(currentPatch);
                                    }
                                }
                            } else {
                                // If there is an empty patch here, draw the floor field value
                                if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                    // Define the target and the floor field state
                                    Security target = (Security) Main.simulator.getCurrentFloorFieldTarget();

                                    // If a floor field value is supposed to be drawn, then go ahead and draw
                                    if (MainScreenController.normalFloorFieldController.getFloorFieldMode()
                                            == NormalFloorFieldController.FloorFieldMode.DRAWING) {
                                        if (!QueueingFloorField.addFloorFieldValue(
                                                currentPatch,
                                                target,
                                                MainScreenController.normalFloorFieldController.getFloorFieldState(),
                                                MainScreenController.normalFloorFieldController.getIntensity()
                                        )) {
                                            // Let the user know if the addition of the floor field value has
                                            // failed
                                            AlertController.showSimpleAlert(
                                                    "Floor field value addition failed",
                                                    "Failed to add a floor field value here",
                                                    "This floor field already has an apex.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // If a floor field value is supposed to be deleted, then go ahead and
                                        // delete it
                                        QueueingFloorField.deleteFloorFieldValue(
                                                currentPatch,
                                                target,
                                                MainScreenController.normalFloorFieldController.getFloorFieldState()
                                        );
                                    }
                                } else {
                                    // If it is a different amenity, turn off floor fields mode
                                    endFloorFieldDrawing(true);

                                    // Switch to editing mode, then open that amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case STAIRS:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(StairPortal.class);

                                // Only add amenities on patches which are empty and do not have floor field values on them
                                // Otherwise, do nothing
                                if (currentPatch.getFloorFieldValues().isEmpty()) {
                                    // If a first portal has already been added, add the second portal this
                                    // click
                                    if (!Main.simulator.isFirstPortalDrawn()) {
                                        // Only add an portal when there are multiple floors
                                        if (Main.simulator.getStation().getFloors().size() > 1) {
                                            // If the user has clicked on a patch without the portal being setup yet,
                                            // show the setup first, then automatically put the first portal where the
                                            // user clicked
                                            if (!Main.simulator.isPortalDrawing()) {
                                                // Display the portal setup prompt
                                                FXMLLoader loader = ScreenController.getLoader(
                                                        getClass(),
                                                        "/com/crowdsimulation/view" +
                                                                "/StairSetupInterface.fxml");
                                                Parent root = loader.load();

                                                StairSetupController stairSetupController = loader.getController();
                                                stairSetupController.setElements();

                                                // Show the window
                                                stairSetupController.showWindow(
                                                        root,
                                                        "Staircase setup",
                                                        true,
                                                        false
                                                );

                                                // Only proceed when this window is closed through the proceed action
                                                if (stairSetupController.isClosedWithAction()) {
                                                    beginPortalDrawing(stairSetupController);
                                                }
                                            }

                                            // Only continue when the portal setup has been completed
                                            if (Main.simulator.isPortalDrawing()) {
                                                // Setup has already been shown, so we may now draw the first portal in
                                                // peace
                                                // Prepare the first portal that will be placed on this floor
                                                StairPortal stairPortalToAdd
                                                        = StairShaft.stairEditor.createPortal(
                                                        currentPatch,
                                                        Main.simulator.getProvisionalPortalShaft().isEnabled(),
                                                        Main.simulator.getCurrentFloor(),
                                                        (StairShaft) Main.simulator.getProvisionalPortalShaft()
                                                );

                                                // Only add the first portal when there is a valid place to put it in
                                                if (stairPortalToAdd != null) {
                                                    // The first portal has now been drawn
                                                    Main.simulator.setFirstPortalDrawn(true);
                                                    Main.simulator.setFirstPortal(stairPortalToAdd);

                                                    // Redraw the interface to briefly show the newly added elevator
                                                    drawInterface(false);

                                                    // Show the window for choosing the floor where the next portal will be
                                                    FXMLLoader loader = ScreenController.getLoader(
                                                            getClass(),
                                                            "/com/crowdsimulation/view" +
                                                                    "/PortalFloorSelectorInterface.fxml");
                                                    Parent root = loader.load();

                                                    PortalFloorSelectorController portalFloorSelectorController
                                                            = loader.getController();
                                                    portalFloorSelectorController.setElements();

                                                    portalFloorSelectorController.showWindow(
                                                            root,
                                                            "Choose the floor of the second stair landing",
                                                            true,
                                                            true
                                                    );

                                                    // Only continue when the floor selection has been completed
                                                    if (portalFloorSelectorController.isClosedWithAction()) {
                                                        // A floor has already been chosen, now retrieve that floor and go there
                                                        Floor chosenFloor = (Floor) portalFloorSelectorController
                                                                .getWindowOutput()
                                                                .get(PortalFloorSelectorController.OUTPUT_KEY);

                                                        // After the chosen floor was selected, we may now set the
                                                        // provisional portal shaft with one of the portals, now that we
                                                        // know which one is the portal located in the upper or lower
                                                        // floor
                                                        List<Floor> floors = Main.simulator.getStation().getFloors();

                                                        // If the current floor is lower than the chosen floor, this
                                                        // current floor will be the lower portal
                                                        if (floors.indexOf(Main.simulator.getCurrentFloor())
                                                                < floors.indexOf(chosenFloor)) {
                                                            Main.simulator.getProvisionalPortalShaft().setLowerPortal(
                                                                    stairPortalToAdd
                                                            );
                                                        } else {
                                                            Main.simulator.getProvisionalPortalShaft().setUpperPortal(
                                                                    stairPortalToAdd
                                                            );
                                                        }

                                                        // On the added floor, mark the position of the added portal
                                                        GraphicsController.floorNextPortal = chosenFloor;
                                                        GraphicsController.firstPortalAmenityBlocks
                                                                = stairPortalToAdd.getAmenityBlocks();

                                                        // Switch to that floor
                                                        switchFloor(chosenFloor);

                                                        // Prompt the user that it is now time to draw the second portal
                                                        AlertController.showSimpleAlert(
                                                                "Add second stair landing",
                                                                "Draw the second stair landing",
                                                                "After closing this window, please draw the" +
                                                                        " second stair landing on this floor. Click X to" +
                                                                        " cancel this operation.",
                                                                Alert.AlertType.INFORMATION
                                                        );
                                                    } else {
                                                        // Cancel portal adding
                                                        // Also delete the earlier added portal shafts and portals
                                                        endPortalDrawing(false);
                                                    }
                                                } else {
                                                    // Cancel portal adding
                                                    // Also delete the earlier added portal shafts and portals
                                                    endPortalDrawing(false);
                                                }
                                            }
                                        } else {
                                            AlertController.showSimpleAlert(
                                                    "Staircase addition failed",
                                                    "Unable to add staircase",
                                                    "You may only add staircases when there are more than one floors in the station.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // Prepare the second portal that will be placed on this floor
                                        StairPortal stairPortalToAdd = StairShaft.stairEditor.createPortal(
                                                currentPatch,
                                                Main.simulator.getProvisionalPortalShaft().isEnabled(),
                                                Main.simulator.getCurrentFloor(),
                                                (StairShaft) Main.simulator.getProvisionalPortalShaft()
                                        );


                                        // Only add the first portal when there is a valid place to put it in
                                        if (stairPortalToAdd != null) {
                                            // If the upper portal has already been set, then this portal will be the
                                            // lower one (and vice versa)
                                            if (Main.simulator.getProvisionalPortalShaft().getUpperPortal() == null) {
                                                Main.simulator.getProvisionalPortalShaft().setUpperPortal(
                                                        stairPortalToAdd
                                                );

                                                stairPortalToAdd.setPair(
                                                        Main.simulator.getProvisionalPortalShaft().getLowerPortal()
                                                );

                                                Main.simulator.getProvisionalPortalShaft().getLowerPortal().setPair(
                                                        stairPortalToAdd
                                                );
                                            } else {
                                                Main.simulator.getProvisionalPortalShaft().setLowerPortal(
                                                        stairPortalToAdd
                                                );

                                                stairPortalToAdd.setPair(
                                                        Main.simulator.getProvisionalPortalShaft().getUpperPortal()
                                                );

                                                Main.simulator.getProvisionalPortalShaft().getUpperPortal().setPair(
                                                        stairPortalToAdd
                                                );
                                            }

                                            StairShaft stairShaft
                                                    = (StairShaft) Main.simulator.getProvisionalPortalShaft();

                                            // Register the provisional shaft to the station
                                            Main.simulator.getStation().getStairShafts().add(
                                                    stairShaft
                                            );

                                            Main.simulator.getStation().getStairPortalsByFloor().get(
                                                    stairShaft.getLowerPortal().getFloorServed()
                                            ).add((StairPortal) stairShaft.getLowerPortal());

                                            Main.simulator.getStation().getStairPortalsByFloor().get(
                                                    stairShaft.getUpperPortal().getFloorServed()
                                            ).add((StairPortal) stairShaft.getUpperPortal());

                                            // Update the graphics of the two added portals
                                            ((StairGraphic) stairShaft.getLowerPortal().getGraphicObject())
                                                    .decideLowerOrUpper();
                                            ((StairGraphic) stairShaft.getUpperPortal().getGraphicObject())
                                                    .decideLowerOrUpper();

                                            // Finish adding the portal
                                            endPortalDrawing(true);

                                            // Again, briefly redraw the interface to let the user have a glimpse at
                                            // the newly added elevator
                                            drawInterface(false);

                                            // Let the user know that portal addition has been successful
                                            AlertController.showSimpleAlert(
                                                    "Staircase addition successful",
                                                    "Staircase successfully added",
                                                    "The staircase has been successfully added.",
                                                    Alert.AlertType.INFORMATION
                                            );
                                        } else {
                                            // Cancel portal adding
                                            // Also delete the earlier added portal shafts and portals
                                            endPortalDrawing(false);
                                        }
                                    }
                                } else {
                                    // We will end up here if a first portal has been added on an empty patch,
                                    // but the second one on a patch with a floor field
                                    if (Main.simulator.isFirstPortalDrawn()) {
                                        // Cancel portal adding
                                        // Also delete the earlier added portal shafts and portals
                                        endPortalDrawing(false);
                                    }
                                }
                            } else {
                                // If clicked on an existing amenity in the middle of adding the portals, cancel
                                // portal adding
                                if (Main.simulator.isPortalDrawing()) {
                                    endPortalDrawing(false);
                                }

                                // Switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // Only edit if there is already a staircase on that patch
                            if (Main.simulator.getCurrentAmenity() instanceof StairPortal) {
                                Main.simulator.setCurrentClass(ElevatorPortal.class);

                                StairPortal stairPortalToEdit
                                        = (StairPortal) Main.simulator.getCurrentAmenity();

                                // Do nothing afterwards, editing is handled by the sidebar button
                            } else {
                                // If there is no amenity there, just do nothing
                                if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                    // If clicked on an existing amenity, switch to editing mode, then open that
                                    // amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case ESCALATOR:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(EscalatorPortal.class);

                                // Only add amenities on patches which are empty and do not have floor field values on them
                                // Otherwise, do nothing
                                if (currentPatch.getFloorFieldValues().isEmpty()) {
                                    // If a first portal has already been added, add the second portal this
                                    // click
                                    if (!Main.simulator.isFirstPortalDrawn()) {
                                        // Only add an portal when there are multiple floors
                                        if (Main.simulator.getStation().getFloors().size() > 1) {
                                            // If the user has clicked on a patch without the portal being setup yet,
                                            // show the setup first, then automatically put the first portal where the
                                            // user clicked
                                            if (!Main.simulator.isPortalDrawing()) {
                                                // Display the portal setup prompt
                                                FXMLLoader loader = ScreenController.getLoader(
                                                        getClass(),
                                                        "/com/crowdsimulation/view" +
                                                                "/EscalatorSetupInterface.fxml");
                                                Parent root = loader.load();

                                                EscalatorSetupController escalatorSetupController;

                                                escalatorSetupController = loader.getController();
                                                escalatorSetupController.setElements();

                                                // Show the window
                                                escalatorSetupController.showWindow(
                                                        root,
                                                        "Escalator setup",
                                                        true,
                                                        false
                                                );

                                                // Only proceed when this window is closed through the proceed action
                                                if (escalatorSetupController.isClosedWithAction()) {
                                                    beginPortalDrawing(escalatorSetupController);
                                                }
                                            }

                                            // Only continue when the portal setup has been completed
                                            if (Main.simulator.isPortalDrawing()) {
                                                // Setup has already been shown, so we may now draw the first portal in
                                                // peace
                                                // Prepare the first portal that will be placed on this floor
                                                EscalatorPortal escalatorPortalToAdd
                                                        = EscalatorShaft.escalatorEditor.createPortal(
                                                        currentPatch,
                                                        Main.simulator.getProvisionalPortalShaft().isEnabled(),
                                                        Main.simulator.getCurrentFloor(),
                                                        (EscalatorShaft) Main.simulator.getProvisionalPortalShaft()
                                                );

                                                // Only add the first portal when there is a valid place to put it in
                                                if (escalatorPortalToAdd != null) {
                                                    // The first portal has now been drawn
                                                    Main.simulator.setFirstPortalDrawn(true);
                                                    Main.simulator.setFirstPortal(escalatorPortalToAdd);

                                                    // Redraw the interface to briefly show the newly added elevator
                                                    drawInterface(false);

                                                    // Show the window for choosing the floor where the next portal will be
                                                    FXMLLoader loader = ScreenController.getLoader(
                                                            getClass(),
                                                            "/com/crowdsimulation/view" +
                                                                    "/PortalFloorSelectorInterface.fxml");
                                                    Parent root = loader.load();

                                                    PortalFloorSelectorController portalFloorSelectorController
                                                            = loader.getController();
                                                    portalFloorSelectorController.setElements();

                                                    portalFloorSelectorController.showWindow(
                                                            root,
                                                            "Choose the floor of the second escalator",
                                                            true,
                                                            true
                                                    );

                                                    // Only continue when the floor selection has been completed
                                                    if (portalFloorSelectorController.isClosedWithAction()) {
                                                        // A floor has already been chosen, now retrieve that floor and go there
                                                        Floor chosenFloor = (Floor) portalFloorSelectorController
                                                                .getWindowOutput()
                                                                .get(PortalFloorSelectorController.OUTPUT_KEY);

                                                        // After the chosen floor was selected, we may now set the
                                                        // provisional portal shaft with one of the portals, now that we
                                                        // know which one is the portal located in the upper or lower
                                                        // floor
                                                        List<Floor> floors = Main.simulator.getStation().getFloors();

                                                        // If the current floor is lower than the chosen floor, this
                                                        // current floor will be the lower portal
                                                        if (floors.indexOf(Main.simulator.getCurrentFloor())
                                                                < floors.indexOf(chosenFloor)) {
                                                            Main.simulator.getProvisionalPortalShaft().setLowerPortal(
                                                                    escalatorPortalToAdd
                                                            );
                                                        } else {
                                                            Main.simulator.getProvisionalPortalShaft().setUpperPortal(
                                                                    escalatorPortalToAdd
                                                            );
                                                        }

                                                        // On the added floor, mark the position of the added portal
                                                        GraphicsController.floorNextPortal = chosenFloor;
                                                        GraphicsController.firstPortalAmenityBlocks
                                                                = escalatorPortalToAdd.getAmenityBlocks();

                                                        // Switch to that floor
                                                        switchFloor(chosenFloor);

                                                        // Prompt the user that it is now time to draw the second portal
                                                        AlertController.showSimpleAlert(
                                                                "Add second escalator",
                                                                "Draw the second escalator",
                                                                "After closing this window, please draw the" +
                                                                        " second escalator on this floor. Click X to" +
                                                                        " cancel this operation.",
                                                                Alert.AlertType.INFORMATION
                                                        );
                                                    } else {
                                                        // Cancel portal adding
                                                        // Also delete the earlier added portal shafts and portals
                                                        endPortalDrawing(false);
                                                    }
                                                } else {
                                                    // Cancel portal adding
                                                    // Also delete the earlier added portal shafts and portals
                                                    endPortalDrawing(false);
                                                }
                                            }
                                        } else {
                                            AlertController.showSimpleAlert(
                                                    "Escalator addition failed",
                                                    "Unable to add escalator",
                                                    "You may only add escalators when there are more than one floors in the station.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // Prepare the second portal that will be placed on this floor
                                        EscalatorPortal escalatorPortalToAdd
                                                = EscalatorShaft.escalatorEditor.createPortal(
                                                currentPatch,
                                                Main.simulator.getProvisionalPortalShaft().isEnabled(),
                                                Main.simulator.getCurrentFloor(),
                                                (EscalatorShaft) Main.simulator.getProvisionalPortalShaft()
                                        );

                                        // Only add the first portal when there is a valid place to put it in
                                        if (escalatorPortalToAdd != null) {
                                            // If the upper portal has already been set, then this portal will be the
                                            // lower one (and vice versa)
                                            if (Main.simulator.getProvisionalPortalShaft().getUpperPortal() == null) {
                                                Main.simulator.getProvisionalPortalShaft().setUpperPortal(
                                                        escalatorPortalToAdd
                                                );

                                                escalatorPortalToAdd.setPair(
                                                        Main.simulator.getProvisionalPortalShaft().getLowerPortal()
                                                );

                                                Main.simulator.getProvisionalPortalShaft().getLowerPortal().setPair(
                                                        escalatorPortalToAdd
                                                );
                                            } else {
                                                Main.simulator.getProvisionalPortalShaft().setLowerPortal(
                                                        escalatorPortalToAdd
                                                );

                                                escalatorPortalToAdd.setPair(
                                                        Main.simulator.getProvisionalPortalShaft().getUpperPortal()
                                                );

                                                Main.simulator.getProvisionalPortalShaft().getUpperPortal().setPair(
                                                        escalatorPortalToAdd
                                                );
                                            }

                                            EscalatorShaft escalatorShaft
                                                    = (EscalatorShaft) Main.simulator.getProvisionalPortalShaft();

                                            // Register the provisional shaft to the station
                                            Main.simulator.getStation().getEscalatorShafts().add(
                                                    escalatorShaft
                                            );

                                            Main.simulator.getStation().getEscalatorPortalsByFloor().get(
                                                    escalatorShaft.getLowerPortal().getFloorServed()
                                            ).add((EscalatorPortal) escalatorShaft.getLowerPortal());

                                            Main.simulator.getStation().getEscalatorPortalsByFloor().get(
                                                    escalatorShaft.getUpperPortal().getFloorServed()
                                            ).add((EscalatorPortal) escalatorShaft.getUpperPortal());

                                            // Update the graphics of the two added portals
                                            ((EscalatorGraphic) escalatorShaft.getLowerPortal().getGraphicObject())
                                                    .decideLowerOrUpper();
                                            ((EscalatorGraphic) escalatorShaft.getUpperPortal().getGraphicObject())
                                                    .decideLowerOrUpper();

                                            // Finish adding the portal
                                            endPortalDrawing(true);

                                            // Again, briefly redraw the interface to let the user have a glimpse at
                                            // the newly added elevator
                                            drawInterface(false);

                                            // Let the user know that portal addition has been successful
                                            AlertController.showSimpleAlert(
                                                    "Escalator addition successful",
                                                    "Escalator successfully added",
                                                    "The escalator has been successfully added.",
                                                    Alert.AlertType.INFORMATION
                                            );
                                        } else {
                                            // Cancel portal adding
                                            // Also delete the earlier added portal shafts and portals
                                            endPortalDrawing(false);
                                        }
                                    }
                                } else {
                                    // We will end up here if a first portal has been added on an empty patch,
                                    // but the second one on a patch with a floor field
                                    if (Main.simulator.isFirstPortalDrawn()) {
                                        // Cancel portal adding
                                        // Also delete the earlier added portal shafts and portals
                                        endPortalDrawing(false);
                                    }
                                }
                            } else {
                                // If clicked on an existing amenity in the middle of adding the portals, cancel
                                // portal adding
                                if (Main.simulator.isPortalDrawing()) {
                                    endPortalDrawing(false);
                                }

                                // Switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // Only edit if there is already an escalator on that patch
                            if (Main.simulator.getCurrentAmenity() instanceof EscalatorPortal) {
                                Main.simulator.setCurrentClass(EscalatorPortal.class);

                                EscalatorPortal escalatorPortalToEdit
                                        = (EscalatorPortal) Main.simulator.getCurrentAmenity();

                                // Do nothing afterwards, editing is handled by the sidebar button
                            } else {
                                // If there is no amenity there, just do nothing
                                if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                    // If clicked on an existing amenity, switch to editing mode, then open that
                                    // amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case ELEVATOR:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(ElevatorPortal.class);

                                // Only add amenities on patches which are empty and do not have floor field values on them
                                // Otherwise, do nothing
                                if (currentPatch.getFloorFieldValues().isEmpty()) {
                                    // If a first portal has already been added, add the second portal this
                                    // click
                                    if (!Main.simulator.isFirstPortalDrawn()) {
                                        // Only add an portal when there are multiple floors
                                        if (Main.simulator.getStation().getFloors().size() > 1) {
                                            // If the user has clicked on a patch without the portal being setup yet,
                                            // show the setup first, then automatically put the first portal where the
                                            // user clicked
                                            if (!Main.simulator.isPortalDrawing()) {
                                                // Display the portal setup prompt
                                                FXMLLoader loader = ScreenController.getLoader(
                                                        getClass(),
                                                        "/com/crowdsimulation/view" +
                                                                "/ElevatorSetupInterface.fxml");
                                                Parent root = loader.load();

                                                ElevatorSetupController elevatorSetupController;

                                                elevatorSetupController = loader.getController();
                                                elevatorSetupController.setElements();

                                                // Show the window
                                                elevatorSetupController.showWindow(
                                                        root,
                                                        "Elevator setup",
                                                        true,
                                                        false
                                                );

                                                // Only proceed when this window is closed through the proceed action
                                                if (elevatorSetupController.isClosedWithAction()) {
                                                    beginPortalDrawing(elevatorSetupController);
                                                }
                                            }

                                            // Only continue when the portal setup has been completed
                                            if (Main.simulator.isPortalDrawing()) {
                                                // Setup has already been shown, so we may now draw the first portal in
                                                // peace
                                                // Prepare the first portal that will be placed on this floor
                                                ElevatorPortal elevatorPortalToAdd
                                                        = ElevatorShaft.elevatorEditor.createPortal(
                                                        currentPatch,
                                                        Main.simulator.getProvisionalPortalShaft().isEnabled(),
                                                        Main.simulator.getCurrentFloor(),
                                                        (ElevatorShaft) Main.simulator.getProvisionalPortalShaft()
                                                );

                                                // Only add the first portal when there is a valid place to put it in
                                                if (elevatorPortalToAdd != null) {
                                                    // The first portal has now been drawn
                                                    Main.simulator.setFirstPortalDrawn(true);
                                                    Main.simulator.setFirstPortal(elevatorPortalToAdd);

                                                    // Redraw the interface to briefly show the newly added elevator
                                                    drawInterface(false);

                                                    // Show the window for choosing the floor where the next portal will be
                                                    FXMLLoader loader = ScreenController.getLoader(
                                                            getClass(),
                                                            "/com/crowdsimulation/view" +
                                                                    "/PortalFloorSelectorInterface.fxml");
                                                    Parent root = loader.load();

                                                    PortalFloorSelectorController portalFloorSelectorController
                                                            = loader.getController();
                                                    portalFloorSelectorController.setElements();

                                                    portalFloorSelectorController.showWindow(
                                                            root,
                                                            "Choose the floor of the second elevator",
                                                            true,
                                                            true
                                                    );

                                                    // Only continue when the floor selection has been completed
                                                    if (portalFloorSelectorController.isClosedWithAction()) {
                                                        // A floor has already been chosen, now retrieve that floor and go there
                                                        Floor chosenFloor = (Floor) portalFloorSelectorController
                                                                .getWindowOutput()
                                                                .get(PortalFloorSelectorController.OUTPUT_KEY);

                                                        // After the chosen floor was selected, we may now set the
                                                        // provisional portal shaft with one of the portals, now that we
                                                        // know which one is the portal located in the upper or lower
                                                        // floor
                                                        List<Floor> floors = Main.simulator.getStation().getFloors();

                                                        // If the current floor is lower than the chosen floor, this
                                                        // current floor will be the lower portal
                                                        if (floors.indexOf(Main.simulator.getCurrentFloor())
                                                                < floors.indexOf(chosenFloor)) {
                                                            Main.simulator.getProvisionalPortalShaft().setLowerPortal(
                                                                    elevatorPortalToAdd
                                                            );
                                                        } else {
                                                            Main.simulator.getProvisionalPortalShaft().setUpperPortal(
                                                                    elevatorPortalToAdd
                                                            );
                                                        }

                                                        // On the added floor, mark the position of the added portal
                                                        GraphicsController.floorNextPortal = chosenFloor;
                                                        GraphicsController.firstPortalAmenityBlocks
                                                                = elevatorPortalToAdd.getAmenityBlocks();

                                                        // Switch to that floor
                                                        switchFloor(chosenFloor);

                                                        // Prompt the user that it is now time to draw the second portal
                                                        AlertController.showSimpleAlert(
                                                                "Add second elevator",
                                                                "Draw the second elevator",
                                                                "After closing this window, please draw the" +
                                                                        " second elevator on this floor. Click X to" +
                                                                        " cancel this operation.",
                                                                Alert.AlertType.INFORMATION
                                                        );
                                                    } else {
                                                        // Cancel portal adding
                                                        // Also delete the earlier added portal shafts and portals
                                                        endPortalDrawing(false);
                                                    }
                                                } else {
                                                    // Cancel portal adding
                                                    // Also delete the earlier added portal shafts and portals
                                                    endPortalDrawing(false);
                                                }
                                            }
                                        } else {
                                            AlertController.showSimpleAlert(
                                                    "Elevator addition failed",
                                                    "Unable to add elevator",
                                                    "You may only add elevators when there are more than one floors in the station.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // Prepare the second portal that will be placed on this floor
                                        ElevatorPortal elevatorPortalToAdd
                                                = ElevatorShaft.elevatorEditor.createPortal(
                                                currentPatch,
                                                Main.simulator.getProvisionalPortalShaft().isEnabled(),
                                                Main.simulator.getCurrentFloor(),
                                                (ElevatorShaft) Main.simulator.getProvisionalPortalShaft()
                                        );

                                        // Only add the first portal when there is a valid place to put it in
                                        if (elevatorPortalToAdd != null) {
                                            // If the upper portal has already been set, then this portal will be the
                                            // lower one (and vice versa)
                                            if (Main.simulator.getProvisionalPortalShaft().getUpperPortal() == null) {
                                                Main.simulator.getProvisionalPortalShaft().setUpperPortal(
                                                        elevatorPortalToAdd
                                                );

                                                elevatorPortalToAdd.setPair(
                                                        Main.simulator.getProvisionalPortalShaft().getLowerPortal()
                                                );

                                                Main.simulator.getProvisionalPortalShaft().getLowerPortal().setPair(
                                                        elevatorPortalToAdd
                                                );
                                            } else {
                                                Main.simulator.getProvisionalPortalShaft().setLowerPortal(
                                                        elevatorPortalToAdd
                                                );

                                                elevatorPortalToAdd.setPair(
                                                        Main.simulator.getProvisionalPortalShaft().getUpperPortal()
                                                );

                                                Main.simulator.getProvisionalPortalShaft().getUpperPortal().setPair(
                                                        elevatorPortalToAdd
                                                );
                                            }

                                            ElevatorShaft elevatorShaft
                                                    = (ElevatorShaft) Main.simulator.getProvisionalPortalShaft();

                                            // Register the provisional shaft to the station
                                            Main.simulator.getStation().getElevatorShafts().add(
                                                    elevatorShaft
                                            );

                                            Main.simulator.getStation().getElevatorPortalsByFloor().get(
                                                    elevatorShaft.getLowerPortal().getFloorServed()
                                            ).add((ElevatorPortal) elevatorShaft.getLowerPortal());

                                            Main.simulator.getStation().getElevatorPortalsByFloor().get(
                                                    elevatorShaft.getUpperPortal().getFloorServed()
                                            ).add((ElevatorPortal) elevatorShaft.getUpperPortal());

                                            // Finish adding the portal
                                            endPortalDrawing(true);

                                            // Again, briefly redraw the interface to let the user have a glimpse at
                                            // the newly added elevator
                                            drawInterface(false);

                                            // Let the user know that portal addition has been successful
                                            AlertController.showSimpleAlert(
                                                    "Elevator addition successful",
                                                    "Elevator successfully added",
                                                    "The elevator has been successfully added.",
                                                    Alert.AlertType.INFORMATION
                                            );
                                        } else {
                                            // Cancel portal adding
                                            // Also delete the earlier added portal shafts and portals
                                            endPortalDrawing(false);
                                        }
                                    }
                                } else {
                                    // We will end up here if a first portal has been added on an empty patch,
                                    // but the second one on a patch with a floor field
                                    if (Main.simulator.isFirstPortalDrawn()) {
                                        // Cancel portal adding
                                        // Also delete the earlier added portal shafts and portals
                                        endPortalDrawing(false);
                                    }
                                }
                            } else {
                                // If clicked on an existing amenity in the middle of adding the portals, cancel
                                // portal adding
                                if (Main.simulator.isPortalDrawing()) {
                                    endPortalDrawing(false);
                                }

                                // Switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // When in adding floor fields mode, draw a floor field instead
                            // Otherwise, just enable the controls in the sidebar
                            if (!Main.simulator.isFloorFieldDrawing()) {
                                // Only edit if there is already a elevator on that patch
                                if (Main.simulator.getCurrentAmenity() instanceof ElevatorPortal) {
                                    Main.simulator.setCurrentClass(ElevatorPortal.class);

                                    ElevatorPortal elevatorPortalToEdit
                                            = (ElevatorPortal) Main.simulator.getCurrentAmenity();

                                    // Take note of this amenity as the one that will own the floor fields once
                                    // drawn
                                    Main.simulator.setCurrentFloorFieldTarget(elevatorPortalToEdit);

                                    // Update the direction choice box
                                    MainScreenController.normalFloorFieldController.updateDirectionChoiceBox();
                                    MainScreenController.normalFloorFieldController.updateLocationChoiceBox();

                                    // Also take note of the current floor field state
                                    QueueingFloorField.FloorFieldState floorFieldState
                                            = normalFloorFieldController.getFloorFieldState();

                                    Main.simulator.setCurrentFloorFieldState(floorFieldState);

                                    // Do nothing afterwards, editing is handled by the sidebar button
                                } else {
                                    // If there is no amenity there, just do nothing
                                    if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                        // If clicked on an existing amenity, switch to editing mode, then open that
                                        // amenity's controls
                                        goToAmenityControls(Main.simulator.getCurrentAmenity());

                                        // Then revisit this method as if that amenity was clicked
                                        buildOrEdit(currentPatch);
                                    }
                                }
                            } else {
                                // If there is an empty patch here, draw the floor field value
                                if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                    // Define the target and the floor field state
                                    ElevatorPortal target =
                                            (ElevatorPortal) Main.simulator.getCurrentFloorFieldTarget();

                                    // If a floor field value is supposed to be drawn, then go ahead and draw
                                    if (MainScreenController.normalFloorFieldController.getFloorFieldMode()
                                            == NormalFloorFieldController.FloorFieldMode.DRAWING) {
                                        if (!QueueingFloorField.addFloorFieldValue(
                                                currentPatch,
                                                target,
                                                MainScreenController.normalFloorFieldController.getFloorFieldState(),
                                                MainScreenController.normalFloorFieldController.getIntensity()
                                        )) {
                                            // Let the user know if the addition of the floor field value has
                                            // failed
                                            AlertController.showSimpleAlert(
                                                    "Floor field value addition failed",
                                                    "Failed to add a floor field value here",
                                                    "This floor field already has an apex.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // If a floor field value is supposed to be deleted, then go ahead and
                                        // delete it
                                        QueueingFloorField.deleteFloorFieldValue(
                                                currentPatch,
                                                target,
                                                MainScreenController.normalFloorFieldController.getFloorFieldState()
                                        );
                                    }
                                } else {
                                    // If it is a different amenity, turn off floor fields mode
                                    endFloorFieldDrawing(true);

                                    // Switch to editing mode, then open that amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case TICKET_BOOTH:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch and the extra patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(TicketBooth.class);

                                TicketBooth.ticketBoothEditor.draw(
                                        currentPatch,
                                        ticketBoothEnableCheckBox.isSelected(),
                                        ticketBoothIntervalSpinner.getValue(),
                                        ticketBoothModeChoiceBox.getValue()
                                );
                            } else {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // When in adding floor fields mode, draw a floor field instead
                            // Otherwise, just enable the controls in the sidebar
                            if (!Main.simulator.isFloorFieldDrawing()) {
                                // Only edit if there is already a ticket booth or its transaction area on that
                                // patch
                                if (Main.simulator.getCurrentAmenity() instanceof TicketBooth) {
                                    Main.simulator.setCurrentClass(TicketBooth.class);

                                    TicketBooth ticketBoothToEdit
                                            = (TicketBooth) Main.simulator.getCurrentAmenity();

                                    // Take note of this amenity as the one that will own the floor fields once
                                    // drawn
                                    Main.simulator.setCurrentFloorFieldTarget(ticketBoothToEdit);

                                    // Update the direction choice box
                                    MainScreenController.normalFloorFieldController.updateDirectionChoiceBox();
                                    MainScreenController.normalFloorFieldController.updateLocationChoiceBox();

                                    // Also take note of the current floor field state
                                    QueueingFloorField.FloorFieldState floorFieldState
                                            = normalFloorFieldController.getFloorFieldState();

                                    Main.simulator.setCurrentFloorFieldState(floorFieldState);

                                    ticketBoothEnableCheckBox.setSelected(
                                            ticketBoothToEdit.isEnabled()
                                    );

                                    ticketBoothModeChoiceBox.setValue(
                                            ticketBoothToEdit.getTicketBoothType()
                                    );

                                    ticketBoothIntervalSpinner.getValueFactory().setValue(
                                            ticketBoothToEdit.getWaitingTime()
                                    );
                                } else {
                                    // If there is no amenity there, just do nothing
                                    if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                        // If clicked on an existing amenity, switch to editing mode, then open that
                                        // amenity's controls
                                        goToAmenityControls(Main.simulator.getCurrentAmenity());

                                        // Then revisit this method as if that amenity was clicked
                                        buildOrEdit(currentPatch);
                                    }
                                }
                            } else {
                                // If there is an empty patch here, draw the floor field value
                                if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                    // Define the target and the floor field state
                                    TicketBooth target
                                            = (TicketBooth) Main.simulator
                                            .getCurrentFloorFieldTarget();

                                    // If a floor field value is supposed to be drawn, then go ahead and draw
                                    if (MainScreenController.normalFloorFieldController.getFloorFieldMode()
                                            == NormalFloorFieldController.FloorFieldMode.DRAWING) {
                                        if (!QueueingFloorField.addFloorFieldValue(
                                                currentPatch,
                                                target,
                                                MainScreenController.normalFloorFieldController.getFloorFieldState(),
                                                MainScreenController.normalFloorFieldController.getIntensity()
                                        )) {
                                            // Let the user know if the addition of the floor field value has
                                            // failed
                                            AlertController.showSimpleAlert(
                                                    "Floor field value addition failed",
                                                    "Failed to add a floor field value here",
                                                    "This floor field already has an apex.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // If a floor field value is supposed to be deleted, then go ahead and
                                        // delete it
                                        QueueingFloorField.deleteFloorFieldValue(
                                                currentPatch,
                                                target,
                                                normalFloorFieldController.getFloorFieldState()
                                        );
                                    }
                                } else {
                                    // If it is a different amenity, turn off floor fields mode
                                    endFloorFieldDrawing(true);

                                    // Switch to editing mode, then open that amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked

                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case TURNSTILE:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(Turnstile.class);

                                if (!turnstileDirectionListView.getSelectionModel().isEmpty()) {
                                    Turnstile.turnstileEditor.draw(
                                            currentPatch,
                                            turnstileEnableCheckBox.isSelected(),
                                            turnstileIntervalSpinner.getValue(),
                                            turnstileBlockPassengerCheckBox.isSelected(),
                                            turnstileModeChoiceBox.getValue(),
                                            turnstileDirectionListView.getSelectionModel().getSelectedItems()

                                    );
                                } else {
                                    AlertController.showSimpleAlert(
                                            "Turnstile addition failed",
                                            "No travel directions selected",
                                            "Please select the travel directions supported by this turnstile.",
                                            Alert.AlertType.ERROR
                                    );
                                }
                            } else {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // When in adding floor fields mode, draw a floor field instead
                            // Otherwise, just enable the controls in the sidebar
                            if (!Main.simulator.isFloorFieldDrawing()) {
                                // Only edit if there is already a turnstile on that patch
                                if (Main.simulator.getCurrentAmenity() instanceof Turnstile) {
                                    Main.simulator.setCurrentClass(Turnstile.class);

                                    Turnstile turnstileToEdit
                                            = (Turnstile) Main.simulator.getCurrentAmenity();

                                    // Take note of this amenity as the one that will own the floor fields once
                                    // drawn
                                    Main.simulator.setCurrentFloorFieldTarget(turnstileToEdit);

                                    // Update the direction choice box
                                    MainScreenController.normalFloorFieldController.updateDirectionChoiceBox();
                                    MainScreenController.normalFloorFieldController.updateLocationChoiceBox();

                                    // Also take note of the current floor field state
                                    TurnstileFloorField.FloorFieldState turnstileFloorFieldState
                                            = normalFloorFieldController.getFloorFieldState();

                                    Main.simulator.setCurrentFloorFieldState(turnstileFloorFieldState);

                                    turnstileEnableCheckBox.setSelected(
                                            turnstileToEdit.isEnabled()
                                    );

                                    turnstileBlockPassengerCheckBox.setSelected(
                                            turnstileToEdit.blockEntry()
                                    );

                                    turnstileModeChoiceBox.setValue(
                                            turnstileToEdit.getTurnstileMode()
                                    );

                                    turnstileDirectionListView.getSelectionModel().clearSelection();

                                    for (
                                            PassengerMovement.TravelDirection travelDirection
                                            : turnstileToEdit.getTurnstileTravelDirections()
                                    ) {
                                        turnstileDirectionListView.getSelectionModel().select(travelDirection);
                                    }

                                    turnstileIntervalSpinner.getValueFactory().setValue(
                                            turnstileToEdit.getWaitingTime()
                                    );
                                } else {
                                    // If there is no amenity there, just do nothing
                                    if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                        // If clicked on an existing amenity, switch to editing mode, then open that
                                        // amenity's controls
                                        goToAmenityControls(Main.simulator.getCurrentAmenity());

                                        // Then revisit this method as if that amenity was clicked
                                        buildOrEdit(currentPatch);
                                    }
                                }
                            } else {
                                // If there is an empty patch here, draw the floor field value
                                if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                    // Define the target and the floor field state
                                    Turnstile target = (Turnstile) Main.simulator.getCurrentFloorFieldTarget();

                                    // If a floor field value is supposed to be drawn, then go ahead and draw
                                    if (MainScreenController.normalFloorFieldController.getFloorFieldMode()
                                            == NormalFloorFieldController.FloorFieldMode.DRAWING) {
                                        if (!TurnstileFloorField.addFloorFieldValue(
                                                currentPatch,
                                                target,
                                                normalFloorFieldController.getFloorFieldState(),
                                                MainScreenController.normalFloorFieldController.getIntensity()
                                        )) {
                                            // Let the user know if the addition of the floor field value has
                                            // failed
                                            AlertController.showSimpleAlert(
                                                    "Floor field value addition failed",
                                                    "Failed to add a floor field value here",
                                                    "This floor field already has an apex.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // If a floor field value is supposed to be deleted, then go ahead and
                                        // delete it
                                        TurnstileFloorField.deleteFloorFieldValue(
                                                currentPatch,
                                                target,
                                                normalFloorFieldController.getFloorFieldState()
                                        );
                                    }
                                } else {
                                    // If it is a different amenity, turn off floor fields mode
                                    endFloorFieldDrawing(true);

                                    // Switch to editing mode, then open that amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case TRAIN_BOARDING_AREA:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(TrainDoor.class);

                                if (!trainDoorCarriageListView.getSelectionModel().isEmpty()) {
                                    TrainDoor.trainDoorEditor.draw(
                                            currentPatch,
                                            trainDoorEnableCheckBox.isSelected(),
                                            trainDoorDirectionChoiceBox.getSelectionModel().getSelectedItem(),
                                            trainDoorCarriageListView.getSelectionModel().getSelectedItems(),
                                            trainDoorOrientationChoiceBox.getValue(),
                                            trainDoorFemalesOnlyCheckBox.isSelected()
                                    );
                                } else {
                                    AlertController.showSimpleAlert(
                                            "Train boarding area addition failed",
                                            "No carriages selected",
                                            "Please select the train carriage(s) supported by" +
                                                    " the train boarding area to be added",
                                            Alert.AlertType.ERROR
                                    );
                                }
                            } else {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // When in adding floor fields mode, draw a floor field instead
                            // Otherwise, just enable the controls in the sidebar
                            if (!Main.simulator.isFloorFieldDrawing()) {
                                // Only edit if there is already a train door on that patch
                                if (Main.simulator.getCurrentAmenity() instanceof TrainDoor) {
                                    Main.simulator.setCurrentClass(TrainDoor.class);

                                    TrainDoor trainDoorToEdit
                                            = (TrainDoor) Main.simulator.getCurrentAmenity();

                                    // Take note of this amenity as the one that will own the floor fields once
                                    // drawn
                                    Main.simulator.setCurrentFloorFieldTarget(trainDoorToEdit);

                                    // Update the direction choice box
                                    MainScreenController.normalFloorFieldController.updateDirectionChoiceBox();
                                    MainScreenController.normalFloorFieldController.updateLocationChoiceBox();

                                    // Also take note of the current floor field state
                                    PlatformFloorField.FloorFieldState platformFloorFieldState
                                            = normalFloorFieldController.getFloorFieldState();

                                    Main.simulator.setCurrentFloorFieldState(platformFloorFieldState);

                                    trainDoorEnableCheckBox.setSelected(
                                            trainDoorToEdit.isEnabled()
                                    );

                                    trainDoorDirectionChoiceBox.setValue(
                                            trainDoorToEdit.getPlatformDirection()
                                    );

                                    trainDoorOrientationChoiceBox.setValue(
                                            trainDoorToEdit.getTrainDoorOrientation()
                                    );

                                    trainDoorFemalesOnlyCheckBox.setSelected(trainDoorToEdit.isFemaleOnly());

                                    trainDoorCarriageListView.getSelectionModel().clearSelection();

                                    for (TrainDoor.TrainDoorCarriage trainDoorCarriage
                                            : trainDoorToEdit.getTrainDoorCarriagesSupported()) {
                                        trainDoorCarriageListView.getSelectionModel().select(trainDoorCarriage);
                                    }
                                } else {
                                    // If there is no amenity there, just do nothing
                                    if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                        // If clicked on an existing amenity, switch to editing mode, then open that
                                        // amenity's controls
                                        goToAmenityControls(Main.simulator.getCurrentAmenity());

                                        // Then revisit this method as if that amenity was clicked
                                        buildOrEdit(currentPatch);
                                    }
                                }
                            } else {
                                // If there is an empty patch here, draw the floor field value
                                if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                    // Define the target and the floor field state
                                    TrainDoor target = (TrainDoor) Main.simulator.getCurrentFloorFieldTarget();

                                    // If a floor field value is supposed to be drawn, then go ahead and draw
                                    if (MainScreenController.normalFloorFieldController.getFloorFieldMode()
                                            == NormalFloorFieldController.FloorFieldMode.DRAWING) {
                                        if (!PlatformFloorField.addFloorFieldValue(
                                                currentPatch,
                                                target,
                                                normalFloorFieldController.getFloorFieldState(),
                                                MainScreenController.normalFloorFieldController.getIntensity()
                                        )) {
                                            // Let the user know if the addition of the floor field value has
                                            // failed
                                            AlertController.showSimpleAlert(
                                                    "Floor field value addition failed",
                                                    "Failed to add a floor field value here",
                                                    "This floor field already has an apex.",
                                                    Alert.AlertType.ERROR
                                            );
                                        }
                                    } else {
                                        // If a floor field value is supposed to be deleted, then go ahead and
                                        // delete it
                                        PlatformFloorField.deleteFloorFieldValue(
                                                currentPatch,
                                                target,
                                                normalFloorFieldController.getFloorFieldState()
                                        );
                                    }
                                } else {
                                    // If it is a different amenity, turn off floor fields mode
                                    endFloorFieldDrawing(true);

                                    // Switch to editing mode, then open that amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case TRAIN_TRACK:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(Track.class);

                                Track.trackEditor.draw(
                                        currentPatch,
                                        trackDirectionChoiceBox.getValue()
                                );
                            } else {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // Only edit if there is already a station gate on that patch
                            if (Main.simulator.getCurrentAmenity() instanceof Track) {
                                // Only edit if there is already a track on that patch
                                Main.simulator.setCurrentClass(Track.class);

                                Track trackToEdit
                                        = (Track) Main.simulator.getCurrentAmenity();

                                trackDirectionChoiceBox.setValue(
                                        trackToEdit.getTrackDirection()
                                );
                            } else {
                                // If there is no amenity there, just do nothing
                                if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                    // If clicked on an existing amenity, switch to editing mode, then open that
                                    // amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case OBSTACLE:
                    switch (buildState) {
                        case DRAWING:
                            // Only add if the current patch doesn't already have an amenity
                            if (Main.simulator.currentAmenityProperty().isNull().get()) {
                                Main.simulator.setCurrentClass(Wall.class);

                                Wall.wallEditor.draw(
                                        currentPatch,
                                        wallTypeChoiceBox.getValue()
                                );
                            } else {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                        case EDITING_ONE:
                            // Only edit if there is already a wall on that patch
                            if (Main.simulator.getCurrentAmenity() instanceof Wall) {
                                Main.simulator.setCurrentClass(Wall.class);

                                Wall wallToEdit
                                        = (Wall) Main.simulator.getCurrentAmenity();

                                wallTypeChoiceBox.setValue(
                                        wallToEdit.getWallType()
                                );
                            } else {
                                // If there is no amenity there, just do nothing
                                if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                    // If clicked on an existing amenity, switch to editing mode, then open that
                                    // amenity's controls
                                    goToAmenityControls(Main.simulator.getCurrentAmenity());

                                    // Then revisit this method as if that amenity was clicked
                                    buildOrEdit(currentPatch);
                                }
                            }

                            break;
                        case EDITING_ALL:
                            // No specific values need to be set here because all amenities will be edited
                            // once save is clicked
                            // If there is no amenity there, just do nothing
                            if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                                // If clicked on an existing amenity, switch to editing mode, then open that
                                // amenity's controls
                                goToAmenityControls(Main.simulator.getCurrentAmenity());

                                // Then revisit this method as if that amenity was clicked
                                buildOrEdit(currentPatch);
                            }

                            break;
                    }

                    break;
                case NONE:
                    if (Main.simulator.currentAmenityProperty().isNotNull().get()) {
                        // If clicked on an existing amenity, switch to editing mode, then open that
                        // amenity's controls
                        goToAmenityControls(Main.simulator.getCurrentAmenity());

                        // Then revisit this method as if that amenity was clicked
                        buildOrEdit(currentPatch);
                    }

                    break;
            }
        } else {
            // If the operation mode is testing, the user wants to edit (one or all)
            System.out.println("test");
        }
    }

    // Given an amenity, open the subcategory (category) and titled pane (subcategory) that contains that amenity
    // in the interface
    public void goToAmenityControls(Amenity amenity) {
        Simulator.BuildCategory buildCategory = null;
        Simulator.BuildSubcategory buildSubcategory = null;

        // Set the choice box to the editing modes
        buildModeChoiceBox.getSelectionModel().select(Simulator.BuildState.EDITING_ONE);

        // Get category and subcategory
        if (
                amenity instanceof StationGate
                        || amenity instanceof Security
        ) {
            buildCategory = Simulator.BuildCategory.ENTRANCES_AND_EXITS;

            if (amenity instanceof StationGate) {
                buildSubcategory = Simulator.BuildSubcategory.STATION_ENTRANCE_EXIT;
            } else {
                buildSubcategory = Simulator.BuildSubcategory.SECURITY;
            }
        } else if (
                amenity instanceof StairPortal
                        || amenity instanceof EscalatorPortal
                        || amenity instanceof ElevatorPortal) {
            buildCategory = Simulator.BuildCategory.STAIRS_AND_ELEVATORS;

            if (amenity instanceof StairPortal) {
                buildSubcategory = Simulator.BuildSubcategory.STAIRS;
            } else if (amenity instanceof EscalatorPortal) {
                buildSubcategory = Simulator.BuildSubcategory.ESCALATOR;
            } else {
                buildSubcategory = Simulator.BuildSubcategory.ELEVATOR;
            }
        } else if (
                amenity instanceof TicketBooth
                        || amenity instanceof Turnstile
        ) {
            buildCategory = Simulator.BuildCategory.CONCOURSE_AMENITIES;

            if (amenity instanceof TicketBooth) {
                buildSubcategory = Simulator.BuildSubcategory.TICKET_BOOTH;
            } else {
                buildSubcategory = Simulator.BuildSubcategory.TURNSTILE;
            }
        } else if (
                amenity instanceof TrainDoor
        ) {
            buildCategory = Simulator.BuildCategory.PLATFORM_AMENITIES;
            buildSubcategory = Simulator.BuildSubcategory.TRAIN_BOARDING_AREA;
        } else if (
                amenity instanceof Track
        ) {
            buildCategory = Simulator.BuildCategory.PLATFORM_AMENITIES;
            buildSubcategory = Simulator.BuildSubcategory.TRAIN_TRACK;
        } else if (
                amenity instanceof Wall
        ) {
            buildCategory = Simulator.BuildCategory.MISCELLANEOUS;
            buildSubcategory = Simulator.BuildSubcategory.OBSTACLE;
        }

        if (buildCategory == null) {
            return;
        }

        Simulator.BuildCategory finalBuildCategory = buildCategory;
        Simulator.BuildSubcategory finalBuildSubcategory = buildSubcategory;

        Accordion accordion;
        ObservableList<TitledPane> titledPanes;

        // Then open the respective tab and titled pane
        switch (finalBuildCategory) {
            case ENTRANCES_AND_EXITS:
                buildTabPane.getSelectionModel().select(0);

                accordion = (Accordion) buildTabPane.getTabs().get(0).getContent();
                titledPanes = accordion.getPanes();

                switch (finalBuildSubcategory) {
                    case STATION_ENTRANCE_EXIT:
                        titledPanes.get(0).setExpanded(true);

                        break;
                    case SECURITY:
                        titledPanes.get(1).setExpanded(true);

                        break;
                }

                break;
            case STAIRS_AND_ELEVATORS:
                buildTabPane.getSelectionModel().select(1);

                accordion = (Accordion) buildTabPane.getTabs().get(1).getContent();
                titledPanes = accordion.getPanes();

                switch (finalBuildSubcategory) {
                    case STAIRS:
                        titledPanes.get(0).setExpanded(true);

                        break;
                    case ESCALATOR:
                        titledPanes.get(1).setExpanded(true);

                        break;
                    case ELEVATOR:
                        titledPanes.get(2).setExpanded(true);

                        break;
                }

                break;
            case CONCOURSE_AMENITIES:
                buildTabPane.getSelectionModel().select(2);

                accordion = (Accordion) buildTabPane.getTabs().get(2).getContent();
                titledPanes = accordion.getPanes();

                switch (finalBuildSubcategory) {
                    case TICKET_BOOTH:
                        titledPanes.get(0).setExpanded(true);

                        break;
                    case TURNSTILE:
                        titledPanes.get(1).setExpanded(true);

                        break;
                }

                break;
            case PLATFORM_AMENITIES:
                buildTabPane.getSelectionModel().select(3);

                accordion = (Accordion) buildTabPane.getTabs().get(3).getContent();
                titledPanes = accordion.getPanes();

                switch (finalBuildSubcategory) {
                    case TRAIN_BOARDING_AREA:
                        titledPanes.get(0).setExpanded(true);

                        break;
                    case TRAIN_TRACK:
                        titledPanes.get(1).setExpanded(true);

                        break;
                }

                break;
            case MISCELLANEOUS:
                buildTabPane.getSelectionModel().select(4);

                accordion = (Accordion) buildTabPane.getTabs().get(4).getContent();
                titledPanes = accordion.getPanes();

                titledPanes.get(0).setExpanded(true);

                break;
        }

        // Switch the class to the class of the current amenity
        Main.simulator.setCurrentClass(amenity.getClass());
    }

    // Draw the interface
    private void drawInterface(boolean drawListeners) {
        // Initially draw the station environment, showing the current floor
        drawStationViewFloorBackground(Main.simulator.getCurrentFloor());

        // Then draw the passengers in the station
        drawStationViewFloorForeground(Main.simulator.getCurrentFloor(), false);

        // Then draw the mouse listeners over the station view
        if (drawListeners) {
            drawListeners();
        }
    }

    // Draw the station view background given a current floor
    public void drawStationViewFloorBackground(Floor currentFloor) {
        GraphicsController.requestDrawStationView(
                interfaceStackPane,
                currentFloor,
                GraphicsController.tileSize,
                true,
                false,
                true,
                Main.simulator.isStationRunOnly()
        );
    }

    // Draw the station view foreground given a current floor
    public void drawStationViewFloorForeground(Floor currentFloor, boolean speedAware) {
        GraphicsController.requestDrawStationView(
                interfaceStackPane,
                currentFloor,
                GraphicsController.tileSize,
                false,
                speedAware,
                true,
                Main.simulator.isStationRunOnly()
        );

        requestUpdateInterfaceSimulationElements();
    }

    // Update the interface elements pertinent to the simulation
    private void requestUpdateInterfaceSimulationElements() {
        Platform.runLater(() -> {
            // Update the simulation time
            updateSimulationTime();

            // Update the passenger counts
            updatePassengerCounts();
        });
    }

    // Draw the mouse listeners
    private void drawListeners() {
        // Draw each mouse listener along with their corresponding actions
        GraphicsController.requestDrawListeners(
                interfaceStackPane
        );
    }
}

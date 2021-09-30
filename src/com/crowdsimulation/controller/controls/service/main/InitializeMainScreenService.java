package com.crowdsimulation.controller.controls.service.main;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.controls.feature.main.MainScreenController;
import com.crowdsimulation.controller.controls.service.InitializeScreenService;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Wall;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;
import com.crowdsimulation.model.simulator.SimulationTime;
import com.crowdsimulation.model.simulator.Simulator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.List;

public class InitializeMainScreenService extends InitializeScreenService {
    private static final ObservableList<Simulator.BuildState> BUILD_MODE_CHOICEBOX_ITEMS;

    // Binding variables
    public static BooleanBinding SAVE_DELETE_BINDING;
    public static BooleanBinding SPECIFIC_CONTROLS_BINDING;
    public static BooleanBinding DRAW_ONLY_BINDING;
    public static BooleanBinding ADD_FLOOR_FIELD_BINDING;
    public static BooleanBinding PORTAL_DRAW_IN_PROGRESS_BINDING;
    public static BooleanBinding FLOOR_FIELD_DRAW_IN_PROGRESS_BINDING;
    public static BooleanBinding AMENITY_NOT_EQUALS_SUBCATEGORY_BINDING;
    public static BooleanBinding EDIT_FLOOR_BINDING;

    static {
        BUILD_MODE_CHOICEBOX_ITEMS = FXCollections.observableArrayList(
                Simulator.BuildState.DRAWING,
                Simulator.BuildState.EDITING_ONE,
                Simulator.BuildState.EDITING_ALL
        );

        AMENITY_NOT_EQUALS_SUBCATEGORY_BINDING = Bindings.createBooleanBinding(() -> {
                    if (Bindings.isNotNull(Main.simulator.currentAmenityProperty()).get()
                            && Bindings.isNotNull(Main.simulator.buildSubcategoryClassProperty()).get()) {
                        return !Main.simulator.buildSubcategoryClassProperty().get().equals(
                                Main.simulator.getCurrentAmenity().getClass()
                        );
                    } else {
                        return false;
                    }
                }, Main.simulator.currentAmenityProperty(), Main.simulator.buildSubcategoryClassProperty()
        );

        InitializeMainScreenService.SAVE_DELETE_BINDING =
                Bindings.or(
                        Bindings.equal(
                                Main.simulator.buildStateProperty(),
                                Simulator.BuildState.DRAWING
                        ),
                        Bindings.and(
                                Bindings.or(
                                        Bindings.isNull(
                                                Main.simulator.currentAmenityProperty()
                                        ),
                                        AMENITY_NOT_EQUALS_SUBCATEGORY_BINDING),
                                Bindings.notEqual(
                                        Main.simulator.buildStateProperty(),
                                        Simulator.BuildState.EDITING_ALL
                                )
                        )
                );

        InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING =
                Bindings.and(
                        Bindings.equal(
                                Main.simulator.buildStateProperty(),
                                Simulator.BuildState.EDITING_ONE
                        ),
                        Bindings.or(
                                Bindings.isNull(
                                        Main.simulator.currentAmenityProperty()
                                ),
                                AMENITY_NOT_EQUALS_SUBCATEGORY_BINDING)
                );

        InitializeMainScreenService.DRAW_ONLY_BINDING =
                Bindings.or(
                        Bindings.notEqual(
                                Main.simulator.buildStateProperty(),
                                Simulator.BuildState.DRAWING
                        ),
                        Main.simulator.portalDrawingProperty()
                );

        InitializeMainScreenService.PORTAL_DRAW_IN_PROGRESS_BINDING =
                Bindings.equal(
                        Main.simulator.portalDrawingProperty(),
                        Bindings.createBooleanBinding(() -> true)
                );

        InitializeMainScreenService.FLOOR_FIELD_DRAW_IN_PROGRESS_BINDING =
                Bindings.equal(
                        Main.simulator.floorFieldDrawingProperty(),
                        Bindings.createBooleanBinding(() -> true)
                );

        InitializeMainScreenService.ADD_FLOOR_FIELD_BINDING =
                Bindings.or(
                        Bindings.or(
                                Bindings.isNull(
                                        Main.simulator.currentAmenityProperty()
                                ),
                                AMENITY_NOT_EQUALS_SUBCATEGORY_BINDING),
                        Bindings.notEqual(
                                Main.simulator.buildStateProperty(),
                                Simulator.BuildState.EDITING_ONE
                        )
                );

        InitializeMainScreenService.EDIT_FLOOR_BINDING =
                Bindings.or(
                        Bindings.or(
                                PORTAL_DRAW_IN_PROGRESS_BINDING,
                                FLOOR_FIELD_DRAW_IN_PROGRESS_BINDING
                        ),
                        Main.simulator.isStationRunOnlyProperty()
                );
    }

    // Initialize the build tab UI controls
    public static void initializeSidebar(TabPane sideBar) {
        sideBar.getTabs().get(0).disableProperty().bind(Main.simulator.isStationRunOnlyProperty());

        sideBar.disableProperty().bind(
                Bindings.or(
                        InitializeMainScreenService.PORTAL_DRAW_IN_PROGRESS_BINDING,
                        InitializeMainScreenService.FLOOR_FIELD_DRAW_IN_PROGRESS_BINDING
                )
        );
    }

    public static void initializeBuildTab(
            Button validateButton,
            Label buildModeLabel,
            ChoiceBox<Simulator.BuildState> buildModeChoiceBox,
            // Entrances/exits
            // Station gate
            CheckBox stationGateEnableCheckBox,
            Label stationGateModeLabel,
            ChoiceBox<StationGate.StationGateMode> stationGateModeChoiceBox,
            Label stationGateDirectionLabel,
            ListView<PassengerMovement.TravelDirection> stationGateDirectionListView,
            Label stationGateSpawnLabel,
            Spinner<Integer> stationGateSpinner,
            Button saveStationGateButton,
            Button deleteStationGateButton,
            // Security
            CheckBox securityEnableCheckBox,
            CheckBox securityBlockPassengerCheckBox,
            Label securityIntervalLabel,
            Spinner<Integer> securityIntervalSpinner,
            Button saveSecurityButton,
            Button deleteSecurityButton,
            Button addFloorFieldsSecurityButton,
            Button flipSecurityButton,
            // Stairs and elevators
            // Stairs
            Button addStairButton,
            Button editStairButton,
            Button deleteStairButton,
            // Escalator
            Button addEscalatorButton,
            Button editEscalatorButton,
            Button deleteEscalatorButton,
            // Elevator
            Button addElevatorButton,
            Button editElevatorButton,
            Button deleteElevatorButton,
            Button addFloorFieldsElevatorButton,
            // Concourse amenities
            // Ticket booth
            CheckBox ticketBoothEnableCheckBox,
            Label ticketBoothModeLabel,
            ChoiceBox<TicketBooth.TicketType> ticketBoothModeChoiceBox,
            Label ticketBoothIntervalLabel,
            Spinner<Integer> ticketBoothIntervalSpinner,
            Button saveTicketBoothButton,
            Button deleteTicketBoothButton,
            Button addFloorFieldsTicketBoothButton,
            Button flipTicketBoothButton,
            // Turnstile
            CheckBox turnstileEnableCheckBox,
            CheckBox turnstileBlockPassengerCheckBox,
            Label turnstileModeLabel,
            ChoiceBox<Turnstile.TurnstileMode> turnstileModeChoiceBox,
            Label turnstileDirectionLabel,
            ListView<PassengerMovement.TravelDirection> turnstileDirectionListView,
            Label turnstileIntervalLabel,
            Spinner<Integer> turnstileIntervalSpinner,
            Button saveTurnstileButton,
            Button deleteTurnstileButton,
            Button addFloorFieldsTurnstileButton,
            // Platform amenities
            // Train boarding area
            CheckBox trainDoorEnableCheckBox,
            Label trainDoorDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> trainDoorDirectionChoiceBox,
            Label trainDoorCarriageLabel,
            ListView<TrainDoor.TrainDoorCarriage> trainDoorCarriageListView,
            Label trainDoorOrientationLabel,
            ChoiceBox<Station.StationOrientation> trainDoorOrientationChoiceBox,
            CheckBox trainDoorFemalesOnlyCheckBox,
            Button saveTrainDoorButton,
            Button deleteTrainDoorButton,
            Button addFloorFieldsTrainDoorButton,
            // Train tracks
            Label trackDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> trackDirectionChoiceBox,
            Button saveTrackButton,
            Button deleteTrackButton,
            // Miscellaneous
            // Obstacle
            Label wallTypeLabel,
            ChoiceBox<Wall.WallType> wallTypeChoiceBox,
            Button saveWallButton,
            Button deleteWallButton,
            Button flipWallButton,
            // Tab pane
            TabPane buildTabPane
    ) {
        // Initialize the build mode choice box
        initializeBuildModeHeader(
                validateButton,
                buildModeChoiceBox,
                buildModeLabel
        );

        // Initialize categories
        initializeEntrancesAndExits(
                stationGateEnableCheckBox,
                stationGateModeLabel,
                stationGateModeChoiceBox,
                stationGateDirectionLabel,
                stationGateDirectionListView,
                stationGateSpawnLabel,
                stationGateSpinner,
                saveStationGateButton,
                deleteStationGateButton,
                securityEnableCheckBox,
                securityBlockPassengerCheckBox,
                securityIntervalLabel,
                securityIntervalSpinner,
                saveSecurityButton,
                deleteSecurityButton,
                addFloorFieldsSecurityButton,
                flipSecurityButton
        );

        initializeStairsElevators(
                addStairButton,
                editStairButton,
                deleteStairButton,
                addEscalatorButton,
                editEscalatorButton,
                deleteEscalatorButton,
                addElevatorButton,
                editElevatorButton,
                deleteElevatorButton,
                addFloorFieldsElevatorButton
        );

        initializeConcourseAmenities(
                ticketBoothEnableCheckBox,
                ticketBoothModeLabel,
                ticketBoothModeChoiceBox,
                ticketBoothIntervalLabel,
                ticketBoothIntervalSpinner,
                saveTicketBoothButton,
                deleteTicketBoothButton,
                addFloorFieldsTicketBoothButton,
                flipTicketBoothButton,
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
                addFloorFieldsTurnstileButton
        );

        initializePlatformAmenities(
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
                trackDirectionLabel,
                trackDirectionChoiceBox,
                saveTrackButton,
                deleteTrackButton
        );

        initializeWalls(
                wallTypeLabel,
                wallTypeChoiceBox,
                saveWallButton,
                deleteWallButton,
                flipWallButton
        );

        // Initialize listeners
        initializeCategoryListeners(buildTabPane);
    }

    // Initialize the build mode choice box
    private static void initializeBuildModeHeader(
            Button validateButton,
            ChoiceBox<Simulator.BuildState> buildModeChoiceBox,
            Label buildModeLabel
    ) {
        validateButton.disableProperty().bind(FLOOR_FIELD_DRAW_IN_PROGRESS_BINDING);

        buildModeLabel.setLabelFor(buildModeChoiceBox);

        buildModeChoiceBox.setItems(BUILD_MODE_CHOICEBOX_ITEMS);
        buildModeChoiceBox.getSelectionModel().select(0);

        buildModeChoiceBox.setOnAction(event -> {
        });

        buildModeChoiceBox.valueProperty().bindBidirectional(Main.simulator.buildStateProperty());
    }

    // Initialize the build tab UI controls
    public static void initializeTopBar(
            Button addFloorBelowButton,
            Button floorBelowButton,
            Button deleteFloorButton,
            Button floorAboveButton,
            Button addFloorAboveButton,
            ToggleButton peekFloorsButton
    ) {
        floorBelowButton.setDisable(true);
        floorAboveButton.setDisable(true);

        addFloorBelowButton.disableProperty().bind(EDIT_FLOOR_BINDING);
        addFloorAboveButton.disableProperty().bind(EDIT_FLOOR_BINDING);

        deleteFloorButton.disableProperty().bind(EDIT_FLOOR_BINDING);

        peekFloorsButton.disableProperty().bind(EDIT_FLOOR_BINDING);
    }

    public static void initializeTestTab(
            // Simulation controls
            Text elapsedTimeText,
            ToggleButton playButton,
            Button resetButton,
            Label simulationSpeedLabel,
            Slider simulationSpeedSlider,
            // Passenger controls
            Text passengerCountStationText,
            Text passengerCountFloorText,
            // Platform controls
            Label platformDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> platformDirectionChoiceBox,
            Label platformCarriagesLabel,
            ChoiceBox<TrainDoor.TrainDoorCarriage> platformCarriagesChoiceBox,
            ToggleButton openTrainDoorsButton
    ) {
        initializeSimulationControls(
                elapsedTimeText,
                playButton,
                resetButton,
                simulationSpeedLabel,
                simulationSpeedSlider
        );

        initializePassengerControls(
                passengerCountStationText,
                passengerCountFloorText
        );

        initializePlatformControls(
                platformDirectionLabel,
                platformDirectionChoiceBox,
                platformCarriagesLabel,
                platformCarriagesChoiceBox,
                openTrainDoorsButton
        );
    }


    // Initialize the entrances and exits build category UI controls
    private static void initializeEntrancesAndExits(
            CheckBox stationGateEnableCheckBox,
            Label stationGateModeLabel,
            ChoiceBox<StationGate.StationGateMode> stationGateModeChoiceBox,
            Label stationGateDirectionLabel,
            ListView<PassengerMovement.TravelDirection> stationGateDirectionListView,
            Label stationGateSpawnLabel,
            Spinner<Integer> stationGateSpinner,
            Button saveStationGateButton,
            Button deleteStationGateButton,
            CheckBox securityEnableCheckBox,
            CheckBox securityBlockPassengerCheckBox,
            Label securityIntervalLabel,
            Spinner<Integer> securityIntervalSpinner,
            Button saveSecurityButton,
            Button deleteSecurityButton,
            Button addFloorFieldsSecurityButton,
            Button flipSecurityButton
    ) {
        initializeStationEntranceExit(
                stationGateEnableCheckBox,
                stationGateModeLabel,
                stationGateModeChoiceBox,
                stationGateDirectionLabel,
                stationGateDirectionListView,
                stationGateSpawnLabel,
                stationGateSpinner,
                saveStationGateButton,
                deleteStationGateButton
        );

        initializeSecurity(
                securityEnableCheckBox,
                securityBlockPassengerCheckBox,
                securityIntervalLabel,
                securityIntervalSpinner,
                saveSecurityButton,
                deleteSecurityButton,
                addFloorFieldsSecurityButton,
                flipSecurityButton
        );
    }

    // Initialize the station entrance/exit UI controls
    private static void initializeStationEntranceExit(
            CheckBox stationGateEnableCheckBox,
            Label stationGateModeLabel,
            ChoiceBox<StationGate.StationGateMode> stationGateModeChoiceBox,
            Label stationGateDirectionLabel,
            ListView<PassengerMovement.TravelDirection> stationGateDirectionListView,
            Label stationGateSpawnLabel,
            Spinner<Integer> stationGateSpinner,
            Button saveStationGateButton,
            Button deleteStationGateButton
    ) {
        stationGateModeLabel.setLabelFor(stationGateModeChoiceBox);

        stationGateModeChoiceBox.setItems(FXCollections.observableArrayList(
                StationGate.StationGateMode.ENTRANCE,
                StationGate.StationGateMode.EXIT,
                StationGate.StationGateMode.ENTRANCE_AND_EXIT
        ));
        stationGateModeChoiceBox.getSelectionModel().select(StationGate.StationGateMode.ENTRANCE_AND_EXIT);

        stationGateDirectionLabel.setLabelFor(stationGateDirectionListView);

        stationGateDirectionListView.setItems(FXCollections.observableArrayList(
                PassengerMovement.TravelDirection.NORTHBOUND,
                PassengerMovement.TravelDirection.SOUTHBOUND,
                PassengerMovement.TravelDirection.EASTBOUND,
                PassengerMovement.TravelDirection.WESTBOUND
        ));
        stationGateDirectionListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        stationGateSpawnLabel.setLabelFor(stationGateSpinner);

        stationGateSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        100,
                        10)
        );

        stationGateEnableCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        stationGateModeChoiceBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        stationGateDirectionListView.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        stationGateSpinner.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);

        saveStationGateButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteStationGateButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
    }

    // Initialize the security UI controls
    private static void initializeSecurity(
            CheckBox securityEnableCheckBox,
            CheckBox securityBlockPassengerCheckBox,
            Label securityIntervalLabel,
            Spinner<Integer> securityIntervalSpinner,
            Button saveSecurityButton,
            Button deleteSecurityButton,
            Button addFloorFieldsSecurityButton,
            Button flipSecurityButton
    ) {
        securityIntervalLabel.setLabelFor(securityIntervalSpinner);

        securityIntervalSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0,
                        60,
                        5)
        );

        securityEnableCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        securityBlockPassengerCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        securityIntervalSpinner.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);

        saveSecurityButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteSecurityButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);

        addFloorFieldsSecurityButton.disableProperty().bind(
                InitializeMainScreenService.ADD_FLOOR_FIELD_BINDING
        );

        flipSecurityButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
    }

    // Initialize the stairs and elevators build category UI controls
    private static void initializeStairsElevators(
            Button addStairButton,
            Button editStairButton,
            Button deleteStairButton,
            Button addEscalatorButton,
            Button editEscalatorButton,
            Button deleteEscalatorButton,
            Button addElevatorButton,
            Button editElevatorButton,
            Button deleteElevatorButton,
            Button addFloorFieldsElevatorButton
    ) {
        initializeStairs(
                addStairButton,
                editStairButton,
                deleteStairButton
        );

        initializeEscalators(
                addEscalatorButton,
                editEscalatorButton,
                deleteEscalatorButton
        );

        initializeElevators(
                addElevatorButton,
                editElevatorButton,
                deleteElevatorButton,
                addFloorFieldsElevatorButton
        );
    }

    // Initialize the stairs controls
    private static void initializeStairs(
            Button addStairButton,
            Button editStairButton,
            Button deleteStairButton
    ) {
        addStairButton.disableProperty().bind(InitializeMainScreenService.DRAW_ONLY_BINDING);

        editStairButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteStairButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
    }

    // Initialize the escalators controls
    private static void initializeEscalators(
            Button addEscalatorButton,
            Button editEscalatorButton,
            Button deleteEscalatorButton
    ) {
        addEscalatorButton.disableProperty().bind(InitializeMainScreenService.DRAW_ONLY_BINDING);

        editEscalatorButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteEscalatorButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
    }

    // Initialize the elevators controls
    private static void initializeElevators(
            Button addElevatorButton,
            Button editElevatorButton,
            Button deleteElevatorButton,
            Button addFloorFieldsElevatorButton
    ) {
        addElevatorButton.disableProperty().bind(InitializeMainScreenService.DRAW_ONLY_BINDING);

        editElevatorButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteElevatorButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);

        addFloorFieldsElevatorButton.disableProperty().bind(InitializeMainScreenService.ADD_FLOOR_FIELD_BINDING);
    }

    // Initialize the concourse amenities build category UI controls
    private static void initializeConcourseAmenities(
            CheckBox ticketBoothEnableCheckBox,
            Label ticketBoothModeLabel,
            ChoiceBox<TicketBooth.TicketType> ticketBoothModeChoiceBox,
            Label ticketBoothIntervalLabel,
            Spinner<Integer> ticketBoothIntervalSpinner,
            Button saveTicketBoothButton,
            Button deleteTicketBoothButton,
            Button addFloorFieldsTicketBoothButton,
            Button flipTicketBoothButton,
            CheckBox turnstileEnableCheckBox,
            CheckBox turnstileBlockPassengerCheckBox,
            Label turnstileModeLabel,
            ChoiceBox<Turnstile.TurnstileMode> turnstileModeChoiceBox,
            Label turnstileDirectionLabel,
            ListView<PassengerMovement.TravelDirection> turnstileDirectionListView,
            Label turnstileIntervalLabel,
            Spinner<Integer> turnstileIntervalSpinner,
            Button saveTurnstileButton,
            Button deleteTurnstileButton,
            Button addFloorFieldsTurnstileButton
    ) {
        initializeTicketBooth(
                ticketBoothEnableCheckBox,
                ticketBoothModeLabel,
                ticketBoothModeChoiceBox,
                ticketBoothIntervalLabel,
                ticketBoothIntervalSpinner,
                saveTicketBoothButton,
                deleteTicketBoothButton,
                addFloorFieldsTicketBoothButton,
                flipTicketBoothButton
        );

        initializeTurnstile(
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
                addFloorFieldsTurnstileButton
        );
    }

    // Initialize the ticket booth controls
    private static void initializeTicketBooth(
            CheckBox ticketBoothEnableCheckBox,
            Label ticketBoothModeLabel,
            ChoiceBox<TicketBooth.TicketType> ticketBoothModeChoiceBox,
            Label ticketBoothIntervalLabel,
            Spinner<Integer> ticketBoothIntervalSpinner,
            Button saveTicketBoothButton,
            Button deleteTicketBoothButton,
            Button addFloorFieldsTicketBoothButton,
            Button flipTicketBoothButton
    ) {
        ticketBoothModeLabel.setLabelFor(ticketBoothModeChoiceBox);

        ticketBoothModeChoiceBox.setItems(FXCollections.observableArrayList(
                TicketBooth.TicketType.SINGLE_JOURNEY,
                TicketBooth.TicketType.STORED_VALUE,
                TicketBooth.TicketType.ALL_TICKET_TYPES
        ));
        ticketBoothModeChoiceBox.getSelectionModel().select(TicketBooth.TicketType.SINGLE_JOURNEY);

        ticketBoothIntervalLabel.setLabelFor(ticketBoothIntervalSpinner);

        ticketBoothIntervalSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        5,
                        60,
                        5)
        );

        ticketBoothEnableCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        ticketBoothModeChoiceBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        ticketBoothIntervalSpinner.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);

        saveTicketBoothButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteTicketBoothButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);

        addFloorFieldsTicketBoothButton.disableProperty().bind(
                InitializeMainScreenService.ADD_FLOOR_FIELD_BINDING
        );

        flipTicketBoothButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
    }

    // Initialize the turnstile controls
    private static void initializeTurnstile(
            CheckBox turnstileEnableCheckBox,
            CheckBox turnstileBlockPassengerCheckBox,
            Label turnstileModeLabel,
            ChoiceBox<Turnstile.TurnstileMode> turnstileModeChoiceBox,
            Label turnstileDirectionLabel,
            ListView<PassengerMovement.TravelDirection> turnstileDirectionListView,
            Label turnstileIntervalLabel,
            Spinner<Integer> turnstileIntervalSpinner,
            Button saveTurnstileButton,
            Button deleteTurnstileButton,
            Button addFloorFieldsTurnstileButton
    ) {
        turnstileModeLabel.setLabelFor(turnstileModeChoiceBox);

        turnstileModeChoiceBox.setItems(FXCollections.observableArrayList(
                Turnstile.TurnstileMode.BOARDING,
                Turnstile.TurnstileMode.ALIGHTING,
                Turnstile.TurnstileMode.BIDIRECTIONAL
        ));
        turnstileModeChoiceBox.getSelectionModel().select(Turnstile.TurnstileMode.BIDIRECTIONAL);

        turnstileModeLabel.setLabelFor(turnstileDirectionListView);

        turnstileDirectionListView.setItems(FXCollections.observableArrayList(
                PassengerMovement.TravelDirection.NORTHBOUND,
                PassengerMovement.TravelDirection.SOUTHBOUND,
                PassengerMovement.TravelDirection.EASTBOUND,
                PassengerMovement.TravelDirection.WESTBOUND
        ));
        turnstileDirectionListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        turnstileIntervalLabel.setLabelFor(turnstileIntervalSpinner);

        turnstileIntervalSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        10,
                        3)
        );

        turnstileEnableCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        turnstileBlockPassengerCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        turnstileModeChoiceBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        turnstileDirectionListView.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        turnstileIntervalSpinner.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);

        saveTurnstileButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteTurnstileButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);

        addFloorFieldsTurnstileButton.disableProperty().bind(InitializeMainScreenService.ADD_FLOOR_FIELD_BINDING);
    }

    // Initialize the platform amenities build category UI controls
    private static void initializePlatformAmenities(
            CheckBox trainDoorEnableCheckBox,
            Label trainDoorDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> trainDoorDirectionChoiceBox,
            Label trainDoorCarriageLabel,
            ListView<TrainDoor.TrainDoorCarriage> trainDoorCarriageListView,
            Label trainDoorOrientationLabel,
            ChoiceBox<Station.StationOrientation> trainDoorOrientationChoiceBox,
            CheckBox trainDoorFemalesOnlyCheckBox,
            Button saveTrainDoorButton,
            Button deleteTrainDoorButton,
            Button addFloorFieldsTrainDoorButton,
            Label trackDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> trackDirectionChoiceBox,
            Button saveTrackButton,
            Button deleteTrackButton
    ) {
        initializeTrainBoardingArea(
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
                addFloorFieldsTrainDoorButton
        );

        initializeTrack(
                trackDirectionLabel,
                trackDirectionChoiceBox,
                saveTrackButton,
                deleteTrackButton
        );
    }

    // Initialize the train boarding area controls
    private static void initializeTrainBoardingArea(
            CheckBox trainDoorEnableCheckBox,
            Label trainDoorDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> trainDoorDirectionChoiceBox,
            Label trainDoorCarriageLabel,
            ListView<TrainDoor.TrainDoorCarriage> trainDoorCarriagesListView,
            Label trainDoorOrientationLabel,
            ChoiceBox<Station.StationOrientation> trainDoorOrientationChoiceBox,
            CheckBox trainDoorFemalesOnlyCheckBox,
            Button saveTrainDoorButton,
            Button deleteTrainDoorButton,
            Button addFloorFieldsTrainDoorButton
    ) {
        trainDoorDirectionLabel.setLabelFor(trainDoorDirectionChoiceBox);

        trainDoorDirectionChoiceBox.setItems(FXCollections.observableArrayList(
                PassengerMovement.TravelDirection.NORTHBOUND,
                PassengerMovement.TravelDirection.SOUTHBOUND,
                PassengerMovement.TravelDirection.WESTBOUND,
                PassengerMovement.TravelDirection.EASTBOUND
        ));
        trainDoorDirectionChoiceBox.getSelectionModel().select(PassengerMovement.TravelDirection.NORTHBOUND);

        trainDoorCarriageLabel.setLabelFor(trainDoorCarriagesListView);

        trainDoorCarriagesListView.setItems(FXCollections.observableArrayList(
                TrainDoor.TrainDoorCarriage.LRT_1_FIRST_GENERATION,
                TrainDoor.TrainDoorCarriage.LRT_1_SECOND_GENERATION,
                TrainDoor.TrainDoorCarriage.LRT_1_THIRD_GENERATION,
                TrainDoor.TrainDoorCarriage.LRT_2_FIRST_GENERATION,
                TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION,
                TrainDoor.TrainDoorCarriage.MRT_3_SECOND_GENERATION
        ));
        trainDoorCarriagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        trainDoorOrientationLabel.setLabelFor(trainDoorOrientationChoiceBox);

        trainDoorOrientationChoiceBox.setItems(FXCollections.observableArrayList(
                Station.StationOrientation.SIDE_PLATFORM,
                Station.StationOrientation.ISLAND_PLATFORM
        ));
        trainDoorOrientationChoiceBox.getSelectionModel().select(Station.StationOrientation.SIDE_PLATFORM);

        trainDoorEnableCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        trainDoorDirectionChoiceBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        trainDoorCarriagesListView.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);
        trainDoorOrientationChoiceBox.disableProperty().bind(SPECIFIC_CONTROLS_BINDING);
        trainDoorFemalesOnlyCheckBox.disableProperty().bind(InitializeMainScreenService.SPECIFIC_CONTROLS_BINDING);

        saveTrainDoorButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteTrainDoorButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);

        addFloorFieldsTrainDoorButton.disableProperty().bind(InitializeMainScreenService.ADD_FLOOR_FIELD_BINDING);
    }

    // Initialize the train track controls
    private static void initializeTrack(
            Label trackDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> trackDirectionChoiceBox,
            Button saveTrackButton,
            Button deleteTrackButton
    ) {
        trackDirectionLabel.setLabelFor(trackDirectionChoiceBox);

        trackDirectionChoiceBox.setItems(FXCollections.observableArrayList(
                PassengerMovement.TravelDirection.NORTHBOUND,
                PassengerMovement.TravelDirection.SOUTHBOUND,
                PassengerMovement.TravelDirection.WESTBOUND,
                PassengerMovement.TravelDirection.EASTBOUND
        ));
        trackDirectionChoiceBox.getSelectionModel().select(PassengerMovement.TravelDirection.NORTHBOUND);

        saveTrackButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteTrackButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
    }

    // Initialize the wall UI controls
    private static void initializeWalls(
            Label wallTypeLabel,
            ChoiceBox<Wall.WallType> wallTypeChoiceBox,
            Button saveWallButton,
            Button deleteWallButton,
            Button flipWallButton
    ) {
        initializeWall(
                wallTypeLabel,
                wallTypeChoiceBox,
                saveWallButton,
                deleteWallButton,
                flipWallButton
        );
    }

    // Initialize the wall controls
    private static void initializeWall(
            Label wallTypeLabel,
            ChoiceBox<Wall.WallType> wallTypeChoiceBox,
            Button saveWallButton,
            Button deleteWallButton,
            Button flipWallButton
    ) {
        wallTypeLabel.setLabelFor(wallTypeChoiceBox);

        wallTypeChoiceBox.setItems(FXCollections.observableArrayList(
                Wall.WallType.WALL,
                Wall.WallType.BUILDING_COLUMN,
                Wall.WallType.BELT_BARRIER,
                Wall.WallType.METAL_BARRIER
        ));
        wallTypeChoiceBox.getSelectionModel().select(Wall.WallType.WALL);

        saveWallButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
        deleteWallButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);

        flipWallButton.disableProperty().bind(InitializeMainScreenService.SAVE_DELETE_BINDING);
    }

    // Iterate through each build subtab to set the listeners for the changing of build categories and subcategories
    private static void initializeCategoryListeners(TabPane buildTabPane) {
        List<Tab> buildSubtabs = buildTabPane.getTabs();

        for (Tab subtab : buildSubtabs) {
            // Get the accordion within this tab and its titled panes
            Accordion currentAccordion = (Accordion) subtab.getContent();
            List<TitledPane> currentTitledPanes = currentAccordion.getPanes();

            for (TitledPane currentTitledPane : currentTitledPanes) {
                currentAccordion.expandedPaneProperty().addListener((observable, oldValue, newValue) -> {
                    // If the this pane is expanded, and the current pane is the one recently expanded, update the
                    // current subcategory
                    // The previous state was either from no expanded subcategories or from another subcategory
                    if (currentTitledPane.isExpanded() && currentTitledPane == newValue) {
                        Main.simulator.setBuildSubcategory(
                                MainScreenController.getBuildSubcategory(buildTabPane.getSelectionModel())
                        );

                        Main.mainScreenController.updatePromptText();
                        GraphicsController.updateCurrentAmenityFootprint();
                    } else {
                        // If not, just check if this pane is the previous subcategory and the new subcategory is null
                        // The previous state was either from an expanded subcategory or has been replaced with another
                        // subcategory
                        if (currentTitledPane == oldValue && currentAccordion.getExpandedPane() == null) {
                            Main.simulator.setBuildSubcategory(
                                    MainScreenController.getBuildSubcategory(buildTabPane.getSelectionModel())
                            );

                            Main.mainScreenController.updatePromptText();
                            GraphicsController.updateCurrentAmenityFootprint();
                        }
                    }
                });

                subtab.setOnSelectionChanged(event -> {
                    if (subtab.isSelected()) {
                        // Update the categories and subcategories
                        Main.simulator.setBuildCategory(
                                MainScreenController.getBuildCategory(buildTabPane.getSelectionModel())
                        );

                        Main.simulator.setBuildSubcategory(
                                MainScreenController.getBuildSubcategory(buildTabPane.getSelectionModel())
                        );

                        Main.mainScreenController.updatePromptText();
                        GraphicsController.updateCurrentAmenityFootprint();
                    }
                });
            }
        }
    }

    private static void initializeSimulationControls(
            Text elapsedTimeText,
            ToggleButton playButton,
            Button resetButton,
            Label simulationSpeedLabel,
            Slider simulationSpeedSlider
    ) {
        simulationSpeedLabel.setLabelFor(simulationSpeedSlider);
        simulationSpeedSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            SimulationTime.SLEEP_TIME_MILLISECONDS.set((int) (1.0 / newValue.intValue() * 1000));
        }));
    }

    private static void initializePassengerControls(
            Text passengerCountStationText,
            Text passengerCountFloorText
    ) {

    }

    private static void initializePlatformControls(
            Label platformDirectionLabel,
            ChoiceBox<PassengerMovement.TravelDirection> platformDirectionChoiceBox,
            Label platformCarriagesLabel,
            ChoiceBox<TrainDoor.TrainDoorCarriage> platformCarriagesChoiceBox,
            ToggleButton openTrainDoorsButton
    ) {
        platformDirectionLabel.setLabelFor(platformDirectionChoiceBox);

        platformDirectionChoiceBox.setItems(FXCollections.observableArrayList(
                PassengerMovement.TravelDirection.NORTHBOUND,
                PassengerMovement.TravelDirection.SOUTHBOUND,
                PassengerMovement.TravelDirection.WESTBOUND,
                PassengerMovement.TravelDirection.EASTBOUND
        ));
        platformDirectionChoiceBox.getSelectionModel().select(PassengerMovement.TravelDirection.NORTHBOUND);

        platformCarriagesLabel.setLabelFor(platformDirectionChoiceBox);

        platformCarriagesChoiceBox.setItems(FXCollections.observableArrayList(
                TrainDoor.TrainDoorCarriage.LRT_1_FIRST_GENERATION,
                TrainDoor.TrainDoorCarriage.LRT_1_SECOND_GENERATION,
                TrainDoor.TrainDoorCarriage.LRT_1_THIRD_GENERATION,
                TrainDoor.TrainDoorCarriage.LRT_2_FIRST_GENERATION,
                TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION,
                TrainDoor.TrainDoorCarriage.MRT_3_SECOND_GENERATION
        ));
        platformCarriagesChoiceBox.getSelectionModel().select(TrainDoor.TrainDoorCarriage.LRT_1_FIRST_GENERATION);
    }

    public static void initializeScrollPane(
            ScrollPane scrollPane,
            StackPane stackPane
    ) {
        // The canvas should only be pannable by the mouse when there are no subcategories selected
        scrollPane.pannableProperty().bind(
                Bindings.equal(
                        Main.simulator.buildSubcategoryProperty(),
                        Simulator.BuildSubcategory.NONE
                )
        );

        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            final KeyCombination zoomInCombination
                    = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);
            final KeyCombination zoomOutCombination
                    = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
            final KeyCombination normalZoomCombination
                    = new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN);

            if (zoomInCombination.match(event)) {
                double newScaleX = stackPane.getScaleX() * 1.25;
                double newScaleY = stackPane.getScaleY() * 1.25;

                stackPane.setScaleX(newScaleX);
                stackPane.setScaleY(newScaleY);
            } else if (zoomOutCombination.match(event)) {
                double newScaleX = stackPane.getScaleX() * 0.75;
                double newScaleY = stackPane.getScaleY() * 0.75;

                stackPane.setScaleX(newScaleX);
                stackPane.setScaleY(newScaleY);
            } else if (normalZoomCombination.match(event)) {
                stackPane.setScaleX(1.0);
                stackPane.setScaleY(1.0);
            }
        });
    }
}

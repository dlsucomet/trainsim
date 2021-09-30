package com.crowdsimulation.model.simulator;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerTripInformation;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Track;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Wall;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.PortalShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

// The simulator has total control over the aspects of the crowd simulation
public class Simulator {
    // State variables
    // Denotes the current mode of operation in the program
    private final SimpleObjectProperty<OperationMode> operationMode;

    // Denotes the current category within the build operation
    private final SimpleObjectProperty<BuildCategory> buildCategory;

    // Denotes the current build category within the current build category
    private final SimpleObjectProperty<BuildSubcategory> buildSubcategory;
    private final SimpleObjectProperty<Class<? extends Amenity>> buildSubcategoryClass;

    // Denotes the current state mostly while the program is in building mode
    private final SimpleObjectProperty<BuildState> buildState;

    // Context variables
    // Denotes the current station loaded
    private Station station;

    // Denotes the index of the floor displayed
    private final SimpleIntegerProperty currentFloorIndex;

    // Denotes the floor currently displayed
    private Floor currentFloor;

    // Denotes the current amenity and classes being drawn or edited
    private final SimpleObjectProperty<Amenity> currentAmenity;
    private final SimpleObjectProperty<Class<? extends Amenity>> currentClass;

    // Miscellaneous variables
    private final SimpleBooleanProperty portalDrawing;
    private final SimpleBooleanProperty firstPortalDrawn;

    private Portal firstPortal;
    private PortalShaft provisionalPortalShaft;

    private final SimpleBooleanProperty floorFieldDrawing;
    private final SimpleBooleanProperty isStationRunOnly;

    private Queueable currentFloorFieldTarget;
    private QueueingFloorField.FloorFieldState currentFloorFieldState;

    private final SimpleBooleanProperty validatingFromRunning;

    // Simulator variables
    private final AtomicBoolean stationValid;
    private final AtomicBoolean running;

    // Denotes the current time in the simulation
    private final SimulationTime time;
    private final Semaphore playSemaphore;

    // Random number generator for all purposes in the simulation
    public static final Random RANDOM_NUMBER_GENERATOR;

    // Simulation variables
//    private static final List<Passenger> passengersToSwitchFloors;
//    private static final List<Passenger> passengersToDespawn;

    static {
        RANDOM_NUMBER_GENERATOR = new Random();

//        passengersToSwitchFloors = Collections.synchronizedList(new ArrayList<>());
//        passengersToDespawn = Collections.synchronizedList(new ArrayList<>());
    }

    public Simulator() {
        // The program is initially in the building mode
        this.operationMode = new SimpleObjectProperty<>(OperationMode.BUILDING);

        // The program is initially in the entrances/exits tab
        this.buildCategory = new SimpleObjectProperty<>(BuildCategory.ENTRANCES_AND_EXITS);

        // The program initially does not have a build subcategory
        this.buildSubcategory = new SimpleObjectProperty<>(BuildSubcategory.NONE);
        this.buildSubcategoryClass = new SimpleObjectProperty<>(null);

        // The program is initially in the drawing state
        this.buildState = new SimpleObjectProperty<>(BuildState.DRAWING);

        this.currentAmenity = new SimpleObjectProperty<>(null);
        this.currentClass = new SimpleObjectProperty<>(null);

        this.station = null;
        this.currentFloorIndex = new SimpleIntegerProperty(0);
        this.currentFloor = null;

        this.portalDrawing = new SimpleBooleanProperty(false);
        this.firstPortalDrawn = new SimpleBooleanProperty(false);

        this.firstPortal = null;
        this.provisionalPortalShaft = null;

        this.floorFieldDrawing = new SimpleBooleanProperty(false);
        this.currentFloorFieldTarget = null;

        this.isStationRunOnly = new SimpleBooleanProperty(false);

        this.validatingFromRunning = new SimpleBooleanProperty(false);

        this.stationValid = new AtomicBoolean(false);
        this.running = new AtomicBoolean(false);

        this.time = new SimulationTime(0, 0, 0);
        this.playSemaphore = new Semaphore(0);

        // Start the simulation thread, but in reality it would be activated much later
        this.start();
    }

    public SimpleObjectProperty<OperationMode> operationModeProperty() {
        return operationMode;
    }

    public OperationMode getOperationMode() {
        return operationMode.get();
    }

    public SimpleObjectProperty<BuildCategory> buildCategoryProperty() {
        return buildCategory;
    }

    public BuildCategory getBuildCategory() {
        return buildCategory.get();
    }

    public SimpleObjectProperty<BuildSubcategory> buildSubcategoryProperty() {
        return buildSubcategory;
    }

    public BuildSubcategory getBuildSubcategory() {
        return buildSubcategory.get();
    }

    public Class<? extends Amenity> getBuildSubcategoryClass() {
        return buildSubcategoryClass.get();
    }

    public SimpleObjectProperty<Class<? extends Amenity>> buildSubcategoryClassProperty() {
        return buildSubcategoryClass;
    }

    public SimpleObjectProperty<BuildState> buildStateProperty() {
        return buildState;
    }

    public BuildState getBuildState() {
        return buildState.get();
    }

    public void setOperationMode(OperationMode operationMode) {
        this.operationMode.set(operationMode);
    }

    public void setBuildCategory(BuildCategory buildCategory) {
        this.buildCategory.set(buildCategory);
    }

    public void setBuildSubcategory(BuildSubcategory buildSubcategory) {
        this.buildSubcategory.set(buildSubcategory);

        // Also set the class equivalent of this build subcategory
        this.buildSubcategoryClass.set(Simulator.buildSubcategoryToClass(this.buildSubcategory.get()));
    }

    public void setBuildState(BuildState buildState) {
        this.buildState.set(buildState);
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public SimpleIntegerProperty currentFloorIndexProperty() {
        return currentFloorIndex;
    }

    public int getCurrentFloorIndex() {
        return currentFloorIndex.get();
    }

    public void setCurrentFloorIndex(int currentFloorIndex) {
        this.currentFloorIndex.set(currentFloorIndex);
    }

    public Floor getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(Floor currentFloor) {
        this.currentFloor = currentFloor;
    }

    public SimpleObjectProperty<Amenity> currentAmenityProperty() {
        return currentAmenity;
    }

    public Amenity getCurrentAmenity() {
        return currentAmenity.get();
    }

    public void setCurrentAmenity(Amenity currentAmenity) {
        this.currentAmenity.set(currentAmenity);
    }

    public SimpleObjectProperty<Class<? extends Amenity>> currentClassProperty() {
        return currentClass;
    }

    public Class<? extends Amenity> getCurrentClass() {
        return currentClass.get();
    }

    public void setCurrentClass(Class<? extends Amenity> currentClass) {
        this.currentClass.set(currentClass);
    }

    public boolean isPortalDrawing() {
        return portalDrawing.get();
    }

    public SimpleBooleanProperty portalDrawingProperty() {
        return portalDrawing;
    }

    public void setPortalDrawing(boolean portalDrawing) {
        this.portalDrawing.set(portalDrawing);
    }

    public boolean isFirstPortalDrawn() {
        return firstPortalDrawn.get();
    }

    public SimpleBooleanProperty firstPortalDrawnProperty() {
        return firstPortalDrawn;
    }

    public void setFirstPortalDrawn(boolean firstPortalDrawn) {
        this.firstPortalDrawn.set(firstPortalDrawn);
    }

    public Portal getFirstPortal() {
        return firstPortal;
    }

    public void setFirstPortal(Portal firstPortal) {
        this.firstPortal = firstPortal;
    }

    public PortalShaft getProvisionalPortalShaft() {
        return provisionalPortalShaft;
    }

    public void setProvisionalPortalShaft(PortalShaft provisionalPortalShaft) {
        this.provisionalPortalShaft = provisionalPortalShaft;
    }

    public boolean isFloorFieldDrawing() {
        return floorFieldDrawing.get();
    }

    public SimpleBooleanProperty floorFieldDrawingProperty() {
        return floorFieldDrawing;
    }

    public void setFloorFieldDrawing(boolean floorFieldDrawing) {
        this.floorFieldDrawing.set(floorFieldDrawing);
    }

    public boolean isStationRunOnly() {
        return isStationRunOnly.get();
    }

    public SimpleBooleanProperty isStationRunOnlyProperty() {
        return isStationRunOnly;
    }

    public void setStationRunOnly(boolean isStationRunOnly) {
        this.isStationRunOnly.set(isStationRunOnly);
    }

    public Queueable getCurrentFloorFieldTarget() {
        return currentFloorFieldTarget;
    }

    public void setCurrentFloorFieldTarget(Queueable currentFloorFieldTarget) {
        this.currentFloorFieldTarget = currentFloorFieldTarget;
    }

    public QueueingFloorField.FloorFieldState getCurrentFloorFieldState() {
        return currentFloorFieldState;
    }

    public void setCurrentFloorFieldState(QueueingFloorField.FloorFieldState currentFloorFieldState) {
        this.currentFloorFieldState = currentFloorFieldState;
    }

    public boolean getValidatingFromRunning() {
        return this.validatingFromRunning.get();
    }

    public void setValidatingFromRunning(boolean validatingFromRunning) {
        this.validatingFromRunning.set(validatingFromRunning);
    }

    public void setStationValid(boolean stationValid) {
        this.stationValid.set(stationValid);
    }

    public boolean isStationValid() {
        return stationValid.get();
    }

    public AtomicBoolean getRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public boolean isRunning() {
        return running.get();
    }

    public SimulationTime getSimulationTime() {
        return time;
    }

    public Semaphore getPlaySemaphore() {
        return playSemaphore;
    }

    public void resetToDefaultConfiguration(Station station) {
        // The program is initially in the building mode
        this.operationMode.set(OperationMode.BUILDING);

        // The program is initially in the entrances/exits tab
        this.buildCategory.set(BuildCategory.ENTRANCES_AND_EXITS);

        // The program initially does not have a build subcategory
        this.buildSubcategory.set(BuildSubcategory.NONE);
        this.buildSubcategoryClass.set(null);

        // The program is initially in the drawing state
        this.buildState.set(BuildState.DRAWING);

        this.currentAmenity.set(null);
        this.currentClass.set(null);

        this.station = station;
        this.currentFloorIndex.set(0);
        this.currentFloor = station.getFloors().get(this.currentFloorIndex.get());

        this.portalDrawing.set(false);
        this.firstPortalDrawn.set(false);

        this.isStationRunOnly.set(station.isRunOnly());

        this.firstPortal = null;
        this.provisionalPortalShaft = null;

        this.floorFieldDrawing.set(false);
        this.currentFloorFieldTarget = null;

        this.time.reset();
        this.running.set(false);
    }

    // Convert a build subcategory to its corresponding class
    private static Class<? extends Amenity> buildSubcategoryToClass(BuildSubcategory buildSubcategory) {
        switch (buildSubcategory) {
            case STATION_ENTRANCE_EXIT:
                return StationGate.class;
            case SECURITY:
                return Security.class;
            case STAIRS:
                return StairPortal.class;
            case ESCALATOR:
                return EscalatorPortal.class;
            case ELEVATOR:
                return ElevatorPortal.class;
            case TICKET_BOOTH:
                return TicketBooth.class;
            case TURNSTILE:
                return Turnstile.class;
            case TRAIN_BOARDING_AREA:
                return TrainDoor.class;
            case TRAIN_TRACK:
                return Track.class;
            case OBSTACLE:
                return Wall.class;
        }

        return null;
    }

    // Start the simulation thread
    private void start() {
        // Run this on a thread so it won't choke the JavaFX UI thread
        new Thread(() -> {
            // For times shorter than this, speed awareness will be implemented
            final int speedAwarenessLimitMilliseconds = 10;

            // Initialize a thread pool to run floors in parallel
            final int NUM_CPUS = Runtime.getRuntime().availableProcessors();
            ExecutorService floorExecutorService = Executors.newFixedThreadPool(NUM_CPUS);

            while (true) {
                try {
                    // Wait until the play button has been pressed
                    playSemaphore.acquire();

                    // Keep looping until paused
                    while (this.isRunning()) {
                        try {
                            // Update the pertinent variables when ticking
                            Simulator.updatePassengersInStation(
                                    floorExecutorService,
                                    Main.simulator.getStation(),
                                    null,
                                    false
                            );
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        // Redraw the visualization
                        // If the refreshes are frequent enough, update the visualization in a speed-aware manner
                        // That is, avoid having too many refreshes within a short period of time
                        Main.mainScreenController.drawStationViewFloorForeground(
                                Main.simulator.getCurrentFloor(),
                                SimulationTime.SLEEP_TIME_MILLISECONDS.get()
                                        < speedAwarenessLimitMilliseconds
                        );

                        // Increment (tick) the clock
                        this.time.tick();

                        Thread.sleep(SimulationTime.SLEEP_TIME_MILLISECONDS.get());
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    // Manage all passenger-related updates
    public static void updatePassengersInStation(
            ExecutorService floorExecutorService,
            Station station,
            List<PassengerTripInformation> passengersToSpawn,
            boolean willDrawFromPassengerList
    ) throws InterruptedException {
        List<Passenger> passengersToSwitchFloors = Collections.synchronizedList(new ArrayList<>());
        List<Passenger> passengersToDespawn = Collections.synchronizedList(new ArrayList<>());
        List<Passenger> passengersToBoard = Collections.synchronizedList(new ArrayList<>());
        List<Passenger> passengersToDispose = Collections.synchronizedList(new ArrayList<>());

        // For each portal, update its passengers' time spent values
        Simulator.updatePassengersInPortals(station);

        // Update all passengers on the floors
        Simulator.updatePassengersOnFloors(
                floorExecutorService,
                station,
                passengersToSpawn,
                passengersToSwitchFloors,
                passengersToDespawn,
                passengersToBoard,
                passengersToDispose,
                willDrawFromPassengerList
        );

        // Manage all passenger despawns
        Simulator.manageDespawning(
                passengersToSwitchFloors,
                passengersToDespawn,
                passengersToBoard,
                passengersToDispose,
                willDrawFromPassengerList
        );
    }

    // For each portal, update its passengers' time spent values
    private static void updatePassengersInPortals(Station station) {
        for (StairShaft stairShaft : station.getStairShafts()) {
            stairShaft.updateQueues();
        }

        for (EscalatorShaft escalatorShaft : station.getEscalatorShafts()) {
            escalatorShaft.updateQueues();
        }
    }

    // Update all passengers on the floors
    private static void updatePassengersOnFloors(
            ExecutorService executorService,
            Station station,
            List<PassengerTripInformation> passengersToSpawn,
            List<Passenger> passengersToSwitchFloors,
            List<Passenger> passengersToDespawn,
            List<Passenger> passengersToBoard,
            List<Passenger> passengersToDispose,
            boolean willDrawFromPassengerList
    ) throws InterruptedException {
        List<FloorUpdateTask> floorsToUpdate = new ArrayList<>();
        HashMap<PassengerTripInformation, StationGate> spawnMap = null;

        if (passengersToSpawn != null) {
            spawnMap = collectPassengersToSpawn(station.getFloors(), passengersToSpawn);
        }

        for (Floor floor : station.getFloors()) {
            spawnPassengersOnFloor(floor, spawnMap, willDrawFromPassengerList);
//            spawnPassengersOnFloor(floor);

            floorsToUpdate.add(
                    new FloorUpdateTask(
                            floor,
                            willDrawFromPassengerList,
                            passengersToSwitchFloors,
                            passengersToDespawn,
                            passengersToBoard,
                            passengersToDispose
                    )
            );
        }

        executorService.invokeAll(floorsToUpdate);
    }

    private static HashMap<PassengerTripInformation, StationGate> collectPassengersToSpawn(
            List<Floor> floors,
            List<PassengerTripInformation> passengersToSpawn
    ) {
        HashMap<PassengerTripInformation, StationGate> spawnMap = new HashMap<>();

        // For each passenger to spawn, get the station gates it is eligible to spawn from
        // Pick the one with the shortest backlog
        for (PassengerTripInformation passengerTripInformation : passengersToSpawn) {
            List<StationGate> eligibleStationGates = new ArrayList<>();

            int leastBacklogs = Integer.MAX_VALUE;

            // Compile all station gates which are eligible to spawn this passenger
            for (Floor floor : floors) {
                for (StationGate stationGate : floor.getStationGates()) {
                    if (
                            stationGate.isEnabled()
                                    && stationGate.getStationGateMode() != StationGate.StationGateMode.EXIT
                    ) {
                        if (
                                stationGate.getStationGatePassengerTravelDirections().contains(
                                        passengerTripInformation.getTravelDirection()
                                )
                        ) {
                            com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station
                                    station = stationGate.getAmenityBlocks().get(0).getPatch().getFloor().getStation()
                                    .getStation();

                            final int currentBacklogs = station.getPassengerBacklogs().get(stationGate).size();

                            if (currentBacklogs < leastBacklogs) {
                                eligibleStationGates.clear();
                                eligibleStationGates.add(stationGate);

                                leastBacklogs = currentBacklogs;
                            } else if (currentBacklogs == leastBacklogs) {
                                eligibleStationGates.add(stationGate);
                            }
                        }
                    }
                }
            }

            // From the station gates compiled, choose one which will actually spawn this passenger
            int eligibleStationGatesSize = eligibleStationGates.size();
            int randomIndex = Simulator.RANDOM_NUMBER_GENERATOR.nextInt(eligibleStationGatesSize);

            StationGate chosenStationGate = eligibleStationGates.get(randomIndex);

            spawnMap.put(passengerTripInformation, chosenStationGate);
        }

        return spawnMap;
    }

    // Manage the passengers which spawn on the floor
    private static void spawnPassengersOnFloor(
            Floor floor,
            HashMap<PassengerTripInformation, StationGate> spawnMap,
            boolean willDrawFromPassengerList
    ) {
        // Take note of all station gates which did not spawn passengers this tick
        List<StationGate> stationGatesWithoutSpawn = new ArrayList<>(floor.getStationGates());

        if (willDrawFromPassengerList) {
            if (spawnMap != null) {
                // Spawn all passengers from their chosen station gates
                for (Map.Entry<PassengerTripInformation, StationGate> entry : spawnMap.entrySet()) {
                    PassengerTripInformation passengerTripInformation = entry.getKey();
                    StationGate stationGate = entry.getValue();

                    if (stationGate.getAmenityBlocks().get(0).getPatch().getFloor().equals(floor)) {
                        spawnPassenger(floor, stationGate, passengerTripInformation);

                        stationGatesWithoutSpawn.remove(stationGate);
                    }
                }
            }

            // In all station gates which did not spawn a passenger, use the free time to spawn one passenger from its
            // backlog, if any
            for (StationGate stationGate : stationGatesWithoutSpawn) {
                // If no passenger happens to spawn this tick, use this free time to spawn a passenger from the
                // backlog, if there are any
                // TODO: Offload to station gate itself
                spawnPassengerFromStationGateBacklog(
                        floor,
                        stationGate,
                        willDrawFromPassengerList
                );
            }

            // Spawn from all open train doors
            synchronized (floor.getTrainDoors()) {
                for (TrainDoor trainDoor : floor.getTrainDoors()) {
                    if (trainDoor.isOpen()) {
                        trainDoor.releasePassenger(false);
                    }
                }
            }
        } else {
            // Make all station gates in this floor spawn passengers depending on their spawn frequency
            // Generate a number from 0.0 to 1.0
            double boardingRandomNumber;
            double alightingRandomNumber;

            final double alightingChancePerSecond = 0.1;

            // Spawn boarding passengers from the station gate
            for (StationGate stationGate : floor.getStationGates()) {
                boardingRandomNumber = RANDOM_NUMBER_GENERATOR.nextDouble();

                // Only deal with station gates which have entrances
                if (
                        stationGate.isEnabled()
                                && stationGate.getStationGateMode() != StationGate.StationGateMode.EXIT
                ) {
                    // Spawn passengers depending on the spawn frequency of the station gate
                    if (stationGate.getChancePerSecond() > boardingRandomNumber) {
                        spawnPassenger(floor, stationGate);
                    } else {
                        // If no passenger happens to spawn this tick, use this free time to spawn a passenger from the
                        // backlog, if there are any
                        spawnPassengerFromStationGateBacklog(floor, stationGate, false);
                    }
                }
            }

            // Spawn alighting passengers from the train doors
            for (TrainDoor trainDoor : floor.getTrainDoors()) {
                alightingRandomNumber = RANDOM_NUMBER_GENERATOR.nextDouble();

                if (trainDoor.isOpen()) {
                    if (alightingChancePerSecond > alightingRandomNumber) {
                        spawnPassenger(floor, trainDoor);
                    }
                }
            }
        }
    }

    // Entertain each passenger marked for switching floors
    private static void manageDespawning(
            List<Passenger> passengersToSwitchFloors,
            List<Passenger> passengersToDespawn,
            List<Passenger> passengersToBoard,
            List<Passenger> passengersToDispose,
            boolean willDrawFromPassengerList
    ) {
        try {
            // Manage passengers to switch floors
            for (Passenger passengerToSwitchFloors : passengersToSwitchFloors) {
                // Get the passenger's portal
                Portal portal = (Portal) passengerToSwitchFloors.getPassengerMovement().getCurrentAmenity();

                // Have the gate absorb that passenger
                portal.absorb(passengerToSwitchFloors);
            }

            // Remove all passengers that are marked for removal
            for (Passenger passengerToDespawn : passengersToDespawn) {
                // Record the time it took
                passengerToDespawn.getPassengerTime().exitStation();

                // Get the passenger's gate
                Gate gate = (Gate) passengerToDespawn.getPassengerMovement().getCurrentAmenity();

                // Have the gate despawn that passenger
                gate.despawnPassenger(passengerToDespawn);

                // Remove the passenger from the train system
                // TODO: Have a better way of checking whether this passenger has necessary information, or not
                //  perhaps insert a different passenger information object to a passenger when at train simulation and
                //  at station editing?
                if (willDrawFromPassengerList) {
                    passengerToDespawn.getPassengerMovement().getRoutePlan().getOriginStation().getTrainSystem()
                            .getPassengers().remove(passengerToDespawn);

                    // Log the passenger who completed the journey
                    com.trainsimulation.model.simulator.Simulator.logPassenger(passengerToDespawn);
                }
            }

            // Have passengers board the carriage connected to its train door
            for (Passenger passengerToBoard : passengersToBoard) {
                // Get the passenger's train door
                TrainDoor trainDoor = (TrainDoor) passengerToBoard.getPassengerMovement().getCurrentAmenity();

                // Have the train door transfer that passenger to the carriage
                trainDoor.boardPassenger(passengerToBoard);
            }

            // Simply discard passengers with errors
            if (willDrawFromPassengerList) {
                for (Passenger passengerToDispose : passengersToDispose) {
                    // Get the passenger's gate
                    Gate gate = (Gate) passengerToDispose.getPassengerMovement().getCurrentAmenity();

                    // Have the gate despawn that passenger
                    gate.despawnPassenger(passengerToDispose);

                    // Remove the passenger from the train system
                    passengerToDispose.getPassengerMovement().getRoutePlan().getOriginStation().getTrainSystem()
                            .getPassengers().remove(passengerToDispose);
                }
            }

            passengersToDespawn.clear();
            passengersToSwitchFloors.clear();
            passengersToBoard.clear();
            passengersToDispose.clear();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    // Reset the simulation
    public void reset() {
        // Reset the simulation time
        this.time.reset();
    }

    // Make all agents tick (move once in a one-second time frame) in the given floor
    private static void updateFloor(
            Floor floor,
            boolean willDrawFromPassengerList,
            List<Passenger> passengersToSwitchFloors,
            List<Passenger> passengersToDespawn,
            List<Passenger> passengersToBoard,
            List<Passenger> passengersToDispose
    ) {
//        for (Turnstile turnstile : floor.getTurnstiles()) {
//            if (
//                    com.trainsimulation.model.simulator.Simulator.isCrowdControlImplemented.get()
//            ) {
//                if (
//                        com.trainsimulation.model.simulator.Simulator.willBlock.get()
//                ) {
//                    turnstile.setBlockEntry(true);
//                } else {
//                    turnstile.setBlockEntry(false);
//                }
//            }
//        }

        // Make each passenger move
        synchronized (floor.getPassengersInFloor()) {
            for (Passenger passenger : floor.getPassengersInFloor()) {
                try {
                    movePassenger(
                            passenger,
                            willDrawFromPassengerList,
                            passengersToSwitchFloors,
                            passengersToDespawn,
                            passengersToBoard
                    );

                    // Also update the graphic of the passenger
                    passenger.getPassengerGraphic().change();
                } catch (Exception ex) {
//                    if (willDrawFromPassengerList) {
//                        // In case of exceptions, dispose of offending passengers
//                        System.out.println("Disposed " + passenger);
//
//                        passengersToDespawn.remove(passenger);
//                        passengersToDispose.add(passenger);
//                    }

                    ex.printStackTrace();
                }
            }
        }
    }

    private static void movePassenger(
            Passenger passenger,
            boolean willDrawFromPassengerList,
            List<Passenger> passengersToSwitchFloors,
            List<Passenger> passengersToDespawn,
            List<Passenger> passengersToBoard
    ) throws Exception {
        PassengerMovement passengerMovement = passenger.getPassengerMovement();

        // Get the three passenger movement states
        PassengerMovement.Disposition disposition = passengerMovement.getDisposition();
        PassengerMovement.State state = passengerMovement.getState();
        PassengerMovement.Action action = passengerMovement.getAction();

        switch (disposition) {
            case BOARDING:
            case ALIGHTING:
                // The passenger has entered the station and is heading towards the platform to board the train
                switch (state) {
                    case WALKING:
                        if (action == PassengerMovement.Action.WILL_QUEUE) {
                            // Look for the goal nearest to this passenger
                            passengerMovement.chooseGoal();

                            // Check if this passenger is set to use a portal to go to another floor
                            if (passengerMovement.willHeadToPortal()) {
                                // Make this passenger face the goal portal
                                passengerMovement.faceNextPosition();

                                // Move towards that direction
                                passengerMovement.moveSocialForce();

                                // Set the appropriate action
                                if (passengerMovement.isGoalFloorLower()) {
                                    passengerMovement.setAction(PassengerMovement.Action.WILL_DESCEND);
                                    action = PassengerMovement.Action.WILL_DESCEND;
                                } else {
                                    passengerMovement.setAction(PassengerMovement.Action.WILL_ASCEND);
                                    action = PassengerMovement.Action.WILL_ASCEND;
                                }

                                break;
                            } else {
                                // This passenger is set to stay on this floor, so simply move towards its goal
                                if (
                                        passengerMovement.getParent().getTicketType()
                                                == TicketBooth.TicketType.SINGLE_JOURNEY
                                                || passengerMovement.getParent().getTicketType()
                                                == TicketBooth.TicketType.STORED_VALUE
                                                && !passengerMovement.willPathFind()
                                ) {
                                    // Make this passenger face the set goal, its queueing area, or the passenger at the
                                    // tail of the queue
                                    passengerMovement.faceNextPosition();

                                    // Move towards that direction
                                    passengerMovement.moveSocialForce();

                                    if (passengerMovement.hasEncounteredPassengerToFollow()) {
                                        // If the passenger did not move, and there is someone blocking it while queueing,
                                        // transition into the "in queue" state and the "assembling" action
                                        passengerMovement.joinQueue();

                                        passengerMovement.setState(PassengerMovement.State.IN_QUEUE);
                                        state = PassengerMovement.State.IN_QUEUE;

                                        passengerMovement.setAction(PassengerMovement.Action.ASSEMBLING);
                                        action = PassengerMovement.Action.ASSEMBLING;

                                        // If this passenger is a stored value card holder, signal that there will
                                        // be no more need to pathfind
                                        if (
                                                passengerMovement.getParent().getTicketType()
                                                        == TicketBooth.TicketType.STORED_VALUE
                                        ) {
                                            passengerMovement.endStoredValuePathfinding();
                                        }

                                        break;
                                    }

                                    // Check whether the passenger's next amenity is a queueable
                                    // If it is, check whether the passenger has reached its floor field
                                    if (passengerMovement.isNextAmenityQueueable()) {
                                        // If the passenger has reached the patch with the nearest floor field value,
                                        // transition
                                        // into the "in queue" state and the "queueing" action
                                        if (passengerMovement.hasReachedQueueingFloorField()) {
                                            // Mark this passenger as the latest one to join its queue
                                            passengerMovement.joinQueue();

                                            passengerMovement.setState(PassengerMovement.State.IN_QUEUE);
                                            state = PassengerMovement.State.IN_QUEUE;

                                            if (passengerMovement.isNextAmenityTrainDoor()) {
                                                passengerMovement.setAction(PassengerMovement.Action.WAITING_FOR_TRAIN);
                                                action = PassengerMovement.Action.WAITING_FOR_TRAIN;
                                            } else {
                                                passengerMovement.setAction(PassengerMovement.Action.QUEUEING);
                                                action = PassengerMovement.Action.QUEUEING;
                                            }

                                            // If this passenger is a stored value card holder, signal that there will
                                            // be no more need to pathfind
                                            if (
                                                    passengerMovement.getParent().getTicketType()
                                                            == TicketBooth.TicketType.STORED_VALUE
                                            ) {
                                                passengerMovement.endStoredValuePathfinding();
                                            }

                                            break;
                                        }
                                    } else {
                                        // If the passenger has reached its non-queueable goal, transition into the
                                        // appropriate state and action
                                        // This non-queueable goal could only be a station gate, so exit the station
                                        if (passengerMovement.hasReachedGoal()) {
                                            // Have the passenger set its current goal
                                            passengerMovement.reachGoal();

                                            // Then have this passenger marked for despawning
                                            passengersToDespawn.add(passenger);

                                            break;
                                        }
                                    }

                                    // If the passenger is stuck, switch to the "rerouting" action except if the
                                    // passenger is a stored value ticket holder
                                    if (
                                            passengerMovement.isStuck()
                                                    && passengerMovement.getState() != PassengerMovement.State.IN_QUEUE
/*                                                    && passengerMovement.getParent().getTicketType()
                                                    != TicketBooth.TicketType.STORED_VALUE*/
                                    ) {
                                        passengerMovement.setAction(PassengerMovement.Action.REROUTING);
                                        action = PassengerMovement.Action.REROUTING;
                                    }

                                    break;
                                } else {
                                    // This passenger is a stored value ticket holder so generate a path, if one hasn't
                                    // been generated yet, then follow it until the passenger reaches its goal
                                    // Get the next path
                                    if (passengerMovement.chooseNextPatchInPath()) {
                                        // Make this passenger face that patch
                                        passengerMovement.faceNextPosition();

                                        // Move towards that patch
                                        passengerMovement.moveSocialForce();

                                        if (passengerMovement.hasEncounteredPassengerToFollow()) {
                                            // If the passenger did not move, and there is someone blocking it while
                                            // queueing, transition into the "in queue" state and the "assembling"
                                            // action
                                            passengerMovement.joinQueue();

                                            passengerMovement.setState(PassengerMovement.State.IN_QUEUE);
                                            state = PassengerMovement.State.IN_QUEUE;

                                            passengerMovement.setAction(PassengerMovement.Action.ASSEMBLING);
                                            action = PassengerMovement.Action.ASSEMBLING;

                                            passengerMovement.endStoredValuePathfinding();

                                            break;
                                        }

                                        if (passengerMovement.hasReachedNextPatchInPath()) {
                                            // The passenger has reached the next patch in the path, so remove this from
                                            // this passenger's current path
                                            passengerMovement.reachPatchInPath();

                                            // Check if there are still patches left in the path
                                            // If there are no more patches left, revert back to the "will queue" action
                                            if (passengerMovement.hasPassengerReachedFinalPatchInPath()) {
                                                passengerMovement.setState(PassengerMovement.State.WALKING);
                                                state = PassengerMovement.State.WALKING;

                                                passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                                action = PassengerMovement.Action.WILL_QUEUE;

                                                passengerMovement.endStoredValuePathfinding();
                                            }

                                            break;
                                        }
                                    } else {
                                        // No more next patches, so transition back into the walking state
                                        passengerMovement.setState(PassengerMovement.State.WALKING);
                                        state = PassengerMovement.State.WALKING;

                                        passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                        action = PassengerMovement.Action.WILL_QUEUE;

                                        passengerMovement.endStoredValuePathfinding();

                                        break;
                                    }
                                }
                            }
                        } else if (
                                action == PassengerMovement.Action.WILL_DESCEND
                                        || action == PassengerMovement.Action.WILL_ASCEND
                        ) {
                            // Check if the passenger is set to switch floors
                            // If it is, this passenger will now head to its chosen portal
                            if (
                                    passengerMovement.getParent().getTicketType()
                                            == TicketBooth.TicketType.SINGLE_JOURNEY
                                            || passengerMovement.getParent().getTicketType()
                                            == TicketBooth.TicketType.STORED_VALUE
                                            && !passengerMovement.willPathFind()
                            ) {
                                // Look for the goal nearest to this passenger
                                passengerMovement.chooseGoal();

                                // Make this passenger face its portal
                                passengerMovement.faceNextPosition();

                                // Then make the passenger move towards that exit
                                passengerMovement.moveSocialForce();
                            } else {
                                // This passenger is a stored value ticket holder so generate a path, if one hasn't been
                                // generated yet, then follow it until the passenger reaches its goal
                                // Get the next path
                                if (passengerMovement.chooseNextPatchInPath()) {
                                    // Make this passenger face that patch
                                    passengerMovement.faceNextPosition();

                                    // Move towards that patch
                                    passengerMovement.moveSocialForce();

                                    if (passengerMovement.hasReachedNextPatchInPath()) {
                                        // The passenger has reached the next patch in the path, so remove this from
                                        // this passenger's current path
                                        passengerMovement.reachPatchInPath();

                                        // Check if there are still patches left in the path
                                        // If there are no more patches left, stop using any pathfinding algorithm
                                        if (passengerMovement.hasPassengerReachedFinalPatchInPath()) {
                                            passengerMovement.endStoredValuePathfinding();
                                        }
                                    }
                                } else {
                                    passengerMovement.endStoredValuePathfinding();
                                }
                            }

                            if (passengerMovement.hasEncounteredPortalWaitingPassenger()) {
                                passengerMovement.beginWaitingOnPortal();
                            } else {
                                passengerMovement.endWaitingOnPortal();
                            }

                            // Check if the passenger is now at the portal
                            if (
                                    passengerMovement.hasReachedGoal()
                            ) {
                                passengerMovement.beginWaitingOnPortal();

                                if (passengerMovement.willEnterPortal()) {
                                    passengerMovement.endWaitingOnPortal();

                                    // Have the passenger set its current goal
                                    passengerMovement.reachGoal();

                                    // Reset the current goal of the passenger
                                    passengerMovement.resetGoal(false);

                                    // Then have this passenger marked for floor switching
                                    passengersToSwitchFloors.add(passenger);
                                } else {
                                    passengerMovement.stop();
                                }

                                break;
                            } else {
                                if (passengerMovement.willEnterPortal()) {
                                    passengerMovement.endWaitingOnPortal();
                                }
                            }

                            // If the passenger is stuck, switch to the "rerouting" action except if the passenger
                            // is a stored value ticket holder
                            if (
                                    passengerMovement.isStuck()/*
                                            && passengerMovement.getParent().getTicketType()
                                            != TicketBooth.TicketType.STORED_VALUE*/
                            ) {
                                passengerMovement.setAction(PassengerMovement.Action.REROUTING);
                                action = PassengerMovement.Action.REROUTING;
                            }

                            break;
                        } else if (action == PassengerMovement.Action.EXITING_STATION) {
                            // This passenger is ready to exit
                            passengerMovement.prepareForStationExit();

                            // This passenger is now heading to its chosen exit
                            passengerMovement.chooseGoal();

                            // Check if this passenger is set to use a portal to go to another floor
                            if (passengerMovement.willHeadToPortal()) {
                                // Make this passenger face the goal portal
                                passengerMovement.faceNextPosition();

                                // Move towards that direction
                                passengerMovement.moveSocialForce();

                                // Set the appropriate action
                                if (passengerMovement.isGoalFloorLower()) {
                                    passengerMovement.setAction(PassengerMovement.Action.WILL_DESCEND);
                                    action = PassengerMovement.Action.WILL_DESCEND;
                                } else {
                                    passengerMovement.setAction(PassengerMovement.Action.WILL_ASCEND);
                                    action = PassengerMovement.Action.WILL_ASCEND;
                                }
                            }

                            if (
                                    passengerMovement.getParent().getTicketType()
                                            == TicketBooth.TicketType.SINGLE_JOURNEY
                                            || passengerMovement.getParent().getTicketType()
                                            == TicketBooth.TicketType.STORED_VALUE
                                            && !passengerMovement.willPathFind()
                            ) {
                                // Make this passenger face its exit
                                passengerMovement.faceNextPosition();

                                // Then make the passenger move towards that exit
                                passengerMovement.moveSocialForce();
                            } else {
                                // This passenger is a stored value ticket holder so generate a path, if one hasn't been
                                // generated yet, then follow it until the passenger reaches its goal
                                // Get the next path
                                if (passengerMovement.chooseNextPatchInPath()) {
                                    // Make this passenger face that patch
                                    passengerMovement.faceNextPosition();

                                    // Move towards that patch
                                    passengerMovement.moveSocialForce();

                                    if (passengerMovement.hasReachedNextPatchInPath()) {
                                        // The passenger has reached the next patch in the path, so remove this from
                                        // this passenger's current path
                                        passengerMovement.reachPatchInPath();

                                        // Check if there are still patches left in the path
                                        // If there are no more patches left, stop using any pathfinding algorithm
                                        if (passengerMovement.hasPassengerReachedFinalPatchInPath()) {
                                            passengerMovement.endStoredValuePathfinding();
                                        }
                                    }
                                } else {
                                    passengerMovement.endStoredValuePathfinding();
                                }
                            }

                            // Check if the passenger is now at the exit
                            if (passengerMovement.hasReachedGoal()) {
                                // Have the passenger set its current goal
                                passengerMovement.reachGoal();

                                // Then have this passenger marked for despawning
                                passengersToDespawn.add(passenger);

                                break;
                            }

                            // If the passenger is stuck, switch to the "rerouting" action except if the passenger
                            // is a stored value ticket holder
                            if (
                                    passengerMovement.isStuck()/*
                                            && passengerMovement.getParent().getTicketType()
                                            != TicketBooth.TicketType.STORED_VALUE*/
                            ) {
                                passengerMovement.setAction(PassengerMovement.Action.REROUTING);
                                action = PassengerMovement.Action.REROUTING;

                                break;
                            }

                            break;
                        } else if (action == PassengerMovement.Action.REROUTING) {
                            // This passenger is stuck, so generate a path, if one hasn't been generated yet, then
                            // follow it until the passenger is not stuck anymore
                            // Get the next path
                            if (passengerMovement.chooseNextPatchInPath()) {
                                // Make this passenger face that patch
                                passengerMovement.faceNextPosition();

                                // Move towards that patch
                                passengerMovement.moveSocialForce();

                                // Check if the passenger has reached its goal
                                if (passengerMovement.hasReachedGoal()) {
                                    // Have the passenger set its current goal
                                    passengerMovement.reachGoal();

                                    if (passengerMovement.getGoalAmenity() instanceof StationGate) {
                                        passengerMovement.setState(PassengerMovement.State.WALKING);
                                        state = PassengerMovement.State.WALKING;

                                        passengerMovement.setAction(PassengerMovement.Action.EXITING_STATION);
                                        action = PassengerMovement.Action.EXITING_STATION;
                                    } else {
                                        passengerMovement.setState(PassengerMovement.State.WALKING);
                                        state = PassengerMovement.State.WALKING;

                                        passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                        action = PassengerMovement.Action.WILL_QUEUE;
                                    }

                                    break;
                                }

                                if (passengerMovement.isReadyToFree()) {
                                    // If the passenger has been moving again for a consistent period of time, free the
                                    // passenger and don't follow the path anymore
                                    if (passengerMovement.getGoalAmenity() instanceof StationGate) {
                                        passengerMovement.setState(PassengerMovement.State.WALKING);
                                        state = PassengerMovement.State.WALKING;

                                        passengerMovement.setAction(PassengerMovement.Action.EXITING_STATION);
                                        action = PassengerMovement.Action.EXITING_STATION;
                                    } else {
                                        passengerMovement.setState(PassengerMovement.State.WALKING);
                                        state = PassengerMovement.State.WALKING;

                                        passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                        action = PassengerMovement.Action.WILL_QUEUE;
                                    }

                                    // Then this passenger will not be stuck anymore
                                    passengerMovement.free();

                                    break;
                                }

                                if (passengerMovement.hasEncounteredPassengerToFollow()) {
                                    // If the passenger did not move, and there is someone blocking it while queueing,
                                    // transition into the "in queue" state and the "assembling" action
                                    passengerMovement.joinQueue();

                                    passengerMovement.setState(PassengerMovement.State.IN_QUEUE);
                                    state = PassengerMovement.State.IN_QUEUE;

                                    passengerMovement.setAction(PassengerMovement.Action.ASSEMBLING);
                                    action = PassengerMovement.Action.ASSEMBLING;

                                    // Then this passenger will not be stuck anymore
                                    passengerMovement.free();

                                    break;
                                }

                                if (passengerMovement.getGoalAmenity() instanceof Portal) {
                                    // Check if the passenger is now at the portal
                                    if (
                                            passengerMovement.hasReachedGoal()
                                    ) {
                                        passengerMovement.beginWaitingOnPortal();

                                        if (passengerMovement.willEnterPortal()) {
                                            passengerMovement.endWaitingOnPortal();

                                            // Have the passenger set its current goal
                                            passengerMovement.reachGoal();

                                            // Reset the current goal of the passenger
                                            passengerMovement.resetGoal(false);

                                            // Then have this passenger marked for floor switching
                                            passengersToSwitchFloors.add(passenger);
                                        } else {
                                            passengerMovement.stop();
                                        }

                                        break;
                                    } else {
                                        if (passengerMovement.willEnterPortal()) {
                                            passengerMovement.endWaitingOnPortal();
                                        }
                                    }
                                }

                                if (passengerMovement.hasReachedNextPatchInPath()) {
                                    // The passenger has reached the next patch in the path, so remove this from this
                                    // passenger's current path
                                    passengerMovement.reachPatchInPath();

                                    // Check if there are still patches left in the path
                                    // If there are no more patches left, revert back to the "will queue" action
                                    if (passengerMovement.hasPassengerReachedFinalPatchInPath()) {
                                        if (passengerMovement.getGoalAmenity() instanceof StationGate) {
                                            passengerMovement.setState(PassengerMovement.State.WALKING);
                                            state = PassengerMovement.State.WALKING;

                                            passengerMovement.setAction(PassengerMovement.Action.EXITING_STATION);
                                            action = PassengerMovement.Action.EXITING_STATION;
                                        } else {
                                            passengerMovement.setState(PassengerMovement.State.WALKING);
                                            state = PassengerMovement.State.WALKING;

                                            passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                            action = PassengerMovement.Action.WILL_QUEUE;
                                        }

                                        // Then this passenger will not be stuck anymore
                                        passengerMovement.free();
                                    }

                                    break;
                                }
                            } else {
                                if (passengerMovement.getGoalAmenity() instanceof StationGate) {
                                    passengerMovement.setState(PassengerMovement.State.WALKING);
                                    state = PassengerMovement.State.WALKING;

                                    passengerMovement.setAction(PassengerMovement.Action.EXITING_STATION);
                                    action = PassengerMovement.Action.EXITING_STATION;
                                } else {
                                    passengerMovement.setState(PassengerMovement.State.WALKING);
                                    state = PassengerMovement.State.WALKING;

                                    passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                    action = PassengerMovement.Action.WILL_QUEUE;
                                }

                                // Then this passenger will not be stuck anymore
                                passengerMovement.free();

                                break;
                            }
                        }
                    case IN_NONQUEUEABLE:
                        // TODO: Like in IN_QUEUEABLE
                        break;
                    case IN_QUEUE:
                        if (action == PassengerMovement.Action.ASSEMBLING) {
                            // The passenger is not yet in the queueing area, but is already queueing
                            // So keep following the end of the queue until the queueing area is reached
                            // Make this passenger face the set goal, its queueing area, or the passenger at the tail of
                            // the queue
                            passengerMovement.faceNextPosition();

                            // Move towards that direction
                            passengerMovement.moveSocialForce();

                            if (passengerMovement.isReadyToFree()) {
                                // Then this passenger will not be stuck anymore
                                passengerMovement.free();
                            }

                            // Check whether the passenger has reached its floor field
                            // If the passenger has reached the patch with the nearest floor field value, transition
                            // into the "queueing" action
                            if (passengerMovement.hasReachedQueueingFloorField()) {
                                if (passengerMovement.isNextAmenityTrainDoor()) {
                                    passengerMovement.setAction(PassengerMovement.Action.WAITING_FOR_TRAIN);
                                    action = PassengerMovement.Action.WAITING_FOR_TRAIN;
                                } else {
                                    passengerMovement.setAction(PassengerMovement.Action.QUEUEING);
                                    action = PassengerMovement.Action.QUEUEING;
                                }
                            }

                            // Check if this passenger has not encountered a queueing passenger anymore
                            if (!passengerMovement.hasEncounteredPassengerToFollow()) {
                                // If the passenger did not move, and there is someone blocking it while queueing,
                                // transition into the "in queue" state and the "assembling" action
                                passengerMovement.leaveQueue();

                                passengerMovement.setState(PassengerMovement.State.WALKING);
                                state = PassengerMovement.State.WALKING;

                                passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                action = PassengerMovement.Action.WILL_QUEUE;

                                break;
                            }
                        } else if (action == PassengerMovement.Action.QUEUEING) {
                            // The passenger is still queueing, so follow the path set by the floor field and its values
                            // Only move if the passenger is not waiting for the amenity to be vacant
                            if (!passengerMovement.isWaitingOnAmenity()) {
                                // In its neighboring patches, look for the patch with the highest floor field
                                passengerMovement.chooseBestQueueingPatch();

                                // Make this passenger face that patch
                                passengerMovement.faceNextPosition();

                                // Move towards that patch
                                passengerMovement.moveSocialForce();
                            }

                            if (passengerMovement.isReadyToFree()) {
                                // Then this passenger will not be stuck anymore
                                passengerMovement.free();
                            }

                            // Check if the passenger is on one of the current floor field's apices
                            // If not, keep following the floor field until it is reached
                            if (passengerMovement.hasReachedQueueingFloorFieldApex()) {
                                // Have the passenger waiting for the amenity to be vacant
                                // TODO: Add waiting for turn state
                                passengerMovement.beginWaitingOnAmenity();

                                // Check first if the goal of this passenger is not currently serving other passengers
                                // If it is, the passenger will now transition into the "heading to queueable" action
                                // Do nothing if there is another passenger still being serviced
                                if (passengerMovement.isGoalFree()) {
                                    // The amenity is vacant, so no need to wait anymore
                                    passengerMovement.endWaitingOnAmenity();

                                    // Have the amenity mark this passenger as the one to be served next
                                    passengerMovement.beginServicingThisPassenger();

                                    passengerMovement.setAction(PassengerMovement.Action.HEADING_TO_QUEUEABLE);
                                    action = PassengerMovement.Action.HEADING_TO_QUEUEABLE;

                                    // Then this passenger will not be stuck anymore
                                    passengerMovement.free();
                                } else {
                                    // Just stop and wait
                                    passengerMovement.stop();
                                }
                            }
                        } else if (action == PassengerMovement.Action.WAITING_FOR_TRAIN) {
                            if (passengerMovement.isReadyToFree()) {
                                // Then this passenger will not be stuck anymore
                                passengerMovement.free();
                            }

                            if (passengerMovement.willEnterTrain()) {
                                // Have the amenity mark this passenger as the one to be served next
                                passengerMovement.beginServicingThisPassenger();

                                passengerMovement.setAction(PassengerMovement.Action.HEADING_TO_TRAIN_DOOR);
                                action = PassengerMovement.Action.HEADING_TO_TRAIN_DOOR;

                                // Then this passenger will not be stuck anymore
                                passengerMovement.free();
                            } else {
                                // In its neighboring patches, look for the patch with the highest floor field
                                passengerMovement.chooseBestQueueingPatch();

                                // Make this passenger face that patch
                                passengerMovement.faceNextPosition();

                                // Move towards that patch
                                passengerMovement.moveSocialForce();
                            }
                        } else if (action == PassengerMovement.Action.HEADING_TO_QUEUEABLE) {
                            // Check if the passenger is now in the goal
                            if (passengerMovement.hasReachedGoal()) {
                                // Check if the passenger is in a pure goal (an amenity with waiting time variables)
                                if (passengerMovement.isNextAmenityGoal()) {
                                    // Transition into the "in queueable" state and the appropriate action
                                    passengerMovement.setState(PassengerMovement.State.IN_QUEUEABLE);
                                    state = PassengerMovement.State.IN_QUEUEABLE;

                                    if (passengerMovement.getGoalAmenity() instanceof Security) {
                                        passengerMovement.setAction(PassengerMovement.Action.SECURITY_CHECKING);
                                        action = PassengerMovement.Action.SECURITY_CHECKING;
                                    } else if (passengerMovement.getGoalAmenity() instanceof TicketBooth) {
                                        passengerMovement.setAction(PassengerMovement.Action.TRANSACTING_TICKET);
                                        action = PassengerMovement.Action.TRANSACTING_TICKET;
                                    } else if (passengerMovement.getGoalAmenity() instanceof Turnstile) {
                                        passengerMovement.setAction(PassengerMovement.Action.USING_TICKET);
                                        action = PassengerMovement.Action.USING_TICKET;
                                    }
                                } else {
                                    // Either the next goal is an elevator
                                    if (passengerMovement.getGoalAmenity() instanceof ElevatorPortal) {
                                        // TODO: The next goal is an elevator, so change to the appropriate actions and
                                        // states
                                    }
                                }
                            } else {
                                // The passenger has exited its goal's floor field and is now headed to the goal itself
                                passengerMovement.chooseGoal();

                                // Make this passenger face the set goal, or its queueing area
                                passengerMovement.faceNextPosition();

                                // Then make the passenger move towards that goal
                                passengerMovement.moveSocialForce();
                            }
                        } else if (action == PassengerMovement.Action.HEADING_TO_TRAIN_DOOR) {
                            // Check if the passenger is now in the goal
                            if (passengerMovement.hasReachedGoal()) {
                                if (passengerMovement.willEnterTrain()) {
                                    // Transition into the "in queueable" state and the appropriate action
                                    passengerMovement.setState(PassengerMovement.State.IN_QUEUEABLE);
                                    state = PassengerMovement.State.IN_QUEUEABLE;

                                    passengerMovement.setAction(PassengerMovement.Action.BOARDING_TRAIN);
                                    action = PassengerMovement.Action.BOARDING_TRAIN;
                                } else {
                                    passengerMovement.endServicingThisPassenger();

                                    // The train door has closed, so revert to waiting for a train
                                    passengerMovement.setState(PassengerMovement.State.IN_QUEUE);
                                    state = PassengerMovement.State.IN_QUEUE;

                                    passengerMovement.setAction(PassengerMovement.Action.WAITING_FOR_TRAIN);
                                    action = PassengerMovement.Action.WAITING_FOR_TRAIN;
                                }
                            } else {
                                if (passengerMovement.willEnterTrain()) {
                                    // The passenger has exited its goal's floor field and is now headed to the goal itself
                                    passengerMovement.chooseGoal();

                                    // Make this passenger face the set goal, or its queueing area
                                    passengerMovement.faceNextPosition();

                                    // Then make the passenger move towards that goal
                                    passengerMovement.moveSocialForce();
                                } else {
                                    passengerMovement.endServicingThisPassenger();

                                    // The train door has closed, so revert to waiting for a train
                                    passengerMovement.setState(PassengerMovement.State.IN_QUEUE);
                                    state = PassengerMovement.State.IN_QUEUE;

                                    passengerMovement.setAction(PassengerMovement.Action.WAITING_FOR_TRAIN);
                                    action = PassengerMovement.Action.WAITING_FOR_TRAIN;
                                }
                            }
                        }
                    case IN_QUEUEABLE:
                        if (action == PassengerMovement.Action.BOARDING_TRAIN) {
                            // Have this passenger's goal wrap up serving this passenger
                            passengerMovement.endServicingThisPassenger();

                            // Leave the queue
                            passengerMovement.leaveQueue();

                            // Have the passenger set its current goal
                            passengerMovement.reachGoal();

                            // Then have this passenger marked for boarding
                            if (willDrawFromPassengerList) {
                                passengersToBoard.add(passenger);
                            } else {
                                passengersToDespawn.add(passenger);
                            }
                        } else if (
                                action == PassengerMovement.Action.ASCENDING
                                        || action == PassengerMovement.Action.DESCENDING
                        ) {
                            // Have the passenger set its current goal
                            passengerMovement.reachGoal();

                            // Leave the queue
                            passengerMovement.leaveQueue();
                        } else if (
                                action == PassengerMovement.Action.SECURITY_CHECKING
                                        || action == PassengerMovement.Action.TRANSACTING_TICKET
                                        || action == PassengerMovement.Action.USING_TICKET
                        ) {
                            // Record the time it took
                            switch (action) {
                                case SECURITY_CHECKING:
                                    passenger.getPassengerTime().passSecurity();

                                    break;
                                case USING_TICKET:
                                    if (passengerMovement.getDisposition() == PassengerMovement.Disposition.BOARDING) {
                                        passenger.getPassengerTime().tapInTurnstile();
                                    } else {
                                        passenger.getPassengerTime().tapOutTurnstile();
                                    }

                                    break;
                            }

                            // Have the passenger set its current goal
                            passengerMovement.reachGoal();

                            // Check if the passenger is allowed passage by the goal
                            // If it is, proceed to the next state
                            // If not, wait for an additional second
                            if (
                                    passengerMovement.isAllowedPass()
                                            && (
                                            action == PassengerMovement.Action.TRANSACTING_TICKET
                                                    || passengerMovement.isFirstStepPositionFree()
                                                    || (
                                                    passengerMovement.getCurrentTurnstileGate() != null
                                                            && passengerMovement.getCurrentTurnstileGate()
                                                            .getTurnstileMode()
                                                            == Turnstile.TurnstileMode.BIDIRECTIONAL
                                            )
                                    )
                            ) {
                                // Have this passenger's goal wrap up serving this passenger
                                passengerMovement.endServicingThisPassenger();

                                // Leave the queue
                                passengerMovement.leaveQueue();

                                // Move forward and go looking for the
                                // next one
                                passengerMovement.getRoutePlan().setNextAmenityClass();

                                // Reset the current goal of the passenger
                                // The passenger is set to step forward initially if the passenger is coming from a
                                // security entrance or a turnstile
                                passengerMovement.resetGoal(
                                        action == PassengerMovement.Action.SECURITY_CHECKING
                                                || action == PassengerMovement.Action.USING_TICKET
                                );

                                // Transition back into the "walking" state, and the "will queue" action
                                // Or the "exiting station" action, if this passenger is alighting and has left a
                                // turnstile
                                passengerMovement.setState(PassengerMovement.State.WALKING);
                                state = PassengerMovement.State.WALKING;

                                if (
                                        passengerMovement.getDisposition() == PassengerMovement.Disposition.ALIGHTING
                                                && action == PassengerMovement.Action.USING_TICKET
                                ) {
                                    passengerMovement.setAction(PassengerMovement.Action.EXITING_STATION);
                                    action = PassengerMovement.Action.EXITING_STATION;
                                } else {
                                    passengerMovement.setAction(PassengerMovement.Action.WILL_QUEUE);
                                    action = PassengerMovement.Action.WILL_QUEUE;
                                }
                            } else {
                                // Just stop and wait
                                passengerMovement.stop();
                            }
                        }

                        break;
                }

                break;
            case RIDING_TRAIN:
                // The passenger is riding the train
                switch (state) {
                    case IN_TRAIN:
                        break;
                }

                break;
        }
    }

    // Spawn a passenger from a gate in the given floor
    private static void spawnPassenger(
            Floor floor,
            Gate gate,
            PassengerTripInformation passengerTripInformation
    ) {
        // Generate the passenger
        Passenger passenger;

        // TODO: Use polymorphism
        if (gate instanceof StationGate) {
            StationGate stationGate = (StationGate) gate;

            passenger = stationGate.spawnPassenger(passengerTripInformation);
        } else {
            TrainDoor trainDoor = (TrainDoor) gate;

            passenger = trainDoor.spawnPassenger();
        }

        if (passenger.getPassengerMovement() != null) {
            // Add the newly created passenger to the list of passengers in the floor, station, and simulation
            floor.getStation().getPassengersInStation().add(passenger);
            floor.getPassengersInFloor().add(passenger);

            // Add the passenger's patch position to its current floor's patch set as well
            floor.getPassengerPatchSet().add(
                    passenger.getPassengerMovement().getCurrentPatch()
            );

            // The passenger has entered the station gate
            passenger.getPassengerTime().passEntrance();
        } else {
            // The passenger was spawned, but outside, so insert passenger into the station gate's queue
            com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station = gate.getAmenityBlocks().get(0).getPatch().getFloor().getStation().getStation();
            station.getPassengerBacklogs().get((StationGate) gate).add(passenger);
        }

//        if (passenger != null) {
//            // Add the newly created passenger to the list of passengers in the floor, station, and simulation
//            floor.getStation().getPassengersInStation().add(passenger);
//            floor.getPassengersInFloor().add(passenger);
//
//            // Add the passenger's patch position to its current floor's patch set as well
//            floor.getPassengerPatchSet().add(
//                    passenger.getPassengerMovement().getCurrentPatch()
//            );
//
//            // The passenger has entered the station gate
//            passenger.getPassengerTime().passEntrance();
//        } else {
//            // No passenger was spawned, so increment the backlog
//            if (gate instanceof StationGate) {
////                StationGate stationGate = ((StationGate) gate);
////
////                stationGate.incrementBacklogs();
////
////                // TODO: Offload to station gate itself
////                passengerTripInformation.getEntryStation().getPassengerBacklogs().get(stationGate).add(
////                        passengerTripInformation
////                );
//            }
//        }
    }

    // Spawn a passenger from a gate in the given floor
    private static void spawnPassenger(
            Floor floor,
            Gate gate
    ) {
        // Generate the passenger
        Passenger passenger = gate.spawnPassenger();

        if (gate instanceof StationGate) {
            if (passenger != null) {
                if (passenger.getPassengerMovement() != null) {
                    // Add the newly created passenger to the list of passengers in the floor, station, and simulation
                    floor.getStation().getPassengersInStation().add(passenger);
                    floor.getPassengersInFloor().add(passenger);

                    // Add the passenger's patch position to its current floor's patch set as well
                    floor.getPassengerPatchSet().add(
                            passenger.getPassengerMovement().getCurrentPatch()
                    );

                    // The passenger has entered the station gate
                    passenger.getPassengerTime().passEntrance();
                } else {
                    // The passenger was spawned, but outside, so insert passenger into the station gate's queue
                    com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station = gate.getAmenityBlocks().get(0).getPatch().getFloor().getStation().getStation();
                    station.getPassengerBacklogs().get((StationGate) gate).add(passenger);
                }
            }
        } else {
            if (passenger != null) {
                // Add the newly created passenger to the list of passengers in the floor, station, and simulation
                floor.getStation().getPassengersInStation().add(passenger);
                floor.getPassengersInFloor().add(passenger);

                // Add the passenger's patch position to its current floor's patch set as well
                floor.getPassengerPatchSet().add(
                        passenger.getPassengerMovement().getCurrentPatch()
                );
            }
        }
    }

    // Spawn passengers from the backlogs of the station gate
    public static void spawnPassengerFromStationGateBacklog(
            Floor floor,
            StationGate stationGate,
            boolean willDrawFromPassengerList
    ) {
        List<Passenger> passengersToSpawn = new ArrayList<>();

        final int additionalPassengersToSpawnPerTick = 2;

        // Spawn two more after the first one has already been spawned
        Passenger passenger = stationGate.spawnPassengerFromBacklogs(willDrawFromPassengerList, false);

        if (passenger != null) {
            passengersToSpawn.add(passenger);

            for (int spawnTimes = 0; spawnTimes < additionalPassengersToSpawnPerTick; spawnTimes++) {
                // Force the spawning of more passengers, to avoid long backlogs
                passenger = stationGate.spawnPassengerFromBacklogs(willDrawFromPassengerList, true);

                // No more to spawn, stop iterating
                if (passenger == null) {
                    break;
                } else {
                    passengersToSpawn.add(passenger);
                }
            }
        }

        for (Passenger passengerToSpawn : passengersToSpawn) {
            // Add the newly created passenger to the list of passengers in the floor, station, and simulation
            floor.getStation().getPassengersInStation().add(passengerToSpawn);
            floor.getPassengersInFloor().add(passengerToSpawn);

            // Add the passenger's patch position to its current floor's patch set as well
            floor.getPassengerPatchSet().add(
                    passengerToSpawn.getPassengerMovement().getCurrentPatch()
            );

            // The passenger has entered the station gate
            passengerToSpawn.getPassengerTime().passEntrance();
        }
    }

//    // Spawn a passenger from the backlogs of the station gate
//    // TODO: Offload to station gate itself
//    public static void spawnPassengerFromStationGateBacklog(
//            Floor floor,
//            StationGate stationGate,
//            List<PassengerTripInformation> backlogs
//    ) {
//        Passenger passenger = stationGate.spawnPassengerFromBacklogs(backlogs);
//
//        if (passenger != null) {
//            // Add the newly created passenger to the list of passengers in the floor, station, and simulation
//            floor.getStationLayout().getPassengersInStation().add(passenger);
//            floor.getPassengersInFloor().add(passenger);
//
//            // Add the passenger's patch position to its current floor's patch set as well
//            floor.getPassengerPatchSet().add(
//                    passenger.getPassengerMovement().getCurrentPatch()
//            );
//        }
//    }


    // Represent a floor update task
    public static class FloorUpdateTask implements Callable<Void> {
        private final Floor floorToUpdate;
        private final boolean willDrawFromPassengerList;
        private final List<Passenger> passengersToSwitchFloors;
        private final List<Passenger> passengersToDespawn;
        private final List<Passenger> passengersToBoard;
        private final List<Passenger> passengersToDispose;

        public FloorUpdateTask(
                Floor floorToUpdate,
                boolean willDrawFromPassengerList,
                List<Passenger> passengersToSwitchFloors,
                List<Passenger> passengersToDespawn,
                List<Passenger> passengersToBoard,
                List<Passenger> passengersToDispose
        ) {
            this.floorToUpdate = floorToUpdate;
            this.willDrawFromPassengerList = willDrawFromPassengerList;
            this.passengersToSwitchFloors = passengersToSwitchFloors;
            this.passengersToDespawn = passengersToDespawn;
            this.passengersToBoard = passengersToBoard;
            this.passengersToDispose = passengersToDispose;
        }

        @Override
        public Void call() throws Exception {
            try {
                Simulator.updateFloor(
                        floorToUpdate,
                        willDrawFromPassengerList,
                        passengersToSwitchFloors,
                        passengersToDespawn,
                        passengersToBoard,
                        passengersToDispose
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }

    // Describes the modes of operation in this program
    public enum OperationMode {
        BUILDING, // The user is building the station
        TESTING // The user is testing the station - no building features available when this is the mode (except edit)
    }

    // Describes the categories within the build operation
    public enum BuildCategory {
        ENTRANCES_AND_EXITS,
        STAIRS_AND_ELEVATORS,
        CONCOURSE_AMENITIES,
        PLATFORM_AMENITIES,
        MISCELLANEOUS
    }

    // Describes the subcategories within the current build category
    public enum BuildSubcategory {
        NONE,

        // Entrances and exits
        STATION_ENTRANCE_EXIT,
        SECURITY,

        // Stairs and elevators
        STAIRS,
        ESCALATOR,
        ELEVATOR,

        // Concourse amenities
        TICKET_BOOTH,
        TURNSTILE,

        // Platform amenities
        TRAIN_BOARDING_AREA,
        TRAIN_TRACK,

        // Miscellaneous
        OBSTACLE
    }

    // Describes the states within the build mode in the program
    public enum BuildState {
        DRAWING("Draw"), // The user is drawing more amenities
        EDITING_ONE("Edit one"), // The user is editing parameters of an existing amenity
        EDITING_ALL("Edit all"); // The user is editing parameters of all existing amenities

        private final String name;

        BuildState(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

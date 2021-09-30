package com.trainsimulation.model.simulator;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerTripInformation;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.trainsimulation.controller.Main;
import com.trainsimulation.controller.graphics.GraphicsController;
import com.trainsimulation.controller.screen.MainScreenController;
import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import com.trainsimulation.model.db.DatabaseInterface;
import com.trainsimulation.model.simulator.setup.EnvironmentSetup;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

// The simulator has total control over the aspects of the train simulation
public class Simulator {
    public static final String TRAIN_SYSTEM_TO_SIMULATE = "LRT-2";

    // Denotes whether the simulation has started yet or not
    private final AtomicBoolean running = new AtomicBoolean(false);

    // The lock which manages each simulation tick
    public static final Object tickLock = new Object();

    // Denotes whether the simulation is done or not
    private final AtomicBoolean done = new AtomicBoolean(false);

    // Contains the database interfacing methods of the simulator
    private final DatabaseInterface databaseInterface;

    // Contains all the train systems in the simulation
    private final List<TrainSystem> trainSystems;

    // Stores the current time in the simulation
    private SimulationTime time;

    // Stores the time when the simulation will end
    private SimulationTime endTime;

    // Used to manage when the simulation is paused/played
    private final Semaphore playSemaphore;

    // Use the number of CPUs as the basis for the number of thread pools
    public static final int NUM_CPUS = Runtime.getRuntime().availableProcessors();

    // Variables for logs
    // Logs completed passenger trips and their attributes (updated every time a passenger completes a trip)
    public static final List<String> PASSENGER_LOGS = Collections.synchronizedList(new ArrayList<>());

    // Logs station information (updated every minute)
    public static final List<String> STATION_LOGS = Collections.synchronizedList(new ArrayList<>());

    // Logs train information (updated every time a train departs a station)
    public static final List<String> TRAIN_LOGS = Collections.synchronizedList(new ArrayList<>());

//    public static final List<Passenger> PASSENGERS_SPAWN = Collections.synchronizedList(new ArrayList<>());

    public Simulator() throws Throwable {
        this.databaseInterface = new DatabaseInterface();
        this.trainSystems = new ArrayList<>();

        this.playSemaphore = new Semaphore(0);

        // Start the simulation thread, but in reality it would be activated much later
        this.start();
    }

    public AtomicBoolean getRunning() {
        return this.running;
    }

    public AtomicBoolean getDone() {
        return this.done;
    }

    public DatabaseInterface getDatabaseInterface() {
        return databaseInterface;
    }

    public SimulationTime getTime() {
        return time;
    }

    public void setTime(SimulationTime time) {
        this.time = time;
    }

    public List<TrainSystem> getTrainSystems() {
        return trainSystems;
    }

    public SimulationTime getEndTime() {
        return endTime;
    }

    public void setEndTime(SimulationTime endTime) {
        this.endTime = endTime;
    }

    public Semaphore getPlaySemaphore() {
        return playSemaphore;
    }

    // Set the simulation up with all its environments, agents, and attributes
    public void setup(SimulationTime startTime, SimulationTime endTime) {
        // Prepare the time at the start of the simulation
        this.time = startTime;
        this.endTime = endTime;

        // Prepare the train systems
        List<TrainSystem> trainSystems = EnvironmentSetup.setup(databaseInterface);

        // Then have the simulation take note of them
        // But not before clearing the current array of train systems, so the setup could be run indefinitely
        this.trainSystems.clear();
        this.trainSystems.addAll(trainSystems);
    }

    // Start the simulation and keep it running until the given ending time
    public void start() {
        new Thread(() -> {
            // Initialize a thread pool to run stations in parallel
            final ExecutorService stationExecutorService = Executors.newFixedThreadPool(Simulator.NUM_CPUS);

            // TODO: Implement simultaneous train systems

            // TODO: List all floors to update in parallel

            while (true) {
                try {
                    // Wait until the play button has been pressed
                    playSemaphore.acquire();

                    // Update the pertinent variables when ticking
                    boolean isTimeBeforeOrDuring = this.time.isTimeBeforeOrDuring(this.endTime);

                    // Keep looping until paused
                    while (this.running.get() && isTimeBeforeOrDuring) {
                        // Redraw the updated time
                        Main.mainScreenController.requestUpdateSimulationTime(this.time);

                        // Manage all passenger-related updates
                        updateTrainSystems(stationExecutorService);

                        // Update the view of the current station only
                        GraphicsController.requestDrawStationView(
                                MainScreenController.getActiveSimulationContext().getStationViewCanvases(),
                                MainScreenController.getActiveSimulationContext().getCurrentStation(),
                                MainScreenController.getActiveSimulationContext().getStationScaleDownFactor(),
                                false
                        );

                        // Increment (tick) the clock
                        this.time.tick();

                        // Pause this simulation thread for a brief amount of time so it could be followed at a pace
                        // conducive to visualization
                        isTimeBeforeOrDuring = this.time.isTimeBeforeOrDuring(this.endTime);
                        Thread.sleep(SimulationTime.SLEEP_TIME_MILLISECONDS.get());

                        synchronized (Simulator.tickLock) {
                            Simulator.tickLock.notifyAll();
                        }
                    }

                    if (!isTimeBeforeOrDuring) {
                        // Once the simulation time stops, stop all the threads
                        Main.simulator.getDone().set(true);

                        // Then tell the UI thread to disable all buttons
                        Main.mainScreenController.requestDisableButtons();

                        // Save all logs into CSV files
                        for (TrainSystem trainSystem : this.trainSystems) {
                            if (trainSystem.getTrainSystemInformation().getName().equals(TRAIN_SYSTEM_TO_SIMULATE)) {
                                savePassengerLogs(trainSystem);

                                saveStationLogs(trainSystem);

                                for (Train train : trainSystem.getActiveTrains()) {
                                    saveTrainLogs(train);
                                }

                                // Calculate breakdown of passengers still in system
                                int passengersOutsideStation = 0;
                                int passengersInStation = 0;
                                int passengersInTrain = 0;

                                for (Passenger passenger : trainSystem.getPassengers()) {
                                    if (passenger.getPassengerMovement() == null) {
                                        passengersOutsideStation++;
                                    } else if (
                                            passenger.getPassengerMovement().getDisposition()
                                                    == PassengerMovement.Disposition.RIDING_TRAIN
                                    ) {
                                        passengersInTrain++;
                                    } else {
                                        passengersInStation++;
                                    }
                                }

                                // Display total passengers
                                System.out.println("Total spawned: " + Passenger.passengerCount.get());
                                System.out.println("Total completed: " + Simulator.PASSENGER_LOGS.size());

                                System.out.println("Total still in system: " + trainSystem.getPassengers().size());

                                System.out.println("\tTotal waiting outside: " + passengersOutsideStation);
                                System.out.println("\tTotal inside a station: " + passengersInStation);
                                System.out.println("\tTotal riding a train: " + passengersInTrain);
                            }
                        }

                        break;
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    // Update passenger logs
    public static void logPassenger(Passenger passengerToDespawn) {
        // Do not log untracked passengers
//        if (passengerToDespawn.getPassengerTime().getTravelTime() > 0) {
        // Add the passenger's information into the list of logs
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

        String identifier = passengerToDespawn.getPassengerInformation().getIdentifier();
        String gender = passengerToDespawn.getGender().toString();

        String hasChosenFemaleOnlyCarriage = String.valueOf(
                passengerToDespawn.getPassengerInformation().hasChosenFemaleOnlyCarriage()
        );

        String actualEntryTime
                = passengerToDespawn.getPassengerTime().getTimeSpawned().format(dateTimeFormatter);
        String entryStation = passengerToDespawn.getPassengerMovement().getRoutePlan().getOriginStation().getName();
        String exitStation = passengerToDespawn.getPassengerMovement().getRoutePlan().getDestinationStation().getName();

        String travelTime = String.valueOf(passengerToDespawn.getPassengerTime().getTravelTime());

        String entranceTime = String.valueOf(passengerToDespawn.getPassengerTime().getPassedEntrance().getSeconds());
        String securityTime = String.valueOf(passengerToDespawn.getPassengerTime().getPassedSecurity().getSeconds());
        String tapInTime = String.valueOf(passengerToDespawn.getPassengerTime().getTappedInTurnstile().getSeconds());
        String enterTrainTime = String.valueOf(passengerToDespawn.getPassengerTime().getEnteredTrain().getSeconds());
        String exitTrainTime = String.valueOf(passengerToDespawn.getPassengerTime().getExitedTrain().getSeconds());
        String tapOutTime = String.valueOf(passengerToDespawn.getPassengerTime().getTappedOutTurnstile().getSeconds());
        String exitStationTime = String.valueOf(passengerToDespawn.getPassengerTime().getExitedStation().getSeconds());
        String totalTicksAlive = String.valueOf(passengerToDespawn.getPassengerTime().getTicksAlive().getSeconds());

        String logString
                = identifier + "," + gender + "," + hasChosenFemaleOnlyCarriage + "," + actualEntryTime + ","
                + entryStation + "," + exitStation + "," + travelTime + "," + entranceTime + "," + securityTime + ","
                + tapInTime + "," + enterTrainTime + "," + exitTrainTime + "," + tapOutTime + "," + exitStationTime
                + "," + totalTicksAlive;

        synchronized (Simulator.PASSENGER_LOGS) {
            Simulator.PASSENGER_LOGS.add(logString);
        }
//        }
    }

    // Update station logs
    private static synchronized void logStation(Station station, LocalTime currentTime) {
        // Add the passenger's information into the list of logs
        String stationName = station.getName();
        String timePeriod = currentTime.toString();

        int allPassengersInStation = station.getPassengersInStation().size();

        int northOrWestBoundPassengerCount = 0;
        int southOrEastBoundPassengerCount = 0;

        // Count passengers in station
        for (Passenger passenger : station.getPassengersInStation()) {
            if (
                    passenger.getPassengerMovement().getTravelDirection() == PassengerMovement.TravelDirection.NORTHBOUND
                            || passenger.getPassengerMovement().getTravelDirection() == PassengerMovement.TravelDirection.WESTBOUND
            ) {
                northOrWestBoundPassengerCount++;
            } else {
                southOrEastBoundPassengerCount++;
            }
        }

        String passengersInStation = String.valueOf(allPassengersInStation);

        String northOrWestBoundPassengers = String.valueOf(northOrWestBoundPassengerCount);
        String southOrEastBoundPassengers = String.valueOf(southOrEastBoundPassengerCount);

        // Count passengers queueing at the station's station gates
        int passengersQueueingToEnter = 0;

        synchronized (station.getFloors()) {
            for (Floor floor : station.getFloors()) {
                synchronized (floor.getStationGates()) {
                    for (StationGate stationGate : floor.getStationGates()) {
                        passengersQueueingToEnter += station.getStation().getPassengerBacklogs().get(stationGate).size();
                    }
                }
            }
        }

        String logString
                = stationName + "," + timePeriod + "," + passengersInStation + "," + northOrWestBoundPassengers
                + "," + southOrEastBoundPassengers + "," + passengersQueueingToEnter;

        synchronized (Simulator.STATION_LOGS) {
            Simulator.STATION_LOGS.add(logString);
        }
    }

    // Update train logs
    public static void logTrain(Train train, LocalTime currentTime) {
        String trainIdentifier = String.valueOf(train.getIdentifier());

        String stationRecentlyDeparted = train.getTrainMovement().getPreviousStoppedStation().getName();

        String trainDirection
                = PassengerMovement.TravelDirection.convertToTravelDirection(
                train.getTrainSystem(), train.getTrainMovement().getActualDirection()
        ).toString();

        String timeDeparted = currentTime.toString();
        String newLoadFactor = train.getTrainProperty().getLoadFactor();

        String logString
                = timeDeparted + "," + trainIdentifier + "," + stationRecentlyDeparted + "," + trainDirection + ","
                + newLoadFactor.substring(0, 5);

        synchronized (Simulator.TRAIN_LOGS) {
            Simulator.TRAIN_LOGS.add(logString);
        }
    }

//    public static AtomicBoolean isCrowdControlImplemented = new AtomicBoolean(true);
//    public static AtomicBoolean willBlock = new AtomicBoolean(false);

    // Update all train systems
    private void updateTrainSystems(ExecutorService stationExecutorService) throws InterruptedException {
        List<StationUpdateTask> stationUpdateTasks = new ArrayList<>();
        List<PassengerTickTask> passengerTickTasks = new ArrayList<>();

        // Update each train system
        for (TrainSystem trainSystem : this.trainSystems) {
            // Every second ending in "00", have the stations log
            if (this.time.getTime().getSecond() == 0) {
                for (com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station : trainSystem.getStations()) {
                    Simulator.logStation(station.getStationLayout(), this.time.getTime());
                }
            }

            // TODO: Consider all train systems
            if (trainSystem.getTrainSystemInformation().getName().equals(TRAIN_SYSTEM_TO_SIMULATE)) {
                // Remove trips that happen before the simulation start time
                trainSystem.removeTripsBeforeStartTime(this.time);

//                if (this.time.getTime().isAfter(LocalTime.of(7, 0)) && this.time.getTime().isBefore(LocalTime.of(8, 0))) {
//                    isCrowdControlImplemented.set(true);
//
//                    if (this.time.getTime().getSecond() == 0) {
//                        counter.incrementAndGet();
//                    }
//
//                    if (counter.get() % 5 == 0) {
//                        willBlock.set(!willBlock.get());
//                    }
//                } else {
//                    isCrowdControlImplemented.set(false);
//                }

                // Collect all stations in the train system
                List<com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station> stationsInTrainSystem = trainSystem.getStations();

                // Collect all passengers to be spawned at this tick
                HashMap<com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station, List<PassengerTripInformation>> passengersToSpawn
                        = trainSystem.getPassengersToSpawn(this.time);

                // Update each station in parallel
                for (com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station : stationsInTrainSystem) {
                    // Collect all passengers to be spawned in this station
                    List<PassengerTripInformation> passengersToSpawnInStation
                            = passengersToSpawn.get(station);

                    // If, in this station, there are passengers to be spawned, spawn them
                    stationUpdateTasks.add(new StationUpdateTask(station, passengersToSpawnInStation));
                }

                // Update each station
                stationExecutorService.invokeAll(stationUpdateTasks);

                // Clear the list of stations to update
                stationUpdateTasks.clear();

                // Tick all passengers
                synchronized (trainSystem.getPassengers()) {
                    for (Passenger passenger : trainSystem.getPassengers()) {
                        passengerTickTasks.add(new PassengerTickTask(passenger));
                    }
                }

                stationExecutorService.invokeAll(passengerTickTasks);

                // Clear the list of passengers to tick
                passengerTickTasks.clear();
            }
        }
    }

    // Save the logs into files
    private void savePassengerLogs(TrainSystem trainSystem) {
        final String workingDirectory = trainSystem.getTrainSystemInformation().getTrainSystemPath() + "\\logs\\";

        final String fileName = workingDirectory + "passenger_logs.csv";

        synchronized (Simulator.PASSENGER_LOGS) {
            Collections.sort(Simulator.PASSENGER_LOGS);
        }

        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(
                    "TRIP_IDENTIFIER," +
                            " GENDER," +
                            " RODE_FEMALE_ONLY_CARRIAGE," +
                            " ACTUAL_ENTRY_TIME," +
                            " ENTRY_STATION," +
                            " EXIT_STATION," +
                            " TRAVEL_TIME," +
                            " ENTRANCE_TIME," +
                            " SECURITY_TIME," +
                            " TAP_IN_TIME," +
                            " ENTER_TRAIN_TIME," +
                            " EXIT_TRAIN_TIME," +
                            " TAP_OUT_TIME," +
                            " EXIT_STATION_TIME," +
                            " TOTAL_SECONDS_ALIVE_TIME"
            );

            stringBuilder.append("\n");

            synchronized (Simulator.PASSENGER_LOGS) {
                for (String log : Simulator.PASSENGER_LOGS) {
                    stringBuilder.append(log);
                    stringBuilder.append("\n");
                }
            }

            printWriter.write(stringBuilder.toString());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void saveStationLogs(TrainSystem trainSystem) {
        final String workingDirectory
                = trainSystem.getTrainSystemInformation().getTrainSystemPath() + "\\logs\\";

        final String fileName = workingDirectory + "station_logs.csv";

        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(
                    "STATION_NAME," +
                            "TIME_PERIOD," +
                            "ALL_PASSENGERS," +
                            "NB_OR_WB_PASSENGERS," +
                            "SB_OR_EB_PASSENGERS," +
                            "QUEUEING_OUTSIDE"
            );

            stringBuilder.append("\n");

            synchronized (Simulator.STATION_LOGS) {
                for (String log : Simulator.STATION_LOGS) {
                    stringBuilder.append(log);
                    stringBuilder.append("\n");
                }
            }

            printWriter.write(stringBuilder.toString());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void saveTrainLogs(Train train) {
        final String workingDirectory
                = train.getTrainSystem().getTrainSystemInformation().getTrainSystemPath() + "\\logs\\";

        final String fileName = workingDirectory + "train_logs.csv";

        synchronized (Simulator.TRAIN_LOGS) {
            Collections.sort(Simulator.TRAIN_LOGS);
        }

        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(
                    "TIME_DEPARTED," +
                            "TRAIN_IDENTIFIER," +
                            "STATION_DEPARTED," +
                            "TRAIN_DIRECTION," +
                            "NEW_LOAD_FACTOR"
            );

            stringBuilder.append("\n");

            synchronized (Simulator.TRAIN_LOGS) {
                for (String log : Simulator.TRAIN_LOGS) {
                    stringBuilder.append(log);
                    stringBuilder.append("\n");
                }
            }

            printWriter.write(stringBuilder.toString());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    // Contains the necessary operations to update stations in parallel
    public static class StationUpdateTask implements Callable<Void> {
        private final com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station;
        private final List<PassengerTripInformation> passengersToSpawn;

        public StationUpdateTask(com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station, List<PassengerTripInformation> passengersToSpawn) {
            this.station = station;
            this.passengersToSpawn = passengersToSpawn;
        }

        @Override
        public Void call() throws Exception {
            try {
                com.crowdsimulation.model.simulator.Simulator.updatePassengersInStation(
                        this.station.getFloorExecutorService(),
                        station.getStationLayout(),
                        this.passengersToSpawn,
                        true
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }

    // Ticks passengers in parallel
    public static class PassengerTickTask implements Callable<Void> {
        private final Passenger passenger;

        public PassengerTickTask(Passenger passenger) {
            this.passenger = passenger;
        }

        @Override
        public Void call() throws Exception {
            try {
                passenger.getPassengerTime().tick();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }
}

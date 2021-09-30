package com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset;

import com.crowdsimulation.controller.controls.feature.main.MainScreenController;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.infrastructure.track.Track;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.property.StationProperty;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import com.trainsimulation.model.db.entity.StationsEntity;
import com.trainsimulation.model.simulator.SimulationTime;
import com.trainsimulation.model.utility.Schedule;
import com.trainsimulation.model.utility.StationCapacity;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Stations are structures in a train line where trains regularly stop to load and unload passengers
public class Station extends StationSet {
    // Sort by the sequence number of the station from the depot in ascending order
    public static Comparator<Station> StationByDepotSequenceAscending = new Comparator<Station>() {
        @Override
        public int compare(Station o1, Station o2) {
            return Integer.compare(o1.depotSequence, o2.depotSequence);
        }
    };

    // Sort by the sequence number of the station from the depot in descending order
    public static Comparator<Station> StationByDepotSequenceDescending = new Comparator<Station>() {
        @Override
        public int compare(Station o1, Station o2) {
            return Integer.compare(o2.depotSequence, o1.depotSequence);
        }
    };

    // Represents the name of the station
    private final String name;

    // Represents the sequence number of this station
    private final int sequence;

    // Represents the operating schedule of the station
    private final Schedule operatingHours;

    // Represents the passenger capacity specifics of the station
    private final StationCapacity capacity;

    // Represents each of the station's platforms in both directions
    private final Map<Track.Direction, Platform> platforms;

    // Represents this station's distance to the previous station, according to its sequence
    private final int distanceToPrevious;

    // Represents this station's chronological order from the depot, according to its sequence
    private final int depotSequence;

    // Represents a summarized version of this station
    private final StationProperty stationProperty;

    // Denotes the train system of this station
    private final TrainSystem trainSystem;

    // Denotes the file path where the files for this train system is found
    private final String stationPath;

    // Represents the physical layout of the station with all the station objects and floors
    private com.crowdsimulation.model.core.environment.station.Station station;

    // TODO: Offload to station gate itself
    // Denotes the backlogs of the station gates of this station
    private final HashMap<StationGate, List<Passenger>> passengerBacklogs;

    // TODO: Offload to station, and when validating
    public final HashMap<
            PassengerMovement.TravelDirection,
            HashMap<TrainDoor, HashMap<TrainDoor.TrainDoorCarriage, Integer>>
            > trainDoorCarriageMap;

    // TODO: Offload to platform
    public final HashMap<PassengerMovement.TravelDirection, Train> trains;

    // Initialize a thread pool to run floors in parallel
    private ExecutorService floorExecutorService;

    public Station(TrainSystem trainSystem, StationsEntity stationsEntity) {
        super(trainSystem);

        // TODO: Fix nulls
        this.name = stationsEntity.getName();
        this.sequence = stationsEntity.getSequence();
        this.operatingHours = new Schedule(stationsEntity.getSchedulesByOperatingHours());
        this.capacity = new StationCapacity(stationsEntity.getStationCapacitiesByStationCapacities());
        this.distanceToPrevious = stationsEntity.getDistanceToPrevious();
        this.depotSequence = stationsEntity.getDepotSequence();

        // Set the platforms up
        this.platforms = new EnumMap<>(Track.Direction.class);

        // TODO: Retrieve from the database instead of using the LRT-1 platform length for all stations
        this.platforms.put(Track.Direction.NORTHBOUND, new Platform(trainSystem, Platform.LRT_1_PLATFORM_LENGTH,
                Track.Direction.NORTHBOUND));

        this.platforms.put(Track.Direction.SOUTHBOUND, new Platform(trainSystem, Platform.LRT_1_PLATFORM_LENGTH,
                Track.Direction.SOUTHBOUND));

        this.stationProperty = new StationProperty(this);

        this.trainSystem = trainSystem;

        this.stationPath = trainSystem.getTrainSystemInformation().getTrainSystemPath() + "\\stations\\" + this.name;
        this.station = null;

        // TODO: Offload to station gate itself
        this.passengerBacklogs = new HashMap<>();

        // TODO: Offload to station, and when validating
        this.trainDoorCarriageMap = new HashMap<>();

        // TODO: Offload to platform
        this.trains = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public int getSequence() {
        return sequence;
    }

    public Schedule getOperatingHours() {
        return operatingHours;
    }

    public StationCapacity getCapacity() {
        return capacity;
    }

    public Map<Track.Direction, Platform> getPlatforms() {
        return platforms;
    }

    public int getDistanceToPrevious() {
        return distanceToPrevious;
    }

    public StationProperty getStationProperty() {
        return stationProperty;
    }

    public String getStationPath() {
        return stationPath;
    }

    public com.crowdsimulation.model.core.environment.station.Station getStationLayout() {
        return station;
    }

    public void setStationLayout(com.crowdsimulation.model.core.environment.station.Station station) {
        this.station = station;
        this.station.setStation(this);

        // TODO: Offload to station gate itself
        // TODO: Offload to station, and when validating
        for (Floor floor : this.station.getFloors()) {
            // Compile backlogs
            for (StationGate stationGate : floor.getStationGates()) {
                this.passengerBacklogs.put(stationGate, Collections.synchronizedList(new ArrayList<>()));
            }

            // Compile train doors
            // TODO: Offload to database
            assignCarriagesToTrainDoors(floor);
        }

        // TODO: Offload to platforms
        if (
                this.trainSystem.getTrainSystemInformation().getName().equals("LRT-1")
                        || this.trainSystem.getTrainSystemInformation().getName().equals("MRT-3")
        ) {
            this.trains.put(PassengerMovement.TravelDirection.NORTHBOUND, null);
            this.trains.put(PassengerMovement.TravelDirection.SOUTHBOUND, null);
        } else {
            this.trains.put(PassengerMovement.TravelDirection.WESTBOUND, null);
            this.trains.put(PassengerMovement.TravelDirection.EASTBOUND, null);
        }
    }

    private void assignCarriagesToTrainDoors(Floor floor) {
        final HashMap<TrainDoor.TrainDoorCarriage, Integer> doorCountPerCarriage = new HashMap<>();

        doorCountPerCarriage.put(TrainDoor.TrainDoorCarriage.LRT_1_FIRST_GENERATION, 4);
        doorCountPerCarriage.put(TrainDoor.TrainDoorCarriage.LRT_1_SECOND_GENERATION, 4);
        doorCountPerCarriage.put(TrainDoor.TrainDoorCarriage.LRT_1_THIRD_GENERATION, 4);

        doorCountPerCarriage.put(TrainDoor.TrainDoorCarriage.LRT_2_FIRST_GENERATION, 5);

        doorCountPerCarriage.put(TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION, 5);
        doorCountPerCarriage.put(TrainDoor.TrainDoorCarriage.MRT_3_SECOND_GENERATION, 5);

        final String trainSystemName = trainSystem.getTrainSystemInformation().getName();

        switch (trainSystemName) {
            case "LRT-1":
            case "MRT-3":
                HashMap<PassengerMovement.TravelDirection, List<TrainDoor>> trainDoorsPerTravelDirectionLRT1MRT3
                        = new HashMap<>();

                trainDoorsPerTravelDirectionLRT1MRT3.put(PassengerMovement.TravelDirection.NORTHBOUND, new ArrayList<>());
                trainDoorsPerTravelDirectionLRT1MRT3.put(PassengerMovement.TravelDirection.SOUTHBOUND, new ArrayList<>());

                this.trainDoorCarriageMap.put(PassengerMovement.TravelDirection.NORTHBOUND, new HashMap<>());
                this.trainDoorCarriageMap.put(PassengerMovement.TravelDirection.SOUTHBOUND, new HashMap<>());

                // Compile train doors and their directions
                synchronized (floor.getTrainDoors()) {
                    for (TrainDoor trainDoor : floor.getTrainDoors()) {
                        trainDoorsPerTravelDirectionLRT1MRT3.get(trainDoor.getPlatformDirection()).add(trainDoor);
                    }
                }

                // Per direction, train doors in ascending order of y-values
                for (
                        Map.Entry<PassengerMovement.TravelDirection, List<TrainDoor>> entry
                        : trainDoorsPerTravelDirectionLRT1MRT3.entrySet()
                ) {
                    PassengerMovement.TravelDirection travelDirection = entry.getKey();
                    List<TrainDoor> trainDoorsInThisDirection = entry.getValue();

                    trainDoorsInThisDirection.sort(new Comparator<TrainDoor>() {
                        @Override
                        public int compare(TrainDoor o1, TrainDoor o2) {
                            return
                                    o1.getAmenityBlocks().get(0).getPatch().getMatrixPosition().getColumn()
                                            - o2.getAmenityBlocks().get(0).getPatch().getMatrixPosition().getColumn();
                        }
                    });

                    int countLRT11G = 0;
                    int countLRT12G = 0;
                    int countLRT13G = 0;

                    int countMRT31G = 0;
                    int countMRT32G = 0;

                    // Iterate through this sorted list, and assign the respective carriage values
                    for (TrainDoor trainDoor : trainDoorsInThisDirection) {
                        HashMap<TrainDoor.TrainDoorCarriage, Integer> trainDoorCarriageIntegerHashMap
                                = new HashMap<>();

                        if (trainSystemName.equals("LRT-1")) {
                            if (
                                    trainDoor.getTrainDoorCarriagesSupported().contains(
                                            TrainDoor.TrainDoorCarriage.LRT_1_FIRST_GENERATION
                                    )
                            ) {
                                trainDoorCarriageIntegerHashMap.put(
                                        TrainDoor.TrainDoorCarriage.LRT_1_FIRST_GENERATION,
                                        countLRT11G / doorCountPerCarriage.get(
                                                TrainDoor.TrainDoorCarriage.LRT_1_FIRST_GENERATION
                                        )
                                );

                                countLRT11G++;
                            }

                            if (
                                    trainDoor.getTrainDoorCarriagesSupported().contains(
                                            TrainDoor.TrainDoorCarriage.LRT_1_SECOND_GENERATION
                                    )
                            ) {
                                trainDoorCarriageIntegerHashMap.put(
                                        TrainDoor.TrainDoorCarriage.LRT_1_SECOND_GENERATION,
                                        countLRT12G / doorCountPerCarriage.get(
                                                TrainDoor.TrainDoorCarriage.LRT_1_SECOND_GENERATION
                                        )
                                );

                                countLRT12G++;
                            }

                            if (
                                    trainDoor.getTrainDoorCarriagesSupported().contains(
                                            TrainDoor.TrainDoorCarriage.LRT_1_SECOND_GENERATION
                                    )
                            ) {
                                trainDoorCarriageIntegerHashMap.put(
                                        TrainDoor.TrainDoorCarriage.LRT_1_THIRD_GENERATION,
                                        countLRT13G / doorCountPerCarriage.get(
                                                TrainDoor.TrainDoorCarriage.LRT_1_THIRD_GENERATION
                                        )
                                );

                                countLRT13G++;
                            }
                        } else if (trainSystemName.equals("MRT-3")) {
                            if (
                                    trainDoor.getTrainDoorCarriagesSupported().contains(
                                            TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION
                                    )
                            ) {
                                trainDoorCarriageIntegerHashMap.put(
                                        TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION,
                                        countMRT31G / doorCountPerCarriage.get(
                                                TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION
                                        )
                                );

                                countMRT31G++;
                            }

                            if (
                                    trainDoor.getTrainDoorCarriagesSupported().contains(
                                            TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION
                                    )
                            ) {
                                trainDoorCarriageIntegerHashMap.put(
                                        TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION,
                                        countMRT32G / doorCountPerCarriage.get(
                                                TrainDoor.TrainDoorCarriage.MRT_3_FIRST_GENERATION
                                        )
                                );

                                countMRT32G++;
                            }
                        }

                        this.trainDoorCarriageMap.get(travelDirection).put(
                                trainDoor,
                                trainDoorCarriageIntegerHashMap
                        );
                    }
                }

                break;
            case "LRT-2":
                HashMap<PassengerMovement.TravelDirection, List<TrainDoor>> trainDoorsPerTravelDirectionLRT2
                        = new HashMap<>();

                trainDoorsPerTravelDirectionLRT2.put(PassengerMovement.TravelDirection.WESTBOUND, new ArrayList<>());
                trainDoorsPerTravelDirectionLRT2.put(PassengerMovement.TravelDirection.EASTBOUND, new ArrayList<>());

                this.trainDoorCarriageMap.put(PassengerMovement.TravelDirection.WESTBOUND, new HashMap<>());
                this.trainDoorCarriageMap.put(PassengerMovement.TravelDirection.EASTBOUND, new HashMap<>());

                // Compile train doors and their directions
                synchronized (floor.getTrainDoors()) {
                    for (TrainDoor trainDoor : floor.getTrainDoors()) {
                        trainDoorsPerTravelDirectionLRT2.get(trainDoor.getPlatformDirection()).add(trainDoor);
                    }
                }

                // Per direction, train doors in ascending order of y-values
                for (
                        Map.Entry<PassengerMovement.TravelDirection, List<TrainDoor>> entry
                        : trainDoorsPerTravelDirectionLRT2.entrySet()
                ) {
                    PassengerMovement.TravelDirection travelDirection = entry.getKey();
                    List<TrainDoor> trainDoorsInThisDirection = entry.getValue();

                    trainDoorsInThisDirection.sort(new Comparator<TrainDoor>() {
                        @Override
                        public int compare(TrainDoor o1, TrainDoor o2) {
                            return
                                    o1.getAmenityBlocks().get(0).getPatch().getMatrixPosition().getColumn()
                                            - o2.getAmenityBlocks().get(0).getPatch().getMatrixPosition().getColumn();
                        }
                    });

                    int countLRT21G = 0;

                    // Iterate through this sorted list, and assign the respective carriage values
                    for (TrainDoor trainDoor : trainDoorsInThisDirection) {
                        HashMap<TrainDoor.TrainDoorCarriage, Integer> trainDoorCarriageIntegerHashMap
                                = new HashMap<>();

                        if (
                                trainDoor.getTrainDoorCarriagesSupported().contains(
                                        TrainDoor.TrainDoorCarriage.LRT_2_FIRST_GENERATION
                                )
                        ) {
                            trainDoorCarriageIntegerHashMap.put(
                                    TrainDoor.TrainDoorCarriage.LRT_2_FIRST_GENERATION,
                                    countLRT21G / doorCountPerCarriage.get(
                                            TrainDoor.TrainDoorCarriage.LRT_2_FIRST_GENERATION
                                    )
                            );

                            countLRT21G++;
                        }

                        this.trainDoorCarriageMap.get(travelDirection).put(
                                trainDoor,
                                trainDoorCarriageIntegerHashMap
                        );
                    }
                }

                break;
        }
    }

    public int getDepotSequence() {
        return depotSequence;
    }

    @Override
    public TrainSystem getTrainSystem() {
        return trainSystem;
    }

    public ExecutorService getFloorExecutorService() {
        return floorExecutorService;
    }

    public void setFloorExecutorService(int threads) {
        this.floorExecutorService = Executors.newFixedThreadPool(threads);
    }

    public HashMap<StationGate, List<Passenger>> getPassengerBacklogs() {
        return passengerBacklogs;
    }

    public HashMap<PassengerMovement.TravelDirection, HashMap<TrainDoor, HashMap<TrainDoor.TrainDoorCarriage, Integer>>> getTrainDoorCarriageMap() {
        return trainDoorCarriageMap;
    }

    public HashMap<PassengerMovement.TravelDirection, Train> getTrains() {
        return trains;
    }

    // Checks the time, inflow rate, current number of passengers in concourse and platform areas, and operational
    // status; tells the gates to temporarily stop the flow of passengers
    public void activateCrowdControl(SimulationTime time, double inflowRate, StationCapacity capacity,
                                     boolean operational) {
        // TODO: Implement crowd control logic
    }

    // Designates the station as operational and passable depending on time
    public void openStation(SimulationTime time) {
        // TODO: Implement station opening logic
    }

    // Designates the station as operational and passable
    public void openStation() {
        // TODO: Implement station opening logic
    }

    // Designates the station as not operational due to the time with the value of passable depending on the parameter
    public void closeStation(SimulationTime time, boolean passable) {
        // TODO: Implement station closing logic
    }

    // Designates the station as not operational with the value of passable depending on the parameter
    public void closeStation(boolean passable) {
        // TODO: Implement station closing logic
    }

    // Have this station activate or deactivate the pertinent train doors so passengers could begin/finish riding the
    // given train
    public int toggleTrainDoors(Train train, PassengerMovement.TravelDirection travelDirection) {
        // Get the class of the train
        String trainClass = train.getTrainProperty().getCarriageClassName();

        // Convert it to the classes as understood by the passengers
        TrainDoor.TrainDoorCarriage trainDoorCarriage = TrainDoor.TrainDoorCarriage.getTrainDoorCarriage(trainClass);

        // In the train's current station, open the train doors matching the direction and the available carriages
        synchronized (this.station.getFloors()) {
            int waitingPassengers = 0;

            for (Floor floor : this.station.getFloors()) {
                synchronized (floor.getTrainDoors()) {
                    for (TrainDoor trainDoor : floor.getTrainDoors()) {
                        if (
                                trainDoor.getPlatformDirection().equals(travelDirection)
                                        && trainDoor.getTrainDoorCarriagesSupported().contains(trainDoorCarriage)
                        ) {
                            if (trainDoor.isOpen()) {
                                // Force the release of passengers before the door closes
                                trainDoor.releasePassenger(true);
                            }

                            trainDoor.toggleTrainDoor();

                            for (QueueObject queueObject : trainDoor.getQueueObjects().values()) {
                                List<Passenger> passengersQueueing = new ArrayList<>(queueObject.getPassengersQueueing());

                                waitingPassengers += passengersQueueing.size();
                            }
                        }
                    }
                }
            }

            return waitingPassengers;
        }
    }

    // Contains the necessary operations to load a station in parallel
    public static class StationLayoutLoadTask implements Callable<Void> {
        private final Station station;

        public StationLayoutLoadTask(Station station) {
            this.station = station;
        }

        @Override
        public Void call() throws Exception {
            try {
                // Prepare the path where the station layout may be found
                String stationLayoutPath
                        = this.station.getStationPath() + "\\run\\"
                        + this.station.getName()
                        + com.crowdsimulation.model.core.environment.station.Station.STATION_LAYOUT_FILE_EXTENSION;

                File stationLayoutFile = new File(stationLayoutPath);

                // Load the station layout from the file
                final com.crowdsimulation.model.core.environment.station.Station station
                        = MainScreenController.loadStation(stationLayoutFile);

                // Set the name of the station to the name of its parent
                station.setName(this.station.getName());

                // Then set its parent
                this.station.setStationLayout(station);

                // Set the thread pool of this station with the number of threads as the number of floors in the station
                this.station.setFloorExecutorService(station.getFloors().size());

                System.out.println("Successfully loaded " + stationLayoutFile.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }
}

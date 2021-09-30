package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate;

import com.crowdsimulation.controller.graphics.amenity.editor.TrainDoorEditor;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.footprint.GateFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.TrainDoorGraphic;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.PlatformFloorField;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.simulator.Simulator;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.TrainCarriage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrainDoor extends Gate implements Queueable {
    public static final long serialVersionUID = 3409867409524074606L;

    // Denotes the platform side served by this train door
    private PassengerMovement.TravelDirection platformDirection;

    // Denotes whether this train door is open and allows entry and exit of passengers
    private boolean open;

    // Denotes the types of carriages supported by this train door
    private final List<TrainDoorCarriage> trainDoorCarriagesSupported;

    // Denotes the orientation of this train door
    private Station.StationOrientation stationOrientation;

    // Denotes whether this train door only accepts females
    private boolean isFemaleOnly;

    // Factory for train door creation
    public static final TrainDoorFactory trainDoorFactory;

    // Denotes the queueing object associated with all goals like this
    private final HashMap<TrainDoorEntranceLocation, QueueObject> queueObjects;

    // Denotes the floor field states needed to access the floor fields of this train door
    private final PlatformFloorField.PlatformFloorFieldState leftTrainDoorFloorFieldState;
    private final PlatformFloorField.PlatformFloorFieldState rightTrainDoorFloorFieldState;

    // Handles how the train door is displayed
    private final TrainDoorGraphic trainDoorGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint trainDoorFootprint;

    // Denotes the editor of this amenity
    public static final TrainDoorEditor trainDoorEditor;

    static {
        trainDoorFactory = new TrainDoorFactory();

        // Initialize this amenity's footprints
        trainDoorFootprint = new AmenityFootprint();

        // Up view
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock01;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock02;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock03;

        AmenityFootprint.Rotation upView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.UP);

        upBlock00 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                0,
                TrainDoor.class,
                true,
                false,
                true
        );

        upBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                1,
                TrainDoor.class,
                false,
                true,
                false
        );

        upBlock02 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                2,
                TrainDoor.class,
                false,
                true,
                false
        );

        upBlock03 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                3,
                TrainDoor.class,
                true,
                false,
                false
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlock01);
        upView.getAmenityBlockTemplates().add(upBlock02);
        upView.getAmenityBlockTemplates().add(upBlock03);

        trainDoorFootprint.addRotation(upView);

        // Initialize the editor
        trainDoorEditor = new TrainDoorEditor();
    }

    protected TrainDoor(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            PassengerMovement.TravelDirection platformDirection,
            List<TrainDoorCarriage> trainDoorCarriagesSupported,
            Station.StationOrientation stationOrientation,
            boolean isFemaleOnly
    ) {
        super(amenityBlocks, enabled);

        this.platformDirection = platformDirection;
        this.open = false;
        this.trainDoorCarriagesSupported = new ArrayList<>();

        setTrainDoorCarriagesSupported(trainDoorCarriagesSupported);

        this.stationOrientation = stationOrientation;

        this.isFemaleOnly = isFemaleOnly;

        this.queueObjects = new HashMap<>();

        // Initialize this elevator portal's floor field state
        // A null in the floor field state means that it may accept any direction
        this.leftTrainDoorFloorFieldState = new PlatformFloorField.PlatformFloorFieldState(
                PassengerMovement.Disposition.BOARDING,
                PassengerMovement.State.IN_QUEUE,
                this,
                TrainDoorEntranceLocation.LEFT
        );

        this.rightTrainDoorFloorFieldState = new PlatformFloorField.PlatformFloorFieldState(
                PassengerMovement.Disposition.BOARDING,
                PassengerMovement.State.IN_QUEUE,
                this,
                TrainDoorEntranceLocation.RIGHT
        );

        if (stationOrientation == Station.StationOrientation.SIDE_PLATFORM) {
            if (
                    platformDirection == PassengerMovement.TravelDirection.SOUTHBOUND
                            || platformDirection == PassengerMovement.TravelDirection.EASTBOUND
            ) {
                this.queueObjects.put(
                        TrainDoorEntranceLocation.LEFT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(0).getPatch()
                        )
                );

                this.queueObjects.put(
                        TrainDoorEntranceLocation.RIGHT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(1).getPatch()
                        )
                );
            } else {
                this.queueObjects.put(
                        TrainDoorEntranceLocation.RIGHT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(0).getPatch()
                        )
                );

                this.queueObjects.put(
                        TrainDoorEntranceLocation.LEFT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(1).getPatch()
                        )
                );
            }
        } else {
            if (
                    platformDirection == PassengerMovement.TravelDirection.SOUTHBOUND
                            || platformDirection == PassengerMovement.TravelDirection.EASTBOUND
            ) {
                this.queueObjects.put(
                        TrainDoorEntranceLocation.RIGHT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(0).getPatch()
                        )
                );

                this.queueObjects.put(
                        TrainDoorEntranceLocation.LEFT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(1).getPatch()
                        )
                );
            } else {
                this.queueObjects.put(
                        TrainDoorEntranceLocation.LEFT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(0).getPatch()
                        )
                );

                this.queueObjects.put(
                        TrainDoorEntranceLocation.RIGHT,
                        new QueueObject(
                                this,
                                this.getAttractors().get(1).getPatch()
                        )
                );
            }
        }

        // Add a blank floor field
        PlatformFloorField leftPlatformFloorField = PlatformFloorField.platformFloorFieldFactory.create(this);
        PlatformFloorField rightPlatformFloorField = PlatformFloorField.platformFloorFieldFactory.create(this);

        // Using the floor field state defined earlier, create the floor field
        this.queueObjects.get(TrainDoorEntranceLocation.LEFT).getFloorFields().put(
                this.leftTrainDoorFloorFieldState, leftPlatformFloorField
        );

        this.queueObjects.get(TrainDoorEntranceLocation.RIGHT).getFloorFields().put(
                this.rightTrainDoorFloorFieldState, rightPlatformFloorField
        );

        this.trainDoorGraphic = new TrainDoorGraphic(this);
    }

    public PassengerMovement.TravelDirection getPlatformDirection() {
        return platformDirection;
    }

    public void setPlatformDirection(PassengerMovement.TravelDirection platformDirection) {
        this.platformDirection = platformDirection;
    }

    public boolean isOpen() {
        return open;
    }

    public void toggleTrainDoor() {
        this.open = !this.open;
    }

    public List<TrainDoorCarriage> getTrainDoorCarriagesSupported() {
        return trainDoorCarriagesSupported;
    }

    public void setTrainDoorCarriagesSupported(List<TrainDoorCarriage> trainDoorCarriagesSupported) {
        this.trainDoorCarriagesSupported.clear();
        this.trainDoorCarriagesSupported.addAll(trainDoorCarriagesSupported);
    }

    public Station.StationOrientation getTrainDoorOrientation() {
        return stationOrientation;
    }

    public void setTrainDoorOrientation(Station.StationOrientation stationOrientation) {
        this.stationOrientation = stationOrientation;
    }

    public boolean isFemaleOnly() {
        return isFemaleOnly;
    }

    public void setFemaleOnly(boolean femaleOnly) {
        isFemaleOnly = femaleOnly;
    }

    @Override
    // Get whichever queue object has the shorter queue
    public QueueObject getQueueObject() {
        return null;
    }

    @Override
    // Check whether this queueable is free to service a passenger
    public boolean isFree(QueueObject queueObject) {
        return queueObject.isFree();
    }

    public HashMap<TrainDoorEntranceLocation, QueueObject> getQueueObjects() {
        return queueObjects;
    }

    public PlatformFloorField.PlatformFloorFieldState getLeftTrainDoorFloorFieldState() {
        return leftTrainDoorFloorFieldState;
    }

    public PlatformFloorField.PlatformFloorFieldState getRightTrainDoorFloorFieldState() {
        return rightTrainDoorFloorFieldState;
    }

    public static boolean isTrainDoor(Amenity amenity) {
        return amenity instanceof TrainDoor;
    }

    public static TrainDoor asTrainDoor(Amenity amenity) {
        if (isTrainDoor(amenity)) {
            return (TrainDoor) amenity;
        } else {
            return null;
        }
    }

    public QueueObject getQueueObjectFromTrainDoorEntranceLocation(
            TrainDoorEntranceLocation trainDoorEntranceLocation
    ) {
        return this.queueObjects.get(trainDoorEntranceLocation);
    }

    public TrainDoorEntranceLocation getTrainDoorEntranceLocationFromAttractor(AmenityBlock attractor) {
        if (this.stationOrientation == Station.StationOrientation.SIDE_PLATFORM) {
            if (
                    this.platformDirection == PassengerMovement.TravelDirection.SOUTHBOUND
                            || this.platformDirection == PassengerMovement.TravelDirection.EASTBOUND
            ) {
                if (this.getAttractors().indexOf(attractor) == 0) {
                    return TrainDoorEntranceLocation.LEFT;
                } else {
                    return TrainDoorEntranceLocation.RIGHT;
                }
            } else {
                if (this.getAttractors().indexOf(attractor) == 0) {
                    return TrainDoorEntranceLocation.RIGHT;
                } else {
                    return TrainDoorEntranceLocation.LEFT;
                }
            }
        } else {
            if (
                    this.platformDirection == PassengerMovement.TravelDirection.SOUTHBOUND
                            || this.platformDirection == PassengerMovement.TravelDirection.EASTBOUND
            ) {
                if (this.getAttractors().indexOf(attractor) == 0) {
                    return TrainDoorEntranceLocation.RIGHT;
                } else {
                    return TrainDoorEntranceLocation.LEFT;
                }
            } else {
                if (this.getAttractors().indexOf(attractor) == 0) {
                    return TrainDoorEntranceLocation.LEFT;
                } else {
                    return TrainDoorEntranceLocation.RIGHT;
                }
            }
        }
    }

    @Override
    public List<QueueingFloorField.FloorFieldState> retrieveFloorFieldStates() {
        List<QueueingFloorField.FloorFieldState> floorFieldStates = new ArrayList<>();

        floorFieldStates.add(this.leftTrainDoorFloorFieldState);
        floorFieldStates.add(this.rightTrainDoorFloorFieldState);

        return floorFieldStates;
    }

    @Override
    public QueueingFloorField retrieveFloorField(
            QueueObject queueObject,
            QueueingFloorField.FloorFieldState floorFieldState
    ) {
        PlatformFloorField.PlatformFloorFieldState platformFloorFieldState
                = ((PlatformFloorField.PlatformFloorFieldState) floorFieldState);

        // Get the corresponding queueing floor field
        return queueObject.getFloorFields().get(
                platformFloorFieldState
        );
    }

    @Override
    // Denotes whether the floor field for this elevator portal is complete
    public boolean isFloorFieldsComplete() {
        PlatformFloorField leftPlatformFloorField = (PlatformFloorField) retrieveFloorField(
                this.getQueueObjects().get(TrainDoorEntranceLocation.LEFT),
                this.leftTrainDoorFloorFieldState
        );

        PlatformFloorField rightPlatformFloorField = (PlatformFloorField) retrieveFloorField(
                this.getQueueObjects().get(TrainDoorEntranceLocation.RIGHT),
                this.rightTrainDoorFloorFieldState
        );

        // The floor field of this queueable is complete when there are floor field values present with an apex patch
        // that is equal to the number of attractors in this queueable target
        return leftPlatformFloorField.getApices().size() == 1 && !leftPlatformFloorField.getAssociatedPatches().isEmpty()
                && rightPlatformFloorField.getApices().size() == 1 && !rightPlatformFloorField.getAssociatedPatches().isEmpty();
    }

    @Override
    // Clear all floor fields of the given floor field state in this train door waiting area
    public void deleteFloorField(
            QueueingFloorField.FloorFieldState floorFieldState
    ) {
        PlatformFloorField.PlatformFloorFieldState platformFloorFieldState
                = ((PlatformFloorField.PlatformFloorFieldState) floorFieldState);

        // Get the location of the train door entrance from the given platform floor field state
        TrainDoorEntranceLocation trainDoorEntranceLocation
                = platformFloorFieldState.getTrainDoorEntranceLocation();

        // Then get the appropriate queue object corresponding to that location
        QueueObject updatedQueueObject = this.getQueueObjects().get(trainDoorEntranceLocation);

        PlatformFloorField platformFloorField
                = (PlatformFloorField) platformFloorFieldState.getTarget().retrieveFloorField(
                updatedQueueObject,
                platformFloorFieldState
        );

        PlatformFloorField.clearFloorField(
                platformFloorField,
                floorFieldState
        );
    }

    @Override
    public void deleteAllFloorFields() {
        // Sweep through each and every floor field and delete them
        List<QueueingFloorField.FloorFieldState> floorFieldStates = retrieveFloorFieldStates();

        for (QueueingFloorField.FloorFieldState queueingFloorFieldState : floorFieldStates) {
            deleteFloorField(queueingFloorFieldState);
        }
    }

    @Override
    public String toString() {
        return "Train boarding area" + ((this.enabled) ? "" : " (disabled)");
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.trainDoorGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.trainDoorGraphic.getGraphicLocation();
    }

    @Override
    public Passenger spawnPassenger() {
        // Check if all attractors and spawners in this amenity have no passengers
        for (AmenityBlock attractor : this.getAttractors()) {
            if (!attractor.getPatch().getPassengers().isEmpty()) {
                return null;
            }
        }

        for (GateBlock spawner : this.getSpawners()) {
            if (!spawner.getPatch().getPassengers().isEmpty()) {
                return null;
            }
        }

        // Randomly choose between the spawner locations in the train door
        int spawnerCount = this.getSpawners().size();
        int randomSpawnerIndex = Simulator.RANDOM_NUMBER_GENERATOR.nextInt(spawnerCount);

        GateBlock spawner = this.getSpawners().get(randomSpawnerIndex);

        // If that spawner is free from passengers, generate one
        if (spawner.getPatch().getPassengers().isEmpty()) {
            return Passenger.passengerFactory.create(spawner.getPatch(), null, false);
        } else {
            // Else, do nothing, so return null
            return null;
        }
    }

    // TODO: Get station from station layout, not from passenger
    // Get the train carriage serviced by this train door, if any
    public TrainCarriage getTrainCarriage(
            com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station
    ) {
        Train trainToBoard = station.getTrains().get(this.platformDirection);

        if (trainToBoard != null) {
            // Get the class of the train
            TrainDoorCarriage trainDoorCarriageClass
                    = TrainDoorCarriage.getTrainDoorCarriage(trainToBoard.getTrainProperty().getCarriageClassName());

            // Then get the carriage where the passenger is supposed to board
            TrainCarriage trainCarriageToBoard;

            try {
                int trainCarriageIndex
                        = station.getTrainDoorCarriageMap().get(this.platformDirection).get(this).get(
                        trainDoorCarriageClass
                );

                // TODO: Just go ahead and fix the file instead of hacking the code to make it work
                trainCarriageToBoard = trainToBoard.getTrainCarriages().get(trainCarriageIndex);
            } catch (IndexOutOfBoundsException | NullPointerException ex) {
                // Go to the last available carriage in the train
                trainCarriageToBoard = trainToBoard.getTail();
            }

            return trainCarriageToBoard;
        } else {
            return null;
        }
    }

    // Transfer a passenger from this station to the train carriage
    public void boardPassenger(Passenger passenger) {
        // Record the time it took
        passenger.getPassengerTime().enterTrain();

        // Get the train carriage to board
        TrainCarriage trainCarriageToBoard = this.getTrainCarriage(
                passenger.getPassengerMovement().getRoutePlan().getOriginStation()
        );

        // Add this passenger to the list of passengers on that train carriage
        trainCarriageToBoard.addPassenger(passenger);

        // Have the passenger board the train
        passenger.getPassengerMovement().boardTrain(this);

        // Update the route variables of the passenger to reflect the fact that the passenger is now on the train
        passenger.getPassengerMovement().getRoutePlan().setNextRoutePlan(
                passenger.getPassengerMovement().getDisposition(),
                passenger.getPassengerInformation().getTicketType()
                        == TicketBooth.TicketType.STORED_VALUE
        );
    }

    // Transfer a passenger from a train carriage to this station
    public void releasePassenger(boolean force) {
        // Get the train carriage connected to this train door
        com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station
                = this.getAmenityBlocks().get(0).getPatch().getFloor().getStation().getStation();

        TrainCarriage trainCarriage = this.getTrainCarriage(station);

        // If there are less than 5 seconds in the train's waiting time, release as many passengers as possible
        // If there is 1 second left in the train's waiting time, spawn all that is remaining
        // Else, spawn normally
//        final int spawnAllTime = 1;

//        System.out.println(trainCarriage.getParentTrain().getTrainMovement().getWaitedTime());

//        if (
//                trainCarriage.getParentTrain().getTrainMovement().getWaitedTime()
//                        >= trainCarriage.getParentTrain().getTrainMovement().getWaitingTime() * 0.9
//        ) {
//            // Get the size of the remaining passengers that have not been spawned
//            int remainingAlightingPassengers = trainCarriage.passengersToAlightLeft();
//
//            // Divide it into the number of spawners
//            int shareOfAlightingPassengersToSpawn = remainingAlightingPassengers / this.getSpawners().size();
////            int remainderAlightingPassengersToSpawn = remainingAlightingPassengers % this.getSpawners().size();
//
//            // Release passengers from each spawn patch
//            for (GateBlock spawner : this.getSpawners()) {
//                // If there are no more passengers to alight, terminate
//                if (!trainCarriage.hasPassengersToAlight()) {
//                    break;
//                }
//
//                for (int spawned = 0; spawned < shareOfAlightingPassengersToSpawn; spawned++) {
//                    // Take one passenger from the train and then position it onto the spawner patch
//                    Passenger passengerAlighted = trainCarriage.removePassenger();
//
//                    // Set the new route plan of the passenger
//                    passengerAlighted.getPassengerMovement().getRoutePlan().setNextRoutePlan(
//                            PassengerMovement.Disposition.ALIGHTING,
//                            passengerAlighted.getTicketType() == TicketBooth.TicketType.STORED_VALUE
//                    );
//
//                    // Have the passenger alight from the train
//                    passengerAlighted.getPassengerMovement().alightTrain(spawner);
//
//                    // Record the time it took
//                    passengerAlighted.getPassengerTime().exitTrain();
//                }
//            }
//
//            // Then have a random spawner spawn the rest
//            GateBlock spawner
//                    = this.getSpawners().get(Simulator.RANDOM_NUMBER_GENERATOR.nextInt(this.getSpawners().size()));
//
//            // Have the remaining passengers alight from the train
//            while (trainCarriage.hasPassengersToAlight()) {
//                // Take one passenger from the train and then position it onto the spawner patch
//                Passenger passengerAlighted = trainCarriage.removePassenger();
//
//                // Set the new route plan of the passenger
//                passengerAlighted.getPassengerMovement().getRoutePlan().setNextRoutePlan(
//                        PassengerMovement.Disposition.ALIGHTING,
//                        passengerAlighted.getTicketType() == TicketBooth.TicketType.STORED_VALUE
//                );
//
//                // Have the passenger alight from the train
//                passengerAlighted.getPassengerMovement().alightTrain(spawner);
//
//                // Record the time it took
//                passengerAlighted.getPassengerTime().exitTrain();
//            }
//        } else {
        if (force) {
            // Then have a random spawner spawn the rest
            GateBlock spawner
                    = this.getSpawners().get(Simulator.RANDOM_NUMBER_GENERATOR.nextInt(this.getSpawners().size()));

            // Have the remaining passengers alight from the train
            while (trainCarriage.hasPassengersToAlight()) {
                // Take one passenger from the train and then position it onto the spawner patch
                Passenger passengerAlighted = trainCarriage.removePassenger();

                // Set the new route plan of the passenger
                passengerAlighted.getPassengerMovement().getRoutePlan().setNextRoutePlan(
                        PassengerMovement.Disposition.ALIGHTING,
                        passengerAlighted.getTicketType() == TicketBooth.TicketType.STORED_VALUE
                );

                // Have the passenger alight from the train
                passengerAlighted.getPassengerMovement().alightTrain(spawner);

                // Record the time it took
                passengerAlighted.getPassengerTime().exitTrain();
            }
        } else {
            // Release passengers from each spawn patch
            for (GateBlock spawner : this.getSpawners()) {
                // If there are no more passengers to alight, terminate
                if (!trainCarriage.hasPassengersToAlight()) {
                    break;
                }

                // Take one passenger from the train and then position it onto the spawner patch
                Passenger passengerAlighted = trainCarriage.removePassenger();

                // Set the new route plan of the passenger
                passengerAlighted.getPassengerMovement().getRoutePlan().setNextRoutePlan(
                        PassengerMovement.Disposition.ALIGHTING,
                        passengerAlighted.getTicketType() == TicketBooth.TicketType.STORED_VALUE
                );

                // Have the passenger alight from the train
                passengerAlighted.getPassengerMovement().alightTrain(spawner);

                // Record the time it took
                passengerAlighted.getPassengerTime().exitTrain();
            }
        }
//        }
    }

    // Train door block
    public static class TrainDoorBlock extends GateBlock {
        public static TrainDoorBlockFactory trainDoorBlockFactory;

        static {
            trainDoorBlockFactory = new TrainDoorBlockFactory();
        }

        private TrainDoorBlock(Patch patch, boolean attractor, boolean spawner, boolean hasGraphic) {
            super(patch, attractor, spawner, hasGraphic);
        }

        // Train door block factory
        public static class TrainDoorBlockFactory extends GateBlockFactory {
            @Override
            public TrainDoorBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new TrainDoorBlock(
                        patch,
                        attractor,
                        false,
                        hasGraphic
                );
            }

            @Override
            public TrainDoorBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean spawner,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new TrainDoorBlock(
                        patch,
                        attractor,
                        spawner,
                        hasGraphic
                );
            }
        }
    }

    // Train door factory
    public static class TrainDoorFactory extends GateFactory {
        public TrainDoor create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                PassengerMovement.TravelDirection travelDirection,
                List<TrainDoorCarriage> trainDoorCarriagesSupported,
                Station.StationOrientation stationOrientation,
                boolean isFemaleOnly
        ) {
            return new TrainDoor(
                    amenityBlocks,
                    enabled,
                    travelDirection,
                    trainDoorCarriagesSupported,
                    stationOrientation,
                    isFemaleOnly
            );
        }
    }

    // The carriages this train door supports
    public enum TrainDoorCarriage {
        LRT_1_FIRST_GENERATION("LRT-1 1G"),
        LRT_1_SECOND_GENERATION("LRT-1 2G"),
        LRT_1_THIRD_GENERATION("LRT-1 3G"),
        LRT_2_FIRST_GENERATION("LRT-2 1G"),
        MRT_3_FIRST_GENERATION("MRT-3 1G"),
        MRT_3_SECOND_GENERATION("MRT-3 2G");

        private final String name;

        TrainDoorCarriage(String name) {
            this.name = name;
        }

        public static TrainDoorCarriage getTrainDoorCarriage(String trainClass) {
            switch (trainClass) {
                case "LRTA Class 1000":
                    return LRT_1_FIRST_GENERATION;
                case "LRTA Class 1100":
                    return LRT_1_SECOND_GENERATION;
                case "LRTA Class 1200":
                    return LRT_1_THIRD_GENERATION;
                case "LRTA Class 2000":
                    return LRT_2_FIRST_GENERATION;
                case "MRTC Class 3000":
                    return MRT_3_FIRST_GENERATION;
                case "MRTC Class 3100":
                    return MRT_3_SECOND_GENERATION;
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    // Denotes the location of a train door entrance
    public enum TrainDoorEntranceLocation {
        LEFT("Left entrance"),
        RIGHT("Right entrance");

        private final String name;

        TrainDoorEntranceLocation(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}

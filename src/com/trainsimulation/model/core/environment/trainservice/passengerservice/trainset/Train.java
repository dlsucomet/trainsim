package com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset;

import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.trainsimulation.controller.Main;
import com.trainsimulation.controller.screen.MainScreenController;
import com.trainsimulation.model.core.agent.Agent;
import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.infrastructure.track.Segment;
import com.trainsimulation.model.core.environment.infrastructure.track.Track;
import com.trainsimulation.model.core.environment.trainservice.maintenance.Depot;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.property.TrainProperty;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import com.trainsimulation.model.db.DatabaseQueries;
import com.trainsimulation.model.db.entity.TrainCarriagesEntity;
import com.trainsimulation.model.db.entity.TrainsEntity;
import com.trainsimulation.model.simulator.Simulator;
import com.trainsimulation.model.utility.TrainMovement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// Vehicles composed of multiple carriages used to transport passengers
public class Train extends TrainSet implements Agent {
    // Denotes the gap between each carriages (in meters)
    private static final double CARRIAGE_GAP = 1.0;

    // Contains the carriages this train is composed of
    private final LinkedList<TrainCarriage> trainCarriages;

    // Contains the variables relevant to the train's movement
    private TrainMovement trainMovement;

    // Contains a summarized representation of this train (for table tracking purposes)
    private TrainProperty trainProperty;

    public Train(TrainSystem trainSystem, TrainsEntity trainsEntity) {
        super(trainSystem, trainsEntity.getId());

        this.trainCarriages = new LinkedList<>();

        resetTrain();
    }

    private void resetTrain() {
        // Remove all train carriages, if any
        this.trainCarriages.clear();

        // Get the carriages associated with this train
        List<TrainCarriagesEntity> trainCarriagesEntities = DatabaseQueries.getTrainCarriages(
                Main.simulator.getDatabaseInterface(), this);

        // Add the train carriages
        Integer maxVelocity = null;
        Double deceleration = null;

        // There should always be carriages associated with this train
        assert trainCarriagesEntities.size() != 0 : "No train carriages associated with this train";

        for (TrainCarriagesEntity trainCarriagesEntity : trainCarriagesEntities) {
            int quantity = trainCarriagesEntity.getQuantity();

            for (int count = 1; count <= quantity; count++) {
                TrainCarriage trainCarriage = new TrainCarriage(
                        this.getTrainSystem(),
                        trainCarriagesEntity,
                        this,
                        count == 1
                );

                // Add the maximum velocity and waiting times
                if (maxVelocity == null && deceleration == null) {
                    maxVelocity = (int) trainCarriagesEntity.getCarriageClassesByCarriageClass().getMaxVelocity();
                    deceleration = (double) trainCarriagesEntity.getCarriageClassesByCarriageClass().getDeceleration();
                }

                this.trainCarriages.add(trainCarriage);
            }
        }

        this.trainMovement = new TrainMovement(maxVelocity, deceleration, this);
        this.trainProperty = new TrainProperty(this);
    }

    public LinkedList<TrainCarriage> getTrainCarriages() {
        return trainCarriages;
    }

    public TrainCarriage getHead() {
        return trainCarriages.getFirst();
    }

    public TrainCarriage getTail() {
        return trainCarriages.getLast();
    }

    public TrainMovement getTrainMovement() {
        return trainMovement;
    }

    public void setTrainMovement(TrainMovement trainMovement) {
        this.trainMovement = trainMovement;
    }

    public TrainProperty getTrainProperty() {
        return trainProperty;
    }

    public void setTrainProperty(TrainProperty trainProperty) {
        this.trainProperty = trainProperty;
    }

    @Override
    public void run() {
        try {
            // Originate the train from the depot belonging to the train system where this train belongs to
            Depot depot = this.getTrainSystem().getDepot();
            Segment outgoingDepotSegment = depot.getPlatforms().get(Track.Direction.NORTHBOUND)
                    .getPlatformHub().getPlatformSegment();

            this.trainMovement.setTrainAtSegment(outgoingDepotSegment, CARRIAGE_GAP);

            // Take note of the actions made
            TrainMovement.TrainAction trainAction;

            // Get the segment that leads out of the depot
            Segment outsideDepotSegment = outgoingDepotSegment.getTo().getOutSegment(Track.Direction.DEPOT_OUT);

            // Then get the segment after that (the first segment of the main line after the segment that leads out of
            // the depot)
            // NOTE: This is only possible when the junction that the segment that leads out of the depot leads to only
            // has one outsegment
            Segment firstMainlineSegment = outsideDepotSegment.getTo().getOutSegments().values().iterator().next();

            // Get the very first direction of the train
            Track.Direction firstDirection = firstMainlineSegment.getDirection();

            // Set the direction of this train based on the first direction
            this.getTrainMovement().setDesiredDirection(firstDirection);
            this.getTrainMovement().setActualDirection(firstDirection);

            // Prepare the directions of the train, based on its assigned station stops and the first direction
            this.getTrainMovement().generateDirectionsToNextStation(firstDirection);

            // Exit the depot, then keep moving until the simulation is done
            // TODO: Trains should also go home when told to, or when it's past operating hours
            while (!Main.simulator.getDone().get()) {
                // Move until it is commanded otherwise (when it stops for a station or to avoid colliding with another
                // train, or when it stops because of a signal), or until the simulation is done
                do {
                    synchronized (Simulator.tickLock) {
                        Simulator.tickLock.wait();
                    }

                    // Take note of the action command
                    trainAction = this.trainMovement.move();

//                    // TODO: Move to own loop
//                    // Redraw the station view
//                    GraphicsController.requestDrawStationView(
//                            MainScreenController.getActiveSimulationContext().getStationViewCanvases(),
//                            MainScreenController.getActiveSimulationContext().getCurrentStation(),
//                            MainScreenController.getActiveSimulationContext().getStationScaleDownFactor(),
//                            false
//                    );

                    // Update the summary
                    this.trainProperty.updateTrainProperty(this.identifier, this.trainMovement, this.trainCarriages
                            .getFirst());

                    // Pause the thread
//                    Thread.sleep(SimulationTime.SLEEP_TIME_MILLISECONDS.get());
                } while (trainAction == TrainMovement.TrainAction.PROCEED && !Main.simulator.getDone().get());

                // Do the specified actions (headway and signal stops do not have any explicit actions)
                switch (trainAction) {
                    case END_STOP:
                        // Open the window for the train to be edited
                        this.trainMovement.setEditable(true);

                        // Wait in the end for the specified amount of time
                        while (this.trainMovement.waitAtEnd() && !Main.simulator.getDone().get()) {
                            synchronized (Simulator.tickLock) {
                                Simulator.tickLock.wait();
                            }

                            // Pause the thread
//                            Thread.sleep(SimulationTime.SLEEP_TIME_MILLISECONDS.get());
                        }

                        // Close the window for the train to be edited
                        this.trainMovement.setEditable(false);

                        // If the train has been deactivated at this point, scrub the previous directions and head
                        // home to the depot instead
                        if (!this.trainMovement.isActive()) {
                            // Clear the stations list
                            this.trainMovement.getStationQueue().setNewStations(
                                    new ArrayList<>(),
                                    !this.trainMovement.isTowardsNearEnd()
                            );

                            // Generate directions to the depot
                            this.trainMovement.generateDirectionsToDepot();
                        } else if (this.trainMovement.isStationListEdited()) {
                            // Or if the train has had its station list edited at this point, re-generate the directions
                            // list based on the new stations list
                            this.getTrainMovement().setStationListEdited(false);

                            // Generate the directions based on the new station list
                            this.trainMovement.generateDirectionsToNextStation(
                                    this.getTrainMovement().getDesiredDirection()
                            );
                        }

                        // Switch directions
                        this.trainMovement.switchDirection();

                        break;
                    case STATION_STOP:
                        // Snap the train flush to its segment in the station
                        this.trainMovement.snapToSegment(
                                this.getHead().getTrainCarriageLocation().getSegmentLocation(),
                                CARRIAGE_GAP,
                                true
                        );

                        // Wait in the station for the specified amount of time
                        while (this.trainMovement.waitAtStation() && !Main.simulator.getDone().get()) {
                            synchronized (Simulator.tickLock) {
                                Simulator.tickLock.wait();
                            }

                            // If the train has been deactivated while waiting at a station, this will serve as the
                            // train's final station stop
                            // TODO: Maybe extend the waiting time to account for passengers disembarking?
                            if (!this.getTrainMovement().isActive()) {
                                this.getTrainMovement().setDisembarkedWhenRemoved(true);
                            }

                            // Pause the thread
//                            Thread.sleep(SimulationTime.SLEEP_TIME_MILLISECONDS.get());
                        }

                        // Remove this station from the station list, as the the train is already here
                        this.trainMovement.getStationQueue().pop();

                        // Before anything else, check if there are any stations left in the station queue
                        // If there aren't any stations left, it means the train has already covered all necessary
                        // stations, and it is time to regenerate the station queue in reverse order
                        if (this.trainMovement.getStationQueue().isEmpty()) {
                            // Update the station list, as it has run out
                            this.trainMovement.getStationQueue().reverseStations(
                                    !this.trainMovement.isTowardsNearEnd()
                            );

                            // Reverse the intended direction of the train
                            this.getTrainMovement().setDesiredDirection(
                                    Track.opposite(this.getTrainMovement().getDesiredDirection())
                            );
                        }

                        // Update the directions to the next station
                        this.trainMovement.generateDirectionsToNextStation(
                                this.getTrainMovement().getDesiredDirection()
                        );

                        break;
                }

                // Finally, check if the train has reached the home depot
                // If it has, then the train is now ready to despawn
                if (!this.getTrainMovement().isActive() && trainAction == TrainMovement.TrainAction.DEPOT_STOP) {
                    this.pullOut(this.getTrainSystem().getActiveTrains(), this.getTrainSystem().getInactiveTrains());

                    break;
                }

                // Update the summary
                // TODO: Include train action in properties
                this.trainProperty.updateTrainProperty(this.identifier, this.trainMovement, this.trainCarriages
                        .getFirst());
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    // Deploy a train
    public void deploy(List<Train> activeTrains, List<Train> inactiveTrains) {
        // Remove this train from the list of inactive trains
        inactiveTrains.remove(this);

        // Add this train to the list of active trains
        activeTrains.add(this);

        // Set the train status to active
        this.getTrainMovement().setActive(true);

        // Finally, start the train thread
        new Thread(this).start();
    }

    // Pull a train out of the system
    public void pullOut(List<Train> activeTrains, List<Train> inactiveTrains) {
        // Remove this train from the list of active trains
        activeTrains.remove(this);

        // Add this train to the list of deactivated trains
        inactiveTrains.add(this);

        // Clear the segment(s) where this train used to be
        // Release the signals for these segments as well
        Segment segment;

        for (TrainCarriage trainCarriage : this.trainCarriages) {
            segment = trainCarriage.getTrainCarriageLocation().getSegmentLocation();

            segment.getTrainQueue().clearTrainQueue();
            segment.getFrom().getSignal().release();
        }

        // Reset the train to its default settings
        this.resetTrain();

        // Enable the insert train button
        MainScreenController.ARM_ADD_TRAIN_BUTTON.release();

        // Update the UI elements
        Main.mainScreenController.requestUpdateUI(MainScreenController.getActiveSimulationContext().getTrainSystem(),
                false);
    }

    // Set the train to this station
    public void arriveAt(Station station) {
        // Compile the passengers that will leave for this station
        for (TrainCarriage trainCarriage : this.trainCarriages) {
            trainCarriage.preparePassengersToAlight(station);
        }

        // Get the direction of the train
        Track.Direction trainDirection = this.getTrainMovement().getActualDirection();

        // Convert it to the travel direction as understood by the passengers
        PassengerMovement.TravelDirection travelDirection = PassengerMovement.TravelDirection.convertToTravelDirection(
                this.getTrainSystem(),
                trainDirection
        );

        // Set the train on its current station at its current direction
        station.getTrains().put(
                travelDirection,
                this
        );

        // Have the station allow the passengers with the same direction as this train
        int passengersAtPlatform = station.toggleTrainDoors(this, travelDirection);

//        synchronized (station.getStationLayout().getPassengersInStation()) {
        // Count the passengers at the platform going the direction of this train
//        int relevantPassengersOnPlatform = 0;

//            // Set the train's waiting time depending on the number of passengers on the platform
//            // Get the floor where the train tracks are
//            Floor platformFloor = null;
//
//            synchronized (station.getStationLayout().getFloors()) {
//                for (Floor floor : station.getStationLayout().getFloors()) {
//                    if (!floor.getTracks().isEmpty()) {
//                        platformFloor = floor;
//
//                        break;
//                    }
//                }
//            }
//
//            assert platformFloor != null;
//
////            synchronized (platformFloor.getPassengersInFloor()) {
//            for (Passenger passenger : platformFloor.getPassengersInFloor()) {
//                if (passenger.getPassengerMovement().getTravelDirection().equals(travelDirection)) {
//                    relevantPassengersOnPlatform++;
//                }
//            }
//            }

//            for (Passenger passenger : station.getStationLayout().getPassengersInStation()) {
//                if (passenger.getPassengerMovement().getTravelDirection().equals(travelDirection)) {
//                    relevantPassengersOnPlatform++;
//                }
//            }

        // Use the number of passengers on the platform to determine the waiting time of the train at the station
        // 0 passengers = 11.14 s
        // > 500 passengers = 30.3 s
//        System.out.println(relevantPassengersOnPlatform);

        // Estimate the number of passengers at the platform from the station's passenger count
//        int estimatedPassengerPlatformCount = station.getStationLayout().getPassengersInStation().size();
////        int stationPassengerCount = station.getStationLayout().getPassengersInStation().size();
////        int estimatedPassengerCountOneDirection = stationPassengerCount / 2;
////        int estimatedPassengerPlatformCount = (int) (estimatedPassengerCountOneDirection * 0.75);
//
        final int passengersOnPlatformLimit = 500;
        passengersAtPlatform = Math.min(passengersAtPlatform, passengersOnPlatformLimit);

        double newWaitingTime = 0.03832 * passengersAtPlatform + 11.14;

        this.trainMovement.setWaitingTime((int) Math.round(newWaitingTime));

//        System.out.println(passengersAtPlatform + " -> " + this.trainMovement.getWaitingTime() + " s");
//        }
    }

    // Have the given train leave its current station
    public void depart() {
        // Get the direction of the train
        Track.Direction trainDirection = this.getTrainMovement().getActualDirection();

        // Convert it to the travel direction as understood by the passengers
        PassengerMovement.TravelDirection travelDirection = PassengerMovement.TravelDirection.convertToTravelDirection(
                this.getTrainSystem(),
                trainDirection
        );

        // Shut the doors of this station
        this.getTrainMovement().getCurrentStation().toggleTrainDoors(this, travelDirection);

        // Remove the train from its current station at its current direction
        this.getTrainMovement().getCurrentStation().getTrains().put(
                travelDirection,
                null
        );

        // Have the train log
        Simulator.logTrain(this, Main.simulator.getTime().getTime());
    }
}

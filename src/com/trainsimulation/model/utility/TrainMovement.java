package com.trainsimulation.model.utility;

import com.crowdsimulation.model.simulator.Simulator;
import com.trainsimulation.controller.graphics.GraphicsController;
import com.trainsimulation.controller.screen.MainScreenController;
import com.trainsimulation.model.core.environment.infrastructure.track.Junction;
import com.trainsimulation.model.core.environment.infrastructure.track.Segment;
import com.trainsimulation.model.core.environment.infrastructure.track.Track;
import com.trainsimulation.model.core.environment.trainservice.maintenance.Depot;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Platform;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.TrainCarriage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

// Contains information regarding the train's movement
public class TrainMovement {
    // Prepare the headway distances (the distances to be maintained between each train)
    private static final int DEFAULT_HEADWAY_DISTANCE = 300;
    public static final AtomicInteger HEADWAY_DISTANCE = new AtomicInteger(DEFAULT_HEADWAY_DISTANCE);

    // Special value for when the track is a dead end
    private static final double TRACK_ENDS = -1.0;

    // Manages the synchronization between different train movements
    private static final Semaphore MOVEMENT_LOCK = new Semaphore(1, true);

    // Denotes the stopping time of the train at the end of the line (s)
    private final int endWaitingTime;

    // Denotes the train's maximum velocity (km/h)
    private final double maxVelocity;

    // Denotes the train which owns this
    private final Train train;

    // Denotes the waiting time of the train (s)
    private int waitingTime;

    // Denotes the deceleration speed of the train (m/s^2)
    private final double deceleration;

    // Denotes the stations which this train should stop at
    private final StationQueue stationQueue;

    // Denotes the train's current waited time (s)
    private int waitedTime;

    // Denotes the train's current stopped time at an end (s)
    private int endWaitedTime;

    // Denotes the train's current velocity (km/h)
    private double velocity;

    // Denotes the train's current station
    private Station currentStation;

    // Denotes the train's last visited station
    private Station previousStoppedStation;

    // Denotes the train's last passed station
    private Station previousPassedStation;

    // Denotes whether the train has recently waited at the end
    private boolean waitedAtEnd;

    // Denotes whether or not the train is going towards the near end of the line (towards the depot)
    private volatile boolean towardsNearEnd;

    // Denotes whether this train has already made its stop to disembark passengers when it was removed
    private boolean disembarkedWhenRemoved;

    // Denotes whether the train has already stopped for its first station in route
    private boolean hasStopped;

    // Denotes whether the train is active or not (if it isn't, it should be going back to the depot)
    private volatile boolean active;

    // Denotes the list of directions the train must take to its next goal when it encounters a junction
    private List<Track.Direction> directions;

    // Denotes the desired direction of the train
    private Track.Direction desiredDirection;

    // Denotes the actual direction of the train
    private Track.Direction actualDirection;

    // Denotes whether the train's station list may be modified externally
    private volatile boolean editable;

    // Denotes whether the train's station list has been edited
    private volatile boolean stationListEdited;

    public TrainMovement(final double maxVelocity, final double deceleration, final Train train) {
        // TODO: Make editable
        final double baseWaitingTime = 18.56;
        final double standardDeviationWaitingTime = 7.03;

        final int endWaitingTime = 300;

        this.deceleration = deceleration;

        // TODO: Remove artificial stochasticity
        this.waitingTime = (int) Math.round(
                baseWaitingTime + Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * standardDeviationWaitingTime
        );
//        this.waitingTime = (int) Math.round(baseWaitingTime);

//         Just in case the waiting time goes below 5 seconds
        if (this.waitingTime < 5) {
            this.waitingTime = 5;
        }

        this.endWaitingTime = endWaitingTime;

        this.maxVelocity = maxVelocity;

        this.currentStation = null;
        this.previousStoppedStation = null;
        this.previousPassedStation = null;

        this.waitedAtEnd = false;
        this.disembarkedWhenRemoved = false;
        this.hasStopped = false;
        this.towardsNearEnd = false;

        this.stationQueue = new StationQueue();
        this.directions = new ArrayList<>();

        this.editable = false;
        this.stationListEdited = false;

        this.train = train;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }

    public int getWaitedTime() {
        return waitedTime;
    }

    public void setWaitedTime(int waitedTime) {
        this.waitedTime = waitedTime;
    }

    public int getEndWaitingTime() {
        return endWaitingTime;
    }

    public int getEndWaitedTime() {
        return endWaitedTime;
    }

    public void setEndWaitedTime(int endWaitedTime) {
        this.endWaitedTime = endWaitedTime;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public Train getTrain() {
        return train;
    }

    public Station getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(Station currentStation) {
        this.currentStation = currentStation;
    }

    public Station getPreviousStoppedStation() {
        return previousStoppedStation;
    }

    public void setPreviousStoppedStation(Station previousStoppedStation) {
        this.previousStoppedStation = previousStoppedStation;
    }

    public boolean isWaitedAtEnd() {
        return waitedAtEnd;
    }

    public void setWaitedAtEnd(boolean waitedAtEnd) {
        this.waitedAtEnd = waitedAtEnd;
    }

    public double getDeceleration() {
        return deceleration;
    }

    public StationQueue getStationQueue() {
        return stationQueue;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDisembarkedWhenRemoved() {
        return disembarkedWhenRemoved;
    }

    public void setDisembarkedWhenRemoved(boolean disembarkedWhenRemoved) {
        this.disembarkedWhenRemoved = disembarkedWhenRemoved;
    }

    public boolean isTowardsNearEnd() {
        return towardsNearEnd;
    }

    public void setTowardsNearEnd(boolean towardsNearEnd) {
        this.towardsNearEnd = towardsNearEnd;
    }

    public Station getPreviousPassedStation() {
        return previousPassedStation;
    }

    public void setPreviousPassedStation(Station previousPassedStation) {
        this.previousPassedStation = previousPassedStation;
    }

    public List<Track.Direction> getDirections() {
        return directions;
    }

    public Track.Direction getDesiredDirection() {
        return desiredDirection;
    }

    public void setDesiredDirection(Track.Direction desiredDirection) {
        this.desiredDirection = desiredDirection;
    }

    public Track.Direction getActualDirection() {
        return actualDirection;
    }

    public void setActualDirection(Track.Direction actualDirection) {
        this.actualDirection = actualDirection;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isStationListEdited() {
        return stationListEdited;
    }

    public void setStationListEdited(boolean stationListEdited) {
        this.stationListEdited = stationListEdited;
    }

    // Make the train switch directions
    public synchronized void switchDirection() {
        this.towardsNearEnd = !this.towardsNearEnd;
        this.actualDirection = Track.opposite(this.actualDirection);
    }

    // Make this train move forward while looking forward to see if there is nothing in its way
    public TrainAction move() throws InterruptedException {
        // Check whether moving is even possible in the first place
        // Lookahead distance (in m)
        TrainAction actionTaken = decideAction(HEADWAY_DISTANCE.get());

        // Only one train may move at a time to avoid race conditions
        TrainMovement.MOVEMENT_LOCK.acquire();

        // Take note of the appropriate action
        if (actionTaken == TrainAction.HEADWAY_STOP) {
            // Stop the train
            this.velocity = 0.0;

            // If the action was to stop because of a train ahead, just draw this train's position and stop
            GraphicsController.requestDrawLineView(MainScreenController.getActiveSimulationContext().getLineViewCanvases(),
                    MainScreenController.getActiveSimulationContext().getTrainSystem(),
                    MainScreenController.getActiveSimulationContext().getLineScaleDownFactor(), false);

            // Signal to all trains waiting to process their movement that they may now proceed to do so
            TrainMovement.MOVEMENT_LOCK.release();

            return TrainAction.HEADWAY_STOP;
        } else if (actionTaken == TrainAction.END_STOP) {
            // Check if the train has already stopped for the end, in which case, it doesn't need to stop anymore
            if (!this.waitedAtEnd) {
                // Stop the train
                this.velocity = 0.0;

                // The train has now begun stopping in the end
                this.waitedAtEnd = true;

                // The train will switch directions, so reset the variable denoting whether the train has stopped at its
                // first station in this route
                this.hasStopped = false;

                // Request a draw
                GraphicsController.requestDrawLineView(MainScreenController.getActiveSimulationContext().getLineViewCanvases(),
                        MainScreenController.getActiveSimulationContext().getTrainSystem(),
                        MainScreenController.getActiveSimulationContext().getLineScaleDownFactor(), false);

                // Signal to all trains waiting to process their movement that they may now proceed to do so
                TrainMovement.MOVEMENT_LOCK.release();

                return TrainAction.END_STOP;
            }
        } else if (actionTaken == TrainAction.STATION_STOP) {
            Station currentStation = this.getTrain().getHead().getTrainCarriageLocation().getSegmentLocation()
                    .getStation();

            // If the train is active, stop at this station if it is in its list
            if (this.active) {
                if (this.stationQueue.peek() == currentStation) {
                    // Check if the train has already stopped for this station, in which case, it doesn't need to stop
                    // anymore
                    // However, when a train changes direction, its previous station will be the same as its next
                    // station, so take that into account
                    // Also consider the situation where this is the train's first station in its route in this
                    // direction
                    if (this.previousStoppedStation != currentStation || this.waitedAtEnd || !this.hasStopped) {
                        // Stop the train
                        this.velocity = 0.0;

                        // If it hasn't been noted yet, the train has now passed the first station from the depot
                        if (this.previousPassedStation == null) {
                            // Tell the GUI to enable the add train button again
                            MainScreenController.ARM_ADD_TRAIN_BUTTON.release();
                        }

                        // If the train hasn't stopped yet for this direction, stop it now
                        if (!this.hasStopped) {
                            this.hasStopped = true;
                        }

                        // This is the current station
                        this.currentStation = currentStation;

                        // The train has now passed this station
                        this.previousPassedStation = currentStation;

                        // This train has now also stopped in this station, so this station will now be considered a
                        // previous one
                        this.previousStoppedStation = currentStation;

                        // Set the train on the current station
                        this.train.arriveAt(this.currentStation);

                        // Request a draw
                        GraphicsController.requestDrawLineView(MainScreenController.getActiveSimulationContext().getLineViewCanvases(),
                                MainScreenController.getActiveSimulationContext().getTrainSystem(),
                                MainScreenController.getActiveSimulationContext().getLineScaleDownFactor(),
                                false);

                        // Signal to all trains waiting to process their movement that they may now proceed to do so
                        TrainMovement.MOVEMENT_LOCK.release();

                        return TrainAction.STATION_STOP;
                    }
                }
            }

            // Since the train is in a station (whether it stops here or not), we may now reset the waited at end
            // variable
            this.waitedAtEnd = false;

            // This is the current station
            this.currentStation = currentStation;

            // If it hasn't been noted yet, the train has now passed the first station from the depot
            if (this.previousPassedStation == null) {
                // Tell the GUI to enable the add train button again
                MainScreenController.ARM_ADD_TRAIN_BUTTON.release();
            }

            // The train has now passed this station
            this.previousPassedStation = currentStation;
        } else if (actionTaken == TrainAction.SIGNAL_STOP) {
            // Stop the train
            this.velocity = 0.0;

            // If the action was to stop because of a train ahead, just draw this train's position and stop
            GraphicsController.requestDrawLineView(MainScreenController.getActiveSimulationContext().getLineViewCanvases(),
                    MainScreenController.getActiveSimulationContext().getTrainSystem(),
                    MainScreenController.getActiveSimulationContext().getLineScaleDownFactor(), false);

            // Signal to all trains waiting to process their movement that they may now proceed to do so
            TrainMovement.MOVEMENT_LOCK.release();

            return TrainAction.SIGNAL_STOP;
        } else if (actionTaken == TrainAction.DEPOT_STOP) {
            // If this train is inactive, it is time for it to despawn
            if (!this.active) {
                // Signal to all trains waiting to process their movement that they may now proceed to do so
                TrainMovement.MOVEMENT_LOCK.release();

                // Deactivate this train thread
                return TrainAction.DEPOT_STOP;
            }
        }

        // If it reaches this point, move the train
        // Compute the location of each train carriage after moving forward by the specified velocity
        // Move each carriage one by one
        synchronized (this.train.getTrainCarriages()) {
            // If the train is moving, and the current station is still set, check if the current station is part of the
            // train's route
            // If it is, have the train depart that station
            if (
                    this.currentStation != null
                            && this.stationQueue.getOriginalStationList().contains(this.currentStation)
            ) {
                // Have the station close the train doors that have opened for this train
                this.train.depart();

                // Because this train is moving, this train has no current station
                this.currentStation = null;
            }

            List<TrainCarriage> trainCarriages = this.train.getTrainCarriages();

            // Compute for the updated velocity
            this.updateVelocity();

            for (TrainCarriage trainCarriage : trainCarriages) {
                // Get the current train carriage
                // Then get its location information
                TrainCarriageLocation trainCarriageLocation = trainCarriage.getTrainCarriageLocation();

                // Get the current clearance of this carriage
                double segmentClearance = trainCarriageLocation.getSegmentClearance();

                // Get the current location of this carriage
                Segment currentSegment = trainCarriageLocation.getSegmentLocation();

                // Get the clearing distance (m/s) given the velocity
                double clearedDistance = toMetersPerSecond(this.velocity);

                // Move this carriage; change its clearance
                segmentClearance += clearedDistance;

                // If the clearance is at least 100% of the current segment, move this carriage to the next segment
                if (segmentClearance / currentSegment.getLength() >= 1.0) {
                    Segment previousSegment = currentSegment;

                    // Move the new location of this carriage until there is no more excess clearance
                    do {
                        // Set the clearance of this carriage relative to the next segment
                        segmentClearance -= currentSegment.getLength();

                        // Move to the segment indicated by the direction
                        currentSegment = currentSegment.getTo().getOutSegment(this.getDirection(trainCarriage));

                        // Reset the direction for this carriage
                        this.nextDirection(trainCarriage);
                    } while (segmentClearance / currentSegment.getLength() >= 1.0);

                    // If this carriage is the head of the train, try to enter the next segment
                    // This will only happen if the next segment is clear
                    Semaphore nextSegmentSignal = currentSegment.getFrom().getSignal();

                    if (trainCarriage == trainCarriage.getParentTrain().getHead()) {
                        // Before trying to move, surrender the movement lock first to allow others to move in case this
                        // carriage has to wait
                        TrainMovement.MOVEMENT_LOCK.release();

                        // Try moving to the next segment
                        nextSegmentSignal.acquire();

                        // Once this carriage can now move, reacquire the movement lock again
                        TrainMovement.MOVEMENT_LOCK.acquire();
                    }

                    // Remove this carriage from its former segment
                    // By this point, this carriage should be at the front of this segment
                    previousSegment.getTrainQueue().removeFirstTrainCarriage();

                    // Add this carriage to the next segment
                    currentSegment.getTrainQueue().insertTrainCarriage(trainCarriage);

                    // If this carriage is the tail of the train, set the signal at the start of the previous segment to
                    // allow other trains to enter it
                    Semaphore previousSegmentSignal = previousSegment.getFrom().getSignal();

                    if (trainCarriage == trainCarriage.getParentTrain().getTail()) {
                        previousSegmentSignal.release();
                    }
                }

                // Set the clearances and locations of this carriage
                trainCarriageLocation.setSegmentClearance(segmentClearance);
                trainCarriageLocation.setSegmentLocation(currentSegment);
            }
        }

        // Request a draw
        GraphicsController.requestDrawLineView(MainScreenController.getActiveSimulationContext().getLineViewCanvases(),
                MainScreenController.getActiveSimulationContext().getTrainSystem(),
                MainScreenController.getActiveSimulationContext().getLineScaleDownFactor(), false);

        // Signal to all trains waiting to process their movement that they may now proceed to do so
        TrainMovement.MOVEMENT_LOCK.release();

        return TrainAction.PROCEED;
    }

    // Update the directions of the train to its next station
    public void generateDirectionsToNextStation(Track.Direction goalPlatformDirection) {
        // Get the train's target location - the goal
        // But only check this when the train is active
        Station goalStation = null;

        if (this.isActive()) {
            goalStation = stationQueue.peek();
        }

        // Use the above information for finding the shortest path to it
        generateDirections(goalPlatformDirection, goalStation);
    }

    private void generateDirections(Track.Direction goalPlatformDirection, Station goalStation) {
        // Get the train's current location - the start
        Segment startSegment = this.getTrain().getHead().getTrainCarriageLocation().getSegmentLocation();

        // Prepare the directions list
        List<Track.Direction> directions;

        // If the train is active, just form the directions based on its next station or its activeness compared to its
        // position
        // Else, form the directions to the depot
        directions = shortestPath(
                startSegment,
                goalStation,
                goalPlatformDirection,
                new ArrayList<>(),
                0,
                this.actualDirection,
                new ArrayList<>(),
                this.active)
                .getDirections();

        // Apply the prepared directions to the train's direction list
        this.directions = directions;

        // Immediately take note of the first direction
        for (TrainCarriage trainCarriage : this.train.getTrainCarriages()) {
            this.resetDirection(trainCarriage);
        }
    }

    // Update the directions of the train to its depot
    public void generateDirectionsToDepot() {
        generateDirectionsToNextStation(null);
    }

    // Get the granular direction of the train carriage
    private Track.Direction getDirection(TrainCarriage trainCarriage) {
        return this.directions.get(trainCarriage.getTrainCarriageLocation().getDirectionIndex());
    }

    // Update the granular direction of the train carriage
    private void nextDirection(TrainCarriage trainCarriage) {
        trainCarriage.getTrainCarriageLocation().nextDirectionIndex();
    }

    // Reset the direction index of the train carriage
    private void resetDirection(TrainCarriage trainCarriage) {
        trainCarriage.getTrainCarriageLocation().resetDirectionIndex();
    }

    // Get the granular virtual direction of the train carriage
    private Track.Direction getVirtualDirection(TrainCarriage trainCarriage) {
        return this.directions.get(trainCarriage.getTrainCarriageLocation().getVirtualDirectionIndex());
    }

    // Update the granular virtual direction of the train carriage
    private void nextVirtualDirection(TrainCarriage trainCarriage) {
        trainCarriage.getTrainCarriageLocation().nextVirtualDirectionIndex();
    }

    // Reset the virtual direction index of the train carriage
    private void resetVirtualDirection(TrainCarriage trainCarriage) {
        trainCarriage.getTrainCarriageLocation().resetVirtualDirectionIndex();
    }

    // Use the Dijkstra's algorithm to find the shortest path to the given station from the given starting segment
    private ShortestPathResult shortestPath(
            Segment startSegment,
            Station goalStation,
            Track.Direction goalPlatformDirection,
            List<Track.Direction> directions,
            int distance,
            Track.Direction virtualDirection,
            List<Segment> visited,
            boolean seekStation) {
        // Get the current station, if any
        Station currentStation = startSegment.getStation();

        // Get the current depot, if any
        Depot currentDepot = startSegment.getDepot();

        // Get the desired platform of the goal station
        // But only check this if the train is looking for a station
        Platform goalPlatform = null;

        if (seekStation) {
            goalPlatform = goalStation.getPlatforms().get(goalPlatformDirection);
        }

        // Check if this segment has already been visited
        // If it has, do not consider this path, because we have by then cycled
        if (visited.contains(startSegment)) {
            return new ShortestPathResult(null, distance);
        } else if (seekStation
                && currentStation != null
                && goalStation == currentStation
                && goalPlatform == currentStation.getPlatforms().get(virtualDirection)) {
            // Check if there is a station on this segment
            // If there is, check if this station is the goal station
            // If there is, check if the desired platform is the goal platform
            // If it is, then return the path to it
            return new ShortestPathResult(directions, distance);
        } else if (!seekStation && currentDepot != null) {
            // Check if there is a depot on this segment
            // If there is, then return the path to it
            return new ShortestPathResult(directions, distance);
        } else {
            // Get the branches of this segment
            List<Segment> branches = new ArrayList<>(startSegment.getTo().getOutSegments().values());

            // Get the shortest path to the goal station from each of the branches
            ShortestPathResult minShortestPathResult = new ShortestPathResult(null, Integer.MAX_VALUE);
            ShortestPathResult currentShortestPathResult;

            for (Segment branch : branches) {
                List<Track.Direction> newDirections = new ArrayList<>(directions);
                newDirections.add(branch.getDirection());

                int newDistance = distance + branch.getLength();

                List<Segment> newVisited = new ArrayList<>(visited);
                newVisited.add(startSegment);

                currentShortestPathResult = shortestPath(
                        branch,
                        goalStation,
                        goalPlatformDirection,
                        newDirections,
                        newDistance,
                        branch.getDirection(),
                        newVisited,
                        seekStation
                );

                // Check whether the directions do not form a cycle
                // If it does, disregard the result
                if (currentShortestPathResult.getDirections() != null) {
                    // If this branch gets to the destination in a shorter path, consider this
                    if (currentShortestPathResult.getDistance() < minShortestPathResult.getDistance()) {
                        minShortestPathResult = currentShortestPathResult;
                    }
                }
            }

            return minShortestPathResult;
        }
    }

    // Convert a speed in km/h to m/s
    private double toMetersPerSecond(double velocity) {
        return velocity / 3600.0 * 1000.0;
    }

    // Makes this train wait for the necessary waiting time
    // Return true if the train still needs to wait
    // Else, return false
    public boolean waitAtStation() {
        this.waitedTime += 1;

        if (this.waitedTime == this.waitingTime) {
            this.waitedTime = 0;

            return false;
        } else {
            return true;
        }
    }

    // Makes this train wait for the necessary end waiting time
    // Return true if the train still needs to wait
    // Else, return false
    public boolean waitAtEnd() {
        this.endWaitedTime += 1;

        if (this.endWaitedTime == this.endWaitingTime) {
            this.endWaitedTime = 0;

            return false;
        } else {
            return true;
        }
    }

    // Decide what the train should to at the current time
    private TrainAction decideAction(final int lookaheadLimit) {
        // Get the front carriage of this train
        TrainCarriage frontCarriage = this.getTrain().getHead();

        // Take note of the next carriage
        TrainCarriage nextCarriage;

        // Get the current segment of this train
        Segment currentSegment = frontCarriage.getTrainCarriageLocation().getSegmentLocation();

        // Get the current train clearance
        double currentClearance = frontCarriage.getTrainCarriageLocation().getSegmentClearance();

        // Get the clearing distance (m/s) given the velocity
        double clearedDistance = toMetersPerSecond(this.maxVelocity);

        // Get the train clearance when the train will have moved one second later
        double nextClearance = currentClearance + clearedDistance;

        // Get the junction after this segment
        Junction nextJunction = currentSegment.getTo();

        // Check whether this train is currently in a depot
        // If it is, and the train has been deactivated, the train has to be despawned
        Depot currentDepot = currentSegment.getDepot();

        // Check whether this train is currently in a station
        // If it is, check whether this station is included in this train's stops
        // If it is, proceed until the train would have missed the station if it went any further
        Station currentStation = currentSegment.getStation();

        if (currentDepot != null) {
            return TrainAction.DEPOT_STOP;
        } else if (currentStation != null) {
            // If the would-be clearance would miss the station, it is time to stop
            if (nextClearance > currentSegment.getLength()) {
                return TrainAction.STATION_STOP;
            } else {
                // Otherwise, proceed
                return TrainAction.PROCEED;
            }
        } else if (nextJunction.isEnd()) {
            // If the junction at the end of the current segment is the end of the line, check how far this train
            // has gone for this segment
            // If the would-be clearance would miss the junction, it is time to stop
            if (nextClearance >= currentSegment.getLength()) {
                return TrainAction.END_STOP;
            } else {
                // Otherwise, proceed
                return TrainAction.PROCEED;
            }
        } else {
            // If the train does not need to stop because of a station or an end, measure the distance of this train and
            // the next train, if any
            // Get the train queue of the current segment
            TrainQueue trainQueue = currentSegment.getTrainQueue();

            double separation;
            int nextCarriageIndex;

            // Check if this carriage is at the head of the train queue of this segment (i.e., this carriage is the
            // carriage furthest along this segment; the first to leave this segment)
            // If it isn't, then look for the distance between this carriage and the next carriage in this segment
            if (!(trainQueue.getFirstTrainCarriage() == frontCarriage)) {
                // Look for the next train carriage in this segment (there should be one)
                // The next carriage following this carriage is the one directly in front of this carriage in the train
                // queue
                nextCarriageIndex = trainQueue.getIndexOfTrainCarriage(frontCarriage) - 1;
                nextCarriage = trainQueue.getTrainCarriage(nextCarriageIndex);

                // Compute the distance between this carriage and that
                separation = computeDistance(frontCarriage, nextCarriage, lookaheadLimit);
            } else {
                // If this carriage is the first in this queue, look for the distance between this carriage and the next
                // carriage in the segment where the lookahead distance falls on
                separation = computeDistance(frontCarriage, null, lookaheadLimit);
            }

            // If the separation between the next train and the current train is less than the lookahead distance, halt
            if (separation != TrainMovement.TRACK_ENDS && separation <= lookaheadLimit) {
                return TrainAction.HEADWAY_STOP;
            }

            // Finally, check whether it is safe to proceed (using signals)
            if (nextJunction.getSignal().availablePermits() == 0) {
                // If the signal at the next junction is a stop signal, proceed until the train would have missed the
                // signal
                if (nextClearance > currentSegment.getLength()) {
                    return TrainAction.SIGNAL_STOP;
                }
            }

            // Otherwise, the train is free to move
            return TrainAction.PROCEED;
        }
    }

    // Compute the distance between two trains (specifically, between the head of this train and the tail of the next
    // train) within the specified lookahead limit
    // If the second train isn't given, the method looks for the distance between the first train and the next train
    // to be found
    // If the distance between the two trains turns out to be more than the lookahead limit, just return a best effort
    // computation to avoid wasting time
    // If the track in front of the train is a dead end, return a special value
    private double computeDistance(TrainCarriage currentTrainHead, TrainCarriage nextTrainTail,
                                   int lookaheadLimit) {
        double distance;

        // If the two trains are in the same segment, just simply compute the distance between them
        if (nextTrainTail != null) {
            // The two trains must be in the same segment
            assert currentTrainHead.getTrainCarriageLocation().getSegmentLocation() == nextTrainTail
                    .getTrainCarriageLocation().getSegmentLocation() : "The two trains provided are not in the same" +
                    " segment";

            double thisCarriageClearance = currentTrainHead.getTrainCarriageLocation().getSegmentClearance();
            double nextCarriageClearance = nextTrainTail.getTrainCarriageLocation().getSegmentClearance();

            distance = nextCarriageClearance - thisCarriageClearance;
        } else {
            // If the two trains are in different segments, the distance between the two trains is the distance between
            // the preceding train and the end of its segment plus the sum of all segments in between the trains without
            // the second train plus the distance of the second train from the start of the segment

            // Get the distance between the current train and the end of its segment
            Segment currentTrainSegment = currentTrainHead.getTrainCarriageLocation().getSegmentLocation();

            double currentTrainToEndOfSegmentDistance
                    = currentTrainSegment.getLength()
                    - currentTrainHead.getTrainCarriageLocation().getSegmentClearance(
            );

            // Get the total distance of the segments in between this train and the next train not containing either of
            // them
            double emptySegmentDistances = 0.0;

            // Keep track of whether the distance accumulated exceeds the lookahead limit
            boolean exceedsLookahead = false;

            // Match the virtual direction index with the actual direction index
            this.resetVirtualDirection(currentTrainHead);

            while (true) {
                try {
                    // Look at the next segment, but only if there is
                    // If there isn't, no point in looking further
                    // Also, just keep looking ahead as far as the directions allow you to
                    // (i.e., no need to look past the current goal of the train)
                    currentTrainSegment = currentTrainSegment.getTo().getOutSegment(
                            this.getVirtualDirection(currentTrainHead));

                    this.nextVirtualDirection(currentTrainHead);
                } catch (IndexOutOfBoundsException ex) {
                    return TrainMovement.TRACK_ENDS;
                }

                // Check if the accumulated distance exceeds the lookahead limit (if it does, no point in this entire
                // calculation, the train may proceed safely)
                if (currentTrainToEndOfSegmentDistance + emptySegmentDistances > lookaheadLimit) {
                    exceedsLookahead = true;

                    break;
                }

                // Check if the next segment contains a train
                // If it does, wrap up with the computation of the accumulated distance of empty segments in between
                if (!currentTrainSegment.getTrainQueue().isTrainQueueEmpty()) {
                    // Take note of the next train
                    nextTrainTail = currentTrainSegment.getTrainQueue().getLastTrainCarriage();

                    // Stop looking for more
                    break;
                } else {
                    // If it doesn't, take note of the distance of the empty segment
                    emptySegmentDistances += currentTrainSegment.getLength();
                }
            }

            // If the distance so far was within the lookahead limit, make the complete computation
            if (!exceedsLookahead) {
                // Finally, get the distance between the start of that segment and the next train
                double startOfSegmentToNextTrainDistance = nextTrainTail.getTrainCarriageLocation()
                        .getSegmentClearance();

                distance = currentTrainToEndOfSegmentDistance + emptySegmentDistances
                        + startOfSegmentToNextTrainDistance;
            } else {
                // If not, just return the distance exceeding the lookahead limit - it doesn't matter anyway
                distance = currentTrainToEndOfSegmentDistance + emptySegmentDistances;
            }
        }

        // The distance between the next and the current train should be positive
        assert distance >= 0.0 : "Distance between trains are negative";

        return distance;
    }

    // Set this train to a segment
    public void setTrainAtSegment(final Segment segment, final double carriageGap) throws InterruptedException {
        // Wait until this segment is free of trains previously here
        segment.getFrom().getSignal().acquire();

        snapToSegment(segment, carriageGap, false);
    }

    // Snap this train to the head of the segment
    public void snapToSegment(final Segment segment, final double carriageGap, boolean isRelocate) {
        // Set in such a way that the train is just about to clear this segment
        final double headClearance = segment.getLength();
        double carriageOffset = 0.0;

        // Compute for the location of each train carriage
        for (TrainCarriage trainCarriage : this.train.getTrainCarriages()) {
            // Only insert the train carriage if it is not already inserted
            if (!isRelocate) {
                // Add this carriage to this segment
                segment.getTrainQueue().insertTrainCarriage(trainCarriage);
            }

            // Set the clearance of this segment
            trainCarriage.getTrainCarriageLocation().setSegmentClearance(headClearance - carriageOffset);

            // Set the location of this carriage to this segment
            trainCarriage.getTrainCarriageLocation().setSegmentLocation(segment);

            // Increment the offset
            carriageOffset += trainCarriage.getLength() + carriageGap;
        }

        // Redraw the line view canvas
        GraphicsController.requestDrawLineView(MainScreenController.getActiveSimulationContext().getLineViewCanvases(),
                MainScreenController.getActiveSimulationContext().getTrainSystem(),
                MainScreenController.getActiveSimulationContext().getLineScaleDownFactor(), false);

        // TODO: Move to own loop
        GraphicsController.requestDrawStationView(MainScreenController.getActiveSimulationContext().getStationViewCanvases(),
                MainScreenController.getActiveSimulationContext().getCurrentStation(),
                MainScreenController.getActiveSimulationContext().getStationScaleDownFactor(), false
        );
    }

    // Computes the velocity of the train (km/h)
    public void updateVelocity() {
        this.velocity = this.maxVelocity;
    }

    // Represents the possible actions for the train
    public enum TrainAction {
        PROCEED, // Tell the train to move
        HEADWAY_STOP, // Tell the train to stop because of a train in front of it (for headway maintenance)
        END_STOP, // Tell the train to stop because it is at the end of the line
        STATION_STOP, // Tell the train to stop because the train is in a station,
        SIGNAL_STOP, // Tell the train to stop because a signal says so
        DEPOT_STOP, // Tell the train to stop at the depot - this also means that the train has been signaled to despawn
    }
}

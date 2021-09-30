package com.crowdsimulation.model.core.agent.passenger.movement;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.pathfinding.MultipleFloorPassengerPath;
import com.crowdsimulation.model.core.agent.passenger.movement.pathfinding.PassengerPath;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.PlatformFloorField;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.NonObstacle;
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
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.Goal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Blockable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;
import com.crowdsimulation.model.core.environment.station.patch.position.Coordinates;
import com.crowdsimulation.model.core.environment.station.patch.position.Vector;
import com.crowdsimulation.model.simulator.Simulator;
import com.crowdsimulation.model.simulator.cache.MultipleFloorPatchCache;
import com.crowdsimulation.model.simulator.cache.PathCache;
import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.infrastructure.track.Track;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PassengerMovement {
    // Denotes the owner of this passenger movement object
    private final Passenger parent;

    // Denotes the positional and navigational variables of the current passenger
    // Denotes the position of the passenger
    private final Coordinates position;

    // Denotes the distance (m) the passenger walks in one second
    private final double baseWalkingDistance;
    private double preferredWalkingDistance;
    private double currentWalkingDistance;

    // Denotes the proposed and actual heading of the passenger in degrees where
    // E = 0 degrees
    // N = 90 degrees
    // W = 180 degrees
    // S = 270 degrees
    private double proposedHeading;
    private double heading;

    // Denotes this passenger's previous heading
    private double previousHeading;

    // Denotes the patch the passenger is currently in
    private Patch currentPatch;

    // Denotes the amenity the passenger is currently in, if any
    private Amenity currentAmenity;

    // Denotes the portals this passenger has already visited between goal amenities
    private final List<Portal> visitedPortals;

    // Denotes the floor this passenger plans to go to, if any
    private Floor goalFloor;

    // Denotes the portal this passenger plans to go to, if any
    private Portal goalPortal;

    // Denotes the patch of the passenger's goal
    private Patch goalPatch;

    // Denotes the amenity the passenger is aiming for
    private Amenity goalAmenity;

    // Denote the queue object the passenger is aiming for
    private QueueObject goalQueueObject;

    // Denotes the attractor the passenger is aiming for
    private Amenity.AmenityBlock goalAttractor;

    // Denotes the chosen train door entrance location, if any
    private TrainDoor.TrainDoorEntranceLocation goalTrainDoorEntranceLocation;

    // Denotes the state of this passenger's floor field
    private QueueingFloorField.FloorFieldState goalFloorFieldState;

    // Denotes the floor field of the passenger goal
    private QueueingFloorField goalFloorField;

    // Denotes the patch with the nearest queueing patch
    private Patch goalNearestQueueingPatch;

    // Denotes the route plan of this passenger
    private RoutePlan routePlan;

    // Denotes the current path followed by this passenger, if any
    private PassengerPath currentPath;

    // Get the floor where this passenger currently is
    private Floor currentFloor;

    // Denotes the direction of travel of the passenger
    private TravelDirection travelDirection;

    // Denotes the disposition of the passenger - whether the passenger is about to ride a train, or the passenger is
    // about to depart the station (macroscopic state)
    private Disposition disposition;

    // Denotes the state of the passenger - the current disposition of the passenger (macroscopic state)
    private State state;

    // Denotes the action of the passenger - the low-level description of what the passenger is doing (microscopic
    // state)
    private Action action;

    // Denotes whether the passenger is temporarily waiting on an amenity to be vacant
    private boolean isWaitingOnAmenity;

    // Denotes whether the passenger is temporarily waiting for a portal to be vacant
    private boolean isWaitingOnPortal;

    // Denotes whether this passenger has encountered the passenger to be followed in the queue
    private boolean hasEncounteredPassengerToFollow;

    // Denotes whether this passenger has encountered a passenger waiting on a portal
    private boolean hasEncounteredPortalWaitingPassenger;

    // Denotes the passenger this passenger is currently following while assembling
    private Passenger passengerFollowedWhenAssembling;

    // Denotes the distance moved by this passenger in the previous tick
    private double distanceMovedInTick;

    // Counts the ticks this passenger moved a distance under a certain threshold
    private int noMovementCounter;

    // Counts the ticks this passenger has spent moving - this will reset when stopping
    private int movementCounter;

    // Counts the ticks this passenger has seen less than the defined number of patches
    private int noNewPatchesSeenCounter;

    // Counts the ticks this passenger has spent seeing new patches - this will reset otherwise
    private int newPatchesSeenCounter;

    // Denotes whether the passenger is stuck
    private boolean isStuck;

    // Counts the ticks this passenger has spent being stuck - this will reset when a condition is reached
    private int stuckCounter;

    // Denotes the time since the passenger left its previous goal
    private int timeSinceLeftPreviousGoal;

    // Denotes the time until the passenger accelerates fully from non-movement
    private final int ticksUntilFullyAccelerated;

    // Denotes the time the passenger has spent accelerating or moving at a constant speed so far without slowing down
    // or stopping
    private int ticksAcceleratedOrMaintainedSpeed;

    // Denotes the field of view angle of the passenger
    private final double fieldOfViewAngle;

    // Denotes whether the passenger is ready to be freed from being stuck
    private boolean isReadyToFree;

    // Denotes whether the passenger as a stored value card holder is ready to pathfind
    private boolean willPathfind;

    // Denotes whether this passenger as a stored value card holder has already pathfound
    private boolean hasPathfound;

    // Denotes whether this passenger should take a step forward after it left its goal
    private boolean shouldStepForward;

    // Denotes whether this passenger should stop and wait at the platform
    private boolean shouldStopAtPlatform;

    // Denotes whether this passenger is ready to exit the station immediately
    private boolean isReadyToExit;

    // Denotes the patches to explore for obstacles or passengers
//    private List<Patch> toExplore;
//    private Patch chosenQueueingPatch;

    // Denotes the recent patches this passenger has been in
    private final ConcurrentHashMap<Patch, Integer> recentPatches;

    // The vectors of this passenger
    private final List<Vector> repulsiveForceFromPassengers;
    private final List<Vector> repulsiveForcesFromObstacles;
    private Vector attractiveForce;
    private Vector motivationForce;

    public PassengerMovement(
            Gate gate,
            Passenger parent,
            double baseWalkingDistance,
            Coordinates coordinates,
            TravelDirection travelDirection,
            boolean isBoarding
    ) {
        this.parent = parent;

        this.position = new Coordinates(
                coordinates.getX(),
                coordinates.getY()
        );

        // The walking speed values shall be in m/s
        final double interQuartileRange = 0.12;

        this.baseWalkingDistance
                = baseWalkingDistance + Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * interQuartileRange;

        this.preferredWalkingDistance = this.baseWalkingDistance;
        this.currentWalkingDistance = preferredWalkingDistance;

        // All newly generated passengers will face the north by default
        // The heading values shall be in degrees, but have to be converted to radians for the math libraries to process
        // East: 0 degrees
        // North: 90 degrees
        // West: 180 degrees
        // South: 270 degrees
        this.proposedHeading = Math.toRadians(90.0);
        this.heading = Math.toRadians(90.0);

        this.previousHeading = Math.toRadians(90.0);

        // Set the passenger's field of view
        this.fieldOfViewAngle = Math.toRadians(90.0);

        // Add this passenger to the start patch
        this.currentPatch
                = gate.getAmenityBlocks().get(0).getPatch().getFloor().getPatch(coordinates);
        this.currentPatch.getPassengers().add(parent);

        // Set the passenger's time until it fully accelerates
        this.ticksUntilFullyAccelerated = 10;
        this.ticksAcceleratedOrMaintainedSpeed = 0;

        // Take note of the amenity where this passenger was spawned
        this.currentAmenity = gate;

        // Prepare the list for the passenger's visited portals
        this.visitedPortals = new ArrayList<>();

        // Assign the route plan of this passenger
        this.routePlan = new RoutePlan(
                this.parent.getTicketType() == TicketBooth.TicketType.STORED_VALUE,
                isBoarding
        );

        // Assign the floor of this passenger
        this.currentFloor = gate.getAmenityBlocks().get(0).getPatch().getFloor();

        // Assign the initial direction, disposition, state, action of this passenger
        this.travelDirection = travelDirection;
        this.disposition = isBoarding ? Disposition.BOARDING : Disposition.ALIGHTING;
        this.state = State.WALKING;
        this.action = Action.WILL_QUEUE;

        this.recentPatches = new ConcurrentHashMap<>();

//        this.toExplore = new ArrayList<>();

        repulsiveForceFromPassengers = new ArrayList<>();
        repulsiveForcesFromObstacles = new ArrayList<>();

        // This passenger will not exit yet
        this.isReadyToExit = false;

        // Set the passenger goal
        resetGoal(false);
    }

    public PassengerMovement(
            Gate gate,
            Passenger parent,
            double baseWalkingDistance,
            Coordinates coordinates,
            PassengerTripInformation passengerTripInformation
    ) {
        this.parent = parent;

        this.position = new Coordinates(
                coordinates.getX(),
                coordinates.getY()
        );

        // The walking speed values shall be in m/s
        final double interQuartileRange = 0.12;

        this.baseWalkingDistance
                = baseWalkingDistance + Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * interQuartileRange;

        this.preferredWalkingDistance = this.baseWalkingDistance;
        this.currentWalkingDistance = preferredWalkingDistance;

        // All newly generated passengers will face the north by default
        // The heading values shall be in degrees, but have to be converted to radians for the math libraries to process
        // East: 0 degrees
        // North: 90 degrees
        // West: 180 degrees
        // South: 270 degrees
        this.proposedHeading = Math.toRadians(90.0);
        this.heading = Math.toRadians(90.0);

        this.previousHeading = Math.toRadians(90.0);

        // Set the passenger's field of view
        this.fieldOfViewAngle = Math.toRadians(90.0);

        // Add this passenger to the start patch
        this.currentPatch
                = gate.getAmenityBlocks().get(0).getPatch().getFloor().getPatch(coordinates);
        this.currentPatch.getPassengers().add(parent);

        // Set the passenger's time until it fully accelerates
        this.ticksUntilFullyAccelerated = 10;
        this.ticksAcceleratedOrMaintainedSpeed = 0;

        // Take note of the amenity where this passenger was spawned
        this.currentAmenity = gate;

        // Prepare the list for the passenger's visited portals
        this.visitedPortals = new ArrayList<>();

        // Assign the route plan of this passenger
        this.routePlan = new RoutePlan(
                this.parent.getTicketType() == TicketBooth.TicketType.STORED_VALUE,
                true,
                passengerTripInformation.getEntryStation(),
                passengerTripInformation.getExitStation(),
                passengerTripInformation.getEntryStation()
        );

        // Assign the floor of this passenger
        this.currentFloor = gate.getAmenityBlocks().get(0).getPatch().getFloor();

        // Assign the initial direction, disposition, state, action of this passenger
        this.travelDirection = passengerTripInformation.getTravelDirection();
        this.disposition = Disposition.BOARDING;
        this.state = State.WALKING;
        this.action = Action.WILL_QUEUE;

        this.recentPatches = new ConcurrentHashMap<>();

//        this.toExplore = new ArrayList<>();

        repulsiveForceFromPassengers = new ArrayList<>();
        repulsiveForcesFromObstacles = new ArrayList<>();

        // This passenger will not exit yet
        this.isReadyToExit = false;

        // Set the passenger goal
        resetGoal(false);
    }

    public Passenger getParent() {
        return parent;
    }

    public Coordinates getPosition() {
        return position;
    }

    public void setPosition(Coordinates coordinates) {
        final int timeElapsedExpiration = 10;

        Patch previousPatch = this.currentPatch;

        this.position.setX(coordinates.getX());
        this.position.setY(coordinates.getY());

        // Get the patch of the new position
        Patch newPatch = this.currentFloor.getPatch(new Coordinates(coordinates.getX(), coordinates.getY()));

        // If the newer position is on a different patch, remove the passenger from its old patch, then
        // add it to the new patch
        if (!previousPatch.equals(newPatch)) {
            previousPatch.getPassengers().remove(this.parent);
            newPatch.getPassengers().add(this.parent);

            // Remove this passenger from the patch set of the previous patch
            SortedSet<Patch> previousPatchSet = previousPatch.getFloor().getPassengerPatchSet();
            SortedSet<Patch> newPatchSet = newPatch.getFloor().getPassengerPatchSet();

            if (previousPatchSet.contains(previousPatch) && hasNoPassenger(previousPatch)) {
                previousPatchSet.remove(previousPatch);
            }

            // Then add this passenger to the patch set of the next patch
            newPatchSet.add(newPatch);

            // Then set the new current patch
            this.currentPatch = newPatch;

            // Update the recent patch list
            updateRecentPatches(this.currentPatch, timeElapsedExpiration);
        } else {
            // Update the recent patch list
            updateRecentPatches(null, timeElapsedExpiration);
        }
    }

    public double getCurrentWalkingDistance() {
        return currentWalkingDistance;
    }

    public double getProposedHeading() {
        return proposedHeading;
    }

    public double getHeading() {
        return heading;
    }

    public Patch getCurrentPatch() {
        return currentPatch;
    }

    public void setCurrentPatch(Patch currentPatch) {
        this.currentPatch = currentPatch;
    }

    public List<Portal> getVisitedPortals() {
        return visitedPortals;
    }

    public Amenity getCurrentAmenity() {
        return currentAmenity;
    }

    public Amenity.AmenityBlock getGoalAttractor() {
        return goalAttractor;
    }

    public Patch getGoalPatch() {
        return goalPatch;
    }

    public Amenity getGoalAmenity() {
        return goalAmenity;
    }

    public QueueingFloorField.FloorFieldState getGoalFloorFieldState() {
        return goalFloorFieldState;
    }

    public QueueingFloorField getGoalFloorField() {
        return goalFloorField;
    }

    public Patch getGoalNearestQueueingPatch() {
        return goalNearestQueueingPatch;
    }

    public RoutePlan getRoutePlan() {
        return routePlan;
    }

    public void setRoutePlan(RoutePlan routePlan) {
        this.routePlan = routePlan;
    }

    public PassengerPath getCurrentPath() {
        return currentPath;
    }

    public Floor getCurrentFloor() {
        return currentFloor;
    }

    public TravelDirection getTravelDirection() {
        return travelDirection;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public void setDisposition(Disposition disposition) {
        this.disposition = disposition;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean isWaitingOnAmenity() {
        return isWaitingOnAmenity;
    }

    public boolean isWaitingOnPortal() {
        return isWaitingOnPortal;
    }

    public Passenger getPassengerFollowedWhenAssembling() {
        return passengerFollowedWhenAssembling;
    }

    public boolean hasEncounteredPassengerToFollow() {
        return hasEncounteredPassengerToFollow;
    }

    public boolean hasEncounteredPortalWaitingPassenger() {
        return hasEncounteredPortalWaitingPassenger;
    }

//    public Patch getChosenQueueingPatch() {
//        return chosenQueueingPatch;
//    }
//
//    public List<Patch> getToExplore() {
//        return toExplore;
//    }

    public ConcurrentHashMap<Patch, Integer> getRecentPatches() {
        return recentPatches;
    }

    public double getDistanceMovedInTick() {
        return distanceMovedInTick;
    }

    public int getNoMovementCounter() {
        return noMovementCounter;
    }

    public int getMovementCounter() {
        return movementCounter;
    }

    public int getNoNewPatchesSeenCounter() {
        return noNewPatchesSeenCounter;
    }

    public int getNewPatchesSeenCounter() {
        return newPatchesSeenCounter;
    }

    public int getStuckCounter() {
        return stuckCounter;
    }

    public int getTimeSinceLeftPreviousGoal() {
        return timeSinceLeftPreviousGoal;
    }

    public boolean isStuck() {
        return isStuck;
    }

    public boolean isReadyToFree() {
        return isReadyToFree;
    }

    public boolean willPathFind() {
        return willPathfind;
    }

    public boolean isReadyToExit() {
        return isReadyToExit;
    }

    public List<Vector> getRepulsiveForceFromPassengers() {
        return repulsiveForceFromPassengers;
    }

    public List<Vector> getRepulsiveForcesFromObstacles() {
        return repulsiveForcesFromObstacles;
    }

    public Vector getAttractiveForce() {
        return attractiveForce;
    }

    public Vector getMotivationForce() {
        return motivationForce;
    }

    //

    public Queueable getGoalAmenityAsQueueable() {
        return Queueable.toQueueable(this.goalAmenity);
    }

    public Goal getGoalAmenityAsGoal() {
        return Goal.toGoal(this.goalAmenity);
    }

    public TrainDoor getGoalAmenityAsTrainDoor() {
        return TrainDoor.asTrainDoor(this.goalAmenity);
    }

    public Turnstile getGoalAmenityAsTurnstile() {
        return Turnstile.asTurnstile(this.goalAmenity);
    }

    public Blockable getGoalAmenityAsBlockable() {
        return Blockable.asBlockable(this.goalAmenity);
    }

    public Portal getGoalAmenityAsPortal() {
        return Portal.asPortal(this.goalAmenity);
    }

    // Use the A* algorithm (with Euclidean distance to compute the f-score) to find the shortest path to the given goal
    // patch
    public static PassengerPath computePathWithinFloor(
            Patch startingPatch,
            Patch goalPatch,
            boolean includeStartingPatch,
            boolean includeGoalPatch,
            boolean passThroughBlockables
    ) {
        boolean hasAlreadyPassedOneBlockable = false;

        // Check the cache first if the path from one patch to another has already been computed beforehand
        PathCache pathCache = startingPatch.getFloor().getPathCache();

        Patch.PatchPair patchPair = new Patch.PatchPair(startingPatch, goalPatch);
        PathCache.PathCacheKey pathCacheKey
                = new PathCache.PathCacheKey(patchPair, includeStartingPatch, includeGoalPatch);
        PassengerPath cachedPath = pathCache.get(pathCacheKey);

        // If the path connecting these patches have already been computed, use that instead
        if (cachedPath != null) {
            return cachedPath;
        }

        // If the starting and goal patches are of different floors, immediately return false
        if (!startingPatch.getFloor().equals(goalPatch.getFloor())) {
            pathCache.put(pathCacheKey, null);

            return null;
        }

        HashSet<Patch> openSet = new HashSet<>();

        HashMap<Patch, Double> gScores = new HashMap<>();
        HashMap<Patch, Double> fScores = new HashMap<>();

        HashMap<Patch, Patch> cameFrom = new HashMap<>();

        for (Patch[] patchRow : startingPatch.getFloor().getPatches()) {
            for (Patch patch : patchRow) {
                gScores.put(patch, Double.MAX_VALUE);
                fScores.put(patch, Double.MAX_VALUE);
            }
        }

        gScores.put(startingPatch, 0.0);
        fScores.put(
                startingPatch,
                Coordinates.distance(
                        startingPatch.getFloor().getStation(),
                        startingPatch,
                        goalPatch
                )
        );

        openSet.add(startingPatch);

        while (!openSet.isEmpty()) {
            Patch patchToExplore;

            double minimumDistance = Double.MAX_VALUE;
            Patch patchWithMinimumDistance = null;

            for (Patch patchInQueue : openSet) {
                double fScore = fScores.get(patchInQueue);

                if (fScore < minimumDistance) {
                    minimumDistance = fScore;
                    patchWithMinimumDistance = patchInQueue;
                }
            }

            patchToExplore = patchWithMinimumDistance;

            if (patchToExplore.equals(goalPatch)) {
                Stack<Patch> path = new Stack<>();
                double length = 0.0;

                Patch currentPatch = goalPatch;

                while (cameFrom.containsKey(currentPatch)) {
                    Patch previousPatch = cameFrom.get(currentPatch);

                    length += Coordinates.distance(
                            previousPatch.getPatchCenterCoordinates(),
                            currentPatch.getPatchCenterCoordinates()
                    );

                    currentPatch = previousPatch;

                    path.push(currentPatch);
                }

                PassengerPath passengerPath = new PassengerPath(length, path);

                pathCache.put(pathCacheKey, passengerPath);

                return passengerPath;
            }

            openSet.remove(patchToExplore);

            List<Patch> patchToExploreNeighbors = patchToExplore.getNeighbors();

            for (Patch patchToExploreNeighbor : patchToExploreNeighbors) {
/*
                boolean willExpand;

                if (patchToExploreNeighbor.getAmenityBlock() == null) {
                    willExpand = true;
                } else {
                    if (patchToExploreNeighbor.getAmenityBlock() != null) {
                        if (
                                passThroughBlockables && patchToExploreNeighbor.getAmenityBlock().getParent()
                                        instanceof BlockableAmenity
                        ) {
                            willExpand = true;

                            // Already passed through one blockable
                            hasAlreadyPassedOneBlockable = false;
                        } else {
                            if (
                                    !includeStartingPatch && patchToExplore.equals(startingPatch)
                                            || !includeGoalPatch && patchToExploreNeighbor.equals(goalPatch)
                            ) {
                                willExpand = true;
                            } else {
                                willExpand = false;
                            }
                        }
                    } else {
                        willExpand = false;
                    }
                }
*/

                if (
//                        willExpand
                        patchToExploreNeighbor.getAmenityBlock() == null
                                || patchToExploreNeighbor.getAmenityBlock() != null
                                && (
                                (
                                        passThroughBlockables
                                                && patchToExploreNeighbor.getAmenityBlock().getParent()
                                                instanceof Blockable
                                )
                                        || (
                                        !includeStartingPatch && patchToExplore.equals(startingPatch)
                                                || !includeGoalPatch && patchToExploreNeighbor.equals(goalPatch)
                                )
                        )
                ) {
                    // Avoid patches that are close to amenity blocks, unless absolutely necessary
                    double obstacleClosenessPenalty = patchToExploreNeighbor.getAmenityBlocksAround() * 2.0;

                    double tentativeGScore
                            = gScores.get(patchToExplore)
                            + Coordinates.distance(
                            startingPatch.getFloor().getStation(),
                            patchToExplore,
                            patchToExploreNeighbor
                    ) + obstacleClosenessPenalty;

                    if (tentativeGScore < gScores.get(patchToExploreNeighbor)) {
                        cameFrom.put(patchToExploreNeighbor, patchToExplore);

                        gScores.put(patchToExploreNeighbor, tentativeGScore);
                        fScores.put(
                                patchToExploreNeighbor,
                                gScores.get(patchToExploreNeighbor)
                                        + Coordinates.distance(
                                        startingPatch.getFloor().getStation(),
                                        patchToExploreNeighbor,
                                        goalPatch)
                        );

                        openSet.add(patchToExploreNeighbor);
                    }
                }
            }
        }

        // There are no paths from the origin to the destination patches
        pathCache.put(pathCacheKey, null);

        return null;
    }

    // Check whether the current goal amenity is a queueable or not
    public boolean isNextAmenityQueueable() {
        return Queueable.isQueueable(this.goalAmenity);
    }

    // Check whether the current goal amenity is a goal or not
    public boolean isNextAmenityGoal() {
        return Goal.isGoal(this.goalAmenity);
    }

    // Check whether the current goal amenity is a train door or not
    public boolean isNextAmenityTrainDoor() {
        return TrainDoor.isTrainDoor(this.goalAmenity);
    }

    // Check whether the passenger has just left the goal (if the passenger is at a certain number of ticks since
    // leaving the goal)
    public boolean hasJustLeftGoal() {
        final int hasJustLeftGoalThreshold = 3;

        return this.timeSinceLeftPreviousGoal <= hasJustLeftGoalThreshold;
    }

    // Reset the passenger's goal
    public void resetGoal(boolean shouldStepForwardFirst) {
        // Reset the passenger goals
        this.goalFloor = null;
        this.goalPortal = null;
        this.goalPatch = null;
        this.goalAmenity = null;
        this.goalQueueObject = null;
        this.goalAttractor = null;
        this.goalTrainDoorEntranceLocation = null;

        // Take note of the floor field state of this passenger
        this.goalFloorFieldState = null;

        // Take note of the floor field of the passenger's goal
        this.goalFloorField = null;

        // Take note of the passenger's nearest queueing patch
        this.goalNearestQueueingPatch = null;

        // No passengers have been encountered yet
        this.hasEncounteredPassengerToFollow = false;
        this.hasEncounteredPortalWaitingPassenger = false;

        // This passenger is not yet waiting
        this.isWaitingOnAmenity = false;
        this.isWaitingOnPortal = false;

        // Set whether this passenger is set to step forward
        this.shouldStepForward = shouldStepForwardFirst;

        // The passenger will not stop at the platform yet
        this.shouldStopAtPlatform = false;

        // This passenger is not following anyone yet
        this.passengerFollowedWhenAssembling = null;

        // This passenger hasn't moved yet
        this.distanceMovedInTick = 0.0;

        this.noMovementCounter = 0;
        this.movementCounter = 0;

        this.noNewPatchesSeenCounter = 0;
        this.newPatchesSeenCounter = 0;

        this.timeSinceLeftPreviousGoal = 0;

        // This passenger hasn't pathfound yet
        this.willPathfind = false;
        this.hasPathfound = false;

        // This passenger has no recent patches yet
        this.recentPatches.clear();

        // Reset debugging variables
//        this.chosenQueueingPatch = null;

        // This passenger is not yet stuck
        this.free();
    }

    // Get the portals needed to be entered from the current patch to the goal amenity
    // Ascend/descend floors if needed
    public static List<MultipleFloorPassengerPath> computePathAcrossFloors(
            Floor currentFloor,
//            List<PortalShaft> visitedPortalShafts,
            List<Station.AmenityCluster> visitedClusters,
            List<Portal> visitedPortals,
            Patch currentPatch,
            Amenity.AmenityBlock goalAmenityBlock
    ) {
        List<MultipleFloorPassengerPath> paths = new ArrayList<>();

        // Check the cache first if the path from one patch to another has already been computed beforehand
        MultipleFloorPatchCache multipleFloorPatchCache = currentFloor.getStation().getMultiFloorPathCache();

        Patch.PatchPair patchPair = new Patch.PatchPair(currentPatch, goalAmenityBlock.getPatch());
        PathCache.PathCacheKey pathCacheKey
                = new PathCache.PathCacheKey(patchPair, false, false);
        List<MultipleFloorPassengerPath> cachedPath = multipleFloorPatchCache.get(pathCacheKey);

        // If the path connecting these patches have already been computed, use that instead
        if (cachedPath != null) {
            return cachedPath;
        }

        // Check if a path to the goal exists on the same floor
        PassengerPath pathToGoal = computePathWithinFloor(
                currentPatch,
                goalAmenityBlock.getPatch(),
                true,
                false,
                false
        );

        // If a path to the goal exists in this floor, no portals need to be traversed anymore
        if (pathToGoal != null) {
            MultipleFloorPassengerPath multipleFloorPassengerPath = new MultipleFloorPassengerPath(
                    pathToGoal.getDistance(),
                    new ArrayList<>()
            );

            // This will be the only path to the goal
            paths.add(multipleFloorPassengerPath);

            multipleFloorPatchCache.put(pathCacheKey, paths);

            return paths;
        } else {
            // If a path to the goal does not exist in this floor, collect the portals that are accessible from the
            // current amenity, and see if there is a portal that eventually leads to the goal
            HashMap<Portal, Double> portalListInFloor = new HashMap<>();

            List<StairShaft> stairShafts = currentFloor.getStation().getStairShafts();
            List<EscalatorShaft> escalatorShafts = currentFloor.getStation().getEscalatorShafts();

            // TODO: Consider other portals
//                List<ElevatorShaft> elevatorShafts = currentFloor.getStation().getElevatorShafts();

            PassengerPath pathToPortal;

            // Compile the stair portals that serve this floor
            for (StairShaft stairShaft : stairShafts) {
                StairPortal lowerStairPortal = (StairPortal) stairShaft.getLowerPortal();
                StairPortal upperStairPortal = (StairPortal) stairShaft.getUpperPortal();

                // Only consider portals reachable from the current position
                pathToPortal = computePathWithinFloor(
                        currentPatch,
                        lowerStairPortal.getAttractors().get(0).getPatch(),
                        true,
                        false,
                        false
                );

                if (pathToPortal != null) {
                    if (lowerStairPortal.getFloorServed().equals(currentFloor)) {
                        portalListInFloor.put(lowerStairPortal, pathToPortal.getDistance());
                    }
                }

                // Only consider portals reachable from the current position
                pathToPortal = computePathWithinFloor(
                        currentPatch,
                        upperStairPortal.getAttractors().get(0).getPatch(),
                        true,
                        false,
                        false
                );

                if (pathToPortal != null) {
                    if (upperStairPortal.getFloorServed().equals(currentFloor)) {
                        portalListInFloor.put(upperStairPortal, pathToPortal.getDistance());
                    }
                }
            }

            // Compile the escalator portals that serve this floor, and whose directions come from this floor (that is,
            // do not take into account escalator portals which land on this floor)
            for (EscalatorShaft escalatorShaft : escalatorShafts) {
                EscalatorPortal lowerStairPortal = (EscalatorPortal) escalatorShaft.getLowerPortal();
                EscalatorPortal upperStairPortal = (EscalatorPortal) escalatorShaft.getUpperPortal();

                // Only consider portals reachable from the current position
                pathToPortal = computePathWithinFloor(
                        currentPatch,
                        lowerStairPortal.getAttractors().get(0).getPatch(),
                        true,
                        false,
                        false
                );

                if (pathToPortal != null) {
                    if (
                            lowerStairPortal.getFloorServed().equals(currentFloor)
                                    && lowerStairPortal.getEscalatorShaft().getEscalatorDirection()
                                    == EscalatorShaft.EscalatorDirection.UP
                    ) {
                        portalListInFloor.put(lowerStairPortal, pathToPortal.getDistance());
                    }
                }

                // Only consider portals reachable from the current position
                pathToPortal = computePathWithinFloor(
                        currentPatch,
                        upperStairPortal.getAttractors().get(0).getPatch(),
                        true,
                        false,
                        false
                );

                if (pathToPortal != null) {
                    if (
                            upperStairPortal.getFloorServed().equals(currentFloor)
                                    && upperStairPortal.getEscalatorShaft().getEscalatorDirection()
                                    == EscalatorShaft.EscalatorDirection.DOWN
                    ) {
                        portalListInFloor.put(upperStairPortal, pathToPortal.getDistance());
                    }
                }
            }

            // Compile all possible paths from this portal to the goal
            // Visit the floor served by each portal
            for (Map.Entry<Portal, Double> portalDistanceInFloor : portalListInFloor.entrySet()) {
                // Get the portal where this passenger will go
                Portal portalToEnter = portalDistanceInFloor.getKey();

                // Get the portal where this passenger will come out
                Portal portalToExit = portalToEnter.getPair();

                PortalShaft portalShaft = null;

                // TODO: Use polymorphism
                if (portalToEnter instanceof StairPortal) {
                    portalShaft = ((StairPortal) portalToEnter).getStairShaft();
                } else if (portalToEnter instanceof EscalatorPortal) {
                    portalShaft = ((EscalatorPortal) portalToEnter).getEscalatorShaft();
                } else if (portalToEnter instanceof ElevatorPortal) {
                    portalShaft = ((ElevatorPortal) portalToEnter).getElevatorShaft();
                }

                assert portalShaft != null;

                // Check if a portal belonging to the same cluster as this portal has already been validated
                // before
                // If so, there is no need to validate another portal of the same cluster anymore
                Station station = currentFloor.getStation();

                Station.AmenityCluster portalEntryCluster = station.getAmenityClusterByAmenity().get(portalToEnter);
                Station.AmenityCluster portalExitCluster = station.getAmenityClusterByAmenity().get(portalToExit);

                if (!visitedClusters.contains(portalExitCluster) && !visitedClusters.contains(portalEntryCluster)) {
                    // Then get the new floor served by that portal
                    Floor floorToEnter = portalToExit.getFloorServed();

                    // See if a path to the goal can be formed when passing through that portal
                    double newDistance = portalDistanceInFloor.getValue();

                    // TODO: Consider portal spatial size and other portals
                    if (portalToExit instanceof StairPortal) {
                        final double stairWeight;

                        if (portalShaft.getLowerPortal().equals(portalToExit)) {
                            // Going down
                            stairWeight = 1.5;
                        } else {
                            // Going up
                            stairWeight = 20.0;
                        }

                        newDistance
                                += ((StairPortal) portalToExit).getStairShaft().getMoveTime() * stairWeight;
                    } else if (portalToExit instanceof EscalatorPortal) {
                        final double escalatorWeight;

                        if (portalShaft.getLowerPortal().equals(portalToExit)) {
                            // Going down
                            escalatorWeight = 1.0;
                        } else {
                            // Going up
                            escalatorWeight = 10.0;
                        }

                        newDistance
                                += ((EscalatorPortal) portalToExit).getEscalatorShaft().getMoveTime() * escalatorWeight;
                    }

                    List<Station.AmenityCluster> newVisitedAmenityClusters = new ArrayList<>(visitedClusters);

                    newVisitedAmenityClusters.add(portalExitCluster);
                    newVisitedAmenityClusters.add(portalEntryCluster);

                    List<Portal> newVisitedPortals = new ArrayList<>(visitedPortals);

                    List<MultipleFloorPassengerPath> multipleFloorPassengerPaths = computePathAcrossFloors(
                            floorToEnter,
                            newVisitedAmenityClusters,
                            newVisitedPortals,
                            portalToExit.getAttractors().get(0).getPatch(),
                            goalAmenityBlock
                    );

                    // If a goal was reached through that path, take note of this path combined with that path
                    for (MultipleFloorPassengerPath multipleFloorPassengerPath : multipleFloorPassengerPaths) {
                        if (multipleFloorPassengerPath != null) {
                            double combinedDistance = newDistance + multipleFloorPassengerPath.getDistance();

                            newVisitedPortals.add(portalToEnter);
                            List<Portal> combinedVisitedPortals = new ArrayList<>(newVisitedPortals);
                            combinedVisitedPortals.addAll(multipleFloorPassengerPath.getPortals());

                            MultipleFloorPassengerPath combinedPath = new MultipleFloorPassengerPath(
                                    combinedDistance,
                                    combinedVisitedPortals
                            );

                            paths.add(combinedPath);

//                            // If a path is found, add the cluster containing this entry portal to the list of validated
//                            // clusters
//                            if (!visitedClusters.contains(portalEntryCluster)) {
//                                visitedClusters.add(portalEntryCluster);
//                            }
                        }
                    }
                }
            }

            multipleFloorPatchCache.put(pathCacheKey, paths);

            return paths;
        }
    }

    // Set the nearest goal to this passenger
    // That goal should also have the fewer passengers queueing for it
    // To determine this, for each two passengers in the queue (or fraction thereof), a penalty of one tile is added to
    // the distance to this goal
    public void chooseGoal() {
        // Only set the goal if one hasn't been set yet
        if (this.goalAmenity == null) {
            // Set the next amenity class
            Class<? extends Amenity> nextAmenityClass = this.routePlan.getCurrentAmenityClass();

            // Get the floors in this station which have the next amenity
            Station currentStation = this.currentFloor.getStation();
            Set<Floor> floorsWithNextAmenityClass = currentStation.getAmenityFloorIndex().get(nextAmenityClass);

            // Get the amenity cluster where this passenger came from
            Station.AmenityCluster originAmenityCluster
                    = currentStation.getAmenityClusterByAmenityAssorted().get(this.currentAmenity);

            // Get the amenity list in this floor
            List<? extends Amenity> amenityListInFloor = this.currentFloor.getAmenityList(nextAmenityClass);

            // If this floor does not contain the next amenity class, consult the directory of each portal that serves
            // this floor and see which portals should be entered to reach the floor with the desired amenity class
            boolean willSeekPortal = false;

            if (!floorsWithNextAmenityClass.contains(this.currentFloor)) {
                willSeekPortal = true;
            } else {
                // If there are no goal portals to be followed, simply have the passenger choose its goal
                Amenity chosenAmenity = null;
                QueueObject chosenQueueObject = null;
                Amenity.AmenityBlock chosenAttractor = null;
                TrainDoor.TrainDoorEntranceLocation chosenTrainDoorEntranceLocation = null;

                // Compile all attractors from each amenity in the amenity list
                HashMap<Amenity.AmenityBlock, Double> distancesToAttractors = new HashMap<>();

                for (Amenity amenity : amenityListInFloor) {
                    // Only consider amenities which are in the same assorted cluster as the amenity where the passenger
                    // came from
                    Station.AmenityCluster amenityCluster
                            = currentStation.getAmenityClusterByAmenityAssorted().get(amenity);

                    if (amenityCluster.equals(originAmenityCluster)) {
                        // Only considered enabled amenities
                        NonObstacle nonObstacle = ((NonObstacle) amenity);

                        // Only consider enabled amenities
                        if (!nonObstacle.isEnabled()) {
                            continue;
                        }

                        // Filter the amenity search space only to what is compatible with this passenger
                        if (amenity instanceof StationGate) {
                            // If the goal of the passenger is a station gate, this means the passenger is leaving
                            // So only consider station gates which allow exits and accepts the passenger's direction
                            StationGate stationGateExit = ((StationGate) amenity);

                            if (stationGateExit.getStationGateMode() == StationGate.StationGateMode.ENTRANCE) {
                                continue;
                            } else {
                                if (
                                        !stationGateExit.getStationGatePassengerTravelDirections().contains(
                                                this.travelDirection
                                        )
                                ) {
                                    continue;
                                }
                            }
                        } else if (amenity instanceof Turnstile) {
                            // Only consider turnstiles which match this passenger's disposition and travel direction
                            Turnstile turnstile = ((Turnstile) amenity);

                            if (!turnstile.getTurnstileTravelDirections().contains(this.travelDirection)) {
                                continue;
                            }

                            if (turnstile.getTurnstileMode() != Turnstile.TurnstileMode.BIDIRECTIONAL) {
                                if (
                                        turnstile.getTurnstileMode() == Turnstile.TurnstileMode.BOARDING
                                                && disposition.equals(Disposition.ALIGHTING)
                                                || turnstile.getTurnstileMode() == Turnstile.TurnstileMode.ALIGHTING
                                                && disposition.equals(Disposition.BOARDING)
                                ) {
                                    continue;
                                }
                            }
                        } else if (amenity instanceof TrainDoor) {
                            // Only consider train doors which match this passenger's travel direction
                            TrainDoor trainDoor = ((TrainDoor) amenity);

                            if (trainDoor.getPlatformDirection() != this.travelDirection) {
                                continue;
                            }

                            // Also, if the train door has a female-only restriction, make sure this passenger is also
                            // female
                            if (trainDoor.isFemaleOnly() && this.parent.getGender() != Passenger.PassengerInformation.Gender.FEMALE) {
                                continue;
                            }
                        }

                        // Compute the distance to each attractor
                        for (Amenity.AmenityBlock attractor : amenity.getAttractors()) {
                            double distanceToAttractor = Coordinates.distance(
                                    currentStation,
                                    this.currentPatch,
                                    attractor.getPatch()
                            );

                            distancesToAttractors.put(attractor, distanceToAttractor);
                        }
                    }
                }

                double minimumAttractorScore = Double.MAX_VALUE;

                // Then for each compiled amenity and their distance from this passenger, see which has the smallest
                // distance while taking into account the passengers queueing for that amenity, if any
                for (
                        Map.Entry<Amenity.AmenityBlock, Double> distancesToAttractorEntry
                        : distancesToAttractors.entrySet()
                ) {
                    Amenity.AmenityBlock candidateAttractor = distancesToAttractorEntry.getKey();
                    Double candidateDistance = distancesToAttractorEntry.getValue();

                    Amenity currentAmenity;
                    QueueObject currentQueueObject;
                    TrainDoor.TrainDoorEntranceLocation currentTrainDoorEntranceLocation = null;

                    List<QueueObject> turnstileQueueObjects = new ArrayList<>();

                    currentAmenity = candidateAttractor.getParent();

                    // Only collect queue objects from queueables
                    if (currentAmenity instanceof Queueable) {
                        if (currentAmenity instanceof Turnstile) {
                            Turnstile turnstile = ((Turnstile) currentAmenity);

                            currentQueueObject
                                    = turnstile.getQueueObjects().get(this.disposition);

                            turnstileQueueObjects.addAll(turnstile.getQueueObjects().values());

//                            turnstileQueueObjects = (List<QueueObject>) turnstile.getQueueObjects().values();
                        } else if (currentAmenity instanceof TrainDoor) {
                            TrainDoor trainDoor = ((TrainDoor) currentAmenity);

                            currentTrainDoorEntranceLocation
                                    = trainDoor.getTrainDoorEntranceLocationFromAttractor(candidateAttractor);
                            currentQueueObject
                                    = trainDoor.getQueueObjectFromTrainDoorEntranceLocation(
                                    currentTrainDoorEntranceLocation
                            );
                        } else {
                            Queueable queueable = ((Queueable) currentAmenity);

                            currentQueueObject = queueable.getQueueObject();
                        }
                    } else {
                        currentQueueObject = null;
                    }

                    // If this is a queueable, take into account the passengers queueing (except if it is a security gate)
                    // If this is not a queueable (or if it's a security gate), the distance will suffice
                    double attractorScore;

                    if (currentQueueObject != null) {
                        if (!(currentAmenity instanceof Security)) {
                            // Avoid queueing to long lines
                            double passengerPenalty = (currentAmenity instanceof TrainDoor) ? 50.0 : 25.0;

                            if (currentAmenity instanceof Turnstile) {
                                double passengersQueueingForTurnstile = 0.0;

                                for (QueueObject queueObject : turnstileQueueObjects) {
                                    passengersQueueingForTurnstile += queueObject.getPassengersQueueing().size();
                                }

                                double modifiedCandidateDistance = candidateDistance;

                                final double candidateDistanceLimit = 15.0;
                                final double distantCandidatePenalty = 100.0;

                                if (modifiedCandidateDistance > candidateDistanceLimit) {
                                    modifiedCandidateDistance = candidateDistance * distantCandidatePenalty;
                                }

                                attractorScore
                                        = modifiedCandidateDistance
                                        + passengersQueueingForTurnstile * passengerPenalty;
                            } else {
                                attractorScore
                                        = candidateDistance
                                        + currentQueueObject.getPassengersQueueing().size() * passengerPenalty;
                            }
                        } else {
                            attractorScore = candidateDistance;
                        }
                    } else {
                        attractorScore = candidateDistance;
                    }

                    if (attractorScore < minimumAttractorScore) {
                        minimumAttractorScore = attractorScore;

                        chosenAmenity = currentAmenity;
                        chosenQueueObject = currentQueueObject;
                        chosenAttractor = candidateAttractor;
                        chosenTrainDoorEntranceLocation = currentTrainDoorEntranceLocation;
                    }
                }

                // If no amenities in this floor were found to have a path from this passenger, seek the portals instead
                if (chosenAmenity == null) {
                    willSeekPortal = true;
                } else {
                    // Set the goal nearest to this passenger
                    this.goalAmenity = chosenAmenity;
                    this.goalQueueObject = chosenQueueObject;
                    this.goalAttractor = chosenAttractor;
                    this.goalPatch = chosenAttractor.getPatch();
                    this.goalTrainDoorEntranceLocation = chosenTrainDoorEntranceLocation;
                }
            }

            if (willSeekPortal) {
                // Get the nearest relevant portal to this passenger
                Portal.Directory.DirectoryItem directoryItemOfPassenger = new Portal.Directory.DirectoryItem(
                        this.travelDirection,
                        nextAmenityClass,
                        currentStation.getAmenityClusterByAmenity().get(this.currentAmenity),
                        null,
                        0.0
                );

                TreeMap<Double, Portal> relevantPortals = new TreeMap<>();

                // TODO: Consider other portals

                // Compile the stair portals that serve this floor
                List<StairShaft> stairShafts = currentStation.getStairShafts();

                double distanceToPortal;
                double distanceToTraverse;
                double peopleInPortalScore;

                double portalScore;

                for (StairShaft stairShaft : stairShafts) {
                    StairPortal lowerStairPortal = (StairPortal) stairShaft.getLowerPortal();
                    StairPortal upperStairPortal = (StairPortal) stairShaft.getUpperPortal();

                    // Only consider amenities which are in the same assorted cluster as the amenity where the passenger
                    // came from
                    Station.AmenityCluster lowerAmenityCluster
                            = currentStation.getAmenityClusterByAmenityAssorted().get(lowerStairPortal);

                    Station.AmenityCluster upperAmenityCluster
                            = currentStation.getAmenityClusterByAmenityAssorted().get(upperStairPortal);

                    if (lowerStairPortal.getFloorServed().equals(currentFloor)) {
                        if (lowerAmenityCluster.equals(originAmenityCluster)) {
                            Portal.Directory.DirectoryItem directoryItemInPortal
                                    = lowerStairPortal.getDirectory().get(directoryItemOfPassenger);

                            if (directoryItemInPortal != null) {
//                                // Only consider attractors in amenities which are accessible from the current position
//                                PassengerPath path = computePathWithinFloor(
//                                        this.currentPatch,
//                                        lowerStairPortal.getAttractors().get(0).getPatch(),
//                                        true,
//                                        false
//                                );
//
//                                if (path != null) {
                                // Other than merely considering the distance to the portal, also take into account the
                                // distance it would take to reach the goal through the portal, as well as the number of
                                // people already in that portal
                                distanceToPortal = Coordinates.distance(
                                        currentStation,
                                        this.currentPatch,
                                        lowerStairPortal.getAttractors().get(0).getPatch()
                                );

                                distanceToTraverse = directoryItemInPortal.getDistance();

                                final double peopleInPortalPenalty = 10.0;
                                peopleInPortalScore
                                        = lowerStairPortal.getStairShaft().getPassengersAscending()
                                        * peopleInPortalPenalty;

                                portalScore = distanceToPortal + distanceToTraverse + peopleInPortalScore;

                                relevantPortals.put(
                                        portalScore,
                                        lowerStairPortal
                                );
                            }
//                            }
                        }
                    }

                    if (upperStairPortal.getFloorServed().equals(currentFloor)) {
                        if (upperAmenityCluster.equals(originAmenityCluster)) {
                            Portal.Directory.DirectoryItem directoryItemInPortal
                                    = upperStairPortal.getDirectory().get(directoryItemOfPassenger);

                            if (directoryItemInPortal != null) {
//                                // Only consider attractors in amenities which are accessible from the current position
//                                PassengerPath path = computePathWithinFloor(
//                                        this.currentPatch,
//                                        upperStairPortal.getAttractors().get(0).getPatch(),
//                                        true,
//                                        false
//                                );
//
//                                if (path != null) {
                                // Other than merely considering the distance to the portal, also take into account the
                                // distance it would take to reach the goal through the portal
                                distanceToPortal = Coordinates.distance(
                                        currentStation,
                                        this.currentPatch,
                                        upperStairPortal.getAttractors().get(0).getPatch()
                                );

                                distanceToTraverse = directoryItemInPortal.getDistance();

                                final double peopleInPortalPenalty = 10.0;
                                peopleInPortalScore
                                        = upperStairPortal.getStairShaft().getPassengersDescending()
                                        * peopleInPortalPenalty;

                                portalScore = distanceToPortal + distanceToTraverse + peopleInPortalScore;

                                relevantPortals.put(
                                        portalScore,
                                        upperStairPortal
                                );
//                                }
                            }
                        }
                    }
                }

                // Compile the escalator portals that serve this floor
                List<EscalatorShaft> escalatorShafts = currentStation.getEscalatorShafts();

                for (EscalatorShaft escalatorShaft : escalatorShafts) {
                    EscalatorPortal lowerEscalatorPortal = (EscalatorPortal) escalatorShaft.getLowerPortal();
                    EscalatorPortal upperEscalatorPortal = (EscalatorPortal) escalatorShaft.getUpperPortal();

                    // Only consider amenities which are in the same assorted cluster as the amenity where the passenger
                    // came from
                    Station.AmenityCluster lowerAmenityCluster
                            = currentStation.getAmenityClusterByAmenityAssorted().get(lowerEscalatorPortal);

                    Station.AmenityCluster upperAmenityCluster
                            = currentStation.getAmenityClusterByAmenityAssorted().get(upperEscalatorPortal);

                    if (lowerEscalatorPortal.getFloorServed().equals(currentFloor)) {
                        if (lowerAmenityCluster.equals(originAmenityCluster)) {
                            Portal.Directory.DirectoryItem directoryItemInPortal
                                    = lowerEscalatorPortal.getDirectory().get(directoryItemOfPassenger);

                            if (directoryItemInPortal != null) {
//                                // Only consider attractors in amenities which are accessible from the current position
//                                PassengerPath path = computePathWithinFloor(
//                                        this.currentPatch,
//                                        lowerEscalatorPortal.getAttractors().get(0).getPatch(),
//                                        true,
//                                        false
//                                );
//
//                                if (path != null) {
                                // Other than merely considering the distance to the portal, also take into account the
                                // distance it would take to reach the goal through the portal
                                distanceToPortal = Coordinates.distance(
                                        currentStation,
                                        this.currentPatch,
                                        lowerEscalatorPortal.getAttractors().get(0).getPatch()
                                );

                                distanceToTraverse = directoryItemInPortal.getDistance();

                                final double peopleInPortalPenalty = 10.0;
                                peopleInPortalScore
                                        = lowerEscalatorPortal.getEscalatorShaft().getPassengers()
                                        * peopleInPortalPenalty;

                                portalScore = distanceToPortal + distanceToTraverse + peopleInPortalScore;

                                relevantPortals.put(
                                        portalScore,
                                        lowerEscalatorPortal
                                );
//                                }
                            }
                        }
                    }

                    if (upperAmenityCluster.equals(originAmenityCluster)) {
                        if (upperEscalatorPortal.getFloorServed().equals(currentFloor)) {
                            Portal.Directory.DirectoryItem directoryItemInPortal
                                    = upperEscalatorPortal.getDirectory().get(directoryItemOfPassenger);

                            if (directoryItemInPortal != null) {
//                                // Only consider attractors in amenities which are accessible from the current position
//                                PassengerPath path = computePathWithinFloor(
//                                        this.currentPatch,
//                                        upperEscalatorPortal.getAttractors().get(0).getPatch(),
//                                        true,
//                                        false
//                                );
//
//                                if (path != null) {
                                // Other than merely considering the distance to the portal, also take into account the
                                // distance it would take to reach the goal through the portal
                                distanceToPortal = Coordinates.distance(
                                        currentStation,
                                        this.currentPatch,
                                        upperEscalatorPortal.getAttractors().get(0).getPatch()
                                );

                                distanceToTraverse = directoryItemInPortal.getDistance();

                                final double peopleInPortalPenalty = 10.0;
                                peopleInPortalScore
                                        = upperEscalatorPortal.getEscalatorShaft().getPassengers()
                                        * peopleInPortalPenalty;

                                portalScore = distanceToPortal + distanceToTraverse + peopleInPortalScore;

                                relevantPortals.put(
                                        portalScore,
                                        upperEscalatorPortal
                                );
//                                }
                            }
                        }
                    }
                }

                Portal closestPortal;

                // Get the closest portal
                closestPortal = relevantPortals.firstEntry().getValue();

                // Set the closest portal as this passenger's goal
                this.goalAmenity = closestPortal;
                this.goalQueueObject = null;
                this.goalAttractor = closestPortal.getAttractors().get(0);
                this.goalPatch = this.goalAttractor.getPatch();
                this.goalTrainDoorEntranceLocation = null;

                this.goalPortal = (Portal) this.goalAmenity;
                this.goalFloor = this.goalPortal.getPair().getFloorServed();
            }
        }
    }

    // Get the future position of this passenger given the current goal, current heading, and the current walking
    // distance
    private Coordinates getFuturePosition() {
        return getFuturePosition(this.goalAmenity, this.proposedHeading, this.preferredWalkingDistance);
    }

    // Get the future position of this passenger given the current goal, current heading, and a given walking distance
    private Coordinates getFuturePosition(double walkingDistance) {
        return getFuturePosition(this.goalAmenity, this.proposedHeading, walkingDistance);
    }

    public Coordinates getFuturePosition(Coordinates startingPosition, double heading, double magnitude) {
        return Coordinates.computeFuturePosition(startingPosition, heading, magnitude);
    }

    // Get the future position of this passenger given a goal and a heading
    public Coordinates getFuturePosition(Amenity goal, double heading, double walkingDistance) {
        // Get the goal's floor
        Floor goalFloor = goal.getAmenityBlocks().get(0).getPatch().getFloor();

        // Get the nearest attractor to this passenger
        double minimumDistance = Double.MAX_VALUE;
        double distance;

        Amenity.AmenityBlock nearestAttractor = null;

        for (Amenity.AmenityBlock attractor : goal.getAttractors()) {
            distance = Coordinates.distance(this.position, attractor.getPatch().getPatchCenterCoordinates());

            if (distance < minimumDistance) {
                minimumDistance = distance;
                nearestAttractor = attractor;
            }
        }

        assert nearestAttractor != null;

        // If the distance between this passenger and the goal is less than the distance this passenger covers every
        // time it walks, "snap" the position of the passenger to the center of the goal immediately, to avoid
        // overshooting its target
        // If not, compute the next coordinates normally
        if (minimumDistance < walkingDistance) {
            return new Coordinates(
                    nearestAttractor.getPatch().getPatchCenterCoordinates().getX(),
                    nearestAttractor.getPatch().getPatchCenterCoordinates().getY()
            );
        } else {
            Coordinates futurePosition = this.getFuturePosition(
                    this.position,
                    heading,
                    walkingDistance
            );

            double newX = futurePosition.getX();
            double newY = futurePosition.getY();

            // Check if the new coordinates are out of bounds
            // If they are, adjust them such that they stay within bounds
            if (newX < 0) {
                newX = 0.0;
            } else if (newX > goalFloor.getColumns() - 1) {
                newX = goalFloor.getColumns() - 0.5;
            }

            if (newY < 0) {
                newY = 0.0;
            } else if (newY > goalFloor.getRows() - 1) {
                newY = goalFloor.getRows() - 0.5;
            }

            return new Coordinates(newX, newY);
        }
    }

    // Make the passenger move in accordance with social forces
    public boolean moveSocialForce() {
        // The smallest repulsion a passenger may inflict on another
        final double minimumPassengerRepulsion = 0.01 * this.preferredWalkingDistance;

        // The smallest repulsion an obstacle may inflict to a passenger
//        final double minimumObstacleRepulsion = 0.01 * this.preferredWalkingDistance;

        // If the passenger has not moved a sufficient distance for more than this number of ticks, the passenger
        // will be considered stuck
        final int noMovementTicksThreshold = (this.getGoalAmenityAsGoal() != null) ? this.getGoalAmenityAsGoal().getWaitingTime() : 5;

        // If the passenger has not seen new patches for more than this number of ticks, the passenger will be considered
        // stuck
        final int noNewPatchesSeenTicksThreshold = 5;

        // If the passenger has been moving a sufficient distance for at least this number of ticks, this passenger will
        // be out of the stuck state, if it was
        final int unstuckTicksThreshold = 60;

        // If the distance the passenger moves per tick is less than this distance, this passenger is considered to not
        // have moved
        final double noMovementThreshold = 0.01 * this.preferredWalkingDistance;

        // If the size of the passenger's memory of recent patches is less than this number, the passenger is considered
        // to not have moved
        final double noNewPatchesSeenThreshold = 5;

        // The distance to another passenger before this passenger slows down
        final double slowdownStartDistance = 2.0;

        // The minimum allowable distance from another passenger at its front before this passenger stops
        final double minimumStopDistance = 0.6;

        // The maximum allowable distance from another passenger at its front before this passenger stops
        double maximumStopDistance = 1.0;

        // Count the number of passengers and obstacles in the the relevant patches
        int numberOfPassengers = 0;
        int numberOfObstacles = 0;

        // The distance from the passenger's center by which repulsive effects from passengers start to occur
        double maximumPassengerStopDistance = 1.0;

        // The distance from the passenger's center by which repulsive effects from passengers are at a maximum
        final double minimumPassengerStopDistance = 0.6;

        // The distance from the passenger's center by which repulsive effects from obstacles start to occur
        double maximumObstacleStopDistance = 1.0;

        // The distance from the passenger's center by which repulsive effects from obstacles are at a maximum
        final double minimumObstacleStopDistance = 0.6;

        // Get the relevant patches
//        List<Patch> patchesToExplore
//                = Floor.get7x7Field(
//                this.currentFloor,
//                this.currentPatch,
//                this.proposedHeading,
//                true,
//                Math.toRadians(360.0)
//        );

        List<Patch> patchesToExplore = this.get7x7Field(
                this.proposedHeading,
                true,
                Math.toRadians(360.0)
        );

//        this.toExplore = patchesToExplore;

        // Clear vectors from the previous computations
        this.repulsiveForceFromPassengers.clear();
        this.repulsiveForcesFromObstacles.clear();
        this.attractiveForce = null;
        this.motivationForce = null;

        // Add the repulsive effects from nearby passengers and obstacles
        TreeMap<Double, Amenity.AmenityBlock> obstaclesEncountered = new TreeMap<>();

        // This will contain the final motivation vector
        List<Vector> vectorsToAdd = new ArrayList<>();

        // Get the current heading, which will be the previous heading later
        this.previousHeading = this.heading;

        // Compute the proposed future position
        Coordinates proposedNewPosition;

/*        double accelerationFactor;

//        if (this.timeSinceLeftPreviousGoal <= this.ticksUntilFullyAccelerated) {
        accelerationFactor = Math.sqrt(this.ticksAcceleratedOrMaintainedSpeed + 1) / (Math.sqrt(this.ticksUntilFullyAccelerated));
*//*        } else {
            accelerationFactor = Math.sqrt(this.newPatchesSeenCounter + 1) / (Math.sqrt(ticksUntilFullyAccelerated));
        }*//*

        System.out.println("this.currentWalkingDistance < previousWalkingDistance: " + this.currentWalkingDistance + ", " + this.ticksAcceleratedOrMaintainedSpeed + ": " + accelerationFactor);

        accelerationFactor = Math.min(accelerationFactor, 1.0);

        proposedNewPosition = this.getFuturePosition(
                accelerationFactor
                        * this.preferredWalkingDistance
        );*/

        // Check if the passenger is set to take one initial step forward
        if (!this.shouldStepForward) {
            // Compute for the proposed future position
            proposedNewPosition = this.getFuturePosition(this.preferredWalkingDistance);

            boolean willEnterTrain = this.isNextAmenityTrainDoor() && this.willEnterTrain();

            if (willEnterTrain && this.shouldStopAtPlatform) {
                this.shouldStopAtPlatform = false;
            }

            if (!this.shouldStopAtPlatform) {
                // If the goal is a train door, and it is open, and the passenger is not waiting yet, walk faster
                if (
                        this.getGoalAmenityAsTrainDoor() != null
                                && this.getGoalAmenityAsTrainDoor().isOpen()
                                && this.action != Action.WAITING_FOR_TRAIN
                ) {
                    final double speedIncreaseFactor = 1.25;

                    this.preferredWalkingDistance = this.baseWalkingDistance * speedIncreaseFactor;
                } else {
                    // If the goal is not a train door, or it is, but it's closed, walk normally
                    this.preferredWalkingDistance = this.baseWalkingDistance;
                }

                // If the passenger is near its goal that is not a train door, and this passenger has a clear line of
                // sight to that goal, walk slower
                final double distanceSlowdownStart = 5.0;
                final double speedDecreaseFactor = 0.5;

                double distanceToGoal = Coordinates.distance(
                        this.currentFloor.getStation(),
                        this.currentPatch,
                        this.getGoalAmenity().getAttractors().get(0).getPatch()
                );

                if (
                        this.getGoalAmenityAsTrainDoor() == null
                                && distanceToGoal < distanceSlowdownStart
                                && this.hasClearLineOfSight(
                                this.position,
                                this.goalAmenity.getAttractors().get(0).getPatch().getPatchCenterCoordinates(),
                                true
                        )
                ) {
                    this.preferredWalkingDistance *= speedDecreaseFactor;
                }

                // If this passenger is queueing, the only social forces that apply are attractive forces to passengers
                // and obstacles (if not in queueing action)
                if (
                        !willEnterTrain && this.state == State.IN_QUEUE || this.isWaitingOnPortal
                ) {
                    // Do not check for stuckness when already heading to the queueable
                    if (this.action != Action.HEADING_TO_QUEUEABLE && !this.isWaitingOnPortal) {
                        // If the passenger hasn't already been moving for a while, consider the passenger stuck, and implement some
                        // measures to free this passenger
                        if (
                                this.isStuck
                                        || (
                                        this.action != Action.WAITING_FOR_TRAIN
                                                && this.hasNoPassenger(
                                                this.goalAttractor.getPatch()
                                        ) && (
                                                this.isAtQueueFront() || this.isServicedByGoal()
                                        )
                                ) && this.noMovementCounter > noMovementTicksThreshold
                        ) {
                            this.isStuck = true;
                            this.stuckCounter++;
                        }
                    }

                    // Get the passengers within the current field of view in these patches
                    // If there are any other passengers within this field of view, this passenger is at least guaranteed to
                    // slow down
                    TreeMap<Double, Passenger> passengersWithinFieldOfView = new TreeMap<>();

                    // Look around the patches that fall on the passenger's field of view
                    for (Patch patch : patchesToExplore) {
                        // Do not apply social forces from obstacles if the passenger is in the queueing action, i.e., when the
                        // passenger is following a floor field
                        // If this patch has an obstacle, take note of it to add a repulsive force from it later
                        if (this.action != Action.QUEUEING) {
                            Amenity.AmenityBlock patchAmenityBlock = patch.getAmenityBlock();

                            // Get the distance between this passenger and the obstacle on this patch
                            if (hasObstacle(patch)) {
                                // Take note of the obstacle density in this area
                                numberOfObstacles++;

                                // If the distance is less than or equal to the specified minimum repulsion distance, compute
                                // for the magnitude of the repulsion force
                                double distanceToObstacle = Coordinates.distance(
                                        this.position,
                                        patchAmenityBlock.getPatch().getPatchCenterCoordinates()
                                );

                                if (distanceToObstacle <= slowdownStartDistance) {
                                    obstaclesEncountered.put(distanceToObstacle, patchAmenityBlock);
                                }
                            }
                        }

                        if (!this.isStuck) {
                            for (Passenger otherPassenger : patch.getPassengers()) {
                                // Make sure that the passenger discovered isn't itself
                                if (!otherPassenger.equals(this.getParent())) {
                                    if (allowRepulsionFrom(otherPassenger)) {
                                        // Take note of the passenger density in this area
                                        numberOfPassengers++;

                                        // Check if this passenger is within the field of view and within the slowdown
                                        // distance
                                        double distanceToPassenger = Coordinates.distance(
                                                this.position,
                                                otherPassenger.getPassengerMovement().getPosition()
                                        );

                                        if (Coordinates.isWithinFieldOfView(
                                                this.position,
                                                otherPassenger.getPassengerMovement().getPosition(),
                                                this.proposedHeading,
                                                this.fieldOfViewAngle)
                                                && distanceToPassenger <= slowdownStartDistance) {
                                            passengersWithinFieldOfView.put(distanceToPassenger, otherPassenger);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Compute the perceived density of the passengers
                    // Assuming the maximum density a passenger sees within its environment is 3 before it thinks the crowd
                    // is very dense, rate the perceived density of the surroundings by dividing the number of people by the
                    // maximum tolerated number of passengers
                    final double maximumDensityTolerated = 3.0;
                    final double passengerDensity
                            = (numberOfPassengers > maximumDensityTolerated ? maximumDensityTolerated : numberOfPassengers)
                            / maximumDensityTolerated;

                    // For each passenger found within the slowdown distance, get the nearest one, if there is any
                    Map.Entry<Double, Passenger> nearestPassengerEntry = passengersWithinFieldOfView.firstEntry();

                    // If there are no passengers within the field of view, good - move normally
                    if (nearestPassengerEntry == null/*|| nearestPassengerEntry.getValue().getPassengerMovement().getGoalAmenity() != null && !nearestPassengerEntry.getValue().getPassengerMovement().getGoalAmenity().equals(this.goalAmenity)*/) {
                        this.hasEncounteredPassengerToFollow = this.passengerFollowedWhenAssembling != null;

                        // Get the attractive force of this passenger to the new position
                        this.attractiveForce = this.computeAttractiveForce(
                                new Coordinates(this.position),
                                this.proposedHeading,
                                proposedNewPosition,
                                this.preferredWalkingDistance
                        );

                        vectorsToAdd.add(attractiveForce);
                    } else {
                        // Get a random (but weighted) floor field value around the other passenger
                        Patch floorFieldPatch = this.getBestQueueingPatchAroundPassenger(
                                nearestPassengerEntry.getValue()
                        );
//                        this.chosenQueueingPatch = floorFieldPatch;

                        // Check the distance of that nearest passenger to this passenger
                        double distanceToNearestPassenger = nearestPassengerEntry.getKey();

                        // Modify the maximum stopping distance depending on the density of the environment
                        // That is, the denser the surroundings, the less space this passenger will allow between other
                        // passengers
                        maximumStopDistance -= (maximumStopDistance - minimumStopDistance) * passengerDensity;

                        this.hasEncounteredPassengerToFollow = this.passengerFollowedWhenAssembling != null;

                        // Else, just slow down and move towards the direction of that passenger in front
                        // The slowdown factor linearly depends on the distance between this passenger and the other
                        final double slowdownFactor
                                = (distanceToNearestPassenger - maximumStopDistance)
                                / (slowdownStartDistance - maximumStopDistance);

                        double computedWalkingDistance = slowdownFactor * this.preferredWalkingDistance;

                        if (this.isNextAmenityTrainDoor() && floorFieldPatch != null) {
                            Double floorFieldValue = null;
                            Map<QueueingFloorField.FloorFieldState, Double> floorFieldValues
                                    = floorFieldPatch.getFloorFieldValues().get(this.getGoalAmenityAsQueueable());

                            if (floorFieldValues != null) {
                                floorFieldValue = floorFieldValues.get(this.goalFloorFieldState);
                            }

                            if (
                                    floorFieldValue != null
                                            && Simulator.RANDOM_NUMBER_GENERATOR.nextDouble() < floorFieldValue
                            ) {
                                this.shouldStopAtPlatform = true;
                            } else {
                                // Only head towards that patch if the distance from that patch to the goal is further than the
                                // distance from this passenger to the goal
                                double distanceFromChosenPatchToGoal = Coordinates.distance(
                                        this.currentFloor.getStation(),
                                        floorFieldPatch,
                                        this.goalAttractor.getPatch()
                                );

                                double distanceFromThisPassengerToGoal = Coordinates.distance(
                                        this.currentFloor.getStation(),
                                        this.currentPatch,
                                        this.goalAttractor.getPatch()
                                );

                                double revisedHeading;
                                Coordinates revisedPosition;

                                if (distanceFromChosenPatchToGoal < distanceFromThisPassengerToGoal) {
                                    if (!this.getGoalAmenityAsTrainDoor().isOpen()) {
                                        // Get the heading towards that patch
                                        revisedHeading = Coordinates.headingTowards(
                                                this.position,
                                                floorFieldPatch.getPatchCenterCoordinates()
                                        );
                                    } else {
                                        revisedHeading = Coordinates.headingTowards(
                                                this.position,
                                                this.goalAttractor.getPatch().getPatchCenterCoordinates()
                                        );
                                    }

                                    revisedPosition = this.getFuturePosition(
                                            this.position,
                                            revisedHeading,
                                            computedWalkingDistance
                                    );

                                    // Get the attractive force of this passenger to the new position
                                    this.attractiveForce = this.computeAttractiveForce(
                                            new Coordinates(this.position),
                                            revisedHeading,
                                            revisedPosition,
                                            computedWalkingDistance
                                    );

                                    vectorsToAdd.add(attractiveForce);

                                    for (
                                            Map.Entry<Double, Passenger> otherPassengerEntry
                                            : passengersWithinFieldOfView.entrySet()
                                    ) {
                                        // Then compute the repulsive force from this passenger
                                        // Compute the perceived density of the passengers
                                        // Assuming the maximum density a passenger sees within its environment is 5 before it thinks the crowd
                                        // is very dense, rate the perceived density of the surroundings by dividing the number of people by the
                                        // maximum tolerated number of passengers
                                        final int maximumPassengerCountTolerated = 5;

                                        // The distance by which the repulsion starts to kick in will depend on the density of the passenger's
                                        // surroundings
                                        final int minimumPassengerCount = 1;
                                        final double maximumDistance = 2.0;
                                        final int maximumPassengerCount = 5;
                                        final double minimumDistance = 0.7;

                                        double computedMaximumDistance = computeMaximumRepulsionDistance(
                                                numberOfObstacles,
                                                maximumPassengerCountTolerated,
                                                minimumPassengerCount,
                                                maximumDistance,
                                                maximumPassengerCount,
                                                minimumDistance
                                        );

                                        Vector passengerRepulsiveForce = computeSocialForceFromPassenger(
                                                otherPassengerEntry.getValue(),
                                                otherPassengerEntry.getKey(),
                                                computedMaximumDistance,
                                                minimumPassengerStopDistance,
                                                this.preferredWalkingDistance
                                        );

                                        // Add the computed vector to the list of vectors
                                        this.repulsiveForceFromPassengers.add(passengerRepulsiveForce);
                                    }
                                }
                            }
                        } else {
                            Coordinates revisedPosition = this.getFuturePosition(computedWalkingDistance);

                            // Get the attractive force of this passenger to the new position
                            this.attractiveForce = this.computeAttractiveForce(
                                    new Coordinates(this.position),
                                    this.proposedHeading,
                                    revisedPosition,
                                    computedWalkingDistance
                            );

                            vectorsToAdd.add(attractiveForce);
                        }
                    }
                } else {
                    // If the passenger hasn't already been moving for a while, consider the passenger stuck, and implement some
                    // measures to free this passenger
                    if (
                            this.isStuck || this.noNewPatchesSeenCounter > noNewPatchesSeenTicksThreshold
                    ) {
                        this.isStuck = true;
                        this.stuckCounter++;
                    }

                    boolean hasEncounteredQueueingPassengerInLoop = false;
                    boolean hasEncounteredPortalWaitingPassengerInLoop = false;

                    // Only apply the social forces of a set number of passengers and obstacles
                    int passengersProcessed = 0;
                    final int passengersProcessedLimit = 5;

                    // Look around the patches that fall on the passenger's field of view
                    for (Patch patch : patchesToExplore) {
                        // If this patch has an obstacle, take note of it to add a repulsive force from it later
                        Amenity.AmenityBlock patchAmenityBlock = patch.getAmenityBlock();

                        // Get the distance between this passenger and the obstacle on this patch
                        if (hasObstacle(patch)) {
                            // Take note of the obstacle density in this area
                            numberOfObstacles++;

                            // If the distance is less than or equal to the specified minimum repulsion distance, compute
                            // for the magnitude of the repulsion force
                            double distanceToObstacle = Coordinates.distance(
                                    this.position,
                                    patchAmenityBlock.getPatch().getPatchCenterCoordinates()
                            );

                            if (distanceToObstacle <= slowdownStartDistance) {
                                obstaclesEncountered.put(distanceToObstacle, patchAmenityBlock);
                            }
                        }

                        // Inspect each passenger in each patch in the patches in the field of view
                        for (Passenger otherPassenger : patch.getPassengers()) {
                            if (passengersProcessed == passengersProcessedLimit) {
                                break;
                            }

                            // Make sure that the passenger discovered isn't itself
                            if (!otherPassenger.equals(this.getParent())) {
                                // Take note of the passenger density in this area
                                numberOfPassengers++;

                                // Get the distance between this passenger and the other passenger
                                double distanceToOtherPassenger = Coordinates.distance(
                                        this.position,
                                        otherPassenger.getPassengerMovement().getPosition()
                                );

                                // If the distance is less than or equal to the distance when repulsion is supposed to kick in,
                                // compute for the magnitude of that repulsion force
                                if (distanceToOtherPassenger <= slowdownStartDistance) {
                                    // Compute the perceived density of the passengers
                                    // Assuming the maximum density a passenger sees within its environment is 3 before it thinks the crowd
                                    // is very dense, rate the perceived density of the surroundings by dividing the number of people by the
                                    // maximum tolerated number of passengers
                                    final int maximumPassengerCountTolerated = 5;

                                    // The distance by which the repulsion starts to kick in will depend on the density of the passenger's
                                    // surroundings
                                    final int minimumPassengerCount = 1;
                                    final double maximumDistance = 2.0;
                                    final int maximumPassengerCount = 5;
                                    final double minimumDistance = 0.7;

                                    double computedMaximumDistance = computeMaximumRepulsionDistance(
                                            numberOfObstacles,
                                            maximumPassengerCountTolerated,
                                            minimumPassengerCount,
                                            maximumDistance,
                                            maximumPassengerCount,
                                            minimumDistance
                                    );

                                    Vector passengerRepulsiveForce = computeSocialForceFromPassenger(
                                            otherPassenger,
                                            distanceToOtherPassenger,
                                            computedMaximumDistance,
                                            minimumPassengerStopDistance,
                                            this.preferredWalkingDistance
                                    );

                                    // Add the computed vector to the list of vectors
                                    this.repulsiveForceFromPassengers.add(passengerRepulsiveForce);

                                    // Also, check this passenger's state
                                    // If this passenger is queueing, set the relevant variable - it will stay true even if just
                                    // one nearby passenger has activated it
                                    if (!hasEncounteredQueueingPassengerInLoop) {
                                        // Check if the other passenger is in a queueing or assembling with the same goal as
                                        // this passenger
                                        if (this.passengerFollowedWhenAssembling == null) {
                                            this.hasEncounteredPassengerToFollow = false;
                                        } else {
                                            if (this.passengerFollowedWhenAssembling.equals(otherPassenger)) {
                                                // If the other passenger encountered is already assembling, decide whether this
                                                // passenger will assemble too depending on whether the other passenger was selected
                                                // to be followed by this one
                                                this.hasEncounteredPassengerToFollow
                                                        = (otherPassenger.getPassengerMovement().getAction() == Action.ASSEMBLING
                                                        || otherPassenger.getPassengerMovement().getAction() == Action.QUEUEING)
                                                        && otherPassenger.getPassengerMovement().getGoalAmenity().equals(this.goalAmenity);
                                            } else {
                                                this.hasEncounteredPassengerToFollow = false;
                                            }
                                        }
                                    }

                                    hasEncounteredQueueingPassengerInLoop
                                            = this.hasEncounteredPassengerToFollow;

                                    // Check if this passenger has encountered a passenger waiting for the same portal
                                    if (!hasEncounteredPortalWaitingPassengerInLoop) {
                                        // If the other passenger encountered is already assembling, decide whether this
                                        // passenger will assemble too depending on whether the other passenger was selected
                                        // to be followed by this one
                                        this.hasEncounteredPortalWaitingPassenger
                                                = otherPassenger.getPassengerMovement().isWaitingOnPortal()
                                                && otherPassenger.getPassengerMovement().getGoalAmenity().equals(this.goalAmenity);
                                    }

                                    hasEncounteredPortalWaitingPassengerInLoop
                                            = this.hasEncounteredPortalWaitingPassenger;

//                                    this.isWaitingOnPortal
//                                            = this.isWaitingOnPortal || hasEncounteredPortalWaitingPassengerInLoop;

                                    // If a queueing passenger has been encountered, do not pathfind anymore for this
                                    // goal
                                    if (
                                            this.parent.getTicketType() == TicketBooth.TicketType.STORED_VALUE
                                                    && this.hasEncounteredPassengerToFollow
                                    ) {
                                        this.hasPathfound = true;
                                    }

                                    passengersProcessed++;
                                }
                            }
                        }
                    }

                    // Get the attractive force of this passenger to the new position
                    this.attractiveForce = this.computeAttractiveForce(
                            new Coordinates(this.position),
                            this.proposedHeading,
                            proposedNewPosition,
                            this.preferredWalkingDistance
                    );

                    vectorsToAdd.add(attractiveForce);
                }
            }
        } else {
            proposedNewPosition = this.computeFirstStepPosition();

            // Check if the patch representing the future position has someone on it
            // Only proceed when there is no one there
//            if (
//                    this.hasNoPassenger(this.currentFloor.getPatch(proposedNewPosition))
//                            || this.getCurrentTurnstileGate() != null
//            ) {
//                this.hasEncounteredPassengerToFollow = this.passengerFollowedWhenAssembling != null;

            // Get the attractive force of this passenger to the new position
            this.attractiveForce = this.computeAttractiveForce(
                    new Coordinates(this.position),
                    Coordinates.headingTowards(
                            this.position,
                            proposedNewPosition
                    ),
                    proposedNewPosition,
                    this.preferredWalkingDistance
            );

            vectorsToAdd.add(attractiveForce);
//            }

            this.shouldStepForward = false;
        }

        // Here ends the few ticks of grace period for the passenger to leave its starting patch
        if (
                !this.willPathfind
                        && !this.hasPathfound
                        && this.parent.getTicketType() == TicketBooth.TicketType.STORED_VALUE
                        && !hasJustLeftGoal()
        ) {
            this.beginStoredValuePathfinding();
        }

        // Take note of the previous walking distance of this passenger
        double previousWalkingDistance = this.currentWalkingDistance;

        vectorsToAdd.addAll(this.repulsiveForceFromPassengers);

        // Then compute the partial motivation force of the passenger
        Vector partialMotivationForce = Vector.computeResultantVector(
                new Coordinates(this.position),
                vectorsToAdd
        );

        // If the resultant vector is null (i.e., no change in position), simply don't move at all
        if (!this.shouldStopAtPlatform && partialMotivationForce != null) {
            // The distance by which the repulsion starts to kick in will depend on the density of the passenger's
            // surroundings
            final int minimumObstacleCount = 1;
            final double maximumDistance = 2.0;
            final int maximumObstacleCount = 2;
            final double minimumDistance = 0.7;

            final int maximumObstacleCountTolerated = 2;

            double computedMaximumDistance = computeMaximumRepulsionDistance(
                    numberOfObstacles,
                    maximumObstacleCountTolerated,
                    minimumObstacleCount,
                    maximumDistance,
                    maximumObstacleCount,
                    minimumDistance
            );

            // Only apply the social forces on a set number of obstacles
            int obstaclesProcessed = 0;
            final int obstaclesProcessedLimit = 4;

            for (Map.Entry<Double, Amenity.AmenityBlock> obstacleEntry : obstaclesEncountered.entrySet()) {
                if (obstaclesProcessed == obstaclesProcessedLimit) {
                    break;
                }

                this.repulsiveForcesFromObstacles.add(
                        computeSocialForceFromObstacle(
                                obstacleEntry.getValue(),
                                obstacleEntry.getKey(),
                                computedMaximumDistance,
                                minimumObstacleStopDistance,
                                partialMotivationForce.getMagnitude()
                        )
                );

                obstaclesProcessed++;
            }

            vectorsToAdd.clear();

            vectorsToAdd.add(partialMotivationForce);
            vectorsToAdd.addAll(this.repulsiveForcesFromObstacles);

            // Finally, compute the final motivation force
            this.motivationForce = Vector.computeResultantVector(
                    new Coordinates(this.position),
                    vectorsToAdd
            );

            if (this.motivationForce != null) {
                // Cap the magnitude of the motivation force to the passenger's preferred walking distance
                if (this.motivationForce.getMagnitude() > this.preferredWalkingDistance) {
                    this.motivationForce.adjustMagnitude(this.preferredWalkingDistance);
                }

                // Then adjust its heading with minor stochastic deviations
                this.motivationForce.adjustHeading(
                        this.motivationForce.getHeading()
                                + Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * Math.toRadians(5)
                );

                try {
                    // Set the new heading
                    double newHeading = motivationForce.getHeading();

                    Coordinates candidatePosition = this.motivationForce.getFuturePosition();

                    if (hasClearLineOfSight(this.position, candidatePosition, false)) {
                        this.move(candidatePosition);
                    } else {
                        double revisedHeading;
                        Coordinates newFuturePosition;

                        int attempts = 0;
                        final int attemptLimit = 2;

                        boolean freeSpaceFound;

                        do {
                            // Go back with the same magnitude as the original motivation force, but at a different
                            // heading
                            revisedHeading
                                    = (motivationForce.getHeading() + Math.toRadians(180)) % Math.toRadians(360);

                            // Add some stochasticity to this revised heading
                            revisedHeading += Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * Math.toRadians(90);
                            revisedHeading %= Math.toRadians(360);

                            // Then calculate the future position from the current position
                            newFuturePosition = this.getFuturePosition(
                                    this.position,
                                    revisedHeading,
                                    this.preferredWalkingDistance * 0.25
                            );

                            freeSpaceFound
                                    = hasClearLineOfSight(this.position, newFuturePosition, false);

                            attempts++;
                        } while (attempts < attemptLimit && !freeSpaceFound);

                        // If all the attempts are used and no free space has been found, don't move at all
                        if (attempts != attemptLimit || freeSpaceFound) {
                            this.move(newFuturePosition);
                        }
                    }

                    if (
                            !this.isStuck
                                    || Coordinates.headingDifference(
                                    this.heading,
                                    newHeading
                            ) <= Math.toDegrees(90.0)
                                    || this.currentWalkingDistance > noMovementThreshold
                    ) {
//                         Take note of the new heading
                        this.heading = newHeading;
                    }

                    // Also take note of the new speed
                    this.currentWalkingDistance = motivationForce.getMagnitude();

                    // Finally, take note of the distance travelled by this passenger
                    this.distanceMovedInTick = motivationForce.getMagnitude();

                    // If this passenger's distance covered falls under the threshold, increment the counter denoting the ticks
                    // spent not moving
                    // Otherwise, reset the counter
                    // Do not count for movements/non-movements when the passenger is in the "in queue" state
                    if (this.state != State.IN_QUEUE) {
                        if (this.recentPatches.size() <= noNewPatchesSeenThreshold) {
                            this.noNewPatchesSeenCounter++;
                            this.newPatchesSeenCounter = 0;
                        } else {
                            this.noNewPatchesSeenCounter = 0;
                            this.newPatchesSeenCounter++;
                        }
                    } else {
                        if (
                                this.distanceMovedInTick < noMovementThreshold
                        ) {
                            this.noMovementCounter++;
                            this.movementCounter = 0;
                        } else {
                            this.noMovementCounter = 0;
                            this.movementCounter++;
                        }
                    }

                    // If the passenger has moved above the no-movement threshold for at least this number of ticks,
                    // remove the passenger from its stuck state
                    if (
                            this.isStuck
                                    && (
                                    (
                                            this.state == State.IN_QUEUE
                                                    && this.movementCounter >= unstuckTicksThreshold
                                                    || this.state != State.IN_QUEUE
                                                    && this.newPatchesSeenCounter >= unstuckTicksThreshold/*
                                            || this.passengerFollowedWhenAssembling != null*/
                                    )
                            )
                    ) {
                        this.isReadyToFree = true;
                    }

                    this.timeSinceLeftPreviousGoal++;

                    // Check if the passenger has slowed down since the last tick
                    // If it did, reset the time spent accelerating counter
                    if (this.currentWalkingDistance < previousWalkingDistance) {
                        this.ticksAcceleratedOrMaintainedSpeed = 0;
                    } else {
                        this.ticksAcceleratedOrMaintainedSpeed++;
                    }

                    return true;
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }

        // If it reaches this point, there is no movement to be made
        this.hasEncounteredPassengerToFollow = this.passengerFollowedWhenAssembling != null;

        this.stop();

        // There was no movement by this passenger, so increment the pertinent counter
        this.distanceMovedInTick = 0.0;

        this.noMovementCounter++;
        this.movementCounter = 0;

        this.timeSinceLeftPreviousGoal++;

        this.ticksAcceleratedOrMaintainedSpeed = 0;

        return false;
    }

    // Checks if another passenger should apply a repulsive force on this passenger, taking into account parameters
    // other than this passenger
    private boolean allowRepulsionFrom(Passenger otherPassenger) {
        boolean isNotHeadingToQueueable = this.action != Action.HEADING_TO_QUEUEABLE;
        boolean isNotInOppositeStatesWithOtherPassenger
                = !(this.state == State.IN_QUEUE && otherPassenger.getPassengerMovement().getState() != State.IN_QUEUE);

        boolean otherPassengerComesBefore;

        if (this.state == State.IN_QUEUE) {
            Queueable queueable = this.getGoalAmenityAsQueueable();

            if (!(queueable instanceof TrainDoor) && !(queueable instanceof Turnstile)) {
                QueueObject queueObject = queueable.getQueueObject();
                List<Passenger> queueingPassengers = queueObject.getPassengersQueueing();

                if (queueingPassengers.contains(this.parent) && queueingPassengers.contains(otherPassenger)) {
                    otherPassengerComesBefore = otherPassenger.getPassengerMovement().comesBefore(this.parent);
                } else {
                    otherPassengerComesBefore = true;
                }
            } else {
                otherPassengerComesBefore = true;
            }
        } else {
            otherPassengerComesBefore = true;
        }

        boolean otherPassengerInSameGoal;

        Queueable thisPassengerQueueable = this.getGoalAmenityAsQueueable();
        Queueable otherPassengerQueueable = otherPassenger.getPassengerMovement().getGoalAmenityAsQueueable();

        if (thisPassengerQueueable != null && otherPassengerQueueable != null) {
            otherPassengerInSameGoal = thisPassengerQueueable.equals(otherPassengerQueueable);
        } else {
            otherPassengerInSameGoal = true;
        }

        return
                isNotHeadingToQueueable
                        && isNotInOppositeStatesWithOtherPassenger
                        && otherPassengerComesBefore
                        && otherPassengerInSameGoal;
    }

    private double computeTurnstileFirstStepHeading() {
        // First, get the apex of the floor field with the state of the passenger
        Turnstile turnstile = (Turnstile) this.currentPatch.getAmenityBlock().getParent();

        QueueingFloorField.FloorFieldState floorFieldState;

        if (this.disposition == Disposition.BOARDING) {
            floorFieldState = turnstile.getTurnstileFloorFieldStateBoarding();
        } else {
            floorFieldState = turnstile.getTurnstileFloorFieldStateAlighting();
        }

        return computeHeadingFromApexToAttractor(
                floorFieldState,
                turnstile.getQueueObjects().get(this.disposition),
                turnstile.getAttractors()
        );
    }

    private double computeSecurityFirstStepHeading() {
        // First, get the apex of the floor field with the state of the passenger
        Security security = (Security) this.currentPatch.getAmenityBlock().getParent();

        return computeHeadingFromApexToAttractor(
                security.getSecurityFloorFieldState(),
                security.getQueueObject(),
                security.getAttractors()
        );
    }

    private double computeHeadingFromApexToAttractor(
            QueueingFloorField.FloorFieldState floorFieldState,
            QueueObject queueObject,
            List<Amenity.AmenityBlock> attractors
    ) {
        Patch apexLocation;
        double newHeading;

        apexLocation
                = queueObject.getFloorFields().get(floorFieldState).getApices()
                .get(0);

        // Then compute the heading from the apex to the turnstile attractor
        newHeading = Coordinates.headingTowards(
                apexLocation.getPatchCenterCoordinates(),
                attractors.get(0).getPatch().getPatchCenterCoordinates()
        );

        return newHeading;
    }

    private double computeFirstStepHeading() {
        double newHeading;

        if (
                this.currentPatch.getAmenityBlock() != null
                        && this.currentPatch.getAmenityBlock().getParent() instanceof Turnstile
        ) {
            newHeading = computeTurnstileFirstStepHeading();
        } else if (
                this.currentPatch.getAmenityBlock() != null
                        && this.currentPatch.getAmenityBlock().getParent() instanceof Security
        ) {
            newHeading = computeSecurityFirstStepHeading();
        } else {
            newHeading = this.previousHeading;
        }

        return newHeading;
    }

    private Coordinates computeFirstStepPosition() {
        double newHeading = computeFirstStepHeading();

        // Compute for the proposed future position
        return this.getFuturePosition(
                this.position,
                newHeading,
                this.preferredWalkingDistance
        );

    }

    public boolean isNearestPassengerOnFirstStepPositionQueueingForTurnstile() {
        Passenger nearestPassengerOnFirstStepPosition = this.getNearestPassengerOnFirstStepPosition();

        if (this.getGoalAmenityAsTurnstile() != null) {
            if (nearestPassengerOnFirstStepPosition != null) {
                return nearestPassengerOnFirstStepPosition.getPassengerMovement().getGoalAmenityAsTurnstile() != null
                        && nearestPassengerOnFirstStepPosition.getPassengerMovement().getGoalAmenityAsTurnstile()
                        .equals(this.getGoalAmenityAsTurnstile());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Passenger getNearestPassengerOnFirstStepPosition() {
        Patch firstStepPosition = this.currentFloor.getPatch(this.computeFirstStepPosition());

        Passenger nearestPassenger = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Passenger passenger : firstStepPosition.getPassengers()) {
            double distanceFromPassenger = Coordinates.distance(
                    this.position,
                    passenger.getPassengerMovement().getPosition()
            );

            if (distanceFromPassenger < nearestDistance) {
                nearestPassenger = passenger;
                nearestDistance = distanceFromPassenger;
            }
        }

        return nearestPassenger;
    }

    public boolean isFirstStepPositionFree() {
        return hasNoPassenger(this.currentFloor.getPatch(this.computeFirstStepPosition()));
    }

    private boolean hasNoPassenger(Patch patch) {
        if (patch == null) {
            return true;
        }

        List<Passenger> passengersOnPatchWithoutThisPassenger = patch.getPassengers();
        passengersOnPatchWithoutThisPassenger.remove(this.parent);

        return passengersOnPatchWithoutThisPassenger.isEmpty();
    }

    public Turnstile getCurrentTurnstileGate() {
        Amenity.AmenityBlock currentAmenityBlock = this.currentPatch.getAmenityBlock();

        if (currentAmenityBlock == null) {
            return null;
        } else {
            if (currentAmenityBlock.getParent() instanceof Turnstile) {
                Turnstile.TurnstileBlock turnstileBlock = ((Turnstile.TurnstileBlock) currentAmenityBlock);

                if (turnstileBlock.isAttractor()) {
                    return (Turnstile) turnstileBlock.getParent();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public List<Patch> get7x7Field(
            double heading,
            boolean includeCenterPatch,
            double fieldOfViewAngle
    ) {
        Patch centerPatch = this.currentPatch;

        List<Patch> patchesToExplore = new ArrayList<>();

        boolean isCenterPatch;

        for (Patch patch : centerPatch.get7x7Neighbors(includeCenterPatch)) {
            // Make sure that the patch to be added is within the field of view of the passenger which invoked
            // this method
            isCenterPatch = patch.equals(centerPatch);

            if ((includeCenterPatch && isCenterPatch) || Coordinates.isWithinFieldOfView(
                    centerPatch.getPatchCenterCoordinates(),
                    patch.getPatchCenterCoordinates(),
                    heading,
                    fieldOfViewAngle)) {
                patchesToExplore.add(patch);
            }
        }

        return patchesToExplore;
    }

    private Vector computeAttractiveForce(
            final Coordinates startingPosition,
            final double proposedHeading,
            final Coordinates proposedNewPosition,
            final double preferredWalkingDistance
    ) {
        // Compute for the attractive force
        Vector attractiveForce = new Vector(
                startingPosition,
                proposedHeading,
                proposedNewPosition,
                preferredWalkingDistance
        );

        return attractiveForce;
    }

    private double computeMaximumRepulsionDistance(
            int objectCount,
            final int maximumObjectCountTolerated,
            final int minimumObjectCount,
            final double maximumDistance,
            final int maximumObjectCount,
            final double minimumDistance
    ) {
        if (objectCount > maximumObjectCountTolerated) {
            objectCount = maximumObjectCountTolerated;
        }

        final double a = (maximumDistance - minimumDistance) / (minimumObjectCount - maximumDistance);
        final double b = minimumDistance - a * maximumObjectCount;

        return a * objectCount + b;
    }

    private double computeRepulsionMagnitudeFactor(
            final double distance,
            final double maximumDistance,
            final double minimumRepulsionFactor,
            final double minimumDistance,
            final double maximumRepulsionFactor
    ) {
        // Formula: for the inverse square law equation y = a / x ^ 2 + b,
        // a = (d_max ^ 2 * (r_min * d_max ^ 2 - r_min * d_min ^ 2 + r_max ^ 2 * d_min ^ 2)) / (d_max ^ 2 - d_min ^ 2)
        // and
        // b = -((r_max ^ 2 * d_min ^ 2) / (d_max ^ 2 - d_min ^ 2))
        double differenceOfSquaredDistances = Math.pow(maximumDistance, 2.0) - Math.pow(minimumDistance, 2.0);
        double productOfMaximumRepulsionAndMinimumDistance
                = Math.pow(maximumRepulsionFactor, 2.0) * Math.pow(minimumDistance, 2.0);

        double a
                = (
                Math.pow(maximumDistance, 2.0) * (minimumRepulsionFactor * Math.pow(maximumDistance, 2.0)
                        - minimumRepulsionFactor * Math.pow(minimumDistance, 2.0)
                        + productOfMaximumRepulsionAndMinimumDistance
                )) / differenceOfSquaredDistances;

        double b = -(productOfMaximumRepulsionAndMinimumDistance / differenceOfSquaredDistances);

        double repulsion = a / Math.pow(distance, 2.0) + b;

        // The repulsion value should always be greater or equal to zero
        if (repulsion <= 0.0) {
            repulsion = 0.0;
        }

        return repulsion;
    }

    private Vector computeSocialForceFromPassenger(
            Passenger passenger,
            final double distanceToOtherPassenger,
            final double maximumDistance,
            final double minimumDistance,
            final double maximumMagnitude
    ) {
        final double maximumRepulsionFactor = 1.0;
        final double minimumRepulsionFactor = 0.0;

        Coordinates passengerPosition = passenger.getPassengerMovement().getPosition();

        // If this passenger is closer than the minimum distance specified, apply a force as if the distance is just at
        // that minimum
        double modifiedDistanceToObstacle = Math.max(distanceToOtherPassenger, minimumDistance);

        double repulsionMagnitudeCoefficient;
        double repulsionMagnitude;

        repulsionMagnitudeCoefficient = computeRepulsionMagnitudeFactor(
                modifiedDistanceToObstacle,
                maximumDistance,
                minimumRepulsionFactor,
                minimumDistance,
                maximumRepulsionFactor
        );

        repulsionMagnitude = repulsionMagnitudeCoefficient * maximumMagnitude;

        // If a passenger is stuck, do not exert much force from this passenger
        if (this.isStuck) {
            final double factor = 0.05;

            repulsionMagnitude -= this.stuckCounter * factor;

            if (repulsionMagnitude <= 0.0001 * this.preferredWalkingDistance) {
                repulsionMagnitude = 0.0001 * this.preferredWalkingDistance;
            }
        }

        // Then compute the heading from that other passenger to this passenger
        double headingFromOtherPassenger = Coordinates.headingTowards(
                passengerPosition,
                this.position
        );

        // Then compute for a future position given the other passenger's position, the heading, and the
        // magnitude
        // This will be used as the endpoint of the repulsion vector from this obstacle
        Coordinates passengerRepulsionVectorFuturePosition = this.getFuturePosition(
                passengerPosition,
                headingFromOtherPassenger,
                repulsionMagnitude
        );

        // Finally, given the current position, heading, and future position, create the vector from
        // the other passenger to the current passenger
        return new Vector(
                passengerPosition,
                headingFromOtherPassenger,
                passengerRepulsionVectorFuturePosition,
                repulsionMagnitude
        );
    }

    private Vector computeSocialForceFromObstacle(
            Amenity.AmenityBlock amenityBlock,
            final double distanceToObstacle,
            final double maximumDistance,
            double minimumDistance,
            final double maximumMagnitude
    ) {
        final double maximumRepulsionFactor = 1.0;
        final double minimumRepulsionFactor = 0.0;

        Coordinates repulsionVectorStartingPosition = amenityBlock.getPatch().getPatchCenterCoordinates();

        // If this passenger is closer than the minimum distance specified, apply a force as if the distance is just at
        // that minimum
        double modifiedDistanceToObstacle = Math.max(distanceToObstacle, minimumDistance);

        double repulsionMagnitudeCoefficient;
        double repulsionMagnitude;

        repulsionMagnitudeCoefficient = computeRepulsionMagnitudeFactor(
                modifiedDistanceToObstacle,
                maximumDistance,
                minimumRepulsionFactor,
                minimumDistance,
                maximumRepulsionFactor
        );

        repulsionMagnitude = repulsionMagnitudeCoefficient * maximumMagnitude;

        // If a passenger is stuck, do not exert much force from this obstacle
        if (this.isStuck) {
            final double factor = 0.05;

            repulsionMagnitude -= this.stuckCounter * factor;

            if (repulsionMagnitude <= 0.0001 * this.preferredWalkingDistance) {
                repulsionMagnitude = 0.0001 * this.preferredWalkingDistance;
            }
        }

/*        // Get the potential origins of the two repulsion vectors
        Coordinates xAxisOrigin
                = new Coordinates(
                this.position.getX(),
                amenityBlock.getPatch().getPatchCenterCoordinates().getY()
        );

        Coordinates yAxisOrigin
                = new Coordinates(amenityBlock.getPatch().getPatchCenterCoordinates().getX(), this.position.getY());

        // Get the distances between these origins and this passenger's position
        double xAxisOriginDistance = Math.abs(xAxisOrigin.getY() - this.position.getY());
        double yAxisOriginDistance = Math.abs(yAxisOrigin.getX() - this.position.getX());

        // Get whichever is the larger of these two distances - this will be the starting position of the vector
        Coordinates repulsionVectorStartingPosition;

        if (xAxisOriginDistance >= yAxisOriginDistance) {
            repulsionVectorStartingPosition = xAxisOrigin;
        } else {
            repulsionVectorStartingPosition = yAxisOrigin;
        }*/

        // Compute the heading from that origin point to this passenger
        double headingFromOtherObstacle = Coordinates.headingTowards(
                repulsionVectorStartingPosition,
                this.position
        );

        // Then compute for a future position given the obstacle's position, the heading, and the
        // magnitude
        // This will be used as the endpoint of the repulsion vector from this obstacle
        Coordinates obstacleRepulsionVectorFuturePosition = this.getFuturePosition(
                repulsionVectorStartingPosition,
                headingFromOtherObstacle,
                repulsionMagnitude
        );

        // Finally, given the current position, heading, and future position, create the vector from
        // the obstacle to the current passenger
        return new Vector(
                repulsionVectorStartingPosition,
                headingFromOtherObstacle,
                obstacleRepulsionVectorFuturePosition,
                repulsionMagnitude
        );
    }

    // Make the passenger move given a walking distance
    private void move(double walkingDistance) {
        this.setPosition(this.getFuturePosition(walkingDistance));
    }

    // Make the passenger move given the future position
    private void move(Coordinates futurePosition) {
        this.setPosition(futurePosition);
    }

    // Check if this passenger has reached its goal's queueing floor field
    public boolean hasReachedQueueingFloorField() {
        for (Patch patch : this.goalFloorField.getAssociatedPatches()) {
            if (
                    isOnOrCloseToPatch(patch)
                            && hasClearLineOfSight(
                            this.position,
                            patch.getPatchCenterCoordinates(),
                            true
                    )
            ) {
                return true;
            }
        }

        return false;
    }

    // Check if this passenger has a path to follow
    public boolean hasPath() {
        return this.currentPath != null;
    }

    // Check if this passenger is on the next patch of its path
    public boolean hasReachedNextPatchInPath() {
        return isOnOrCloseToPatch(this.currentPath.getPath().peek());
    }

    // Register this passenger to its queueable goal's queue
    public void joinQueue() {
        this.goalQueueObject.getPassengersQueueing().addLast(this.parent);
    }

    // Have the passenger stop
    public void stop() {
        this.currentWalkingDistance = 0.0;
    }

    // Unregister this passenger to its queueable goal's queue
    public void leaveQueue() {
        this.goalQueueObject.getPassengersQueueing().remove(this.parent);
    }

    // Check if this passenger has reached an apex of its floor field
    public boolean hasReachedQueueingFloorFieldApex() {
        // If the passenger is in any of this floor field's apices, return true
        for (Patch apex : this.goalFloorField.getApices()) {
            if (isOnOrCloseToPatch(apex)) {
                return true;
            }
        }

        return false;
    }

    // Have this passenger start waiting for an amenity to become vacant
    public void beginWaitingOnAmenity() {
        this.isWaitingOnAmenity = true;
    }

    // Have this passenger start waiting for a portal to become vacant
    public void beginWaitingOnPortal() {
        this.isWaitingOnPortal = true;
    }

    // Check if the goal of this passenger is currently not servicing anyone
    public boolean isGoalFree() {
        return this.getGoalAmenityAsGoal().isFree(this.goalQueueObject)
                && this.goalQueueObject.getPatch().getPassengers().isEmpty();
    }

    // Check if this passenger the one currently served by its goal
    public boolean isServicedByGoal() {
        Passenger passengerServiced = this.goalQueueObject.getPassengerServiced();

        return passengerServiced != null && passengerServiced.equals(this.parent);
    }

    // Check if this passenger is at the front of the queue
    public boolean isAtQueueFront() {
        LinkedList<Passenger> passengersQueueing
                = this.goalQueueObject.getPassengersQueueing();

        if (passengersQueueing.isEmpty()) {
            return false;
        }

        return passengersQueueing.getFirst() == this.parent;
    }

    // Have this passenger stop waiting for an amenity to become vacant
    public void endWaitingOnAmenity() {
        this.isWaitingOnAmenity = false;
    }

    // Have this passenger stop waiting for a portal to become vacant
    public void endWaitingOnPortal() {
        this.isWaitingOnPortal = false;
    }

    // Enable pathfinding for stored value card passengers
    public void beginStoredValuePathfinding() {
        this.willPathfind = true;
    }

    // Disable pathfinding for stored value card passengers
    public void endStoredValuePathfinding() {
        this.currentPath = null;

        this.willPathfind = false;
        this.hasPathfound = true;
    }

    // Check if this passenger has reached its goal
    public boolean hasReachedGoal() {
        // If the passenger is still waiting for an amenity to be vacant, it hasn't reached the goal yet
        if (this.isWaitingOnAmenity) {
            return false;
        }

        return isOnOrCloseToPatch(this.goalAttractor.getPatch());
    }

    // Set the passenger's current amenity and position as it reaches the next goal
    public void reachGoal() {
        // Just in case the passenger isn't actually on its goal, but is adequately close to it, just move the passenger
        // there
        // Make sure to offset the passenger from the center a little so a force will be applied to this passenger
        Coordinates patchCenter = this.goalAttractor.getPatch().getPatchCenterCoordinates();
        Coordinates offsetPatchCenter = this.getFuturePosition(
                patchCenter,
                this.previousHeading,
                Patch.PATCH_SIZE_IN_SQUARE_METERS * 0.1
        );

        this.setPosition(offsetPatchCenter);

        // Set the current amenity
        this.currentAmenity = this.goalAmenity;

        // If this goal is a portal, add it to the list of visited portals
        if (this.currentAmenity instanceof Portal) {
            this.visitedPortals.add((Portal) this.currentAmenity);
        } else if (this.currentAmenity instanceof Turnstile) {
            // If this goal is a turnstile, set the heading
            this.heading = this.computeFirstStepHeading();
        }
    }

    // Set the passenger's next patch in its current path as it reaches it
    public void reachPatchInPath() {
        // Keep popping while there are still patches from the path to pop and these patches are still close enough to
        // the passenger
        Patch nextPatch;

        do {
            this.currentPath.getPath().pop();

            // If there are no more next patches, terminate the loop
            if (!this.currentPath.getPath().isEmpty()) {
                nextPatch = this.currentPath.getPath().peek();
            } else {
                break;
            }
        } while (
                !this.currentPath.getPath().isEmpty()
                        && nextPatch.getAmenityBlocksAround() == 0
                        && this.isOnOrCloseToPatch(nextPatch)
                        && this.hasClearLineOfSight(
                        this.position,
                        nextPatch.getPatchCenterCoordinates(),
                        true
                )
        );
    }

    // Have this passenger's goal service this passenger
    public void beginServicingThisPassenger() {
        // This passenger will now be the one to be served next
        this.goalQueueObject.setPassengerServiced(this.parent);
    }

    // Have this passenger's goal finish serving this passenger
    public void endServicingThisPassenger() {
        // This passenger is done being serviced by this goal
        this.goalQueueObject.setPassengerServiced(null);

        // Reset the goal's waiting time counter
        if (this.getGoalAmenityAsGoal() != null) {
            this.getGoalAmenityAsGoal().resetWaitingTime();
        }
    }

    // Check if this passenger has reached its final goal
    public boolean hasReachedFinalGoal() {
        return !this.routePlan.getCurrentRoutePlan().hasNext();
    }

    // Check if this passenger has reached the final patch in its current path
    public boolean hasPassengerReachedFinalPatchInPath() {
        return this.currentPath.getPath().isEmpty();
    }

    // Check if this passenger has reached the specified patch
    private boolean isOnPatch(Patch patch) {
        return ((int) (this.position.getX() / Patch.PATCH_SIZE_IN_SQUARE_METERS)) == patch.getMatrixPosition().getColumn()
                && ((int) (this.position.getY() / Patch.PATCH_SIZE_IN_SQUARE_METERS)) == patch.getMatrixPosition().getRow();
    }

    // Check if this passenger is adequately close enough to a patch
    // In this case, a passenger is close enough to a patch when the distance between this passenger and the patch is
    // less than the distance covered by the passenger per second
    private boolean isOnOrCloseToPatch(Patch patch) {
        return Coordinates.distance(this.position, patch.getPatchCenterCoordinates()) <= this.preferredWalkingDistance;
    }

    // Check if this passenger is allowed by its goal to pass
    public boolean isAllowedPass() {
        boolean allowPassAsGoal = this.getGoalAmenityAsGoal().allowPass();

        Blockable blockable = this.getGoalAmenityAsBlockable();

        if (blockable != null) {
            return allowPassAsGoal && !blockable.blockEntry();
        } else {
            return allowPassAsGoal;
        }
    }

    // Check if this passenger will enter the train
    public boolean willEnterTrain() {
        TrainDoor closestTrainDoor = getGoalAmenityAsTrainDoor();

        if (closestTrainDoor != null) {
            // If the goal train door is open, check if the passenger is willing to ride the train
            if (isTrainDoorOpen(closestTrainDoor)) {
                // TODO: Have a better way of checking whether this passenger has necessary information, or not
                //  perhaps insert a different passenger information object to a passenger when at train simulation and
                //  at station editing?
                if (this.getRoutePlan().getOriginStation() != null) {
                    // TODO: Get station in a better way
                    com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station
                            = this.getRoutePlan().getOriginStation();

                    final double loadLimit = 0.7;
                    final double carriageLoadLimit = closestTrainDoor.getTrainCarriage(station).getLoadFactor();

                    // Carriage full/overfull, don't ride
                    if (carriageLoadLimit >= 1.0) {
                        return false;
                    } else if (carriageLoadLimit > loadLimit) {
                        // Carriage almost full, ride (or don't) based on a probability
                        final double loadLimitLeft = 1.0 - loadLimit;
                        final double probability = (1 - carriageLoadLimit) / loadLimitLeft;

                        return Simulator.RANDOM_NUMBER_GENERATOR.nextDouble() < probability;
                    } else {
                        // A lot of room in the carriages, ride
                        return true;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // Check whether this passenger's goal as a train door is open
    private boolean isTrainDoorOpen(TrainDoor trainDoor) {
        return trainDoor.isOpen();
    }

    // Check if this passenger will use a portal
    public boolean willHeadToPortal() {
        return this.goalFloor != null && this.goalPortal != null;
    }

    // Check if this passenger's next floor is below the current floor
    public boolean isGoalFloorLower() {
        if (!willHeadToPortal()) {
            return false;
        } else {
            List<Floor> floorsInThisStation = this.currentFloor.getStation().getFloors();

            // Get the index of the current and goal floors
            int currentFloorIndex = floorsInThisStation.indexOf(this.currentFloor);
            int goalFloorIndex = floorsInThisStation.indexOf(this.goalFloor);

            assert currentFloorIndex != goalFloorIndex;

            return goalFloorIndex < currentFloorIndex;
        }
    }

    // Check if this passenger will enter the portal
    public boolean willEnterPortal() {
        Portal closestPortal = getGoalAmenityAsPortal();

        if (closestPortal != null) {
            if (closestPortal instanceof StairPortal) {
                StairPortal stairPortal = ((StairPortal) closestPortal);

                if (this.isGoalFloorLower()) {
                    return !stairPortal.getStairShaft().isDescendingQueueAtCapacity();
                } else {
                    return !stairPortal.getStairShaft().isAscendingQueueAtCapacity();
                }
            } else if (closestPortal instanceof EscalatorPortal) {
                EscalatorPortal escalatorPortal = ((EscalatorPortal) closestPortal);

                return !escalatorPortal.getEscalatorShaft().isQueueAtCapacity();
            } else if (closestPortal instanceof ElevatorPortal) {
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // Have this passenger enter its portal
    public void enterPortal() {
        // Remove the passenger from its patch
        this.currentPatch.getPassengers().remove(this.parent);

        // Remove this passenger from this floor
        this.currentFloor.getPassengersInFloor().remove(this.parent);

        // Remove this passenger from its current floor's patch set, if necessary
        SortedSet<Patch> currentPatchSet = this.currentPatch.getFloor().getPassengerPatchSet();

        if (currentPatchSet.contains(this.currentPatch) && hasNoPassenger(this.currentPatch)) {
            currentPatchSet.remove(this.currentPatch);
        }

        // Set the passenger's patch to null
        this.currentPatch = null;
    }

    // Have this passenger try exiting its portal
    public boolean exitPortal() {
        // Move towards the other end of the portal
        Portal portal = (Portal) this.currentAmenity;
        portal = portal.getPair();

        // Try to emit a passenger
        Patch spawnPatch = portal.emit();

        // Only proceed is a passenger can be emitted
        if (spawnPatch != null) {
            // Get the patch of the spawner which released this passenger
            Patch spawnerPatch = spawnPatch;

            // Set the current patch, floor
            this.currentPatch = spawnerPatch;

            this.currentFloor = portal.getFloorServed();
            this.currentAmenity = portal;

            // Set the passenger's position
            this.position.setX(spawnerPatch.getPatchCenterCoordinates().getX());
            this.position.setY(spawnerPatch.getPatchCenterCoordinates().getY());

            // Set the new state and action
            this.state = State.WALKING;

            if (this.isReadyToExit) {
                this.action = Action.EXITING_STATION;
            } else {
                this.action = Action.WILL_QUEUE;
            }

            // Add the newly created passenger to the list of passengers in the floor
            this.currentFloor.getPassengersInFloor().add(this.parent);

            // Add the passenger's patch position to its current floor's patch set as well
            this.currentFloor.getPassengerPatchSet().add(spawnerPatch);

            return true;
        } else {
            // No passenger emitted, return false
            return false;
        }
    }

    // Board train
    public void boardTrain(TrainDoor trainDoor) {
        // Remove the passenger from its current station
        trainDoor.despawnPassenger(this.parent);

        // Set the passenger's new disposition, state, and action
        this.disposition = Disposition.RIDING_TRAIN;
        this.state = State.IN_TRAIN;
        this.action = Action.RIDING_TRAIN;

        // If this passenger is female and has chosen a female only carriage, mark as such
        Passenger.PassengerInformation passengerInformation = this.getParent().getPassengerInformation();

        if (passengerInformation.getGender() == Passenger.PassengerInformation.Gender.FEMALE) {
            if (trainDoor.isFemaleOnly()) {
                passengerInformation.setChosenFemaleOnlyCarriage(true);
            }
        }

        // The passenger is not at any patch
        this.currentPatch = null;
    }

    // Alight train
    public void alightTrain(Gate.GateBlock spawner) {
        // Before anything else, reset the passenger movement to avoid residual values from the previous station
        this.resetGoal(false);

        // Get the patch of the spawner which released this passenger
        Patch spawnerPatch = spawner.getPatch();

        // Set the new disposition, state, and action of the alighted passenger
        this.disposition = Disposition.ALIGHTING;
        this.state = State.WALKING;
        this.action = Action.WILL_QUEUE;

        // Set the current patch, floor
        this.currentPatch = spawnerPatch;

        this.currentFloor = spawnerPatch.getFloor();
        this.currentAmenity = spawner.getParent();

        // Set the passenger's position
        this.position.setX(spawner.getPatch().getPatchCenterCoordinates().getX());
        this.position.setY(spawner.getPatch().getPatchCenterCoordinates().getY());

        // Add the newly created passenger to the list of passengers in the floor
        this.currentFloor.getPassengersInFloor().add(this.parent);

        // Add the passenger's patch position to its current floor's patch set as well
        this.currentFloor.getPassengerPatchSet().add(spawner.getPatch());
    }

    // Despawn this passenger
    public void despawn() {
        if (this.currentPatch != null) {
            // Remove the passenger from its patch
            this.currentPatch.getPassengers().remove(this.parent);

            // Remove this passenger from this floor
            this.currentFloor.getPassengersInFloor().remove(this.parent);

            // Remove this passenger from this station
            this.currentFloor.getStation().getPassengersInStation().remove(this.parent);

            // Remove this passenger from its current floor's patch set, if necessary
            SortedSet<Patch> currentPatchSet = this.currentPatch.getFloor().getPassengerPatchSet();

            if (currentPatchSet.contains(this.currentPatch) && hasNoPassenger(this.currentPatch)) {
                currentPatchSet.remove(this.currentPatch);
            }
        }
    }

    // Have the passenger face its current goal, or its queueing area, or the passenger at the end of the queue
    public void faceNextPosition() {
        double newHeading;
        boolean willFaceQueueingPatch;
        Patch proposedGoalPatch;

        // iI the passenger is already heading for a queueable, no need to seek its floor fields again, as
        // it has already done so, and is now just heading to the goal itself
        // If it has floor fields, get the heading towards the nearest floor field value
        // If it doesn't have floor fields, just get the heading towards the goal itself
        if (
                this.action != Action.HEADING_TO_QUEUEABLE
                        && this.action != Action.HEADING_TO_TRAIN_DOOR
                        && this.goalAmenity instanceof Queueable
        ) {
            // If a queueing patch has not yet been set for this goal, set it
            if (this.goalNearestQueueingPatch == null) {
                // If the next floor field has not yet been set for this queueing patch, set it
                if (this.goalFloorFieldState == null && this.goalFloorField == null) {
                    Queueable queueable = this.getGoalAmenityAsQueueable();

                    if (queueable instanceof Turnstile) {
                        this.goalFloorFieldState = new QueueingFloorField.FloorFieldState(
                                this.disposition,
                                State.IN_QUEUE,
                                this.getGoalAmenityAsQueueable()
                        );

                        this.goalFloorField = queueable.retrieveFloorField(
                                this.goalQueueObject,
                                this.goalFloorFieldState
                        );
                    } else if (queueable instanceof TrainDoor) {
                        this.goalFloorFieldState = new PlatformFloorField.PlatformFloorFieldState(
                                this.disposition,
                                State.IN_QUEUE,
                                this.getGoalAmenityAsQueueable(),
                                this.goalTrainDoorEntranceLocation
                        );

                        this.goalFloorField = queueable.retrieveFloorField(
                                this.goalQueueObject,
                                this.goalFloorFieldState
                        );
                    } else {
                        this.goalFloorFieldState = new QueueingFloorField.FloorFieldState(
                                this.disposition,
                                State.IN_QUEUE,
                                this.getGoalAmenityAsQueueable()
                        );

                        this.goalFloorField = queueable.retrieveFloorField(
                                queueable.getQueueObject(),
                                this.goalFloorFieldState
                        );
                    }
                }

                if (this.goalFloorField == null) {
                    this.getGoalAmenityAsQueueable().retrieveFloorField(
                            this.goalQueueObject,
                            this.goalFloorFieldState
                    );
                }

                this.goalNearestQueueingPatch = this.getPatchWithNearestFloorFieldValue();
                proposedGoalPatch = this.goalNearestQueueingPatch;
            }

            // If this passenger is in the "will queue" state, choose between facing the queueing patch, and facing the
            // passenger at the back of the queue
            if (action == Action.WILL_QUEUE || action == Action.ASSEMBLING) {
                LinkedList<Passenger> passengerQueue
                        = this.goalQueueObject.getPassengersQueueing();

                // Check whether there are passengers queueing for the goal
                if (passengerQueue.isEmpty()) {
                    // If there are no passengers queueing yet, simply compute the heading towards the nearest queueing
                    // patch
                    this.passengerFollowedWhenAssembling = null;
                    this.goalNearestQueueingPatch = this.getPatchWithNearestFloorFieldValue();
                    proposedGoalPatch = this.goalNearestQueueingPatch;

                    willFaceQueueingPatch = true;
                } else {
                    if (this.isNextAmenityTrainDoor()) {
                        this.passengerFollowedWhenAssembling = null;
                        this.goalNearestQueueingPatch = this.getPatchWithNearestFloorFieldValue();
                        proposedGoalPatch = this.goalNearestQueueingPatch;

                        willFaceQueueingPatch = true;
                    } else {
                        Passenger passengerFollowedCandidate;

                        // If there are passengers queueing, join the queue and follow either the last person in the queue
                        // or the person before this
                        if (action == Action.WILL_QUEUE) {
                            passengerFollowedCandidate = passengerQueue.getLast();
                        } else {
                            int passengerFollowedCandidateIndex = passengerQueue.indexOf(this.parent) - 1;

                            if (passengerFollowedCandidateIndex >= 0) {
                                passengerFollowedCandidate
                                        = passengerQueue.get(passengerFollowedCandidateIndex);
                            } else {
                                passengerFollowedCandidate = null;
                            }
                        }

                        // But if the person to be followed is this person itself, or is not assembling, or follows this
                        // person too (forming a cycle), disregard it, and just follow the queueing patch
                        // Otherwise, follow that passenger
                        if (
                                passengerFollowedCandidate == null
                                        || passengerFollowedCandidate.equals(this.parent)
                                        || !passengerFollowedCandidate.equals(this.parent)
                                        && passengerFollowedCandidate.getPassengerMovement()
                                        .getPassengerFollowedWhenAssembling() != null
                                        && passengerFollowedCandidate.getPassengerMovement()
                                        .getPassengerFollowedWhenAssembling().equals(this.parent)
                        ) {
                            this.passengerFollowedWhenAssembling = null;
                            this.goalNearestQueueingPatch = this.getPatchWithNearestFloorFieldValue();
                            proposedGoalPatch = this.goalNearestQueueingPatch;

                            willFaceQueueingPatch = true;
                        } else {
                            // But only follow passengers who are nearer to this passenger than to the chosen queueing
                            // patch and are within this passenger's walking distance and have a clear line of sight to
                            // this passenger
                            final double distanceToPassengerFollowedCandidate = Coordinates.distance(
                                    this.position,
                                    passengerFollowedCandidate.getPassengerMovement().getPosition()
                            );

                            final double passengerFollowedRange = this.preferredWalkingDistance * 3.0;

                            if (
                                    !hasClearLineOfSight(this.position, passengerFollowedCandidate.getPassengerMovement().getPosition(), true)
                                            || distanceToPassengerFollowedCandidate > passengerFollowedRange
                            ) {
                                this.passengerFollowedWhenAssembling = null;
                                this.goalNearestQueueingPatch = this.getPatchWithNearestFloorFieldValue();
                                proposedGoalPatch = this.goalNearestQueueingPatch;

                                willFaceQueueingPatch = true;
                            } else {
                                this.passengerFollowedWhenAssembling = passengerFollowedCandidate;
                                proposedGoalPatch = this.goalNearestQueueingPatch;

                                willFaceQueueingPatch = false;

                            }
                        }
                    }
                }
            } else {
                this.passengerFollowedWhenAssembling = null;
                proposedGoalPatch = this.goalNearestQueueingPatch;

                willFaceQueueingPatch = true;
            }

            if (willFaceQueueingPatch) {
                newHeading = Coordinates.headingTowards(
                        this.position,
                        this.goalNearestQueueingPatch.getPatchCenterCoordinates()
                );
            } else {
                // Get the distance from here to both the proposed passenger followed and the nearest queueing
                // patch
                double distanceToPassenger = Coordinates.distance(
                        this.position,
                        this.passengerFollowedWhenAssembling.getPassengerMovement().getPosition()
                );

                double distanceToQueueingPatch = Coordinates.distance(
                        this.position,
                        this.goalNearestQueueingPatch.getPatchCenterCoordinates()
                );

                // Head towards whoever is nearer
                if (distanceToPassenger > distanceToQueueingPatch) {
                    newHeading = Coordinates.headingTowards(
                            this.position,
                            this.goalNearestQueueingPatch.getPatchCenterCoordinates()
                    );
                } else {
                    newHeading = Coordinates.headingTowards(
                            this.position,
                            this.passengerFollowedWhenAssembling.getPassengerMovement().getPosition()
                    );
                }
            }
        } else {
            proposedGoalPatch = this.goalAttractor.getPatch();

            // Compute the heading towards the goal's attractor
            newHeading = Coordinates.headingTowards(
                    this.position,
                    this.goalAttractor.getPatch().getPatchCenterCoordinates()
            );
        }
//        }

        if (this.willPathfind || this.action == Action.REROUTING) {
            // Get the heading towards the goal patch, which was set as the next patch in the path
            newHeading = Coordinates.headingTowards(
                    this.position,
                    this.goalPatch.getPatchCenterCoordinates()
            );

//            this.proposedHeading = newHeading;
        } else {
            this.goalPatch = proposedGoalPatch;
        }

        // Then set the passenger's proposed heading to it
        this.proposedHeading = newHeading;
    }

    // While the passenger is already on a floor field, have the passenger face the one with the highest value
    public void chooseBestQueueingPatch() {
        // Retrieve the patch with the highest floor field value around the passenger's vicinity
        this.goalNearestQueueingPatch = this.getBestQueueingPatch();
        this.goalPatch = this.goalNearestQueueingPatch;
    }

    // If the passenger is following a path, have the passenger face the next one, if any
    public boolean chooseNextPatchInPath() {
        // Generate a path, if one hasn't been generated yet
        boolean wasPathJustGenerated = false;

        final int recomputeThreshold = 10;

        if (
                this.currentPath == null
                        || this.isStuck
                        && this.noNewPatchesSeenCounter > recomputeThreshold
        ) {
            PassengerPath passengerPath;

            if (this.getGoalAmenityAsQueueable() != null) {
                // Head towards the queue of the goal
                LinkedList<Passenger> passengersQueueing
                        = this.goalQueueObject.getPassengersQueueing();

                // If there are no passengers in that queue at all, simply head for the goal patch
                if (passengersQueueing.isEmpty()) {
                    passengerPath = computePathWithinFloor(
                            this.currentPatch,
                            this.goalPatch,
                            true,
                            true,
                            false
                    );
                } else {
                    // If there are passengers in the queue, this passenger should only follow the last passenger in
                    // that queue if that passenger is assembling
                    // If the last passenger is not assembling, simply head for the goal patch instead
                    Passenger lastPassenger = passengersQueueing.getLast();

                    if (
                            !(this.getGoalAmenityAsQueueable() instanceof TrainDoor)
                                    && !(this.getGoalAmenityAsQueueable() instanceof Turnstile)
                                    && lastPassenger.getPassengerMovement().getAction() == Action.ASSEMBLING
                    ) {
                        double distanceToGoalPatch = Coordinates.distance(
                                this.currentFloor.getStation(),
                                this.currentPatch,
                                this.goalPatch
                        );

                        double distanceToLastPassenger = Coordinates.distance(
                                this.currentFloor.getStation(),
                                this.currentPatch,
                                lastPassenger.getPassengerMovement().getCurrentPatch()
                        );

                        // Head to whichever is closer to this passenger, the last passenger, or the nearest queueing
                        // path
                        if (distanceToGoalPatch <= distanceToLastPassenger) {
                            passengerPath = computePathWithinFloor(
                                    this.currentPatch,
                                    this.goalPatch,
                                    true,
                                    true,
                                    false
                            );
                        } else {
                            passengerPath = computePathWithinFloor(
                                    this.currentPatch,
                                    lastPassenger.getPassengerMovement().getCurrentPatch(),
                                    true,
                                    true,
                                    false
                            );
                        }
                    } else {
                        passengerPath = computePathWithinFloor(
                                this.currentPatch,
                                this.goalPatch,
                                true,
                                true,
                                false
                        );
                    }
                }
            } else {
                passengerPath = computePathWithinFloor(
                        this.currentPatch,
                        this.goalPatch,
                        true,
                        false,
                        false
                );
            }

            if (passengerPath != null) {
                // Create a copy of the object, to avoid using up the path directly from the cache
                this.currentPath = new PassengerPath(passengerPath);

                wasPathJustGenerated = true;
            }
        }

        // Get the first patch still unvisited in the path
        if (this.currentPath == null || this.currentPath.getPath().isEmpty()) {
            return false;
        }

        // If a path was just generated, determine the first patch to visit
        if (wasPathJustGenerated) {
            Patch nextPatchInPath;

            while (true) {
                nextPatchInPath = this.currentPath.getPath().peek();

                if (
                        !(
                                this.currentPath.getPath().size() > 1
                                        && nextPatchInPath.getAmenityBlocksAround() == 0
                                        && this.isOnOrCloseToPatch(nextPatchInPath)
                                        && this.hasClearLineOfSight(
                                        this.position,
                                        nextPatchInPath.getPatchCenterCoordinates(),
                                        true
                                )
                        )
                ) {
                    break;
                }

                this.currentPath.getPath().pop();
            }

            this.goalPatch = nextPatchInPath;

//            ///
//
//            do {
//                goalPatchInPath = this.currentPath.getPath().pop();
//            } while (
//                    !this.currentPath.getPath().isEmpty()
//                            && goalPatchInPath.getAmenityBlocksAround() == 0
//                            && this.isOnOrCloseToPatch(goalPatchInPath)
//                            && this.hasClearLineOfSight(
//                            this.position,
//                            goalPatchInPath.getPatchCenterCoordinates(),
//                            true
//                    )
//            );
//
////            if (goalPatchInPath == null) {
////                return false;
////            } else {
//            this.goalPatch = goalPatchInPath;
////            }
        } else {
            this.goalPatch = this.currentPath.getPath().peek();
        }

        return true;
    }

    // Make this passenger free from being stuck
    public void free() {
        this.isStuck = false;

        this.stuckCounter = 0;
        this.noMovementCounter = 0;
        this.noNewPatchesSeenCounter = 0;

        this.currentPath = null;

        this.isReadyToFree = false;
    }

    // From a set of patches associated with a goal's floor field, get the nearest patch below a threshold
    public Patch getPatchWithNearestFloorFieldValue() {
        final double maximumFloorFieldValueThreshold = 0.8;

        // Get the patches associated with the current goal
        List<Patch> associatedPatches = this.goalFloorField.getAssociatedPatches();

        double minimumDistance = Double.MAX_VALUE;
        Patch nearestPatch = null;

        // Look for the nearest patch from the patches associated with the floor field
        double distanceFromPassenger;

        for (Patch patch : associatedPatches) {
            double floorFieldValue
                    = patch.getFloorFieldValues().get(this.getGoalAmenityAsQueueable()).get(this.goalFloorFieldState);

//            if (floorFieldValue <= maximumFloorFieldValueThreshold) {
            // Get the distance of that patch from this passenger
            distanceFromPassenger = Coordinates.distance(this.position, patch.getPatchCenterCoordinates());

            if (distanceFromPassenger < minimumDistance) {
                minimumDistance = distanceFromPassenger;
                nearestPatch = patch;
            }
//            }
        }

        return nearestPatch;
    }

    private Patch computeBestQueueingPatchWeighted(List<Patch> floorFieldList) {
        // Collect the patches with the highest floor field values
        List<Patch> floorFieldCandidates = new ArrayList<>();
        List<Double> floorFieldValueCandidates = new ArrayList<>();

        double valueSum = 0.0;

        for (Patch patch : floorFieldList) {
            Map<QueueingFloorField.FloorFieldState, Double> floorFieldStateDoubleMap
                    = patch.getFloorFieldValues().get(this.getGoalAmenityAsQueueable());

            if (
                    !patch.getFloorFieldValues().isEmpty()
                            && floorFieldStateDoubleMap != null
                            && !floorFieldStateDoubleMap.isEmpty()
                            && floorFieldStateDoubleMap.get(
                            this.goalFloorFieldState
                    ) != null
            ) {
                double futureFloorFieldValue = patch.getFloorFieldValues()
                        .get(this.getGoalAmenityAsQueueable())
                        .get(this.goalFloorFieldState);

//                if (currentFloorFieldValue == null) {
                valueSum += futureFloorFieldValue;

                floorFieldCandidates.add(patch);
                floorFieldValueCandidates.add(futureFloorFieldValue);
//                }
            }
        }

        // If it gets to this point without finding a floor field value greater than zero, return early
        if (floorFieldCandidates.isEmpty()) {
//            if (this.getGoalAmenityAsTrainDoor() != null) {
//                this.computeBestQueueingPatchWeighted(floorFieldList);
//            }

            return null;
        }

        Patch chosenPatch;
        int choiceIndex = 0;

        // Use the floor field values as weights to choose among patches
        for (
                double randomNumber = Simulator.RANDOM_NUMBER_GENERATOR.nextDouble() * valueSum;
                choiceIndex < floorFieldValueCandidates.size() - 1;
                choiceIndex++) {
            randomNumber -= floorFieldValueCandidates.get(choiceIndex);

            if (randomNumber <= 0.0) {
                break;
            }
        }

        chosenPatch = floorFieldCandidates.get(choiceIndex);
        return chosenPatch;
    }

    // Get the next queueing patch in a floor field given the current floor field state
    private Patch computeBestQueueingPatch(List<Patch> floorFieldList) {
        // Collect the patches with the highest floor field values
        List<Patch> highestPatches = new ArrayList<>();

        double maximumFloorFieldValue = 0.0;

        for (Patch patch : floorFieldList) {
            Map<QueueingFloorField.FloorFieldState, Double> floorFieldStateDoubleMap
                    = patch.getFloorFieldValues().get(this.getGoalAmenityAsQueueable());

            if (
                    !patch.getFloorFieldValues().isEmpty()
                            && floorFieldStateDoubleMap != null
                            && !floorFieldStateDoubleMap.isEmpty()
                            && floorFieldStateDoubleMap.get(
                            this.goalFloorFieldState
                    ) != null
            ) {
                double floorFieldValue = patch.getFloorFieldValues()
                        .get(this.getGoalAmenityAsQueueable())
                        .get(this.goalFloorFieldState);

                if (floorFieldValue >= maximumFloorFieldValue) {
                    if (floorFieldValue > maximumFloorFieldValue) {
                        maximumFloorFieldValue = floorFieldValue;

                        highestPatches.clear();
                    }

                    highestPatches.add(patch);
                }
            }
        }

        // If it gets to this point without finding a floor field value greater than zero, return early
        if (maximumFloorFieldValue == 0.0) {
            return null;
        }

        // If there are more than one highest valued-patches, choose the one where it would take the least heading
        // difference
        Patch chosenPatch /*= highestPatches.get(0)*/ = null;

        List<Double> headingChanges = new ArrayList<>();
        List<Double> distances = new ArrayList<>();

        double headingToHighestPatch;
        double headingChangeRequired;

        double distance;

        for (Patch patch : highestPatches) {
            headingToHighestPatch = Coordinates.headingTowards(this.position, patch.getPatchCenterCoordinates());
            headingChangeRequired = Coordinates.headingDifference(this.proposedHeading, headingToHighestPatch);

            double headingChangeRequiredDegrees = Math.toDegrees(headingChangeRequired);

            headingChanges.add(headingChangeRequiredDegrees);

            distance = Coordinates.distance(this.position, patch.getPatchCenterCoordinates());
            distances.add(distance);
        }

        double minimumHeadingChange = Double.MAX_VALUE;

        for (int index = 0; index < highestPatches.size(); index++) {
            double individualScore = headingChanges.get(index) * 1.0/* + (distances.get(index) * 10.0) * 0.5*/;

            if (individualScore < minimumHeadingChange) {
                minimumHeadingChange = individualScore;
                chosenPatch = highestPatches.get(index);
            }
        }

        return chosenPatch;
    }

    private Patch getBestQueueingPatch() {
        // Get the patches to explore
//        List<Patch> patchesToExplore
//                = Floor.get7x7Field(
//                this.currentFloor,
//                this.currentPatch,
//                this.proposedHeading,
//                false,
//                this.fieldOfViewAngle
//        );

        List<Patch> patchesToExplore = this.get7x7Field(
                this.proposedHeading,
                false,
                this.fieldOfViewAngle
        );

//        this.toExplore = patchesToExplore;

        return this.computeBestQueueingPatch(patchesToExplore);
    }

    // Get the best queueing patch around the current patch of another passenger given the current floor field state
    private Patch getBestQueueingPatchAroundPassenger(Passenger otherPassenger) {
        // Get the other passenger's patch
        Patch otherPassengerPatch = otherPassenger.getPassengerMovement().getCurrentPatch();

        // Get the neighboring patches of that patch
        List<Patch> neighboringPatches = otherPassengerPatch.getNeighbors();

        // Remove the patch containing this passenger
        neighboringPatches.remove(this.currentPatch);

        // Only add patches with the fewest passengers
        List<Patch> neighboringPatchesWithFewestPassengers = new ArrayList<>();
        int minimumPassengerCount = Integer.MAX_VALUE;

        for (Patch neighboringPatch : neighboringPatches) {
            int neighboringPatchPassengerCount = neighboringPatch.getPassengers().size();

            if (neighboringPatchPassengerCount < minimumPassengerCount) {
                neighboringPatchesWithFewestPassengers.clear();

                minimumPassengerCount = neighboringPatchPassengerCount;
            }

            if (neighboringPatchPassengerCount == minimumPassengerCount) {
                neighboringPatchesWithFewestPassengers.add(neighboringPatch);
            }
        }

        // Choose a floor field patch from this
        Patch chosenPatch = this.computeBestQueueingPatchWeighted(neighboringPatchesWithFewestPassengers);

        return chosenPatch;
    }

    // Check if the given patch has an obstacle
    private boolean hasObstacle(Patch patch) {
        // If there is literally no patch there, then there is no obstacle
        if (patch == null) {
            return true;
        }

        Amenity.AmenityBlock amenityBlock = patch.getAmenityBlock();

        if (amenityBlock == null) {
            return false;
        } else {
            Amenity parent = amenityBlock.getParent();

            if (parent instanceof NonObstacle && ((NonObstacle) parent).isEnabled()) {
                if (parent.equals(this.goalAmenity)) {
                    if (parent instanceof Queueable) {
                        Passenger passengerServiced = this.goalQueueObject.getPassengerServiced();

                        if (passengerServiced != null && passengerServiced.equals(this.parent)) {
                            if (amenityBlock instanceof Gate.GateBlock) {
                                Gate.GateBlock gateBlock = ((Gate.GateBlock) amenityBlock);

                                return !amenityBlock.isAttractor() && !gateBlock.isSpawner();
                            } else {
                                return !amenityBlock.isAttractor();
                            }
                        } else {
                            return true;
                        }
                    } else {
                        if (amenityBlock instanceof Gate.GateBlock) {
                            Gate.GateBlock gateBlock = ((Gate.GateBlock) amenityBlock);

                            return !amenityBlock.isAttractor() && !gateBlock.isSpawner();
                        } else {
                            return !amenityBlock.isAttractor();
                        }
                    }
                } else {
                    if (parent instanceof Gate) {
                        if (amenityBlock instanceof Gate.GateBlock) {
                            Gate.GateBlock gateBlock = ((Gate.GateBlock) amenityBlock);

                            return !amenityBlock.isAttractor() && !gateBlock.isSpawner();
                        } else {
                            return !amenityBlock.isAttractor();
                        }
                    } else {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }

/*        return amenityBlock != null
                && (
                !amenityBlock.getParent().equals(this.goalAmenity)
                        && (
                        !(amenityBlock.getParent() instanceof Gate)
                                || (amenityBlock.getParent() instanceof Gate) && !amenityBlock.isAttractor()
                )
        );*/

//        return amenityBlock != null && amenityBlock.getParent() instanceof Obstacle;
    }

    // Check if there is a clear line of sight from one point to another
    private boolean hasClearLineOfSight(
            Coordinates sourceCoordinates,
            Coordinates targetCoordinates,
            boolean includeStartingPatch
    ) {
        // First of all, check if the target has an obstacle
        // If it does, then no need to check what is between the two points
        if (hasObstacle(this.currentFloor.getPatch(targetCoordinates))) {
            return false;
        }

        final double resolution = 0.2;

        final double distanceToTargetCoordinates = Coordinates.distance(sourceCoordinates, targetCoordinates);
        final double headingToTargetCoordinates = Coordinates.headingTowards(sourceCoordinates, targetCoordinates);

        Patch startingPatch = this.currentFloor.getPatch(sourceCoordinates);

        Coordinates currentPosition = new Coordinates(sourceCoordinates);
        double distanceCovered = 0.0;

        // Keep looking for blocks while there is still distance to cover
        while (distanceCovered <= distanceToTargetCoordinates) {
            if (includeStartingPatch || !this.currentFloor.getPatch(currentPosition).equals(startingPatch)) {
                // Check if there is an obstacle in the current position
                // If there is, return early
                if (hasObstacle(this.currentFloor.getPatch(currentPosition))) {
                    return false;
                }
            }

            // If there isn't any, move towards the target coordinates with the given increment
            currentPosition = this.getFuturePosition(
                    currentPosition,
                    headingToTargetCoordinates,
                    resolution
            );

            distanceCovered += resolution;
        }

        // The target has been reached without finding an obstacle, so there is a clear line of sight between the two
        // given points
        return true;
    }

    // Check if this passenger comes before the given passenger
    private boolean comesBefore(Passenger passenger) {
        if (this.goalQueueObject != null) {
            List<Passenger> goalQueue = this.goalQueueObject.getPassengersQueueing();

            if (goalQueue.size() >= 2) {
                int thisPassengerIndex = goalQueue.indexOf(this.parent);
                int otherPassengerIndex = goalQueue.indexOf(passenger);

                if (thisPassengerIndex != -1 && otherPassengerIndex != -1) {
                    return thisPassengerIndex < otherPassengerIndex;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // Update the passenger's recent patches
    private void updateRecentPatches(Patch currentPatch, final int timeElapsedExpiration) {
        List<Patch> patchesToForget = new ArrayList<>();

        // Update the time elapsed in all of the recent patches
        for (Map.Entry<Patch, Integer> recentPatchesAndTimeElapsed : this.recentPatches.entrySet()) {
            this.recentPatches.put(recentPatchesAndTimeElapsed.getKey(), recentPatchesAndTimeElapsed.getValue() + 1);

            // Remove all patches that are equal to the expiration time given
            if (recentPatchesAndTimeElapsed.getValue() == timeElapsedExpiration) {
                patchesToForget.add(recentPatchesAndTimeElapsed.getKey());
            }
        }

        // If there is a new patch to add or update to the recent patch list, do so
        if (currentPatch != null) {
            // The time lapsed value of any patch added or updated will always be zero, as it means this patch has been
            // recently encountered by this passenger
            this.recentPatches.put(currentPatch, 0);
        }

        // Remove all patches set to be forgotten
        for (Patch patchToForget : patchesToForget) {
            this.recentPatches.remove(patchToForget);
        }
    }

    // Mark this passenger as ready to exit the station
    public void prepareForStationExit() {
        this.isReadyToExit = true;
    }

    public enum Disposition {
        BOARDING("Going to ride a train"),
        RIDING_TRAIN("Riding train"),
        ALIGHTING("Going out of the station");

        private final String name;

        Disposition(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum State {
        WALKING,
        IN_QUEUEABLE,
        IN_QUEUE,
        IN_NONQUEUEABLE,
        IN_TRAIN,
    }

    public enum Action {
        /* Walking actions */
        WILL_QUEUE,
        WILL_ASCEND,
        WILL_DESCEND,
        EXITING_STATION,
        REROUTING,
        /* In queue actions */
        ASSEMBLING,
        QUEUEING,
        HEADING_TO_QUEUEABLE,
        HEADING_TO_TRAIN_DOOR,
        WAITING_FOR_TRAIN,
        /* In queueable actions */
        SECURITY_CHECKING,
        TRANSACTING_TICKET,
        USING_TICKET,
        BOARDING_TRAIN
        /* In non queueable */,
        ASCENDING,
        DESCENDING,
        /* Train actions */
        RIDING_TRAIN
    }

    public enum TravelDirection {
        NORTHBOUND("Northbound"),
        SOUTHBOUND("Southbound"),
        EASTBOUND("Eastbound"),
        WESTBOUND("Westbound");

        private final String name;

        // Convert the given track direction to the travel direction
        public static TravelDirection convertToTravelDirection(
                TrainSystem trainSystem,
                Track.Direction trackDirection
        ) {
            switch (trainSystem.getTrainSystemInformation().getName()) {
                case "LRT-1":
                case "MRT-3":
                    if (trackDirection == Track.Direction.NORTHBOUND) {
                        return TravelDirection.SOUTHBOUND;
                    } else if (trackDirection == Track.Direction.SOUTHBOUND) {
                        return TravelDirection.NORTHBOUND;
                    } else {
                        return null;
                    }
                case "LRT-2":
                    if (trackDirection == Track.Direction.NORTHBOUND) {
                        return TravelDirection.EASTBOUND;
                    } else if (trackDirection == Track.Direction.SOUTHBOUND) {
                        return TravelDirection.WESTBOUND;
                    }
                default:
                    return null;
            }
        }


        TravelDirection(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

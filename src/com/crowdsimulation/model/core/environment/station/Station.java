package com.crowdsimulation.model.core.environment.station;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.agent.passenger.movement.RoutePlan;
import com.crowdsimulation.model.core.agent.passenger.movement.pathfinding.DirectoryResult;
import com.crowdsimulation.model.core.agent.passenger.movement.pathfinding.MultipleFloorPassengerPath;
import com.crowdsimulation.model.core.agent.passenger.movement.pathfinding.PassengerPath;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Track;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.NonObstacle;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairShaft;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;
import com.crowdsimulation.model.simulator.cache.DistanceCache;
import com.crowdsimulation.model.simulator.cache.MultipleFloorPatchCache;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Station extends BaseStationObject implements Environment {
    public static final long serialVersionUID = -1547427240063646235L;

    // Default station name
    private static final String DEFAULT_STATION_NAME = "New station";

    // File extension
    public static final String STATION_LAYOUT_FILE_EXTENSION = ".stn";

    // Denotes the parent station of this layout
    private com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station;

    // The name of the station
    private String name;

    // Denotes the dimensions of this station
    private final int rows;
    private final int columns;

    // Each station contains a list of floors
    private final List<Floor> floors;

    // And a list of portal shafts associated with this station
    // These lists belong here as portal shafts transcend single floors
    private final List<StairShaft> stairShafts;
    private final List<EscalatorShaft> escalatorShafts;
    private final List<ElevatorShaft> elevatorShafts;

    // Also maintain listings of each type of portal in each floor
    private final HashMap<Floor, List<StairPortal>> stairPortalsByFloor;
    private final HashMap<Floor, List<EscalatorPortal>> escalatorPortalsByFloor;
    private final HashMap<Floor, List<ElevatorPortal>> elevatorPortalsByFloor;

    // Take note of which floor each amenity is located
    private final HashMap<Class<? extends Amenity>, HashSet<Floor>> amenityFloorIndex;

    // Take note of clusters of certain amenities
    private final HashMap<Floor, HashMap<Class<? extends Amenity>, List<AmenityCluster>>> amenityClustersByFloor;
    private final HashMap<Amenity, AmenityCluster> amenityClusterByAmenity;

    private final HashMap<Floor, List<AmenityCluster>> amenityClustersByFloorAssorted;
    private final HashMap<Amenity, AmenityCluster> amenityClusterByAmenityAssorted;

    // The list of passengers in this station
    private final CopyOnWriteArrayList<Passenger> passengersInStation;

    // Caches for optimized performance
    private final MultipleFloorPatchCache multipleFloorPatchCache;
    private final DistanceCache distanceCache;

    // Denotes if this station is supposed to be interpreted as a run-only station
    private boolean isRunOnly;

    public Station(String name, int rows, int columns) {
        this.station = null;

        this.name = name;
        this.floors = Collections.synchronizedList(new ArrayList<>());

        this.rows = rows;
        this.columns = columns;

        this.stairShafts = Collections.synchronizedList(new ArrayList<>());
        this.escalatorShafts = Collections.synchronizedList(new ArrayList<>());
        this.elevatorShafts = Collections.synchronizedList(new ArrayList<>());

        this.stairPortalsByFloor = new HashMap<>();
        this.escalatorPortalsByFloor = new HashMap<>();
        this.elevatorPortalsByFloor = new HashMap<>();

        this.amenityFloorIndex = new HashMap<>();

        this.amenityClustersByFloor = new HashMap<>();
        this.amenityClusterByAmenity = new HashMap<>();

        this.amenityClustersByFloorAssorted = new HashMap<>();
        this.amenityClusterByAmenityAssorted = new HashMap<>();

        this.passengersInStation = new CopyOnWriteArrayList<>();

        int multiFloorPathCacheCapacity = 50;
        this.multipleFloorPatchCache = new MultipleFloorPatchCache(multiFloorPathCacheCapacity);

        int distanceCacheCapacity = 50;
        this.distanceCache = new DistanceCache(distanceCacheCapacity);

        this.isRunOnly = false;

        // Initially, the station has one floor
        Floor.addFloor(this, 0, rows, columns);
    }

    public Station(int rows, int columns) {
        this.station = null;

        this.name = DEFAULT_STATION_NAME;
        this.floors = Collections.synchronizedList(new ArrayList<>());

        this.rows = rows;
        this.columns = columns;

        this.stairShafts = Collections.synchronizedList(new ArrayList<>());
        this.escalatorShafts = Collections.synchronizedList(new ArrayList<>());
        this.elevatorShafts = Collections.synchronizedList(new ArrayList<>());

        this.stairPortalsByFloor = new HashMap<>();
        this.escalatorPortalsByFloor = new HashMap<>();
        this.elevatorPortalsByFloor = new HashMap<>();

        this.amenityFloorIndex = new HashMap<>();

        this.amenityClustersByFloor = new HashMap<>();
        this.amenityClusterByAmenity = new HashMap<>();

        this.amenityClustersByFloorAssorted = new HashMap<>();
        this.amenityClusterByAmenityAssorted = new HashMap<>();

        this.passengersInStation = new CopyOnWriteArrayList<>();

        int multiFloorPathCacheCapacity = 50;
        this.multipleFloorPatchCache = new MultipleFloorPatchCache(multiFloorPathCacheCapacity);

        int distanceCacheCapacity = 50;
        this.distanceCache = new DistanceCache(distanceCacheCapacity);

        this.isRunOnly = false;

        // Initially, the station has one floor
        Floor.addFloor(this, 0, rows, columns);
    }

    public com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station getStation() {
        return station;
    }

    public void setStation(com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station station) {
        this.station = station;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public HashMap<Class<? extends Amenity>, HashSet<Floor>> getAmenityFloorIndex() {
        return amenityFloorIndex;
    }

    public HashMap<Floor, HashMap<Class<? extends Amenity>, List<AmenityCluster>>> getAmenityClustersByFloor() {
        return amenityClustersByFloor;
    }

    public HashMap<Amenity, AmenityCluster> getAmenityClusterByAmenity() {
        return amenityClusterByAmenity;
    }

    public HashMap<Floor, List<AmenityCluster>> getAmenityClustersByFloorAssorted() {
        return amenityClustersByFloorAssorted;
    }

    public HashMap<Amenity, AmenityCluster> getAmenityClusterByAmenityAssorted() {
        return amenityClusterByAmenityAssorted;
    }

    public List<StairShaft> getStairShafts() {
        return stairShafts;
    }

    public List<EscalatorShaft> getEscalatorShafts() {
        return escalatorShafts;
    }

    public List<ElevatorShaft> getElevatorShafts() {
        return elevatorShafts;
    }

    public HashMap<Floor, List<StairPortal>> getStairPortalsByFloor() {
        return stairPortalsByFloor;
    }

    public HashMap<Floor, List<EscalatorPortal>> getEscalatorPortalsByFloor() {
        return escalatorPortalsByFloor;
    }

    public HashMap<Floor, List<ElevatorPortal>> getElevatorPortalsByFloor() {
        return elevatorPortalsByFloor;
    }

    public CopyOnWriteArrayList<Passenger> getPassengersInStation() {
        return passengersInStation;
    }

    public MultipleFloorPatchCache getMultiFloorPathCache() {
        return multipleFloorPatchCache;
    }

    public DistanceCache getDistanceCache() {
        return distanceCache;
    }

    public boolean isRunOnly() {
        return isRunOnly;
    }

    public void setRunOnly(boolean runOnly) {
        isRunOnly = runOnly;
    }

    // Assemble this station's amenity-floor index
    private void assembleAmenityFloorIndex() {
        this.amenityFloorIndex.clear();

        this.amenityFloorIndex.put(StationGate.class, new HashSet<>());
        this.amenityFloorIndex.put(Security.class, new HashSet<>());
        this.amenityFloorIndex.put(TicketBooth.class, new HashSet<>());
        this.amenityFloorIndex.put(Turnstile.class, new HashSet<>());
        this.amenityFloorIndex.put(TrainDoor.class, new HashSet<>());

        for (Floor floor : this.floors) {
            if (!floor.getStationGates().isEmpty()) {
                for (StationGate stationGate : floor.getStationGates()) {
                    this.amenityFloorIndex.get(StationGate.class).add(
                            stationGate.getAmenityBlocks().get(0).getPatch().getFloor()
                    );
                }
            }

            if (!floor.getSecurities().isEmpty()) {
                for (Security security : floor.getSecurities()) {
                    this.amenityFloorIndex.get(Security.class).add(
                            security.getAmenityBlocks().get(0).getPatch().getFloor()
                    );
                }
            }

            if (!floor.getTicketBooths().isEmpty()) {
                for (TicketBooth ticketBooth : floor.getTicketBooths()) {
                    this.amenityFloorIndex.get(TicketBooth.class).add(
                            ticketBooth.getAmenityBlocks().get(0).getPatch().getFloor()
                    );
                }
            }

            if (!floor.getTurnstiles().isEmpty()) {
                for (Turnstile turnstile : floor.getTurnstiles()) {
                    this.amenityFloorIndex.get(Turnstile.class).add(
                            turnstile.getAmenityBlocks().get(0).getPatch().getFloor()
                    );
                }
            }

            if (!floor.getTrainDoors().isEmpty()) {
                for (TrainDoor trainDoor : floor.getTrainDoors()) {
                    this.amenityFloorIndex.get(TrainDoor.class).add(
                            trainDoor.getAmenityBlocks().get(0).getPatch().getFloor()
                    );
                }
            }
        }
    }

    // Assemble this station's clusters
    private void assembleClusters() {
        // Each floor will contain its own clusters
        for (Floor floor : this.getFloors()) {
            // Prepare the list of clusters for this floor
            HashMap<Class<? extends Amenity>, List<AmenityCluster>> amenityClassClusterMapInFloor = new HashMap<>();
            this.getAmenityClustersByFloor().put(floor, amenityClassClusterMapInFloor);

            // Prepare the station gate clusters
            List<AmenityCluster> stationGateClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(StationGate.class, stationGateClusters);

            for (StationGate stationGate : floor.getStationGates()) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (stationGateClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster stationGateCluster = new AmenityCluster(floor, StationGate.class);
                    stationGateClusters.add(stationGateCluster);

                    // Add the first amenity into the cluster
                    stationGateCluster.getAmenities().add(stationGate);
                    this.amenityClusterByAmenity.put(stationGate, stationGateCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : stationGateClusters) {
                        for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                            PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                    stationGate.getAttractors().get(0).getPatch(),
                                    amenityInCluster.getAttractors().get(0).getPatch(),
                                    true,
                                    false,
                                    false
                            );

                            // If a path to this amenity in the cluster has been found, check if the distance to this
                            // amenity is the closest one found so far
                            if (pathToAmenity != null) {
                                if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                    minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                    // Furthermore, if this distance is already within the allowed maximum distance from the
                                    // cluster, as cluster has immediately been found
                                    // The maximum distance allowable of an amenity from the other amenities in its cluster
                                    final double maximumAllowableDistanceFromCluster = 5.0;

                                    if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                        hasFoundCluster = true;

                                        amenityCluster.getAmenities().add(stationGate);
                                        this.amenityClusterByAmenity.put(stationGate, amenityCluster);

                                        break;
                                    }
                                }
                            }
                        }

                        if (hasFoundCluster) {
                            break;
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster stationGateCluster = new AmenityCluster(floor, StationGate.class);
                        stationGateClusters.add(stationGateCluster);

                        stationGateCluster.getAmenities().add(stationGate);
                        this.amenityClusterByAmenity.put(stationGate, stationGateCluster);
                    }
                }
            }

            // Prepare the security clusters
            List<AmenityCluster> securityClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(Security.class, securityClusters);

            for (Security security : floor.getSecurities()) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (securityClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster securityCluster = new AmenityCluster(floor, Security.class);
                    securityClusters.add(securityCluster);

                    // Add the first amenity into the cluster
                    securityCluster.getAmenities().add(security);
                    this.amenityClusterByAmenity.put(security, securityCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : securityClusters) {
                        for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                            PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                    security.getAttractors().get(0).getPatch(),
                                    amenityInCluster.getAttractors().get(0).getPatch(),
                                    true,
                                    false,
                                    false
                            );

                            // If a path to this amenity in the cluster has been found, check if the distance to this
                            // amenity is the closest one found so far
                            if (pathToAmenity != null) {
                                if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                    minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                    // Furthermore, if this distance is already within the allowed maximum distance from the
                                    // cluster, as cluster has immediately been found
                                    // The maximum distance allowable of an amenity from the other amenities in its cluster
                                    final double maximumAllowableDistanceFromCluster = 5.0;

                                    if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                        hasFoundCluster = true;

                                        amenityCluster.getAmenities().add(security);
                                        this.amenityClusterByAmenity.put(security, amenityCluster);

                                        break;
                                    }
                                }
                            }
                        }

                        if (hasFoundCluster) {
                            break;
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster securityCluster = new AmenityCluster(floor, Security.class);
                        securityClusters.add(securityCluster);

                        securityCluster.getAmenities().add(security);
                        this.amenityClusterByAmenity.put(security, securityCluster);
                    }
                }
            }

            // Prepare the ticket booth clusters
            List<AmenityCluster> ticketBoothClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(TicketBooth.class, ticketBoothClusters);

            for (TicketBooth ticketBooth : floor.getTicketBooths()) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (ticketBoothClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster ticketBoothCluster = new AmenityCluster(floor, TicketBooth.class);
                    ticketBoothClusters.add(ticketBoothCluster);

                    // Add the first amenity into the cluster
                    ticketBoothCluster.getAmenities().add(ticketBooth);
                    this.amenityClusterByAmenity.put(ticketBooth, ticketBoothCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : ticketBoothClusters) {
                        for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                            PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                    ticketBooth.getAttractors().get(0).getPatch(),
                                    amenityInCluster.getAttractors().get(0).getPatch(),
                                    true,
                                    false,
                                    false
                            );

                            // If a path to this amenity in the cluster has been found, check if the distance to this
                            // amenity is the closest one found so far
                            if (pathToAmenity != null) {
                                if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                    minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                    // Furthermore, if this distance is already within the allowed maximum distance from
                                    // the cluster, as cluster has immediately been found
                                    // The maximum distance allowable of an amenity from the other amenities in its cluster
                                    final double maximumAllowableDistanceFromCluster = 10.0;

                                    if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                        hasFoundCluster = true;

                                        amenityCluster.getAmenities().add(ticketBooth);
                                        this.amenityClusterByAmenity.put(ticketBooth, amenityCluster);

                                        break;
                                    }
                                }
                            }
                        }

                        if (hasFoundCluster) {
                            break;
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster ticketBoothCluster = new AmenityCluster(floor, TicketBooth.class);
                        ticketBoothClusters.add(ticketBoothCluster);

                        ticketBoothCluster.getAmenities().add(ticketBooth);
                        this.amenityClusterByAmenity.put(ticketBooth, ticketBoothCluster);
                    }
                }
            }

            // Prepare the turnstile clusters
            List<AmenityCluster> turnstileClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(Turnstile.class, turnstileClusters);

            for (Turnstile turnstile : floor.getTurnstiles()) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (turnstileClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster turnstileCluster = new AmenityCluster(floor, Turnstile.class);
                    turnstileClusters.add(turnstileCluster);

                    // Add the first amenity into the cluster
                    turnstileCluster.getAmenities().add(turnstile);
                    this.amenityClusterByAmenity.put(turnstile, turnstileCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : turnstileClusters) {
                        // Aside from checking whether this turnstile is connected to this cluster, also check if its
                        // direction matches it
                        Turnstile turnstileInCluster = ((Turnstile) amenityCluster.getAmenities().get(0));

                        if (
                                turnstile.getTurnstileTravelDirections().equals(
                                        turnstileInCluster.getTurnstileTravelDirections()
                                )
                                        && turnstile.getTurnstileMode().equals(
                                        turnstileInCluster.getTurnstileMode()
                                )
                        ) {
                            for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                                PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                        turnstile.getAttractors().get(0).getPatch(),
                                        amenityInCluster.getAttractors().get(0).getPatch(),
                                        true,
                                        false,
                                        false
                                );

                                // If a path to this amenity in the cluster has been found, check if the distance to this
                                // amenity is the closest one found so far
                                if (pathToAmenity != null) {
                                    if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                        minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                        // Furthermore, if this distance is already within the allowed maximum distance from
                                        // the cluster, as cluster has immediately been found
                                        // The maximum distance allowable of an amenity from the other amenities in its cluster
                                        final double maximumAllowableDistanceFromCluster = 5.0;

                                        if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                            hasFoundCluster = true;

                                            amenityCluster.getAmenities().add(turnstile);
                                            this.amenityClusterByAmenity.put(turnstile, amenityCluster);

                                            break;
                                        }
                                    }
                                }
                            }

                            if (hasFoundCluster) {
                                break;
                            }
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster turnstileCluster = new AmenityCluster(floor, Turnstile.class);
                        turnstileClusters.add(turnstileCluster);

                        turnstileCluster.getAmenities().add(turnstile);
                        this.amenityClusterByAmenity.put(turnstile, turnstileCluster);
                    }
                }
            }

            // Prepare the train door clusters
            List<AmenityCluster> trainDoorClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(TrainDoor.class, trainDoorClusters);

//            List<AmenityCluster> leftTrainDoorClusters = new ArrayList<>();
//            List<AmenityCluster> rightTrainDoorClusters = new ArrayList<>();

//            List<TrainDoor> leftTrainDoors = new ArrayList<>();
//            List<TrainDoor> rightTrainDoors = new ArrayList<>();

            // Get the half-length of the station
            final double stationMidsection = this.columns / 2.0 * Patch.PATCH_SIZE_IN_SQUARE_METERS;

            for (TrainDoor trainDoor : floor.getTrainDoors()) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (trainDoorClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster trainDoorCluster = new AmenityCluster(floor, TrainDoor.class);
                    trainDoorClusters.add(trainDoorCluster);

                    // Add the first amenity into the cluster
                    trainDoorCluster.getAmenities().add(trainDoor);
                    this.amenityClusterByAmenity.put(trainDoor, trainDoorCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : trainDoorClusters) {
                        // Aside from checking whether this turnstile is connected to this cluster, also check if its
                        // direction matches it
                        TrainDoor trainDoorInCluster = ((TrainDoor) amenityCluster.getAmenities().get(0));

                        if (
                                trainDoor.getPlatformDirection().equals(
                                        trainDoorInCluster.getPlatformDirection()
                                )
                        ) {
                            for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                                PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                        trainDoor.getAttractors().get(0).getPatch(),
                                        amenityInCluster.getAttractors().get(0).getPatch(),
                                        true,
                                        false,
                                        false
                                );

                                // If a path to this amenity in the cluster has been found, check if the distance to this
                                // amenity is the closest one found so far
                                if (pathToAmenity != null) {
                                    double trainDoorXValue
                                            = trainDoor.getAttractors().get(0).getPatch().getPatchCenterCoordinates()
                                            .getX();
                                    boolean trainDoorOnLeft = trainDoorXValue < stationMidsection;

                                    double trainDoorInClusterXValue
                                            = trainDoorInCluster.getAttractors().get(0).getPatch()
                                            .getPatchCenterCoordinates().getX();
                                    boolean trainDoorInClusterOnLeft = trainDoorInClusterXValue < stationMidsection;

                                    if (
                                            trainDoorOnLeft && trainDoorInClusterOnLeft
                                                    || !trainDoorOnLeft && !trainDoorInClusterOnLeft
                                    ) {
                                        if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                            minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                            // Furthermore, if this distance is already within the allowed maximum
                                            // distance from the cluster, as cluster has immediately been found
                                            // The maximum distance allowable of an amenity from the other amenities in
                                            // its cluster
                                            final double maximumAllowableDistanceFromCluster = Double.MAX_VALUE;

                                            if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                                hasFoundCluster = true;

                                                amenityCluster.getAmenities().add(trainDoor);
                                                this.amenityClusterByAmenity.put(trainDoor, amenityCluster);

                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (hasFoundCluster) {
                                break;
                            }
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster trainDoorCluster = new AmenityCluster(floor, TrainDoor.class);
                        trainDoorClusters.add(trainDoorCluster);

                        trainDoorCluster.getAmenities().add(trainDoor);
                        this.amenityClusterByAmenity.put(trainDoor, trainDoorCluster);
                    }
                }
            }

            // Generate the portal clusters
            List<StairPortal> stairPortals = this.getStairPortalsByFloor().get(floor);
            List<EscalatorPortal> escalatorPortals = this.getEscalatorPortalsByFloor().get(floor);
            List<ElevatorPortal> elevatorPortals = this.getElevatorPortalsByFloor().get(floor);

            // Prepare the stair portal clusters
            List<AmenityCluster> stairPortalClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(StairPortal.class, stairPortalClusters);

            for (StairPortal stairPortal : stairPortals) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (stairPortalClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster stairPortalCluster = new AmenityCluster(floor, StairPortal.class);
                    stairPortalClusters.add(stairPortalCluster);

                    // Add the first amenity into the cluster
                    stairPortalCluster.getAmenities().add(stairPortal);
                    this.amenityClusterByAmenity.put(stairPortal, stairPortalCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : stairPortalClusters) {
                        for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                            PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                    stairPortal.getAttractors().get(0).getPatch(),
                                    amenityInCluster.getAttractors().get(0).getPatch(),
                                    true,
                                    false,
                                    false
                            );

                            // If a path to this amenity in the cluster has been found, check if the distance to this
                            // amenity is the closest one found so far
                            if (pathToAmenity != null) {
                                if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                    minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                    // Furthermore, if this distance is already within the allowed maximum distance from the
                                    // cluster, as cluster has immediately been found
                                    // The maximum distance allowable of an amenity from the other amenities in its cluster
                                    final double maximumAllowableDistanceFromCluster = 5.0;

                                    if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                        hasFoundCluster = true;

                                        amenityCluster.getAmenities().add(stairPortal);
                                        this.amenityClusterByAmenity.put(stairPortal, amenityCluster);

                                        break;
                                    }
                                }
                            }
                        }

                        if (hasFoundCluster) {
                            break;
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster stairPortalCluster = new AmenityCluster(floor, StairPortal.class);
                        stairPortalClusters.add(stairPortalCluster);

                        stairPortalCluster.getAmenities().add(stairPortal);
                        this.amenityClusterByAmenity.put(stairPortal, stairPortalCluster);
                    }
                }
            }

            // Prepare the escalator portal clusters
            List<AmenityCluster> escalatorPortalClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(EscalatorPortal.class, escalatorPortalClusters);

            for (EscalatorPortal escalatorPortal : escalatorPortals) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (escalatorPortalClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster escalatorPortalCluster = new AmenityCluster(floor, EscalatorPortal.class);
                    escalatorPortalClusters.add(escalatorPortalCluster);

                    // Add the first amenity into the cluster
                    escalatorPortalCluster.getAmenities().add(escalatorPortal);
                    this.amenityClusterByAmenity.put(escalatorPortal, escalatorPortalCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : escalatorPortalClusters) {
                        for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                            PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                    escalatorPortal.getAttractors().get(0).getPatch(),
                                    amenityInCluster.getAttractors().get(0).getPatch(),
                                    true,
                                    false,
                                    false
                            );

                            // If a path to this amenity in the cluster has been found, check if the distance to this
                            // amenity is the closest one found so far
                            if (pathToAmenity != null) {
                                if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                    minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                    // Furthermore, if this distance is already within the allowed maximum distance from the
                                    // cluster, as cluster has immediately been found
                                    // The maximum distance allowable of an amenity from the other amenities in its cluster
                                    final double maximumAllowableDistanceFromCluster = 5.0;

                                    if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                        hasFoundCluster = true;

                                        amenityCluster.getAmenities().add(escalatorPortal);
                                        this.amenityClusterByAmenity.put(escalatorPortal, amenityCluster);

                                        break;
                                    }
                                }
                            }
                        }

                        if (hasFoundCluster) {
                            break;
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster escalatorPortalCluster = new AmenityCluster(floor, EscalatorPortal.class);
                        escalatorPortalClusters.add(escalatorPortalCluster);

                        escalatorPortalCluster.getAmenities().add(escalatorPortal);
                        this.amenityClusterByAmenity.put(escalatorPortal, escalatorPortalCluster);
                    }
                }
            }

            // Prepare the elevator portal clusters
            List<AmenityCluster> elevatorPortalClusters = new ArrayList<>();
            amenityClassClusterMapInFloor.put(ElevatorPortal.class, elevatorPortalClusters);

            for (ElevatorPortal elevatorPortal : elevatorPortals) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (elevatorPortalClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster elevatorPortalCluster = new AmenityCluster(floor, ElevatorPortal.class);
                    elevatorPortalClusters.add(elevatorPortalCluster);

                    // Add the first amenity into the cluster
                    elevatorPortalCluster.getAmenities().add(elevatorPortal);
                    this.amenityClusterByAmenity.put(elevatorPortal, elevatorPortalCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters within the allowable
                    // distance
                    boolean hasFoundCluster = false;
                    double minimumDistanceFromClusterFound = Double.MAX_VALUE;

                    for (AmenityCluster amenityCluster : elevatorPortalClusters) {
                        for (Amenity amenityInCluster : amenityCluster.getAmenities()) {
                            PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                    elevatorPortal.getAttractors().get(0).getPatch(),
                                    amenityInCluster.getAttractors().get(0).getPatch(),
                                    true,
                                    false,
                                    false
                            );

                            // If a path to this amenity in the cluster has been found, check if the distance to this
                            // amenity is the closest one found so far
                            if (pathToAmenity != null) {
                                if (pathToAmenity.getDistance() < minimumDistanceFromClusterFound) {
                                    minimumDistanceFromClusterFound = pathToAmenity.getDistance();

                                    // Furthermore, if this distance is already within the allowed maximum distance from the
                                    // cluster, as cluster has immediately been found
                                    // The maximum distance allowable of an amenity from the other amenities in its cluster
                                    final double maximumAllowableDistanceFromCluster = 5.0;

                                    if (pathToAmenity.getDistance() < maximumAllowableDistanceFromCluster) {
                                        hasFoundCluster = true;

                                        amenityCluster.getAmenities().add(elevatorPortal);
                                        this.amenityClusterByAmenity.put(elevatorPortal, amenityCluster);

                                        break;
                                    }
                                }
                            }
                        }

                        if (hasFoundCluster) {
                            break;
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster within the allowable
                    // distance, create a cluster with only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster elevatorPortalCluster = new AmenityCluster(floor, ElevatorPortal.class);
                        elevatorPortalClusters.add(elevatorPortalCluster);

                        // Add the first amenity into the cluster
                        elevatorPortalCluster.getAmenities().add(elevatorPortal);
                        this.amenityClusterByAmenity.put(elevatorPortal, elevatorPortalCluster);
                    }
                }
            }

            // Assemble the assorted clusters for this floor
            // Compile all amenities in this floor
            // Create the clusters
            List<AmenityCluster> amenityClusters = new ArrayList<>();
            this.amenityClustersByFloorAssorted.put(floor, amenityClusters);

            List<Amenity> amenitiesInFloor = this.retrieveAmenitiesInFloor(floor);

            for (Amenity amenityInFloor : amenitiesInFloor) {
                // If this is the first amenity in the cluster, create a new cluster containing the first element
                if (amenityClusters.isEmpty()) {
                    // Create the new first-time cluster
                    AmenityCluster amenityCluster = new AmenityCluster(floor, null);
                    amenityClusters.add(amenityCluster);

                    // Add the first amenity into the cluster
                    amenityCluster.getAmenities().add(amenityInFloor);
                    this.amenityClusterByAmenityAssorted.put(amenityInFloor, amenityCluster);
                } else {
                    // Check if this amenity is connected to one of the already existing clusters
                    boolean hasFoundCluster = false;

                    // TODO: Fix clustering issues
                    // Blockables should be part of multiple clusters
                    for (AmenityCluster amenityCluster : amenityClusters) {
                        PassengerPath pathToAmenity = PassengerMovement.computePathWithinFloor(
                                amenityInFloor.getAttractors().get(0).getPatch(),
                                amenityCluster.getAmenities().get(0).getAttractors().get(0).getPatch(),
                                true,
                                false,
                                true
                        );

                        // If a path to this amenity in the cluster has been found, add it to the cluster
                        if (pathToAmenity != null) {
                            hasFoundCluster = true;

                            amenityCluster.getAmenities().add(amenityInFloor);
                            this.amenityClusterByAmenityAssorted.put(amenityInFloor, amenityCluster);

                            break;
                        }
                    }

                    // If this amenity is not found to be connectable in any pre-existing cluster, create a cluster with
                    // only this amenity in it
                    if (!hasFoundCluster) {
                        AmenityCluster amenityCluster = new AmenityCluster(floor, null);
                        amenityClusters.add(amenityCluster);

                        // Add the first amenity into the cluster
                        amenityCluster.getAmenities().add(amenityInFloor);
                        this.amenityClusterByAmenityAssorted.put(amenityInFloor, amenityCluster);
                    }
                }
            }
        }
    }

    // Validate the floor fields of each amenity in the station
    public static boolean validateFloorFieldsInStation(Station station) {
        // For each floor in the station, validate the amenities in there which have floor fields
        List<Security> securities;
        List<TicketBooth> ticketBooths;
        List<Turnstile> turnstiles;
        List<TrainDoor> trainDoors;

        for (Floor floor : station.getFloors()) {
            securities = floor.getSecurities();
            ticketBooths = floor.getTicketBooths();
            turnstiles = floor.getTurnstiles();
            trainDoors = floor.getTrainDoors();

            for (Security security : securities) {
                if (!security.isFloorFieldsComplete()) {
                    return false;
                }
            }

            for (TicketBooth ticketBooth : ticketBooths) {
                if (!ticketBooth.isFloorFieldsComplete()) {
                    return false;
                }
            }

            for (Turnstile turnstile : turnstiles) {
                if (!turnstile.isFloorFieldsComplete()) {
                    return false;
                }
            }

            for (TrainDoor trainDoor : trainDoors) {
                if (!trainDoor.isFloorFieldsComplete()) {
                    return false;
                }
            }
        }

        // Then validate the elevator portals in this station
        for (ElevatorShaft elevatorShaft : station.getElevatorShafts()) {
            if (!((ElevatorPortal) elevatorShaft.getUpperPortal()).isFloorFieldsComplete()) {
                return false;
            }
        }

        // This part will only be reached once we pass every test
        return true;
    }

    // Clear all caches of this station
    public void clearCaches() {
        this.getMultiFloorPathCache().clear();
        this.getDistanceCache().clear();

        for (Floor floor : this.getFloors()) {
            floor.getPathCache().clear();
        }
    }

    // Clear the directories of each portal in the station
    private void clearDirectories() {
        for (StairShaft stairShaft : this.stairShafts) {
            stairShaft.getLowerPortal().getDirectory().clear();
            stairShaft.getUpperPortal().getDirectory().clear();
        }

        for (EscalatorShaft escalatorShaft : this.escalatorShafts) {
            escalatorShaft.getLowerPortal().getDirectory().clear();
            escalatorShaft.getUpperPortal().getDirectory().clear();
        }

        for (ElevatorShaft elevatorShaft : this.elevatorShafts) {
            elevatorShaft.getLowerPortal().getDirectory().clear();
            elevatorShaft.getUpperPortal().getDirectory().clear();
        }
    }

    // Clear the clusters in the station
    private void clearClusters() {
        this.amenityClustersByFloor.clear();
        this.amenityClusterByAmenity.clear();

        this.amenityClustersByFloorAssorted.clear();
        this.amenityClusterByAmenityAssorted.clear();
    }

    // Clear all patches which will never be reached by the passengers (areas outside the station)
    public void removeIrrelevantPatches() {
        // Clear all non-reachable patches in this floor
        for (Floor floor : this.floors) {

            // Collect all reachable patches
            // Start by collecting all the patches of the amenities in the floor
            Set<Patch> patchesReachable = new HashSet<>();
            Set<Patch> patchesToAdd = new HashSet<>();

            List<Amenity> amenities = this.retrieveAmenitiesInFloor(floor);

            for (Amenity amenity : amenities) {
                for (Amenity.AmenityBlock amenityBlock : amenity.getAttractors()) {
                    patchesReachable.add(amenityBlock.getPatch());
                }
            }

            int previousSize = 0;
            int newSize = patchesReachable.size();

            // Keep expanding the frontier until its size stops changing
            while (newSize > previousSize) {
                // Remember the previous size
                previousSize = newSize;

                // Expand the frontier
                for (Patch patch : patchesReachable) {
                    if (
                            patch.getAmenityBlock() == null
                                    || amenities.contains(patch.getAmenityBlock().getParent())
                                    || patch.getAmenityBlock().getParent() instanceof Track
                    ) {
                        List<Patch> neighborsToAdd = new ArrayList<>(patch.getNeighbors());

                        patchesToAdd.addAll(neighborsToAdd);
                    }

                    patchesToAdd.add(patch);
                }

                patchesReachable.addAll(patchesToAdd);
                patchesToAdd.clear();

                // Take note of the new size
                newSize = patchesReachable.size();
            }

            // Find the non-reachable patches
            List<Patch> nonReachablePatches = Arrays.stream(floor.getPatches())
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());

            nonReachablePatches.removeAll(patchesReachable);

            // Set the patches that were in this position to null
            Patch[][] patchesInFloor = floor.getPatches();

            for (Patch patch : nonReachablePatches) {
                if (patch != null) {
                    patchesInFloor[patch.getMatrixPosition().getRow()][patch.getMatrixPosition().getColumn()] = null;
                    floor.getAmenityPatchSet().remove(patch);
                }
            }
        }
    }

    private List<Amenity> retrieveAmenitiesInFloor(Floor floor) {
        List<Amenity> amenitiesInFloor = new ArrayList<>();

        amenitiesInFloor.addAll(floor.getStationGates());
        amenitiesInFloor.addAll(floor.getSecurities());
        amenitiesInFloor.addAll(floor.getTicketBooths());
        amenitiesInFloor.addAll(floor.getTurnstiles());
        amenitiesInFloor.addAll(floor.getTrainDoors());

        amenitiesInFloor.addAll(this.getStairPortalsByFloor().get(floor));
        amenitiesInFloor.addAll(this.getEscalatorPortalsByFloor().get(floor));
        amenitiesInFloor.addAll(this.getElevatorPortalsByFloor().get(floor));

        return amenitiesInFloor;
    }

    // Thoroughly validate the layout of the station
    public static StationValidationResult validateStationLayoutDeeply(Station station) {
        // Clear caches
        station.clearCaches();

        // Clear directories
        station.clearDirectories();

        // Clear clusters
        station.clearClusters();

        // For each floor in the station, collect station gates
        List<StationGate> stationGates = new ArrayList<>();

        for (Floor floor : station.getFloors()) {
            stationGates.addAll(floor.getStationGates());
        }

        // If there are no station gates at all, we now know that this station is instantly invalid
        if (stationGates.isEmpty()) {
            return new StationValidationResult(
                    StationValidationResult.StationValidationResultType.NO_STATION_GATES,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        // For each floor in the station, collect train doors
        List<TrainDoor> trainDoors = new ArrayList<>();

        for (Floor floor : station.getFloors()) {
            trainDoors.addAll(floor.getTrainDoors());
        }

        // If there are no train doors at all, we now know that this station is instantly invalid
        if (trainDoors.isEmpty()) {
            return new StationValidationResult(
                    StationValidationResult.StationValidationResultType.NO_TRAIN_DOORS,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        // For each floor in the station, collect train tracks
        List<Track> trainTracks = new ArrayList<>();

        for (Floor floor : station.getFloors()) {
            trainTracks.addAll(floor.getTracks());
        }

        // If there are no train tracks at all, we now know that this station is instantly invalid
        if (trainTracks.isEmpty()) {
            return new StationValidationResult(
                    StationValidationResult.StationValidationResultType.NO_TRAIN_TRACKS,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } else {
            // Collect all the directions of the train tracks and the train doors
            HashSet<PassengerMovement.TravelDirection> trainTrackDirections = new HashSet<>();
            HashSet<PassengerMovement.TravelDirection> trainDoorDirections = new HashSet<>();

            for (Track track : trainTracks) {
                trainTrackDirections.add(track.getTrackDirection());
            }

            for (TrainDoor trainDoor : trainDoors) {
                trainDoorDirections.add(trainDoor.getPlatformDirection());
            }

            // See if all directions of the train doors and train tracks match
            if (!trainTrackDirections.equals(trainDoorDirections)) {
                // All train door directions that are not in the train track directions
                HashSet<PassengerMovement.TravelDirection> trainTrackDirectionsCopy;
                HashSet<PassengerMovement.TravelDirection> trainDoorDirectionsCopy;

                trainTrackDirectionsCopy = new HashSet<>(trainTrackDirections);
                trainDoorDirectionsCopy = new HashSet<>(trainDoorDirections);

                trainTrackDirectionsCopy.removeAll(trainDoorDirections);
                trainDoorDirectionsCopy.removeAll(trainTrackDirections);

                if (trainTrackDirectionsCopy.size() < trainDoorDirectionsCopy.size()) {
                    return new StationValidationResult(
                            StationValidationResult.StationValidationResultType.TRAIN_TRACKS_MISMATCH,
                            (PassengerMovement.TravelDirection) trainDoorDirectionsCopy.toArray()[0],
                            null,
                            null,
                            null,
                            Track.class
                    );
                } else {
                    return new StationValidationResult(
                            StationValidationResult.StationValidationResultType.TRAIN_TRACKS_MISMATCH,
                            (PassengerMovement.TravelDirection) trainTrackDirectionsCopy.toArray()[0],
                            null,
                            null,
                            null,
                            TrainDoor.class
                    );
                }
            }
        }

        // Assemble this station's amenity-floor index
        station.assembleAmenityFloorIndex();

        // Assemble this station's clusters
        station.assembleClusters();

        // For each station gate, for each direction that the station gate may spawn, check if a single journey and
        // stored value card holder passenger may be able to navigate from this station gate to its corresponding
        // train door
        boolean isStoredValueCardHolder;

        StationValidationResult stationValidationResult;

        RoutePlan boardingRoutePlanSingleJourney;
        RoutePlan boardingRoutePlanStoredValue;
        RoutePlan alightingRoutePlan;

        // Take note of all the validated amenities' clusters
        List<AmenityCluster> validatedSpawnClusters = new ArrayList<>();

        // Check the validity for each station gate in the station, for each boarding passenger
        boolean isEntranceStationGateFound = false;

        for (StationGate stationGate : stationGates) {
            // Check if a station gate belonging to the same cluster as this station gate has already been validated
            // before
            // If so, there is no need to validate another station gate of the same cluster anymore
            AmenityCluster stationGateCluster = station.getAmenityClusterByAmenity().get(stationGate);

            if (!validatedSpawnClusters.contains(stationGateCluster)) {
                // For the validation of the boarding route, only deal with patches which allow entrances
                if (stationGate.getStationGateMode().equals(StationGate.StationGateMode.EXIT)) {
                    continue;
                } else {
                    isEntranceStationGateFound = true;
                }

                // Get the directions of the passengers that may be spawned by this station gate
                List<PassengerMovement.TravelDirection> travelDirectionsSpawnable
                        = stationGate.getStationGatePassengerTravelDirections();

                // Check the validity for each travel direction spawnable by this station gate
                for (PassengerMovement.TravelDirection travelDirectionSpawnable : travelDirectionsSpawnable) {
                    // Initialize a potential passenger's route plans
                    isStoredValueCardHolder = false;
                    boardingRoutePlanSingleJourney = new RoutePlan(isStoredValueCardHolder, true);

                    isStoredValueCardHolder = true;
                    boardingRoutePlanStoredValue = new RoutePlan(isStoredValueCardHolder, true);

                    // Check the validity for boarding single journey ticket holders
                    List<Class<? extends Amenity>> boardingRoutePlanSingleJourneyAsList = new ArrayList<>();

                    boardingRoutePlanSingleJourneyAsList.add(Security.class);
                    boardingRoutePlanSingleJourney.getCurrentRoutePlan().forEachRemaining(
                            boardingRoutePlanSingleJourneyAsList::add
                    );

                    stationValidationResult = Station.validatePath(
                            station,
                            boardingRoutePlanSingleJourneyAsList,
                            0,
                            travelDirectionSpawnable,
                            TicketBooth.TicketType.SINGLE_JOURNEY,
                            PassengerMovement.Disposition.BOARDING,
                            stationGate,
                            boardingRoutePlanSingleJourney.getCurrentAmenityClass(),
                            TrainDoor.class
                    );

                    // If an error is found, return it
                    if (
                            stationValidationResult.getStationValidationResultType()
                                    != StationValidationResult.StationValidationResultType.NO_ERROR
                    ) {
                        return stationValidationResult;
                    }

                    // Check the validity for boarding stored value ticket holders
                    List<Class<? extends Amenity>> boardingRoutePlanStoredValueAsList = new ArrayList<>();

                    boardingRoutePlanStoredValueAsList.add(Security.class);
                    boardingRoutePlanStoredValue.getCurrentRoutePlan().forEachRemaining(
                            boardingRoutePlanStoredValueAsList::add
                    );

                    stationValidationResult = Station.validatePath(
                            station,
                            boardingRoutePlanStoredValueAsList,
                            0,
                            travelDirectionSpawnable,
                            TicketBooth.TicketType.STORED_VALUE,
                            PassengerMovement.Disposition.BOARDING,
                            stationGate,
                            boardingRoutePlanStoredValue.getCurrentAmenityClass(),
                            TrainDoor.class
                    );

                    // If an error is found, return it
                    if (
                            stationValidationResult.getStationValidationResultType()
                                    != StationValidationResult.StationValidationResultType.NO_ERROR
                    ) {
                        return stationValidationResult;
                    }
                }

                // Get the cluster this station gate belongs to
                validatedSpawnClusters.add(stationGateCluster);
            }
        }

        // If, until this point, no entrance station gates were found, this station is invalid
        if (!isEntranceStationGateFound) {
            return new StationValidationResult(
                    StationValidationResult.StationValidationResultType.NO_STATION_GATES,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        validatedSpawnClusters.clear();

        // Check the validity for each train door in the station, for each alighting passenger
        for (TrainDoor trainDoor : trainDoors) {
            // Check if a train door belonging to the same cluster as this train door has already been validated before
            // If so, there is no need to validate another train door of the same cluster anymore
            AmenityCluster trainDoorCluster = station.getAmenityClusterByAmenity().get(trainDoor);

            if (!validatedSpawnClusters.contains(trainDoorCluster)) {
                // Initialize a potential passenger's route plans
                alightingRoutePlan = new RoutePlan(false, false);

                // Get the direction of the passengers that are spawned by this train door
                PassengerMovement.TravelDirection travelDirectionSpawnable = trainDoor.getPlatformDirection();

                // Check the validity for alighting passengers
                List<Class<? extends Amenity>> aligthingRoutePlanAsList = new ArrayList<>();

                aligthingRoutePlanAsList.add(Turnstile.class);
                alightingRoutePlan.getCurrentRoutePlan().forEachRemaining(
                        aligthingRoutePlanAsList::add
                );

                stationValidationResult = Station.validatePath(
                        station,
                        aligthingRoutePlanAsList,
                        0,
                        travelDirectionSpawnable,
                        null,
                        PassengerMovement.Disposition.ALIGHTING,
                        trainDoor,
                        alightingRoutePlan.getCurrentAmenityClass(),
                        StationGate.class
                );

                // If an error is found, return it
                if (
                        stationValidationResult.getStationValidationResultType()
                                != StationValidationResult.StationValidationResultType.NO_ERROR
                ) {
                    return stationValidationResult;
                }

                // Get the cluster this station gate belongs to
                validatedSpawnClusters.add(trainDoorCluster);
            }
        }

        // If this point is reached, no errors are found at all, and so the station is valid
        return new StationValidationResult(
                StationValidationResult.StationValidationResultType.NO_ERROR,
                null,
                null,
                null,
                null,
                null
        );
    }

    // Check if a path exists from the current amenity to an amenity of the given class
    public static StationValidationResult validatePath(
            Station station,
            List<Class<? extends Amenity>> routePlan,
            int routePlanIndex,
            PassengerMovement.TravelDirection travelDirectionSpawnable,
            TicketBooth.TicketType ticketType,
            PassengerMovement.Disposition disposition,
            Amenity currentAmenity,
            Class<? extends Amenity> nextAmenityClass,
            Class<? extends Amenity> terminalAmenityClass
    ) {
        List<DirectoryResult> directoryResults;

        // Check if a path exists from the current goal to any amenity that comes after it
        directoryResults = getPortalsToGoal(
                station,
                travelDirectionSpawnable,
                PassengerMovement.Disposition.BOARDING,
                currentAmenity,
                nextAmenityClass
        );

        // If there are no paths found to any next amenity, this station is instantly invalid
        if (directoryResults.isEmpty()) {
            return new StationValidationResult(
                    StationValidationResult.StationValidationResultType.UNREACHABLE,
                    travelDirectionSpawnable,
                    ticketType,
                    disposition,
                    currentAmenity,
                    nextAmenityClass
            );
        } else {
            // For each portal in the goal portals list, attach the goal amenity to its directory,
            // signifying that to get to that amenity, this goal portal is the way
            for (DirectoryResult directoryResult : directoryResults) {
                AmenityCluster originAmenityCluster = station.getAmenityClusterByAmenity().get(currentAmenity);
                AmenityCluster destinationAmenityCluster = station.getAmenityClusterByAmenity().get(
                        directoryResult.getGoalAmenity()
                );

                for (Portal portal : directoryResult.getPortals()) {
                    portal.getDirectory().put(
                            travelDirectionSpawnable,
                            nextAmenityClass,
                            originAmenityCluster,
                            destinationAmenityCluster,
                            directoryResult.getDistance()
                    );

//                    previousAmenity = portal;
                    originAmenityCluster = station.getAmenityClusterByAmenity().get(portal.getPair());
                }

                // If possible, go one level deeper and go look for the succeeding amenity class
                Amenity goalAmenity = directoryResult.getGoalAmenity();

                StationValidationResult stationValidationResult;

                // If this class of this amenity is already the last one expected, do not go down one level anymore
                if (goalAmenity.getClass().equals(terminalAmenityClass)) {
                    stationValidationResult = new StationValidationResult(
                            StationValidationResult.StationValidationResultType.NO_ERROR,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                } else {
                    Class<? extends Amenity> nextNextAmenityClass = routePlan.get(routePlanIndex + 1);

                    stationValidationResult = validatePath(
                            station,
                            routePlan,
                            routePlanIndex + 1,
                            travelDirectionSpawnable,
                            ticketType,
                            disposition,
                            directoryResult.getGoalAmenity(),
                            nextNextAmenityClass,
                            terminalAmenityClass
                    );
                }

                // If an unreachable amenity was found in this branch, immediately return an error
                if (
                        stationValidationResult.getStationValidationResultType()
                                != StationValidationResult.StationValidationResultType.NO_ERROR
                ) {
                    return stationValidationResult;
                }
            }

            // No errors found at all
            return new StationValidationResult(
                    StationValidationResult.StationValidationResultType.NO_ERROR,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    // Get all possible paths to all reachable amenities of the given class
    // Pass through portals if necessary
    private static List<DirectoryResult> getPortalsToGoal(
            Station station,
            PassengerMovement.TravelDirection travelDirection,
            PassengerMovement.Disposition disposition,
            Amenity currentAmenity,
            Class<? extends Amenity> nextAmenityClass
    ) {
        List<DirectoryResult> directoryResults = new ArrayList<>();

        // Based on the passenger's current direction and route plan, get the next amenity class to be sought
        // Given the next amenity class, collect the floors which have this amenity class
        Set<Floor> floors = station.getAmenityFloorIndex().get(nextAmenityClass);

        assert !floors.isEmpty();

        // Compile all amenities in each floor
        List<Amenity> amenityListInFloors = new ArrayList<>();

        for (Floor floorToExplore : floors) {
            amenityListInFloors.addAll(floorToExplore.getAmenityList(nextAmenityClass));
        }

        // Compute the distance from the current position to the possible goal, taking into account possible
        // paths passing through other floors
        // Take note of all the validated amenities' clusters
        List<AmenityCluster> validatedGoalClusters = new ArrayList<>();

        for (Amenity candidateGoal : amenityListInFloors) {
            // Check if an amenity belonging to the same cluster as this amenity has already been validated
            // before
            // If so, there is no need to validate another amenity of the same cluster anymore
            AmenityCluster amenityCluster = station.getAmenityClusterByAmenity().get(candidateGoal);

            if (!validatedGoalClusters.contains(amenityCluster)) {
                // Only consider amenities that are enabled
                NonObstacle nonObstacle = ((NonObstacle) candidateGoal);

                if (!nonObstacle.isEnabled()) {
                    continue;
                }

                // Filter the amenity search space only to what is compatible with this passenger
                if (candidateGoal instanceof StationGate) {
                    // If the goal of the passenger is a station gate, this means the passenger is leaving
                    // So only consider station gates which allow exits and accepts the passenger's direction
                    StationGate stationGateExit = ((StationGate) candidateGoal);

                    if (stationGateExit.getStationGateMode() == StationGate.StationGateMode.ENTRANCE) {
                        continue;
                    } else {
                        if (!stationGateExit.getStationGatePassengerTravelDirections().contains(travelDirection)) {
                            continue;
                        }
                    }
                } else if (candidateGoal instanceof Turnstile) {
                    // Only consider turnstiles which match this passenger's disposition and travel direction
                    Turnstile turnstile = ((Turnstile) candidateGoal);

                    if (!turnstile.getTurnstileTravelDirections().contains(travelDirection)) {
                        continue;
                    }

                    if (turnstile.getTurnstileMode() != Turnstile.TurnstileMode.BIDIRECTIONAL) {
                        if (
                                turnstile.getTurnstileMode() == Turnstile.TurnstileMode.BOARDING
                                        && disposition.equals(PassengerMovement.Disposition.ALIGHTING)
                                        || turnstile.getTurnstileMode() == Turnstile.TurnstileMode.ALIGHTING
                                        && disposition.equals(PassengerMovement.Disposition.BOARDING)
                        ) {
                            continue;
                        }
                    }
                } else if (candidateGoal instanceof TrainDoor) {
                    // Only consider train doors which match this passenger's travel direction
                    TrainDoor trainDoor = ((TrainDoor) candidateGoal);

                    if (trainDoor.getPlatformDirection() != travelDirection) {
                        continue;
                    }
                }

                Patch currentAmenityPatch = currentAmenity.getAmenityBlocks().get(0).getPatch();

                List<MultipleFloorPassengerPath> multipleFloorPassengerPaths
                        = PassengerMovement.computePathAcrossFloors(
                        currentAmenityPatch.getFloor(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        currentAmenityPatch,
                        candidateGoal.getAmenityBlocks().get(0)
                );

                boolean pathsFound = false;

                for (MultipleFloorPassengerPath multipleFloorPassengerPath : multipleFloorPassengerPaths) {
                    if (multipleFloorPassengerPath != null) {
                        pathsFound = true;

                        directoryResults.add(
                                new DirectoryResult(
                                        multipleFloorPassengerPath.getPortals(),
                                        candidateGoal,
                                        multipleFloorPassengerPath.getDistance()
                                )
                        );
                    }
                }

                if (pathsFound) {
                    validatedGoalClusters.add(amenityCluster);
                }
            }
        }

        return directoryResults;
    }

    // Denotes the orientation of this station
    public enum StationOrientation {
        SIDE_PLATFORM("Side platform"),
        ISLAND_PLATFORM("Island platform");

        private final String name;

        StationOrientation(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static class StationValidationResult {
        private final StationValidationResultType stationValidationResultType;
        private final PassengerMovement.TravelDirection travelDirection;
        private final TicketBooth.TicketType ticketType;
        private final PassengerMovement.Disposition disposition;
        private final Amenity lastValidAmenity;
        private final Class<? extends Amenity> nextAmenityClass;

        public enum StationValidationResultType {
            NO_ERROR,
            NO_STATION_GATES,
            NO_TRAIN_DOORS,
            NO_TRAIN_TRACKS,
            TRAIN_TRACKS_MISMATCH,
            UNREACHABLE
        }

        public StationValidationResult(
                StationValidationResultType stationValidationResultType,
                PassengerMovement.TravelDirection travelDirection,
                TicketBooth.TicketType ticketType,
                PassengerMovement.Disposition disposition,
                Amenity lastValidAmenity,
                Class<? extends Amenity> nextAmenityClass
        ) {
            this.stationValidationResultType = stationValidationResultType;
            this.travelDirection = travelDirection;
            this.ticketType = ticketType;
            this.disposition = disposition;
            this.lastValidAmenity = lastValidAmenity;
            this.nextAmenityClass = nextAmenityClass;
        }

        public StationValidationResultType getStationValidationResultType() {
            return stationValidationResultType;
        }

        @Override
        public String toString() {
            String amenityClassName;
            String errorMessageTemplate;

            switch (this.stationValidationResultType) {
                case NO_STATION_GATES:
                    return "This station does not have any station gates.";
                case NO_TRAIN_DOORS:
                    return "This station does not have any train boarding areas.";
                case NO_TRAIN_TRACKS:
                    return "This station does not have any train tracks.";
                case TRAIN_TRACKS_MISMATCH:
                    amenityClassName = getAmenityName(this.nextAmenityClass);

                    errorMessageTemplate = "There are no {0}s for the {1}";

                    if (this.nextAmenityClass == Track.class) {
                        errorMessageTemplate += " train boarding areas.";
                    } else {
                        errorMessageTemplate += " train tracks.";
                    }

                    errorMessageTemplate = errorMessageTemplate.replace("{0}", amenityClassName);
                    errorMessageTemplate
                            = errorMessageTemplate.replace("{1}", this.travelDirection.toString().toLowerCase());

                    return errorMessageTemplate;
                case UNREACHABLE:
                    String lastValidAmenityClassName = getAmenityName(this.lastValidAmenity.getClass());

                    amenityClassName = getAmenityName(this.nextAmenityClass);

                    errorMessageTemplate = "No {0}s are reachable from the {1} on floor #{2} at patch {3} for {4}" +
                            " passengers who are {5}.";

                    errorMessageTemplate = errorMessageTemplate.replace("{0}", amenityClassName);
                    errorMessageTemplate = errorMessageTemplate.replace("{1}", lastValidAmenityClassName);

                    Floor floor = this.lastValidAmenity.getAmenityBlocks().get(0).getPatch().getFloor();

                    errorMessageTemplate = errorMessageTemplate.replace(
                            "{2}",
                            String.valueOf(floor.getStation().getFloors().indexOf(floor) + 1)
                    );
                    errorMessageTemplate = errorMessageTemplate.replace(
                            "{3}",
                            String.valueOf(this.lastValidAmenity.getAttractors().get(0).getPatch())
                    );
                    errorMessageTemplate = errorMessageTemplate.replace(
                            "{4}",
                            this.travelDirection.toString().toLowerCase()
                    );
                    errorMessageTemplate = errorMessageTemplate.replace(
                            "{5}",
                            this.disposition.toString().toLowerCase()
                    );

                    return errorMessageTemplate;
                default:
                    return "The station layout is valid.";
            }
        }

        private String getAmenityName(Class<? extends Amenity> amenityClass) {
            String amenityClassName = "";

            if (amenityClass == StationGate.class) {
                amenityClassName = "station entrance/exit";
            } else if (amenityClass == StairPortal.class) {
                amenityClassName = "staircase";
            } else if (amenityClass == EscalatorPortal.class) {
                amenityClassName = "escalator";
            } else if (amenityClass == ElevatorPortal.class) {
                amenityClassName = "elevator";
            } else if (amenityClass == Security.class) {
                amenityClassName = "security gate";
            } else if (amenityClass == TicketBooth.class) {
                amenityClassName = "ticket booth";
            } else if (amenityClass == Turnstile.class) {
                amenityClassName = "turnstile";
            } else if (amenityClass == TrainDoor.class) {
                amenityClassName = "train boarding area";
            } else if (amenityClass == Track.class) {
                amenityClassName = "train track";
            }

            return amenityClassName;
        }
    }

    private static class AmenityClassFloorPair {
        private final Class<? extends Amenity> amenityClass;
        private final Floor floor;

        public AmenityClassFloorPair(Class<? extends Amenity> amenityClass, Floor floor) {
            this.amenityClass = amenityClass;
            this.floor = floor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AmenityClassFloorPair that = (AmenityClassFloorPair) o;
            return amenityClass.equals(that.amenityClass) && floor.equals(that.floor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(amenityClass, floor);
        }
    }

    public static class AmenityFloorPair {
        private final Amenity amenity;
        private final Floor floor;

        public AmenityFloorPair(Amenity amenity, Floor floor) {
            this.amenity = amenity;
            this.floor = floor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AmenityFloorPair that = (AmenityFloorPair) o;
            return amenity.equals(that.amenity) && floor.equals(that.floor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(amenity, floor);
        }
    }

    public static class AmenityCluster implements Environment {
        // Denotes the floor where this cluster is
        private final Floor floor;

        // Denotes the amenity class of the amenities in this cluster
        private final Class<? extends Amenity> amenityClass;

        // Denotes the amenities belonging to this cluster
        private final List<Amenity> amenities;

        public AmenityCluster(Floor floor, Class<? extends Amenity> amenityClass) {
            this.floor = floor;
            this.amenityClass = amenityClass;
            this.amenities = new ArrayList<>();
        }

        public Floor getFloor() {
            return floor;
        }

        public Class<? extends Amenity> getAmenityClass() {
            return amenityClass;
        }

        public List<Amenity> getAmenities() {
            return amenities;
        }
    }
}

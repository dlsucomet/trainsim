package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate;

import com.crowdsimulation.controller.graphics.amenity.editor.StationGateEditor;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.footprint.GateFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.StationGateGraphic;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerTripInformation;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StationGate extends Gate {
    public static final long serialVersionUID = -6190517689755159925L;

    // Denotes the chance of generating a passenger per second
    private double chancePerSecond;

    // Denotes the mode of this station gate (whether it's entry/exit only, or both)
    private StationGateMode stationGateMode;

    // Denotes the direction of passengers this station gate produces
    private final List<PassengerMovement.TravelDirection> stationGatePassengerTravelDirections;

    // Denotes the number of passengers who are supposed to enter the station gate, but cannot
    private int passengerBacklogCount;

    // Factory for station gate creation
    public static final StationGateFactory stationGateFactory;

    // Handles how the station gate is displayed
    private final StationGateGraphic stationGateGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint stationGateFootprint;

    // Denotes the editor of this amenity
    public static final StationGateEditor stationGateEditor;

    static {
        stationGateFactory = new StationGateFactory();

        // Initialize this amenity's footprints
        stationGateFootprint = new AmenityFootprint();

        // Up view
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlockN10;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlockN11;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock01;

        AmenityFootprint.Rotation upView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.UP);

        upBlock00 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                0,
                StationGate.class,
                false,
                true,
                false
        );

        upBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                0,
                StationGate.class,
                false,
                false,
                true
        );

        upBlockN11 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                1,
                StationGate.class,
                false,
                false,
                false
        );

        upBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                1,
                StationGate.class,
                true,
                false,
                false
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlockN10);
        upView.getAmenityBlockTemplates().add(upBlockN11);
        upView.getAmenityBlockTemplates().add(upBlock01);

        stationGateFootprint.addRotation(upView);

        // Right view
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock01;
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock10;
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock11;

        AmenityFootprint.Rotation rightView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.RIGHT);

        rightBlock00 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                0,
                0,
                StationGate.class,
                false,
                true,
                true
        );

        rightBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                0,
                1,
                StationGate.class,
                false,
                false,
                false
        );

        rightBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                0,
                StationGate.class,
                true,
                false,
                false
        );

        rightBlock11 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                1,
                StationGate.class,
                false,
                false,
                false
        );

        rightView.getAmenityBlockTemplates().add(rightBlock00);
        rightView.getAmenityBlockTemplates().add(rightBlock01);
        rightView.getAmenityBlockTemplates().add(rightBlock10);
        rightView.getAmenityBlockTemplates().add(rightBlock11);

        stationGateFootprint.addRotation(rightView);

        // Down view
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock0N1;
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock1N1;
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock10;

        AmenityFootprint.Rotation downView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.DOWN);

        downBlock00 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                0,
                0,
                StationGate.class,
                false,
                true,
                false
        );

        downBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                0,
                -1,
                StationGate.class,
                true,
                false,
                true
        );

        downBlock1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                -1,
                StationGate.class,
                false,
                false,
                false
        );

        downBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                0,
                StationGate.class,
                false,
                false,
                false
        );

        downView.getAmenityBlockTemplates().add(downBlock00);
        downView.getAmenityBlockTemplates().add(downBlock0N1);
        downView.getAmenityBlockTemplates().add(downBlock1N1);
        downView.getAmenityBlockTemplates().add(downBlock10);

        stationGateFootprint.addRotation(downView);

        // Left view
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlockN1N1;
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlockN10;
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlock0N1;

        AmenityFootprint.Rotation leftView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.LEFT);

        leftBlock00 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                0,
                0,
                StationGate.class,
                false,
                true,
                false
        );

        leftBlockN1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                -1,
                StationGate.class,
                false,
                false,
                true
        );

        leftBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                0,
                StationGate.class,
                true,
                false,
                false
        );

        leftBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                0,
                -1,
                StationGate.class,
                false,
                false,
                false
        );

        leftView.getAmenityBlockTemplates().add(leftBlock00);
        leftView.getAmenityBlockTemplates().add(leftBlockN1N1);
        leftView.getAmenityBlockTemplates().add(leftBlockN10);
        leftView.getAmenityBlockTemplates().add(leftBlock0N1);

        stationGateFootprint.addRotation(leftView);

        // Initialize the editor
        stationGateEditor = new StationGateEditor();
    }

    protected StationGate(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            double chancePerSecond,
            StationGateMode stationGateMode,
            List<PassengerMovement.TravelDirection> stationGatePassengerTravelDirections
    ) {
        super(amenityBlocks, enabled);

        this.chancePerSecond = chancePerSecond;
        this.stationGateMode = stationGateMode;
        this.stationGatePassengerTravelDirections = new ArrayList<>();

        setPassengerTravelDirectionsSpawned(stationGatePassengerTravelDirections);

        passengerBacklogCount = 0;

        this.stationGateGraphic = new StationGateGraphic(this);
    }

    public double getChancePerSecond() {
        return chancePerSecond;
    }

    public void setChancePerSecond(double chancePerSecond) {
        this.chancePerSecond = chancePerSecond;
    }

    public StationGateMode getStationGateMode() {
        return stationGateMode;
    }

    public void setStationGateMode(StationGateMode stationGateMode) {
        this.stationGateMode = stationGateMode;
    }

    public List<PassengerMovement.TravelDirection> getStationGatePassengerTravelDirections() {
        return stationGatePassengerTravelDirections;
    }

    public void setPassengerTravelDirectionsSpawned(List<PassengerMovement.TravelDirection> travelDirections) {
        this.stationGatePassengerTravelDirections.clear();
        this.stationGatePassengerTravelDirections.addAll(travelDirections);
    }

    public int getPassengerBacklogCount() {
        return passengerBacklogCount;
    }

    public void incrementBacklogs() {
        this.passengerBacklogCount++;
    }

    public void resetBacklogs() {
        this.passengerBacklogCount = 0;
    }

    @Override
    public String toString() {
        return "Station entrance/exit" + ((this.enabled) ? "" : " (disabled)");
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.stationGateGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.stationGateGraphic.getGraphicLocation();
    }

    @Override
    // Spawn a passenger in this position
    public Passenger spawnPassenger() {
        Station station = this.getAmenityBlocks().get(0).getPatch().getFloor().getStation().getStation();

        boolean patchesFree = isGateFree();

        GateBlock spawner = this.getSpawners().get(0);

//        // If that spawner is free from passengers, generate one
//        if (spawner.getPatch().getPassengers().isEmpty()) {
//            return Passenger.passengerFactory.create(spawner.getPatch(), passengerTripInformation);
//        } else {
//            // No passengers were generated because the spawner was blocked
//            return null;
//        }

        if (station != null || patchesFree) {
            return Passenger.passengerFactory.create(
                    spawner.getPatch(),
                    null,
                    !patchesFree
//                !spawner.getPatch().getPassengers().isEmpty()
            );
        } else {
            return null;
        }
    }

    // TODO: Use abstraction
    public Passenger spawnPassenger(PassengerTripInformation passengerTripInformation) {
        Station station = this.getAmenityBlocks().get(0).getPatch().getFloor().getStation().getStation();

        boolean patchesFree = isGateFree();

        GateBlock spawner = this.getSpawners().get(0);

//        // If that spawner is free from passengers, generate one
//        if (spawner.getPatch().getPassengers().isEmpty()) {
//            return Passenger.passengerFactory.create(spawner.getPatch(), passengerTripInformation);
//        } else {
//            // No passengers were generated because the spawner was blocked
//            return null;
//        }

        if (station != null || patchesFree) {
            return Passenger.passengerFactory.create(
                    spawner.getPatch(),
                    passengerTripInformation,
                    !patchesFree
//                !spawner.getPatch().getPassengers().isEmpty()
            );
        } else {
            return null;
        }
    }

    public boolean isGateFree() {
        HashSet<Patch> patchesToCheck = new HashSet<>();

        boolean patchesFree = true;

        // Check if all attractors and spawners in this amenity have no passengers
        for (AmenityBlock attractor : this.getAttractors()) {
            patchesToCheck.add(attractor.getPatch());
            patchesToCheck.addAll(attractor.getPatch().getNeighbors());
        }

        for (GateBlock spawner : this.getSpawners()) {
            patchesToCheck.add(spawner.getPatch());
            patchesToCheck.addAll(spawner.getPatch().getNeighbors());
        }

        for (Patch patchToCheck : patchesToCheck) {
            if (!patchToCheck.getPassengers().isEmpty()) {
                patchesFree = false;

                break;
            }
        }

        return patchesFree;
    }

    // Spawn a passenger from the backlogs
    public Passenger spawnPassengerFromBacklogs(boolean willDrawFromPassengerList, boolean forceEntry) {
        // Get the backlog queue for this station gate
        Station station = this.getAmenityBlocks().get(0).getPatch().getFloor().getStation().getStation();

        if (station != null) {
            List<Passenger> stationGateQueue = station.getPassengerBacklogs().get(this);

            // If the backlog queue isn't empty, check if this gate is free from passengers
            if (!stationGateQueue.isEmpty()) {
                // If this gate is free from other passengers, get one from the backlog queue
                if (forceEntry || this.isGateFree()) {
                    Passenger passenger = stationGateQueue.remove(0);

                    Patch spawnPatch = this.getSpawners().get(0).getPatch();

                    if (willDrawFromPassengerList) {
                        passenger.initializePassengerMovementWithPassengerTripInformation(
                                spawnPatch,
                                passenger.getPassengerTripInformation(),
                                passenger.getPassengerInformation().getDemographic()
                        );
                    } else {
                        passenger.initializePassengerMovementWithoutPassengerTripInformation(
                                spawnPatch,
                                passenger.getDemographic()
                        );
                    }

                    return passenger;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }

//        if (this.passengerBacklogCount > 0) {
//            Passenger passengerSpawned = this.spawnPassenger();
//
//            if (passengerSpawned != null) {
//                this.passengerBacklogCount--;
//            }
//
//            return passengerSpawned;
//        }
//
//        return null;
    }

//    // Spawn a passenger from the backlogs
//    // TODO: Offload to station gate itself
//    public Passenger spawnPassengerFromBacklogs(List<PassengerTripInformation> backlogs) {
//        if (this.passengerBacklogCount > 0) {
//            PassengerTripInformation backlog = backlogs.get(0);
//
//            Passenger passengerSpawned = this.spawnPassenger(backlog);
//
//            if (passengerSpawned != null) {
//                this.passengerBacklogCount--;
//
//                backlogs.remove(backlog);
//            }
//
//            return passengerSpawned;
//        }
//
//        return null;
//    }

    // Lists the mode of this station gate (whether it's entry/exit only, or both)
    public enum StationGateMode {
        ENTRANCE("Entrance"),
        EXIT("Exit"),
        ENTRANCE_AND_EXIT("Entrance and exit");

        private final String name;

        StationGateMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    // Station gate block
    public static class StationGateBlock extends GateBlock {
        public static StationGateBlockFactory stationGateBlockFactory;

        static {
            stationGateBlockFactory = new StationGateBlockFactory();
        }

        private StationGateBlock(Patch patch, boolean attractor, boolean spawner, boolean hasGraphic) {
            super(patch, attractor, spawner, hasGraphic);
        }

        // Station gate block factory
        public static class StationGateBlockFactory extends GateBlockFactory {
            @Override
            public StationGateBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new StationGateBlock(
                        patch,
                        attractor,
                        false,
                        hasGraphic
                );
            }

            @Override
            public StationGateBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean spawner,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new StationGateBlock(
                        patch,
                        attractor,
                        spawner,
                        hasGraphic
                );
            }
        }
    }

    // Station gate factory
    public static class StationGateFactory extends GateFactory {
        public StationGate create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                double chancePerSecond,
                StationGateMode stationGateMode,
                List<PassengerMovement.TravelDirection> stationGatePassengerTravelDirections
        ) {
            return new StationGate(
                    amenityBlocks,
                    enabled,
                    chancePerSecond,
                    stationGateMode,
                    stationGatePassengerTravelDirections
            );
        }
    }
}

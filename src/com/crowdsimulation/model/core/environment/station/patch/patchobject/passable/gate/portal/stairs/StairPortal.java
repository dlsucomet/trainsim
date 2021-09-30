package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.footprint.GateFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.StairGraphic;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;
import com.crowdsimulation.model.simulator.Simulator;

import java.util.HashSet;
import java.util.List;

public class StairPortal extends Portal {
    // Denotes the stair shaft which contains this stair portal
    private final StairShaft stairShaft;

    // Factory for stair portal creation
    public static final StairPortalFactory stairPortalFactory;

    // Handles how this stair portal is displayed
    private final StairGraphic stairGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint stairPortalFootprint;

    static {
        stairPortalFactory = new StairPortalFactory();

        // Initialize this amenity's footprints
        stairPortalFootprint = new AmenityFootprint();

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
                StairPortal.class,
                false,
                true,
                false
        );

        upBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                0,
                StairPortal.class,
                false,
                false,
                true
        );

        upBlockN11 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                1,
                StairPortal.class,
                false,
                false,
                false
        );

        upBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                1,
                StairPortal.class,
                true,
                false,
                false
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlockN10);
        upView.getAmenityBlockTemplates().add(upBlockN11);
        upView.getAmenityBlockTemplates().add(upBlock01);

        stairPortalFootprint.addRotation(upView);

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
                StairPortal.class,
                false,
                true,
                true
        );

        rightBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                0,
                1,
                StairPortal.class,
                false,
                false,
                false
        );

        rightBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                0,
                StairPortal.class,
                true,
                false,
                false
        );

        rightBlock11 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                1,
                StairPortal.class,
                false,
                false,
                false
        );

        rightView.getAmenityBlockTemplates().add(rightBlock00);
        rightView.getAmenityBlockTemplates().add(rightBlock01);
        rightView.getAmenityBlockTemplates().add(rightBlock10);
        rightView.getAmenityBlockTemplates().add(rightBlock11);

        stairPortalFootprint.addRotation(rightView);

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
                StairPortal.class,
                false,
                true,
                false
        );

        downBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                0,
                -1,
                StairPortal.class,
                true,
                false,
                true
        );

        downBlock1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                -1,
                StairPortal.class,
                false,
                false,
                false
        );

        downBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                0,
                StairPortal.class,
                false,
                false,
                false
        );

        downView.getAmenityBlockTemplates().add(downBlock00);
        downView.getAmenityBlockTemplates().add(downBlock0N1);
        downView.getAmenityBlockTemplates().add(downBlock1N1);
        downView.getAmenityBlockTemplates().add(downBlock10);

        stairPortalFootprint.addRotation(downView);

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
                StairPortal.class,
                false,
                true,
                false
        );

        leftBlockN1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                -1,
                StairPortal.class,
                false,
                false,
                true
        );

        leftBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                0,
                StairPortal.class,
                true,
                false,
                false
        );

        leftBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                0,
                -1,
                StairPortal.class,
                false,
                false,
                false
        );

        leftView.getAmenityBlockTemplates().add(leftBlock00);
        leftView.getAmenityBlockTemplates().add(leftBlockN1N1);
        leftView.getAmenityBlockTemplates().add(leftBlockN10);
        leftView.getAmenityBlockTemplates().add(leftBlock0N1);

        stairPortalFootprint.addRotation(leftView);
    }

    protected StairPortal(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            Floor floorServed,
            StairShaft stairShaft
    ) {
        super(amenityBlocks, enabled, floorServed);

        this.stairShaft = stairShaft;

        this.stairGraphic = new StairGraphic(this);
    }

    public StairShaft getStairShaft() {
        return stairShaft;
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.stairGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.stairGraphic.getGraphicLocation();
    }

    @Override
    public String toString() {
        String string = "Stair" + ((this.enabled) ? "" : " (disabled)");

        Floor floorServed = this.getPair().getFloorServed();
        int numberFloorServed = Main.simulator.getStation().getFloors().indexOf(floorServed) + 1;

        string += "\n" + "Connects to floor #" + numberFloorServed;

        return string;
    }

    @Override
    public Passenger spawnPassenger() {
        return null;
    }

    @Override
    public Patch emit() {
        HashSet<Patch> patchesToCheck = new HashSet<>();

        // Check if all attractors and spawners in this amenity have no passengers
        for (AmenityBlock attractor : this.getAttractors()) {
            patchesToCheck.add(attractor.getPatch());
//            patchesToCheck.addAll(attractor.getPatch().getNeighbors());
        }

        for (GateBlock spawner : this.getSpawners()) {
            patchesToCheck.add(spawner.getPatch());
//            patchesToCheck.addAll(spawner.getPatch().getNeighbors());
        }

        for (Patch patchToCheck : patchesToCheck) {
            if (!patchToCheck.getPassengers().isEmpty()) {
                Passenger passenger = patchToCheck.getPassengers().get(0);
                Portal goalAmenityAsPortal = passenger.getPassengerMovement().getGoalAmenityAsPortal();

                // If some passengers are spotted, check if they are about to use this portal
                // Only refuse to exit if the blocking passengers are not using this portal
                if (goalAmenityAsPortal == null || !goalAmenityAsPortal.equals(this)) {
                    return null;
                }
            }
        }

        // Randomly choose between the spawner locations in the portal
        int spawnerCount = this.getSpawners().size();
        int randomSpawnerIndex = Simulator.RANDOM_NUMBER_GENERATOR.nextInt(spawnerCount);

        GateBlock spawner = this.getSpawners().get(randomSpawnerIndex);

        // Once this point is reached, return the patch where the passenger will be emitted
        return spawner.getPatch();
    }

    @Override
    public void absorb(Passenger passenger) {
        PassengerMovement passengerMovement = passenger.getPassengerMovement();

        passengerMovement.enterPortal();

        // Set the appropriate passenger state (ascending or descending)
        passengerMovement.setState(PassengerMovement.State.IN_NONQUEUEABLE);

        if (passengerMovement.getAction() == PassengerMovement.Action.WILL_DESCEND) {
            passengerMovement.setAction(PassengerMovement.Action.DESCENDING);

            this.stairShaft.getDescendingQueue().get(this.stairShaft.getDescendingQueue().size() - 1).add(passenger);
            this.stairShaft.incrementPassengersDescending();
        } else {
            passengerMovement.setAction(PassengerMovement.Action.ASCENDING);

            this.stairShaft.getAscendingQueue().get(0).add(passenger);
            this.stairShaft.incrementPassengersAscending();
        }
    }

    // Stair portal block
    public static class StairPortalBlock extends GateBlock {
        public static StairPortalBlockFactory stairPortalBlockFactory;

        static {
            stairPortalBlockFactory = new StairPortalBlockFactory();
        }

        private StairPortalBlock(Patch patch, boolean attractor, boolean spawner, boolean hasGraphic) {
            super(patch, attractor, spawner, hasGraphic);
        }

        // Stair portal block factory
        public static class StairPortalBlockFactory extends GateBlockFactory {
            @Override
            public StairPortalBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new StairPortalBlock(
                        patch,
                        attractor,
                        false,
                        hasGraphic
                );
            }

            @Override
            public StairPortalBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean spawner,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new StairPortalBlock(
                        patch,
                        attractor,
                        spawner,
                        hasGraphic
                );
            }
        }
    }

    // Stair portal factory
    public static class StairPortalFactory extends PortalFactory {
        public StairPortal create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                Floor floorServed,
                StairShaft stairShaft
        ) {
            return new StairPortal(
                    amenityBlocks,
                    enabled,
                    floorServed,
                    stairShaft
            );
        }
    }
}

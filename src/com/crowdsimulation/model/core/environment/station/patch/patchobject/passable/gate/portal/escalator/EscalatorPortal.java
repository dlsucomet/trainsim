package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.footprint.GateFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.EscalatorGraphic;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;
import com.crowdsimulation.model.simulator.Simulator;

import java.util.HashSet;
import java.util.List;

public class EscalatorPortal extends Portal {
    // Denotes the escalator shaft which contains this escalator portal
    private final EscalatorShaft escalatorShaft;

    // Factory for escalator portal creation
    public static final EscalatorPortalFactory escalatorPortalFactory;

    // Handles how this escalator portal is displayed
    private final EscalatorGraphic escalatorGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint escalatorPortalFootprint;

    static {
        escalatorPortalFactory = new EscalatorPortalFactory();

        // Initialize this amenity's footprints
        escalatorPortalFootprint = new AmenityFootprint();

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
                EscalatorPortal.class,
                false,
                true,
                false
        );

        upBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                0,
                EscalatorPortal.class,
                false,
                false,
                true
        );

        upBlockN11 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                1,
                EscalatorPortal.class,
                false,
                false,
                false
        );

        upBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                1,
                EscalatorPortal.class,
                true,
                false,
                false
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlockN10);
        upView.getAmenityBlockTemplates().add(upBlockN11);
        upView.getAmenityBlockTemplates().add(upBlock01);

        escalatorPortalFootprint.addRotation(upView);

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
                EscalatorPortal.class,
                false,
                true,
                true
        );

        rightBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                0,
                1,
                EscalatorPortal.class,
                false,
                false,
                false
        );

        rightBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                0,
                EscalatorPortal.class,
                true,
                false,
                false
        );

        rightBlock11 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                1,
                EscalatorPortal.class,
                false,
                false,
                false
        );

        rightView.getAmenityBlockTemplates().add(rightBlock00);
        rightView.getAmenityBlockTemplates().add(rightBlock01);
        rightView.getAmenityBlockTemplates().add(rightBlock10);
        rightView.getAmenityBlockTemplates().add(rightBlock11);

        escalatorPortalFootprint.addRotation(rightView);

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
                EscalatorPortal.class,
                false,
                true,
                false
        );

        downBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                0,
                -1,
                EscalatorPortal.class,
                true,
                false,
                true
        );

        downBlock1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                -1,
                EscalatorPortal.class,
                false,
                false,
                false
        );

        downBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                0,
                EscalatorPortal.class,
                false,
                false,
                false
        );

        downView.getAmenityBlockTemplates().add(downBlock00);
        downView.getAmenityBlockTemplates().add(downBlock0N1);
        downView.getAmenityBlockTemplates().add(downBlock1N1);
        downView.getAmenityBlockTemplates().add(downBlock10);

        escalatorPortalFootprint.addRotation(downView);

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
                EscalatorPortal.class,
                false,
                true,
                false
        );

        leftBlockN1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                -1,
                EscalatorPortal.class,
                false,
                false,
                true
        );

        leftBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                0,
                EscalatorPortal.class,
                true,
                false,
                false
        );

        leftBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                0,
                -1,
                EscalatorPortal.class,
                false,
                false,
                false
        );

        leftView.getAmenityBlockTemplates().add(leftBlock00);
        leftView.getAmenityBlockTemplates().add(leftBlockN1N1);
        leftView.getAmenityBlockTemplates().add(leftBlockN10);
        leftView.getAmenityBlockTemplates().add(leftBlock0N1);

        escalatorPortalFootprint.addRotation(leftView);
    }

    protected EscalatorPortal(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            Floor floorServed,
            EscalatorShaft escalatorShaft
    ) {
        super(amenityBlocks, enabled, floorServed);

        this.escalatorShaft = escalatorShaft;

        this.escalatorGraphic = new EscalatorGraphic(this);
    }

    public EscalatorShaft getEscalatorShaft() {
        return escalatorShaft;
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.escalatorGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.escalatorGraphic.getGraphicLocation();
    }

    @Override
    public String toString() {
        String string = "Escalator" + ((this.enabled) ? "" : " (disabled)");

        Floor floorServed = this.getPair().getFloorServed();
        int numberFloorServed = Main.simulator.getStation().getFloors().indexOf(floorServed) + 1;

        string += "\n" + "Connects to floor #" + numberFloorServed;
        string += "\n" + this.escalatorShaft.getEscalatorDirection();

        return string;
    }

    @Override
    public Passenger spawnPassenger() {
        return null;
    }

    @Override
    public void absorb(Passenger passenger) {
        PassengerMovement passengerMovement = passenger.getPassengerMovement();

        passengerMovement.enterPortal();

        // Set the appropriate passenger state (ascending or descending)
        passengerMovement.setState(PassengerMovement.State.IN_NONQUEUEABLE);

        if (passengerMovement.getAction() == PassengerMovement.Action.WILL_DESCEND) {
            passengerMovement.setAction(PassengerMovement.Action.DESCENDING);

            this.escalatorShaft.getQueue().get(this.escalatorShaft.getQueue().size() - 1).add(passenger);
        } else {
            passengerMovement.setAction(PassengerMovement.Action.ASCENDING);

            this.escalatorShaft.getQueue().get(0).add(passenger);
        }

        this.escalatorShaft.incrementPassengers();
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

    // Escalator portal block
    public static class EscalatorPortalBlock extends GateBlock {
        public static EscalatorPortalBlockFactory escalatorPortalBlockFactory;

        static {
            escalatorPortalBlockFactory = new EscalatorPortalBlockFactory();
        }

        private EscalatorPortalBlock(Patch patch, boolean attractor, boolean spawner, boolean hasGraphic) {
            super(patch, attractor, spawner, hasGraphic);
        }

        // Escalator portal block factory
        public static class EscalatorPortalBlockFactory extends GateBlockFactory {
            @Override
            public EscalatorPortalBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new EscalatorPortalBlock(
                        patch,
                        attractor,
                        false,
                        hasGraphic
                );
            }

            @Override
            public EscalatorPortalBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean spawner,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new EscalatorPortalBlock(
                        patch,
                        attractor,
                        spawner,
                        hasGraphic
                );
            }
        }
    }

    // Escalator portal factory
    public static class EscalatorPortalFactory extends PortalFactory {
        public EscalatorPortal create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                Floor floorServed,
                EscalatorShaft escalatorShaft
        ) {
            return new EscalatorPortal(
                    amenityBlocks,
                    enabled,
                    floorServed,
                    escalatorShaft
            );
        }
    }
}

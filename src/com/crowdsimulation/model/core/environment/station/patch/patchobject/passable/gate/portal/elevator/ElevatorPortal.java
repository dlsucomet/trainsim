package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.footprint.GateFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.ElevatorGraphic;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ElevatorPortal extends Portal implements Queueable {
    // Denotes the elevator shaft which contains this elevator portal
    private final ElevatorShaft elevatorShaft;

    // Denotes whether the door of this elevator portal is open
    private boolean isOpen;

    // Factory for elevator portal creation
    public static final ElevatorPortalFactory elevatorPortalFactory;

    // Denotes the queueing object associated with all goals like this
    private final QueueObject queueObject;

    // Denotes the floor field state needed to access the floor fields of this security gate
    private final QueueingFloorField.FloorFieldState elevatorPortalFloorFieldState;

    // Handles how this elevator portal is displayed
    private final ElevatorGraphic elevatorPortalGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint elevatorPortalFootprint;

    static {
        elevatorPortalFactory = new ElevatorPortalFactory();

        // Initialize this amenity's footprints
        elevatorPortalFootprint = new AmenityFootprint();

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
                ElevatorPortal.class,
                false,
                true,
                false
        );

        upBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                0,
                ElevatorPortal.class,
                false,
                false,
                true
        );

        upBlockN11 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                -1,
                1,
                ElevatorPortal.class,
                false,
                false,
                false
        );

        upBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                upView.getOrientation(),
                0,
                1,
                ElevatorPortal.class,
                true,
                false,
                false
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlockN10);
        upView.getAmenityBlockTemplates().add(upBlockN11);
        upView.getAmenityBlockTemplates().add(upBlock01);

        elevatorPortalFootprint.addRotation(upView);

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
                ElevatorPortal.class,
                false,
                true,
                true
        );

        rightBlock01 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                0,
                1,
                ElevatorPortal.class,
                false,
                false,
                false
        );

        rightBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                0,
                ElevatorPortal.class,
                true,
                false,
                false
        );

        rightBlock11 = new GateFootprint.GateRotation.GateBlockTemplate(
                rightView.getOrientation(),
                1,
                1,
                ElevatorPortal.class,
                false,
                false,
                false
        );

        rightView.getAmenityBlockTemplates().add(rightBlock00);
        rightView.getAmenityBlockTemplates().add(rightBlock01);
        rightView.getAmenityBlockTemplates().add(rightBlock10);
        rightView.getAmenityBlockTemplates().add(rightBlock11);

        elevatorPortalFootprint.addRotation(rightView);

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
                ElevatorPortal.class,
                false,
                true,
                false
        );

        downBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                0,
                -1,
                ElevatorPortal.class,
                true,
                false,
                true
        );

        downBlock1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                -1,
                ElevatorPortal.class,
                false,
                false,
                false
        );

        downBlock10 = new GateFootprint.GateRotation.GateBlockTemplate(
                downView.getOrientation(),
                1,
                0,
                ElevatorPortal.class,
                false,
                false,
                false
        );

        downView.getAmenityBlockTemplates().add(downBlock00);
        downView.getAmenityBlockTemplates().add(downBlock0N1);
        downView.getAmenityBlockTemplates().add(downBlock1N1);
        downView.getAmenityBlockTemplates().add(downBlock10);

        elevatorPortalFootprint.addRotation(downView);

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
                ElevatorPortal.class,
                false,
                true,
                false
        );

        leftBlockN1N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                -1,
                ElevatorPortal.class,
                false,
                false,
                true
        );

        leftBlockN10 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                -1,
                0,
                ElevatorPortal.class,
                true,
                false,
                false
        );

        leftBlock0N1 = new GateFootprint.GateRotation.GateBlockTemplate(
                leftView.getOrientation(),
                0,
                -1,
                ElevatorPortal.class,
                false,
                false,
                false
        );

        leftView.getAmenityBlockTemplates().add(leftBlock00);
        leftView.getAmenityBlockTemplates().add(leftBlockN1N1);
        leftView.getAmenityBlockTemplates().add(leftBlockN10);
        leftView.getAmenityBlockTemplates().add(leftBlock0N1);

        elevatorPortalFootprint.addRotation(leftView);
    }

    protected ElevatorPortal(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            Floor floorServed,
            ElevatorShaft elevatorShaft
    ) {
        super(amenityBlocks, enabled, floorServed);

        this.elevatorShaft = elevatorShaft;

        this.isOpen = false;

        this.queueObject = new QueueObject(this, this.getAttractors().get(0).getPatch());

        // Initialize this elevator portal's floor field state
        // A null in the floor field state means that it may accept any direction
        this.elevatorPortalFloorFieldState = new QueueingFloorField.FloorFieldState(
                null,
                PassengerMovement.State.IN_QUEUE,
                this
        );

        // Add a blank floor field
        QueueingFloorField queueingFloorField = QueueingFloorField.queueingFloorFieldFactory.create(this);

        // Using the floor field state defined earlier, create the floor field
        this.queueObject.getFloorFields().put(this.elevatorPortalFloorFieldState, queueingFloorField);

        this.elevatorPortalGraphic = new ElevatorGraphic(this);
    }

    public ElevatorShaft getElevatorShaft() {
        return elevatorShaft;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public QueueObject getQueueObject() {
        return queueObject;
    }

    public QueueingFloorField.FloorFieldState getElevatorPortalFloorFieldState() {
        return elevatorPortalFloorFieldState;
    }

    @Override
    public List<QueueingFloorField.FloorFieldState> retrieveFloorFieldStates() {
        List<QueueingFloorField.FloorFieldState> floorFieldStates = new ArrayList<>();

        floorFieldStates.add(this.elevatorPortalFloorFieldState);

        return floorFieldStates;
    }

    @Override
    public QueueingFloorField retrieveFloorField(
            QueueObject queueObject,
            QueueingFloorField.FloorFieldState floorFieldState) {
        return queueObject.getFloorFields().get(
                floorFieldState
        );
    }

    @Override
    // Denotes whether the floor field for this elevator portal is complete
    // Calling this method should work in either portal
    public boolean isFloorFieldsComplete() {
        QueueingFloorField queueingFloorField = retrieveFloorField(
                this.queueObject,
                this.elevatorPortalFloorFieldState
        );

        QueueingFloorField queueingFloorFieldOther;

        ElevatorPortal otherPortal = (ElevatorPortal) this.getPair();

        queueingFloorFieldOther = otherPortal.retrieveFloorField(
                otherPortal.getQueueObject(),
                otherPortal.getElevatorPortalFloorFieldState()
        );

        boolean thisFloorFieldCheck;
        boolean otherFloorFieldCheck;

        thisFloorFieldCheck
                = queueingFloorField.getApices() != null
                && !queueingFloorField.getAssociatedPatches().isEmpty();

        otherFloorFieldCheck
                = queueingFloorFieldOther.getApices() != null
                && !queueingFloorFieldOther.getAssociatedPatches().isEmpty();

        // The floor field of this queueable is complete when, for both portals in this elevator shaft, there are floor\
        // fields present and it has an apex patch
        return thisFloorFieldCheck && otherFloorFieldCheck;
    }

    @Override
    // Clear all floor fields of this given floor field state in this elevator portal
    public void deleteFloorField(QueueingFloorField.FloorFieldState floorFieldState) {
        QueueingFloorField queueingFloorField = retrieveFloorField(
                this.queueObject,
                floorFieldState
        );

        QueueingFloorField.clearFloorField(
                queueingFloorField,
                floorFieldState
        );
    }

    @Override
    public void deleteAllFloorFields() {
        // Sweep through each and every floor field and delete them
        List<QueueingFloorField.FloorFieldState> floorFieldStates = retrieveFloorFieldStates();

        for (QueueingFloorField.FloorFieldState floorFieldState : floorFieldStates) {
            deleteFloorField(floorFieldState);
        }
    }

    @Override
    // Check whether this queueable is free to service a passenger
    public boolean isFree(QueueObject queueObject) {
        return this.queueObject.isFree();
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.elevatorPortalGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.elevatorPortalGraphic.getGraphicLocation();
    }

    @Override
    public String toString() {
        String string = "Elevator" + ((this.enabled) ? "" : " (disabled)");

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
    public void absorb(Passenger passenger) {

    }

    @Override
    public Patch emit() {
        return null;
    }

    // Elevator portal block
    public static class ElevatorPortalBlock extends GateBlock {
        public static ElevatorPortalBlockFactory elevatorPortalBlockFactory;

        static {
            elevatorPortalBlockFactory = new ElevatorPortalBlockFactory();
        }

        private ElevatorPortalBlock(Patch patch, boolean attractor, boolean spawner, boolean hasGraphic) {
            super(patch, attractor, spawner, hasGraphic);
        }

        // Elevator portal block factory
        public static class ElevatorPortalBlockFactory extends GateBlockFactory {
            @Override
            public ElevatorPortalBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new ElevatorPortalBlock(
                        patch,
                        attractor,
                        false,
                        hasGraphic
                );
            }

            @Override
            public ElevatorPortalBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean spawner,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new ElevatorPortalBlock(
                        patch,
                        attractor,
                        spawner,
                        hasGraphic
                );
            }
        }
    }

    // Elevator portal factory
    public static class ElevatorPortalFactory extends PortalFactory {
        public ElevatorPortal create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                Floor floorServed,
                ElevatorShaft elevatorShaft
        ) {
            return new ElevatorPortal(
                    amenityBlocks,
                    enabled,
                    floorServed,
                    elevatorShaft
            );
        }
    }
}

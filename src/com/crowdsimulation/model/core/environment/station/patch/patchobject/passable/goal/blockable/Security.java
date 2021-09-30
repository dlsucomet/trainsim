package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable;

import com.crowdsimulation.controller.graphics.amenity.editor.SecurityEditor;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.SecurityGraphic;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;

import java.util.ArrayList;
import java.util.List;

public class Security extends Blockable {
    public static final long serialVersionUID = -5458621245735102190L;

    public static final double standardDeviation = 3.0;

    // Denotes the queueing object associated with this security gate
    private final QueueObject queueObject;

    // Factory for security gate creation
    public static final SecurityFactory securityFactory;

    // Denotes the floor field state needed to access the floor fields of this security gate
    private final QueueingFloorField.FloorFieldState securityFloorFieldState;

    // Handles how this security is displayed
    private final SecurityGraphic securityGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint securityFootprint;

    // Denotes the editor of this amenity
    public static final SecurityEditor securityEditor;

    static {
        securityFactory = new SecurityFactory();

        // Initialize this amenity's footprints
        securityFootprint = new AmenityFootprint();

        // Up view
        AmenityFootprint.Rotation upView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.UP);

        AmenityFootprint.Rotation.AmenityBlockTemplate block00
                = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                0,
                0,
                Security.class,
                true,
                true
        );

        upView.getAmenityBlockTemplates().add(block00);

        securityFootprint.addRotation(upView);

        // Initialize the editor
        securityEditor = new SecurityEditor();
    }

    protected Security(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            int waitingTime,
            boolean blockPassengers
    ) {
        super(
                amenityBlocks,
                enabled,
                waitingTime,
                blockPassengers
        );

        // Initialize this turnstile's queue objects
        this.queueObject = new QueueObject(
                this,
                this.getAttractors().get(0).getPatch()
        );

        // Initialize this security gate's floor field state
        this.securityFloorFieldState = new QueueingFloorField.FloorFieldState(
                PassengerMovement.Disposition.BOARDING,
                PassengerMovement.State.IN_QUEUE,
                this
        );

        // Add a blank floor field
        QueueingFloorField queueingFloorField = QueueingFloorField.queueingFloorFieldFactory.create(this);

        // Using the floor field state defined earlier, create the floor field
        this.getQueueObject().getFloorFields().put(this.securityFloorFieldState, queueingFloorField);

        // Define the relationships between the queue objects and the attractors
        this.getQueueObjectAmenityBlockMap().put(this.getQueueObject(), this.getAttractors().get(0));

        this.securityGraphic = new SecurityGraphic(this);
    }

    public QueueingFloorField.FloorFieldState getSecurityFloorFieldState() {
        return securityFloorFieldState;
    }

    @Override
    // Check whether this queueable is free to service a passenger
    public boolean isFree(QueueObject queueObject) {
        return this.queueObject.isFree();
    }

    @Override
    public String toString() {
        return "Security" + ((this.enabled) ? "" : " (disabled)");
    }

    @Override
    public List<QueueingFloorField.FloorFieldState> retrieveFloorFieldStates() {
        List<QueueingFloorField.FloorFieldState> floorFieldStates = new ArrayList<>();

        floorFieldStates.add(this.securityFloorFieldState);

        return floorFieldStates;
    }

    @Override
    public QueueingFloorField retrieveFloorField(
            QueueObject queueObject,
            QueueingFloorField.FloorFieldState floorFieldState
    ) {
        return queueObject.getFloorFields().get(
                floorFieldState
        );
    }

    @Override
    // Denotes whether the floor field for this security gate is complete
    public boolean isFloorFieldsComplete() {
        QueueingFloorField queueingFloorField = retrieveFloorField(this.getQueueObject(), this.securityFloorFieldState);

        // The floor field of this queueable is complete when there are floor field values present with an apex patch
        // that is equal to the number of attractors in this queueable target
        return queueingFloorField.getApices().size() == this.getAttractors().size()
                && !queueingFloorField.getAssociatedPatches().isEmpty();
    }

    // Clear all floor fields of the given floor field state in this security gate
    @Override
    public void deleteFloorField(QueueingFloorField.FloorFieldState floorFieldState) {
        QueueingFloorField queueingFloorField = retrieveFloorField(this.getQueueObject(), floorFieldState);

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
    public QueueObject getQueueObject() {
        return this.queueObject;
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.securityGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.securityGraphic.getGraphicLocation();
    }

    // Security block
    public static class SecurityBlock extends AmenityBlock {
        public static SecurityBlockFactory securityBlockFactory;

        static {
            securityBlockFactory = new SecurityBlockFactory();
        }

        private SecurityBlock(Patch patch, boolean attractor, boolean hasGraphic) {
            super(patch, attractor, hasGraphic);
        }

        // Security block factory
        public static class SecurityBlockFactory extends AmenityBlockFactory {
            @Override
            public SecurityBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new SecurityBlock(
                        patch,
                        attractor,
                        hasGraphic
                );
            }
        }
    }

    // Security factory
    public static class SecurityFactory extends GoalFactory {
        public Security create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                int waitingTime,
                boolean blockPassengers
        ) {
            return new Security(
                    amenityBlocks,
                    enabled,
                    waitingTime,
                    blockPassengers
            );
        }
    }
}

package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal;

import com.crowdsimulation.controller.graphics.amenity.editor.TicketBoothEditor;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.TicketBoothGraphic;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;

import java.util.ArrayList;
import java.util.List;

public class TicketBooth extends Goal {
    public static final long serialVersionUID = -4576236425454267953L;

    public static final double standardDeviation = 15.2;

    // Denotes the type of tickets the ticket booth dispenses
    private TicketType ticketType;

    // Takes note of the passenger currently transacting in the ticket booth
    private Passenger passengerTransacting;

    // Denotes the queueing object associated with this ticket booth
    private final QueueObject queueObject;

    // Factory for ticket booth
    public static final TicketBoothFactory ticketBoothFactory;

    // Denotes the floor field state needed to access the floor fields of this ticket booth
    private final QueueingFloorField.FloorFieldState ticketBoothFloorFieldState;

    // Handles how this ticket booth is displayed
    private final TicketBoothGraphic ticketBoothGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint ticketBoothFootprint;

    // Denotes the editor of this amenity
    public static final TicketBoothEditor ticketBoothEditor;

    static {
        ticketBoothFactory = new TicketBoothFactory();

        // Initialize this amenity's footprints
        ticketBoothFootprint = new AmenityFootprint();

        // Up view
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlockN10;

        AmenityFootprint.Rotation upView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.UP);

        upBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                0,
                0,
                TicketBooth.class,
                true,
                false
        );

        upBlockN10 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                -1,
                0,
                TicketBooth.class,
                false,
                true
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlockN10);

        ticketBoothFootprint.addRotation(upView);

        // Right view
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock01;

        AmenityFootprint.Rotation rightView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.RIGHT);

        rightBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                rightView.getOrientation(),
                0,
                0,
                TicketBooth.class,
                true,
                false
        );

        rightBlock01 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                rightView.getOrientation(),
                0,
                1,
                TicketBooth.class,
                false,
                true
        );

        rightView.getAmenityBlockTemplates().add(rightBlock00);
        rightView.getAmenityBlockTemplates().add(rightBlock01);

        ticketBoothFootprint.addRotation(rightView);

        // Down view
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock10;

        AmenityFootprint.Rotation downView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.DOWN);

        downBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                downView.getOrientation(),
                0,
                0,
                TicketBooth.class,
                true,
                true
        );

        downBlock10 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                downView.getOrientation(),
                1,
                0,
                TicketBooth.class,
                false,
                false
        );

        downView.getAmenityBlockTemplates().add(downBlock00);
        downView.getAmenityBlockTemplates().add(downBlock10);

        ticketBoothFootprint.addRotation(downView);

        // Left view
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlock0N1;

        AmenityFootprint.Rotation leftView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.LEFT);

        leftBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                leftView.getOrientation(),
                0,
                0,
                TicketBooth.class,
                true,
                false
        );

        leftBlock0N1 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                leftView.getOrientation(),
                0,
                -1,
                TicketBooth.class,
                false,
                true
        );

        leftView.getAmenityBlockTemplates().add(leftBlock00);
        leftView.getAmenityBlockTemplates().add(leftBlock0N1);

        ticketBoothFootprint.addRotation(leftView);

        // Initialize the editor
        ticketBoothEditor = new TicketBoothEditor();
    }

    protected TicketBooth(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            int waitingTime,
            TicketType ticketType
    ) {
        super(
                amenityBlocks,
                enabled,
                waitingTime
        );

        // Initialize this turnstile's queue objects
        this.queueObject = new QueueObject(this, this.getAttractors().get(0).getPatch());

        this.ticketType = ticketType;
        this.passengerTransacting = null;

        // Initialize this ticket booth's floor field state
        this.ticketBoothFloorFieldState = new QueueingFloorField.FloorFieldState(
                PassengerMovement.Disposition.BOARDING,
                PassengerMovement.State.IN_QUEUE,
                this
        );

        // Add a blank floor field
        QueueingFloorField queueingFloorField = QueueingFloorField.queueingFloorFieldFactory.create(this);

        // Using the floor field state defined earlier, create the floor field
        this.getQueueObject().getFloorFields().put(this.ticketBoothFloorFieldState, queueingFloorField);

        // Define the relationships between the queue objects and the attractors
        this.getQueueObjectAmenityBlockMap().put(this.getQueueObject(), this.getAttractors().get(0));

        this.ticketBoothGraphic = new TicketBoothGraphic(this);
    }

    public TicketType getTicketBoothType() {
        return ticketType;
    }

    public void setTicketBoothType(TicketType ticketType) {
        this.ticketType = ticketType;
    }

    public Passenger getPassengerTransacting() {
        return passengerTransacting;
    }

    public void setPassengerTransacting(Passenger passengerTransacting) {
        this.passengerTransacting = passengerTransacting;
    }

    public QueueingFloorField.FloorFieldState getTicketBoothFloorFieldState() {
        return ticketBoothFloorFieldState;
    }

    @Override
    // Check whether this queueable is free to service a passenger
    public boolean isFree(QueueObject queueObject) {
        return this.queueObject.isFree();
    }

    @Override
    public String toString() {
        return "Ticket booth" + ((this.enabled) ? "" : " (disabled)");
    }

    @Override
    public List<QueueingFloorField.FloorFieldState> retrieveFloorFieldStates() {
        List<QueueingFloorField.FloorFieldState> floorFieldStates = new ArrayList<>();

        floorFieldStates.add(this.ticketBoothFloorFieldState);

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
    // Denotes whether the floor field for this ticket booth is complete
    public boolean isFloorFieldsComplete() {
        QueueingFloorField queueingFloorField = retrieveFloorField(
                this.getQueueObject(),
                this.ticketBoothFloorFieldState
        );

        // The floor field of this queueable is complete when there are floor field values present with an apex patch
        // that is equal to the number of attractors in this queueable target
        return queueingFloorField.getApices().size() == this.getAttractors().size()
                && !queueingFloorField.getAssociatedPatches().isEmpty();
    }

    @Override
    // Clear all floor fields of the given floor field state in this ticket booth
    public void deleteFloorField(QueueingFloorField.FloorFieldState floorFieldState) {
        QueueingFloorField queueingFloorField = retrieveFloorField(
                this.getQueueObject(),
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
    public QueueObject getQueueObject() {
        return this.queueObject;
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.ticketBoothGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.ticketBoothGraphic.getGraphicLocation();
    }

    // Ticket booth block
    public static class TicketBoothBlock extends AmenityBlock {
        public static TicketBoothBlockFactory ticketBoothBlockFactory;

        static {
            ticketBoothBlockFactory = new TicketBoothBlockFactory();
        }

        private TicketBoothBlock(Patch patch, boolean attractor, boolean hasGraphic) {
            super(patch, attractor, hasGraphic);
        }

        // Ticket booth block factory
        public static class TicketBoothBlockFactory extends AmenityBlockFactory {
            @Override
            public TicketBoothBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new TicketBoothBlock(
                        patch,
                        attractor,
                        hasGraphic
                );
            }
        }
    }

    // Ticket booth factory
    public static class TicketBoothFactory extends GoalFactory {
        public TicketBooth create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                int waitingTime,
                TicketType ticketType
        ) {
            return new TicketBooth(
                    amenityBlocks,
                    enabled,
                    waitingTime,
                    ticketType
            );
        }
    }

    // Lists the types of tickets this ticket booth dispenses
    public enum TicketType {
        SINGLE_JOURNEY("Single journey"),
        STORED_VALUE("Stored value"),
        ALL_TICKET_TYPES("All ticket types");

        private final String name;

        TicketType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    // The drawing orientation of this ticket booth
    public enum DrawOrientation {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }
}

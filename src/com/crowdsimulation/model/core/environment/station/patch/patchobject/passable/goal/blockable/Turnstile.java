package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable;

import com.crowdsimulation.controller.graphics.amenity.editor.TurnstileEditor;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.TurnstileGraphic;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.TurnstileFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Turnstile extends Blockable {
    public static final long serialVersionUID = 2924559893737226506L;

    public static final double standardDeviation = 0.8;

    // Denotes the current mode of this turnstile
    private TurnstileMode turnstileMode;

    // Denotes the accepted directions of this turnstile
    private final List<PassengerMovement.TravelDirection> turnstileTravelDirections;

    // Denotes the queueing object associated with this turnstile
    private final HashMap<PassengerMovement.Disposition, QueueObject> queueObjects;

    // Factory for turnstile creation
    public static final TurnstileFactory turnstileFactory;

    // Denotes the floor field states needed to access the floor fields of this turnstile
    private final TurnstileFloorField.FloorFieldState turnstileFloorFieldStateBoarding;
    private final TurnstileFloorField.FloorFieldState turnstileFloorFieldStateAlighting;

    // Handles how this turnstile is displayed
    private final TurnstileGraphic turnstileGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint turnstileFootprint;

    // Denotes the editor of this amenity
    public static final TurnstileEditor turnstileEditor;

    static {
        turnstileFactory = new TurnstileFactory();

        // Initialize this amenity's footprints
        turnstileFootprint = new AmenityFootprint();

        // Up view
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlockN1N1;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlockN10;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlockN11;

        AmenityFootprint.Rotation upView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.UP);

        upBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                0,
                0,
                Turnstile.class,
                true,
                false
        );

        upBlockN1N1 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                -1,
                -1,
                Turnstile.class,
                false,
                true
        );

        upBlockN10 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                -1,
                0,
                Turnstile.class,
                false,
                false
        );

        upBlockN11 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                -1,
                1,
                Turnstile.class,
                false,
                false
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlockN1N1);
        upView.getAmenityBlockTemplates().add(upBlockN10);
        upView.getAmenityBlockTemplates().add(upBlockN11);

        turnstileFootprint.addRotation(upView);

        // Right view
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlockN11;
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock01;
        AmenityFootprint.Rotation.AmenityBlockTemplate rightBlock11;

        AmenityFootprint.Rotation rightView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.RIGHT);

        rightBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                rightView.getOrientation(),
                0,
                0,
                Turnstile.class,
                true,
                false
        );

        rightBlockN11 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                rightView.getOrientation(),
                -1,
                1,
                Turnstile.class,
                false,
                true
        );

        rightBlock01 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                rightView.getOrientation(),
                0,
                1,
                Turnstile.class,
                false,
                false
        );

        rightBlock11 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                rightView.getOrientation(),
                1,
                1,
                Turnstile.class,
                false,
                false
        );

        rightView.getAmenityBlockTemplates().add(rightBlock00);
        rightView.getAmenityBlockTemplates().add(rightBlockN11);
        rightView.getAmenityBlockTemplates().add(rightBlock01);
        rightView.getAmenityBlockTemplates().add(rightBlock11);

        turnstileFootprint.addRotation(rightView);

        // Down view
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock1N1;
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock10;
        AmenityFootprint.Rotation.AmenityBlockTemplate downBlock11;

        AmenityFootprint.Rotation downView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.DOWN);

        downBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                downView.getOrientation(),
                0,
                0,
                Turnstile.class,
                true,
                false
        );

        downBlock1N1 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                downView.getOrientation(),
                1,
                -1,
                Turnstile.class,
                false,
                true
        );

        downBlock10 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                downView.getOrientation(),
                1,
                0,
                Turnstile.class,
                false,
                false
        );

        downBlock11 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                downView.getOrientation(),
                1,
                1,
                Turnstile.class,
                false,
                false
        );

        downView.getAmenityBlockTemplates().add(downBlock00);
        downView.getAmenityBlockTemplates().add(downBlock1N1);
        downView.getAmenityBlockTemplates().add(downBlock10);
        downView.getAmenityBlockTemplates().add(downBlock11);

        turnstileFootprint.addRotation(downView);

        // Left view
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlockN1N1;
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlock0N1;
        AmenityFootprint.Rotation.AmenityBlockTemplate leftBlock1N1;

        AmenityFootprint.Rotation leftView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.LEFT);

        leftBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                leftView.getOrientation(),
                0,
                0,
                Turnstile.class,
                true,
                false
        );

        leftBlockN1N1 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                leftView.getOrientation(),
                -1,
                -1,
                Turnstile.class,
                false,
                true
        );

        leftBlock0N1 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                leftView.getOrientation(),
                0,
                -1,
                Turnstile.class,
                false,
                false
        );

        leftBlock1N1 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                leftView.getOrientation(),
                1,
                -1,
                Turnstile.class,
                false,
                false
        );

        leftView.getAmenityBlockTemplates().add(leftBlock00);
        leftView.getAmenityBlockTemplates().add(leftBlockN1N1);
        leftView.getAmenityBlockTemplates().add(leftBlock0N1);
        leftView.getAmenityBlockTemplates().add(leftBlock1N1);

        turnstileFootprint.addRotation(leftView);

        // Initialize the editor
        turnstileEditor = new TurnstileEditor();
    }

    protected Turnstile(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            int waitingTime,
            boolean blockEntry,
            TurnstileMode turnstileMode,
            List<PassengerMovement.TravelDirection> turnstileTravelDirections
    ) {
        super(
                amenityBlocks,
                enabled,
                waitingTime,
                blockEntry
        );

        this.turnstileMode = turnstileMode;

        this.turnstileTravelDirections = new ArrayList<>();
        setTurnstileTravelDirections(turnstileTravelDirections);

        // Initialize this turnstile's queue objects
        this.queueObjects = new HashMap<>();

        this.queueObjects.put(
                PassengerMovement.Disposition.BOARDING,
                new QueueObject(
                        this,
                        this.getAttractors().get(0).getPatch()
                )
        );

        this.queueObjects.put(
                PassengerMovement.Disposition.ALIGHTING,
                new QueueObject(
                        this,
                        this.getAttractors().get(0).getPatch()
                )
        );

        // Initialize this turnstile's floor field states
        this.turnstileFloorFieldStateBoarding = new TurnstileFloorField.FloorFieldState(
                PassengerMovement.Disposition.BOARDING,
                PassengerMovement.State.IN_QUEUE,
                this
        );

        this.turnstileFloorFieldStateAlighting = new TurnstileFloorField.FloorFieldState(
                PassengerMovement.Disposition.ALIGHTING,
                PassengerMovement.State.IN_QUEUE,
                this
        );

        // Add blank floor fields, one for each direction
        TurnstileFloorField floorFieldBoarding
                = TurnstileFloorField.turnstileFloorFieldFactory.create(this);
        TurnstileFloorField floorFieldAlighting
                = TurnstileFloorField.turnstileFloorFieldFactory.create(this);

        // Using the floor field states defined earlier, create the floor fields
        this.queueObjects.get(PassengerMovement.Disposition.BOARDING).getFloorFields().put(
                this.turnstileFloorFieldStateBoarding,
                floorFieldBoarding
        );

        this.queueObjects.get(PassengerMovement.Disposition.ALIGHTING).getFloorFields().put(
                this.turnstileFloorFieldStateAlighting,
                floorFieldAlighting
        );

        this.turnstileGraphic = new TurnstileGraphic(this);
    }

    public TurnstileMode getTurnstileMode() {
        return turnstileMode;
    }

    public void setTurnstileMode(TurnstileMode turnstileMode) {
        this.turnstileMode = turnstileMode;
    }

    public List<PassengerMovement.TravelDirection> getTurnstileTravelDirections() {
        return turnstileTravelDirections;
    }

    public void setTurnstileTravelDirections(List<PassengerMovement.TravelDirection> travelDirections) {
        this.turnstileTravelDirections.clear();
        this.turnstileTravelDirections.addAll(travelDirections);
    }

    public HashMap<PassengerMovement.Disposition, QueueObject> getQueueObjects() {
        return queueObjects;
    }

    public TurnstileFloorField.FloorFieldState getTurnstileFloorFieldStateBoarding() {
        return turnstileFloorFieldStateBoarding;
    }

    public TurnstileFloorField.FloorFieldState getTurnstileFloorFieldStateAlighting() {
        return turnstileFloorFieldStateAlighting;
    }

    public static boolean isTurnstile(Amenity amenity) {
        return amenity instanceof Turnstile;
    }

    public static Turnstile asTurnstile(Amenity amenity) {
        if (isTurnstile(amenity)) {
            return (Turnstile) amenity;
        } else {
            return null;
        }
    }

    @Override
    // Check whether this queueable is free to service a passenger
    public boolean isFree(QueueObject queueObject) {
        return
                this.queueObjects.get(PassengerMovement.Disposition.BOARDING).isFree()
                        && this.queueObjects.get(PassengerMovement.Disposition.ALIGHTING).isFree();
    }

    @Override
    public String toString() {
        return "Turnstile" + ((this.enabled) ? "" : " (disabled)");
    }

    @Override
    public List<TurnstileFloorField.FloorFieldState> retrieveFloorFieldStates() {
        List<TurnstileFloorField.FloorFieldState> floorFieldStates = new ArrayList<>();

        floorFieldStates.add(this.turnstileFloorFieldStateBoarding);
        floorFieldStates.add(this.turnstileFloorFieldStateAlighting);

        return floorFieldStates;
    }

    @Override
    public TurnstileFloorField retrieveFloorField(
            QueueObject queueObject,
            TurnstileFloorField.FloorFieldState floorFieldState
    ) {
        return (TurnstileFloorField) queueObject.getFloorFields().get(
                floorFieldState
        );
    }

    @Override
    // Denotes whether the floor field for this turnstile is complete
    public boolean isFloorFieldsComplete() {
        TurnstileFloorField boardingFloorField;
        TurnstileFloorField alightingFloorField;

        boolean boardingCheck;
        boolean alightingCheck;

        boardingFloorField = retrieveFloorField(
                this.getQueueObjects().get(PassengerMovement.Disposition.BOARDING),
                turnstileFloorFieldStateBoarding
        );

        alightingFloorField = retrieveFloorField(
                this.getQueueObjects().get(PassengerMovement.Disposition.ALIGHTING),
                turnstileFloorFieldStateAlighting
        );

        // Despite the actual mode of the turnstile, always require that the user fill all (boarding and alighting)
        // floor fields
        // The floor field of this queueable is complete when there are floor field values present with an apex patch
        // that is equal to the number of attractors in this queueable target
        boardingCheck
                = boardingFloorField.getApices().size() == this.getAttractors().size()
                && !boardingFloorField.getAssociatedPatches().isEmpty();

        alightingCheck
                = alightingFloorField.getApices().size() == this.getAttractors().size()
                && !alightingFloorField.getAssociatedPatches().isEmpty();

        // The floor field of this queueable is complete when, for both floor fields, there are floor fields present and
        // apex patches are present
        return boardingCheck && alightingCheck;
    }

    // Clear all floor fields of the given floor field state in this turnstile
    @Override
    public void deleteFloorField(TurnstileFloorField.FloorFieldState floorFieldState) {
        TurnstileFloorField boardingFloorField
                = retrieveFloorField(this.getQueueObjects().get(floorFieldState.getDisposition()), floorFieldState);

        TurnstileFloorField.clearFloorField(
                boardingFloorField,
                floorFieldState
        );
    }

    @Override
    public void deleteAllFloorFields() {
        // Sweep through each and every floor field and delete them
        List<TurnstileFloorField.FloorFieldState> floorFieldStates = retrieveFloorFieldStates();

        for (TurnstileFloorField.FloorFieldState floorFieldState : floorFieldStates) {
            deleteFloorField(floorFieldState);
        }
    }

    @Override
    public QueueObject getQueueObject() {
        return null;
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.turnstileGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.turnstileGraphic.getGraphicLocation();
    }

    // Turnstile block
    public static class TurnstileBlock extends AmenityBlock {
        public static TurnstileBlockFactory turnstileBlockFactory;

        static {
            turnstileBlockFactory = new TurnstileBlockFactory();
        }

        private TurnstileBlock(
                Patch patch,
                boolean attractor,
                boolean hasGraphic
        ) {
            super(patch, attractor, hasGraphic);
        }

        // Turnstile block factory
        public static class TurnstileBlockFactory extends AmenityBlockFactory {
            @Override
            public TurnstileBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                // Make the necessary adjustments
                return new TurnstileBlock(
                        patch,
                        attractor,
                        hasGraphic
                );
            }
        }
    }

    // Turnstile factory
    public static class TurnstileFactory extends GoalFactory {
        public Turnstile create(
                List<AmenityBlock> amenityBlocks,
                boolean enabled,
                int waitingTime,
                boolean blockEntry,
                TurnstileMode turnstileMode,
                List<PassengerMovement.TravelDirection> turnstileTravelDirections

        ) {
            return new Turnstile(
                    amenityBlocks,
                    enabled,
                    waitingTime,
                    blockEntry,
                    turnstileMode,
                    turnstileTravelDirections
            );
        }
    }

    // Lists the possible modes of this turnstile
    public enum TurnstileMode {
        BOARDING("Boarding"),
        ALIGHTING("Alighting"),
        BIDIRECTIONAL("Bidirectional");

        private final String name;

        TurnstileMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

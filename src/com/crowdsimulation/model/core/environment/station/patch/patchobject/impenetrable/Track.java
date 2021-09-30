package com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.amenity.editor.TrackEditor;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.TrackGraphic;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.position.MatrixPosition;

import java.util.ArrayList;
import java.util.List;

public class Track extends Obstacle {
    // Denotes the direction of this track
    private PassengerMovement.TravelDirection trackDirection;

    // Factory for track creation
    public static final TrackFactory trackFactory;

    // Handles how the track is displayed
    private final TrackGraphic trackGraphic;

    // Denotes the footprint of this amenity when being drawn
    public static final AmenityFootprint trackFootprint;

    // Denotes the editor of this amenity
    public static final TrackEditor trackEditor;

    static {
        trackFactory = new TrackFactory();

        // Initialize this amenity's footprints
        trackFootprint = new AmenityFootprint();

        // Up view
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlock00;
        AmenityFootprint.Rotation.AmenityBlockTemplate upBlockN10;

        AmenityFootprint.Rotation upView
                = new AmenityFootprint.Rotation(AmenityFootprint.Rotation.Orientation.UP);

        upBlock00 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                0,
                0,
                Track.class,
                false,
                false
        );

        upBlockN10 = new AmenityFootprint.Rotation.AmenityBlockTemplate(
                upView.getOrientation(),
                -1,
                0,
                Track.class,
                false,
                true
        );

        upView.getAmenityBlockTemplates().add(upBlock00);
        upView.getAmenityBlockTemplates().add(upBlockN10);

        trackFootprint.addRotation(upView);

        // Initialize the editor
        trackEditor = new TrackEditor();
    }

    protected Track(List<AmenityBlock> amenityBlocks, PassengerMovement.TravelDirection trackDirection) {
        super(amenityBlocks);

        this.trackDirection = trackDirection;

        this.trackGraphic = new TrackGraphic(this);
    }

    public PassengerMovement.TravelDirection getTrackDirection() {
        return trackDirection;
    }

    public void setTrackDirection(PassengerMovement.TravelDirection trackDirection) {
        this.trackDirection = trackDirection;
    }

    @Override
    public String toString() {
        return "Train track (" + this.trackDirection.toString() + ")";
    }

    @Override
    public AmenityGraphic getGraphicObject() {
        return this.trackGraphic;
    }

    @Override
    public AmenityGraphicLocation getGraphicLocation() {
        return this.trackGraphic.getGraphicLocation();
    }

    // Track block
    public static class TrackBlock extends AmenityBlock {
        public static TrackBlockFactory trackBlockFactory;

        static {
            trackBlockFactory = new TrackBlockFactory();
        }

        private TrackBlock(Patch patch, boolean attractor, boolean hasGraphic) {
            super(patch, attractor, hasGraphic);
        }

        public static List<AmenityBlock> convertToAmenityBlocks(
                int currentRow,
                int columnsSpanned,
                AmenityFootprint.Rotation.AmenityBlockTemplate templateAbove,
                AmenityFootprint.Rotation.AmenityBlockTemplate templateBelow
        ) {
            List<AmenityBlock> trackBlocks = new ArrayList<>();

            // Create a track block for all of the specified columns
            for (int currentColumn = 0; currentColumn < columnsSpanned; currentColumn++) {
                MatrixPosition patchPositionAbove = new MatrixPosition(
                        currentRow - 1,
                        currentColumn
                );

                MatrixPosition patchPositionBelow = new MatrixPosition(
                        currentRow,
                        currentColumn
                );

                // If either of the positions are out of bounds, return null
                if (!MatrixPosition.inBounds(
                        patchPositionAbove,
                        Main.simulator.getStation())
                        || !MatrixPosition.inBounds(
                        patchPositionBelow,
                        Main.simulator.getStation()
                )) {
                    return null;
                }

                // Create the amenity block, then add it to the list
                Patch patchAbove = Main.simulator.getCurrentFloor().getPatch(currentRow - 1, currentColumn);
                Patch patchBelow = Main.simulator.getCurrentFloor().getPatch(currentRow, currentColumn);

                TrackBlock trackBlockAbove = TrackBlock.trackBlockFactory.create(
                        patchAbove,
                        templateAbove.isAttractor(),
                        templateAbove.hasGraphic(),
                        templateAbove.getOrientation()
                );

                TrackBlock trackBlockBelow = TrackBlock.trackBlockFactory.create(
                        patchBelow,
                        templateBelow.isAttractor(),
                        templateBelow.hasGraphic(),
                        templateBelow.getOrientation()
                );

                trackBlocks.add(trackBlockAbove);
                trackBlocks.add(trackBlockBelow);
            }

            return trackBlocks;
        }

        // Track block factory
        public static class TrackBlockFactory extends AmenityBlockFactory {
            @Override
            public TrackBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            ) {
                return new TrackBlock(
                        patch,
                        attractor,
                        hasGraphic
                );
            }
        }
    }

    // Track factory
    public static class TrackFactory extends ObstacleFactory {
        public Track create(List<AmenityBlock> amenityBlocks, PassengerMovement.TravelDirection trackDirection) {
            return new Track(
                    amenityBlocks,
                    trackDirection
            );
        }
    }
}

package com.crowdsimulation.model.core.environment.station.patch.patchobject;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.BaseStationObject;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Track;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Wall;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;
import com.crowdsimulation.model.core.environment.station.patch.position.MatrixPosition;

import java.util.ArrayList;
import java.util.List;

public abstract class Amenity extends PatchObject implements Environment {
    // Denotes the amenity blocks which compose this amenity
    private final List<AmenityBlock> amenityBlocks;

    // Denotes the attractors of this amenity
    private final List<AmenityBlock> attractors;

    protected Amenity(List<AmenityBlock> amenityBlocks) {
        this.amenityBlocks = amenityBlocks;

        // Only proceed when this amenity has blocks
        if (this.amenityBlocks != null) {
            // Set the parent of each amenity block to this amenity
            // In turn, set the contents of the patch in each amenity block to the amenity block
            // Signal to this patch's neighbors that an amenity was placed in here
            // Also, set all this amenity's attractors to the pertinent list
            this.attractors = new ArrayList<>();

            for (AmenityBlock amenityBlock : this.amenityBlocks) {
                amenityBlock.setParent(this);

                amenityBlock.getPatch().setAmenityBlock(amenityBlock);
                amenityBlock.getPatch().signalAddAmenityBlock();

                if (amenityBlock.isAttractor()) {
                    this.attractors.add(amenityBlock);
                }
            }
        } else {
            this.attractors = null;
        }
    }

    public List<AmenityBlock> getAmenityBlocks() {
        return amenityBlocks;
    }

    public List<AmenityBlock> getAttractors() {
        return attractors;
    }

    // Denotes a single component of an amenity that occupies one patch
    public abstract static class AmenityBlock implements Environment {
        private Amenity parent;
        private final Patch patch;
        private final boolean attractor;
        private final boolean hasGraphic;

        protected AmenityBlock(Patch patch, boolean attractor, boolean hasGraphic) {
            this.patch = patch;
            this.attractor = attractor;
            this.hasGraphic = hasGraphic;
        }

        public Amenity getParent() {
            return parent;
        }

        public void setParent(Amenity parent) {
            this.parent = parent;
        }

        public Patch getPatch() {
            return patch;
        }

        public boolean isAttractor() {
            return attractor;
        }

        public boolean hasGraphic() {
            return hasGraphic;
        }

        // Convert the list of amenity block templates to a list of amenity blocks
        public static List<AmenityBlock> convertToAmenityBlocks(
                Patch referencePatch,
                List<AmenityFootprint.Rotation.AmenityBlockTemplate> amenityBlockTemplates
        ) {
            List<AmenityBlock> amenityBlocks = new ArrayList<>();

            // Convert each template into an actual amenity block
            for (AmenityFootprint.Rotation.AmenityBlockTemplate amenityBlockTemplate : amenityBlockTemplates) {
                // Compute for the position of the patch using the offset data
                int row
                        = referencePatch.getMatrixPosition().getRow()
                        + amenityBlockTemplate.getOffset().getRowOffset();

                int column
                        = referencePatch.getMatrixPosition().getColumn()
                        + amenityBlockTemplate.getOffset().getColumnOffset();

                MatrixPosition patchPosition = new MatrixPosition(
                        row,
                        column
                );

                // If the position is out of bounds, return null
                if (!MatrixPosition.inBounds(
                        patchPosition,
                        Main.simulator.getStation()
                )) {
                    return null;
                }

                // Create the amenity block, then add it to the list
                Patch patch = Main.simulator.getCurrentFloor().getPatch(row, column);

                assert getAmenityBlockFactory(amenityBlockTemplate.getAmenityClass()) != null;

                AmenityBlockFactory amenityBlockFactory
                        = getAmenityBlockFactory(amenityBlockTemplate.getAmenityClass());

                AmenityBlock amenityBlock = amenityBlockFactory.create(
                        patch,
                        amenityBlockTemplate.isAttractor(),
                        amenityBlockTemplate.hasGraphic(),
                        amenityBlockTemplate.getOrientation()
                );

                amenityBlocks.add(amenityBlock);
            }

            return amenityBlocks;
        }

        private static AmenityBlockFactory getAmenityBlockFactory(Class<? extends Amenity> amenityClass) {
            if (amenityClass == StationGate.class) {
                return StationGate.StationGateBlock.stationGateBlockFactory;
            } else if (amenityClass == Security.class) {
                return Security.SecurityBlock.securityBlockFactory;
            } else if (amenityClass == Turnstile.class) {
                return Turnstile.TurnstileBlock.turnstileBlockFactory;
            } else if (amenityClass == Track.class) {
                return Track.TrackBlock.trackBlockFactory;
            } else if (amenityClass == TicketBooth.class) {
                return TicketBooth.TicketBoothBlock.ticketBoothBlockFactory;
            } else if (amenityClass == Wall.class) {
                return Wall.WallBlock.wallBlockFactory;
            } else {
                return null;
            }
        }

        // Template class for amenity block factories
        public abstract static class AmenityBlockFactory extends StationObjectFactory {
            public abstract AmenityBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            );
        }
    }

    // Template class for amenity factories
    public abstract static class AmenityFactory extends StationObjectFactory {
    }
}

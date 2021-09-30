package com.crowdsimulation.controller.graphics.amenity.footprint;

import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;

import java.util.ArrayList;
import java.util.List;

public class AmenityFootprint {
    // Lists all available rotations of this amenity when being drawn
    private final List<Rotation> rotations;

    // The index of the current rotation
    private int rotationIndex;

    public AmenityFootprint() {
        this.rotations = new ArrayList<>();
        this.rotationIndex = 0;
    }

    public void addRotation(Rotation rotation) {
        this.rotations.add(rotation);
    }

    public void rotateClockwise() {
        if (this.rotationIndex == rotations.size() - 1) {
            this.rotationIndex = 0;
        } else {
            this.rotationIndex++;
        }
    }

    public void rotateCounterclockwise() {
        if (this.rotationIndex == 0) {
            this.rotationIndex = rotations.size() - 1;
        } else {
            this.rotationIndex--;
        }
    }

    public Rotation getCurrentRotation() {
        return this.rotations.get(this.rotationIndex);
    }

    // Denotes a rotation of an amenity when being drawn, along with its necessary properties
    public static class Rotation {
        // Denotes the orientation of this rotation
        private final Orientation orientation;

        // Lists all the amenity block templates in this rotation view
        private final List<AmenityBlockTemplate> amenityBlockTemplates;

        public Rotation(Orientation orientation) {
            this.orientation = orientation;
            this.amenityBlockTemplates = new ArrayList<>();
        }

        public List<AmenityBlockTemplate> getAmenityBlockTemplates() {
            return amenityBlockTemplates;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public boolean isVertical() {
            return this.orientation == Orientation.UP || this.orientation == Orientation.DOWN;
        }

        public boolean isHorizontal() {
            return this.orientation == Orientation.RIGHT || this.orientation == Orientation.LEFT;
        }

        public static class AmenityBlockTemplate {
            private final Orientation orientation;
            private final Patch.Offset offset;
            private final Class<? extends Amenity> amenityClass;
            private final boolean attractor;
            private final boolean hasGraphic;

            public AmenityBlockTemplate(
                    Orientation orientation,
                    int rowOffset,
                    int columnOffset,
                    Class<? extends Amenity> amenityClass,
                    boolean attractor,
                    boolean hasGraphic
            ) {
                this.orientation = orientation;
                this.offset = new Patch.Offset(rowOffset, columnOffset);
                this.amenityClass = amenityClass;
                this.attractor = attractor;
                this.hasGraphic = hasGraphic;
            }

            public Orientation getOrientation() {
                return orientation;
            }

            public Patch.Offset getOffset() {
                return offset;
            }

            public Class<? extends Amenity> getAmenityClass() {
                return amenityClass;
            }

            public boolean isAttractor() {
                return attractor;
            }

            public boolean hasGraphic() {
                return hasGraphic;
            }

            // Convert the list of amenity block templates to a list of amenity blocks
            public static List<Amenity.AmenityBlock> realizeAmenityBlockTemplates(
                    Patch cursorPatch,
                    List<AmenityBlockTemplate> amenityBlockTemplates
            ) {
                return Amenity.AmenityBlock.convertToAmenityBlocks(
                        cursorPatch,
                        amenityBlockTemplates
                );
            }
        }

        // Denotes the possible orientations of an amenity being drawn
        public enum Orientation {
            UP,
            RIGHT,
            DOWN,
            LEFT
        }
    }
}

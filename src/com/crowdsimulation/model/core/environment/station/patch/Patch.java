package com.crowdsimulation.model.core.environment.station.patch;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.BaseStationObject;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.position.Coordinates;
import com.crowdsimulation.model.core.environment.station.patch.position.MatrixPosition;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Patch extends BaseStationObject implements Environment, Comparable<Patch> {
    // Denotes the size of the path in square meters
    public static final double PATCH_SIZE_IN_SQUARE_METERS = 0.6;

    // Denotes the position of this patch based on a discrete row x column matrix
    private final MatrixPosition matrixPosition;

    // Denotes the center of this patch in a Cartesian pixel coordinate system
    private final Coordinates patchCenterCoordinates;

    // Denotes the list of passengers that are currently on this patch
    private final CopyOnWriteArrayList<Passenger> passengers;

    // Denotes the amenity block present on this patch
    private Amenity.AmenityBlock amenityBlock;

    // Denotes the floor which contains this patch
    private final Floor floor;

    // Denotes the positions of the Moore neighbors of this patch (because the serializer can't handle references to the
    // patch itself)
    private final List<MatrixPosition> neighborIndices;

    // Denotes the positions of the neighbors of this patch within a 7x7 range
    private final List<MatrixPosition> neighbor7x7Indices;

    // Denotes the number of amenity blocks around this patch
    private int amenityBlocksAround;

    // Denotes the individual floor field value of this patch, given the queueable goal patch and the desired state
    private final Map<Queueable, Map<QueueingFloorField.FloorFieldState, Double>> floorFieldValues;

    public Patch(Floor floor, MatrixPosition matrixPosition) {
        super();

        this.matrixPosition = matrixPosition;
        this.patchCenterCoordinates = Coordinates.getPatchCenterCoordinates(this);

        this.passengers = new CopyOnWriteArrayList<>();
        this.amenityBlock = null;
        this.floor = floor;

        this.neighborIndices = this.computeNeighboringPatches();
        this.neighbor7x7Indices = this.compute7x7Neighbors();

        this.amenityBlocksAround = 0;

        this.floorFieldValues = new HashMap<>();
    }

    public MatrixPosition getMatrixPosition() {
        return matrixPosition;
    }

    public Coordinates getPatchCenterCoordinates() {
        return patchCenterCoordinates;
    }

    public CopyOnWriteArrayList<Passenger> getPassengers() {
        return passengers;
    }

    public Map<Queueable, Map<QueueingFloorField.FloorFieldState, Double>> getFloorFieldValues() {
        return floorFieldValues;
    }

    public Amenity.AmenityBlock getAmenityBlock() {
        return amenityBlock;
    }

    public void setAmenityBlock(Amenity.AmenityBlock amenityBlock) {
        this.amenityBlock = amenityBlock;
    }

    public int getAmenityBlocksAround() {
        return amenityBlocksAround;
    }

    public Floor getFloor() {
        return floor;
    }

    private List<MatrixPosition> computeNeighboringPatches() {
        int patchRow = this.matrixPosition.getRow();
        int patchColumn = this.matrixPosition.getColumn();

        List<MatrixPosition> neighboringPatchIndices = new ArrayList<>();

        if (patchRow - 1 >= 0 && patchColumn - 1 >= 0) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow - 1, patchColumn - 1));
        }

        if (patchRow - 1 >= 0) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow - 1, patchColumn));
        }

        if (patchRow - 1 >= 0 && patchColumn + 1 < this.getFloor().getColumns()) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow - 1, patchColumn + 1));
        }

        if (patchColumn - 1 >= 0) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow, patchColumn - 1));
        }

        if (patchColumn + 1 < this.getFloor().getColumns()) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow, patchColumn + 1));
        }

        if (patchRow + 1 < this.getFloor().getRows() && patchColumn - 1 >= 0) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow + 1, patchColumn - 1));
        }

        if (patchRow + 1 < this.getFloor().getRows()) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow + 1, patchColumn));
        }

        if (patchRow + 1 < this.getFloor().getRows() && patchColumn + 1 < this.getFloor().getColumns()) {
            neighboringPatchIndices.add(new MatrixPosition(patchRow + 1, patchColumn + 1));
        }

        return neighboringPatchIndices;
    }

    private List<MatrixPosition> compute7x7Neighbors() {
        int patchRow = this.matrixPosition.getRow();
        int patchColumn = this.matrixPosition.getColumn();

        int truncatedX = (int) (this.getPatchCenterCoordinates().getX() / Patch.PATCH_SIZE_IN_SQUARE_METERS);
        int truncatedY = (int) (this.getPatchCenterCoordinates().getY() / Patch.PATCH_SIZE_IN_SQUARE_METERS);

        List<MatrixPosition> patchIndicesToExplore = new ArrayList<>();

        for (int rowOffset = -3; rowOffset <= 3; rowOffset++) {
            for (int columnOffset = -3; columnOffset <= 3; columnOffset++) {
                boolean xCondition;
                boolean yCondition;

                // Separate upper and lower rows
                if (rowOffset < 0) {
                    yCondition = truncatedY + rowOffset > 0;
                } else if (rowOffset > 0) {
                    yCondition = truncatedY + rowOffset < floor.getRows();
                } else {
                    yCondition = true;
                }

                // Separate left and right columns
                if (columnOffset < 0) {
                    xCondition = truncatedX + columnOffset > 0;
                } else if (columnOffset > 0) {
                    xCondition = truncatedX + columnOffset < floor.getColumns();
                } else {
                    xCondition = true;
                }

                // Insert the patch to the list of patches to be explored if the patches are within the bounds of the
                // floor
                if (xCondition && yCondition) {
                    patchIndicesToExplore.add(
                            new MatrixPosition(
                                    patchRow + rowOffset,
                                    patchColumn + columnOffset
                            )
                    );
                }
            }
        }

        return patchIndicesToExplore;
    }

    public List<Patch> getNeighbors() {
        List<Patch> neighboringPatches = new ArrayList<>();

        for (MatrixPosition neighboringPatchIndex : this.neighborIndices) {
            Patch patch = this.getFloor().getPatch(neighboringPatchIndex.getRow(), neighboringPatchIndex.getColumn());

            if (patch != null) {
                neighboringPatches.add(patch);
            }
        }

        return neighboringPatches;
    }

    public List<Patch> get7x7Neighbors(boolean includeCenterPatch) {
        List<Patch> neighboringPatches = new ArrayList<>();

        for (MatrixPosition neighboringPatchIndex : this.neighbor7x7Indices) {
            Patch patch = this.getFloor().getPatch(neighboringPatchIndex.getRow(), neighboringPatchIndex.getColumn());

            if (patch != null) {
                if (!includeCenterPatch || !patch.equals(this)) {
                    neighboringPatches.add(patch);
                }
            }
        }

        return neighboringPatches;
    }

    // Signal to this patch and to its neighbors that an amenity block was added here
    public void signalAddAmenityBlock() {
        // Increment the amenity blocks around counter on this patch
        this.incrementAmenityBlocksAround();

        // Then increment the amenity blocks around counters of its neighbors
        for (Patch neighbor : this.getNeighbors()) {
            neighbor.incrementAmenityBlocksAround();
        }
    }

    // Signal to this patch and to its neighbors that an amenity block was removed from here
    public void signalRemoveAmenityBlock() {
        // Decrement the amenity blocks around counter on this patch
        this.decrementAmenityBlocksAround();

        // Then decrement the amenity blocks around counters of its neighbors
        for (Patch neighbor : this.getNeighbors()) {
            neighbor.decrementAmenityBlocksAround();
        }
    }

    public boolean isNextToAmenityBlock() {
        return this.amenityBlocksAround > 0;
    }

    private void incrementAmenityBlocksAround() {
        this.amenityBlocksAround++;
    }

    private void decrementAmenityBlocksAround() {
        this.amenityBlocksAround--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patch patch = (Patch) o;
        return matrixPosition.equals(patch.matrixPosition) && floor.equals(patch.floor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrixPosition);
    }

    @Override
    public int compareTo(Patch patch) {
        int thisRow = this.getMatrixPosition().getRow();
        int patchRow = patch.getMatrixPosition().getRow();

        int thisColumn = this.getMatrixPosition().getColumn();
        int patchColumn = patch.getMatrixPosition().getColumn();

        if (thisRow > patchRow) {
            return 1;
        } else if (thisRow == patchRow) {
            return Integer.compare(thisColumn, patchColumn);
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "[" + this.getMatrixPosition().getRow() + ", " + this.getMatrixPosition().getColumn() + "]";
    }

    public static class PatchPair implements Environment {
        private final Patch patch1;
        private final Patch patch2;

        public PatchPair(Patch patch1, Patch patch2) {
            this.patch1 = patch1;
            this.patch2 = patch2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatchPair patchPair = (PatchPair) o;
            return patch1.equals(patchPair.patch1) && patch2.equals(patchPair.patch2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(patch1, patch2);
        }

        @Override
        public String toString() {
            return "(" + patch1 + ", " + patch2 + ")";
        }
    }

    // Denotes the offset of a specific offset of an object in terms of its matrix position
    public static class Offset {
        private final MatrixPosition offset;

        public Offset(int rowOffset, int columnOffset) {
            this.offset = new MatrixPosition(rowOffset, columnOffset);
        }

        public int getRowOffset() {
            return this.offset.getRow();
        }

        public int getColumnOffset() {
            return this.offset.getColumn();
        }

        public static Offset getOffsetFromPatch(Patch patch, Patch reference) {
            int rowOffset = patch.getMatrixPosition().getRow() - reference.getMatrixPosition().getRow();
            int columnOffset = patch.getMatrixPosition().getColumn() - reference.getMatrixPosition().getColumn();

            return new Offset(rowOffset, columnOffset);
        }

        public static Patch getPatchFromOffset(Floor floor, Patch reference, Offset offset) {
            int newRow = reference.getMatrixPosition().getRow() + offset.getRowOffset();
            int newColumn = reference.getMatrixPosition().getColumn() + offset.getColumnOffset();

            // Get the new patch from the offset, if that patch is within the bounds of the given floor
            if (newRow >= 0 && newRow < floor.getRows() && newColumn >= 0 && newColumn < floor.getColumns()) {
                Patch patch = floor.getPatch(newRow, newColumn);

                if (patch.getAmenityBlock() == null) {
                    return patch;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}

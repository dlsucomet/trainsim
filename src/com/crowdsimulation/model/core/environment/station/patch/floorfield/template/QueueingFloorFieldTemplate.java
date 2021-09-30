package com.crowdsimulation.model.core.environment.station.patch.floorfield.template;

import com.crowdsimulation.model.core.environment.station.patch.Patch;

import java.util.List;
import java.util.Objects;

public class QueueingFloorFieldTemplate {
    private final List<Patch.Offset> apicesOffsets;
    private final List<PatchOffsetFloorFieldValuePair> associatedPatches;

    public QueueingFloorFieldTemplate(
            List<Patch.Offset> apicesOffsets, List<PatchOffsetFloorFieldValuePair> associatedPatches
    ) {
        this.apicesOffsets = apicesOffsets;
        this.associatedPatches = associatedPatches;
    }

    public List<Patch.Offset> getApicesOffsets() {
        return apicesOffsets;
    }

    public List<PatchOffsetFloorFieldValuePair> getAssociatedPatches() {
        return associatedPatches;
    }

    public static class PatchOffsetFloorFieldValuePair {
        public final Patch.Offset offset;
        public final Double floorFieldValue;

        public PatchOffsetFloorFieldValuePair(Patch.Offset offset, Double floorFieldValue) {
            this.offset = offset;
            this.floorFieldValue = floorFieldValue;
        }

        public Patch.Offset getOffset() {
            return offset;
        }

        public Double getFloorFieldValue() {
            return floorFieldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatchOffsetFloorFieldValuePair that = (PatchOffsetFloorFieldValuePair) o;
            return offset.equals(that.offset) && floorFieldValue.equals(that.floorFieldValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(offset, floorFieldValue);
        }
    }
}

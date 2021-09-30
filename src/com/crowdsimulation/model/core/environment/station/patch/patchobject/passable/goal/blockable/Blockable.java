package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable;

import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.Goal;

import java.util.List;

public abstract class Blockable extends Goal {
    // Denotes whether passengers are able to pass through this amenity
    private boolean blockEntry;

    public Blockable(
            List<AmenityBlock> amenityBlocks,
            boolean enabled,
            int waitingTime,
            boolean blockEntry) {
        super(amenityBlocks, enabled, waitingTime);

        this.blockEntry = blockEntry;
    }

    public boolean blockEntry() {
        return blockEntry;
    }

    public void setBlockEntry(boolean blockEntry) {
        this.blockEntry = blockEntry;
    }

    public static boolean isBlockable(Amenity amenity) {
        return amenity instanceof Blockable;
    }

    public static Blockable asBlockable(Amenity amenity) {
        if (isBlockable(amenity)) {
            return (Blockable) amenity;
        } else {
            return null;
        }
    }
}

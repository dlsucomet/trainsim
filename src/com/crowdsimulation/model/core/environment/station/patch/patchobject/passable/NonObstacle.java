package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable;

import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;

import java.util.List;

public abstract class NonObstacle extends Amenity {
    // Denotes whether this passable patch object is enabled or not (passengers cannot pass through it)
    protected boolean enabled;

    public NonObstacle(List<AmenityBlock> amenityBlocks, boolean enabled) {
        super(amenityBlocks);

        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // Non-obstacle factory
    public static abstract class NonObstacleFactory extends AmenityFactory {
    }
}

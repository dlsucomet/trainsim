package com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable;

import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Drawable;

import java.util.List;

public abstract class Obstacle extends Amenity implements Drawable {
    protected Obstacle(List<AmenityBlock> amenityBlocks) {
        super(amenityBlocks);
    }

    // Obstacle factory template
    public static abstract class ObstacleFactory extends AmenityFactory {
    }
}

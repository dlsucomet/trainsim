package com.crowdsimulation.model.core.environment.station.patch.patchobject;

import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.AmenityGraphicLocation;

public interface Drawable {
    AmenityGraphic getGraphicObject();

    AmenityGraphicLocation getGraphicLocation();
}

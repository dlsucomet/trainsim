package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.amenity.graphic.GraphicLocation;

// Contains the location and other relevant information of a single sprite in a sprite sheet
public class AmenityGraphicLocation extends GraphicLocation {
    // Denotes the base image unit of the sprite sheet in pixels
    // i.e.: a 1x1 ratio image will be a 200x200 pixel one
    public static final int BASE_IMAGE_UNIT = 200;

    public AmenityGraphicLocation(int graphicRow, int graphicColumn) {
        super(graphicRow, graphicColumn);
    }

    public int getSourceY() {
        return this.graphicRow * BASE_IMAGE_UNIT;
    }

    public int getSourceX() {
        return  this.graphicColumn * BASE_IMAGE_UNIT;
    }

    public int getSourceWidth() {
        return  this.graphicWidth * BASE_IMAGE_UNIT;
    }

    public int getSourceHeight() {
        return  this.graphicHeight * BASE_IMAGE_UNIT;
    }
}

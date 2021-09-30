package com.crowdsimulation.controller.graphics.amenity.graphic.passenger;

import com.crowdsimulation.controller.graphics.amenity.graphic.GraphicLocation;

// Contains the location and other relevant information of a single sprite in a sprite sheet
public class PassengerGraphicLocation extends GraphicLocation {
    // Denotes the base image unit of the sprite sheet in pixels
    // i.e.: a 1x1 ratio image will be a 64x64 pixel one
    public static final int BASE_IMAGE_UNIT = 64;

    public PassengerGraphicLocation(int graphicRow, int graphicColumn) {
        super(graphicRow, graphicColumn);
    }

    public int getSourceY() {
        return  this.graphicRow * BASE_IMAGE_UNIT;
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

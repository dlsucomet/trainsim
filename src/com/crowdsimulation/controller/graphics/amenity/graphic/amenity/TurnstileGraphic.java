package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;

public class TurnstileGraphic extends AmenityGraphic {
    private static final int ROW_SPAN_VERTICAL = 1;
    private static final int COLUMN_SPAN_VERTICAL = 3;

    private static final int ROW_SPAN_HORIZONTAL = 3;
    private static final int COLUMN_SPAN_HORIZONTAL = 1;

    private static final int NORMAL_ROW_OFFSET = 0;
    private static final int NORMAL_COLUMN_OFFSET = 0;

    public TurnstileGraphic(Turnstile turnstile) {
        super(
                turnstile,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL,
                NORMAL_ROW_OFFSET,
                NORMAL_COLUMN_OFFSET
        );

        AmenityFootprint.Rotation.Orientation orientation
                = GraphicsController.currentAmenityFootprint.getCurrentRotation().getOrientation();

        switch (orientation) {
            case UP:
                this.graphicIndex = 0;

                break;
            case RIGHT:
                this.graphicIndex = 1;

                break;
            case DOWN:
                this.graphicIndex = 2;

                break;
            case LEFT:
                this.graphicIndex = 3;

                break;
        }
    }
}

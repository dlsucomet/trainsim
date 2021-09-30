package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;

public class TicketBoothGraphic extends AmenityGraphic implements Cyclable {
    private static final int ROW_SPAN_VERTICAL = 2;
    private static final int COLUMN_SPAN_VERTICAL = 1;

    private static final int ROW_SPAN_HORIZONTAL = 2;
    private static final int COLUMN_SPAN_HORIZONTAL = 1;

    private static final int NORMAL_ROW_OFFSET = 0;
    private static final int NORMAL_COLUMN_OFFSET = 0;

    private static final int FRONT_ROW_OFFSET = -1;
    private static final int FRONT_COLUMN_OFFSET = 0;

    private static final int SIDEWAYS_ROW_OFFSET = -1;
    private static final int SIDEWAYS_COLUMN_OFFSET = 0;

    public TicketBoothGraphic(TicketBooth ticketBooth) {
        super(
                ticketBooth,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? (
                        (GraphicsController.currentAmenityFootprint.getCurrentRotation().getOrientation()
                                == AmenityFootprint.Rotation.Orientation.UP)
                                ? FRONT_ROW_OFFSET : NORMAL_ROW_OFFSET
                ) : SIDEWAYS_ROW_OFFSET,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? NORMAL_COLUMN_OFFSET : SIDEWAYS_COLUMN_OFFSET
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

    @Override
    public void cycle() {
        // Cycle through the graphics list in steps of four
        this.graphicIndex = (this.graphicIndex + 4) % this.graphics.size();
    }
}

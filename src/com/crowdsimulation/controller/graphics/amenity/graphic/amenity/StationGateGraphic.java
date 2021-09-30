package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;

public class StationGateGraphic extends AmenityGraphic implements Changeable {
    public static final int ROW_SPAN_VERTICAL = 2;
    public static final int COLUMN_SPAN_VERTICAL = 2;

    public static final int ROW_SPAN_HORIZONTAL = 2;
    public static final int COLUMN_SPAN_HORIZONTAL = 2;

    private static final int ROW_OFFSET = 0;
    private static final int COLUMN_OFFSET = 0;

    public StationGateGraphic(StationGate stationGate) {
        super(
                stationGate,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL,
                ROW_OFFSET,
                COLUMN_OFFSET
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
    public void change() {
        StationGate stationGate = (StationGate) this.amenity;

        if (stationGate.isEnabled()) {
            if (this.graphicIndex >= 4 && this.graphicIndex <= 7) {
                this.graphicIndex -= 4;
            }
        } else {
            if (this.graphicIndex >= 0 && this.graphicIndex <= 3) {
                this.graphicIndex += 4;
            }
        }
    }
}

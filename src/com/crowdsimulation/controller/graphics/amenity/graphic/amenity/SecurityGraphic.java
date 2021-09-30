package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;

public class SecurityGraphic extends AmenityGraphic implements Cyclable {
    private static final int ROW_SPAN_VERTICAL = 2;
    private static final int COLUMN_SPAN_VERTICAL = 1;

    private static final int ROW_SPAN_HORIZONTAL = 2;
    private static final int COLUMN_SPAN_HORIZONTAL = 1;

    private static final int NORMAL_ROW_OFFSET = -1;
    private static final int NORMAL_COLUMN_OFFSET = 0;

    public SecurityGraphic(Security security) {
        super(
                security,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL,
                NORMAL_ROW_OFFSET,
                NORMAL_COLUMN_OFFSET
        );
    }

    @Override
    public void cycle() {
        if (this.graphicIndex + 1 == this.graphics.size()) {
            this.graphicIndex = 0;
        } else {
            this.graphicIndex++;
        }
    }
}

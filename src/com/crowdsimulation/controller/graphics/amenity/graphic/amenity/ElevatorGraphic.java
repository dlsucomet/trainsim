package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Drawable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;

public class ElevatorGraphic extends AmenityGraphic implements Changeable {
    private static final int ROW_SPAN_HORIZONTAL = 2;
    private static final int COLUMN_SPAN_HORIZONTAL = 2;

    private static final int ROW_SPAN_VERTICAL = 2;
    private static final int COLUMN_SPAN_VERTICAL = 2;

    private static final int ROW_OFFSET = 0;
    private static final int COLUMN_OFFSET = 0;

    public ElevatorGraphic(ElevatorPortal elevatorPortal) {
        super(
                elevatorPortal,
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
                this.graphicIndex = 4;

                break;
            case RIGHT:
                this.graphicIndex = 5;

                break;
            case DOWN:
                this.graphicIndex = 6;

                break;
            case LEFT:
                this.graphicIndex = 7;

                break;
        }
    }

    @Override
    public void change() {
        ElevatorPortal elevatorPortal = (ElevatorPortal) this.amenity;

        if (elevatorPortal.isOpen()) {
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

package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Drawable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorShaft;

public class EscalatorGraphic extends AmenityGraphic implements Changeable {
    private static final int ROW_SPAN_VERTICAL = 2;
    private static final int COLUMN_SPAN_VERTICAL = 2;

    private static final int ROW_SPAN_HORIZONTAL = 2;
    private static final int COLUMN_SPAN_HORIZONTAL = 2;

    private static final int ROW_OFFSET = 0;
    private static final int COLUMN_OFFSET = 0;

    public EscalatorGraphic(EscalatorPortal escalatorPortal) {
        super(
                escalatorPortal,
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
                if (escalatorPortal.getEscalatorShaft().getEscalatorDirection()
                        == EscalatorShaft.EscalatorDirection.UP) {
                    this.graphicIndex = 0;
                } else {
                    this.graphicIndex = 1;
                }

                break;
            case RIGHT:
                if (escalatorPortal.getEscalatorShaft().getEscalatorDirection()
                        == EscalatorShaft.EscalatorDirection.UP) {
                    this.graphicIndex = 2;
                } else {
                    this.graphicIndex = 3;
                }

                break;
            case DOWN:
                if (escalatorPortal.getEscalatorShaft().getEscalatorDirection()
                        == EscalatorShaft.EscalatorDirection.UP) {
                    this.graphicIndex = 4;
                } else {
                    this.graphicIndex = 5;
                }
                break;
            case LEFT:
                if (escalatorPortal.getEscalatorShaft().getEscalatorDirection()
                        == EscalatorShaft.EscalatorDirection.UP) {
                    this.graphicIndex = 6;
                } else {
                    this.graphicIndex = 7;
                }

                break;
        }
    }

    @Override
    public void change() {
        EscalatorPortal escalatorPortal = (EscalatorPortal) this.amenity;

        if (escalatorPortal.getEscalatorShaft().getEscalatorDirection() == EscalatorShaft.EscalatorDirection.DOWN) {
            this.graphicIndex++;
        } else {
            this.graphicIndex--;
        }
    }

    public void decideLowerOrUpper() {
        EscalatorPortal escalatorPortal = (EscalatorPortal) this.getAmenity();
        EscalatorShaft escalatorShaft = escalatorPortal.getEscalatorShaft();

        if (escalatorShaft.getLowerPortal() == escalatorPortal) {
            if (this.graphicIndex >= 8 && this.graphicIndex <= 15) {
                this.graphicIndex -= 8;
            }
        } else {
            if (this.graphicIndex >= 0 && this.graphicIndex <= 7) {
                this.graphicIndex += 8;
            }
        }
    }
}

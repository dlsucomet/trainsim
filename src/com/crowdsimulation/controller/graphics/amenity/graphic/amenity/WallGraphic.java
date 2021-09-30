package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Wall;

public class WallGraphic extends AmenityGraphic implements Cyclable, Changeable {
    private static final int ROW_SPAN_VERTICAL = 1;
    private static final int COLUMN_SPAN_VERTICAL = 1;

    private static final int ROW_SPAN_HORIZONTAL = 1;
    private static final int COLUMN_SPAN_HORIZONTAL = 1;

    private static final int ROW_SPAN_BUILDING_COLUMN = 2;
    private static final int COLUMN_SPAN_BUILDING_COLUMN = 1;

    private static final int NORMAL_ROW_OFFSET = 0;
    private static final int NORMAL_COLUMN_OFFSET = 0;

    private static final int BUILDING_COLUMN_ROW_OFFSET = -1;
    private static final int BUILDING_COLUMN_COLUMN_OFFSET = 0;

    public WallGraphic(Wall wall) {
        super(
                wall,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL,
                GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                        ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL,
                NORMAL_ROW_OFFSET,
                NORMAL_COLUMN_OFFSET
        );

        this.graphicIndex = 0;

        change();
    }

    @Override
    public void cycle() {
        switch (this.graphicIndex) {
            case 2:
            case 4:
                this.graphicIndex++;

                break;
            case 3:
            case 5:
                this.graphicIndex--;

                break;
        }
    }

    @Override
    public void change() {
        Wall wall = (Wall) this.amenity;

        switch (wall.getWallType()) {
            case WALL:
                this.graphicIndex = 0;

                this.getAmenityGraphicScale().setRowSpan(
                        GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                                ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL
                );

                this.getAmenityGraphicScale().setColumnSpan(
                        GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                                ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL
                );

                this.getAmenityGraphicOffset().setRowOffset(NORMAL_ROW_OFFSET);
                this.getAmenityGraphicOffset().setColumnOffset(NORMAL_COLUMN_OFFSET);

                this.getGraphicLocation().setGraphicWidth(COLUMN_SPAN_VERTICAL);
                this.getGraphicLocation().setGraphicHeight(ROW_SPAN_VERTICAL);

                break;
            case BUILDING_COLUMN:
                this.graphicIndex = 1;

                this.getAmenityGraphicScale().setRowSpan(ROW_SPAN_BUILDING_COLUMN);
                this.getAmenityGraphicScale().setColumnSpan(COLUMN_SPAN_BUILDING_COLUMN);

                this.getAmenityGraphicOffset().setRowOffset(BUILDING_COLUMN_ROW_OFFSET);
                this.getAmenityGraphicOffset().setColumnOffset(BUILDING_COLUMN_COLUMN_OFFSET);

                this.getGraphicLocation().setGraphicWidth(COLUMN_SPAN_BUILDING_COLUMN);
                this.getGraphicLocation().setGraphicHeight(ROW_SPAN_BUILDING_COLUMN);

                break;
            case BELT_BARRIER:
                if (this.graphicIndex == 4 || this.graphicIndex == 5) {
                    if (this.graphicIndex == 4) {
                        this.graphicIndex = 2;
                    } else {
                        this.graphicIndex = 3;
                    }
                } else {
                    this.graphicIndex = 2;
                }

                this.getAmenityGraphicScale().setRowSpan(
                        GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                                ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL
                );

                this.getAmenityGraphicScale().setColumnSpan(
                        GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                                ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL
                );

                this.getAmenityGraphicOffset().setRowOffset(NORMAL_ROW_OFFSET);
                this.getAmenityGraphicOffset().setColumnOffset(NORMAL_COLUMN_OFFSET);

                this.getGraphicLocation().setGraphicWidth(COLUMN_SPAN_VERTICAL);
                this.getGraphicLocation().setGraphicHeight(ROW_SPAN_VERTICAL);

                break;
            case METAL_BARRIER:
                if (this.graphicIndex == 3 || this.graphicIndex == 4) {
                    if (this.graphicIndex == 3) {
                        this.graphicIndex = 4;
                    } else {
                        this.graphicIndex = 5;
                    }
                } else {
                    this.graphicIndex = 4;
                }

                this.getAmenityGraphicScale().setRowSpan(
                        GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                                ? ROW_SPAN_VERTICAL : ROW_SPAN_HORIZONTAL
                );

                this.getAmenityGraphicScale().setColumnSpan(
                        GraphicsController.currentAmenityFootprint.getCurrentRotation().isVertical()
                                ? COLUMN_SPAN_VERTICAL : COLUMN_SPAN_HORIZONTAL
                );

                this.getAmenityGraphicOffset().setRowOffset(NORMAL_ROW_OFFSET);
                this.getAmenityGraphicOffset().setColumnOffset(NORMAL_COLUMN_OFFSET);

                this.getGraphicLocation().setGraphicWidth(COLUMN_SPAN_VERTICAL);
                this.getGraphicLocation().setGraphicHeight(ROW_SPAN_VERTICAL);

                break;
        }
    }
}

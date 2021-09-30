package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;

public class TrainDoorGraphic extends AmenityGraphic implements Changeable {
    private static final int ROW_SPAN_VERTICAL = 1;
    private static final int COLUMN_SPAN_VERTICAL = 4;

    private static final int NORMAL_ROW_OFFSET = 0;
    private static final int NORMAL_COLUMN_OFFSET = 0;

    public TrainDoorGraphic(TrainDoor trainDoor) {
        super(
                trainDoor,
                ROW_SPAN_VERTICAL,
                COLUMN_SPAN_VERTICAL,
                NORMAL_ROW_OFFSET,
                NORMAL_COLUMN_OFFSET
        );

        this.graphicIndex = 0;

        change();
    }

    @Override
    public void change() {
        TrainDoor trainDoor = (TrainDoor) this.amenity;

        if (trainDoor.getTrainDoorOrientation() == Station.StationOrientation.SIDE_PLATFORM) {
            switch (trainDoor.getPlatformDirection()) {
                case NORTHBOUND:
                case WESTBOUND:
                    this.graphicIndex = 0;

                    break;
                case SOUTHBOUND:
                case EASTBOUND:
                    this.graphicIndex = 1;

                    break;
            }
        } else {
            switch (trainDoor.getPlatformDirection()) {
                case NORTHBOUND:
                case WESTBOUND:
                    this.graphicIndex = 1;

                    break;
                case SOUTHBOUND:
                case EASTBOUND:
                    this.graphicIndex = 0;

                    break;
            }
        }
    }
}

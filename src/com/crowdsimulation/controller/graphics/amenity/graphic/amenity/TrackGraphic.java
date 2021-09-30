package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Track;

public class TrackGraphic extends AmenityGraphic implements Changeable {
    private static final int ROW_SPAN_VERTICAL = 2;
    private static final int COLUMN_SPAN_VERTICAL = 1;

    private static final int NORMAL_ROW_OFFSET = 0;
    private static final int NORMAL_COLUMN_OFFSET = 0;

    public TrackGraphic(Track track) {
        super(
                track,
                ROW_SPAN_VERTICAL,
                COLUMN_SPAN_VERTICAL,
                NORMAL_ROW_OFFSET,
                NORMAL_COLUMN_OFFSET
        );

        change();
    }

    @Override
    public void change() {
        Track track = (Track) this.amenity;
        PassengerMovement.TravelDirection trackDirection = track.getTrackDirection();

        if (
                trackDirection == PassengerMovement.TravelDirection.NORTHBOUND
                        || trackDirection == PassengerMovement.TravelDirection.WESTBOUND
        ) {
            this.graphicIndex = 1;
        } else {
            this.graphicIndex = 0;
        }
    }
}

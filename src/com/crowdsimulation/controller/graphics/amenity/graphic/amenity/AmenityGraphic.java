package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

import com.crowdsimulation.controller.graphics.amenity.graphic.Graphic;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Track;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Wall;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AmenityGraphic extends Graphic {
    // The amenity sprite sheet
    public static final String AMENITY_SPRITE_SHEET_URL
            = "com/crowdsimulation/view/image/amenity/amenity_spritesheet.png";

    public static final HashMap<Class<?>, List<AmenityGraphicLocation>> AMENITY_GRAPHICS = new HashMap<>();

    static {
        // The designated graphic of the amenities
        final List<AmenityGraphicLocation> stationGateGraphic = new ArrayList<>();
        stationGateGraphic.add(new AmenityGraphicLocation(15, 0));
        stationGateGraphic.add(new AmenityGraphicLocation(15, 6));
        stationGateGraphic.add(new AmenityGraphicLocation(15, 4));
        stationGateGraphic.add(new AmenityGraphicLocation(15, 2));
        stationGateGraphic.add(new AmenityGraphicLocation(15, 8));
        stationGateGraphic.add(new AmenityGraphicLocation(15, 14));
        stationGateGraphic.add(new AmenityGraphicLocation(15, 12));
        stationGateGraphic.add(new AmenityGraphicLocation(15, 10));
        AMENITY_GRAPHICS.put(StationGate.class, stationGateGraphic);

        final List<AmenityGraphicLocation> securityGraphic = new ArrayList<>();
        securityGraphic.add(new AmenityGraphicLocation(9, 12));
        securityGraphic.add(new AmenityGraphicLocation(9, 13));
        AMENITY_GRAPHICS.put(Security.class, securityGraphic);

        final List<AmenityGraphicLocation> stairsGraphic = new ArrayList<>();
        stairsGraphic.add(new AmenityGraphicLocation(1, 0));
        stairsGraphic.add(new AmenityGraphicLocation(1, 2));
        stairsGraphic.add(new AmenityGraphicLocation(1, 4));
        stairsGraphic.add(new AmenityGraphicLocation(1, 6));
        stairsGraphic.add(new AmenityGraphicLocation(3, 0));
        stairsGraphic.add(new AmenityGraphicLocation(3, 2));
        stairsGraphic.add(new AmenityGraphicLocation(3, 4));
        stairsGraphic.add(new AmenityGraphicLocation(3, 6));
        AMENITY_GRAPHICS.put(StairPortal.class, stairsGraphic);

        final List<AmenityGraphicLocation> escalatorGraphic = new ArrayList<>();
        escalatorGraphic.add(new AmenityGraphicLocation(5, 0));
        escalatorGraphic.add(new AmenityGraphicLocation(5, 2));
        escalatorGraphic.add(new AmenityGraphicLocation(5, 4));
        escalatorGraphic.add(new AmenityGraphicLocation(5, 6));
        escalatorGraphic.add(new AmenityGraphicLocation(5, 8));
        escalatorGraphic.add(new AmenityGraphicLocation(5, 10));
        escalatorGraphic.add(new AmenityGraphicLocation(5, 12));
        escalatorGraphic.add(new AmenityGraphicLocation(5, 14));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 0));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 2));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 4));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 6));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 8));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 10));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 12));
        escalatorGraphic.add(new AmenityGraphicLocation(7, 14));
        AMENITY_GRAPHICS.put(EscalatorPortal.class, escalatorGraphic);

        final List<AmenityGraphicLocation> elevatorGraphic = new ArrayList<>();
        elevatorGraphic.add(new AmenityGraphicLocation(1, 8));
        elevatorGraphic.add(new AmenityGraphicLocation(1, 10));
        elevatorGraphic.add(new AmenityGraphicLocation(1, 12));
        elevatorGraphic.add(new AmenityGraphicLocation(1, 14));
        elevatorGraphic.add(new AmenityGraphicLocation(3, 8));
        elevatorGraphic.add(new AmenityGraphicLocation(3, 10));
        elevatorGraphic.add(new AmenityGraphicLocation(3, 12));
        elevatorGraphic.add(new AmenityGraphicLocation(3, 14));
        AMENITY_GRAPHICS.put(ElevatorPortal.class, elevatorGraphic);

        final List<AmenityGraphicLocation> ticketBoothGraphic = new ArrayList<>();
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 0));
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 1));
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 2));
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 3));
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 4));
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 5));
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 6));
        ticketBoothGraphic.add(new AmenityGraphicLocation(9, 7));
        AMENITY_GRAPHICS.put(TicketBooth.class, ticketBoothGraphic);

        final List<AmenityGraphicLocation> turnstileGraphic = new ArrayList<>();
        turnstileGraphic.add(new AmenityGraphicLocation(11, 3));
        turnstileGraphic.add(new AmenityGraphicLocation(12, 0));
        turnstileGraphic.add(new AmenityGraphicLocation(11, 0));
        turnstileGraphic.add(new AmenityGraphicLocation(12, 1));
        AMENITY_GRAPHICS.put(Turnstile.class, turnstileGraphic);

        final List<AmenityGraphicLocation> trainDoorGraphic = new ArrayList<>();
        trainDoorGraphic.add(new AmenityGraphicLocation(12, 2));
        trainDoorGraphic.add(new AmenityGraphicLocation(12, 6));
        trainDoorGraphic.add(new AmenityGraphicLocation(13, 2));
        trainDoorGraphic.add(new AmenityGraphicLocation(13, 6));
        AMENITY_GRAPHICS.put(TrainDoor.class, trainDoorGraphic);

        final List<AmenityGraphicLocation> trackGraphic = new ArrayList<>();
        trackGraphic.add(new AmenityGraphicLocation(12, 10));
        trackGraphic.add(new AmenityGraphicLocation(12, 11));
        AMENITY_GRAPHICS.put(Track.class, trackGraphic);

        final List<AmenityGraphicLocation> wallGraphic = new ArrayList<>();
        wallGraphic.add(new AmenityGraphicLocation(9, 8));
        wallGraphic.add(new AmenityGraphicLocation(9, 9));
        wallGraphic.add(new AmenityGraphicLocation(9, 10));
        wallGraphic.add(new AmenityGraphicLocation(10, 10));
        wallGraphic.add(new AmenityGraphicLocation(9, 11));
        wallGraphic.add(new AmenityGraphicLocation(10, 11));
        AMENITY_GRAPHICS.put(Wall.class, wallGraphic);
    }

    protected final Amenity amenity;

    protected final List<AmenityGraphicLocation> graphics;
    protected int graphicIndex;

    // Denotes the rows and columns spanned by this graphic
    private final AmenityGraphicScale amenityGraphicScale;

    // Denotes the offset of this graphic
    private final AmenityGraphicOffset amenityGraphicOffset;

    public AmenityGraphic(Amenity amenity, int rowSpan, int columnSpan, int rowOffset, int columnOffset) {
        this.amenity = amenity;

        this.amenityGraphicScale = new AmenityGraphicScale(rowSpan, columnSpan);
        this.amenityGraphicOffset = new AmenityGraphicOffset(rowOffset, columnOffset);

        this.graphics = new ArrayList<>();

        for (AmenityGraphicLocation amenityGraphicLocation : AMENITY_GRAPHICS.get(amenity.getClass())) {
            AmenityGraphicLocation newAmenityGraphicLocation = new AmenityGraphicLocation(
                    amenityGraphicLocation.getGraphicRow(),
                    amenityGraphicLocation.getGraphicColumn()
            );

            newAmenityGraphicLocation.setGraphicWidth(columnSpan);
            newAmenityGraphicLocation.setGraphicHeight(rowSpan);

            this.graphics.add(newAmenityGraphicLocation);
        }

        this.graphicIndex = 0;
    }

    public AmenityGraphicScale getAmenityGraphicScale() {
        return amenityGraphicScale;
    }

    public AmenityGraphicOffset getAmenityGraphicOffset() {
        return amenityGraphicOffset;
    }

    public Amenity getAmenity() {
        return amenity;
    }

    public AmenityGraphicLocation getGraphicLocation() {
        return this.graphics.get(this.graphicIndex);
    }

    public static class AmenityGraphicScale implements Environment {
        private int rowSpan;
        private int columnSpan;

        public AmenityGraphicScale(int rowSpan, int columnSpan) {
            this.rowSpan = rowSpan;
            this.columnSpan = columnSpan;
        }

        public int getRowSpan() {
            return rowSpan;
        }

        public void setRowSpan(int rowSpan) {
            this.rowSpan = rowSpan;
        }

        public int getColumnSpan() {
            return columnSpan;
        }

        public void setColumnSpan(int columnSpan) {
            this.columnSpan = columnSpan;
        }
    }

    public static class AmenityGraphicOffset implements Environment {
        private int rowOffset;
        private int columnOffset;

        public AmenityGraphicOffset(int rowOffset, int columnOffset) {
            this.rowOffset = rowOffset;
            this.columnOffset = columnOffset;
        }

        public int getRowOffset() {
            return rowOffset;
        }

        public void setRowOffset(int rowOffset) {
            this.rowOffset = rowOffset;
        }

        public int getColumnOffset() {
            return columnOffset;
        }

        public void setColumnOffset(int columnOffset) {
            this.columnOffset = columnOffset;
        }
    }
}

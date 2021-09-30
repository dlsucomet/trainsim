package com.crowdsimulation.controller.graphics.amenity.graphic.passenger;

import com.crowdsimulation.controller.graphics.amenity.graphic.Graphic;
import com.crowdsimulation.controller.graphics.amenity.graphic.amenity.Changeable;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.simulator.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PassengerGraphic extends Graphic implements Changeable {
    // The amenity sprite sheet
    public static final String PASSENGER_SPRITE_SHEET_URL
            = "com/crowdsimulation/view/image/passenger/passenger_spritesheet.png";

    public static final HashMap<Passenger.PassengerInformation.Gender, List<List<PassengerGraphicLocation>>> PASSENGER_GRAPHICS
            = new HashMap<>();

    static {
        List<List<PassengerGraphicLocation>> femaleGraphics = new ArrayList<>();
        List<List<PassengerGraphicLocation>> maleGraphics = new ArrayList<>();

        // The designated graphic of the passengers
        // Female graphics
        final List<PassengerGraphicLocation> female1Graphic = new ArrayList<>();
        female1Graphic.add(new PassengerGraphicLocation(0, 0));
        female1Graphic.add(new PassengerGraphicLocation(0, 1));
        female1Graphic.add(new PassengerGraphicLocation(0, 2));
        female1Graphic.add(new PassengerGraphicLocation(0, 3));
        femaleGraphics.add(female1Graphic);

        final List<PassengerGraphicLocation> female2Graphic = new ArrayList<>();
        female2Graphic.add(new PassengerGraphicLocation(1, 0));
        female2Graphic.add(new PassengerGraphicLocation(1, 1));
        female2Graphic.add(new PassengerGraphicLocation(1, 2));
        female2Graphic.add(new PassengerGraphicLocation(1, 3));
        femaleGraphics.add(female2Graphic);

        final List<PassengerGraphicLocation> female3Graphic = new ArrayList<>();
        female3Graphic.add(new PassengerGraphicLocation(2, 0));
        female3Graphic.add(new PassengerGraphicLocation(2, 1));
        female3Graphic.add(new PassengerGraphicLocation(2, 2));
        female3Graphic.add(new PassengerGraphicLocation(2, 3));
        femaleGraphics.add(female3Graphic);

        final List<PassengerGraphicLocation> female4Graphic = new ArrayList<>();
        female4Graphic.add(new PassengerGraphicLocation(3, 0));
        female4Graphic.add(new PassengerGraphicLocation(3, 1));
        female4Graphic.add(new PassengerGraphicLocation(3, 2));
        female4Graphic.add(new PassengerGraphicLocation(3, 3));
        femaleGraphics.add(female4Graphic);

        final List<PassengerGraphicLocation> female5Graphic = new ArrayList<>();
        female5Graphic.add(new PassengerGraphicLocation(4, 0));
        female5Graphic.add(new PassengerGraphicLocation(4, 1));
        female5Graphic.add(new PassengerGraphicLocation(4, 2));
        female5Graphic.add(new PassengerGraphicLocation(4, 3));
        femaleGraphics.add(female5Graphic);

        final List<PassengerGraphicLocation> female6Graphic = new ArrayList<>();
        female6Graphic.add(new PassengerGraphicLocation(5, 0));
        female6Graphic.add(new PassengerGraphicLocation(5, 1));
        female6Graphic.add(new PassengerGraphicLocation(5, 2));
        female6Graphic.add(new PassengerGraphicLocation(5, 3));
        femaleGraphics.add(female6Graphic);

        final List<PassengerGraphicLocation> female7Graphic = new ArrayList<>();
        female7Graphic.add(new PassengerGraphicLocation(6, 0));
        female7Graphic.add(new PassengerGraphicLocation(6, 1));
        female7Graphic.add(new PassengerGraphicLocation(6, 2));
        female7Graphic.add(new PassengerGraphicLocation(6, 3));
        femaleGraphics.add(female7Graphic);

        final List<PassengerGraphicLocation> female8Graphic = new ArrayList<>();
        female8Graphic.add(new PassengerGraphicLocation(7, 0));
        female8Graphic.add(new PassengerGraphicLocation(7, 1));
        female8Graphic.add(new PassengerGraphicLocation(7, 2));
        female8Graphic.add(new PassengerGraphicLocation(7, 3));
        femaleGraphics.add(female8Graphic);

        final List<PassengerGraphicLocation> female9Graphic = new ArrayList<>();
        female9Graphic.add(new PassengerGraphicLocation(8, 0));
        female9Graphic.add(new PassengerGraphicLocation(8, 1));
        female9Graphic.add(new PassengerGraphicLocation(8, 2));
        female9Graphic.add(new PassengerGraphicLocation(8, 3));
        femaleGraphics.add(female9Graphic);

        final List<PassengerGraphicLocation> female10Graphic = new ArrayList<>();
        female10Graphic.add(new PassengerGraphicLocation(9, 0));
        female10Graphic.add(new PassengerGraphicLocation(9, 1));
        female10Graphic.add(new PassengerGraphicLocation(9, 2));
        female10Graphic.add(new PassengerGraphicLocation(9, 3));
        femaleGraphics.add(female10Graphic);

        // Male graphics
        final List<PassengerGraphicLocation> male1Graphic = new ArrayList<>();
        male1Graphic.add(new PassengerGraphicLocation(10, 0));
        male1Graphic.add(new PassengerGraphicLocation(10, 1));
        male1Graphic.add(new PassengerGraphicLocation(10, 2));
        male1Graphic.add(new PassengerGraphicLocation(10, 3));
        maleGraphics.add(male1Graphic);

        final List<PassengerGraphicLocation> male2Graphic = new ArrayList<>();
        male2Graphic.add(new PassengerGraphicLocation(11, 0));
        male2Graphic.add(new PassengerGraphicLocation(11, 1));
        male2Graphic.add(new PassengerGraphicLocation(11, 2));
        male2Graphic.add(new PassengerGraphicLocation(11, 3));
        maleGraphics.add(male2Graphic);

        final List<PassengerGraphicLocation> male3Graphic = new ArrayList<>();
        male3Graphic.add(new PassengerGraphicLocation(12, 0));
        male3Graphic.add(new PassengerGraphicLocation(12, 1));
        male3Graphic.add(new PassengerGraphicLocation(12, 2));
        male3Graphic.add(new PassengerGraphicLocation(12, 3));
        maleGraphics.add(male3Graphic);

        final List<PassengerGraphicLocation> male4Graphic = new ArrayList<>();
        male4Graphic.add(new PassengerGraphicLocation(13, 0));
        male4Graphic.add(new PassengerGraphicLocation(13, 1));
        male4Graphic.add(new PassengerGraphicLocation(13, 2));
        male4Graphic.add(new PassengerGraphicLocation(13, 3));
        maleGraphics.add(male4Graphic);

        final List<PassengerGraphicLocation> male5Graphic = new ArrayList<>();
        male5Graphic.add(new PassengerGraphicLocation(14, 0));
        male5Graphic.add(new PassengerGraphicLocation(14, 1));
        male5Graphic.add(new PassengerGraphicLocation(14, 2));
        male5Graphic.add(new PassengerGraphicLocation(14, 3));
        maleGraphics.add(male5Graphic);

        final List<PassengerGraphicLocation> male6Graphic = new ArrayList<>();
        male6Graphic.add(new PassengerGraphicLocation(15, 0));
        male6Graphic.add(new PassengerGraphicLocation(15, 1));
        male6Graphic.add(new PassengerGraphicLocation(15, 2));
        male6Graphic.add(new PassengerGraphicLocation(15, 3));
        maleGraphics.add(male6Graphic);

        final List<PassengerGraphicLocation> male7Graphic = new ArrayList<>();
        male7Graphic.add(new PassengerGraphicLocation(16, 0));
        male7Graphic.add(new PassengerGraphicLocation(16, 1));
        male7Graphic.add(new PassengerGraphicLocation(16, 2));
        male7Graphic.add(new PassengerGraphicLocation(16, 3));
        maleGraphics.add(male7Graphic);

        final List<PassengerGraphicLocation> male8Graphic = new ArrayList<>();
        male8Graphic.add(new PassengerGraphicLocation(17, 0));
        male8Graphic.add(new PassengerGraphicLocation(17, 1));
        male8Graphic.add(new PassengerGraphicLocation(17, 2));
        male8Graphic.add(new PassengerGraphicLocation(17, 3));
        maleGraphics.add(male8Graphic);

        final List<PassengerGraphicLocation> male9Graphic = new ArrayList<>();
        male9Graphic.add(new PassengerGraphicLocation(18, 0));
        male9Graphic.add(new PassengerGraphicLocation(18, 1));
        male9Graphic.add(new PassengerGraphicLocation(18, 2));
        male9Graphic.add(new PassengerGraphicLocation(18, 3));
        maleGraphics.add(male9Graphic);

        final List<PassengerGraphicLocation> male10Graphic = new ArrayList<>();
        male10Graphic.add(new PassengerGraphicLocation(19, 0));
        male10Graphic.add(new PassengerGraphicLocation(19, 1));
        male10Graphic.add(new PassengerGraphicLocation(19, 2));
        male10Graphic.add(new PassengerGraphicLocation(19, 3));
        maleGraphics.add(male10Graphic);

        PASSENGER_GRAPHICS.put(Passenger.PassengerInformation.Gender.FEMALE, femaleGraphics);
        PASSENGER_GRAPHICS.put(Passenger.PassengerInformation.Gender.MALE, maleGraphics);
    }

    private final Passenger passenger;

    protected final List<PassengerGraphicLocation> graphics;
    protected int graphicIndex;

    public PassengerGraphic(Passenger passenger) {
        final int typesPerGender = 10;

        this.passenger = passenger;

        this.graphics = new ArrayList<>();

        // Get a random number from 0 to 4 - this will be the random color of the passenger
        int graphicType = Simulator.RANDOM_NUMBER_GENERATOR.nextInt(typesPerGender);

        List<PassengerGraphicLocation> passengerGraphics
                = PASSENGER_GRAPHICS.get(passenger.getGender()).get(graphicType);

        for (PassengerGraphicLocation passengerGraphicLocations : passengerGraphics) {
            PassengerGraphicLocation newPassengerGraphicLocation = new PassengerGraphicLocation(
                    passengerGraphicLocations.getGraphicRow(),
                    passengerGraphicLocations.getGraphicColumn()
            );

            newPassengerGraphicLocation.setGraphicWidth(1);
            newPassengerGraphicLocation.setGraphicHeight(1);

            this.graphics.add(newPassengerGraphicLocation);
        }

        this.graphicIndex = 2;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public PassengerGraphicLocation getGraphicLocation() {
        return this.graphics.get(this.graphicIndex);
    }

    @Override
    public void change() {
        Passenger passenger = this.passenger;

        double passengerHeading = passenger.getPassengerMovement().getHeading();
        double passengerHeadingDegrees = Math.toDegrees(passengerHeading);

        if (passengerHeadingDegrees >= 315 && passengerHeadingDegrees < 360
                || passengerHeadingDegrees >= 0 && passengerHeadingDegrees < 45) {
            this.graphicIndex = 1;
        } else if (passengerHeadingDegrees >= 45 && passengerHeadingDegrees < 135) {
            this.graphicIndex = 0;
        } else if (passengerHeadingDegrees >= 135 && passengerHeadingDegrees < 225) {
            this.graphicIndex = 3;
        } else if (passengerHeadingDegrees >= 225 && passengerHeadingDegrees < 315) {
            this.graphicIndex = 2;
        }
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

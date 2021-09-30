package com.crowdsimulation.model.core.environment.station.patch.position;

import java.util.List;

public class Vector {
    private Coordinates startingPosition;
    private double heading;
    private Coordinates futurePosition;

    private double xDisplacement;
    private double yDisplacement;
    private double magnitude;

    public Vector(Vector vector) {
        this.startingPosition = vector.getStartingPosition();
        this.heading = vector.getHeading();
        this.futurePosition = vector.getFuturePosition();

        this.xDisplacement = vector.getXDisplacement();
        this.yDisplacement = vector.getYDisplacement();
        this.magnitude = vector.getMagnitude();
    }

    // Create a vector given the starting position, the heading, and the future position
    public Vector(
            Coordinates startingPosition,
            double heading,
            Coordinates futurePosition,
            double magnitude
    ) {
        setVector(
                startingPosition,
                heading,
                futurePosition
        );

        this.magnitude = magnitude;
    }

    private static double computeMagnitude(Coordinates startingPosition, Coordinates futurePosition) {
        return Coordinates.distance(
                startingPosition,
                futurePosition
        );
    }

    // Create a vector given already-computed elements
    public Vector(
            Coordinates startingPosition,
            double heading,
            Coordinates futurePosition,
            double xDisplacement,
            double yDisplacement,
            double magnitude
    ) {
        this.startingPosition = startingPosition;
        this.heading = heading;
        this.futurePosition = futurePosition;
        this.xDisplacement = xDisplacement;
        this.yDisplacement = yDisplacement;

        this.magnitude = magnitude;
    }

    // Given the starting position, heading, and ending position compute for the x and y displacements of the vector
    public void setVector(
            Coordinates currentPosition,
            double currentHeading,
            Coordinates futurePosition
    ) {
        // Set the known values first
        this.startingPosition = currentPosition;
        this.heading = currentHeading;
        this.futurePosition = futurePosition;

        // Then from these known values, compute the displacement values
        // They will be needed for adding and subtracting vectors
        this.xDisplacement = this.futurePosition.getX() - this.startingPosition.getX();
        this.yDisplacement = this.futurePosition.getY() - this.startingPosition.getY();

        this.magnitude = Coordinates.distance(this.startingPosition, this.futurePosition);
    }

    // With the same starting position and magnitude, adjust the heading of the vector
    public void adjustHeading(double newHeading) {
        // Compute the new future position with a new heading
        Coordinates futurePosition = Coordinates.computeFuturePosition(
                this.startingPosition,
                newHeading,
                this.magnitude
        );

        this.setVector(
                this.startingPosition,
                newHeading,
                futurePosition
        );
    }

    // With the same starting position and heading, adjust the magnitude of the vector
    public void adjustMagnitude(double newMagnitude) {
        // Compute the new future position with a new magnitude
        Coordinates futurePosition = Coordinates.computeFuturePosition(
                this.startingPosition,
                this.heading,
                newMagnitude
        );

        this.setVector(
                this.startingPosition,
                this.heading,
                futurePosition
        );
    }

    public Coordinates getStartingPosition() {
        return startingPosition;
    }

    public double getHeading() {
        return heading;
    }

    public Coordinates getFuturePosition() {
        return futurePosition;
    }

    public double getXDisplacement() {
        return xDisplacement;
    }

    public double getYDisplacement() {
        return yDisplacement;
    }

    public double getMagnitude() {
        return magnitude;
    }

    // Given a list of vectors, compute for the resultant vector - the sum of all such vectors
    public static Vector computeResultantVector(Coordinates startingPosition, List<Vector> vectors) {
        double sumX = 0.0;
        double sumY = 0.0;

        // Get the sum of the vector displacements
        for (Vector vector : vectors) {
            // Ignore null vectors, if any is encountered in the list
            if (vector != null) {
                sumX += vector.getXDisplacement();
                sumY += vector.getYDisplacement();
            }
        }

        // Compute for the ending position, given the computed displacements
        double endX = startingPosition.getX() + sumX;
        double endY = startingPosition.getY() + sumY;
        Coordinates endingPosition = new Coordinates(endX, endY);

        // Finally, compute for the heading from the starting to the ending position
        double newHeading = Coordinates.headingTowards(startingPosition, endingPosition);

        // If the heading is NaN, this means the result of the vector superposition ended up in the starting point
        // That is, there is no vector produced
        // In this case, return null
        if (!Double.isNaN(newHeading)) {
            // Then return the resultant vector given the computed values
            return new Vector(
                    startingPosition,
                    newHeading,
                    endingPosition,
                    sumX,
                    sumY,
                    Vector.computeMagnitude(
                            startingPosition,
                            endingPosition
                    )
            );
        } else {
            return null;
        }
    }
}

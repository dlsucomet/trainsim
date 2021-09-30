package com.trainsimulation.model.utility;

import com.trainsimulation.model.core.environment.infrastructure.track.Segment;

// Contains information about a train carriage's location
public class TrainCarriageLocation {
    // Denotes the distance this carriage has cleared
    private double segmentClearance;

    // Denotes where (segment) this carriage is located
    private Segment segmentLocation;

    // Denotes the index of the direction this carriage should head towards
    private int directionIndex;

    // Denotes the index of the direction this carriage should head towards, but virtually (used only in headway
    // computation)
    private int virtualDirectionIndex;

    public TrainCarriageLocation() {
        this.directionIndex = 0;
    }

    public double getSegmentClearance() {
        return segmentClearance;
    }

    public void setSegmentClearance(double segmentClearance) {
        this.segmentClearance = segmentClearance;
    }

    public Segment getSegmentLocation() {
        return segmentLocation;
    }

    public void setSegmentLocation(Segment segmentLocation) {
        this.segmentLocation = segmentLocation;
    }

    public int getDirectionIndex() {
        return directionIndex;
    }

    public void resetDirectionIndex() {
        this.directionIndex = 0;
    }

    public void nextDirectionIndex() {
        this.directionIndex++;
    }

    public int getVirtualDirectionIndex() {
        return virtualDirectionIndex;
    }

    public void resetVirtualDirectionIndex() {
        this.virtualDirectionIndex = this.directionIndex;
    }

    public void nextVirtualDirectionIndex() {
        this.virtualDirectionIndex++;
    }
}

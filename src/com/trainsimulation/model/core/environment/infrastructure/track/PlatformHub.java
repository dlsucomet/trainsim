package com.trainsimulation.model.core.environment.infrastructure.track;

import com.trainsimulation.model.core.environment.TrainSystem;

// Allows places where trains stop (platforms and depots) to connect with the rest of the train system
public class PlatformHub extends Track {
    // Allows a segment to come in to the platform
    private final Junction inConnector;

    // Gives the train a place to stop in the platform
    private final Segment platformSegment;

    // Allows a segment to come out of the platform
    private final Junction outConnector;

    public PlatformHub(TrainSystem trainSystem, final int platformSegmentLength, final Direction direction) {
        super(trainSystem);

        this.inConnector = new Junction(trainSystem);
        this.outConnector = new Junction(trainSystem);
        this.platformSegment = new Segment(trainSystem, platformSegmentLength);

        // Connect the segments of the platform hub
        Track.setPlatformHub(inConnector, platformSegment, outConnector, direction);
    }

    public Junction getInConnector() {
        return inConnector;
    }

    public Segment getPlatformSegment() {
        return platformSegment;
    }

    public Junction getOutConnector() {
        return outConnector;
    }
}

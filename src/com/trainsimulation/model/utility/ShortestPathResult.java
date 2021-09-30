package com.trainsimulation.model.utility;

import com.trainsimulation.model.core.environment.infrastructure.track.Track;

import java.util.List;

public class ShortestPathResult {
    private final List<Track.Direction> directions;
    private final int distance;

    public ShortestPathResult(List<Track.Direction> directions, int distance) {
        this.directions = directions;
        this.distance = distance;
    }

    public List<Track.Direction> getDirections() {
        return directions;
    }

    public int getDistance() {
        return distance;
    }
}

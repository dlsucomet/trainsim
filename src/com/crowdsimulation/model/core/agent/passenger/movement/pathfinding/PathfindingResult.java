package com.crowdsimulation.model.core.agent.passenger.movement.pathfinding;

import com.crowdsimulation.model.core.environment.Environment;

public abstract class PathfindingResult implements Environment {
    private final double distance;

    public PathfindingResult(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }
}

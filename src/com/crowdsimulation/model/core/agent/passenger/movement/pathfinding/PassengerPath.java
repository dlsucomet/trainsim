package com.crowdsimulation.model.core.agent.passenger.movement.pathfinding;

import com.crowdsimulation.model.core.environment.station.patch.Patch;

import java.util.Stack;

public class PassengerPath extends PathfindingResult {
    private final Stack<Patch> path;

    public PassengerPath(PassengerPath passengerPath) {
        super(passengerPath.getDistance());

        this.path = (Stack<Patch>) passengerPath.path.clone();
    }

    public PassengerPath(double distance, Stack<Patch> path) {
        super(distance);

        this.path = path;
    }

    public Stack<Patch> getPath() {
        return path;
    }
}

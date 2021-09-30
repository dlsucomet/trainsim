package com.crowdsimulation.model.core.agent.passenger.movement.pathfinding;

import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;

import java.util.List;

public class MultipleFloorPassengerPath extends PathfindingResult {
    private final List<Portal> portals;

    public MultipleFloorPassengerPath(double distance, List<Portal> portals) {
        super(distance);

        this.portals = portals;
    }

    public List<Portal> getPortals() {
        return portals;
    }
}

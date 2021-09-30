package com.crowdsimulation.model.core.agent.passenger.movement.pathfinding;

import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;

import java.util.List;

public class DirectoryResult {
    private final List<Portal> portals;
    private final Amenity goalAmenity;
    private final double distance;

    public DirectoryResult(List<Portal> portals, Amenity goalAmenity, double distance) {
        this.portals = portals;
        this.goalAmenity = goalAmenity;
        this.distance = distance;
    }

    public List<Portal> getPortals() {
        return portals;
    }

    public Amenity getGoalAmenity() {
        return goalAmenity;
    }

    public double getDistance() {
        return distance;
    }
}

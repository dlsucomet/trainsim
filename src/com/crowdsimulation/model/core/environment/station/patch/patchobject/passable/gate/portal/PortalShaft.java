package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.NonObstacle;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Portal;

import java.util.List;

public abstract class PortalShaft extends NonObstacle {
    // Denotes the lower and upper portals of this shaft
    private Portal lowerPortal;
    private Portal upperPortal;

    // Denotes the time (s) needed to move the passengers from one end of the shaft to another
    private int moveTime;

    // Denotes the number of passengers this shaft can hold
    private int capacity;

    protected PortalShaft(List<AmenityBlock> amenityBlocks, boolean enabled, int moveTime, int capacity) {
        super(amenityBlocks, enabled);

        this.moveTime = moveTime;
        this.capacity = capacity;
    }

    public Portal getLowerPortal() {
        return lowerPortal;
    }

    public void setLowerPortal(Portal lowerPortal) {
        this.lowerPortal = lowerPortal;
    }

    public Portal getUpperPortal() {
        return upperPortal;
    }

    public void setUpperPortal(Portal upperPortal) {
        this.upperPortal = upperPortal;
    }

    public int getMoveTime() {
        return moveTime;
    }

    public void setMoveTime(int moveTime) {
        this.moveTime = moveTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Update the passengers' time spent in this portal
    public abstract void updateQueues();

    // Clear the portal shaft's queues
    public abstract List<Passenger> clearQueues();

    // Portal shaft factory
    public static abstract class PortalShaftFactory extends Gate.GateFactory {
    }
}

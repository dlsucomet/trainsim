package com.crowdsimulation.model.core.environment.station.patch.floorfield;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;

import java.util.*;

public class QueueObject extends AbstractFloorFieldObject {
    // Denotes the parent queueable of this queue object
    private final Queueable parent;

    // Denotes the patch where this queue object is
    private final Patch patch;

    // Any amenity that is queueable must contain a hashmap of floor fields
    // Given a passenger state, a floor field may be retrieved from that goal
    private final Map<QueueingFloorField.FloorFieldState, QueueingFloorField> floorFields = new HashMap<>();

    // Denotes the list of passengers who are queueing for this goal
    private final LinkedList<Passenger> passengersQueueing = new LinkedList<>();

    // Denotes the passenger at the back of the queue
    private Passenger lastPassengerQueueing;

    // Denotes the passenger currently being serviced by this queueable
    private Passenger passengerServiced;

    public QueueObject(Queueable parent, Patch patch) {
        this.parent = parent;
        this.patch = patch;
    }

    public Queueable getParent() {
        return parent;
    }

    public Patch getPatch() {
        return patch;
    }

    public Map<QueueingFloorField.FloorFieldState, QueueingFloorField> getFloorFields() {
        return floorFields;
    }

    public LinkedList<Passenger> getPassengersQueueing() {
        return passengersQueueing;
    }

    public Passenger getLastPassengerQueueing() {
        return lastPassengerQueueing;
    }

    public void setLastPassengerQueueing(Passenger lastPassengerQueueing) {
        this.lastPassengerQueueing = lastPassengerQueueing;
    }

    public Passenger getPassengerServiced() {
        return passengerServiced;
    }

    public void setPassengerServiced(Passenger passengerServiced) {
        this.passengerServiced = passengerServiced;
    }

    public boolean isFree() {
        return this.getPassengerServiced() == null;
    }

}

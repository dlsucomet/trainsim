package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator;

import com.crowdsimulation.controller.graphics.amenity.editor.EscalatorEditor;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.PortalShaft;

import java.util.ArrayList;
import java.util.List;

public class EscalatorShaft extends PortalShaft {
    // Denotes the direction of this escalator
    private EscalatorDirection escalatorDirection;

    // Denotes whether the direction of the escalator shaft has been changed
    private boolean hasChangedDirection;

    // Denotes the internal queues this escalator maintains
    private final List<List<Passenger>> queue;

    // Denotes the number of passengers using this escalator
    private int passengers;

    // Denotes the editor of this amenity
    public static final EscalatorEditor escalatorEditor;

    static {
        // Initialize the editor
        escalatorEditor = new EscalatorEditor();
    }

    protected EscalatorShaft(
            boolean enabled,
            int moveTime,
            EscalatorDirection escalatorDirection,
            int capacity
    ) {
        super(null, enabled, moveTime, capacity);

        this.escalatorDirection = escalatorDirection;
        this.hasChangedDirection = false;

        this.queue = new ArrayList<>();

        for (int index = 0; index < capacity; index++) {
            this.queue.add(new ArrayList<>());
        }

        this.passengers = 0;
    }

    public List<List<Passenger>> getQueue() {
        return queue;
    }

    public int getPassengers() {
        return passengers;
    }

    public boolean isQueueAtCapacity() {
        return this.passengers >= this.getCapacity();
    }

    public void incrementPassengers() {
        this.passengers++;
    }

    public void decrementPassengers() {
        this.passengers--;
    }

    @Override
    public void updateQueues() {
        List<Passenger> passengersToRemove = new ArrayList<>();

        if (this.escalatorDirection == EscalatorDirection.DOWN) {
            // For each passenger in the queue, move the passenger down a bucket, if that bucket is not filled,
            // Do this operation from bottom to top
            List<Passenger> passengersInBucket;

            for (int index = 0; index < this.queue.size(); index++) {
                passengersInBucket = this.queue.get(index);

                if (!passengersInBucket.isEmpty()) {
                    if (index == 0) {
                        // Remove the passengers at the bottom of the queue to spawn them into the new floor, if the
                        // pertinent spawn patch is empty
                        for (Passenger passenger : passengersInBucket) {
                            if (passenger.getPassengerMovement().exitPortal()) {
                                passengersToRemove.add(passenger);
                            }
                        }
                    } else {
                        // Only move down if the succeeding bucket is empty
                        if (this.queue.get(index - 1).isEmpty()) {
                            this.queue.get(index - 1).addAll(passengersInBucket);
                            this.queue.get(index).clear();
                        }
                    }

                    // Remove all those who've successfully exited
                    for (Passenger passengerToRemove : passengersToRemove) {
                        passengersInBucket.remove(passengerToRemove);
                        this.decrementPassengers();
                    }

                    passengersToRemove.clear();
                }
            }
        } else {
            // For each passenger in the queue, move the passenger up a bucket, if that bucket is not filled,
            // Do this operation from top to bottom
            for (int index = this.queue.size() - 1; index >= 0; index--) {
                List<Passenger> passengersInBucket = this.queue.get(index);

                if (!passengersInBucket.isEmpty()) {
                    if (index == this.queue.size() - 1) {
                        // Remove the passengers at the top of the queue to spawn them into the new floor, if the pertinent spawn
                        // patch is empty
                        for (Passenger passenger : passengersInBucket) {
                            if (passenger.getPassengerMovement().exitPortal()) {
                                passengersToRemove.add(passenger);
                            }
                        }
                    } else {
                        // Only move up if the succeeding bucket is empty
                        if (this.queue.get(index + 1).isEmpty()) {
                            this.queue.get(index + 1).addAll(passengersInBucket);
                            this.queue.get(index).clear();
                        }
                    }

                    // Remove all those who've successfully exited
                    for (Passenger passengerToRemove : passengersToRemove) {
                        passengersInBucket.remove(passengerToRemove);
                        this.decrementPassengers();
                    }

                    passengersToRemove.clear();
                }
            }
        }
    }

    @Override
    public List<Passenger> clearQueues() {
        List<Passenger> passengersToRemove = new ArrayList<>();

        for (List<Passenger> passengersInBucket : this.queue) {
            passengersToRemove.addAll(passengersInBucket);
            passengersInBucket.clear();
        }

        this.passengers = 0;

        return passengersToRemove;
    }

    // Escalator shaft factory
    public static class EscalatorShaftFactory extends PortalShaftFactory {
        public EscalatorShaft create(
                boolean enabled,
                int moveTime,
                EscalatorDirection escalatorDirection,
                int capacity
        ) {
            return new EscalatorShaft(
                    enabled,
                    moveTime,
                    escalatorDirection,
                    capacity
            );
        }
    }

    public EscalatorDirection getEscalatorDirection() {
        return escalatorDirection;
    }

    public void setEscalatorDirection(EscalatorDirection escalatorDirection) {
        this.escalatorDirection = escalatorDirection;
    }

    public boolean hasChangedDirection() {
        return hasChangedDirection;
    }

    public void setChangedDirection(boolean hasChangedDirection) {
        this.hasChangedDirection = hasChangedDirection;
    }

    // Denotes the next direction of the escalator
    public enum EscalatorDirection {
        UP("Going up"), // The escalator is about to move up
        DOWN("Going down"); // The escalator is about to move down

        private final String name;

        EscalatorDirection(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

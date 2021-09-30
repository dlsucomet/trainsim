package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs;

import com.crowdsimulation.controller.graphics.amenity.editor.StairEditor;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.PortalShaft;

import java.util.ArrayList;
import java.util.List;

public class StairShaft extends PortalShaft {
    // Denotes the internal queues this staircase maintains
    private final List<List<Passenger>> descendingQueue;
    private final List<List<Passenger>> ascendingQueue;

    // Denotes the number of passengers using this staircase in both directions
    private int passengersDescending;
    private int passengersAscending;

    // Denotes the editor of this amenity
    public static final StairEditor stairEditor;

    static {
        // Initialize the editor
        stairEditor = new StairEditor();
    }

    protected StairShaft(boolean enabled, int moveTime, int capacity) {
        super(null, enabled, moveTime, capacity);

        this.descendingQueue = new ArrayList<>();
        this.ascendingQueue = new ArrayList<>();

        for (int index = 0; index < capacity; index++) {
            this.descendingQueue.add(new ArrayList<>());
            this.ascendingQueue.add(new ArrayList<>());
        }

        this.passengersDescending = 0;
        this.passengersAscending = 0;
    }

    public List<List<Passenger>> getDescendingQueue() {
        return descendingQueue;
    }

    public List<List<Passenger>> getAscendingQueue() {
        return ascendingQueue;
    }

    public int getPassengersDescending() {
        return passengersDescending;
    }

    public int getPassengersAscending() {
        return passengersAscending;
    }

    public boolean isDescendingQueueAtCapacity() {
        return this.passengersDescending >= this.getCapacity();
    }

    public boolean isAscendingQueueAtCapacity() {
        return this.passengersAscending >= this.getCapacity();
    }

    public void incrementPassengersDescending() {
        this.passengersDescending++;
    }

    public void incrementPassengersAscending() {
        this.passengersAscending++;
    }

    public void decrementPassengersDescending() {
        this.passengersDescending--;
    }

    public void decrementPassengersAscending() {
        this.passengersAscending--;
    }

    @Override
    public void updateQueues() {
        List<Passenger> passengersToRemove = new ArrayList<>();

        // For each passenger in the descending queue, move the passenger down a bucket, if that bucket is not filled,
        // Do this operation from bottom to top
        for (int index = 0; index < this.descendingQueue.size(); index++) {
            List<Passenger> passengersInBucket = this.descendingQueue.get(index);

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
                    if (this.descendingQueue.get(index - 1).isEmpty()) {
                        this.descendingQueue.get(index - 1).addAll(passengersInBucket);
                        this.descendingQueue.get(index).clear();
                    }
                }

                // Remove all those who've successfully exited
                for (Passenger passengerToRemove : passengersToRemove) {
                    passengersInBucket.remove(passengerToRemove);
                    this.decrementPassengersDescending();
                }

                passengersToRemove.clear();
            }
        }

        // For each passenger in the ascending queue, move the passenger up a bucket, if that bucket is not filled,
        // Do this operation from top to bottom
        for (int index = this.ascendingQueue.size() - 1; index >= 0; index--) {
            List<Passenger> passengersInBucket = this.ascendingQueue.get(index);

            if (!passengersInBucket.isEmpty()) {
                if (index == this.ascendingQueue.size() - 1) {
                    // Remove the passengers at the top of the queue to spawn them into the new floor, if the pertinent spawn
                    // patch is empty
                    for (Passenger passenger : passengersInBucket) {
                        if (passenger.getPassengerMovement().exitPortal()) {
                            passengersToRemove.add(passenger);
                        }
                    }
                } else {
                    // Only move up if the succeeding bucket is empty
                    if (this.ascendingQueue.get(index + 1).isEmpty()) {
                        this.ascendingQueue.get(index + 1).addAll(passengersInBucket);
                        this.ascendingQueue.get(index).clear();
                    }
                }

                // Remove all those who've successfully exited
                for (Passenger passengerToRemove : passengersToRemove) {
                    passengersInBucket.remove(passengerToRemove);
                    this.decrementPassengersAscending();
                }

                passengersToRemove.clear();
            }
        }
    }

    @Override
    public List<Passenger> clearQueues() {
        List<Passenger> passengersToRemove = new ArrayList<>();

        for (List<Passenger> passengersInBucket : this.descendingQueue) {
            passengersToRemove.addAll(passengersInBucket);
            passengersInBucket.clear();
        }

        for (List<Passenger> passengersInBucket : this.ascendingQueue) {
            passengersToRemove.addAll(passengersInBucket);
            passengersInBucket.clear();
        }

        this.passengersDescending = 0;
        this.passengersAscending = 0;

        return passengersToRemove;
    }

    // Stair shaft factory
    public static class StairShaftFactory extends PortalShaftFactory {
        public StairShaft create(boolean enabled, int moveTime, int capacity) {
            return new StairShaft(
                    enabled,
                    moveTime,
                    capacity
            );
        }
    }
}

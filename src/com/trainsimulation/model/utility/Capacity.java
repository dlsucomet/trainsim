package com.trainsimulation.model.utility;

// Represents a limit on the number of passengers an infrastructure can contain
public class Capacity {
    private final int capacity;

    public Capacity(Capacity capacity) {
        this.capacity = capacity.getCapacity();
    }

    public Capacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}

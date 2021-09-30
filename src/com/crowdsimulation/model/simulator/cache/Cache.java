package com.crowdsimulation.model.simulator.cache;

import com.crowdsimulation.model.core.environment.Environment;

public abstract class Cache implements Environment {
    private final int capacity;

    public Cache(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}

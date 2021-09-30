package com.trainsimulation.model.core.environment.infrastructure.walkway;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.infrastructure.Infrastructure;

// Defines objects which directly support the movement of passengers
public abstract class Walkway extends Infrastructure {
    public Walkway(TrainSystem trainSystem) {
        super(trainSystem);
    }
}

package com.trainsimulation.model.core.environment.infrastructure;

import com.trainsimulation.model.core.environment.BaseEnvironment;
import com.trainsimulation.model.core.environment.TrainSystem;

// Static non-agents meant to support the train services and their passengers
public abstract class Infrastructure extends BaseEnvironment {
    public Infrastructure(TrainSystem trainSystem) {
        super(trainSystem);
    }
}

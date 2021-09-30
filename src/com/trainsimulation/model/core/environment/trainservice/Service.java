package com.trainsimulation.model.core.environment.trainservice;

import com.trainsimulation.model.core.environment.BaseEnvironment;
import com.trainsimulation.model.core.environment.TrainSystem;

public abstract class Service extends BaseEnvironment {
    public Service(TrainSystem trainSystem) {
        super(trainSystem);
    }
}

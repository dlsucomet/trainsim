package com.trainsimulation.model.core.environment;

// The base class of all objects associated to a train system
public abstract class BaseEnvironment implements Environment {
    // The train system associated with this environment
    private final TrainSystem trainSystem;

    public BaseEnvironment(TrainSystem trainSystem) {
        this.trainSystem = trainSystem;
    }

    public TrainSystem getTrainSystem() {
        return trainSystem;
    }
}

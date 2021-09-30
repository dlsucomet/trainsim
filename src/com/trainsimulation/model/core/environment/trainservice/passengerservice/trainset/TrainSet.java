package com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.PassengerService;

import java.util.Objects;

// A class of objects which are needed in trains
public abstract class TrainSet extends PassengerService {
    // Uniquely identifies this specific train set object
    protected short identifier;

    public TrainSet(TrainSystem trainSystem, short identifier) {
        super(trainSystem);

        this.identifier = identifier;
    }

    public short getIdentifier() {
        return identifier;
    }

    public void setIdentifier(short identifier) {
        this.identifier = identifier;
    }
}

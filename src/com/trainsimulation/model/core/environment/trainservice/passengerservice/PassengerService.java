package com.trainsimulation.model.core.environment.trainservice.passengerservice;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.Service;

public abstract class PassengerService extends Service {
    public PassengerService(TrainSystem trainSystem) {
        super(trainSystem);
    }
}

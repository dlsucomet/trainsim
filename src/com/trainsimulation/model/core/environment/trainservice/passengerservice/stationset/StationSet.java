package com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.PassengerService;

// A class of objects which are needed in train stations
public abstract class StationSet extends PassengerService {
    protected StationSet(TrainSystem trainSystem) {
        super(trainSystem);
    }
}

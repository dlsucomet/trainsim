package com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.passengerfacility;

import com.trainsimulation.model.core.agent.Agent;
import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.StationSet;

// Passenger facilities are amenities in the station which interact directly to the passengers
abstract class PassengerFacility extends StationSet implements Agent {
    PassengerFacility(TrainSystem trainSystem) {
        super(trainSystem);
    }
}

package com.trainsimulation.model.core.environment.trainservice.maintenance;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.Service;

// Defines an object which is related to the maintenance of trains (e.g., a depot)
abstract class Maintenance extends Service {
    Maintenance(TrainSystem trainSystem) {
        super(trainSystem);
    }
}

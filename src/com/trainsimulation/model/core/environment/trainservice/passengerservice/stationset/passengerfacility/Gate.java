package com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.passengerfacility;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.maintenance.Depot;
import com.trainsimulation.model.simulator.SimulationTime;
import com.trainsimulation.model.utility.Inflow;
import com.trainsimulation.model.utility.TrainSystemInformation;

// Gates are structures in a station which serve as a boundary between major spaces of a station; it also keeps track of
// the passengers which pass through them
public class Gate extends PassengerFacility {
    // Denotes the type of the gate
    private final GateType gateType;

    // Denotes how many passengers have passed through
    private Inflow inflowCount;

    // Denotes the time needed to wait (subject to stochasticity)
    private SimulationTime waitTime;

    public Gate(TrainSystem trainSystem, GateType gateType, Inflow inflowCount,
                SimulationTime waitTime) {
        super(trainSystem);

        this.gateType = gateType;
        this.inflowCount = inflowCount;
        this.waitTime = waitTime;
    }

    public GateType getGateType() {
        return gateType;
    }

    public Inflow getInflowCount() {
        return inflowCount;
    }

    public void setInflowCount(Inflow inflowCount) {
        this.inflowCount = inflowCount;
    }

    public SimulationTime getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(SimulationTime waitTime) {
        this.waitTime = waitTime;
    }

    // Allows a passenger to pass through the booth
    void allowPass() {
        // TODO: Implement passenger passing logic
    }

    @Override
    public void run() {

    }

    private enum GateType {
        SECURITY_ENTRANCE,
        TURNSTILE
    }
}

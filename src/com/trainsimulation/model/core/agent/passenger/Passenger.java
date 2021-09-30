package com.trainsimulation.model.core.agent.passenger;

import com.trainsimulation.model.core.agent.Agent;
import com.trainsimulation.model.simulator.SimulationTime;
import com.trainsimulation.model.utility.PassengerDemographic;
import com.trainsimulation.model.utility.PassengerLocationHistory;

// Passengers are the people who use the train system to go from an origin to a destination
public class Passenger implements Agent {
    // Denotes the demographics of this passenger
    private final PassengerDemographic passengerDemographic;

    // Denotes the time the passengers are alive (i.e., inside the system)
    private final SimulationTime timeAlive;

    // Denotes the location history of the passenger
    private final PassengerLocationHistory locationHistory;

    public Passenger(PassengerDemographic passengerDemographic, SimulationTime timeAlive,
                     PassengerLocationHistory locationHistory) {
        this.passengerDemographic = passengerDemographic;
        this.timeAlive = timeAlive;
        this.locationHistory = locationHistory;
    }

    public PassengerDemographic getPassengerDemographic() {
        return passengerDemographic;
    }

    public SimulationTime getTimeAlive() {
        return timeAlive;
    }

    public PassengerLocationHistory getLocationHistory() {
        return locationHistory;
    }

    // TODO: Implement train waiting logic
    public void waitForTrain() {

    }

    // TODO: Implement embarking logic
    public void embarkTrain() {

    }

    // TODO: Implement disembarking logic
    public void disembarkTrain() {

    }

    @Override
    public void run() {

    }
}

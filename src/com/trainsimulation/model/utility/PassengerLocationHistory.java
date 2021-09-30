package com.trainsimulation.model.utility;

import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;

// Represents the location history of a passenger
public class PassengerLocationHistory {
    // Denotes the origin station of the passenger
    private final Station originStation;

    // Denotes the destination station fo the passenger
    private final Station destinationStation;

    // Denotes the current station of the passenger
    private final Station currentStation;

    public PassengerLocationHistory(Station originStation, Station destinationStation, Station currentStation) {
        this.originStation = originStation;
        this.destinationStation = destinationStation;
        this.currentStation = currentStation;
    }

    public Station getOriginStation() {
        return originStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public Station getCurrentStation() {
        return currentStation;
    }
}

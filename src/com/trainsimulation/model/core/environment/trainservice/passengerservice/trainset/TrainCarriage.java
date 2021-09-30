package com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import com.trainsimulation.model.db.entity.TrainCarriagesEntity;
import com.trainsimulation.model.utility.Capacity;
import com.trainsimulation.model.utility.PassengerDemographic;
import com.trainsimulation.model.utility.TrainCarriageLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Vehicles which compose a complete train
public class TrainCarriage extends TrainSet {
    // Denotes the number of passengers the carriage can hold
    private final Capacity carriageCapacity;

    // Denotes the class name of the carriage
    private final String className;

    // Denotes the length of the train carriage
    private final double length;

    // Denotes the list of passenger demographics allowed on the train carriage
    private final List<PassengerDemographic> passengerWhitelist;

    // Denotes the list of passengers in this train
    private final HashMap<Station, List<Passenger>> passengers;

    // Denotes the list of passengers to alight
    private final List<Passenger> passengersToAlight;

    // Denotes the number of passengers in this carriage
    private int passengersInCarriage;

    // Denotes the load factor of this carriage
    private double loadFactor;

    // Denotes the parent of this train carriage
    private final Train parentTrain;

    // Denotes information about the location of this carriage
    private final TrainCarriageLocation trainCarriageLocation;

    public TrainCarriage(
            TrainSystem trainSystem,
            TrainCarriagesEntity trainCarriagesEntity,
            Train trainParent,
            boolean femalesOnly
    ) {
        super(trainSystem, trainCarriagesEntity.getId());

        this.carriageCapacity = new Capacity(trainCarriagesEntity.getCarriageClassesByCarriageClass().getCapacity());
        this.className = trainCarriagesEntity.getCarriageClassesByCarriageClass().getClassName();
        this.length = trainCarriagesEntity.getCarriageClassesByCarriageClass().getLength();
        this.parentTrain = trainParent;
        this.trainCarriageLocation = new TrainCarriageLocation();

        // TODO: No need for internal whitelist? Train door already restricts it
        this.passengerWhitelist = new ArrayList<>();

        this.passengers = new HashMap<>();
        this.passengersToAlight = new ArrayList<>();

        this.passengersInCarriage = 0;
        this.loadFactor = 0.0;

        for (Station station : trainSystem.getStations()) {
            this.passengers.put(station, new ArrayList<>());
        }
    }

    public Capacity getCarriageCapacity() {
        return carriageCapacity;
    }

    public String getClassName() {
        return className;
    }

    public double getLength() {
        return length;
    }

    public List<PassengerDemographic> getPassengerWhitelist() {
        return passengerWhitelist;
    }

    public HashMap<Station, List<Passenger>> getPassengers() {
        return passengers;
    }

    public int getPassengersInCarriage() {
        return passengersInCarriage;
    }

    public double getLoadFactor() {
        return loadFactor;
    }

    public Train getParentTrain() {
        return parentTrain;
    }

    public TrainCarriageLocation getTrainCarriageLocation() {
        return trainCarriageLocation;
    }

    // Add a passenger to this train carriage
    public void addPassenger(Passenger passenger) {
        this.passengers.get(passenger.getPassengerMovement().getRoutePlan().getDestinationStation()).add(
                passenger
        );

        // Increment the passenger count and the load factor
        this.passengersInCarriage++;
        this.loadFactor = (double) this.passengersInCarriage / (double) this.carriageCapacity.getCapacity();
    }

    // Retrieve the passengers that will alight at the given station while removing them from this carriage
    public void preparePassengersToAlight(Station station) {
        // Retrieve the passengers that will alight at the given station
        List<Passenger> passengersToAlight = new ArrayList<>(this.passengers.get(station));
        int passengersToAlightSize = passengersToAlight.size();

        // Remove all these passengers from the carriage passenger list
        this.passengers.get(station).clear();

        // Decrement the passenger counts and the load factor
        this.passengersInCarriage -= passengersToAlightSize;
        this.loadFactor = (double) this.passengersInCarriage / (double) this.carriageCapacity.getCapacity();

        // Set the list of passengers to alight
        this.passengersToAlight.addAll(passengersToAlight);
    }

    // Check if there are still passengers to alight
    public boolean hasPassengersToAlight() {
        return !this.passengersToAlight.isEmpty();
    }

    // Get the number of alighting passengers left
    public int passengersToAlightLeft() {
        return this.passengersToAlight.size();
    }

    // Remove one passenger to alight from this train carriage
    public Passenger removePassenger() {
        return this.passengersToAlight.remove(0);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TrainCarriage) {
            return this.identifier == ((TrainCarriage) object).identifier;
        } else {
            return false;
        }
    }
}

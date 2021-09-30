package com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.passengerfacility;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.trainservice.maintenance.Depot;
import com.trainsimulation.model.utility.TrainSystemInformation;

// Booths are where passengers can claim train tickets
public class Booth extends PassengerFacility {
    // Denotes the rate of ticket dispensing
    private final double dispensingRate;

    // Denotes the probability that it dispenses Beep cards
    private final double beepCardProbability;

    // Denotes the type of the booth
    private final BoothType boothType;

    public Booth(TrainSystem trainSystem, double dispensingRate, double beepCardProbability, BoothType boothType) {
        super(trainSystem);

        this.dispensingRate = dispensingRate;
        this.beepCardProbability = beepCardProbability;
        this.boothType = boothType;
    }

    public double getDispensingRate() {
        return dispensingRate;
    }

    public double getBeepCardProbability() {
        return beepCardProbability;
    }

    public BoothType getBoothType() {
        return boothType;
    }

    // Dispense a ticket
    public void dispenseTicket() {
        // TODO: Implement ticket dispensing logic
    }

    @Override
    public void run() {

    }

    private enum BoothType {
        VENDING_MACHINE,
        TICKET_BOOTH
    }
}

package com.crowdsimulation.model.core.agent.passenger.movement;

import com.crowdsimulation.model.simulator.Simulator;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class PassengerTripInformation {
    private final LocalTime turnstileTapInTime;
    private final String cardNumber;
    private final boolean isStoredValueHolder;
    private final Station entryStation;
    private final Station exitStation;
    private final PassengerMovement.TravelDirection travelDirection;
    private Duration travelTime;

    private final LocalTime approximateStationEntryTime;

    public PassengerTripInformation(
            LocalTime turnstileTapInTime,
            String cardNumber,
            boolean isStoredValueHolder,
            Station entryStation,
            Station exitStation,
            PassengerMovement.TravelDirection travelDirection,
            Duration travelTime
    ) {
        this.turnstileTapInTime = turnstileTapInTime;
        this.cardNumber = cardNumber;
        this.isStoredValueHolder = isStoredValueHolder;
        this.entryStation = entryStation;
        this.exitStation = exitStation;
        this.travelDirection = travelDirection;
        this.travelTime = travelTime;

        // From the entry time of the passenger in the empirical data (at the turnstile tap-in), approximate the
        // time the passenger actually enters the station
        this.approximateStationEntryTime = approximateStationEntryTime(turnstileTapInTime, isStoredValueHolder);
    }

    public LocalTime getTurnstileTapInTime() {
        return turnstileTapInTime;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public boolean isStoredValueHolder() {
        return isStoredValueHolder;
    }

    public Station getEntryStation() {
        return entryStation;
    }

    public Station getExitStation() {
        return exitStation;
    }

    public PassengerMovement.TravelDirection getTravelDirection() {
        return travelDirection;
    }

    public Duration getTravelTime() {
        return travelTime;
    }

    public LocalTime getApproximateStationEntryTime() {
        return approximateStationEntryTime;
    }

    private LocalTime approximateStationEntryTime(LocalTime turnstileTapInTime, boolean isStoredValueHolder) {
        // The time to be subtracted from the turnstile tap-in time primarily depends on whether the passenger is
        // a single journey, or a stored value ticket holder
        final double singleJourneyAverageTime = 42.7;
        final double singleJourneyStandardDeviation = 15.4;

        final double storedValueAverageTime = 8.6;
        final double storedValueStandardDeviation = 2.8;

        final int approximateWalkTimeBase = (isPeakHour(turnstileTapInTime) ? 90 : 60);
        final double approximateWalkTimeStandardDeviation = 1.0;

        double approximateWalkTime
                = approximateWalkTimeBase
                + Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * approximateWalkTimeStandardDeviation;

        if (!isStoredValueHolder) {
            double singleJourneyApproximateTime
                    = singleJourneyAverageTime
                    + Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * singleJourneyStandardDeviation
                    + approximateWalkTime;

            long singleJourneyApproximateTimeRounded = Math.round(singleJourneyApproximateTime);

            return turnstileTapInTime.minus(
                    singleJourneyApproximateTimeRounded, ChronoUnit.SECONDS
            );
        } else {
            double storedValueApproximateTime
                    = storedValueAverageTime
                    + Simulator.RANDOM_NUMBER_GENERATOR.nextGaussian() * storedValueStandardDeviation
                    + approximateWalkTime;

            long storedValueApproximateTimeRounded = Math.round(storedValueApproximateTime);

            return turnstileTapInTime.minus(
                    storedValueApproximateTimeRounded, ChronoUnit.SECONDS
            );
        }
    }

    // Check if the given time is within the peak hours
    public boolean isPeakHour(LocalTime time) {
        LocalTime morningPeakHourStart = LocalTime.of(7, 0);
        LocalTime morningPeakHourEnd = LocalTime.of(8, 0);

        LocalTime eveningPeakHourStart = LocalTime.of(17, 0);
        LocalTime eveningPeakHourEnd = LocalTime.of(18, 0);

        if (time.isAfter(morningPeakHourStart) && time.isBefore(morningPeakHourEnd)) {
            return true;
        }

        if (time.isAfter(eveningPeakHourStart) && time.isBefore(eveningPeakHourEnd)) {
            return true;
        }

        return false;
    }
}

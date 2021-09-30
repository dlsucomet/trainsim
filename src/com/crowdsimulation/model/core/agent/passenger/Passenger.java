package com.crowdsimulation.model.core.agent.passenger;

import com.crowdsimulation.controller.graphics.amenity.graphic.passenger.PassengerGraphic;
import com.crowdsimulation.model.core.agent.Agent;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerTripInformation;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.PatchObject;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.simulator.Simulator;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Passenger extends PatchObject implements Agent {
    // Keep track of the number of passengers generated
    public static AtomicInteger passengerCount = new AtomicInteger(0);

    // Contains the important information of this passenger
    private final PassengerInformation passengerInformation;

    // Contains the information about this passenger's trip
    private final PassengerTripInformation passengerTripInformation;

    // Contains the mechanisms for this passenger's movement
    private PassengerMovement passengerMovement;

    // Keeps track of the passenger's time in the simulation
    private final PassengerTime passengerTime;

    // Handles how this passenger is displayed
    private final PassengerGraphic passengerGraphic;

    // Factory for passenger creation
    public static final PassengerFactory passengerFactory;

    static {
        passengerFactory = new PassengerFactory();
    }

    // TODO: Passengers don't actually despawn until at the destination station, so the only disposition of the
    //  passenger will be boarding
    private Passenger(
            Patch spawnPatch,
            PassengerTripInformation passengerTripInformation,
            boolean queueOutside
    ) {
        this.passengerTripInformation = passengerTripInformation;

        if (passengerTripInformation == null) {
            PassengerInformation.Gender gender
                    = Simulator.RANDOM_NUMBER_GENERATOR.nextBoolean()
                    ? PassengerInformation.Gender.FEMALE : PassengerInformation.Gender.MALE;

            Demographic demographic = Demographic.generateDemographic();

            // Initialize card-related variables
            String cardNumber = null;

            final double singleJourneyPercentage = 0.5;

            TicketBooth.TicketType ticketType =
                    Simulator.RANDOM_NUMBER_GENERATOR.nextDouble() < singleJourneyPercentage
                            ? TicketBooth.TicketType.SINGLE_JOURNEY : TicketBooth.TicketType.STORED_VALUE;

            // The identifier of this passenger is its serial number (based on the number of passengers generated)
            int serialNumber = passengerCount.getAndIncrement();

            // Initialize this passenger's information
            this.passengerInformation = new PassengerInformation(
                    serialNumber,
                    cardNumber,
                    ticketType,
                    gender,
                    demographic,
                    null
            );

            // Initialize this passenger's timekeeping
            this.passengerTime = new PassengerTime(null);

            if (!queueOutside) {
                initializePassengerMovementWithoutPassengerTripInformation(spawnPatch, demographic);
            } else {
                this.passengerMovement = null;
            }
        } else {
            PassengerInformation.Gender gender = Simulator.RANDOM_NUMBER_GENERATOR.nextBoolean() ? PassengerInformation.Gender.FEMALE : PassengerInformation.Gender.MALE;
            Demographic demographic = Demographic.generateDemographic();

            // Initialize card-related variables
            String cardNumber = passengerTripInformation.getCardNumber();

            TicketBooth.TicketType ticketType
                    = passengerTripInformation.isStoredValueHolder()
                    ? TicketBooth.TicketType.STORED_VALUE : TicketBooth.TicketType.SINGLE_JOURNEY;

            // The identifier of this passenger is its serial number (based on the number of passengers generated)
            int serialNumber = passengerCount.getAndIncrement();

            // Initialize this passenger's information
            this.passengerInformation = new PassengerInformation(
                    serialNumber,
                    cardNumber,
                    ticketType,
                    gender,
                    demographic,
                    passengerTripInformation.getTurnstileTapInTime()
            );

            // Initialize this passenger's timekeeping
            this.passengerTime = new PassengerTime(passengerTripInformation.getApproximateStationEntryTime());

            if (!queueOutside) {
                initializePassengerMovementWithPassengerTripInformation(spawnPatch, passengerTripInformation, demographic);
            } else {
                this.passengerMovement = null;
            }
        }

        // Set the graphic object of this passenger
        this.passengerGraphic = new PassengerGraphic(this);
    }

    public void initializePassengerMovementWithoutPassengerTripInformation(
            Patch spawnPatch,
            Demographic demographic
    ) {
        boolean isBoarding = true;

        Gate gate = ((Gate) spawnPatch.getAmenityBlock().getParent());

        PassengerMovement.TravelDirection travelDirectionChosen = null;

        if (gate instanceof StationGate) {
            StationGate stationGate = ((StationGate) gate);

            // Get the pool of possible travel directions of the passengers to be spawned, depending on the settings of this
            // passenger gate
            // From this pool of travel directions, pick a random one
            int randomIndex
                    = Simulator.RANDOM_NUMBER_GENERATOR.nextInt(
                    stationGate.getStationGatePassengerTravelDirections().size()
            );

            travelDirectionChosen = stationGate.getStationGatePassengerTravelDirections().get(randomIndex);

            isBoarding = true;
        } else if (gate instanceof TrainDoor) {
            TrainDoor trainDoor = ((TrainDoor) gate);

            travelDirectionChosen = trainDoor.getPlatformDirection();

            isBoarding = false;
        }

        // Given the demographic, get this passenger's walking distance
        double baseWalkingDistance = Demographic.walkingSpeedsByAgeRange.get(demographic.getAgeRange());

        // Initialize all movement-related fields
        this.passengerMovement = new PassengerMovement(
                gate,
                this,
                baseWalkingDistance,
                spawnPatch.getPatchCenterCoordinates(),
                travelDirectionChosen,
                isBoarding
        );
    }

    public void initializePassengerMovementWithPassengerTripInformation(
            Patch spawnPatch,
            PassengerTripInformation passengerTripInformation,
            Demographic demographic
    ) {
        Gate gate = ((Gate) spawnPatch.getAmenityBlock().getParent());

        // Given the demographic, get this passenger's walking distance
        double baseWalkingDistance = Demographic.walkingSpeedsByAgeRange.get(demographic.getAgeRange());

        // Initialize all movement-related fields
        this.passengerMovement = new PassengerMovement(
                gate,
                this,
                baseWalkingDistance,
                spawnPatch.getPatchCenterCoordinates(),
                passengerTripInformation
        );
    }

    public PassengerInformation getPassengerInformation() {
        return passengerInformation;
    }

    public PassengerTripInformation getPassengerTripInformation() {
        return passengerTripInformation;
    }

    public PassengerInformation.Gender getGender() {
        return this.passengerInformation.gender;
    }

    public Demographic getDemographic() {
        return this.passengerInformation.getDemographic();
    }

    public String getCardNumber() {
        return this.passengerInformation.getCardNumber();
    }

    public TicketBooth.TicketType getTicketType() {
        return this.passengerInformation.ticketType;
    }

    public int getSerialNumber() {
        return this.passengerInformation.serialNumber;
    }

    public PassengerGraphic getPassengerGraphic() {
        return this.passengerGraphic;
    }

    public PassengerMovement getPassengerMovement() {
        return this.passengerMovement;
    }

    public PassengerTime getPassengerTime() {
        return passengerTime;
    }

    public static class PassengerFactory extends StationObjectFactory {
        public Passenger create(
                Patch spawnPatch,
                PassengerTripInformation passengerTripInformation,
                boolean queueOutside
        ) {
            Passenger newPassenger = new Passenger(spawnPatch, passengerTripInformation, queueOutside);

            if (passengerTripInformation != null) {
                passengerTripInformation.getEntryStation().getTrainSystem().getPassengers().add(newPassenger);

//                com.trainsimulation.model.simulator.Simulator.PASSENGERS_SPAWN.add(newPassenger);
            }

            return newPassenger;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return this.passengerInformation.identifier.equals(passenger.passengerInformation.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSerialNumber());
    }

    @Override
    public String toString() {
        return this.passengerInformation.identifier;
    }

    public static class PassengerInformation {
        // Denotes the unique identifier of this passenger trip
        private final String identifier;

        // Denotes the serial number of this passenger
        private final int serialNumber;

        // Denotes the card number of this passenger
        private final String cardNumber;

        // Denotes the ticket type of this passenger
        private final TicketBooth.TicketType ticketType;

        // Denotes the gender of this passenger
        private final Gender gender;

        // Denotes the demographic of this passenger
        private final Demographic demographic;

        // Denotes whether this passenger has chosen to ride in a female-only carriage
        private boolean hasChosenFemaleOnlyCarriage;

        public PassengerInformation(
                int serialNumber,
                String cardNumber,
                TicketBooth.TicketType ticketType,
                Gender gender,
                Demographic demographic,
                LocalTime originalTapInTime
        ) {
            if (originalTapInTime == null) {
                this.identifier = String.valueOf(serialNumber);
            } else {
                // The identifier is the original entry time plus the card number
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_TIME;

                this.identifier = originalTapInTime.format(dateTimeFormatter) + "=" + cardNumber;
            }

            this.serialNumber = serialNumber;
            this.cardNumber = cardNumber;
            this.ticketType = ticketType;
            this.gender = gender;
            this.demographic = demographic;

            this.hasChosenFemaleOnlyCarriage = false;
        }

        public String getIdentifier() {
            return identifier;
        }

        public int getSerialNumber() {
            return serialNumber;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public TicketBooth.TicketType getTicketType() {
            return ticketType;
        }

        public Gender getGender() {
            return gender;
        }

        public Demographic getDemographic() {
            return demographic;
        }

        public boolean hasChosenFemaleOnlyCarriage() {
            return hasChosenFemaleOnlyCarriage;
        }

        public void setChosenFemaleOnlyCarriage(boolean hasChosenFemaleOnlyCarriage) {
            this.hasChosenFemaleOnlyCarriage = hasChosenFemaleOnlyCarriage;
        }

        // Denotes the gender of this passenger
        public enum Gender {
            FEMALE,
            MALE
        }
    }

    // Contains the timekeeping aspects of the passenger
    public static class PassengerTime {
        // Denotes the ticks until this passenger has achieved the following milestones:
        //     (a) Passed security,
        //     (b) Tapped in to a turnstile,
        //     (c) Entered train,
        //     (d) Exited train,
        //     (e) Tapped out of a turnstile,
        //     (f) Exited the station (and despawned)
        private final LocalTime timeSpawned;

        private Duration passedEntrance;
        private Duration passedSecurity;
        private Duration tappedInTurnstile;
        private Duration enteredTrain;
        private Duration exitedTrain;
        private Duration tappedOutTurnstile;
        private Duration exitedStation;

        private Duration ticksAlive;

        private boolean hasPassedEntrance;
        private boolean hasPassedSecurity;
        private boolean hasTappedInTurnstile;
        private boolean hasEnteredTrain;
        private boolean hasExitedTrain;
        private boolean hasTappedOutTurnstile;
        private boolean hasExitedStation;

        public PassengerTime(LocalTime timeSpawned) {
            this.timeSpawned = timeSpawned;

            this.passedEntrance = Duration.ZERO;
            this.passedSecurity = Duration.ZERO;
            this.tappedInTurnstile = Duration.ZERO;
            this.enteredTrain = Duration.ZERO;
            this.exitedTrain = Duration.ZERO;
            this.tappedOutTurnstile = Duration.ZERO;
            this.exitedStation = Duration.ZERO;

            this.ticksAlive = Duration.ZERO;

            this.hasPassedEntrance = false;
            this.hasPassedSecurity = false;
            this.hasTappedInTurnstile = false;
            this.hasEnteredTrain = false;
            this.hasExitedTrain = false;
            this.hasTappedOutTurnstile = false;
            this.hasExitedStation = false;
        }

        public LocalTime getTimeSpawned() {
            return timeSpawned;
        }

        public Duration getPassedEntrance() {
            return passedEntrance;
        }

        public void setPassedEntrance(Duration passedEntrance) {
            this.passedEntrance = passedEntrance;
        }

        public Duration getPassedSecurity() {
            return passedSecurity;
        }

        public void setPassedSecurity(Duration passedSecurity) {
            this.passedSecurity = passedSecurity;
        }

        public Duration getTappedInTurnstile() {
            return tappedInTurnstile;
        }

        public void setTappedInTurnstile(Duration tappedInTurnstile) {
            this.tappedInTurnstile = tappedInTurnstile;
        }

        public Duration getEnteredTrain() {
            return enteredTrain;
        }

        public void setEnteredTrain(Duration enteredTrain) {
            this.enteredTrain = enteredTrain;
        }

        public Duration getExitedTrain() {
            return exitedTrain;
        }

        public void setExitedTrain(Duration exitedTrain) {
            this.exitedTrain = exitedTrain;
        }

        public Duration getTappedOutTurnstile() {
            return tappedOutTurnstile;
        }

        public void setTappedOutTurnstile(Duration tappedOutTurnstile) {
            this.tappedOutTurnstile = tappedOutTurnstile;
        }

        public Duration getExitedStation() {
            return exitedStation;
        }

        public void setExitedStation(Duration exitedStation) {
            this.exitedStation = exitedStation;
        }

        public Duration getTicksAlive() {
            return ticksAlive;
        }

        public void passEntrance() {
            this.hasPassedEntrance = true;
        }

        public void passSecurity() {
            this.hasPassedSecurity = true;
        }

        public void tapInTurnstile() {
            this.hasTappedInTurnstile = true;
        }

        public void enterTrain() {
            this.hasEnteredTrain = true;
        }

        public void exitTrain() {
            this.hasExitedTrain = true;
        }

        public void tapOutTurnstile() {
            this.hasTappedOutTurnstile = true;
        }

        public void exitStation() {
            this.hasExitedStation = true;
        }

        public long getTravelTime() {
            return this.enteredTrain.getSeconds()
                    + this.exitedTrain.getSeconds()
                    + this.tappedOutTurnstile.getSeconds();
        }

        // Update all relevant timekeeping variables
        public void tick() {
            // Tick interval variables
            if (!this.hasPassedEntrance) {
                this.passedEntrance = this.passedEntrance.plusSeconds(1);
            } else {
                if (!this.hasPassedSecurity) {
                    this.passedSecurity = this.passedSecurity.plusSeconds(1);
                } else {
                    if (!this.hasTappedInTurnstile) {
                        this.tappedInTurnstile = this.tappedInTurnstile.plusSeconds(1);
                    } else {
                        if (!this.hasEnteredTrain) {
                            this.enteredTrain = this.enteredTrain.plusSeconds(1);
                        } else {
                            if (!this.hasExitedTrain) {
                                this.exitedTrain = this.exitedTrain.plusSeconds(1);
                            } else {
                                if (!this.hasTappedOutTurnstile) {
                                    this.tappedOutTurnstile = this.tappedOutTurnstile.plusSeconds(1);
                                } else {
                                    if (!this.hasExitedStation) {
                                        this.exitedStation = this.exitedStation.plusSeconds(1);
                                    }
                                }
                            }
                        }
                    }
                }

                // TODO: Move outside when analysis is done
                // Tick the general counter
                this.ticksAlive = this.ticksAlive.plusSeconds(1);
            }
        }
    }
}

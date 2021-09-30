package com.crowdsimulation.model.core.agent.passenger.movement;

import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;

import java.util.*;

public class RoutePlan {
    // Contains the list of pattern plans
    public static final Map<PassengerMovement.Disposition, List<Class<? extends Amenity>>> DIRECTION_ROUTE_MAP;

    // Denotes the current route plan of the passenger which owns this
    private Iterator<Class<? extends Amenity>> currentRoutePlan;

    // Denotes the current class of the amenity in the route plan
    private Class<? extends Amenity> currentAmenityClass;

    // Denotes the origin and destination stations of this passenger, as well as its current station
    private com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station originStation;
    private com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station destinationStation;
    private com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station currentStation;

    static {
        // Prepare the structure that maps directions to the plans
        DIRECTION_ROUTE_MAP = new HashMap<>();

        // Prepare the plans
        final List<Class<? extends Amenity>> boardingPlanList = new ArrayList<>();

        boardingPlanList.add(StationGate.class);
        boardingPlanList.add(Security.class);
        boardingPlanList.add(TicketBooth.class);
        boardingPlanList.add(Turnstile.class);
        boardingPlanList.add(TrainDoor.class);

        final List<Class<? extends Amenity>> alightingPlanList = new ArrayList<>();

        alightingPlanList.add(TrainDoor.class);
        alightingPlanList.add(Turnstile.class);
        alightingPlanList.add(StationGate.class);

        DIRECTION_ROUTE_MAP.put(PassengerMovement.Disposition.BOARDING, boardingPlanList);
        DIRECTION_ROUTE_MAP.put(PassengerMovement.Disposition.RIDING_TRAIN, null);
        DIRECTION_ROUTE_MAP.put(PassengerMovement.Disposition.ALIGHTING, alightingPlanList);
    }

    public RoutePlan(
            boolean isStoredValueCardHolder,
            boolean isBoarding
    ) {
        initializeRoutePlan(isStoredValueCardHolder, isBoarding);
    }

    public RoutePlan(
            boolean isStoredValueCardHolder,
            boolean isBoarding,
            Station originStation,
            Station destinationStation,
            Station currentStation
    ) {
        initializeRoutePlan(isStoredValueCardHolder, isBoarding);

        this.originStation = originStation;
        this.destinationStation = destinationStation;
        this.currentStation = currentStation;
    }

    private void initializeRoutePlan(boolean isStoredValueCardHolder, boolean isBoarding) {
        // TODO: Passengers don't actually despawn until at the destination station, so the only disposition of the
        //  passenger will be boarding
        // All newly-spawned passengers will have a boarding route plan
        setNextRoutePlan(
                isBoarding ? PassengerMovement.Disposition.BOARDING : PassengerMovement.Disposition.ALIGHTING,
                isStoredValueCardHolder
        );

        // Burn off the first amenity class in the route plan, as the passenger will have already spawned there
//        setNextAmenityClass();
//        setNextAmenityClass();
    }

    // Set the next route plan
    public void setNextRoutePlan(PassengerMovement.Disposition disposition, boolean isStoredValueCardHolder) {
        // Passengers riding the train won't have route plans
        if (disposition != PassengerMovement.Disposition.RIDING_TRAIN) {
            List<Class<? extends Amenity>> routePlan = new ArrayList<>(DIRECTION_ROUTE_MAP.get(disposition));

            // If the passenger is a stored value card holder, remove the ticket booth from its route plan
            if (disposition == PassengerMovement.Disposition.BOARDING && isStoredValueCardHolder) {
                routePlan.remove(TicketBooth.class);
            }

            this.currentRoutePlan = routePlan.iterator();

            // Burn off the first amenity class in the route plan, as the passenger will have already spawned there
            setNextAmenityClass();
            setNextAmenityClass();
        } else {
            this.currentRoutePlan = null;
        }
    }

    // Set the next amenity class in the route plan
    public void setNextAmenityClass() {
        this.currentAmenityClass = this.currentRoutePlan.next();
    }

    public Iterator<Class<? extends Amenity>> getCurrentRoutePlan() {
        return currentRoutePlan;
    }

    public Class<? extends Amenity> getCurrentAmenityClass() {
        return currentAmenityClass;
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

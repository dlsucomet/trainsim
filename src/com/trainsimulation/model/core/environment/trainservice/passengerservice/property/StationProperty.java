package com.trainsimulation.model.core.environment.trainservice.passengerservice.property;

import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;
import javafx.beans.property.SimpleStringProperty;

public class StationProperty extends PassengerServiceProperty {
    private final SimpleStringProperty stationName = new SimpleStringProperty();

    public StationProperty(Station station) {
        super(station);

        this.stationName.set(station.getName());
    }

    public SimpleStringProperty stationNameProperty() {
        return this.stationName;
    }

    public String getStationName() {
        return stationName.get();
    }

    public void setStationName(String stationName) {
        this.stationName.set(stationName);
    }
}

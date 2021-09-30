package com.trainsimulation.model.core.environment.trainservice.passengerservice.property;

import com.trainsimulation.model.core.environment.trainservice.passengerservice.PassengerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

// Used to summarize the essential properties of a service
public class PassengerServiceProperty {
    // The service object summarized
    private final PassengerService passengerService;

    public PassengerServiceProperty(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    public static <T extends PassengerServiceProperty> ObservableList<T> toObservablePropertyList(
            List<T> propertyList) {
        ObservableList<T> objectsObservableList = FXCollections.observableArrayList();

        // For each object in the list, add to the observable list
        objectsObservableList.addAll(propertyList);

        return objectsObservableList;
    }

    public PassengerService getOwner() {
        return passengerService;
    }
}

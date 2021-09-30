package com.trainsimulation.model.core.environment.trainservice.passengerservice.property;

import com.trainsimulation.model.core.environment.infrastructure.track.Segment;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.Train;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.TrainCarriage;
import com.trainsimulation.model.utility.TrainMovement;
import javafx.beans.property.SimpleStringProperty;

// A summarized representation of this train class in terms of properties
public class TrainProperty extends PassengerServiceProperty {
    // Train property values
    private final SimpleStringProperty trainNumber = new SimpleStringProperty();
    private final SimpleStringProperty status = new SimpleStringProperty();
    private final SimpleStringProperty velocity = new SimpleStringProperty();
    private final SimpleStringProperty numberOfCarriages = new SimpleStringProperty();
    private final SimpleStringProperty carriageClassName = new SimpleStringProperty();
    private final SimpleStringProperty capacity = new SimpleStringProperty();
    private final SimpleStringProperty loadFactor = new SimpleStringProperty();
//        private final IntegerProperty passengers;

    public TrainProperty(Train train) {
        super(train);

        updateTrainProperty(train.getIdentifier(), train.getTrainMovement(),
                train.getTrainCarriages().getFirst());
    }

    public SimpleStringProperty trainNumberProperty() {
        return this.trainNumber;
    }

    public SimpleStringProperty statusProperty() {
        return this.status;
    }

    public SimpleStringProperty velocityProperty() {
        return this.velocity;
    }

    public SimpleStringProperty numberOfCarriagesProperty() {
        return this.numberOfCarriages;
    }

    public SimpleStringProperty carriageClassNameProperty() {
        return this.carriageClassName;
    }

    public SimpleStringProperty totalPassengerCapacity() {
        return this.capacity;
    }

    public String getTrainNumber() {
        return trainNumber.get();
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber.set(trainNumber);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getVelocity() {
        return velocity.get();
    }

    public void setVelocity(String velocity) {
        this.velocity.set(velocity);
    }

    public String getNumberOfCarriages() {
        return numberOfCarriages.get();
    }

    public void setNumberOfCarriages(String numberOfCarriages) {
        this.numberOfCarriages.set(numberOfCarriages);
    }

    public String getCarriageClassName() {
        return carriageClassName.get();
    }

    public void setCarriageClassName(String carriageClassName) {
        this.carriageClassName.set(carriageClassName);
    }

    public String getCapacity() {
        return capacity.get();
    }

    public void setCapacity(String capacity) {
        this.capacity.set(capacity);
    }

    public String getLoadFactor() {
        return loadFactor.get();
    }

    public void setLoadFactor(String loadFactor) {
        this.loadFactor.set(loadFactor);
    }

    public SimpleStringProperty loadFactorProperty() {
        return loadFactor;
    }

    // Update this train property
    public void updateTrainProperty(short identifier, TrainMovement trainMovement, TrainCarriage trainHead) {
        // Compute for the status of this train
        this.trainNumber.set(Integer.toString(identifier));
        this.status.set(this.setStatus(trainMovement));
        this.velocity.set(trainMovement.getVelocity() + " km/h");

        int numberOfCarriages = trainHead.getParentTrain().getTrainCarriages().size();

        this.numberOfCarriages.set(Integer.toString(numberOfCarriages));
        this.carriageClassName.set(trainHead.getClassName());
        this.capacity.set(Integer.toString(numberOfCarriages * trainHead.getCarriageCapacity()
                .getCapacity()));

        this.loadFactor.set(this.setLoadFactor(trainHead, numberOfCarriages));
    }

    // Set the status text to be displayed
    public String setStatus(TrainMovement trainMovement) {
        if (trainMovement.getTrain().getHead().getTrainCarriageLocation().getSegmentLocation() != null) {
            String segmentName = trainMovement.getTrain().getHead().getTrainCarriageLocation().getSegmentLocation()
                    .getName();
            String[] segmentNamedParsed = segmentName.split(Segment.SegmentIdentifier.DELIMITER);

            switch (segmentNamedParsed[0]) {
                case Segment.SegmentIdentifier.DEPOT:
                    if (segmentNamedParsed[1].equals(Segment.SegmentIdentifier.DEPOT_IN)) {
                        return "Exiting system";
                    } else {
                        return "Entering system";
                    }
                case Segment.SegmentIdentifier.LOOP:
                    return "Going " + segmentNamedParsed[1] + " (" + segmentNamedParsed[2].toLowerCase() + ")";
                case Segment.SegmentIdentifier.STATION:
                    return "At " + segmentNamedParsed[1] + " (" + segmentNamedParsed[2].toLowerCase() + ")";
                case Segment.SegmentIdentifier.MAINLINE:
                    return "From " + segmentNamedParsed[1] + " (" + segmentNamedParsed[2].toLowerCase() + ")";
                default:
                    // This shouldn't be reached
                    assert false : "Unknown segment identifier encountered: " + segmentNamedParsed[0];

                    return null;
            }
        } else {
            return "Preparing";
        }
    }

    // Set the load factor text to be displayed
    public String setLoadFactor(TrainCarriage trainHead, int numberOfCarriages) {
        int totalPassengers = 0;
        int totalCapacity = numberOfCarriages * trainHead.getCarriageCapacity().getCapacity();

        StringBuilder carriageLoadFactorsString = new StringBuilder("|");

        for (TrainCarriage trainCarriage : trainHead.getParentTrain().getTrainCarriages()) {
            totalPassengers += trainCarriage.getPassengersInCarriage();

            carriageLoadFactorsString.append(trainCarriage.getPassengersInCarriage()).append("|");
        }

//        String loadFactor =
//                totalPassengers + "/" + totalCapacity
//                        + " ("
//                        + String.format("%.3f", (double) totalPassengers / totalCapacity)
//                        + ") " + carriageLoadFactorsString;

        String loadFactor = String.format("%.3f", (double) totalPassengers / totalCapacity)
                + " (" + totalPassengers + "/" + totalCapacity + ") " + carriageLoadFactorsString;

        return loadFactor;
    }
}

package com.trainsimulation.model.utility;

import com.trainsimulation.model.core.environment.trainservice.passengerservice.trainset.TrainCarriage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// A custom-made synchronized linked list class specialized for handling a (pseudo-)queue of train carriages
public class TrainQueue {
    private final List<TrainCarriage> trainCarriages;

    public TrainQueue() {
        this.trainCarriages = Collections.synchronizedList(new LinkedList<>());
    }

    public synchronized TrainCarriage getTrainCarriage(int index) {
        return this.trainCarriages.get(index);
    }

    public synchronized boolean insertTrainCarriage(TrainCarriage trainCarriage) {
        return this.trainCarriages.add(trainCarriage);
    }

    public synchronized TrainCarriage removeFirstTrainCarriage() {
        return this.trainCarriages.remove(0);
    }

    public synchronized TrainCarriage getFirstTrainCarriage() {
        return this.getTrainCarriage(0);
    }

    public synchronized TrainCarriage getLastTrainCarriage() {
        return this.getTrainCarriage(this.trainCarriages.size() - 1);
    }

    public synchronized int getIndexOfTrainCarriage(TrainCarriage trainCarriage) {
        return this.trainCarriages.indexOf(trainCarriage);
    }

    public synchronized boolean isTrainQueueEmpty() {
        return this.trainCarriages.isEmpty();
    }

    public synchronized int getTrainQueueSize() {
        return this.trainCarriages.size();
    }

    public synchronized void clearTrainQueue() {
        this.trainCarriages.clear();
    }
}

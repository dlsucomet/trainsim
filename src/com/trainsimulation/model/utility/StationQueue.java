package com.trainsimulation.model.utility;

import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Station;

import java.util.ArrayList;
import java.util.List;

public class StationQueue {
    private final List<Station> originalStationList;
    private final List<Station> stationQueue;

    public StationQueue() {
        this.originalStationList = new ArrayList<>();
        this.stationQueue = new ArrayList<>();
    }

    public List<Station> getOriginalStationList() {
        return originalStationList;
    }

    public void setNewStations(List<Station> stationList, boolean isNearEnd) {
        this.originalStationList.clear();
        this.originalStationList.addAll(stationList);

        setStations(stationList, isNearEnd);
    }

    public void reverseStations(boolean isNearEnd) {
        setStations(this.originalStationList, isNearEnd);
    }

    private void setStations(List<Station> stationList, boolean isNearEnd) {
        // Clear the previous queue first
        this.stationQueue.clear();

        // Then get the elements from the given station list
        this.stationQueue.addAll(stationList);

        // Arrange the station list in order of increasing or decreasing distance from the depot
        if (isNearEnd) {
            this.stationQueue.sort(Station.StationByDepotSequenceDescending);
        } else {
            this.stationQueue.sort(Station.StationByDepotSequenceAscending);
        }
    }

    public Station pop() {
        return this.stationQueue.remove(0);
    }

    public Station peek() {
        return this.stationQueue.get(0);
    }

    public Station peekLast() {
        return this.stationQueue.get(this.stationQueue.size() - 1);
    }

    public boolean isEmpty() {
        return this.stationQueue.isEmpty();
    }
}

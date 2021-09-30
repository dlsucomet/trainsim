package com.trainsimulation.model.utility;

import com.trainsimulation.model.db.entity.StationCapacitiesEntity;

public class StationCapacity {
    // Denotes the maximum passenger inflow rate the station can handle per minute
    private final double inflowRateThreshold;

    // Denotes the maximum capacity of the station's concourse
    private final Capacity concourseCapacity;

    public StationCapacity(StationCapacity stationCapacity) {
        this.inflowRateThreshold = stationCapacity.getInflowRateThreshold();
        this.concourseCapacity = new Capacity(stationCapacity.getConcourseCapacity());
    }

    public StationCapacity(StationCapacitiesEntity stationCapacitiesEntity) {
        this.inflowRateThreshold = stationCapacitiesEntity.getInflowRateThreshold();
        this.concourseCapacity = new Capacity(stationCapacitiesEntity.getConcourseCapacity());
    }

    public double getInflowRateThreshold() {
        return inflowRateThreshold;
    }

    public Capacity getConcourseCapacity() {
        return concourseCapacity;
    }
}

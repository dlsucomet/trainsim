package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "station_capacities", schema = "main", catalog = "")
public class StationCapacitiesEntity {
    private short id;
    private double inflowRateThreshold;
    private short concourseCapacity;
    private Collection<StationsEntity> stationsById;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Basic
    @Column(name = "inflow_rate_threshold", nullable = false, precision = 0)
    public double getInflowRateThreshold() {
        return inflowRateThreshold;
    }

    public void setInflowRateThreshold(double inflowRateThreshold) {
        this.inflowRateThreshold = inflowRateThreshold;
    }

    @Basic
    @Column(name = "concourse_capacity", nullable = false)
    public short getConcourseCapacity() {
        return concourseCapacity;
    }

    public void setConcourseCapacity(short concourseCapacity) {
        this.concourseCapacity = concourseCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationCapacitiesEntity that = (StationCapacitiesEntity) o;
        return id == that.id &&
                Double.compare(that.inflowRateThreshold, inflowRateThreshold) == 0 &&
                concourseCapacity == that.concourseCapacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, inflowRateThreshold, concourseCapacity);
    }

    @OneToMany(mappedBy = "stationCapacitiesByStationCapacities")
    public Collection<StationsEntity> getStationsById() {
        return stationsById;
    }

    public void setStationsById(Collection<StationsEntity> stationsById) {
        this.stationsById = stationsById;
    }
}

package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "stations", schema = "main", catalog = "")
public class StationsEntity {
    private short id;
    private String name;
    private short sequence;
    private short distanceToPrevious;
    private short depotSequence;
    private Collection<PlatformsEntity> platformsById;
    private TrainSystemsEntity trainSystemsByTrainSystem;
    private SchedulesEntity schedulesByOperatingHours;
    private StationCapacitiesEntity stationCapacitiesByStationCapacities;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name", nullable = false, length = -1)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "sequence", nullable = false)
    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }

    @Basic
    @Column(name = "distance_to_previous", nullable = false)
    public short getDistanceToPrevious() {
        return distanceToPrevious;
    }

    public void setDistanceToPrevious(short distanceToPrevious) {
        this.distanceToPrevious = distanceToPrevious;
    }

    @Basic
    @Column(name = "depot_sequence", nullable = false)
    public short getDepotSequence() {
        return depotSequence;
    }

    public void setDepotSequence(short depotSequence) {
        this.depotSequence = depotSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationsEntity that = (StationsEntity) o;
        return id == that.id &&
                sequence == that.sequence &&
                distanceToPrevious == that.distanceToPrevious &&
                depotSequence == that.depotSequence &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sequence, distanceToPrevious, depotSequence);
    }

    @OneToMany(mappedBy = "stationsByStation")
    public Collection<PlatformsEntity> getPlatformsById() {
        return platformsById;
    }

    public void setPlatformsById(Collection<PlatformsEntity> platformsById) {
        this.platformsById = platformsById;
    }

    @ManyToOne
    @JoinColumn(name = "train_system", referencedColumnName = "id", nullable = false)
    public TrainSystemsEntity getTrainSystemsByTrainSystem() {
        return trainSystemsByTrainSystem;
    }

    public void setTrainSystemsByTrainSystem(TrainSystemsEntity trainSystemsByTrainSystem) {
        this.trainSystemsByTrainSystem = trainSystemsByTrainSystem;
    }

    @ManyToOne
    @JoinColumn(name = "operating_hours", referencedColumnName = "id", nullable = false)
    public SchedulesEntity getSchedulesByOperatingHours() {
        return schedulesByOperatingHours;
    }

    public void setSchedulesByOperatingHours(SchedulesEntity schedulesByOperatingHours) {
        this.schedulesByOperatingHours = schedulesByOperatingHours;
    }

    @ManyToOne
    @JoinColumn(name = "station_capacities", referencedColumnName = "id", nullable = false)
    public StationCapacitiesEntity getStationCapacitiesByStationCapacities() {
        return stationCapacitiesByStationCapacities;
    }

    public void setStationCapacitiesByStationCapacities(StationCapacitiesEntity stationCapacitiesByStationCapacities) {
        this.stationCapacitiesByStationCapacities = stationCapacitiesByStationCapacities;
    }
}

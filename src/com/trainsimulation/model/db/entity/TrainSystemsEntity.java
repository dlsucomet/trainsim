package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "train_systems", schema = "main", catalog = "")
public class TrainSystemsEntity {
    private short id;
    private String name;
    private String color;
    private Collection<DepotsEntity> depotsById;
    private Collection<EndSegmentsEntity> endSegmentsById;
    private Collection<StationsEntity> stationsById;
    private SchedulesEntity schedulesBySchedule;
    private Collection<TrainsEntity> trainsById;

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
    @Column(name = "color", nullable = false, length = -1)
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainSystemsEntity that = (TrainSystemsEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }

    @OneToMany(mappedBy = "trainSystemsByTrainSystem")
    public Collection<DepotsEntity> getDepotsById() {
        return depotsById;
    }

    public void setDepotsById(Collection<DepotsEntity> depotsById) {
        this.depotsById = depotsById;
    }

    @OneToMany(mappedBy = "trainSystemsByTrainSystem")
    public Collection<EndSegmentsEntity> getEndSegmentsById() {
        return endSegmentsById;
    }

    public void setEndSegmentsById(Collection<EndSegmentsEntity> endSegmentsById) {
        this.endSegmentsById = endSegmentsById;
    }

    @OneToMany(mappedBy = "trainSystemsByTrainSystem")
    public Collection<StationsEntity> getStationsById() {
        return stationsById;
    }

    public void setStationsById(Collection<StationsEntity> stationsById) {
        this.stationsById = stationsById;
    }

    @ManyToOne
    @JoinColumn(name = "schedule", referencedColumnName = "id", nullable = false)
    public SchedulesEntity getSchedulesBySchedule() {
        return schedulesBySchedule;
    }

    public void setSchedulesBySchedule(SchedulesEntity schedulesBySchedule) {
        this.schedulesBySchedule = schedulesBySchedule;
    }

    @OneToMany(mappedBy = "trainSystemsByTrainSystem")
    public Collection<TrainsEntity> getTrainsById() {
        return trainsById;
    }

    public void setTrainsById(Collection<TrainsEntity> trainsById) {
        this.trainsById = trainsById;
    }
}

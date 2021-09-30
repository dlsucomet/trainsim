package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "schedules", schema = "main", catalog = "")
public class SchedulesEntity {
    private short id;
    private short startHour;
    private short startMinute;
    private short endHour;
    private short endMinute;
    private Collection<CheckIntervalsEntity> checkIntervalsById;
    private Collection<PeakHoursEntity> peakHoursById;
    private Collection<StationsEntity> stationsById;
    private Collection<TrainSystemsEntity> trainSystemsById;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Basic
    @Column(name = "start_hour", nullable = false)
    public short getStartHour() {
        return startHour;
    }

    public void setStartHour(short startHour) {
        this.startHour = startHour;
    }

    @Basic
    @Column(name = "start_minute", nullable = false)
    public short getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(short startMinute) {
        this.startMinute = startMinute;
    }

    @Basic
    @Column(name = "end_hour", nullable = false)
    public short getEndHour() {
        return endHour;
    }

    public void setEndHour(short endHour) {
        this.endHour = endHour;
    }

    @Basic
    @Column(name = "end_minute", nullable = false)
    public short getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(short endMinute) {
        this.endMinute = endMinute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulesEntity that = (SchedulesEntity) o;
        return id == that.id &&
                startHour == that.startHour &&
                startMinute == that.startMinute &&
                endHour == that.endHour &&
                endMinute == that.endMinute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startHour, startMinute, endHour, endMinute);
    }

    @OneToMany(mappedBy = "schedulesBySchedule")
    public Collection<CheckIntervalsEntity> getCheckIntervalsById() {
        return checkIntervalsById;
    }

    public void setCheckIntervalsById(Collection<CheckIntervalsEntity> checkIntervalsById) {
        this.checkIntervalsById = checkIntervalsById;
    }

    @OneToMany(mappedBy = "schedulesBySchedule")
    public Collection<PeakHoursEntity> getPeakHoursById() {
        return peakHoursById;
    }

    public void setPeakHoursById(Collection<PeakHoursEntity> peakHoursById) {
        this.peakHoursById = peakHoursById;
    }

    @OneToMany(mappedBy = "schedulesByOperatingHours")
    public Collection<StationsEntity> getStationsById() {
        return stationsById;
    }

    public void setStationsById(Collection<StationsEntity> stationsById) {
        this.stationsById = stationsById;
    }

    @OneToMany(mappedBy = "schedulesBySchedule")
    public Collection<TrainSystemsEntity> getTrainSystemsById() {
        return trainSystemsById;
    }

    public void setTrainSystemsById(Collection<TrainSystemsEntity> trainSystemsById) {
        this.trainSystemsById = trainSystemsById;
    }
}

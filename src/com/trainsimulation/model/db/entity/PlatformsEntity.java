package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "platforms", schema = "main", catalog = "")
public class PlatformsEntity {
    private short id;
    private short operational;
    private short passable;
    private short platformCapacity;
    private short length;
    private Collection<CheckIntervalsEntity> checkIntervalsById;
    private Collection<PeakHoursEntity> peakHoursById;
    private StationsEntity stationsByStation;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Basic
    @Column(name = "operational", nullable = false)
    public short getOperational() {
        return operational;
    }

    public void setOperational(short operational) {
        this.operational = operational;
    }

    @Basic
    @Column(name = "passable", nullable = false)
    public short getPassable() {
        return passable;
    }

    public void setPassable(short passable) {
        this.passable = passable;
    }

    @Basic
    @Column(name = "platform_capacity", nullable = false)
    public short getPlatformCapacity() {
        return platformCapacity;
    }

    public void setPlatformCapacity(short platformCapacity) {
        this.platformCapacity = platformCapacity;
    }

    @Basic
    @Column(name = "length", nullable = false)
    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlatformsEntity that = (PlatformsEntity) o;
        return id == that.id &&
                operational == that.operational &&
                passable == that.passable &&
                platformCapacity == that.platformCapacity &&
                length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, operational, passable, platformCapacity, length);
    }

    @OneToMany(mappedBy = "platformsByPlatform")
    public Collection<CheckIntervalsEntity> getCheckIntervalsById() {
        return checkIntervalsById;
    }

    public void setCheckIntervalsById(Collection<CheckIntervalsEntity> checkIntervalsById) {
        this.checkIntervalsById = checkIntervalsById;
    }

    @OneToMany(mappedBy = "platformsByPlatform")
    public Collection<PeakHoursEntity> getPeakHoursById() {
        return peakHoursById;
    }

    public void setPeakHoursById(Collection<PeakHoursEntity> peakHoursById) {
        this.peakHoursById = peakHoursById;
    }

    @ManyToOne
    @JoinColumn(name = "station", referencedColumnName = "id", nullable = false)
    public StationsEntity getStationsByStation() {
        return stationsByStation;
    }

    public void setStationsByStation(StationsEntity stationsByStation) {
        this.stationsByStation = stationsByStation;
    }
}

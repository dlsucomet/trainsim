package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "peak_hours", schema = "main", catalog = "")
public class PeakHoursEntity {
    private short id;
    private PlatformsEntity platformsByPlatform;
    private SchedulesEntity schedulesBySchedule;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeakHoursEntity that = (PeakHoursEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @ManyToOne
    @JoinColumn(name = "platform", referencedColumnName = "id", nullable = false)
    public PlatformsEntity getPlatformsByPlatform() {
        return platformsByPlatform;
    }

    public void setPlatformsByPlatform(PlatformsEntity platformsByPlatform) {
        this.platformsByPlatform = platformsByPlatform;
    }

    @ManyToOne
    @JoinColumn(name = "schedule", referencedColumnName = "id", nullable = false)
    public SchedulesEntity getSchedulesBySchedule() {
        return schedulesBySchedule;
    }

    public void setSchedulesBySchedule(SchedulesEntity schedulesBySchedule) {
        this.schedulesBySchedule = schedulesBySchedule;
    }
}

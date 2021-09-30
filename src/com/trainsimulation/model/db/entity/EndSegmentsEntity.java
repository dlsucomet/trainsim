package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "end_segments", schema = "main", catalog = "")
public class EndSegmentsEntity {
    private short id;
    private short isNorthEnd;
    private short length;
    private TrainSystemsEntity trainSystemsByTrainSystem;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Basic
    @Column(name = "is_north_end", nullable = false)
    public short getIsNorthEnd() {
        return isNorthEnd;
    }

    public void setIsNorthEnd(short isNorthEnd) {
        this.isNorthEnd = isNorthEnd;
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
        EndSegmentsEntity that = (EndSegmentsEntity) o;
        return id == that.id &&
                isNorthEnd == that.isNorthEnd &&
                length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isNorthEnd, length);
    }

    @ManyToOne
    @JoinColumn(name = "train_system", referencedColumnName = "id", nullable = false)
    public TrainSystemsEntity getTrainSystemsByTrainSystem() {
        return trainSystemsByTrainSystem;
    }

    public void setTrainSystemsByTrainSystem(TrainSystemsEntity trainSystemsByTrainSystem) {
        this.trainSystemsByTrainSystem = trainSystemsByTrainSystem;
    }
}

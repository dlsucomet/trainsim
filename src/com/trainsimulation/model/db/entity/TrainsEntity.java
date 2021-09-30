package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "trains", schema = "main", catalog = "")
public class TrainsEntity {
    private short id;
    private Collection<TrainCarriagesEntity> trainCarriagesById;
    private TrainSystemsEntity trainSystemsByTrainSystem;

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
        TrainsEntity that = (TrainsEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @OneToMany(mappedBy = "trainsByTrain")
    public Collection<TrainCarriagesEntity> getTrainCarriagesById() {
        return trainCarriagesById;
    }

    public void setTrainCarriagesById(Collection<TrainCarriagesEntity> trainCarriagesById) {
        this.trainCarriagesById = trainCarriagesById;
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

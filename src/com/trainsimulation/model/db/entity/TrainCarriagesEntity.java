package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "train_carriages", schema = "main", catalog = "")
public class TrainCarriagesEntity {
    private short id;
    private short quantity;
    private TrainsEntity trainsByTrain;
    private CarriageClassesEntity carriageClassesByCarriageClass;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Basic
    @Column(name = "quantity", nullable = false)
    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainCarriagesEntity that = (TrainCarriagesEntity) o;
        return id == that.id &&
                quantity == that.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity);
    }

    @ManyToOne
    @JoinColumn(name = "train", referencedColumnName = "id", nullable = false)
    public TrainsEntity getTrainsByTrain() {
        return trainsByTrain;
    }

    public void setTrainsByTrain(TrainsEntity trainsByTrain) {
        this.trainsByTrain = trainsByTrain;
    }

    @ManyToOne
    @JoinColumn(name = "carriage_class", referencedColumnName = "id", nullable = false)
    public CarriageClassesEntity getCarriageClassesByCarriageClass() {
        return carriageClassesByCarriageClass;
    }

    public void setCarriageClassesByCarriageClass(CarriageClassesEntity carriageClassesByCarriageClass) {
        this.carriageClassesByCarriageClass = carriageClassesByCarriageClass;
    }
}

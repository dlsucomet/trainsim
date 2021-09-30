package com.trainsimulation.model.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "carriage_classes", schema = "main", catalog = "")
public class CarriageClassesEntity {
    private short id;
    private String className;
    private short capacity;
    private double length;
    private short maxVelocity;
    private short deceleration;
    private Collection<TrainCarriagesEntity> trainCarriagesById;

    @Id
    @Column(name = "id", nullable = false)
    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Basic
    @Column(name = "class_name", nullable = false, length = -1)
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Basic
    @Column(name = "capacity", nullable = false)
    public short getCapacity() {
        return capacity;
    }

    public void setCapacity(short capacity) {
        this.capacity = capacity;
    }

    @Basic
    @Column(name = "length", nullable = false, precision = 0)
    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    @Basic
    @Column(name = "max_velocity", nullable = false)
    public short getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(short maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    @Basic
    @Column(name = "deceleration", nullable = false)
    public short getDeceleration() {
        return deceleration;
    }

    public void setDeceleration(short deceleration) {
        this.deceleration = deceleration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarriageClassesEntity that = (CarriageClassesEntity) o;
        return id == that.id &&
                capacity == that.capacity &&
                Double.compare(that.length, length) == 0 &&
                maxVelocity == that.maxVelocity &&
                deceleration == that.deceleration &&
                Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, className, capacity, length, maxVelocity, deceleration);
    }

    @OneToMany(mappedBy = "carriageClassesByCarriageClass")
    public Collection<TrainCarriagesEntity> getTrainCarriagesById() {
        return trainCarriagesById;
    }

    public void setTrainCarriagesById(Collection<TrainCarriagesEntity> trainCarriagesById) {
        this.trainCarriagesById = trainCarriagesById;
    }
}

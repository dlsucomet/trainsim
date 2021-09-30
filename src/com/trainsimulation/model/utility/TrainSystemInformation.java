package com.trainsimulation.model.utility;

import com.trainsimulation.model.db.entity.TrainSystemsEntity;
import com.trainsimulation.model.simulator.SimulationTime;

import java.util.Objects;

// Used to identify which train system a simulation component is part of
public class TrainSystemInformation {
    // Denotes the base path where the files of the train systems are located
    public static final String BASE_PATH = "C:\\Users\\ERDT\\Desktop\\train-simulation\\train-systems";

    // Denotes the name of the train system
    private final String name;

    // Denotes the id version of the name of the train system
    private final String idName;

    // Denotes the assigned color of the train system (for visualization purposes)
    private final String color;

    // Denotes the schedule of operations for this train system
    private final Schedule schedule;

    // Denotes the file path where the files for this train system is found
    private final String trainSystemPath;

    public TrainSystemInformation(String trainSystem, String color, SimulationTime startTime, SimulationTime endTime) {
        this.name = trainSystem;
        this.idName = this.name.replace("\\-", "").toLowerCase();
        this.color = color;

        this.schedule = new Schedule(startTime, endTime);

        this.trainSystemPath = TrainSystemInformation.BASE_PATH + "\\" + this.name;
    }

    public TrainSystemInformation(TrainSystemsEntity trainSystemsEntity) {
        this.name = trainSystemsEntity.getName();
        this.idName = this.name.replace("\\-", "").toLowerCase();
        this.color = trainSystemsEntity.getColor();

        this.schedule = new Schedule(trainSystemsEntity.getSchedulesBySchedule());

        this.trainSystemPath = TrainSystemInformation.BASE_PATH + "\\" + this.name;
    }

    public String getName() {
        return name;
    }

    public String getIdName() {
        return idName;
    }

    public String getColor() {
        return color;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getTrainSystemPath() {
        return trainSystemPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrainSystemInformation that = (TrainSystemInformation) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

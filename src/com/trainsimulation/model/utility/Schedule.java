package com.trainsimulation.model.utility;

import com.trainsimulation.model.db.entity.SchedulesEntity;
import com.trainsimulation.model.simulator.SimulationTime;

// Defines a time range composed of a start time and an end time
public class Schedule {
    private final SimulationTime startTime;
    private final SimulationTime endTime;

    public Schedule(Schedule schedule) {
        this.startTime = new SimulationTime(schedule.getStartTime());
        this.endTime = new SimulationTime(schedule.getEndTime());
    }

    public Schedule(SimulationTime startTime, SimulationTime endTime) {
        // The start time should not be later than the end time
        assert startTime.getTime().compareTo(endTime.getTime()) < 0 : "The specified time is later than the end time";

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Schedule(SchedulesEntity schedulesEntity) {
        SimulationTime startTime = new SimulationTime(
                schedulesEntity.getStartHour(),
                schedulesEntity.getStartMinute(),
                0
        );

        SimulationTime endTime = new SimulationTime(
                schedulesEntity.getEndHour(),
                schedulesEntity.getEndMinute(),
                0
        );

        // The start time should not be later than the end time
        assert startTime.getTime().compareTo(endTime.getTime()) < 0 : "The specified time is later than the end time";

        this.startTime = startTime;
        this.endTime = endTime;
    }

    public SimulationTime getStartTime() {
        return startTime;
    }

    public SimulationTime getEndTime() {
        return endTime;
    }
}

package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator;

import com.crowdsimulation.controller.graphics.amenity.editor.ElevatorEditor;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.PortalShaft;

import java.util.List;

public class ElevatorShaft extends PortalShaft {
    // Denotes the time it takes between the elevator reaching its destination and the elevator having its doors fully
    // opened
    private int openDelayTime;

    // Denotes the time this portal's elevator doors are open to let passengers in and out
    private int doorOpenTime;

    // Denotes the floor the elevator is currently at
    private Floor currentFloor;

    // Denotes whether the elevator is boarding passengers or moving between floors
    private ElevatorState elevatorState;

    // Denotes the direction the elevator is going
    private ElevatorDirection elevatorDirection;

    // Denotes the editor of this amenity
    public static final ElevatorEditor elevatorEditor;

    static {
        // Initialize the editor
        elevatorEditor = new ElevatorEditor();
    }

    protected ElevatorShaft(
            boolean enabled,
            int moveTime,
            int openDelayTime,
            int doorOpenTime,
            ElevatorDirection initialElevatorDirection,
            int capacity
    ) {
        super(null, enabled, moveTime, capacity);

        this.openDelayTime = openDelayTime;
        this.doorOpenTime = doorOpenTime;
        this.elevatorState = ElevatorState.IDLE;
        this.elevatorDirection = initialElevatorDirection;
    }

    public int getOpenDelayTime() {
        return openDelayTime;
    }

    public int getDoorOpenTime() {
        return doorOpenTime;
    }

    public void setOpenDelayTime(int openDelayTime) {
        this.openDelayTime = openDelayTime;
    }

    public void setDoorOpenTime(int doorOpenTime) {
        this.doorOpenTime = doorOpenTime;
    }

    public Floor getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(Floor currentFloor) {
        this.currentFloor = currentFloor;
    }

    public ElevatorState getElevatorState() {
        return elevatorState;
    }

    public void setElevatorState(ElevatorState elevatorState) {
        this.elevatorState = elevatorState;
    }

    public ElevatorDirection getElevatorDirection() {
        return elevatorDirection;
    }

    public void setElevatorDirection(ElevatorDirection elevatorDirection) {
        this.elevatorDirection = elevatorDirection;
    }

    @Override
    public void updateQueues() {

    }

    @Override
    public List<Passenger> clearQueues() {
        return null;
    }

    // Elevator shaft factory
    public static class ElevatorShaftFactory extends PortalShaftFactory {
        public ElevatorShaft create(
                boolean enabled,
                int moveTime,
                int openDelayTime,
                int doorOpenTime,
                ElevatorDirection initialElevatorDirection,
                int capacity
        ) {
            return new ElevatorShaft(
                    enabled,
                    moveTime,
                    openDelayTime,
                    doorOpenTime,
                    initialElevatorDirection,
                    capacity
            );
        }
    }

    // Denotes the current state of the elevator
    public enum ElevatorState {
        BOARDING, // The elevator is open and allowing passengers to move in and out of its cab
        MOVING, // The elevator is closed and in transit from one floor to another
        IDLE // The elevator is closed, but is not moving as there are no passengers
    }

    // Denotes the next direction of the elevator
    public enum ElevatorDirection {
        UP("Bottom (to go up)"), // The elevator is about to move up
        DOWN("Top (to go down)"); // The elevator is about to move down

        private final String name;

        ElevatorDirection(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

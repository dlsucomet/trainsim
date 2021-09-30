package com.crowdsimulation.model.core.environment.station.patch.floorfield.headful;

import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;

import java.util.List;
import java.util.Objects;

public class PlatformFloorField extends QueueingFloorField {
    protected PlatformFloorField(Queueable target) {
        super(target);
    }

    // Factory for platform floor field creation
    public static PlatformFloorFieldFactory platformFloorFieldFactory;

    static {
        platformFloorFieldFactory = new PlatformFloorFieldFactory();
    }

    public static boolean registerPatch(
            Patch patch,
            TrainDoor target,
            PlatformFloorFieldState platformFloorFieldState,
            double value) {
        final double EPSILON = 1E-6;

        // Get the location of the train door entrance from the given platform floor field state
        TrainDoor.TrainDoorEntranceLocation trainDoorEntranceLocation
                = platformFloorFieldState.getTrainDoorEntranceLocation();

        // Then get the appropriate queue object corresponding to that location
        QueueObject queueObject = target.getQueueObjects().get(trainDoorEntranceLocation);

        PlatformFloorField platformFloorField
                = (PlatformFloorField) target.retrieveFloorField(queueObject, platformFloorFieldState);

        List<Patch> associatedPatches = platformFloorField.getAssociatedPatches();

        TrainDoor trainDoor = target;

        // If the floor field value is one, check if the number of apices in this floor field is already equal to the
        // number of attractors in the amenity
        // This is to make sure that there is only one apex in the floor field
        if (Math.abs(value - 1.0) < EPSILON) {
            // If it is, refuse to register the patch
            if (platformFloorField.getApices().size() == 1) {
                return false;
            } else {
                // If it hasn't yet, add the patch to the list of apices, if it isn't already in the list
                if (!platformFloorField.getApices().contains(patch)) {
                    platformFloorField.getApices().add(patch);
                }
            }
        }

        // Check if the floor field already contains the patch
        if (associatedPatches.contains(patch)) {
            // If it already does, just modify the value that's already there
            // Get the value in the patch present
            double valuePresent = patch.getFloorFieldValues().get(target).get(platformFloorFieldState);

            // If the present value is 1.0, and the value to replace it isn't 1.0, indicate that this patch doesn't have
            // an apex anymore as it was replaced with another value
            if (Math.abs(valuePresent - 1.0) < EPSILON && value < 1.0 - EPSILON) {
                platformFloorField.getApices().remove(patch);
            }
        } else {
            // If it doesn't contain the patch yet, add it
            associatedPatches.add(patch);
        }

        return true;
    }

    // Remove the patch from the floor fields kept tracked on by the target queueable
    public static void unregisterPatch(
            Patch patch,
            TrainDoor target,
            PlatformFloorFieldState platformFloorFieldState,
            double value
    ) {
        final double EPSILON = 1E-6;

        // Get the location of the train door entrance from the given platform floor field state
        TrainDoor.TrainDoorEntranceLocation trainDoorEntranceLocation
                = platformFloorFieldState.getTrainDoorEntranceLocation();

        // Then get the appropriate queue object corresponding to that location
        QueueObject queueObject = target.getQueueObjects().get(trainDoorEntranceLocation);

        PlatformFloorField platformFloorField
                = (PlatformFloorField) target.retrieveFloorField(queueObject, platformFloorFieldState);

        // Unregister the patch from this target
        platformFloorField.getAssociatedPatches().remove(patch);

        // If the value being removed is 1.0, this means this floor field won't have an apex anymore
        if (Math.abs(value - 1.0) < EPSILON) {
            platformFloorField.getApices().remove(patch);
        }
    }

    // A combination of a passenger's direction, state, and current target, this object is used for the differentiation
    // of floor fields
    public static class PlatformFloorFieldState extends FloorFieldState {
        private final TrainDoor.TrainDoorEntranceLocation trainDoorEntranceLocation;

        public PlatformFloorFieldState(
                PassengerMovement.Disposition disposition,
                PassengerMovement.State state,
                Queueable target,
                TrainDoor.TrainDoorEntranceLocation trainDoorEntranceLocation
        ) {
            super(disposition, state, target);

            this.trainDoorEntranceLocation = trainDoorEntranceLocation;
        }

        public TrainDoor.TrainDoorEntranceLocation getTrainDoorEntranceLocation() {
            return trainDoorEntranceLocation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            PlatformFloorFieldState that = (PlatformFloorFieldState) o;
            return trainDoorEntranceLocation == that.trainDoorEntranceLocation;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), trainDoorEntranceLocation);
        }
    }

    // Create floor fields
    public static class PlatformFloorFieldFactory extends QueueingFloorFieldFactory {
        public PlatformFloorField create(
                Queueable target
        ) {
            return new PlatformFloorField(
                    target
            );
        }
    }
}

package com.crowdsimulation.model.core.environment.station.patch.floorfield.headful;

import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.AbstractFloorFieldObject;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;

import java.util.*;

public class QueueingFloorField extends HeadfulFloorField {
    // Factory for queueing floor field creation
    public static QueueingFloorFieldFactory queueingFloorFieldFactory;

    static {
        queueingFloorFieldFactory = new QueueingFloorFieldFactory();
    }

    protected QueueingFloorField(Queueable target) {
        super(target);
    }

    // Adds an individual floor field to a patch and its associated floor field
    public static boolean addFloorFieldValue(
            Patch patch,
            Queueable target,
            FloorFieldState floorFieldState,
            double value) {
        // When adding a floor field value, these things have to happen:
        //   1) Register the patch where the floor field value is to be drawn to the target queueable's floor field
        //   2) Add the floor field value to the patch itself
        // In the target queueable, register the patch into the target's list of floor fields, if possible
        boolean success;

        if (target instanceof Turnstile) {
            success = TurnstileFloorField.registerPatch(patch, (Turnstile) target, floorFieldState, value);
        } else if (floorFieldState instanceof PlatformFloorField.PlatformFloorFieldState) {
            success = PlatformFloorField.registerPatch(
                    patch,
                    (TrainDoor) target,
                    (PlatformFloorField.PlatformFloorFieldState) floorFieldState,
                    value
            );
        } else {
            success = QueueingFloorField.registerPatch(patch, target, floorFieldState, value);
        }

        if (success) {
            // Add the floor field value to the patch itself
            // If the patch still doesn't have an entry for the target, add one and put the map there
            if (!patch.getFloorFieldValues().containsKey(target)) {
                // Prepare the floor field state and value map
                Map<FloorFieldState, Double> map = new HashMap<>();
                map.put(floorFieldState, value);

                // Put the target and the map into the patch
                patch.getFloorFieldValues().put(target, map);
            } else {
                // If it already has, just put the floor field state and the value there
                patch.getFloorFieldValues().get(target).put(floorFieldState, value);
            }

            // Patch registration successful, return true
            return true;
        } else {
            // If patch registration as unsuccessful, return false
            return false;
        }
    }

    // Add the patch to the floor fields kept tracked by the target queueable
    private static boolean registerPatch(
            Patch patch,
            Queueable target,
            FloorFieldState floorFieldState,
            double value) {
        final double EPSILON = 1E-6;

        QueueingFloorField queueingFloorField = target.retrieveFloorField(target.getQueueObject(), floorFieldState);
        List<Patch> associatedPatches = queueingFloorField.getAssociatedPatches();

        Amenity amenity = ((Amenity) target);

        // If the floor field value is one, check if the number of apices in this floor field is already equal to the
        // number of attractors in the amenity
        // This is to make sure that there is only one apex in the floor field
        if (Math.abs(value - 1.0) < EPSILON) {
            // If it is, refuse to register the patch
            if (queueingFloorField.getApices().size() == 1) {
                return false;
            } else {
                // If it hasn't yet, add the patch to the list of apices, if it isn't already in the list
                if (!queueingFloorField.getApices().contains(patch)) {
                    queueingFloorField.getApices().add(patch);
                }
            }
        }

        // Check if the floor field already contains the patch
        if (associatedPatches.contains(patch)) {
            // If it already does, just modify the value that's already there
            // Get the value in the patch present
            double valuePresent = patch.getFloorFieldValues().get(target).get(floorFieldState);

            // If the present value is 1.0, and the value to replace it isn't 1.0, indicate that this patch doesn't have
            // an apex anymore as it was replaced with another value
            if (Math.abs(valuePresent - 1.0) < EPSILON && value < 1.0 - EPSILON) {
                queueingFloorField.getApices().remove(patch);
            }
        } else {
            // If it doesn't contain the patch yet, add it
            associatedPatches.add(patch);
        }

        return true;
    }

    // Remove the patch from the floor fields kept tracked on by the target queueable
    private static void unregisterPatch(
            Patch patch,
            Queueable target,
            FloorFieldState floorFieldState,
            double value
    ) {
        final double EPSILON = 1E-6;

        QueueingFloorField queueingFloorField = target.retrieveFloorField(target.getQueueObject(), floorFieldState);

        // Unregister the patch from this target
        queueingFloorField.getAssociatedPatches().remove(patch);

        // If the value being removed is 1.0, this means this floor field won't have an apex anymore
        if (Math.abs(value - 1.0) < EPSILON) {
            queueingFloorField.getApices().remove(patch);
        }
    }

    // In a given patch, delete an individual floor field value in a floor field owned by a given target
    public static void deleteFloorFieldValue(
            Patch patch,
            Queueable target,
            FloorFieldState floorFieldState
    ) {
        // When deleting a floor field value, these things have to happen:
        //   1) Unregister the patch where the floor field value to be deleted is from the target queueable's floor
        //      field
        //   2) Remove the floor field value from the patch itself
        // Unregister the patch from the target's list of floor fields
        // Get the value of the floor field value to be removed as well
        Map<FloorFieldState, Double> map = patch.getFloorFieldValues().get(target);

        // Only perform deletion when there definitely is a floor field value in this patch
        // Else, do nothing
        if (map != null) {
            Double value = map.get(floorFieldState);

            // Make sure that a floor field value exists with the given floor field state
            if (value != null) {
                if (floorFieldState instanceof PlatformFloorField.PlatformFloorFieldState) {
                    PlatformFloorField.unregisterPatch(
                            patch,
                            (TrainDoor) target,
                            (PlatformFloorField.PlatformFloorFieldState) floorFieldState,
                            value
                    );
                } else {
                    if (target instanceof Turnstile) {
                        TurnstileFloorField.unregisterPatch(patch, (Turnstile) target, floorFieldState, value);
                    } else {
                        QueueingFloorField.unregisterPatch(patch, target, floorFieldState, value);
                    }
                }

                // In the given patch, remove the entry with the reference to the queueable target
                map.remove(floorFieldState);

                // If the previous deletion has left the floor field state and value map empty for that target queueable, delete
                // the map from the target
                if (map.isEmpty()) {
                    patch.getFloorFieldValues().remove(target);
                }
            }
        }
    }

    // Clear the given floor field
    public static void clearFloorField(
            QueueingFloorField queueingFloorField,
            FloorFieldState floorFieldState
    ) {
        // In each patch in the floor field to be deleted, delete the reference to its target
        // This deletes the value within that patch
        // Note that deletion should only be done when the patch contains a floor field value in the given floor field
        // state
        List<Patch> associatedPatches = queueingFloorField.getAssociatedPatches();
        Queueable target = queueingFloorField.getTarget();

        List<Patch> associatedPatchesCopy = new ArrayList<>(associatedPatches);

        for (Patch patch : associatedPatchesCopy) {
            QueueingFloorField.deleteFloorFieldValue(
                    patch,
                    target,
                    floorFieldState
            );
        }

        associatedPatchesCopy.clear();
    }

    // A combination of a passenger's direction, state, and current target, this object is used for the differentiation
    // of floor fields
    public static class FloorFieldState extends AbstractFloorFieldObject {
        private final DispositionStatePair dispositionStatePair;
        private final Queueable target;

        public FloorFieldState(
                PassengerMovement.Disposition disposition,
                PassengerMovement.State state,
                Queueable target) {
            this.dispositionStatePair = new DispositionStatePair(disposition, state);
            this.target = target;
        }

        public PassengerMovement.Disposition getDisposition() {
            return dispositionStatePair.getDisposition();
        }

        public PassengerMovement.State getState() {
            return dispositionStatePair.getState();
        }

        public Queueable getTarget() {
            return target;
        }

        @Override
        public String toString() {
            if (dispositionStatePair.getDisposition() != null) {
                return getDisposition().toString();
            } else {
                return "(any direction)";
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FloorFieldState that = (FloorFieldState) o;
            return Objects.equals(dispositionStatePair, that.dispositionStatePair) && target.equals(that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dispositionStatePair, target);
        }

        public static class DispositionStatePair implements Environment {
            private final PassengerMovement.Disposition disposition;
            private final PassengerMovement.State state;

            public DispositionStatePair(PassengerMovement.Disposition disposition, PassengerMovement.State state) {
                this.disposition = disposition;
                this.state = state;
            }

            public PassengerMovement.Disposition getDisposition() {
                return disposition;
            }

            public PassengerMovement.State getState() {
                return state;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                DispositionStatePair that = (DispositionStatePair) o;
                return disposition == that.disposition && state == that.state;
            }

            @Override
            public int hashCode() {
                return Objects.hash(disposition, state);
            }
        }
    }

    // Create floor fields
    public static class QueueingFloorFieldFactory extends HeadfulFloorFieldFactory {
        public QueueingFloorField create(
                Queueable target
        ) {
            return new QueueingFloorField(
                    target
            );
        }
    }
}

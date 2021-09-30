package com.crowdsimulation.model.core.environment.station.patch.floorfield.headful;

import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;

import java.util.List;

public class TurnstileFloorField extends QueueingFloorField {
    protected TurnstileFloorField(Queueable target) {
        super(target);
    }

    // Factory for platform floor field creation
    public static TurnstileFloorFieldFactory turnstileFloorFieldFactory;

    static {
        turnstileFloorFieldFactory = new TurnstileFloorFieldFactory();
    }

    public static boolean registerPatch(
            Patch patch,
            Turnstile target,
            FloorFieldState queueingFloorFieldState,
            double value) {
        final double EPSILON = 1E-6;

        // Get the current disposition
        PassengerMovement.Disposition disposition = queueingFloorFieldState.getDisposition();

        // Then get the appropriate queue object corresponding to that location
        QueueObject queueObject = target.getQueueObjects().get(disposition);

        TurnstileFloorField turnstileFloorField
                = (TurnstileFloorField) target.retrieveFloorField(queueObject, queueingFloorFieldState);

        List<Patch> associatedPatches = turnstileFloorField.getAssociatedPatches();

        Turnstile turnstile = target;

        // If the floor field value is one, check if the number of apices in this floor field is already equal to the
        // number of attractors in the amenity
        // This is to make sure that there is only one apex in the floor field
        if (Math.abs(value - 1.0) < EPSILON) {
            // If it is, refuse to register the patch
            if (turnstileFloorField.getApices().size() == 1) {
                return false;
            } else {
                // If it hasn't yet, add the patch to the list of apices, if it isn't already in the list
                if (!turnstileFloorField.getApices().contains(patch)) {
                    turnstileFloorField.getApices().add(patch);
                }
            }
        }

        // Check if the floor field already contains the patch
        if (associatedPatches.contains(patch)) {
            // If it already does, just modify the value that's already there
            // Get the value in the patch present
            double valuePresent = patch.getFloorFieldValues().get(target).get(queueingFloorFieldState);

            // If the present value is 1.0, and the value to replace it isn't 1.0, indicate that this patch doesn't have
            // an apex anymore as it was replaced with another value
            if (Math.abs(valuePresent - 1.0) < EPSILON && value < 1.0 - EPSILON) {
                turnstileFloorField.getApices().remove(patch);
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
            Turnstile target,
            FloorFieldState queueingFloorFieldState,
            double value
    ) {
        final double EPSILON = 1E-6;

        // Get the current disposition
        PassengerMovement.Disposition disposition = queueingFloorFieldState.getDisposition();

        // Then get the appropriate queue object corresponding to that location
        QueueObject queueObject = target.getQueueObjects().get(disposition);

        TurnstileFloorField turnstileFloorField = target.retrieveFloorField(queueObject, queueingFloorFieldState);

        // Unregister the patch from this target
        turnstileFloorField.getAssociatedPatches().remove(patch);

        // If the value being removed is 1.0, this means this floor field won't have an apex anymore
        if (Math.abs(value - 1.0) < EPSILON) {
            turnstileFloorField.getApices().remove(patch);
        }
    }

    // Create floor fields
    public static class TurnstileFloorFieldFactory extends QueueingFloorFieldFactory {
        public TurnstileFloorField create(
                Queueable target
        ) {
            return new TurnstileFloorField(
                    target
            );
        }
    }
}

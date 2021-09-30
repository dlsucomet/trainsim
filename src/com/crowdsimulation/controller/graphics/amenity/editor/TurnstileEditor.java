package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Turnstile;

import java.util.List;

public class TurnstileEditor extends AmenityEditor {
    public void draw(
            Patch currentPatch,
            boolean enabled,
            int waitingTIme,
            boolean blockEntry,
            Turnstile.TurnstileMode turnstileMode,
            List<PassengerMovement.TravelDirection> turnstileTravelDirections
    ) {
        List<Amenity.AmenityBlock> amenityBlocks
                = Amenity.AmenityBlock.convertToAmenityBlocks(
                currentPatch,
                GraphicsController.currentAmenityFootprint.getCurrentRotation()
                        .getAmenityBlockTemplates()
        );

        // If there are no amenity blocks to be formed from the footprint at all, do nothing
        if (amenityBlocks == null) {
            return;
        }

        // Only add amenities on patches which are empty and do not have floor field values on them
        // Check if, in each amenity block, there are no floor field values
        boolean patchesClear = true;

        for (Amenity.AmenityBlock amenityBlock : amenityBlocks) {
            if (
                    amenityBlock.getPatch().getAmenityBlock() != null
                            || !amenityBlock.getPatch().getFloorFieldValues().isEmpty()
            ) {
                patchesClear = false;

                break;
            }
        }

        // Otherwise, do nothing
        if (patchesClear) {
            // Prepare the amenity that will be placed on the station
            Turnstile turnstileToAdd = Turnstile.turnstileFactory.create(
                    amenityBlocks,
                    enabled,
                    waitingTIme,
                    blockEntry,
                    turnstileMode,
                    turnstileTravelDirections
            );

            // Add this station gate to the list of all turnstiles on this floor
            Main.simulator.getCurrentFloor().getTurnstiles().add(turnstileToAdd);

            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );
        }
    }

    public void edit(
            Turnstile turnstileToEdit,
            boolean enabled,
            int waitingTime,
            boolean blockEntry,
            Turnstile.TurnstileMode turnstileMode,
            List<PassengerMovement.TravelDirection> turnstileTravelDirections
    ) {
        turnstileToEdit.setEnabled(
                enabled
        );

        turnstileToEdit.setWaitingTime(
                waitingTime
        );

        turnstileToEdit.setBlockEntry(
                blockEntry
        );

        turnstileToEdit.setTurnstileMode(
                turnstileMode
        );

        turnstileToEdit.setTurnstileTravelDirections(
                turnstileTravelDirections
        );
    }

    public void delete(
            Turnstile turnstileToDelete
    ) {
        Main.simulator.getCurrentFloor().getTurnstiles().remove(
                turnstileToDelete
        );

        turnstileToDelete.getAmenityBlocks().forEach(amenityBlock -> {
            Floor amenityFloor = amenityBlock.getPatch().getFloor();
            Patch currentPatch = amenityBlock.getPatch();

            amenityFloor.getAmenityPatchSet().remove(currentPatch);
        });
    }
}

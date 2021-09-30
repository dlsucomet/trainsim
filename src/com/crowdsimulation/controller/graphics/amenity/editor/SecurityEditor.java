package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.blockable.Security;

import java.util.List;

public class SecurityEditor extends AmenityEditor {
    public void draw(
            Patch currentPatch,
            boolean enabled,
            int waitingTime,
            boolean blockPassenger
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
            Security securityToAdd = Security.securityFactory.create(
                    amenityBlocks,
                    enabled,
                    waitingTime,
                    blockPassenger
            );

            // Add this station gate to the list of all securities on this floor
            Main.simulator.getCurrentFloor().getSecurities().add(securityToAdd);

            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );
        }
    }

    public void edit(
            Security securityToEdit,
            boolean enabled,
            int waitingTime,
            boolean blockPassenger
    ) {
        securityToEdit.setEnabled(
                enabled
        );

        securityToEdit.setBlockEntry(
                blockPassenger
        );

        securityToEdit.setWaitingTime(
                waitingTime
        );
    }

    public void delete(
            Security securityToDelete
    ) {
        Main.simulator.getCurrentFloor().getSecurities().remove(
                securityToDelete
        );

        securityToDelete.getAmenityBlocks().forEach(amenityBlock -> {
            Floor amenityFloor = amenityBlock.getPatch().getFloor();
            Patch currentPatch = amenityBlock.getPatch();

            amenityFloor.getAmenityPatchSet().remove(currentPatch);
        });
    }
}

package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.goal.TicketBooth;

import java.util.List;

public class TicketBoothEditor extends AmenityEditor {
    public void draw(
            Patch currentPatch,
            boolean enabled,
            int waitingTime,
            TicketBooth.TicketType ticketType
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
            TicketBooth ticketBoothToAdd = TicketBooth.ticketBoothFactory.create(
                    amenityBlocks,
                    enabled,
                    waitingTime,
                    ticketType
            );

            // Add this station gate to the list of all ticket booths on this floor
            Main.simulator.getCurrentFloor().getTicketBooths().add(ticketBoothToAdd);

            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );
        }
    }

    public void edit(
            TicketBooth ticketBoothToEdit,
            boolean enabled,
            int waitingTime,
            TicketBooth.TicketType ticketType
    ) {
        ticketBoothToEdit.setEnabled(
                enabled
        );

        ticketBoothToEdit.setWaitingTime(
                waitingTime
        );

        ticketBoothToEdit.setTicketBoothType(
                ticketType
        );
    }

    public void delete(
            TicketBooth ticketBoothToDelete
    ) {
        Main.simulator.getCurrentFloor().getTicketBooths().remove(
                ticketBoothToDelete
        );

        ticketBoothToDelete.getAmenityBlocks().forEach(amenityBlock -> {
            Floor amenityFloor = amenityBlock.getPatch().getFloor();
            Patch currentPatch = amenityBlock.getPatch();

            amenityFloor.getAmenityPatchSet().remove(currentPatch);
        });
    }
}

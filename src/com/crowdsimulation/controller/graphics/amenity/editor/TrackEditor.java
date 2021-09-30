package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Track;

import java.util.List;

public class TrackEditor extends AmenityEditor {
    public void draw(
            Patch currentPatch,
            PassengerMovement.TravelDirection trackDirection
    ) {
        List<Amenity.AmenityBlock> amenityBlocks
                = Track.TrackBlock.convertToAmenityBlocks(
                currentPatch.getMatrixPosition().getRow(),
                Main.simulator.getCurrentFloor().getColumns(),
                GraphicsController.currentAmenityFootprint.getCurrentRotation().getAmenityBlockTemplates().get(1),
                GraphicsController.currentAmenityFootprint.getCurrentRotation().getAmenityBlockTemplates().get(0)
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
            Track trackToAdd = Track.trackFactory.create(
                    amenityBlocks,
                    trackDirection
            );

            // Add this track to the list of all tracks on this floor
            Main.simulator.getCurrentFloor().getTracks().add(trackToAdd);

            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );
        }
    }

    public void edit(
            Track trackToEdit,
            PassengerMovement.TravelDirection trackDirection
    ) {
        trackToEdit.setTrackDirection(trackDirection);
    }

    public void delete(
            Track trackToDelete
    ) {
        Main.simulator.getCurrentFloor().getTracks().remove(
                trackToDelete
        );

        trackToDelete.getAmenityBlocks().forEach(amenityBlock -> {
            Floor amenityFloor = amenityBlock.getPatch().getFloor();
            Patch currentPatch = amenityBlock.getPatch();

            amenityFloor.getAmenityPatchSet().remove(currentPatch);
        });
    }
}

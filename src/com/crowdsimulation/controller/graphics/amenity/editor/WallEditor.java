package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.impenetrable.Wall;

import java.util.List;

public class WallEditor extends AmenityEditor {
    public void draw(
            Patch currentPatch,
            Wall.WallType wallType
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
            Wall wallToAdd = Wall.wallFactory.create(
                    amenityBlocks,
                    wallType
            );

            // Add this wall to the list of all walls on this floor
            Main.simulator.getCurrentFloor().getWalls().add(wallToAdd);

            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );
        }
    }

    public void edit(
            Wall wallToEdit,
            Wall.WallType wallType
    ) {
        wallToEdit.setWallType(
                wallType
        );
    }

    public void delete(
            Wall wallToDelete
    ) {
        Main.simulator.getCurrentFloor().getWalls().remove(
                wallToDelete
        );

        wallToDelete.getAmenityBlocks().forEach(amenityBlock -> {
            Floor amenityFloor = amenityBlock.getPatch().getFloor();
            Patch currentPatch = amenityBlock.getPatch();

            amenityFloor.getAmenityPatchSet().remove(currentPatch);
        });
    }
}

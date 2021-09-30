package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairShaft;

import java.util.List;

public class StairEditor extends AmenityEditor {
    public StairShaft createShaft(
            boolean enabled,
            int moveTime,
            int capacity
    ) {
        // Prepare the provisional stair shaft
        // If the user chooses not to go through with the stair, this shaft will
        // simply be discarded
        StairShaft.StairShaftFactory stairShaftFactory =
                new StairShaft.StairShaftFactory();

        return stairShaftFactory.create(
                enabled,
                moveTime,
                capacity
        );
    }

    public StairPortal createPortal(
            Patch currentPatch,
            boolean enabled,
            Floor currentFloor,
            StairShaft stairShaft
    ) {
        List<Amenity.AmenityBlock> amenityBlocks
                = Gate.GateBlock.convertToGateBlocks(
                currentPatch,
                GraphicsController.currentAmenityFootprint.getCurrentRotation()
                        .getAmenityBlockTemplates()
        );

        // If there are no amenity blocks to be formed from the footprint at all, do nothing
        if (amenityBlocks == null) {
            return null;
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
            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );

            return StairPortal.stairPortalFactory.create(
                    amenityBlocks,
                    enabled,
                    currentFloor,
                    stairShaft
            );
        } else {
            return null;
        }
    }

    public void edit(
            StairShaft stairShaftToEdit,
            boolean enabled,
            int moveTime,
            int capacity
    ) {
        stairShaftToEdit.setEnabled(enabled);
        stairShaftToEdit.setMoveTime(moveTime);
        stairShaftToEdit.setCapacity(capacity);
    }

    public void delete(
            StairShaft stairShaftToDelete
    ) {
        // Retrieve portal components
        StairPortal upperStairPortal = (StairPortal) stairShaftToDelete.getUpperPortal();
        StairPortal lowerStairPortal = (StairPortal) stairShaftToDelete.getLowerPortal();

        // Remove the portals from their patches in their respective floors
        if (Main.simulator.getFirstPortal() == null) {
            // Portal drawing completed, deleting portal from portal shaft
            if (upperStairPortal != null) {
                for (Amenity.AmenityBlock amenityBlock : upperStairPortal.getAmenityBlocks()) {
                    amenityBlock.getPatch().setAmenityBlock(null);

                    amenityBlock.getPatch().getFloor().getAmenityPatchSet().remove(
                            amenityBlock.getPatch()
                    );
                }

                // Unregister the portal from its floor
                Floor floor = upperStairPortal.getFloorServed();
                Main.simulator.getStation().getStairPortalsByFloor().get(floor).remove(upperStairPortal);
            }

            if (lowerStairPortal != null) {
                for (Amenity.AmenityBlock amenityBlock : lowerStairPortal.getAmenityBlocks()) {
                    amenityBlock.getPatch().setAmenityBlock(null);

                    amenityBlock.getPatch().getFloor().getAmenityPatchSet().remove(
                            amenityBlock.getPatch()
                    );
                }

                // Unregister the portal from its floor
                Floor floor = lowerStairPortal.getFloorServed();
                Main.simulator.getStation().getStairPortalsByFloor().get(floor).remove(lowerStairPortal);
            }

            // Remove stair shaft
            Main.simulator.getStation().getStairShafts().remove(
                    stairShaftToDelete
            );
        } else {
            // Portal drawing uncompleted, deleting portal from simulator
            StairPortal portal = (StairPortal) Main.simulator.getFirstPortal();

            for (Amenity.AmenityBlock amenityBlock : portal.getAmenityBlocks()) {
                amenityBlock.getPatch().setAmenityBlock(null);

                amenityBlock.getPatch().getFloor().getAmenityPatchSet().remove(
                        amenityBlock.getPatch()
                );
            }
        }
    }
}

package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorShaft;

import java.util.List;

public class ElevatorEditor extends AmenityEditor {
    public ElevatorShaft createShaft(
            boolean enabled,
            int delayTime,
            int openTime,
            int moveTime,
            ElevatorShaft.ElevatorDirection elevatorDirection,
            int capacity
    ) {
        // Prepare the provisional elevator shaft
        // If the user chooses not to go through with the elevator, this shaft will
        // simply be discarded
        ElevatorShaft.ElevatorShaftFactory elevatorShaftFactory =
                new ElevatorShaft.ElevatorShaftFactory();

        return elevatorShaftFactory.create(
                enabled,
                moveTime,
                delayTime,
                openTime,
                elevatorDirection,
                capacity
        );
    }

    public ElevatorPortal createPortal(
            Patch currentPatch,
            boolean enabled,
            Floor currentFloor,
            ElevatorShaft elevatorShaft
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

            return ElevatorPortal.elevatorPortalFactory.create(
                    amenityBlocks,
                    enabled,
                    currentFloor,
                    elevatorShaft
            );
        } else {
            return null;
        }
    }

    public void edit(
            ElevatorShaft elevatorShaftToEdit,
            boolean enabled,
            int delayTime,
            int openTime,
            int moveTime,
            ElevatorShaft.ElevatorDirection elevatorDirection,
            int capacity
    ) {
        elevatorShaftToEdit.setEnabled(enabled);
        elevatorShaftToEdit.setOpenDelayTime(delayTime);
        elevatorShaftToEdit.setDoorOpenTime(openTime);
        elevatorShaftToEdit.setMoveTime(moveTime);
        elevatorShaftToEdit.setElevatorDirection(elevatorDirection);
        elevatorShaftToEdit.setCapacity(capacity);
    }

    public void delete(
            ElevatorShaft elevatorShaftToDelete
    ) {
        // Retrieve portal components
        ElevatorPortal upperElevatorPortal = (ElevatorPortal) elevatorShaftToDelete.getUpperPortal();
        ElevatorPortal lowerElevatorPortal = (ElevatorPortal) elevatorShaftToDelete.getLowerPortal();

        // Remove the portals from their patches in their respective floors
        if (Main.simulator.getFirstPortal() == null) {
            // Portal drawing completed, deleting portal from portal shaft
            if (upperElevatorPortal != null) {
                for (Amenity.AmenityBlock amenityBlock : upperElevatorPortal.getAmenityBlocks()) {
                    amenityBlock.getPatch().setAmenityBlock(null);

                    amenityBlock.getPatch().getFloor().getAmenityPatchSet().remove(
                            amenityBlock.getPatch()
                    );
                }

                // Unregister the portal from its floor
                Floor floor = upperElevatorPortal.getFloorServed();
                Main.simulator.getStation().getElevatorPortalsByFloor().get(floor).remove(upperElevatorPortal);
            }

            if (lowerElevatorPortal != null) {
                for (Amenity.AmenityBlock amenityBlock : lowerElevatorPortal.getAmenityBlocks()) {
                    amenityBlock.getPatch().setAmenityBlock(null);

                    amenityBlock.getPatch().getFloor().getAmenityPatchSet().remove(
                            amenityBlock.getPatch()
                    );
                }

                // Unregister the portal from its floor
                Floor floor = lowerElevatorPortal.getFloorServed();
                Main.simulator.getStation().getElevatorPortalsByFloor().get(floor).remove(lowerElevatorPortal);
            }

            // Remove elevator shaft
            Main.simulator.getStation().getElevatorShafts().remove(
                    elevatorShaftToDelete
            );
        } else {
            // Portal drawing uncompleted, deleting portal from simulator
            ElevatorPortal portal = (ElevatorPortal) Main.simulator.getFirstPortal();

            for (Amenity.AmenityBlock amenityBlock : portal.getAmenityBlocks()) {
                amenityBlock.getPatch().setAmenityBlock(null);

                amenityBlock.getPatch().getFloor().getAmenityPatchSet().remove(
                        amenityBlock.getPatch()
                );
            }
        }
    }
}

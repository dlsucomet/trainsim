package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;

import java.util.List;

public class TrainDoorEditor {
    public void draw(
            Patch currentPatch,
            boolean enabled,
            PassengerMovement.TravelDirection travelDirection,
            List<TrainDoor.TrainDoorCarriage> trainDoorCarriages,
            Station.StationOrientation stationOrientation,
            boolean isFemaleOnly
    ) {
        List<Amenity.AmenityBlock> amenityBlocks
                = Gate.GateBlock.convertToGateBlocks(
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
            TrainDoor trainDoorToAdd = TrainDoor.trainDoorFactory.create(
                    amenityBlocks,
                    enabled,
                    travelDirection,
                    trainDoorCarriages,
                    stationOrientation,
                    isFemaleOnly
            );

            // Add this station gate to the list of all train doors on this floor
            Main.simulator.getCurrentFloor().getTrainDoors().add(trainDoorToAdd);

            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );
        }
    }

    public void edit(
            TrainDoor trainDoorToEdit,
            boolean enabled,
            PassengerMovement.TravelDirection travelDirection,
            List<TrainDoor.TrainDoorCarriage> trainDoorCarriages,
            Station.StationOrientation stationOrientation,
            boolean isFemaleOnly
    ) {
        trainDoorToEdit.setEnabled(
                enabled
        );

        trainDoorToEdit.setPlatformDirection(
                travelDirection
        );

        trainDoorToEdit.setTrainDoorCarriagesSupported(
                trainDoorCarriages
        );

        trainDoorToEdit.setTrainDoorOrientation(
                stationOrientation

        );

        trainDoorToEdit.setFemaleOnly(
                isFemaleOnly
        );
    }

    public void delete(
            TrainDoor trainDoorToDelete
    ) {
        Main.simulator.getCurrentFloor().getTrainDoors().remove(
                trainDoorToDelete
        );

        trainDoorToDelete.getAmenityBlocks().forEach(amenityBlock -> {
            Floor amenityFloor = amenityBlock.getPatch().getFloor();
            Patch currentPatch = amenityBlock.getPatch();

            amenityFloor.getAmenityPatchSet().remove(currentPatch);
        });
    }
}

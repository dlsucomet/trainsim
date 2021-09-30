package com.crowdsimulation.controller.graphics.amenity.editor;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.GraphicsController;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.Gate;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.StationGate;

import java.util.List;

public class StationGateEditor extends AmenityEditor {
    public void draw(
            Patch currentPatch,
            boolean enabled,
            double chancePerSecond,
            StationGate.StationGateMode stationGateMode,
            List<PassengerMovement.TravelDirection> stationGatePassengerTravelDirections
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
            StationGate stationGateToAdd = StationGate.stationGateFactory.create(
                    amenityBlocks,
                    enabled,
                    chancePerSecond,
                    stationGateMode,
                    stationGatePassengerTravelDirections
            );

            // Add this station gate to the list of all station gates on this floor
            Main.simulator.getCurrentFloor().getStationGates().add(stationGateToAdd);

            amenityBlocks.forEach(
                    amenityBlock -> amenityBlock.getPatch().getFloor().getAmenityPatchSet().add(
                            amenityBlock.getPatch()
                    )
            );
        }
    }

    public void edit(
            StationGate stationGateToEdit,
            boolean stationGateEnabled,
            double chancePerSecond,
            StationGate.StationGateMode stationGateMode,
            List<PassengerMovement.TravelDirection> stationGateDirection
    ) {
        stationGateToEdit.setEnabled(
                stationGateEnabled
        );

        stationGateToEdit.setChancePerSecond(
                chancePerSecond
        );

        stationGateToEdit.setStationGateMode(
                stationGateMode
        );

        stationGateToEdit.setPassengerTravelDirectionsSpawned(
                stationGateDirection
        );
    }

    public void delete(
            StationGate stationGateToDelete
    ) {
        Main.simulator.getCurrentFloor().getStationGates().remove(
                stationGateToDelete
        );

        stationGateToDelete.getAmenityBlocks().forEach(amenityBlock -> {
            Floor amenityFloor = amenityBlock.getPatch().getFloor();
            Patch currentPatch = amenityBlock.getPatch();

            amenityFloor.getAmenityPatchSet().remove(currentPatch);
        });
    }
}

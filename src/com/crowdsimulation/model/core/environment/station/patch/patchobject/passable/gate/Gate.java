package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate;

import com.crowdsimulation.controller.Main;
import com.crowdsimulation.controller.graphics.amenity.footprint.AmenityFootprint;
import com.crowdsimulation.controller.graphics.amenity.footprint.GateFootprint;
import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Drawable;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.NonObstacle;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.elevator.ElevatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.escalator.EscalatorPortal;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.portal.stairs.StairPortal;
import com.crowdsimulation.model.core.environment.station.patch.position.MatrixPosition;

import java.util.ArrayList;
import java.util.List;

public abstract class Gate extends NonObstacle implements Drawable {
    // Denotes the spawners of this gate
    private final List<GateBlock> spawners;

    protected Gate(List<AmenityBlock> amenityBlocks, boolean enabled) {
        super(amenityBlocks, enabled);

        // Only proceed when this amenity has blocks
        if (this.getAmenityBlocks() != null) {
            this.spawners = new ArrayList<>();

            // Set all this amenity's spawners to the pertinent list
            for (AmenityBlock amenityBlock : this.getAmenityBlocks()) {
                GateBlock gateBlock = ((GateBlock) amenityBlock);

                if (gateBlock.isSpawner()) {
                    this.spawners.add(gateBlock);
                }
            }
        } else {
            this.spawners = null;
        }
    }

    public List<GateBlock> getSpawners() {
        return spawners;
    }

    // Spawn a passenger in this position
    public abstract Passenger spawnPassenger();

    // Despawn a passenger in this position
    public void despawnPassenger(Passenger passenger) {
        passenger.getPassengerMovement().despawn();
    }

    // Gate block
    public static abstract class GateBlock extends AmenityBlock {
        private final boolean spawner;

        public GateBlock(Patch patch, boolean attractor, boolean spawner, boolean hasGraphic) {
            super(patch, attractor, hasGraphic);

            this.spawner = spawner;
        }

        public boolean isSpawner() {
            return spawner;
        }

        // Convert the list of amenity block templates to a list of amenity blocks
        public static List<AmenityBlock> convertToGateBlocks(
                Patch referencePatch,
                List<AmenityFootprint.Rotation.AmenityBlockTemplate> amenityBlockTemplates
        ) {
            List<AmenityBlock> gateBlocks = new ArrayList<>();

            // Convert each template into an actual gate block
            for (AmenityFootprint.Rotation.AmenityBlockTemplate amenityBlockTemplate : amenityBlockTemplates) {
                GateFootprint.GateRotation.GateBlockTemplate gateBlockTemplate
                        = ((GateFootprint.GateRotation.GateBlockTemplate) amenityBlockTemplate);

                // Compute for the position of the patch using the offset data
                int row
                        = referencePatch.getMatrixPosition().getRow()
                        + gateBlockTemplate.getOffset().getRowOffset();

                int column
                        = referencePatch.getMatrixPosition().getColumn()
                        + gateBlockTemplate.getOffset().getColumnOffset();

                MatrixPosition patchPosition = new MatrixPosition(
                        row,
                        column
                );

                // If the position is out of bounds, return null
                if (!MatrixPosition.inBounds(
                        patchPosition,
                        Main.simulator.getStation()
                )) {
                    return null;
                }

                // Create the amenity block, then add it to the list
                Patch patch = Main.simulator.getCurrentFloor().getPatch(row, column);

                assert getGateBlockFactory(gateBlockTemplate.getAmenityClass()) != null;

                GateBlockFactory gateBlockFactory
                        = getGateBlockFactory(gateBlockTemplate.getAmenityClass());

                GateBlock gateBlock = gateBlockFactory.create(
                        patch,
                        gateBlockTemplate.isAttractor(),
                        gateBlockTemplate.isSpawner(),
                        gateBlockTemplate.hasGraphic(),
                        gateBlockTemplate.getOrientation()
                );

                gateBlocks.add(gateBlock);
            }

            return gateBlocks;
        }

        private static GateBlockFactory getGateBlockFactory(Class<? extends Amenity> amenityClass) {
            if (amenityClass == StationGate.class) {
                return StationGate.StationGateBlock.stationGateBlockFactory;
            } else if (amenityClass == TrainDoor.class) {
                return TrainDoor.TrainDoorBlock.trainDoorBlockFactory;
            } else if (amenityClass == StairPortal.class) {
                return StairPortal.StairPortalBlock.stairPortalBlockFactory;
            } else if (amenityClass == EscalatorPortal.class) {
                return EscalatorPortal.EscalatorPortalBlock.escalatorPortalBlockFactory;
            } else if (amenityClass == ElevatorPortal.class) {
                return ElevatorPortal.ElevatorPortalBlock.elevatorPortalBlockFactory;
            } else {
                return null;
            }
        }

        // Train door block factory
        public static abstract class GateBlockFactory extends AmenityBlockFactory {
            public abstract GateBlock create(
                    Patch patch,
                    boolean attractor,
                    boolean spawner,
                    boolean hasGraphic,
                    AmenityFootprint.Rotation.Orientation... orientation
            );
        }
    }

    // Gate factory
    public static abstract class GateFactory extends NonObstacleFactory {
    }
}

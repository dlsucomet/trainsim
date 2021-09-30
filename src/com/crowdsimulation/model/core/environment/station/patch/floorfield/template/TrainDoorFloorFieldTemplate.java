package com.crowdsimulation.model.core.environment.station.patch.floorfield.template;

import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.PlatformFloorField;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate.TrainDoor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainDoorFloorFieldTemplate extends QueueableFloorFieldTemplate {
    private final HashMap<
            TrainDoor.TrainDoorEntranceLocation,
            HashMap<
                    PlatformFloorField.FloorFieldState.DispositionStatePair,
                    QueueingFloorFieldTemplate
                    >
            > floorFieldsTemplate;

    public TrainDoorFloorFieldTemplate(HashMap<TrainDoor.TrainDoorEntranceLocation, QueueObject> queueObjects) {
        this.floorFieldsTemplate = new HashMap<>();

        for (
                Map.Entry<TrainDoor.TrainDoorEntranceLocation, QueueObject> queueObjectEntry : queueObjects.entrySet()
        ) {
            TrainDoor.TrainDoorEntranceLocation trainDoorEntranceLocation = queueObjectEntry.getKey();
            QueueObject queueObject = queueObjectEntry.getValue();

            HashMap<
                    PlatformFloorField.PlatformFloorFieldState.DispositionStatePair,
                    QueueingFloorFieldTemplate
                    > queueingFloorFieldTemplateHashMap = new HashMap<>();

            for (
                    Map.Entry<QueueingFloorField.FloorFieldState, QueueingFloorField>
                            floorFieldStateQueueingFloorFieldEntry
                    : queueObject.getFloorFields().entrySet()
            ) {
                PlatformFloorField.PlatformFloorFieldState PlatformFloorFieldState = (PlatformFloorField.PlatformFloorFieldState) floorFieldStateQueueingFloorFieldEntry.getKey();
                PlatformFloorField PlatformFloorField
                        = (com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.PlatformFloorField) floorFieldStateQueueingFloorFieldEntry.getValue();

                // Get the disposition-state pair
                PlatformFloorField.PlatformFloorFieldState.DispositionStatePair dispositionStatePair
                        = new PlatformFloorField.PlatformFloorFieldState.DispositionStatePair(
                        PlatformFloorFieldState.getDisposition(),
                        PlatformFloorFieldState.getState()
                );

                // Get the reference patch
                Patch referencePatch = queueObject.getPatch();

                // Compute for the offsets of the apices
                List<Patch.Offset> apicesOffsets = new ArrayList<>();

                for (Patch apexPatch : PlatformFloorField.getApices()) {
                    apicesOffsets.add(
                            Patch.Offset.getOffsetFromPatch(apexPatch, referencePatch)
                    );
                }

                // Compute for the offsets of the associated patches
                List<QueueingFloorFieldTemplate.PatchOffsetFloorFieldValuePair> associatedPatchesOffsets
                        = new ArrayList<>();

                for (Patch associatedPatch : PlatformFloorField.getAssociatedPatches()) {
                    Patch.Offset offset = Patch.Offset.getOffsetFromPatch(associatedPatch, referencePatch);
                    double floorFieldValue
                            = associatedPatch.getFloorFieldValues().get(queueObject.getParent()).get(PlatformFloorFieldState);

                    QueueingFloorFieldTemplate.PatchOffsetFloorFieldValuePair patchOffsetFloorFieldValuePair
                            = new QueueingFloorFieldTemplate.PatchOffsetFloorFieldValuePair(offset, floorFieldValue);

                    associatedPatchesOffsets.add(patchOffsetFloorFieldValuePair);
                }

                // Create the queueing floor field template
                QueueingFloorFieldTemplate queueingFloorFieldTemplate
                        = new QueueingFloorFieldTemplate(
                        apicesOffsets,
                        associatedPatchesOffsets
                );

                // Insert it into the map
                queueingFloorFieldTemplateHashMap.put(dispositionStatePair, queueingFloorFieldTemplate);
            }

            // Insert it into the map
            floorFieldsTemplate.put(trainDoorEntranceLocation, queueingFloorFieldTemplateHashMap);
        }
    }

    public HashMap<
            TrainDoor.TrainDoorEntranceLocation,
            HashMap<
                    PlatformFloorField.PlatformFloorFieldState.DispositionStatePair,
                    QueueingFloorFieldTemplate
                    >
            > getFloorFieldsTemplate() {
        return floorFieldsTemplate;
    }
}

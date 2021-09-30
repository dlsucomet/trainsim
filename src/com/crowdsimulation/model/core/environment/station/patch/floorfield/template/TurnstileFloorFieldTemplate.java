package com.crowdsimulation.model.core.environment.station.patch.floorfield.template;

import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.QueueObject;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.headful.QueueingFloorField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurnstileFloorFieldTemplate extends QueueableFloorFieldTemplate {
    private final HashMap<
            PassengerMovement.Disposition,
            HashMap<
                    QueueingFloorField.FloorFieldState.DispositionStatePair,
                    QueueingFloorFieldTemplate
                    >
            > floorFieldsTemplate;

    public TurnstileFloorFieldTemplate(HashMap<PassengerMovement.Disposition, QueueObject> queueObjects) {
        this.floorFieldsTemplate = new HashMap<>();

        for (
                Map.Entry<PassengerMovement.Disposition, QueueObject> queueObjectEntry : queueObjects.entrySet()
        ) {
            PassengerMovement.Disposition disposition = queueObjectEntry.getKey();
            QueueObject queueObject = queueObjectEntry.getValue();

            HashMap<
                    QueueingFloorField.FloorFieldState.DispositionStatePair,
                    QueueingFloorFieldTemplate
                    > queueingFloorFieldTemplateHashMap = new HashMap<>();

            for (
                    Map.Entry<QueueingFloorField.FloorFieldState, QueueingFloorField>
                            floorFieldStateQueueingFloorFieldEntry
                    : queueObject.getFloorFields().entrySet()
            ) {
                QueueingFloorField.FloorFieldState floorFieldState = floorFieldStateQueueingFloorFieldEntry.getKey();
                QueueingFloorField queueingFloorField = floorFieldStateQueueingFloorFieldEntry.getValue();

                // Get the disposition-state pair
                QueueingFloorField.FloorFieldState.DispositionStatePair dispositionStatePair
                        = new QueueingFloorField.FloorFieldState.DispositionStatePair(
                        floorFieldState.getDisposition(),
                        floorFieldState.getState()
                );

                // Get the reference patch
                Patch referencePatch = queueObject.getPatch();

                // Compute for the offsets of the apices
                List<Patch.Offset> apicesOffsets = new ArrayList<>();

                for (Patch apexPatch : queueingFloorField.getApices()) {
                    apicesOffsets.add(
                            Patch.Offset.getOffsetFromPatch(apexPatch, referencePatch)
                    );
                }

                // Compute for the offsets of the associated patches
                List<QueueingFloorFieldTemplate.PatchOffsetFloorFieldValuePair> associatedPatchesOffsets
                        = new ArrayList<>();

                for (Patch associatedPatch : queueingFloorField.getAssociatedPatches()) {
                    Patch.Offset offset = Patch.Offset.getOffsetFromPatch(associatedPatch, referencePatch);
                    double floorFieldValue
                            = associatedPatch.getFloorFieldValues().get(queueObject.getParent()).get(floorFieldState);

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
            floorFieldsTemplate.put(disposition, queueingFloorFieldTemplateHashMap);
        }
    }

    public HashMap<
            PassengerMovement.Disposition,
            HashMap<
                    QueueingFloorField.FloorFieldState.DispositionStatePair,
                    QueueingFloorFieldTemplate
                    >
            > getFloorFieldsTemplate() {
        return floorFieldsTemplate;
    }
}

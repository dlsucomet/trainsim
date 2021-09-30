package com.crowdsimulation.model.core.environment.station.patch.floorfield.headful;

import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.floorfield.FloorField;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.Queueable;

import java.util.ArrayList;
import java.util.List;

public abstract class HeadfulFloorField extends FloorField {
    // Denotes the patches in this floor field with the highest values
    private final List<Patch> apices;

    // Denotes the target associated with this floor field
    private final Queueable target;

    protected HeadfulFloorField(Queueable target) {
        this.apices = new ArrayList<>();
        this.target = target;
    }

    public List<Patch> getApices() {
        return apices;
    }

    public Queueable getTarget() {
        return target;
    }

    public static abstract class HeadfulFloorFieldFactory {

    }
}

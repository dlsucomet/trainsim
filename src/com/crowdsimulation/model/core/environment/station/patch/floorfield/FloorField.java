package com.crowdsimulation.model.core.environment.station.patch.floorfield;

import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.BaseStationObject;
import com.crowdsimulation.model.core.environment.station.patch.Patch;

import java.util.ArrayList;
import java.util.List;

// A floor field is a group of patches with values from 0.0 to 1.0 corresponding to a state
public abstract class FloorField extends BaseStationObject implements Environment {
    // Denotes the patches contained in this floor field
    private final List<Patch> associatedPatches;

    protected FloorField() {
        super();

        this.associatedPatches = new ArrayList<>();
    }

    public List<Patch> getAssociatedPatches() {
        return associatedPatches;
    }

    public static abstract class FloorFieldFactory extends StationObjectFactory {

    }
}

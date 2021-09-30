package com.crowdsimulation.model.simulator.cache;

import com.crowdsimulation.model.core.environment.station.patch.Patch;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DistanceCache extends Cache {
    private final Map<Patch.PatchPair, Double> distanceMap;

    public DistanceCache(int capacity) {
        super(capacity);

        this.distanceMap = Collections.synchronizedMap(new LinkedHashMap<Patch.PatchPair, Double>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Patch.PatchPair, Double> eldest) {
                return size() > capacity;
            }
        });
    }

    public void put(Patch.PatchPair patchPair, Double distance) {
        this.distanceMap.put(patchPair, distance);
    }

    public Double get(Patch.PatchPair patchPair) {
        return this.distanceMap.get(patchPair);
    }

    public void clear() {
        this.distanceMap.clear();
    }
}

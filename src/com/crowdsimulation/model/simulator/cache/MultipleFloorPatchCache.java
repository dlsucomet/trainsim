package com.crowdsimulation.model.simulator.cache;

import com.crowdsimulation.model.core.agent.passenger.movement.pathfinding.MultipleFloorPassengerPath;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultipleFloorPatchCache extends Cache {
    private final Map<PathCache.PathCacheKey, List<MultipleFloorPassengerPath>> pairPathMap;

    public MultipleFloorPatchCache(int capacity) {
        super(capacity);

        this.pairPathMap = Collections.synchronizedMap(
                new LinkedHashMap<PathCache.PathCacheKey, List<MultipleFloorPassengerPath>>() {
                    @Override
                    protected boolean removeEldestEntry(
                            Map.Entry<PathCache.PathCacheKey,
                                    List<MultipleFloorPassengerPath>> eldest
                    ) {
                        return size() > capacity;
                    }
                }
        );
    }

    public void put(PathCache.PathCacheKey pathCacheKey, List<MultipleFloorPassengerPath> passengerPath) {
        this.pairPathMap.put(pathCacheKey, passengerPath);
    }

    public List<MultipleFloorPassengerPath> get(PathCache.PathCacheKey pathCacheKey) {
        return this.pairPathMap.get(pathCacheKey);
    }

    public void clear() {
        this.pairPathMap.clear();
    }
}

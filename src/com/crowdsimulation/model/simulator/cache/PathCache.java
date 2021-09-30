package com.crowdsimulation.model.simulator.cache;

import com.crowdsimulation.model.core.agent.passenger.movement.pathfinding.PassengerPath;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.patch.Patch;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class PathCache extends Cache {
    private final Map<PathCacheKey, PassengerPath> pairPathMap;

    public PathCache(int capacity) {
        super(capacity);

        this.pairPathMap = Collections.synchronizedMap(new LinkedHashMap<PathCacheKey, PassengerPath>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<PathCacheKey, PassengerPath> eldest) {
                return size() > capacity;
            }
        });
    }

    public void put(PathCacheKey pathCacheKey, PassengerPath passengerPath) {
        this.pairPathMap.put(pathCacheKey, passengerPath);
    }

    public PassengerPath get(PathCacheKey pathCacheKey) {
        return this.pairPathMap.get(pathCacheKey);
    }

    public void clear() {
        this.pairPathMap.clear();
    }

    public static class PathCacheKey implements Environment {
        private final Patch.PatchPair patchPair;
        private final boolean includeStartingPatch;
        private final boolean includeGoalPatch;

        public PathCacheKey(Patch.PatchPair patchPair, boolean includeStartingPatch, boolean includeGoalPatch) {
            this.patchPair = patchPair;
            this.includeStartingPatch = includeStartingPatch;
            this.includeGoalPatch = includeGoalPatch;
        }

        public Patch.PatchPair getPatchPair() {
            return patchPair;
        }

        public boolean isIncludeStartingPatch() {
            return includeStartingPatch;
        }

        public boolean isIncludeGoalPatch() {
            return includeGoalPatch;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathCacheKey that = (PathCacheKey) o;
            return includeStartingPatch == that.includeStartingPatch && includeGoalPatch == that.includeGoalPatch && patchPair.equals(that.patchPair);
        }

        @Override
        public int hashCode() {
            return Objects.hash(patchPair, includeStartingPatch, includeGoalPatch);
        }
    }
}

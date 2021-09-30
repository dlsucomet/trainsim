package com.trainsimulation.model.core.environment.trainservice.maintenance;

import com.trainsimulation.model.core.environment.TrainSystem;
import com.trainsimulation.model.core.environment.infrastructure.track.Track;
import com.trainsimulation.model.core.environment.trainservice.passengerservice.stationset.Platform;

import java.util.EnumMap;
import java.util.Map;

// A depot is where the trains of a train system originate
public class Depot extends Maintenance {
    // Represents each of the depot's platforms in both directions
    private final Map<Track.Direction, Platform> platforms;

    public Depot(TrainSystem trainSystem) {
        super(trainSystem);

        // The length of the depot platform is arbitrary because the depot is just seen as the spawning and de-spawning
        // point of trains
        final int ARBITRARY_DEPOT_LENGTH = 300;

        // Set the platforms up
        this.platforms = new EnumMap<>(Track.Direction.class);
        this.platforms.put(Track.Direction.NORTHBOUND, new Platform(trainSystem, ARBITRARY_DEPOT_LENGTH,
                Track.Direction.NORTHBOUND));

        this.platforms.put(Track.Direction.SOUTHBOUND, new Platform(trainSystem, ARBITRARY_DEPOT_LENGTH,
                Track.Direction.SOUTHBOUND));
    }

    public Map<Track.Direction, Platform> getPlatforms() {
        return platforms;
    }
}

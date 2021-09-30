package com.crowdsimulation.model.core.environment.station.patch.patchobject.passable.gate;

import com.crowdsimulation.model.core.agent.passenger.Passenger;
import com.crowdsimulation.model.core.agent.passenger.movement.PassengerMovement;
import com.crowdsimulation.model.core.environment.Environment;
import com.crowdsimulation.model.core.environment.station.Floor;
import com.crowdsimulation.model.core.environment.station.Station;
import com.crowdsimulation.model.core.environment.station.patch.Patch;
import com.crowdsimulation.model.core.environment.station.patch.patchobject.Amenity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Portal extends Gate {
    // Denotes the floor that this portal serves
    private final Floor floorServed;

    // Denotes the location of this portal
    private PortalLocation portalLocation;

    // Denotes the pair (other end) of this portal
    private Portal pair;

    // Denotes the directory of this portal
    private Directory directory;

    protected Portal(List<AmenityBlock> amenityBlocks, boolean enabled, Floor floorServed) {
        super(amenityBlocks, enabled);

        this.floorServed = floorServed;
        this.directory = new Directory();
    }

    public Directory getDirectory() {
        return directory;
    }

    public static boolean isPortal(Amenity amenity) {
        return amenity instanceof Portal;
    }

    public static Portal asPortal(Amenity amenity) {
        if (isPortal(amenity)) {
            return (Portal) amenity;
        } else {
            return null;
        }
    }

    public Floor getFloorServed() {
        return floorServed;
    }

    public PortalLocation getPortalLocation() {
        return portalLocation;
    }

    public void setPortalLocation(PortalLocation portalLocation) {
        this.portalLocation = portalLocation;
    }

    public Portal getPair() {
        return pair;
    }

    public void setPair(Portal pair) {
        this.pair = pair;
    }

    // Have a passenger use this portal
    public abstract void absorb(Passenger passenger);

    public abstract Patch emit();

    // Portal factory
    public static abstract class PortalFactory extends GateFactory {
    }

    // Denotes the two locations that the portal may be in
    protected enum PortalLocation {
        LOWER,
        UPPER
    }

    public static class Directory implements Environment {
        // Denotes ths amenity classes accessible from this portal
        private final List<DirectoryItem> directoryItems;

        public Directory() {
            this.directoryItems = new ArrayList<>();
        }

        public void put(
                PassengerMovement.TravelDirection travelDirection,
                Class<? extends Amenity> nextAmenityClass,
                Station.AmenityCluster originAmenityCluster,
                Station.AmenityCluster destinationAmenityCluster,
                double distance
        ) {
            final DirectoryItem directoryItem = new DirectoryItem(
                    travelDirection,
                    nextAmenityClass,
                    originAmenityCluster,
                    destinationAmenityCluster,
                    distance
            );

            if (!this.directoryItems.contains(directoryItem)) {
                this.directoryItems.add(directoryItem);
            }
        }

        public DirectoryItem get(DirectoryItem directoryItemTemplate) {
            double minimumDistance = Double.MAX_VALUE;
            DirectoryItem shortestDirectoryItem = null;

            for (DirectoryItem directoryItem : this.directoryItems) {
                if (directoryItem.equals(directoryItemTemplate)) {
                    if (directoryItem.getDistance() < minimumDistance) {
                        minimumDistance = directoryItem.getDistance();
                        shortestDirectoryItem = directoryItem;
                    }
                }
            }

            return shortestDirectoryItem;
        }

        public void clear() {
            this.directoryItems.clear();
        }

        public static class DirectoryItem implements Environment {
            private final PassengerMovement.TravelDirection travelDirection;
            private final Class<? extends Amenity> amenityClass;
            private final Station.AmenityCluster originAmenityCluster;
            private final Station.AmenityCluster destinationAmenityCluster;
            private final double distance;

            public DirectoryItem(
                    PassengerMovement.TravelDirection travelDirection,
                    Class<? extends Amenity> amenityClass,
                    Station.AmenityCluster originAmenityCluster,
                    Station.AmenityCluster destinationAmenityCluster,
                    double distance
            ) {
                this.travelDirection = travelDirection;
                this.amenityClass = amenityClass;
                this.originAmenityCluster = originAmenityCluster;
                this.destinationAmenityCluster = destinationAmenityCluster;
                this.distance = distance;
            }

            public PassengerMovement.TravelDirection getTravelDirection() {
                return travelDirection;
            }

            public Class<? extends Amenity> getAmenityClass() {
                return amenityClass;
            }

            public Station.AmenityCluster getOriginAmenityCluster() {
                return originAmenityCluster;
            }

            public Station.AmenityCluster getDestinationAmenityCluster() {
                return destinationAmenityCluster;
            }

            public double getDistance() {
                return distance;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                DirectoryItem that = (DirectoryItem) o;

                if (destinationAmenityCluster != null && that.destinationAmenityCluster != null) {
                    return
                            travelDirection == that.travelDirection
                                    && amenityClass.equals(that.amenityClass)
                                    && originAmenityCluster.equals(that.originAmenityCluster)
                                    && destinationAmenityCluster.equals(that.destinationAmenityCluster);
                } else {
                    if (destinationAmenityCluster == null && that.destinationAmenityCluster == null) {
                        return false;
                    } else {
                        return
                                travelDirection == that.travelDirection
                                        && amenityClass.equals(that.amenityClass)
                                        && originAmenityCluster.equals(that.originAmenityCluster);
                    }
                }
            }

            @Override
            public int hashCode() {
                return Objects.hash(travelDirection, amenityClass, originAmenityCluster, destinationAmenityCluster);
            }
        }
    }
}

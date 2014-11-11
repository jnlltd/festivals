package ie.festivals.notify

/**
 * Encapsulates the lineup changes to a single festival
 */
class FestivalLineupDelta {

    Set<PerformanceDelta> addedArtists = []
    Set<PerformanceDelta> deletedArtists = []

    Set<PerformanceDelta> getNetAddedArtists() {
        addedArtists - deletedArtists
    }

    Set<PerformanceDelta> getNetDeletedArtists() {
        deletedArtists - addedArtists
    }

    boolean hasChanges() {
        netAddedArtists || netDeletedArtists
    }
}

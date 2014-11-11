package ie.festivals.notify

/**
 * Encapsulates the lineup changes to one or more festivals for a single user
 */
class FestivalNotification {

    private Map<LinkData, FestivalLineupDelta> festivalChanges = [:]

    void addArtist(LinkData festival, PerformanceDelta artist) {
        getFestivalLineupChange(festival).addedArtists << artist
    }

    void removeArtist(LinkData festival, PerformanceDelta artist) {
        getFestivalLineupChange(festival).deletedArtists << artist
    }

    private FestivalLineupDelta getFestivalLineupChange(LinkData festival) {
        if (!festivalChanges.containsKey(festival)) {
            festivalChanges[festival] = new FestivalLineupDelta()
        }
        festivalChanges[festival]
    }

    Map<LinkData, FestivalLineupDelta> getFestivalChanges() {
        this.festivalChanges
    }

    boolean hasChanges() {
        festivalChanges.values().any {FestivalLineupDelta festivalLineupChange ->
            festivalLineupChange.hasChanges()
        }
    }
}

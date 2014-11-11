package ie.festivals.notify

class ArtistNotification {

    final Set<PerformanceNotification> addedPerformances = []
    final Set<PerformanceNotification> deletedPerformances = []

    private Set<PerformanceNotification> getGrossAddedPerformances() {
        addedPerformances - deletedPerformances
    }

    private Set<PerformanceNotification> getGrossDeletedPerformances() {
        deletedPerformances - addedPerformances
    }

    Set<PerformanceNotification> getNetDeletedPerformances() {
        // if a performer without a date is deleted, then added with a date, the two PerformanceNotification objects
        // will not be equal, so we would otherwise have the following entries in the email
        //
        // - U2 will be performing at Very Long on 22 Mar 2013
        // - U2 will not be performing at Very Long
        //
        // We only want the first of these entries to appear. We can't just remove the date field from
        // PerformanceNotification.equals() because we then couldn't have a performer being added for multiple
        // dates for the same festival. Instead filter out any deleted performers without a date that have
        // also been added with a date.
        Set<PerformanceNotification> grossDeletedPerformances = getGrossDeletedPerformances()
        Set<PerformanceNotification> grossAddedPerformances = getGrossAddedPerformances()

        grossDeletedPerformances.removeAll { PerformanceNotification deleted ->
            !deleted.date && grossAddedPerformances.find { PerformanceNotification added ->
                added.date && added.artist == deleted.artist && added.festival == deleted.festival
            }
        }
        grossDeletedPerformances
    }

    Set<PerformanceNotification> getNetAddedPerformances() {
        getGrossAddedPerformances()
    }
}

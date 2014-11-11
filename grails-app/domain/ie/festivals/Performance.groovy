package ie.festivals

import grails.util.Holders
import ie.festivals.enums.FestivalSource
import ie.festivals.enums.Priority
import org.apache.commons.lang.time.DateUtils

class Performance {

    static auditable = true

    Priority priority
    Date date

    /**
     * We need to record whether a time was specified for this performance. Otherwise this is no way to
     * distinguish between a specified time of midnight and no time unspecified (the latter defaults to midnight).
     */
    Boolean hasPerformanceTime = false
    Date dateCreated

    /**
     * We don't delete performances until notifications thereabout have been sent
     */
    boolean deleted = false
    static belongsTo = [artist: Artist, festival: Festival]

    static mapping = {
        priority length: Holders.config.festival.utf8mb4MaxLength
    }

    static constraints = {

        festival validator: { Festival festival ->
            // Can't add a performer to a festival that doesn't have a lineup
            festival.hasLineup
        }

        // artist can't appear multiple times on same day, but we can't use a DB constraint to enforce this because
        // of the deleted column
        date nullable: true, validator: { Date perfDate, self ->

            // Optimisation, skip all checks if soft-deleting an artist or if it's a Skiddle festival
            // Skiddle festivals should never specify the same artist more than once
            if (self.deleted || self.festival.source == FestivalSource.SKIDDLE) {
                return true
            }

            // get any other performances the artist is making at this festival
            List<Performance> otherPerformances = withCriteria {
                eq 'festival', self.festival
                eq 'artist', self.artist
                eq 'deleted', false
                ne 'id', self.id
            }

            if (perfDate) {

                if (!perfDate in self.festival.start..self.festival.end) {
                    return 'not.during.festival'
                }

                Closure isSameDayOrNull = { Performance performance ->
                    !performance.date || DateUtils.isSameDay(performance.date, perfDate)
                }

                if (otherPerformances.any(isSameDayOrNull)) {
                    // we can't add a performer with a date if the performer is already scheduled to perform
                    // with an unspecified date or on the same day
                    return 'duplicate'
                }
            } else if (otherPerformances) {
                // we can't add a performer without a date if performer is already in lineup
                return 'duplicate'
            }
        }

        // we need this because there were already rows in the table where dateCreated was added
        dateCreated nullable: true
    }
}

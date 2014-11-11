package ie.festivals.job

import ie.festivals.Artist
import ie.festivals.ArtistService

/**
 * Updates the last.fm data for a range of artists. The implementation assumes this job will be run every day
 */
class UpdateMusicianDetailsJob {

    static triggers = {
        // Trigger patterns explained: http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
        cron cronExpression: '0 0 3 * * ?'
    }

    def concurrent = false
    ArtistService artistService

    private static final DAYS_PER_WEEK = 7

    def execute() {
        log.info "$UpdateMusicianDetailsJob.simpleName started at ${new Date()}"

        // Each day we check if 1/7th of the artists are eligible for updates. We need to minimise the number we check
        // every day in order to avoid getting banned from last.fm
        def maxArtistId = Artist.listOrderById(max: 1, order: 'desc')

        if (maxArtistId) {
            maxArtistId = maxArtistId[0].id
            List<Long> dailyArtistIdRanges = []

            DAYS_PER_WEEK.times {
                def val = maxArtistId / DAYS_PER_WEEK * it
                dailyArtistIdRanges << (val as Long)
            }
            dailyArtistIdRanges << maxArtistId

            // Get the ID range for today
            def todayIndex = new Date()[Calendar.DAY_OF_WEEK]

            Long minId = dailyArtistIdRanges[todayIndex - 1]
            Long maxId = dailyArtistIdRanges[todayIndex]

            log.info "Checking for updates to artists with IDs in range $minId..$maxId"
            updateLastFmArtists(minId, maxId)
        }
        log.info "$UpdateMusicianDetailsJob.simpleName completed at ${new Date()}"
    }

    /**
     * Update the bio, top tracks, top albums for one or more artists. This method manages its own transactions
     * programmatically. Artists with an ID > the lower end of the range and <= the upper end will be updated
     *
     * @param minId
     * @param maxId
     */
    private void updateLastFmArtists(Long minId, Long maxId) {

        List<Artist> artists = Artist.withCriteria {

            // only check last.fm artists
            eq('lastFm', true)

            gt('id', minId)
            le('id', maxId)
        }

        def successCount = 0
        def failCount = 0

        artists.eachWithIndex { Artist artist, i ->

            log.debug "Updating artist '$artist.name', ${i + 1} of ${artists.size()}"

            // We need to update each artist in their own session, otherwise a failure of one will invalidate
            // the session, causing all subsequent artists to also fail
            Artist.withNewSession { session ->
                try {
                    artistService.updateArtistInfo(artist)
                    log.info "Updated musician details for $artist.name. Updated image $artist.image"
                    ++successCount

                } catch (ex) {
                    log.error "Failed to update musician details for artist ID: $artist.id", ex
                    ++failCount
                }
            }
        }

        log.info "Successfully updated $successCount artists. Failed to update $failCount"
    }
}
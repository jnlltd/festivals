package ie.festivals

import grails.plugin.searchable.internal.lucene.LuceneUtils
import grails.plugins.springsecurity.SpringSecurityService
import ie.festivals.music.Album
import ie.festivals.music.Track
import ie.festivals.util.StringComparatorIgnoreCaseAndDiacritics
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.imgscalr.AsyncScalr
import org.imgscalr.Scalr.Mode
import grails.transaction.Transactional

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

@Transactional(rollbackFor = Throwable)
class ArtistService {

    SpringSecurityService springSecurityService
    GrailsApplication grailsApplication
    ImageService imageService
    LastFmService lastFmService
    ArtistVideoService artistVideoService

    /**
     * Locally saves an artists image and thumbnail
     *
     * @param artist
     * @param artistImageURL full-size image of the artist
     * @return
     * @throws IOException
     */
    void saveArtistImagesLocally(Artist artist, String artistImageURL) throws IOException {

        if (!artistImageURL) {
            log.warn "No image available for artist ID: $artist.id"
            return
        }
        assert artist.id, "Cannot save images for transient artist $artist"

        def imageConfig = grailsApplication.config.festival.images
        String imageBasePath = imageConfig.artistDir
        String imageType = imageConfig.artistImageType

        String imageRelativePath = "$artist.id/image.$imageType"
        String thumbRelativePath = "$artist.id/thumb.$imageType"

        File imageFile = new File(imageBasePath, imageRelativePath)
        File thumbFile = new File(imageBasePath, thumbRelativePath)
        File artistImageDir = imageFile.parentFile

        // Save the path to the local copy of the image and thumbnail
        artist.thumbnail = thumbRelativePath
        artist.image = imageRelativePath

        if (!artistImageDir.isDirectory() && !artistImageDir.mkdirs()) {
            throw new IOException("Failed to create artist image dir: $artistImageDir")
        }

        BufferedImage fullSizeImage = saveImageLocally(artistImageURL, imageType, imageFile)
        artist.imageDimensions = new ImageDimensions(width: fullSizeImage.width, height: fullSizeImage.height)

        try {
            // Resize the image to a thumbnail and save the thumbnail to the filesystem
            Integer thumbWidth = imageConfig.thumbnailWidth
            Image thumbnail = AsyncScalr.resize(fullSizeImage, Mode.FIT_TO_WIDTH, thumbWidth, 0).get()
            artist.thumbnailDimensions = new ImageDimensions(width: thumbWidth, height: thumbnail.height)

            try {
                ImageIO.write(thumbnail, imageType, thumbFile)
            } finally {
                thumbnail.flush()
            }
        } finally {
            fullSizeImage.flush()
        }
    }

    private BufferedImage saveImageLocally(String remoteImageURL, String imageType, File localImage) {

        BufferedImage image = imageService.read(remoteImageURL)
        ImageIO.write(image, imageType, localImage)
        log.debug "Saved image $remoteImageURL to $localImage"
        image
    }

    private List<Long> getArtistsWithoutPerformanceDate(Festival festival) {

        Performance.withCriteria {
            eq('festival', festival)
            eq('deleted', false)
            isNull('date')

            projections {
                property("artist.id")
            }
            cache true
        }
    }

    Artist saveNew(Artist artist) {

        // If this method is used to update an existing artist there's a some properties will be reset to null
        // This can happen if the artist was retrieved from the Lucene index, which omits properties like mbid, image, etc.
        assert !artist.id, "Artist $artist.name has already been saved"

        artist = artist.save(failOnError: true, flush: true)
        updateArtistInfo(artist)
        log.debug "Saved new artist with name: ${artist.name}"
        artist
    }

    /**
     * Search for artists by name on last.fm and in the database. We need to look in the DB as well as on
     * last.fm, because otherwise someone like Des Bishop would never be found
     * @param artistName
     * @return
     */
    private List<Artist> searchArtistsByName(String artistName, Boolean matchNameExactly = false) {
        artistName = artistName?.trim()
        if (!artistName) {
            return Collections.emptyList()
        }

        // We need to look in the DB as well as on LastFM, because otherwise someone like Des Bishop would never be found
        List<Artist> lastFMResults = Collections.emptyList()

        try {
            lastFMResults = lastFmService.searchArtists(artistName, matchNameExactly)
        } catch (IOException ex) {
            // If the last.fm service is throwing errors, it's probably down, so only show DB results #459
            log.error "Error retrieving last.fm results, only database results will be shown", ex
        }
        String escapedArtistName = LuceneUtils.escapeQuery(artistName)

        // http://grails.org/Searchable+Plugin+-+Methods+-+search
        def queryOptions = matchNameExactly ? [max: 1] : Collections.emptyMap()
        String query = matchNameExactly ? escapedArtistName : "$escapedArtistName*"

        List<Artist> dbResults = Artist.search(query, queryOptions).results

        if (!lastFMResults) {
            dbResults

        } else if (!dbResults) {
            lastFMResults

        } else {
            // To reduce the possibility of an artist being added twice, if an artist in the last.fm results has the same
            // name or mbid as an artist in the DB results, replace the former with the latter.
            List<Artist> mergedResults = []

            // call findAll() to remove nulls
            def dbArtistMbids = dbResults.mbid.findAll()
            def dbArtistNames = dbResults.name

            lastFMResults.removeAll {
                it.name in dbArtistNames || it.mbid in dbArtistMbids
            }

            dbResults + lastFMResults
        }

    }

    /**
     * Search for artists to add to a festival's lineup. Artists already included in the festival lineup should be
     * omitted from the results
     * @param artistName
     * @param festival
     * @return
     */
    @Transactional(readOnly = true)
    List<Artist> festivalLineupSearch(String artistName, Festival festival, Boolean matchNameExactly = false) {
        List<Artist> searchResults = searchArtistsByName(artistName, matchNameExactly)

        // Remove any artists that are already in the lineup but haven't been assigned a performance date
        if (searchResults && festival) {
            def currentLineupArtistIds = getArtistsWithoutPerformanceDate(festival)
            searchResults.removeAll { Artist searchResult -> searchResult.id in currentLineupArtistIds }
        }
        searchResults
    }

    /**
     * Search for artists to subscribe to. Filter out artists the user is already subscribed to
     * @param artistName
     * @return
     */
    @Transactional(readOnly = true)
    List<Artist> artistSubscriptionSearch(String artistName) {

        List<Artist> searchResults = searchArtistsByName(artistName)

        if (searchResults) {
            def currentSubscriptionIds = currentSubscriptions()*.id
            searchResults.removeAll { Artist searchResult -> searchResult.id in currentSubscriptionIds }
        }
        searchResults
    }

    @Transactional(readOnly = true)
    List<Artist> currentSubscriptions() {

        // optimization: skip the query if the user is not logged in
        if (springSecurityService.loggedIn) {

            Artist.withCriteria {
                subscriptions {
                    eq('user', springSecurityService.currentUser)
                }
                order("name")

                cache true
            }
        } else {
            Collections.emptyList()
        }
    }

    private boolean isUnique(Collection<Named> currentEntities, String newEntityName) {

        if (!currentEntities) {
            return true
        }

        !currentEntities.any {
            // If we have one entity with name "Dónal" and another named "Donal" the unique constraint in the domain class
            // will fail, so we need to do the comparison after normalising diacritics
            StringComparatorIgnoreCaseAndDiacritics.instance.compare(newEntityName, it.name) == 0
        }
    }

    /**
     * Download and persist an artist's bio, top tracks, top albums, etc. from last.fm
     * @param artist
     * @throws IOException e.g. if the artist we're trying to update has been deleted from last.fm
     */
    void updateArtistInfo(Artist artist) throws IOException {

        lastFmService.updateArtistInfo(artist)

        // Refresh the top tracks
        List<Track> topTracks = lastFmService.getTopTracks(artist)
        Track.executeUpdate('delete Track where artist = ?', [artist])

        topTracks.each { Track newTrack ->
            if (isUnique(artist.topTracks, newTrack.name)) {
                artist.addToTopTracks(newTrack)
            }
        }

        // Refresh the top albums
        List<Album> topAlbums = lastFmService.getTopAlbums(artist)
        Album.executeUpdate('delete Album where artist = ?', [artist])

        topAlbums.each { Album newAlbum ->
            if (isUnique(artist.topAlbums, newAlbum.name)) {
                artist.addToTopAlbums(newAlbum)
            }
        }

        artist.videoEmbedCode = artistVideoService.getVideoEmbedCode(artist)
    }

    @Transactional(readOnly = true)
    List<Artist> recentlyAddedArtist(Integer count) {
        Artist.withCriteria {
            order("dateCreated", "desc")

            maxResults(count)
            cache true
        }
    }
}
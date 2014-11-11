package ie.festivals

import ie.festivals.music.Album
import ie.festivals.music.Track
import ie.festivals.notify.ArtistSubscription
import org.apache.commons.io.FileUtils

import static groovy.io.FileType.FILES

class Artist implements Named {

    def grailsApplication

    Date dateCreated
    Date lastUpdated

    String name
    String mbid
    String thumbnail
    String image

    ImageDimensions thumbnailDimensions
    ImageDimensions imageDimensions

    static embedded = ['thumbnailDimensions', 'imageDimensions']

    String bioSummary
    String bioFull
    Boolean lastFm = true
    String videoEmbedCode

    static searchable = {
        mapping {
            spellCheck "include"
        }

        // Searchable fields
        only = ['name']

        // When we search for an artist to subscribe to, or to add to a lineup, we need these fields to be returned
        // but we don't want them to be searchable
        thumbnail index: 'no', store: 'yes'
        bioSummary index: 'no', store: 'yes'
        mbid index: 'no', store: 'yes'
    }

    static hasMany = [topTracks: Track, topAlbums: Album, performances: Performance, subscriptions: ArtistSubscription]

    static constraints = {
        imageDimensions nullable: true
        thumbnailDimensions nullable: true
        videoEmbedCode nullable: true, shared: 'unlimitedSize'
        bioSummary nullable: true, shared: 'unlimitedSize'
        bioFull nullable: true, shared: 'unlimitedSize'
        mbid nullable: true, unique: true
        name blank: false
        performances nullable: true
        thumbnail nullable: true
        image nullable: true

        // need to prevent the default maxSize constraint of 191 from applying to these collections
        topTracks shared: 'unlimitedSize'
        topAlbums shared: 'unlimitedSize'
        performances shared: 'unlimitedSize'
        subscriptions shared: 'unlimitedSize'
    }

    void setName(String artistName) {
        this.name = artistName?.trim()
    }

    @Override
    String toString() {
        name
    }

    boolean hasLocalImages() {
        image && !image.startsWith('http')
    }

    static mapping = {
        bioSummary type: 'text'
        bioFull type: 'text'
        topTracks cache: true
        topAlbums cache: true
        videoEmbedCode type: 'text'

        cache true
    }

    def afterDelete() {
        // remove any local images for this artist
        if (hasLocalImages()) {
            def imagesDir = "$grailsApplication.config.festival.images.artistDir/$id"
            File localImagesDir = new File(imagesDir)

            if (localImagesDir.exists()) {
                boolean imagesInSVN = new File(localImagesDir, '.svn').isDirectory()

                if (imagesInSVN) {
                    // Be careful not to try and remove the .svn directory
                    localImagesDir.eachFile FILES, {it.delete()}

                } else {
                    FileUtils.deleteDirectory(localImagesDir)
                }
            }
        }
    }
}

class ImageDimensions {
    Integer width
    Integer height

    static constraints = {
        width min: 1
        height min: 1
    }
}
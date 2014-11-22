package ie.festivals.xmlparser.lastfm

import groovy.util.slurpersupport.GPathResult
import ie.festivals.Artist
import ie.festivals.ImageDimensions
import ie.festivals.ImageService
import ie.festivals.util.HtmlUtils

import java.awt.image.BufferedImage

class ArtistInfoParser extends AbstractLastFmParser<Artist> {

    private final Artist artist
    private final ImageService imageService

    ArtistInfoParser(Artist artist, ImageService imageService) {
        this.artist = artist
        this.imageService = imageService
    }

    @Override
    Artist parse(GPathResult artistInfo) {

        artist.bioSummary = HtmlUtils.normalize(artistInfo?.artist?.bio?.summary?.text()) ?: artist.bioSummary
        artist.bioFull = HtmlUtils.normalize(artistInfo?.artist?.bio?.content?.text()) ?: artist.bioFull

        // never replace local images
        if (!artist.hasLocalImages()) {
            def artistElement = artistInfo.artist
            ArtistImage newImage = getArtistImage(artistElement, 'extralarge')

            if (newImage) {
                artist.image = newImage.url
                artist.imageDimensions = newImage.dimensions
            }

            ArtistImage newThumbnail = getArtistImage(artistElement, 'large')

            if (newThumbnail) {
                artist.thumbnail = newThumbnail.url
                artist.thumbnailDimensions = newThumbnail.dimensions
            }
        }
        artist
    }

    private ImageDimensions getRemoteImageSize(String url) {
        BufferedImage image = imageService.read(url)
        new ImageDimensions(width: image.width, height: image.height)
    }

    private ArtistImage getArtistImage(artistElement, size) {
        String image = getImage(artistElement, size)

        if (imageService.isValidImageUrl(image)) {
            try {
                ImageDimensions imageDimensions = getRemoteImageSize(image)
                new ArtistImage(url: image, dimensions: imageDimensions)

            } catch (IllegalArgumentException ex) {
                // Sometimes the Last fm XML contains a corrupt image, e.g. http://userserve-ak.last.fm/serve/126/73753888.jpg
                // In these cases we will get an IllegalArgumentException here
                log.warn "Error retrieving size of image: $image", ex
            }
        }
    }


    private static class ArtistImage {
        String url
        ImageDimensions dimensions
    }
}

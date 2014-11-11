package ie.festivals.xmlparser.lastfm

import groovy.util.slurpersupport.GPathResult
import ie.festivals.music.Album
import ie.festivals.ImageService

class TopAlbumsParser extends AbstractLastFmParser<List<Album>> {

    private final ImageService imageService

    TopAlbumsParser(ImageService imageService) {
        this.imageService = imageService
    }

    @Override
    List<Album> parse(GPathResult topAlbumsXml) {

        Iterator topAlbumsIterator = topAlbumsXml.topalbums.album.iterator()
        def topAlbums = []

        while (topAlbumsIterator.hasNext()) {
            def topAlbumXml = topAlbumsIterator.next()

            // Don't allow duplicate album names for a given artist
            String albumName = topAlbumXml.name.text()

            def album = new Album(
                    name: albumName,
                    url: topAlbumXml.url.text(),
                    largeImageUrl: getImage(topAlbumXml, 'large'))

            if (imageService.isValidImageUrl(album.largeImageUrl)) {
                topAlbums << album
            }
        }

        topAlbums
    }
}

package ie.festivals.parser.lastfm

import ie.festivals.Artist
import groovy.util.slurpersupport.GPathResult


class ArtistSearchParser extends AbstractLastFmParser<List<Artist>> {

    private final Boolean matchNameExactly
    private final String name

    ArtistSearchParser(String name, Boolean matchNameExactly) {
        this.matchNameExactly = matchNameExactly
        this.name = name
    }

    @Override
    List<Artist> parse(GPathResult records) {
        def artistsXml = records.results.artistmatches.artist

        Iterator resultsIterator = artistsXml.iterator()
        List<Artist> artists = []

        while (resultsIterator.hasNext()) {
            def artistXml = resultsIterator.next()

            def musicBrainzId = artistXml.mbid.text()
            def artistName = artistXml.name.text()
            def largeImg = getImage(artistXml, 'large')
            def extraLargeImg = getImage(artistXml, 'extralarge')

            // even though the images aren't mandatory, if a result doesn't have an image, it's probably rubbish #530
            if (artistName && largeImg && extraLargeImg) {
                artists << new Artist(
                        name: artistName,
                        mbid: musicBrainzId,
                        thumbnail: largeImg,
                        image: extraLargeImg)
            }
        }

        // last.fm doesn't provide exact name matching so filter out inexact matches ourselves
        matchNameExactly ? removeInexactMatches(name, artists) : artists
    }

    private List<Artist> removeInexactMatches(String name, List<Artist> artists) {
        artists.findAll {it.name == name} ?: artists.findAll {it.name.equalsIgnoreCase(name)}
    }
}

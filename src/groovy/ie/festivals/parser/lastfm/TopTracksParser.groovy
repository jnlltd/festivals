package ie.festivals.parser.lastfm

import groovy.util.slurpersupport.GPathResult
import ie.festivals.music.Track

class TopTracksParser extends AbstractLastFmParser<List<Track>> {

    @Override
    List<Track> parse(GPathResult topTracksXml) {

        Iterator topTracksIterator = topTracksXml.toptracks.track.iterator()
        def topTracks = []

        while (topTracksIterator.hasNext()) {
            def topTrackXml = topTracksIterator.next()

            // Don't allow duplicate track names for the same artist
            String trackName = topTrackXml.name.text()

            topTracks << new Track(
                    name: trackName,
                    duration: topTrackXml.duration.text(),
                    url: topTrackXml.url.text())
        }

        topTracks
    }
}

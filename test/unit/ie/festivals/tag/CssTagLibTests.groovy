package ie.festivals.tag

import grails.test.mixin.TestFor
import ie.festivals.Artist

@TestFor(CssTagLib)
class CssTagLibTests {

    void testGetArtistClass() {

        // if we set properties using the map constructor, e.g. new Artist(id: 10) it doesn't seem to work
        Artist artist = new Artist()
        artist.id = 10

        def cssClass = tagLib.getArtistClass(artist: artist)
        assertEquals 'artist-10 saved', cssClass.toString()

        artist.id = null
        artist.mbid = 'some-crazy-assed-mbid'
        cssClass = tagLib.getArtistClass(artist: artist)
        assertEquals 'artist-some-crazy-assed-mbid', cssClass.toString()

        artist.mbid = null
        assertEquals 'artist-', tagLib.getArtistClass(artist: new Artist()).toString()
    }
}

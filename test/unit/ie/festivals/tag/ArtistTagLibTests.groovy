package ie.festivals.tag

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import ie.festivals.Artist
import ie.festivals.codecs.SeoNameCodec
import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import org.junit.Before

@Mock([Artist])
@TestFor(ArtistTagLib)
class ArtistTagLibTests {

    @Before
    void mockCodecs() {
        mockCodec(SeoNameCodec)
        mockCodec(URLCodec)
    }

    void testShowWithoutBody() {
        Artist artist = new Artist(name: 'Leonard Cohen').save()
        def artistLink = tagLib.show(id: artist.id, name: artist.name)
        def expected = getExpectedLink(artist)
        assertEquals expected, artistLink.toString()
    }

    void testShowWithBody() {
        Artist artist = new Artist(name: 'Leonard Cohen').save()
        def artistLink = tagLib.show(id: artist.id, name: artist.name, 'linked text')
        def expected = getExpectedLink(artist, 'linked text')
        assertEquals expected, artistLink.toString()
    }

    void testShowArtistWithNameContainingXmlEntity() {
        Artist artist = new Artist(name: 'Nick Cave & The Bad Seeds').save()
        def artistLink = tagLib.show(id: artist.id, name: artist.name)

        // in the name param value, the '&' is encoded once by SeoNameCodec to '%26' and Grails then converts this
        // to '%2526'. I don't think this really matters because we never actually use (decode) this name, it's just
        // for SEO purposes
        def expected = '<a href="/artist/show/1?name=nick-cave-%2526-the-bad-seeds">Nick Cave &amp; The Bad Seeds</a>'
        assertEquals expected, artistLink.toString()
    }

    private String getExpectedLink(Artist artist, String linkedText = null) {
        def seoName = artist.name.encodeAsSeoName()
        linkedText = linkedText ?: artist.name.encodeAsHTML()

        // The structure of the URL in the link below is different to what is generated in production. I guess this
        // is because the URL mappings are not used in unit tests
        "<a href=\"/artist/show/$artist.id?name=$seoName\">$linkedText</a>".toString()
    }
}

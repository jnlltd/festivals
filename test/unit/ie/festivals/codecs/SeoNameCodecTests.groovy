package ie.festivals.codecs

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.plugins.codecs.URLCodec


@TestMixin(GrailsUnitTestMixin)
class SeoNameCodecTests {

    void testEncodeAsSeoName() {
        mockCodec(SeoNameCodec)
        mockCodec(URLCodec)

        assertEquals 'bob-dylan', 'Bob Dylan'.encodeAsSeoName()
        assertEquals 'bob-dylan', 'Bob_Dylan'.encodeAsSeoName()
        assertEquals 'nick-cave-%26-the-bad-seeds', 'Nick Cave & The Bad Seeds'.encodeAsSeoName()
        assertEquals 'beyonce-knowles', 'Beyoncé Knowles'.encodeAsSeoName()
        assertEquals 'u2', ' U2 '.encodeAsSeoName()
    }
}

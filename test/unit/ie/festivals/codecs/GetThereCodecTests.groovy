package ie.festivals.codecs

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.plugins.codecs.URLCodec

@TestMixin(GrailsUnitTestMixin)
class GetThereCodecTests {

    void testEncodeAsSeoName() {
        mockCodec(GetThereCodec)
        mockCodec(SeoNameCodec)
        mockCodec(URLCodec)

        assertEquals 'bob_dylan', 'Bob Dylan'.encodeAsGetThere()
        assertEquals 'nick_cave_%26_the_bad_seeds', 'Nick Cave & The Bad Seeds'.encodeAsGetThere()
        assertEquals 'beyonce_knowles', 'Beyoncé Knowles'.encodeAsGetThere()
        assertEquals 'u2', ' U2 '.encodeAsGetThere()
    }
}

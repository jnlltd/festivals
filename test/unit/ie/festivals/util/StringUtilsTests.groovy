package ie.festivals.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class StringUtilsTests {

    void testInvalidHttpUrls() {

        ['ftp://example.org', ' ', '//example.org', 'http:/example.org'].each {
            assertFalse StringUtils.isWebUrl(it)
        }

        assertFalse StringUtils.isWebUrl(null, false)
    }

    void testValidHttpUrls() {

        ['http://example.org', 'https://example.org', 'https://example.rocks', null].each {
            assertTrue StringUtils.isWebUrl(it)
        }
    }
}

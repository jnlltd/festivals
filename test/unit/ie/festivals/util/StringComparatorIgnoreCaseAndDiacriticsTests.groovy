package ie.festivals.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class StringComparatorIgnoreCaseAndDiacriticsTests {

    private Comparator stringComparator = StringComparatorIgnoreCaseAndDiacritics.instance

    void testDiacriticAndCaseInsensitiveStringComparator() {

        assertTrue stringComparator.compare('foo', 'bar') != 0
        assertEquals 0, stringComparator.compare('foo', 'FOO')
        assertEquals 0, stringComparator.compare('donal', 'Dónàl')
    }
}

package ie.festivals.databinding

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import ie.festivals.enums.FestivalType
import org.grails.databinding.converters.FormattedValueConverter

import static ie.festivals.enums.FestivalType.*


@TestMixin(GrailsUnitTestMixin)
class EnumCollectionConverterTests {

    private FormattedValueConverter converter = new EnumCollectionConverter()

    void testFestivalTypeStringConversion() {

        Collection<FestivalType> result = doFestivalTypeConversion('HEADLINE')
        assertEquals 1, result.size()
        assertEquals HEADLINE, result[0]

        result = doFestivalTypeConversion('HEADLINE,MUSIC')
        assertEquals 2, result.size()
        assertEquals result.toSet(), [HEADLINE, MUSIC].toSet()

        result = doFestivalTypeConversion('  HEADLINE  ,  MUSIC  ')
        assertEquals result.toSet(), [HEADLINE, MUSIC].toSet()

        result = doFestivalTypeConversion(null)
        assertTrue result.empty

        result = doFestivalTypeConversion('')
        assertTrue result.empty

        result = doFestivalTypeConversion('   ')
        assertTrue result.empty
    }

    void testFestivalTypeStringArrayConversion() {
        String[] values = ['HEADLINE', 'MUSIC']
        Collection result = doFestivalTypeConversion(values)
        compareCollections([HEADLINE, MUSIC], result.toList())

        assertTrue doFestivalTypeConversion(Collections.emptyList().toArray()).empty
    }

    void testFestivalTypeStringConversionBadFormatName() {

        // if the format name is invalid, the original value should be returned
        assertEquals 'HEADLINE', converter.convert('HEADLINE', 'badFormatName')
    }

    void testFestivalTypeStringCaseInsensitiveIgnoreDuplicates() {

        def result = doFestivalTypeConversion('headline,MUSIC,sport,SPORT,Sport')
        assertEquals 3, result.size()
        compareCollections result, [HEADLINE, MUSIC, SPORT]
    }

    /**
     * Invalid values should be skipped
     */
    void testFestivalTypeBadValue() {

        def result = doFestivalTypeConversion('invalidValue')
        assertEquals 0, result.size()

        result = doFestivalTypeConversion('HEADLINE,invalidValue')
        compareCollections result, [HEADLINE]
    }

    void testLegacyFestivalTypeConversion() {
        String convertable = FestivalType.RENAMED_CONSTANTS.keySet().join(',')
        Collection<FestivalType> actualResult = doFestivalTypeConversion(convertable)
        Collection<FestivalType> expectedResult = FestivalType.RENAMED_CONSTANTS.values()

        compareCollections expectedResult, actualResult
    }

    private Collection<FestivalType> doFestivalTypeConversion(convertableValue) {
        converter.convert(convertableValue, FestivalType.simpleName)
    }

    private compareCollections(Collection expected, Collection actual) {
        assertEquals expected.size(), actual.size()

        expected.every {
            assertTrue it in actual
        }
    }
}

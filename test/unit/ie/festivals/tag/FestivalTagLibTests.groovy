package ie.festivals.tag

import grails.test.mixin.TestFor
import ie.festivals.Festival
import ie.festivals.enums.FestivalType

@TestFor(FestivalTagLib)
class FestivalTagLibTests {

    void testGetDescriptionWithSynopsis() {
        def festival = new Festival(synopsis: '<p> This <strong> festival</strong>  is  a crock of shit. </p>  ')
        def description = tagLib.getDescription(festival: festival)

        assertEquals 'This festival is a crock of shit.'.toString(), description.toString()
    }

    void testGetDescriptionWithoutSynopsis() {

        def today = new Date().clearTime()
        def festival = new Festival(type: FestivalType.ARTS, city: 'Berlin', start: today)
        def description = tagLib.getDescription(festival: festival)

        def startDate = today.format(tagLib.DESCRIPTION_DATE_FORMAT)
        def expected = "${festival.type} festival in ${festival.city.encodeAsHTML()} starting on $startDate"
        assertEquals expected.toString(), description.toString()
    }


    void testConcatenateNameAndYear() {
        def result = tagLib.concatenateNameAndYear(festival: createFestival("festival", 2012))
        assertEquals "festival, 2012", result.toString()

        result = tagLib.concatenateNameAndYear(festival: createFestival("festival2013", 2012))
        assertEquals "festival, 2013", result.toString()

        result = tagLib.concatenateNameAndYear(festival: createFestival("festival2013  ", 2012))
        assertEquals "festival, 2013", result.toString()

        result = tagLib.concatenateNameAndYear(festival: createFestival("festival 2013", 2012))
        assertEquals "festival, 2013", result.toString()

        result = tagLib.concatenateNameAndYear(festival: createFestival("festival 2013  ", 2012))
        assertEquals "festival, 2013", result.toString()

        result = tagLib.concatenateNameAndYear(festival: createFestival("festival  2013  ", 2012))
        assertEquals "festival, 2013", result.toString()

        result = tagLib.concatenateNameAndYear(festival: createFestival("  festival  2013  ", 2012))
        assertEquals "festival, 2013", result.toString()

        // a festival that will "never" be over
        result = tagLib.concatenateNameAndYear(festival: createFestival("festival", 9999))
        assertEquals "festival, 9999", result.toString()
    }

    private Festival createFestival(String name, Integer year) {
        def calendar = Calendar.instance
        calendar[Calendar.YEAR] = year
        new Festival(name: name, start: calendar.time, end: calendar.time + 3)
    }
}

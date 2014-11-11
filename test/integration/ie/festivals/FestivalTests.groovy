package ie.festivals

class FestivalTests extends GroovyTestCase {

    void testUpdateFestivalWithSeveralPerformancesBySingleArtist() {
        Date now = new Date()
        Festival festival = Festival.build(start: now - 5, end: now + 5)
        Artist artist = Artist.build()

        // can't have multiple performances by the same artist if none of them have a date
        def firstPerformance = Performance.build(festival: festival, artist: artist)
        assertTrue festival.validate()

        def secondPerformance = Performance.build(festival: festival, artist: artist)
        assertFalse festival.validate()

        // can't have multiple performances by the same artist if any of them don't have a date
        firstPerformance.date = now
        assertFalse festival.validate()

        // can't have multiple performances by the same artist on the same day
        secondPerformance.date = now
        assertFalse festival.validate()

        // can have multiple performances by the same artist on the different days
        secondPerformance.date = now + 1
        assertTrue festival.validate()
    }
}

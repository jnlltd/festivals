package ie.festivals.notify


class FestivalLineupDeltaTests extends GroovyTestCase {

    void testPerformanceDeltaEquals() {
        Date now = new Date()
        PerformanceDelta bobDylan = new PerformanceDelta(name: 'Bob Dylan', id: 11, date: now)
        PerformanceDelta bobDylan2 = new PerformanceDelta(name: 'Bob Dylan', id: 11, date: now)
        assertEquals bobDylan, bobDylan2

        bobDylan2.date = now + 1
        assertFalse bobDylan == bobDylan2
    }

    void testChangeDetection() {
        PerformanceDelta bobDylan = new PerformanceDelta(name: 'Bob Dylan', id: 11)
        PerformanceDelta theCure = new PerformanceDelta(name: 'The Cure', id: 12)

        FestivalLineupDelta lineupDelta = new FestivalLineupDelta()
        lineupDelta.addedArtists << bobDylan
        lineupDelta.deletedArtists << theCure
        assertTrue lineupDelta.hasChanges()

        // Same artist added and removed is not a change
        lineupDelta = new FestivalLineupDelta()
        lineupDelta.addedArtists << bobDylan
        lineupDelta.deletedArtists << bobDylan
        assertFalse lineupDelta.hasChanges()

        // Same artist added and removed for same date is not a change
        lineupDelta = new FestivalLineupDelta()
        Date today = new Date().clearTime()
        lineupDelta.addedArtists << new PerformanceDelta(name: 'Bob Dylan', id: 11, date: today)
        lineupDelta.deletedArtists << new PerformanceDelta(name: 'Bob Dylan', id: 11, date: today)
        assertFalse lineupDelta.hasChanges()

        // Same artist added and removed for different dates is a change
        lineupDelta.addedArtists.clear()
        lineupDelta.addedArtists << new PerformanceDelta(name: 'Bob Dylan', id: 11, date: ++today)
        assertTrue lineupDelta.hasChanges()
    }
}

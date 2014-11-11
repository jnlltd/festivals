package ie.festivals.notify


class ArtistNotificationTests extends GroovyTestCase {

    void testArtistWithoutDateDeletedThenAddedWithDate() {
        def notifications = new ArtistNotification()

        LinkData artist = new LinkData(id: 1, name: 'artist')
        LinkData festival = new LinkData(id: 1, name: 'festival')

        def deletedPerformer = new PerformanceNotification(artist: artist, festival: festival)
        notifications.deletedPerformances << deletedPerformer

        def addedPerformer = new PerformanceNotification(artist: artist, festival: festival, date: new Date())
        notifications.addedPerformances << addedPerformer

        assertEquals([addedPerformer] as Set, notifications.netAddedPerformances)
        assertTrue notifications.netDeletedPerformances.isEmpty()
    }
}

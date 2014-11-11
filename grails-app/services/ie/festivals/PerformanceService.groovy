package ie.festivals

import grails.transaction.Transactional

@Transactional(rollbackFor = Throwable)
class PerformanceService {

    ArtistService artistService

    Performance addPerformer(AddPerformerCommand command) {

        if (!command.artist.id) {
            command.artist = artistService.saveNew(command.artist)
        }
        savePerformance(command)
    }

    Performance addPerformerWithCustomImage(AddPerformerCommand command) {
        command.artist = command.artist.save(failOnError: true)

        try {
            savePerformance(command)
            artistService.saveArtistImagesLocally(command.artist, command.image)

        } catch (ex) {
            // It seems that domain objects are not automatically removed from the searchable index if the
            // transaction in which they were added is rolled back
            command.artist.unindex()
            throw ex
        }
    }

    private Performance savePerformance(AddPerformerCommand command) {
        Performance performance = new Performance(
                artist: command.artist,
                festival: command.festival,
                priority: command.priority,
                date: command.date)

        Integer hour = command.hour
        Integer minute = command.minute

        if (performance.date && hour != null && minute != null) {
            performance.date.set(hourOfDay: hour, minute: minute)
            performance.hasPerformanceTime = true
        }

        performance.save(failOnError: true)
    }
}

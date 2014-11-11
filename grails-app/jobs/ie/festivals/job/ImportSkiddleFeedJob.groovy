package ie.festivals.job

import grails.gsp.PageRenderer
import grails.plugin.geocode.GeocodingService
import groovy.util.slurpersupport.GPathResult
import ie.festivals.Artist
import ie.festivals.ArtistService
import ie.festivals.Festival
import ie.festivals.ImportAudit
import ie.festivals.Performance
import ie.festivals.xmlparser.SkiddleXmlFestivalParser
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.transaction.TransactionStatus

import static ie.festivals.enums.FestivalSource.SKIDDLE

class ImportSkiddleFeedJob {

    static triggers = {
        // Run daily at 6AM because feed is updated every 24h http://www.skiddle.com/affiliates/links.php
        cron cronExpression: '0 0 6 * * ?'
    }

    def concurrent = false

    GrailsApplication grailsApplication
    ArtistService artistService
    GeocodingService geocodingService
    PageRenderer groovyPageRenderer

    def execute() {
        log.info "$ImportSkiddleFeedJob.simpleName started at ${new Date()}"

        def skiddleConfig = grailsApplication.config.festival.skiddle

        URL feedUrl = skiddleConfig.feedUrl.toURL()
        Reader feedReader = feedUrl.newReader('ISO-8859-1')
        GPathResult skiddleXml = new XmlSlurper().parse(feedReader)

        def skiddleParser = new SkiddleXmlFestivalParser(artistService, geocodingService, skiddleConfig, groovyPageRenderer)
        Map<Long, Festival> parsedFestivals = skiddleParser.parse(skiddleXml)
        def importedFestivals = persistFestivals(parsedFestivals)

        log.info "$ImportSkiddleFeedJob.simpleName ended at ${new Date()}. $importedFestivals festivals were imported."
    }

    private Integer persistFestivals(Map<Long, Festival> parsedFestivals) {
        def importedFestivals = 0

        parsedFestivals.each { Long eventId, Festival festival ->

            // If we've previously parsed this event, ignore it
            if (!ImportAudit.findByEventIdAndSource(eventId, SKIDDLE)) {

                Festival.withTransaction { TransactionStatus status ->
                    try {
                        new ImportAudit(eventId: eventId, source: SKIDDLE).save(failOnError: true)

                        // Each day of a multi-day festival is represented as a separate event in the Skiddle XML file. Try to merge these into
                        // a single festival by looking for an existing Skiddle festival with the same name
                        Festival savedSkiddleFestival = Festival.createCriteria().get {
                            eq('name', festival.name)
                            eq('source', SKIDDLE)

                            // only consider future festivals because we don't want to update the end date of "Festival X" (last year)
                            // to the end date of "Festival X" (this year)
                            gt('start', new Date())
                        }

                        if (savedSkiddleFestival) {
                            // merge the festivals, i.e. update the start/end date of the existing (saved) festival
                            Date festivalDate = festival.start
                            log.debug "Merging festival $festival on $festivalDate with [id: $savedSkiddleFestival.id, name: $savedSkiddleFestival.name, start: $savedSkiddleFestival.start, end: $savedSkiddleFestival.end]"

                            savedSkiddleFestival.start = savedSkiddleFestival.start < festivalDate ? savedSkiddleFestival.start : festivalDate
                            savedSkiddleFestival.end = savedSkiddleFestival.end > festivalDate ? savedSkiddleFestival.end : festivalDate
                            savedSkiddleFestival.ticketInfo += festival.ticketInfo

                        } else {
                            // if we don't persist any new artist instances before saving the festival, we get
                            // an InvalidDataAccessApiUsageException
                            festival.performances?.each { Performance performance ->

                                // we might have already persisted this artist when saving a previous festival
                                // during this import
                                Artist artist = performance.artist
                                Artist savedArtist = Artist.findByMbid(artist.mbid)

                                if (savedArtist) {
                                    performance.artist = savedArtist
                                } else {
                                    artist.save(failOnError: true)
                                }
                            }
                            festival.save(failOnError: true)
                            importedFestivals++
                        }
                    } catch (ex) {
                        status.setRollbackOnly()
                        log.error "Failed to import festival '$festival.name' due to errors: $festival.errors", ex
                    }
                }
            }
        }
        importedFestivals
    }
}
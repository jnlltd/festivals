package ie.festivals

import grails.plugin.cache.Cacheable
import ie.festivals.competition.Competition
import ie.festivals.enums.FestivalType
import ie.festivals.notify.EmailSender
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.blog.BlogEntry

class HomeController {

    FestivalService festivalService
    ArtistService artistService

    def springSecurityService
    EmailSender emailSender
    def simpleCaptchaService
    GrailsApplication grailsApplication

    private static final W3C_DATE_FORMAT = "yyyy-MM-dd"

    def index() {
        // read the data for the festivals shown in the 3 tables
        def festivalCount = 5
        def upcomingFestivals = festivalService.nextFestivals(festivalCount)
        def freeFestivals = festivalService.nextFestivals(festivalCount, true)
        def musicFestivals = festivalService.nextFestivals(festivalCount, false, false, [FestivalType.MUSIC])

        Festival videoFestival = festivalService.videoFestival

        // read the data for the entries in the New Festivals/Updates sections
        def recentFestivalsCount = grailsApplication.config.festival.recentFestivalsCount
        def recentlyAddedFestivals = festivalService.recentlyAddedFestivals(recentFestivalsCount)
        def lineupChanges = festivalService.listFestivalsByPerformanceAdded(2)
        def recentlyAddedArtists = artistService.recentlyAddedArtist(2)

        def currentCompetitions = Competition.findAllByEndGreaterThanEquals(new Date().clearTime(), [cache: true])

        def recentChanges = [
                reviews      : festivalService.latestReviews(1),
                competitions : currentCompetitions,
                lineupChanges: lineupChanges,
                newFestivals : recentlyAddedFestivals,
                artists      : recentlyAddedArtists]

        def favoriteFestivals = festivalService.favoriteFestivals()

        render view: 'index', model: [
                upcomingFestivals: upcomingFestivals,
                freeFestivals    : freeFestivals,
                musicFestivals   : musicFestivals,
                video            : videoFestival,
                recentChanges    : recentChanges,
                favorites        : favoriteFestivals]
    }

    def contact(String subject) {
        [contact: new FeedbackCommand(subject: subject)]
    }

    def writeForUs() {
        [application: new FeedbackCommand()]
    }

    def sendWriterApplication(FeedbackCommand command) {

        boolean captchaValid = initFeedbackCommand(command)

        if (!command.validate() || !captchaValid) {
            log.debug "Writer application form submission has errors: $command.errors"
            flashHelper.warn captchaValid ? 'writer.error' : 'writer.captchaError'
            render view: 'writeForUs', model: [application: command]

        } else {
            command.subject = g.message(code: 'writer.subject', args: [command.name, command.email, command.subject])
            sendFeedbackEmail(command)
            flashHelper.info 'writer.success'
            redirect controller: 'home'
        }
    }

    /**
     * Set the user name and email fields
     * @param command
     * @return indicates whether the CAPTCHA was solved successfully
     */
    private boolean initFeedbackCommand(FeedbackCommand command) {
        User currentUser = springSecurityService.currentUser

        // registered users don't need to provide a name or email
        if (currentUser) {
            command.email = currentUser.username
            command.name = currentUser.name
        }

        // registered users are exempt from solving a CAPTCHA
        currentUser ?: simpleCaptchaService.validateCaptcha(params.captcha)
    }

    def sendFeedback(FeedbackCommand command) {

        boolean captchaValid = initFeedbackCommand(command)

        if (!command.validate() || !captchaValid) {
            log.debug "Feedback form submission has errors: $command.errors"
            flashHelper.warn captchaValid ? 'feedback.error' : 'feedback.captchaError'
            render view: 'contact', model: [contact: command]

        } else {
            command.subject = g.message(code: 'feedback.subject', args: [command.name, command.email, command.subject])
            sendFeedbackEmail(command)
            flashHelper.info 'feedback.success'
            redirect controller: 'home'
        }
    }

    private sendFeedbackEmail(FeedbackCommand command) {

        def feedbackEmail = grailsApplication.config.festival.feedbackEmail

        def mailArgs = {
            async true
            to feedbackEmail
            subject command.subject
            body command.message

            // Doesn't seem to make any difference when mail is sent via GMail
            replyTo command.email
        }

        emailSender.send(mailArgs)
    }

    /**
     * Get a link to a blog post
     * @param blogEntry The post to link to
     * @param absoluteUrl Indicates whether an absolute or relative URL should be generated
     * @param fragment Fragment to be appended to the URL
     * @return
     */
    private String getBlogPostUrl(BlogEntry blogEntry, boolean absoluteUrl, fragment = null) {
        def linkParams = [controller: "blog", action: "showEntry", absolute: absoluteUrl,
                          params    : [title: blogEntry.title, author: blogEntry.author]]

        if (fragment) {
            linkParams.fragment = fragment
        }
        g.createLink(linkParams)
    }

    private String formatW3CDate(Date date) {
        date.format(W3C_DATE_FORMAT)
    }

    @Cacheable('festival-default')
    def robots() {
        render(contentType: "text/plain", view: 'robots')
    }

    /**
     * Generates the content of the sitemap
     */
    @Cacheable('festival-default')
    def sitemap() {
        render(contentType: 'text/xml', encoding: 'UTF-8') {
            mkp.yieldUnescaped '<?xml version="1.0" encoding="UTF-8"?>'
            urlset(xmlns: "http://www.sitemaps.org/schemas/sitemap/0.9",
                    'xmlns:xsi': "http://www.w3.org/2001/XMLSchema-instance",
                    'xsi:schemaLocation': "http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd") {

                // Homepage
                url {
                    loc(g.createLink(absolute: true, controller: 'home'))
                    changefreq('daily')
                    priority(1.0)
                }

                // festival map, calendar, list
                [map: 0.9, calendar: 0.9, list: 0.7].each { action, importance ->
                    url {
                        loc(g.createLink(absolute: true, controller: 'festival', action: action))
                        changefreq('daily')
                        priority(importance)
                    }
                }

                // Check this is in synch with the corresponding entry in UrlMappings.groovy
                ['/about': 0.4, '/apiDocs': 0.7, '/writeForUs': 0.6].each { uri, importance ->
                    url {
                        loc(g.createLink(absolute: true, uri: uri))
                        changefreq('yearly')
                        priority(importance)
                    }
                }

                // Include all artist pages
                Artist.executeQuery("select a.id, a.lastUpdated, a.name from Artist a").each { artist ->
                    def artistId = artist[0]
                    def lastUpdated = formatW3CDate(artist[1])
                    def name = artist[2].encodeAsSeoName()

                    def params = [id: artistId, name: name]

                    url {
                        loc(g.createLink(absolute: true, controller: 'artist', action: 'show', params: params))
                        lastmod(lastUpdated)
                        changefreq('weekly')
                        priority(0.5)
                    }
                }

                // Include all approved festivals that have not finished
                festivalService.findAll(futureOnly: true).each { Festival fvl ->
                    def showFestivalUrl = festival.showFestivalUrl(festival: fvl, absolute: true)

                    url {
                        loc(showFestivalUrl)
                        lastmod(formatW3CDate(fvl.lastUpdated))
                        changefreq('weekly')
                        priority(0.8)
                    }
                }

                BlogEntry.list().each { BlogEntry post ->
                    url {
                        loc(getBlogPostUrl(post, true))
                        lastmod(formatW3CDate(post.lastUpdated))
                        changefreq('monthly')
                        priority(0.7)
                    }
                }

                Competition.findAllByEndGreaterThanEquals(new Date().clearTime()).each { Competition competition ->
                    url {
                        loc(g.createLink(absolute: true, controller: 'competition', action: 'show', params: [code: competition.code]))
                        changefreq('monthly')
                        priority(0.7)
                    }
                }
            }
        }
    }
}

class FeedbackCommand {
    String email
    String subject
    String message
    String name

    static constraints = {
        email(blank: false, email: true)
        message(blank: false)
        name(nullable: true)
        subject(nullable: true)
    }
}
package ie.festivals

import grails.util.Holders
import ie.festivals.enums.FestivalSource
import ie.festivals.enums.FestivalType
import ie.festivals.notify.FestivalSubscription
import ie.festivals.tag.EuropeTagLib
import ie.festivals.util.HtmlUtils
import org.apache.commons.lang.StringUtils
import static ie.festivals.util.StringUtils.isWebUrl

class Festival implements Cloneable, Named {

    @Lazy
    private skiddleConfig = grailsApplication.config.festival.skiddle

    // if types are defined for these beans, they need to be added to transients
    def springSecurityService
    def grailsApplication

    static final String ELLIPSE = "\u2026";

    static final CLONEABLE_PROPERTIES = [
            'name',
            'type',
            'website',
            'freeEntry',
            'hasLineup',
            'twitterUsername',
            'longitude',
            'latitude',
            'videoUrl',
            'countryName',
            'addressLine1',
            'addressLine2',
            'city',
            'region',
            'postCode',
            'countryCode',
            'synopsis'].asImmutable()

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy

    String name
    Festival previousOccurrence

    /**
     * This field allows the Skiddle affiliate ticket URL to festivals that were not imported from the
     * Skiddle feed. This should be used if a festival that has already been manually entered subsequently
     * appears in a Skiddle feed.
     */
    String skiddleUrl

    FestivalSource source = FestivalSource.HUMAN

    /**
     * If festival has an "early bird" price, this is the day it expires on. Some festivals offer multiple early bird
     * prices, but currently we only support one-at-a-time
     */
    Date earlyBirdExpiry
    Date start
    Date end
    FestivalType type
    String website
    Boolean freeEntry = false
    Boolean hasLineup = true
    String lineupUrl
    String twitterUsername
    String ticketInfo
    String synopsis

    /**
     * Transient property that indicates the distance in KMs of a point from the festival
     */
    Float distance
    Float longitude
    Float latitude
    Boolean approved = true

    String videoUrl

    /**
     * The name of the country is saved to the DB (as opposed to being resolved using countryCode), so that it can
     * be indexed by the searchable plugin
     */
    String countryName

    // Address fields as per the "Generic Formats" http://www.uxmatters.com/mt/archives/2008/06/international-address-fields-in-web-forms.php
    String addressLine1
    String addressLine2
    String city
    String region
    String postCode

    void setVideoUrl(String url) {
        this.videoUrl = url?.trim()
    }

    /**
     * 3-letter ISO country code
     */
    String countryCode

    @Override
    public Object clone() throws CloneNotSupportedException {

        Map<String, Object> source = this.properties
        Map cloneableProperties = source.findAll {
            it.key in CLONEABLE_PROPERTIES
        }
        Festival clone = new Festival(cloneableProperties)
        clone.previousOccurrence = this
        clone
    }

    static transients = ['fullAddress', 'multiDayDuration', 'finished', 'distance']

    // each festival can have multiple Skiddle IDs because consider each day of a multi-day festival as a separate event
    static hasMany = [
            performances : Performance,
            subscriptions: FestivalSubscription,
            favorites    : FavoriteFestival,
            ratings      : Rating,
            reminders    : Reminder,
            reviews      : Review
    ]

    static searchable = {
        mapping {
            spellCheck "include"
        }
        only = ['name', 'addressLine1', 'addressLine2', 'city', 'region', 'postCode', 'countryName', 'approved', 'type',
                'start', 'end']

        // hits on the name are more important than other fields
        name boost: 2.0

        // We want these fields included in the index because they're shown on the results page, but we don't want
        // them to be searchable
        start index: 'no', store: 'yes'
        end index: 'no', store: 'yes'
    }

    Boolean isFinished() {
        (new Date() - end) > 0
    }

    Boolean isMultiDayDuration() {

        if (end && start) {
            end - start > 0
        }
    }

    void setTicketInfo(String info) {
        // don't normalize the ticket info for imported festivals (Eventbrite, Skiddle, etc.)
        this.ticketInfo = source == FestivalSource.HUMAN ? HtmlUtils.normalize(info, null) : info
    }

    void setSynopsis(String info) {
        this.synopsis = HtmlUtils.normalize(info, null)
    }

    void setName(String festivalName) {
        this.name = festivalName?.trim()
    }

    void setAddressLine1(String line1) {
        this.addressLine1 = line1?.trim()
    }

    void setAddressLine2(String line2) {
        this.addressLine2 = line2?.trim()
    }

    void setTwitterUsername(String username) {

        username = username?.trim()

        if (username && username[0] == '@') {
            username = username[1..-1]
        }
        this.twitterUsername = username
    }

    void setEarlyBirdExpiry(Date earlyBird) {

        if (!freeEntry) {
            this.earlyBirdExpiry = earlyBird?.clearTime()
        }
    }

    void setFreeEntry(Boolean isFree) {

        if (isFree) {
            earlyBirdExpiry = null
        }
        this.freeEntry = isFree
    }

    void setStart(Date start) {
        this.start = start?.clearTime()
    }

    void setEnd(Date end) {
        this.end = end?.clearTime()
    }

    void setCity(String city) {
        city = city?.trim()
        this.city = city ?: null
    }

    String getFullAddress(boolean excludeCountry = false) {

        [addressLine1, addressLine2, city, region, postCode, countryName].with {
            if (excludeCountry) {
                pop()
            }

            retainAll { it?.trim() }
            join(', ')
        }
    }

    void setCountryCode(String code) {
        this.countryCode = code
        this.countryName = EuropeTagLib.ISO3166_3[code]

        // Ignore any post codes entered for ireland
        if (countryCode == 'irl') {
            postCode = null
        }
    }

    void setSkiddleUrl(String url) {
        url = url?.trim()

        if (url) {
            def ourSkiddleTag = skiddleConfig.tag
            url = StringUtils.replaceOnce(url, 'sktag=XXX', "sktag=$ourSkiddleTag")
            this.skiddleUrl = url
        }
    }

    void setPostCode(String code) {
        postCode = countryCode == 'irl' ? null : code
    }

    @Override
    String toString() {
        name
    }

    def beforeInsert() {
        if (source == FestivalSource.HUMAN) {
            createdBy = springSecurityService.currentUser
        }
    }

    static constraints = {

        // the festival author could be deleted after they add a festival
        createdBy nullable: true
        previousOccurrence nullable: true

        name blank: false, shared: 'unlimitedSize', validator: { name, self ->

            Integer maxNameLength = Holders.config.festival.utf8mb4MaxLength
            boolean nameTooLong = name.size() > maxNameLength

            // if the name of a festival imported from Eventbrite, Skiddle etc. is too long, truncate it
            if (nameTooLong && self.source != FestivalSource.HUMAN) {
                self.name = name[0..maxNameLength - 2] + ELLIPSE
            }

            return self.name.size() <= maxNameLength
        }

        end validator: { end, self ->

            if (end < self.start) {
                return 'before.start'
            }

            // end date can't be earlier than latest performer - only need to check this for updates
            if (self.id) {
                Date latestPerformance = Performance.createCriteria().get {

                    eq('festival', self)
                    eq('deleted', false)

                    projections {
                        max "date"
                    }
                }

                if (latestPerformance?.clearTime() > end) {
                    return 'before.last.performer'
                }
            }
        }

        start validator: { start, self ->
            // start date can't be after earliest performer - only need to check this for updates
            if (self.id) {
                def criteria = Performance.createCriteria()
                Date firstPerformer = criteria.get {

                    eq('festival', self)
                    eq('deleted', false)

                    projections {
                        min "date"
                    }
                }

                if (firstPerformer && firstPerformer < start) {
                    return 'after.first.performer'
                }
            }
        }

        earlyBirdExpiry nullable: true, validator: { earlyBird, self ->

            if (!earlyBird) {
                return true
            }

            // if we're creating a new festival then early bird should not be earlier than today
            // this constraint is questionable and we may want to remove it in future
            boolean newFestivalEarlyBirdExpired = !self.id && earlyBird < new Date().clearTime()

            if (newFestivalEarlyBirdExpired || self.end < earlyBird) {
                return 'badRange'
            }

            // free festival can't have an early bird price
            if (self.freeEntry) {
                return 'freeFestival'
            }
        }

        countryCode size: 3..3
        performances nullable: true, shared: 'unlimitedSize'
        subscriptions shared: 'unlimitedSize'
        favorites shared: 'unlimitedSize'
        ratings shared: 'unlimitedSize'
        reminders shared: 'unlimitedSize'
        reviews shared: 'unlimitedSize'

        // the Grails url constraint rejects some valid URLs because of their suffix, e.g. http://example.rocks
        // so use a custom URL validator instead https://jira.grails.org/browse/GRAILS-11764
        website nullable: true, shared: 'unlimitedSize', validator: {
            if (!isWebUrl(it, true)) {
                'url.invalid'
            }
        }

        lineupUrl nullable: true, shared: 'unlimitedSize', validator: {
            if (!isWebUrl(it, true)) {
                'url.invalid'
            }
        }

        skiddleUrl nullable: true, url: true
        ticketInfo nullable: true, shared: 'unlimitedSize'
        synopsis nullable: true, shared: 'unlimitedSize'

        twitterUsername nullable: true, validator: { username ->

            if (username && isWebUrl(username)) {
                return 'url.invalid'
            }
        }

        addressLine1 nullable: true
        addressLine2 nullable: true
        region nullable: true
        postCode nullable: true

        longitude nullable: true, range: -180..180, validator: { longVal, Festival self ->
            // The combination of this custom constraint and 'nullable: true' means regular users can provide null,
            // but admins must provide a value between -180 and 180
            if (longVal == null && self.approved) {
                'default.null.message'
            }
        }

        latitude nullable: true, range: -90..90, validator: { latVal, Festival self ->
            // The combination of this custom constraint and 'nullable: true' means regular users can provide null,
            // but admins must provide a value between -90 and 90
            if (latVal == null && self.approved) {
                'default.null.message'
            }
        }

        type nullable: true, validator: { FestivalType type, Festival self ->

            // Type is optional if festival was parsed from Skiddle XML file because there's no way to
            // determine it in this case. When approving a Skiddle festival, type must be provided
            if (!type && self.approved) {
                'default.null.message'
            }
        }

        city nullable: true, validator: { String city, Festival self ->

            // Some Eventbrite festivals don't provide a city, but when approving a festival, a city must be specified
            if (!city && self.approved) {
                'default.null.message'
            }
        }

        videoUrl nullable: true, matches: 'http://www.youtube.com/embed/.+'
    }

    static mapping = {
        ticketInfo type: 'text'
        synopsis type: 'text'
        website type: 'text'

        def maxEnumLength = Holders.config.festival.utf8mb4MaxLength

        // Add an index to improve queries. Another candidates is freeEntry but as I don't really know whether I
        // should define individual indices for each column or compound indices, keep it simple.
        type index: 'type_index', length: maxEnumLength
        countryCode index: 'country_index'
        source length: maxEnumLength

        cache true
    }
}

package ie.festivals.tag

import grails.util.GrailsUtil
import ie.festivals.Festival
import ie.festivals.enums.FestivalType
import ie.festivals.map.MapFocalPoint
import ie.festivals.util.HtmlUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

class FestivalTagLib {

    static namespace = "festival"

    static final DESCRIPTION_DATE_FORMAT = 'd MMM yyyy'

    // this pattern will expire in 2100, I think I'm fine with that
    private static final Pattern DATE_PATTERN = ~/(.+)(20\d\d)/

    /**
     * Creates a link to the map
     *
     * @attr location Defaults to Ireland if omitted
     * @attr types Festival types, defaults to all if omitted
     * @attr freeOnly Indicates whether only free festivals should be displayed. Default to false if omitted
     */
    def mapLink = { attrs, body ->
        def location = attrs.remove('location') ?: MapFocalPoint.IRELAND
        def types = attrs.remove('types') ?: FestivalType.values()
        types = types*.name()

        def params = [location: location.name(), types: types]
        if (attrs.freeOnly != null) {
            params.freeOnly = attrs.remove('freeOnly').asBoolean()
        }

        attrs += [controller: "festival", action: "map", params: params]
        out << g.link(attrs, body)
    }

    /**
     * Creates a link that will search for festivals by type
     *
     * @attr type the <tt>FestivalType</tt> to search for
     */
    def typeSearch = { attrs, body ->

        FestivalType type = attrs.remove('type')

        // Using attrs in this way ensures that any unhandled attributes (class, id, etc.) will ultimately be passed
        // to the generated <a> tag
        attrs.params = [query: type.searchToken]
        attrs.controller = 'search'
        attrs.action = 'festivalTypeSearch'

        out << g.link(attrs, body)
    }

    def listLink = { attrs, body ->
        def types = attrs.type ? [attrs.remove('type').name()] : FestivalType.values().collect { it.name() }
        def location = attrs.remove('location') ?: MapFocalPoint.EUROPE
        Boolean freeOnly = attrs.remove('freeOnly') ?: false

        attrs.params = [types: types, location: location.name(), freeOnly: freeOnly]
        attrs.controller = "festival"
        attrs.action = "list"

        out << g.link(attrs, body)
    }

    /**
     * Generates a link to the show festival page
     * @attr festival if this argument is provided all others are ignored
     * @attr id this argument is required unless the festival argument is supplied
     * @attr name if included a more SEO-friendly URL will be generated that includes the festival's name
     * @attr type if included a more SEO-friendly URL will be generated that includes the festival's type
     * @attr useSchemaDotOrgProps
     */
    def show = { attrs, body ->

        log.trace "Creating festival link for params $attrs"
        Festival festival = attrs.festival

        // If name or type are missing we'll need to load the festival to get them, unless the festival
        // itself has been supplied as an argument
        if (!festival && (!attrs.name || !attrs.type)) {
            def id = removeMandatoryAttribute(attrs, 'id')
            festival = Festival.read(id)

            // Ignore attempts to show missing festivals in dev mode
            if (GrailsUtil.developmentEnv && !festival) {
                log.warn "<$namespace:show> invoked with invalid ID: $attrs.id"
                return
            }

            attrs.festival = festival
        }
        boolean useSchemaDotOrgProps = attrs.remove('useSchemaDotOrgProps')
        attrs = getGrailsLinkTagAttributes(attrs)

        def name = festival.name.encodeAsHTML()

        // schema.org event markup http://schema.org/Event
        if (useSchemaDotOrgProps) {
            attrs.itemprop = 'url'
            name = "<span itemprop='name'>$name</span>"
        }

        // If tag is invoked without a body the name will be used as the link body
        out << g.link(attrs, body ?: name)
    }

    private Map getLinkAttrs(Map attrs) {

        def id = removeMandatoryAttribute(attrs, 'id')
        def name = attrs.remove('name')
        def type = attrs.remove('type')

        def urlParams = [id: id, name: name?.encodeAsSeoName(), type: type?.name()?.encodeAsSeoName()]
        attrs + [action: 'show', controller: 'festival', params: urlParams]
    }

    private removeMandatoryAttribute(attrs, String attrName) {
        def attrValue = attrs.remove(attrName)

        if (!attrValue) {
            throwTagError("tag requires a '$attrName' attribute")
        }
        attrValue
    }
    /**
     * Returns the URL of a festival
     * @attr festival if this argument is provided all others are ignored
     * @attr id this argument is required unless the festival argument is supplied
     * @attr name if included a more SEO-friendly URL will be generated that includes the festival's name
     * @attr type if included a more SEO-friendly URL will be generated that includes the festival's type
     */
    def showFestivalUrl = { attrs ->

        log.trace "Retrieving festival URL for params $attrs"
        def grailsLinkTagAttrs = getGrailsLinkTagAttributes(attrs)
        def url = g.createLink(grailsLinkTagAttrs)
        log.trace "Returning festival URL: $url"

        out << url
    }

    private Map getGrailsLinkTagAttributes(attrs) {
        Festival festival = attrs.remove('festival')

        if (festival) {
            attrs += [id: festival.id, name: festival.name, type: festival.type, mapping: 'showFestival']
        }
        getLinkAttrs(attrs)
    }

    def getDescription = { attrs ->
        Festival festival = attrs.festival

        if (festival.synopsis) {
            def text = HtmlUtils.removeTags(festival.synopsis)

            if (text) {
                // replace multiple spaces with single space
                out << text.trim().replaceAll(" +", " ")
                return
            }
        }
        out << "${festival.type} festival in ${festival.city.encodeAsHTML()} starting on ${festival.start.format(DESCRIPTION_DATE_FORMAT)}"
    }

    def concatenateNameAndYear = { attrs ->

        Festival festival = attrs.festival
        def name = festival.name.trim()

        Calendar cal = Calendar.instance
        cal.time = festival.start
        def year = cal[Calendar.YEAR]

        // check if the year is appended to the name
        Matcher yearMatcher = DATE_PATTERN.matcher(name)

        def festivalName = yearMatcher ? yearMatcher[0][1].trim() + ', ' + yearMatcher[0][2] : "$name, $year"
        out << festivalName
    }
}

package ie.festivals

import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine
import net.sf.json.test.JSONAssert

import static ie.festivals.enums.FestivalType.*
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import static javax.servlet.http.HttpServletResponse.SC_OK

class ApiControllerTests extends GroovyTestCase {

    FestivalService festivalService

    Artist futurePerformer
    Artist pastPerformer

    private ApiController getController() {
        new ApiController(festivalService: festivalService)
    }

    /**
     * We need to explicitly test the interceptor because Grails does not invoke interceptors or servlet filters when
     * calling actions during integration testing
     */
    void testAuthenticationInterceptor() {

        User apiUser = User.build(apiKey: 'apiKey')
        ApiController apiController = getController()

        // no params should fail authentication
        assertFalse apiController.beforeInterceptor()

        // valid user should authenticate
        apiController.params.key = apiUser.apiKey
        apiController.params.user = apiUser.username
        assertTrue apiController.beforeInterceptor()

        // bad key only
        apiController.params.key = 'not valid'
        apiController.params.user = apiUser.username
        assertFalse apiController.beforeInterceptor()

        // bad username only
        apiController.params.key = apiUser.apiKey
        apiController.params.user = 'not valid'
        assertFalse apiController.beforeInterceptor()
    }

    void testFestivalGeoSearchMissingParams() {

        // lat, lng and radius params are required
        ApiController apiController = getController()
        apiController.request.method = 'GET'
        apiController.festivalGeoSearch()
        assertEquals SC_BAD_REQUEST, apiController.response.status
    }

    private List<Festival> saveTwoNearbyFestivalsAndOneFaraway() {

        List<Festival> festivals = []

        // create 2 festivals < 30km from lat: 53, lng: -7 and 1 festival > 30km from this point
        festivals << Festival.build(latitude: 53.01, longitude: -7.01, name: 'nearby', type: FILM)
        festivals << Festival.build(latitude: 53.02, longitude: -7.02, name: 'nearby2', type: SPORT)
        festivals << Festival.build(latitude: 1, longitude: 1, name: 'faraway', countryCode: 'rus', type: FOOD_AND_DRINK)

        futurePerformer = Artist.build(name: 'nick')
        pastPerformer = Artist.build(name: 'tom')

        // nick is appearing twice at nearby and faraway once
        Festival nearby =  festivals.first()
        Performance.build(artist: futurePerformer, festival: nearby, date: new Date() + 1)
        Performance.build(artist: futurePerformer, festival: nearby, date: new Date() + 2)

        Festival faraway = festivals.last()
        Performance.build(artist: futurePerformer, festival: faraway, date: new Date() + 1)

        // tom appeared at nearby2 on a past date
        Festival nearby2 = festivals[1]
        Performance.build(artist: pastPerformer, festival: nearby2, date: new Date() - 2)

        festivals
    }

    void testFestivalDetailInvalidId() {
        ApiController controller = getController()
        controller.params.id = Integer.MAX_VALUE
        controller.festivalDetail()
        assertEquals SC_BAD_REQUEST, controller.response.status
    }

    void testFestivalDetail() {

        // we can't use new Date() here, because otherwise the expected JSON will become invalid as the date changes
        Date startDate = Date.parse('yyyy-MM-dd', '2014-01-01')

        Festival festival = Festival.build(latitude: 53.01, longitude: -7.01, name: 'nearby', start: startDate, end: startDate + 2)
        Artist futurePerformer = Artist.build(name: 'nick')
        Performance.build(artist: futurePerformer, festival: festival, date: startDate + 1)
        Performance.build(artist: futurePerformer, festival: festival, date: startDate + 2)

        String jsonResponse = invokeAction(([id: festival.id]), {it.festivalDetail()}, false)

        // We need to make a template for the expected output and bind the actual IDs into it to generate
        // the actual expected output.
        Long artistId = futurePerformer.id

        String templateText = getClass().getResourceAsStream('expectedFestivalDetailResponse.tmpl').text
        def templateBinding = [
                festivalId: festival.id,
                artistId: artistId
        ]
        def templateOutput = new SimpleTemplateEngine().createTemplate(templateText).make(templateBinding)
        String expectedJsonResponse = templateOutput.toString()

        JSONAssert.assertJsonEquals(expectedJsonResponse, jsonResponse)
    }

    void testFestivalsByArtistMissingArtistId() {
        ApiController apiController = getController()
        apiController.festivalsByArtist()
        assertEquals SC_BAD_REQUEST, apiController.response.status
    }

    void testFestivalsByArtistWithFuturePerformer() {

        saveTwoNearbyFestivalsAndOneFaraway()
        List results = invokeAction([id: futurePerformer.id]) {it.festivalsByArtist()}

        // artist is performing twice at nearby festival, and once at faraway but duplicate festivals should be excluded
        assertEquals 2, results.size()
        Set<String> expectedFestivalNames = ['nearby', 'faraway']
        assertEquals expectedFestivalNames, results.name.toSet()
    }

    void testFestivalsByArtistWithPastPerformerFromCurrentFestival() {

        saveTwoNearbyFestivalsAndOneFaraway()
        List results = invokeAction([id: pastPerformer.id]) {it.festivalsByArtist()}

        // artist is performing at a current festival but their performance is over
        assertTrue results.empty
    }

    void testFestivalsByArtistWithPastPerformerFromPastFestival() {

        def festival = Festival.build(start: new Date() -5, end: new Date() - 3)
        def artistWithDate = Artist.build()
        def artistWithoutDate = Artist.build()

        Performance.build(artist: artistWithDate, festival: festival, date: new Date() -4)
        Performance.build(artist: artistWithoutDate, festival: festival)

        // festival is in the past so no artists should be returned
        assertTrue invokeAction([id: artistWithDate.id]) {it.festivalsByArtist()}.empty
        assertTrue invokeAction([id: artistWithoutDate.id]) {it.festivalsByArtist()}.empty
    }

    void testFestivalGeoSearchValidParams() {

        saveTwoNearbyFestivalsAndOneFaraway()
        def params = [latitude: 53, longitude: -7, radius: 30]
        List results = invokeAction(params) {it.festivalGeoSearch()}
        assertEquals 2, results.size()

        Set<String> expectedMatches = ['nearby', 'nearby2']
        assertEquals expectedMatches, results.name.toSet()
    }

    void testFestivalGeoSearchWithTypeRestriction() {

        saveTwoNearbyFestivalsAndOneFaraway()
        def params = [latitude: 53, longitude: -7, radius: 30, types: FILM.name()]
        List results = invokeAction(params) {it.festivalGeoSearch()}
        assertEquals 1, results.size()
    }

    void testFestivalGeoSearchWithLimit() {
        def festivals = saveTwoNearbyFestivalsAndOneFaraway()
        def params = [latitude: 53, longitude: -7, radius: 30, max: 1]
        List oneResult = invokeAction(params) {it.festivalGeoSearch()}
        assertEquals 1, oneResult.size()
        assertEquals festivals[0].name, oneResult[0].name
    }

    void testFestivalGeoSearchWithLimitAndOffset() {
        def festivals = saveTwoNearbyFestivalsAndOneFaraway()
        def params = [latitude: 53, longitude: -7, radius: 30, max: 1, offset: 1]
        List oneResult = invokeAction(params) {it.festivalGeoSearch()}
        assertEquals 1, oneResult.size()
        assertEquals festivals[params.offset].name, oneResult[0].name
    }

    void testFestivalSearchWithoutParams() {
        def allFestivals = saveTwoNearbyFestivalsAndOneFaraway()
        List results = invokeAction([:]) {it.festivalSearch()}
        assertEquals allFestivals.size(), results.size()
    }

    void testFestivalSearchWithCountryRestriction() {
        saveTwoNearbyFestivalsAndOneFaraway()
        def params = [location: 'IRELAND']
        List results = invokeAction(params) {it.festivalSearch()}
        assertEquals 2, results.size()
    }

    void testFestivalSearchWithTypeRestriction() {
        saveTwoNearbyFestivalsAndOneFaraway()
        def params = [types: 'FOOD_AND_DRINK']
        List results = invokeAction(params) {it.festivalSearch()}
        assertEquals 1, results.size()
    }

    void testFestivalSearchWithLimit() {
        saveTwoNearbyFestivalsAndOneFaraway()
        List results = invokeAction([max: 2]) {it.festivalSearch()}
        assertEquals 2, results.size()
    }

    void testFestivalSearchWithLimitAndOffset() {
        def festivals = saveTwoNearbyFestivalsAndOneFaraway()
        List results = invokeAction([max: 1, offset: 1]) {it.festivalSearch()}
        assertEquals 1, results.size()
        assertEquals festivals[1].name, results[0].name
    }

    private invokeAction(Map params, Closure apiMethod, boolean parseJsonResponse = true) {

        ApiController apiController = getController()
        apiController.params << params
        apiController.request.method = 'GET'

        apiMethod(apiController)
        assertEquals SC_OK, apiController.response.status
        def jsonResponse = apiController.response.contentAsString
        parseJsonResponse ? new JsonSlurper().parseText(jsonResponse) : jsonResponse
    }

}
package ie.festivals

import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor

@TestFor(Festival)
@Build([Festival, Performance])
class FestivalUnitTests {

    void testFullAddressFormat() {
        Festival festival = new Festival(city: 'city', countryName: 'country')
        assertEquals 'city, country', festival.getFullAddress(false)
        assertEquals 'city', festival.getFullAddress(true)
    }

    private Festival buildFestival(Map festivalProperties = [:]) {

        festivalProperties.springSecurityService = [currentUser: new User()]
        festivalProperties.grailsApplication = [config: [festival: [skiddle: null]]]

        Festival.build(festivalProperties)
    }

    void testFestivalWithLongUrl() {
        def url = 'http://example.org/' + ('f' * 200)
        Festival festival = buildFestival(website: url)
        assertTrue festival.validate()
    }


    void testValidateTwitterUsername() {

        Festival festival = buildFestival(twitterUsername: '@test')
        assertTrue festival.validate()

        festival.twitterUsername = null
        assertTrue festival.validate()

        festival.twitterUsername = 'http://twitter.com/test'
        assertFalse festival.validate()
    }

    void testClone() {

        Festival original = buildFestival()
        Festival clone = original.clone()

        // check that all the cloneable properties match
        Festival.CLONEABLE_PROPERTIES.each { String propertyName ->
            assertEquals original."$propertyName", clone."$propertyName"
        }

        // check that a couple of the mandatory cloneable properties are not null
        assertNotNull clone.name
        assertNotNull clone.type

        // check a couple of the non-cloneable properties
        assertNull clone.start
        assertNull clone.performances

        assertTrue original.is(clone.previousOccurrence)
    }
}

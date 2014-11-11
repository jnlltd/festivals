package ie.festivals

import grails.test.mixin.TestFor

import static org.junit.Assert.assertNotNull

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ImageService)
class ImageServiceTests {

    void testRead() {
        def troublesomeImages = ['bike.jpg', 'josh.jpg']

        troublesomeImages.each {
            URL url = getClass().getResource(it)
            assertNotNull service.read(url.toString())
        }
    }
}
package ie.festivals.databinding

import ie.festivals.Festival


class DomainClassConverterTests extends GroovyTestCase {

    DomainClassConverter domainClassConverter

    void testConversion() {
        assertEquals Festival, domainClassConverter.convert(Festival.simpleName)
        assertNull domainClassConverter.convert('noSuchClass')
    }
}

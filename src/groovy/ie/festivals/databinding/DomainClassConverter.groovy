package ie.festivals.databinding

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.grails.databinding.converters.ValueConverter

import javax.annotation.PostConstruct

/**
 * Converts the simple name of a domain class to the corresponding class instance. This will not work if the domain
 * has several classes with the same simple name
 */
class DomainClassConverter implements ValueConverter {

    GrailsApplication grailsApplication
    private Map<String, Class<?>> domainClasses = [:]

    @PostConstruct
    private initializeDomainClassMap() {
        grailsApplication.domainClasses.each { GrailsDomainClass domainClass ->
            domainClasses[domainClass.clazz.simpleName] = domainClass.clazz
        }
    }

    @Override
    boolean canConvert(Object value) {
        value in domainClasses.keySet()
    }

    @Override
    Object convert(Object value) {
        domainClasses[value]
    }

    @Override
    Class<?> getTargetType() {
        Class
    }
}

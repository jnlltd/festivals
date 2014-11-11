package ie.festivals.xmlparser

import groovy.util.slurpersupport.GPathResult

public interface XmlParser<T> {
    T parse(GPathResult xmlResponse)
}
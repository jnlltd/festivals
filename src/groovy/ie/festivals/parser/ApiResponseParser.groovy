package ie.festivals.parser

import groovy.util.slurpersupport.GPathResult

public interface ApiResponseParser<T> {
    T parse(GPathResult response)
}
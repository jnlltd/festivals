package ie.festivals.parser

import groovy.util.slurpersupport.GPathResult


public interface ApiResponseParser<T, U> {
    T parse(U response)
}
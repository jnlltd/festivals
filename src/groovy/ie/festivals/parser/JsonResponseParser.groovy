package ie.festivals.parser

import groovy.util.slurpersupport.GPathResult


interface JsonResponseParser<T> extends ApiResponseParser<T, Map> {

    @Override
    T parse(Map response)
}

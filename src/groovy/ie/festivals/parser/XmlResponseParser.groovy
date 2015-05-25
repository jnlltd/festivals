package ie.festivals.parser

import groovy.util.slurpersupport.GPathResult

public interface XmlResponseParser<T> extends ApiResponseParser<T, GPathResult> {

    @Override
    T parse(GPathResult response)
}
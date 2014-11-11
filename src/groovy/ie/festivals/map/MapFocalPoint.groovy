package ie.festivals.map

import grails.plugin.geocode.Point

public enum MapFocalPoint {
    IRELAND(53.22F, -7.82F, 7, "Ireland", "irl"),
    UK(54.2F, -1.89F, 6, "UK", "gbr"),
    EUROPE(53.2F, 6F, 4, "Europe")

    MapFocalPoint(Float latitude, Float longitude, Integer zoom, String name, String code = null) {
        location = new Point(latitude: latitude, longitude: longitude)
        zoomLevel = zoom
        displayName = name
        countryCode = code
    }

    final Point location
    final Integer zoomLevel
    final String displayName
    final String countryCode

    static MapFocalPoint fromCountryCode(String code) {
        MapFocalPoint mapFocalPoint = MapFocalPoint.values().find {it.countryCode == code}
        mapFocalPoint ?: EUROPE
    }
}
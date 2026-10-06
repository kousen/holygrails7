package com.kousenit

import groovy.json.JsonSlurper

/**
 * Looks up a castle's coordinates from its town using the Open-Meteo geocoding
 * API (https://open-meteo.com/en/docs/geocoding-api): free, no key, plain JSON.
 */
class GeocoderService {

    static final String BASE = 'https://geocoding-api.open-meteo.com/v1/search'

    Castle fillInLatLng(Castle castle) {
        Map place = lookup(castle.city)
        if (place) {
            castle.latitude = place.latitude as Double
            castle.longitude = place.longitude as Double
        }
        castle
    }

    Map lookup(String city) {
        String url = "$BASE?name=${URLEncoder.encode(city, 'UTF-8')}&count=1"
        Map response = new JsonSlurper().parse(url.toURL()) as Map
        (response.results as List<Map>)?.find()
    }
}

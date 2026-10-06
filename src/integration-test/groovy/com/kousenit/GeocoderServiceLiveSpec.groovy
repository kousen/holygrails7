package com.kousenit

import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * Calls the real Open-Meteo API. Set OFFLINE=1 in the environment to skip it,
 * for example on conference wifi.
 */
@Integration
@IgnoreIf({ env.OFFLINE })
class GeocoderServiceLiveSpec extends Specification {

    GeocoderService geocoderService

    void "Doune is where we left it"() {
        when:
        Map place = geocoderService.lookup('Doune')

        then:
        place.country == 'United Kingdom'
        (place.latitude - 56.19).abs() < 0.05
        (place.longitude - -4.05).abs() < 0.05
    }

    void "a town the Bridgekeeper has never heard of returns nothing"() {
        expect:
        geocoderService.lookup('Nowheresville') == null
    }
}

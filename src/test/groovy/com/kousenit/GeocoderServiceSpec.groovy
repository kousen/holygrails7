package com.kousenit

import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class GeocoderServiceSpec extends Specification
        implements ServiceUnitTest<GeocoderService>, DomainUnitTest<Castle> {

    Castle camelot = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland')

    void "coordinates are copied from the first result"() {
        given: 'a service whose lookup never touches the network'
        GeocoderService geocoder = Spy(GeocoderService) {
            lookup('Doune') >> [name: 'Doune', latitude: 56.18995, longitude: -4.05288]
        }

        when:
        geocoder.fillInLatLng(camelot)

        then:
        camelot.latitude == 56.18995d
        camelot.longitude == -4.05288d
        camelot.validate()
    }

    void "an unknown town leaves the coordinates alone"() {
        given:
        GeocoderService geocoder = Spy(GeocoderService) {
            lookup(_) >> null
        }
        Castle anthrax = new Castle(name: 'Castle Anthrax', city: 'Nowheresville', country: 'Scotland')

        when:
        geocoder.fillInLatLng(anthrax)

        then:
        anthrax.latitude == null
        anthrax.longitude == null
    }

    void "the service returns the castle so calls can chain"() {
        given:
        GeocoderService geocoder = Spy(GeocoderService) { lookup(_) >> null }

        expect:
        geocoder.fillInLatLng(camelot).is(camelot)
    }
}

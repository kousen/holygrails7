package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class CastleSpec extends Specification implements DomainUnitTest<Castle> {

    Castle doune = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland')

    void "coordinates are optional until the geocoder fills them in"() {
        expect:
        doune.validate()
        doune.latitude == null
    }

    void "coordinates must be on the planet"() {
        when:
        doune.latitude = 91
        doune.longitude = -181

        then:
        !doune.validate()
        doune.errors['latitude'].code == 'range.toobig'
        doune.errors['longitude'].code == 'range.toosmall'
    }
}

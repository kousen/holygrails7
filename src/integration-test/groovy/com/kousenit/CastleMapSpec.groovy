package com.kousenit

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.cleanup.core.DatabaseCleanup

/**
 * Drives a real browser, running in a Docker container, against the running
 * application. Functional tests see the real database and are not rolled
 * back, so this spec seeds what it needs and @DatabaseCleanup truncates the tables after each test.
 */
@Integration
@DatabaseCleanup
class CastleMapSpec extends ContainerGebSpec {

    def setup() {
        Castle.withNewTransaction {
            SeedData.theCourt(SeedData.seekTheGrail())
        }
    }

    void 'the castle list shows every castle with coordinates on the map'() {
        when: 'visiting the castle list'
        go '/castle'

        then: 'the page is the scaffolded list'
        title == 'Castle List'

        and: 'Leaflet has drawn one marker per castle'
        waitFor { $('#map .leaflet-marker-icon').size() == 3 }

        when: 'clicking the first marker'
        $('#map .leaflet-marker-icon', 0).click()

        then: 'its popup names the castle and links to it'
        waitFor { $('.leaflet-popup-content').text().contains('knight(s)') }
        $('.leaflet-popup-content a').text() in ['Camelot', 'Castle Aaargh', 'Swamp Castle']
    }
}

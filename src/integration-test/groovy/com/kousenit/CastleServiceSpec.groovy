package com.kousenit

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import spock.lang.Specification

@Integration
@Rollback
class CastleServiceSpec extends Specification {

    CastleService castleService
    SessionFactory sessionFactory

    private Long setupData() {
        new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland').save(flush: true, failOnError: true)
        new Castle(name: 'Castle Aaargh', city: 'Port Appin', country: 'Scotland').save(flush: true, failOnError: true)
        Castle castle = new Castle(name: 'Swamp Castle', city: 'Robertsbridge', country: 'England').save(flush: true, failOnError: true)
        new Castle(name: 'Castle Anthrax', city: 'Doune', country: 'Scotland').save(flush: true, failOnError: true)
        new Castle(name: 'The French Castle', city: 'Doune', country: 'Scotland').save(flush: true, failOnError: true)
        castle.id
    }

    void "test get"() {
        Long castleId = setupData()

        expect:
        castleService.get(castleId) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Castle> castleList = castleService.list(max: 2, offset: 2)

        then:
        castleList.size() == 2
    }

    void "test count"() {
        setupData()

        expect:
        castleService.count([:]) == 5
    }

    void "test delete"() {
        Long castleId = setupData()

        expect:
        castleService.count([:]) == 5

        when:
        castleService.delete(castleId)
        sessionFactory.currentSession.flush()

        then:
        castleService.count([:]) == 4
    }

    void "test save"() {
        when:
        Castle castle = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland')
        castleService.save(castle)

        then:
        castle.id != null
    }
}

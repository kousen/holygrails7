package com.kousenit

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import spock.lang.Specification

@Integration
@Rollback
class KnightServiceSpec extends Specification {

    KnightService knightService
    SessionFactory sessionFactory

    private Long setupData() {
        new Knight(title: 'King', name: 'Arthur', favouriteColour: 'Blue').save(flush: true, failOnError: true)
        new Knight(name: 'Lancelot the Brave', favouriteColour: 'Blue').save(flush: true, failOnError: true)
        Knight knight = new Knight(name: 'Galahad the Pure').save(flush: true, failOnError: true)
        new Knight(name: 'Robin the Not-Quite-So-Brave-as-Sir-Lancelot', favouriteColour: 'Yellow').save(flush: true, failOnError: true)
        new Knight(name: 'Bedevere the Wise', favouriteColour: 'Green').save(flush: true, failOnError: true)
        knight.id
    }

    void "test get"() {
        Long knightId = setupData()

        expect:
        knightService.get(knightId) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Knight> knightList = knightService.list(max: 2, offset: 2)

        then:
        knightList.size() == 2
    }

    void "test count"() {
        setupData()

        expect:
        knightService.count() == 5
    }

    void "test delete"() {
        Long knightId = setupData()

        expect:
        knightService.count() == 5

        when:
        knightService.delete(knightId)
        sessionFactory.currentSession.flush()

        then:
        knightService.count() == 4
    }

    void "test save"() {
        when:
        Knight knight = new Knight(name: 'Lancelot the Brave', favouriteColour: 'Blue')
        knightService.save(knight)

        then:
        knight.id != null
    }
}

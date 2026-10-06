package com.kousenit

import grails.testing.gorm.DataTest
import spock.lang.Specification

class EnemySpec extends Specification implements DataTest {

    Quest quest = new Quest(name: 'Seek the grail')

    void setupSpec() {
        mockDomains Quest, Enemy, BlackKnight, RabbitOfCaerbannog, Bridgekeeper
    }

    void "subclasses inherit the base constraints"() {
        expect:
        !new BlackKnight(name: ' ', location: 'A bridge', quest: quest).validate()
        new BlackKnight(name: 'The Black Knight', location: 'A bridge', quest: quest).validate()
    }

    void "each enemy has its own challenge"() {
        expect:
        new BlackKnight().challenge() == 'None shall pass'
        new BlackKnight(limbsRemaining: 0).challenge() == "All right, we'll call it a draw"
        new RabbitOfCaerbannog().challenge() == 'Look at the bones!'
        new Bridgekeeper().challenge().startsWith('Stop!')
    }

    void "a flesh wound is a matter of degree"() {
        expect:
        new BlackKnight(limbsRemaining: 1).merelyAFleshWound
        !new BlackKnight(limbsRemaining: 0).merelyAFleshWound
        !new BlackKnight(limbsRemaining: 5).validate()
    }

    void "the Bridgekeeper asks at most three questions"() {
        when:
        Bridgekeeper keeper = new Bridgekeeper(name: 'The Bridgekeeper', location: 'The Bridge of Death', quest: quest,
                questions: ['name', 'quest', 'colour', 'airspeed velocity of an unladen swallow'])

        then:
        !keeper.validate()
        keeper.errors['questions'].code == 'maxSize.exceeded'
    }

    void "queries on the base class are polymorphic"() {
        given:
        quest.save(failOnError: true)
        SeedData.theOpposition(quest)

        expect:
        Enemy.count() == 4
        BlackKnight.count() == 2
        Enemy.findAllByDefeated(false)*.class*.simpleName.sort() == ['BlackKnight', 'Bridgekeeper', 'RabbitOfCaerbannog']
        Enemy.list().collect { it.challenge() }.every { it }
    }
}

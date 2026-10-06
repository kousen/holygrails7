package com.kousenit

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class EnemyQueriesSpec extends Specification {

    private Quest seed() {
        Quest quest = SeedData.seekTheGrail()
        SeedData.theOpposition(quest)
        quest
    }

    void "one table holds the whole hierarchy, and GORM restores the right class"() {
        seed()

        when:
        List<Enemy> enemies = Enemy.list(sort: 'name')

        then:
        enemies*.class*.simpleName == ['BlackKnight', 'BlackKnight', 'Bridgekeeper', 'RabbitOfCaerbannog']
        enemies.find { it instanceof RabbitOfCaerbannog }.knightsEaten == 3
        enemies.find { it instanceof Bridgekeeper }.questions.size() == 3
    }

    void "finders on a subclass only see that subclass"() {
        seed()

        expect:
        BlackKnight.findAllByDefeated(false)*.name == ['The Black Knight']
        RabbitOfCaerbannog.findByKnightsEatenGreaterThan(0).name == 'The Rabbit of Caerbannog'
        Enemy.countByDefeated(true) == 1
    }

    void "where queries can mix base and subclass properties"() {
        seed()

        when:
        def undefeatedBlackKnights = BlackKnight.where { defeated == false && limbsRemaining > 0 }

        then:
        undefeatedBlackKnights.count() == 1
    }

    void "an enemy belongs to its quest"() {
        Quest quest = seed()

        expect:
        Enemy.findAllByQuest(quest).size() == 4
        Enemy.list().every { it.quest == quest }
    }
}

package com.kousenit

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import spock.lang.Specification

@Integration
@Rollback
class QuestServiceSpec extends Specification {

    QuestService questService
    SessionFactory sessionFactory

    private Long setupData() {
        new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
        new Quest(name: 'Find a shrubbery').save(flush: true, failOnError: true)
        Quest quest = new Quest(name: 'Cross the Bridge of Death').save(flush: true, failOnError: true)
        new Quest(name: 'Storm the Castle Anthrax').save(flush: true, failOnError: true)
        new Quest(name: 'Consult Tim the Enchanter').save(flush: true, failOnError: true)
        quest.id
    }

    void "test get"() {
        Long questId = setupData()

        expect:
        questService.get(questId) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Quest> questList = questService.list(max: 2, offset: 2)

        then:
        questList.size() == 2
    }

    void "test count"() {
        setupData()

        expect:
        questService.count() == 5
    }

    void "test delete"() {
        Long questId = setupData()

        expect:
        questService.count() == 5

        when:
        questService.delete(questId)
        sessionFactory.currentSession.flush()

        then:
        questService.count() == 4
    }

    void "test save"() {
        when:
        Quest quest = new Quest(name: 'Seek the grail')
        questService.save(quest)

        then:
        quest.id != null
    }
}

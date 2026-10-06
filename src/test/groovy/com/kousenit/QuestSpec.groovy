package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class QuestSpec extends Specification implements DomainUnitTest<Quest> {

    void "a quest with a name is valid"() {
        expect:
        new Quest(name: 'Seek the grail').validate()
    }

    void "a quest needs a name"() {
        when:
        Quest quest = new Quest(name: ' ')

        then:
        !quest.validate()
        quest.errors['name'].code == 'blank'
    }
}

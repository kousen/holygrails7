package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class TaskSpec extends Specification implements DomainUnitTest<Task> {

    Quest quest = new Quest(name: 'Seek the grail')
    Task task = new Task(name: 'Defeat the Black Knight', quest: quest)

    void "a task with defaults is valid"() {
        expect:
        task.validate()
        task.priority == 3
        !task.completed
    }

    void "a task that starts and ends today lasts one day"() {
        expect:
        task.duration == 1
    }

    void "the end date may not precede the start date"() {
        when:
        task.endDate = task.startDate.minusDays(1)

        then:
        !task.validate()
        task.errors['endDate'].code == 'validator.invalid'
    }
}

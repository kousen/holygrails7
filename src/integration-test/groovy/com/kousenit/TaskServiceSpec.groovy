package com.kousenit

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import spock.lang.Specification

@Integration
@Rollback
class TaskServiceSpec extends Specification {

    TaskService taskService
    SessionFactory sessionFactory

    private Long setupData() {
        Quest quest = new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
        new Task(name: 'Run away from killer rabbit', quest: quest).save(flush: true, failOnError: true)
        new Task(name: 'Answer the Bridgekeeper', priority: 4, quest: quest).save(flush: true, failOnError: true)
        Task task = new Task(name: 'Defeat the Black Knight', completed: true, quest: quest).save(flush: true, failOnError: true)
        new Task(name: 'Bring out your dead', quest: quest).save(flush: true, failOnError: true)
        new Task(name: 'Find a shrubbery', priority: 2, quest: quest).save(flush: true, failOnError: true)
        task.id
    }

    void "test get"() {
        Long taskId = setupData()

        expect:
        taskService.get(taskId) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Task> taskList = taskService.list(max: 2, offset: 2)

        then:
        taskList.size() == 2
    }

    void "test count"() {
        setupData()

        expect:
        taskService.count() == 5
    }

    void "test delete"() {
        Long taskId = setupData()

        expect:
        taskService.count() == 5

        when:
        taskService.delete(taskId)
        sessionFactory.currentSession.flush()

        then:
        taskService.count() == 4
    }

    void "test save"() {
        when:
        Quest quest = new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
        Task task = new Task(name: 'Defeat the Black Knight', quest: quest)
        taskService.save(task)

        then:
        task.id != null
    }
}

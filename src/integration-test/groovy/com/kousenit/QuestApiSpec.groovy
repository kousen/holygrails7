package com.kousenit

import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.cleanup.core.DatabaseCleanup
import org.apache.grails.testing.http.client.HttpClientSupport
import spock.lang.Specification

/**
 * Exercises the JSON API over real HTTP with the HttpClientSupport trait (Grails 7.1+).
 * Functional tests are not rolled back, so this spec seeds its own data and @DatabaseCleanup
 * truncates every table after each test.
 */
@Integration
@DatabaseCleanup
class QuestApiSpec extends Specification implements HttpClientSupport {

    def setup() {
        Quest.withNewTransaction {
            SeedData.theCourt(SeedData.seekTheGrail())
        }
    }

    void "the quest list is JSON shaped by the view"() {
        when:
        def response = http('/api/quests')

        then:
        response.assertStatus(200)
                .assertHeadersIgnoreCase('content-type': 'application/json;charset=UTF-8')
        with(response.json()) {
            count == 1
            quests[0].name == 'Seek the grail'
            quests[0].knights.size() == 5
            quests[0].tasks.size() == 9
            quests[0].remaining == 6
            quests[0].tasks*.priority == quests[0].tasks*.priority.sort()
        }
    }

    void "a single quest can be fetched by id"() {
        given:
        Long id = Quest.withNewTransaction { Quest.findByName('Seek the grail').id }

        when:
        def response = http("/api/quests/$id")

        then:
        response.assertJsonContains([name: 'Seek the grail', remaining: 6])
    }

    void "posting JSON creates a quest"() {
        when:
        def response = httpPostJson('/api/quests', [name: 'Find a shrubbery'])

        then:
        response.assertStatus(201)
        response.assertJsonContains([name: 'Find a shrubbery', knights: [], tasks: [], remaining: 0])
        Quest.withNewTransaction { Quest.countByName('Find a shrubbery') } == 1
    }

    void "a blank name is rejected with 422"() {
        when:
        def response = httpPostJson('/api/quests', [name: ' '])

        then:
        response.assertStatus(422)
        response.assertContains('Quests must have a name')
    }

    void "unknown quests are 404"() {
        expect:
        http('/api/quests/9999').assertStatus(404)
    }
}

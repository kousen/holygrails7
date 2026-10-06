package com.kousenit

import java.time.LocalDate

/**
 * The quest for the Holy Grail, as a set of tasks. Used by BootStrap for the
 * running application and by the integration tests, so both see the same data.
 */
class SeedData {

    static Quest seekTheGrail() {
        LocalDate today = LocalDate.now()
        new Quest(name: 'Seek the grail')
                .addToTasks(name: 'Run away from killer rabbit', priority: 1)
                .addToTasks(name: 'Answer the Bridgekeeper', priority: 4, startDate: today + 1, endDate: today + 1)
                .addToTasks(name: 'Defeat the Black Knight', completed: true, startDate: today - 3, endDate: today - 2)
                .addToTasks(name: 'Bring out your dead', completed: true, priority: 5, startDate: today - 1, endDate: today - 1)
                .addToTasks(name: 'Find a shrubbery for the Knights Who Say Ni', priority: 2, endDate: today + 7)
                .addToTasks(name: 'Get taunted by the French', completed: true, priority: 4)
                .addToTasks(name: 'Weigh a witch against a duck', priority: 3)
                .addToTasks(name: 'Build a giant wooden rabbit', priority: 2, endDate: today + 14)
                .addToTasks(name: 'Lobbeth the Holy Hand Grenade of Antioch', priority: 5, startDate: today + 2, endDate: today + 2)
                .save(failOnError: true)
    }
}

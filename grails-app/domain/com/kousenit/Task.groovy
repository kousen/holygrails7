package com.kousenit

import groovy.transform.ToString

import java.time.LocalDate

@ToString(includeNames = true, includes = ['name', 'priority', 'completed'])
class Task {
    String name
    int priority = 3
    LocalDate startDate = LocalDate.now()
    LocalDate endDate = LocalDate.now()
    boolean completed

    static belongsTo = [quest: Quest]

    int getDuration() { (endDate - startDate) + 1 }

    static constraints = {
        name blank: false
        priority range: 1..5
        endDate validator: { LocalDate value, Task task ->
            value >= task.startDate
        }
    }
}

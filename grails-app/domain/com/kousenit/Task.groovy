package com.kousenit

import java.time.LocalDate

class Task {
    String name
    int priority = 3
    LocalDate startDate = LocalDate.now()
    LocalDate endDate = LocalDate.now()
    boolean completed

    String toString() { name }

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

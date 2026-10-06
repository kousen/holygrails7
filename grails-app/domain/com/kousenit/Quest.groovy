package com.kousenit

import java.time.LocalDateTime

class Quest {
    String name
    LocalDateTime dateCreated
    LocalDateTime lastUpdated

    static hasMany = [tasks: Task]

    String toString() { name }

    static constraints = {
        name blank: false
    }
}

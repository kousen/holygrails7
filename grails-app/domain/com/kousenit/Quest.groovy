package com.kousenit

class Quest {
    String name

    String toString() { name }

    static constraints = {
        name blank: false
    }
}

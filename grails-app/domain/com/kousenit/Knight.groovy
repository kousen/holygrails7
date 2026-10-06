package com.kousenit

class Knight {
    String title = 'Sir'
    String name
    String favouriteColour
    Quest quest
    Castle castle

    String toString() { "$title $name" }

    static constraints = {
        title inList: ['Sir', 'Lord', 'Lady', 'King', 'Queen']
        name blank: false
        favouriteColour nullable: true, validator: { String colour, Knight knight ->
            // The Bridgekeeper asks every knight. Only Galahad is allowed not to know.
            if (!colour && !knight.name.contains('Galahad')) {
                return 'bridgeOfDeath'
            }
        }
        quest nullable: true
        castle nullable: true
    }
}

package com.kousenit

class Bridgekeeper extends Enemy {
    static hasMany = [questions: String]

    @Override
    String challenge() { 'Stop! Who would cross the Bridge of Death must answer me these questions three' }

    static constraints = {
        questions maxSize: 3
    }
}

package com.kousenit

class RabbitOfCaerbannog extends Enemy {
    int knightsEaten = 0
    String weakness = 'Holy Hand Grenade of Antioch'

    @Override
    String challenge() { 'Look at the bones!' }

    static constraints = {
        knightsEaten min: 0
    }
}

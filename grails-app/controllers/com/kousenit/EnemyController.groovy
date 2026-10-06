package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold

/**
 * One annotation puts the whole hierarchy on the web, with one catch: respond()
 * names the model after the runtime class of what it is given, so a list that
 * starts with a BlackKnight becomes blackKnightList and the scaffolded view,
 * which expects enemyList, renders nothing. Name the model yourself.
 */
@Scaffold(Enemy)
class EnemyController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [enemyList: Enemy.list(params), enemyCount: Enemy.count()]
    }

    def show(Long id) {
        [enemy: Enemy.get(id)]
    }
}

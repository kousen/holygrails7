package com.kousenit

import grails.rest.RestfulController

/** JSON-only REST endpoint for quests, rendered by the JSON views in grails-app/views/questApi. */
class QuestApiController extends RestfulController<Quest> {

    static responseFormats = ['json']

    QuestApiController() {
        super(Quest)
    }
}

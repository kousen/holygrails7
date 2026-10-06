package com.kousenit

import grails.util.Environment

class BootStrap {

    def init = { servletContext ->
        if (Environment.current != Environment.TEST && Quest.count() == 0) {
            Quest quest = SeedData.seekTheGrail()
            SeedData.theCourt(quest)
        }
    }

    def destroy = {
    }
}

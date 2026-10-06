package com.kousenit

import grails.util.Environment

class BootStrap {

    def init = { servletContext ->
        if (Environment.current != Environment.TEST && Quest.count() == 0) {
            SeedData.seekTheGrail()
        }
    }

    def destroy = {
    }
}

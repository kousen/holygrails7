package com.kousenit

import grails.rest.RestfulController
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class EnemyControllerSpec extends Specification implements ControllerUnitTest<EnemyController> {

    void "the scaffolded controller serves the base class"() {
        expect:
        controller instanceof RestfulController
        controller.resource == Enemy
    }
}

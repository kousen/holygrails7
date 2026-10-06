package com.kousenit

import grails.rest.RestfulController
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class CastleControllerSpec extends Specification implements ControllerUnitTest<CastleController> {

    void "the annotation turns an empty class into a RestfulController for Castle"() {
        expect:
        controller instanceof RestfulController
        controller.resource == Castle
        ['index', 'show', 'create', 'save', 'edit', 'update', 'delete'].every { controller.respondsTo(it) }
    }
}

package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold
import grails.plugin.scaffolding.RestfulServiceController

@Scaffold(RestfulServiceController<Castle>)
class CastleController {

    /** The scaffolded index, plus one extra model entry: every castle that knows where it is. */
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond listAllResources(params), model: [castleCount: countResources(), markers: markers()]
    }

    private List<Map> markers() {
        Castle.findAllByLatitudeIsNotNullAndLongitudeIsNotNull().collect { Castle castle ->
            [name: castle.name, city: castle.city, lat: castle.latitude, lng: castle.longitude,
             knights: castle.knights.size(), url: createLink(action: 'show', id: castle.id)]
        }
    }
}

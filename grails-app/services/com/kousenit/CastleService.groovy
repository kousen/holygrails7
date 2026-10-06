package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(Castle)
class CastleService {

    GeocoderService geocoderService

    @Override
    Castle save(Castle castle) {
        if (castle.latitude == null || castle.longitude == null) {
            geocoderService.fillInLatLng(castle)
        }
        super.save(castle)
    }
}

package com.kousenit

class UrlMappings {
    static mappings = {
        "/$namespace/$controller/$action?/$id?(.$format)?" {}
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/api/quests"(resources: "questApi")
        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}

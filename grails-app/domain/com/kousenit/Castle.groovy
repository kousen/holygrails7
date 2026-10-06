package com.kousenit

class Castle {
    String name
    String city
    String country
    Double latitude
    Double longitude

    static hasMany = [knights: Knight]

    String toString() { name }

    static constraints = {
        name blank: false
        city blank: false
        country blank: false
        latitude nullable: true, range: -90d..90d
        longitude nullable: true, range: -180d..180d
    }
}

package com.kousenit

/**
 * Base class of everything that stands between a knight and the grail.
 * GORM maps the hierarchy to one table with a discriminator column by default.
 */
class Enemy {
    String name
    String location
    boolean defeated

    static belongsTo = [quest: Quest]

    String toString() { name }

    /** What this enemy says when a knight approaches. Subclasses override. */
    String challenge() { "None shall pass" }

    static constraints = {
        name blank: false
        location blank: false
    }
}

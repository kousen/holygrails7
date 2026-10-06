package com.kousenit

class BlackKnight extends Enemy {
    int limbsRemaining = 4

    @Override
    String challenge() {
        limbsRemaining > 0 ? "None shall pass" : "All right, we'll call it a draw"
    }

    boolean isMerelyAFleshWound() { limbsRemaining > 0 }

    static constraints = {
        limbsRemaining range: 0..4
    }
}

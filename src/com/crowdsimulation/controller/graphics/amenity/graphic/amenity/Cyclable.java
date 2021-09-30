package com.crowdsimulation.controller.graphics.amenity.graphic.amenity;

// Cyclable graphics are graphics not influenced by the object's state
// (i.e., a purely visual change not tied to the object state, such as how a staircase is facing one way or another)
public interface Cyclable {
    void cycle();
}

package com.trainsimulation.model.utility;

// Represents an amount pertaining to the number of passengers passing through an area
public class Inflow {
    private final int inflow;

    public Inflow(int inflow) {
        this.inflow = inflow;
    }

    public int getInflow() {
        return inflow;
    }
}

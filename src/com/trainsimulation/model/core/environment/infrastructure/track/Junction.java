package com.trainsimulation.model.core.environment.infrastructure.track;

import com.trainsimulation.model.core.environment.TrainSystem;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

// Junction connect segments with each other
public class Junction extends Track {

    // Projects the segment(s) coming out of this connector
    private final HashMap<Direction, Segment> outSegments;
//    private final List<Segment> outSegments;

    // Acts as a "stoplight" for trains
    private final Semaphore signal;

    // Denotes whether the junction is the end of the train line or not
    private boolean end;

    public Junction(TrainSystem trainSystem) {
        super(trainSystem);

        this.outSegments = new HashMap<>();
//        this.outSegments = new ArrayList<>();
        this.end = false;
        this.signal = new Semaphore(1, true);
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public Semaphore getSignal() {
        return signal;
    }

    public HashMap<Direction, Segment> getOutSegments() {
        return outSegments;
    }

    public void insertOutSegment(Direction direction, Segment segment) {
        this.outSegments.put(direction, segment);
    }

    public Segment getOutSegment(Direction direction) {
        return this.outSegments.get(direction);
    }
}

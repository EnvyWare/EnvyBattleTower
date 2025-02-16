package com.envyful.battle.tower.api;

public class AttemptDetails {

    private BattleTower tower;
    private long attemptStart;
    private long attemptDuration;
    private int floorReached;

    public AttemptDetails(BattleTower tower, long attemptStart, long attemptDuration, int floorReached) {
        this.tower = tower;
        this.attemptStart = attemptStart;
        this.attemptDuration = attemptDuration;
        this.floorReached = floorReached;
    }

    public BattleTower getTower() {
        return this.tower;
    }

    public long getAttemptStart() {
        return this.attemptStart;
    }

    public long getAttemptDuration() {
        return this.attemptDuration;
    }

    public int getFloorReached() {
        return this.floorReached;
    }
}

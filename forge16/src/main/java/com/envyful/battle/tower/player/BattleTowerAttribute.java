package com.envyful.battle.tower.player;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.attribute.AbstractForgeAttribute;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.battle.tower.EnvyBattleTower;

import java.util.UUID;

public class BattleTowerAttribute extends AbstractForgeAttribute<EnvyBattleTower> {

    private AttemptDetails lastAttempt;
    private AttemptDetails bestAttempt;

    public BattleTowerAttribute(EnvyBattleTower manager, EnvyPlayer<?> parent) {
        super(manager, (ForgeEnvyPlayer) parent);
    }

    public BattleTowerAttribute(UUID uuid) {
        super(uuid);
    }

    public AttemptDetails getLastAttempt() {
        return this.lastAttempt;
    }

    public void setLastAttempt(AttemptDetails lastAttempt) {
        this.lastAttempt = lastAttempt;
    }

    public AttemptDetails getBestAttempt() {
        return this.bestAttempt;
    }

    public void setBestAttempt(AttemptDetails bestAttempt) {
        this.bestAttempt = bestAttempt;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }

    public static class AttemptDetails {

        private long attemptStart;
        private long attemptDuration;
        private int floorReached;

        public AttemptDetails(long attemptStart, long attemptDuration, int floorReached) {
            this.attemptStart = attemptStart;
            this.attemptDuration = attemptDuration;
            this.floorReached = floorReached;
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
}

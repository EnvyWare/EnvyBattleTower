package com.envyful.battle.tower.api;

import com.envyful.api.config.ConfigLocation;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class FloorPosition {

    private ConfigLocation playerPosition;
    private ConfigLocation trainerPosition;

    public FloorPosition(ConfigLocation playerPosition, ConfigLocation trainerPosition) {
        this.playerPosition = playerPosition;
        this.trainerPosition = trainerPosition;
    }

    public FloorPosition() {
    }

    public ConfigLocation getPlayerPosition() {
        return this.playerPosition;
    }

    public ConfigLocation getTrainerPosition() {
        return this.trainerPosition;
    }
}

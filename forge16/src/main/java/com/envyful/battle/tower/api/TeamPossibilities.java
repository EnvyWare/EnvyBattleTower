package com.envyful.battle.tower.api;

import com.envyful.api.config.type.ConfigRandomWeightedSet;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class TeamPossibilities {

    private int startFloor = 0;
    private int endFloor = 1000;
    private ConfigRandomWeightedSet<PokePaste> teams = ConfigRandomWeightedSet.builder(new PokePaste("https://pokepast.es/7fb798994199787a"), 10).build();

    public TeamPossibilities() {
    }

    public int getStartFloor() {
        return this.startFloor;
    }

    public int getEndFloor() {
        return this.endFloor;
    }

    public ConfigRandomWeightedSet<PokePaste> getTeams() {
        return this.teams;
    }
}

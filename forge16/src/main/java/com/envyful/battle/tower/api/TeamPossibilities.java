package com.envyful.battle.tower.api;

import com.envyful.api.config.type.ConfigRandomWeightedSet;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class TeamPossibilities {

    @Comment("The lowest floor this team can be encountered on (if you want it to be available on all floors, set this to 0)")
    private int startFloor = 0;
    @Comment("The highest floor this team can be encountered on (if you want it to be available on all floors, set this to the max floor)")
    private int endFloor = 1000;
    @Comment("A weighted set of teams that can be encountered on this floor. The higher the weight, the more likely it is to be selected")
    private ConfigRandomWeightedSet<PokePaste> teams = ConfigRandomWeightedSet.builder(new PokePaste("https://pokepast.es/8c84d94a0da05c13"), 10).build();

    public TeamPossibilities() {
    }

    public TeamPossibilities(int startFloor, int endFloor, ConfigRandomWeightedSet<PokePaste> teams) {
        this.startFloor = startFloor;
        this.endFloor = endFloor;
        this.teams = teams;
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

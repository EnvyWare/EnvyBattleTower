package com.envyful.battle.tower.config;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.ConfigRandomWeightedSet;
import com.envyful.api.config.type.SQLDatabaseDetails;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/config.yml")
public class BattleTowerConfig extends AbstractYamlConfig {

    private SQLDatabaseDetails databaseDetails = new SQLDatabaseDetails(
            "EnvyBattleTower", "0.0.0.0", 3306, "admin", "password", "database"
    );

    private Map<String, PossiblePosition> positions = ImmutableMap.of(
            "example", new PossiblePosition(
                    new ConfigLocation("world", 1, 1, 1, 1, 1),
                    new ConfigLocation("world", 1, 1, 1, 1, 1)
    ));

    private Map<String, TeamPossibilities> teamOptions = ImmutableMap.of(
            "one", new TeamPossibilities()
    );

    private int maxFloor = -1;
    private long cooldownSeconds = TimeUnit.DAYS.toSeconds(1);

    public BattleTowerConfig() {
        super();
    }

    public SQLDatabaseDetails getDatabaseDetails() {
        return this.databaseDetails;
    }

    public List<PossiblePosition> getPositions() {
        return Lists.newArrayList(this.positions.values());
    }

    public boolean canContinue(int nextFloor) {
        if (this.maxFloor == -1) {
            return true;
        }

        return nextFloor <= this.maxFloor;
    }

    public List<TeamPossibilities> getTeamPossibilities() {
        return Lists.newArrayList(this.teamOptions.values());
    }

    public long getCooldownSeconds() {
        return this.cooldownSeconds;
    }

    @ConfigSerializable
    public static class PossiblePosition {

        private ConfigLocation playerPosition;
        private ConfigLocation trainerPosition;

        public PossiblePosition(ConfigLocation playerPosition, ConfigLocation trainerPosition) {
            this.playerPosition = playerPosition;
            this.trainerPosition = trainerPosition;
        }

        public PossiblePosition() {
        }

        public ConfigLocation getPlayerPosition() {
            return this.playerPosition;
        }

        public ConfigLocation getTrainerPosition() {
            return this.trainerPosition;
        }
    }

    @ConfigSerializable
    public static class TeamPossibilities {

        private int startFloor = 1;
        private int endFloor = 1000;
        private ConfigRandomWeightedSet<String> teams = new ConfigRandomWeightedSet<>(
                new ConfigRandomWeightedSet.WeightedObject<>(10, "https://pokepast.es/")
        );

        public TeamPossibilities() {
        }

        public int getStartFloor() {
            return this.startFloor;
        }

        public int getEndFloor() {
            return this.endFloor;
        }

        public ConfigRandomWeightedSet<String> getTeams() {
            return this.teams;
        }
    }
}

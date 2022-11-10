package com.envyful.battle.tower.config;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.ConfigRandomWeightedSet;
import com.envyful.api.config.type.SQLDatabaseDetails;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.reforged.battle.ConfigBattleRule;
import com.envyful.api.reforged.pixelmon.PokePasteReader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
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

    private ConfigLocation returnPosition = new ConfigLocation(
            "world", 0, 0, 0, 0, 0
    );

    private int maxFloor = -1;
    private long cooldownSeconds = TimeUnit.DAYS.toSeconds(1);
    private boolean allowExpGain = false;
    private Map<String, ConfigBattleRule> battleRules = ImmutableMap.of(
            "one", new ConfigBattleRule("example", "value")
    );

    private List<String> attemptFinishLossCommands = Lists.newArrayList("broadcast %player% %floor%");
    private List<String> attemptFinishWinCommands = Lists.newArrayList("broadcast %player% %floor%");

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

    public boolean isAllowExpGain() {
        return this.allowExpGain;
    }

    public List<ConfigBattleRule> getRules() {
        return Lists.newArrayList(this.battleRules.values());
    }

    public TeamPossibilities getTeamPossibilities(int floor) {
        for (TeamPossibilities teamPossibility : this.getTeamPossibilities()) {
            if (teamPossibility.getEndFloor() > floor && teamPossibility.getStartFloor() < floor) {
                return teamPossibility;
            }
        }

        return null;
    }

    public List<String> getAttemptFinishLossCommands() {
        return this.attemptFinishLossCommands;
    }

    public List<String> getAttemptFinishWinCommands() {
        return this.attemptFinishWinCommands;
    }

    public ConfigLocation getReturnPosition() {
        return this.returnPosition;
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

        private int startFloor = 0;
        private int endFloor = 1000;
        private ConfigRandomWeightedSet<PokePaste> teams = new ConfigRandomWeightedSet<>(
                new ConfigRandomWeightedSet.WeightedObject<>(10, new PokePaste("https://pokepast.es/")));

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

    @ConfigSerializable
    public static class PokePaste {

        private String paste;
        private transient List<Pokemon> team;
        private ConfigRandomWeightedSet<Commands> playerWinCommands = new ConfigRandomWeightedSet<>(
                new ConfigRandomWeightedSet.WeightedObject<>(10, new Commands(Lists.newArrayList("broadcast %player%")))
        );
        private ConfigRandomWeightedSet<Commands> playerLossCommands = new ConfigRandomWeightedSet<>(
                new ConfigRandomWeightedSet.WeightedObject<>(10, new Commands(Lists.newArrayList("broadcast %player%")))
        );

        public PokePaste(String paste) {
            this.paste = paste;
        }

        public PokePaste() {
        }

        public List<Pokemon> getTeam() {
            if (this.team == null) {
                this.team = PokePasteReader.from(this.paste).build();
            }

            return this.team;
        }

        public List<String> getPlayerWinCommands() {
            return this.playerWinCommands.getRandom().getCommands();
        }

        public List<String> getPlayerLossCommands() {
            return this.playerLossCommands.getRandom().getCommands();
        }

        @Override
        public String toString() {
            return this.paste;
        }
    }

    @ConfigSerializable
    public static class Commands {

        private List<String> commands;

        public Commands(List<String> commands) {
            this.commands = commands;
        }

        public Commands() {
        }

        public List<String> getCommands() {
            return this.commands;
        }
    }
}

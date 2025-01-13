package com.envyful.battle.tower.config;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.database.DatabaseDetailsConfig;
import com.envyful.api.config.type.ConfigRandomWeightedSet;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.reforged.battle.ConfigBattleRule;
import com.envyful.api.reforged.pixelmon.PokePasteReader;
import com.envyful.api.sqlite.config.SQLiteDatabaseDetailsConfig;
import com.envyful.battle.tower.EnvyBattleTower;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/config.yml")
public class BattleTowerConfig extends AbstractYamlConfig {

    private DatabaseDetailsConfig databaseDetails = new SQLiteDatabaseDetailsConfig("config/EnvyBattleTower/data.db");

    private Map<String, PossiblePosition> positions = ImmutableMap.of(
            "example", new PossiblePosition(
                    ConfigLocation.builder()
                            .worldName("world")
                            .posX(0)
                            .posY(0)
                            .posZ(0)
                            .build(),
                    ConfigLocation.builder()
                            .worldName("world")
                            .posX(0)
                            .posY(0)
                            .posZ(0)
                            .build()
    ));

    private Map<String, TeamPossibilities> teamOptions = ImmutableMap.of(
            "one", new TeamPossibilities()
    );

    private ConfigLocation returnPosition = ConfigLocation.builder()
            .worldName("world")
            .posX(0)
            .posY(0)
            .posZ(0)
            .build();

    private int maxFloor = -1;
    private boolean allowSpectating = false;
    private long cooldownSeconds = TimeUnit.DAYS.toSeconds(1);
    private boolean allowExpGain = false;
    private Map<String, ConfigBattleRule> battleRules = ImmutableMap.of(
            "one", new ConfigBattleRule("example", "value")
    );

    private List<String> attemptFinishLossCommands = Lists.newArrayList("broadcast %player% %floor%");
    private List<String> attemptFinishWinCommands = Lists.newArrayList("broadcast %player% %floor%");

    private List<String> blacklistedPokemon = Lists.newArrayList(
            "bidoof"
    );
    private transient List<PokemonSpecification> blacklistedCache = null;

    public BattleTowerConfig() {
        super();
    }

    public DatabaseDetailsConfig getDatabaseDetails() {
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

    public boolean isAllowSpectating() {
        return this.allowSpectating;
    }

    public boolean isBlacklisted(Pokemon pokemon) {
        if (pokemon == null) {
            return false;
        }

        if (this.blacklistedCache == null) {
            this.blacklistedCache = Lists.newArrayList();

            for (String s : this.blacklistedPokemon) {
                this.blacklistedCache.add(PokemonSpecificationProxy.create(s));
            }
        }

        for (PokemonSpecification pokemonSpecification : this.blacklistedCache) {
            if (pokemonSpecification.matches(pokemon)) {
                return true;
            }
        }

        return false;
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

        public String getPaste() {
            return this.paste;
        }

        public List<Pokemon> getTeam() {
            if (this.team == null) {
                try {
                    this.team = PokePasteReader.from(this.paste).build();
                } catch (Exception e) {
                    EnvyBattleTower.getLogger().error("Error during PokePaste load attempt for URL `{}`", this.paste);
                    EnvyBattleTower.getLogger().catching(e);
                }
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

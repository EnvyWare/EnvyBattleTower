package com.envyful.battle.tower.api;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.config.ConfigLocation;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.leaderboard.Leaderboard;
import com.envyful.api.reforged.battle.ConfigBattleRule;
import com.envyful.api.text.parse.SimplePlaceholder;
import com.envyful.api.type.Pair;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.api.attribute.BattleTowerAttribute;
import com.envyful.battle.tower.api.attribute.BattleTowerAttributeAdapter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ConfigSerializable
public class BattleTower extends AbstractYamlConfig implements SimplePlaceholder {

    private boolean enabled = true;
    private String id;
    private Map<String, FloorPosition> positions;
    private Map<String, TeamPossibilities> teamOptions;
    private ConfigLocation returnPosition;
    private int maxFloor;
    private boolean allowSpectating;
    private long cooldownSeconds;
    private boolean allowExpGain;
    private Map<String, ConfigBattleRule> battleRules;
    private List<String> attemptFinishLossCommands;
    private List<String> attemptFinishWinCommands;
    private List<PokemonSpecification> blacklistedPokemon;
    private ExtendedConfigItem cooldownItem;
    private ExtendedConfigItem displayItem;
    private boolean virtual = true;

    private transient Leaderboard<BattleTowerEntry> leaderboard;

    public BattleTower() {
        super();
    }

    public void init() {
        var adapter = (BattleTowerAttributeAdapter) EnvyBattleTower.getPlayerManager().getAdapter(BattleTowerAttribute.class);
        this.leaderboard = adapter.getLeaderboard(this.id);
    }

    public boolean enabled() {
        return this.enabled;
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    public String id() {
        return this.id;
    }

    public List<FloorPosition> getPositions() {
        return List.copyOf(this.positions.values());
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

    public ExtendedConfigItem getDisplayItem() {
        return this.displayItem;
    }

    public ExtendedConfigItem getCooldownItem() {
        return this.cooldownItem;
    }

    public List<ConfigBattleRule> getRules() {
        return Lists.newArrayList(this.battleRules.values());
    }

    public TeamPossibilities getTeamPossibilities(int floor) {
        for (var teamPossibility : this.getTeamPossibilities()) {
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

    public Pokemon hasBlacklistedPokemon(ForgeEnvyPlayer player) {
        var party = StorageProxy.getParty(player.getParent());

        if (party == null) {
            return null;
        }

        for (var pokemon : party.getAll()) {
            if (this.isBlacklisted(pokemon)) {
                return pokemon;
            }
        }

        return null;
    }

    public boolean isBlacklisted(Pokemon pokemon) {
        if (pokemon == null) {
            return false;
        }

        for (var spec : this.blacklistedPokemon) {
            if (spec.matches(pokemon)) {
                return true;
            }
        }

        return false;
    }

    public CompletableFuture<Pair<PokePaste, List<Pokemon>>> getRandomLeaderTeam(int currentFloor) {
        var teamPossibilities = this.getTeamPossibilities(currentFloor);

        if (teamPossibilities == null || teamPossibilities.getTeams() == null || teamPossibilities.getTeams().getWeightedSet() == null ||
                teamPossibilities.getTeams().getWeightedSet().getRandom() == null) {
            return CompletableFuture.completedFuture(null);
        }

        var random = teamPossibilities.getTeams().getWeightedSet().getRandom();

        List<Pokemon> team = new ArrayList<>(6);

        return UtilConcurrency.supplyAsync(random::getTeam).thenApply(pokePasteTeam -> {
            if (pokePasteTeam == null || pokePasteTeam.isEmpty()) {
                EnvyBattleTower.getLogger().error("Invalid PokePaste found: " + random.getPaste());
                return null;
            }

            for (var pokemon : pokePasteTeam) {
                Pokemon copy = PokemonBuilder.copy(pokemon).build();
                copy.heal();
                team.add(copy);
            }

            return Pair.of(random, team);
        });
    }

    @Override
    public String replace(String s) {
        var firstPage = this.leaderboard.getPage(0);

        for (int i = 0; i < firstPage.size(); i++) {
            s = this.replace(s, firstPage.get(i), i);
        }

        return s;
    }

    private String replace(String s, BattleTowerEntry entry, int pos) {
        return s.replace("%leaderboard_" + (pos + 1) + "_player%", entry.getName())
                .replace("%leaderboard_" + (pos + 1) + "_floor%", entry.getFloors() + "")
                .replace("%leaderboard_" + (pos + 1) + "_time%", EnvyBattleTower.getLocale().getTimeFormat().format(entry.getTime()))
                .replace("%leaderboard_" + (pos + 1) + "_date%", EnvyBattleTower.getLocale().getDateFormat().format(entry.getStart()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private Map<String, FloorPosition> positions = ImmutableMap.of(
                "example", new FloorPosition(
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
        private Map<String, ConfigBattleRule> battleRules = new HashMap<>();
        private List<String> attemptFinishLossCommands = new ArrayList<>();
        private List<String> attemptFinishWinCommands = new ArrayList<>();
        private List<PokemonSpecification> blacklistedPokemon = new ArrayList<>();
        private ExtendedConfigItem displayItem;
        private ExtendedConfigItem cooldownItem;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder position(FloorPosition position) {
            this.positions.put(this.positions.size() + "", position);
            return this;
        }

        public Builder teamOption(TeamPossibilities teamPossibilities) {
            this.teamOptions.put(this.teamOptions.size() + "", teamPossibilities);
            return this;
        }

        public Builder returnPosition(ConfigLocation returnPosition) {
            this.returnPosition = returnPosition;
            return this;
        }

        public Builder maxFloor(int maxFloor) {
            this.maxFloor = maxFloor;
            return this;
        }

        public Builder allowSpectating(boolean allowSpectating) {
            this.allowSpectating = allowSpectating;
            return this;
        }

        public Builder cooldownSeconds(long cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
            return this;
        }

        public Builder allowExpGain(boolean allowExpGain) {
            this.allowExpGain = allowExpGain;
            return this;
        }

        public Builder battleRule(String key, ConfigBattleRule rule) {
            this.battleRules.put(key, rule);
            return this;
        }

        public Builder attemptFinishLossCommand(String command) {
            this.attemptFinishLossCommands.add(command);
            return this;
        }

        public Builder attemptFinishWinCommand(String command) {
            this.attemptFinishWinCommands.add(command);
            return this;
        }

        public Builder blacklistedPokemon(PokemonSpecification pokemon) {
            this.blacklistedPokemon.add(pokemon);
            return this;
        }

        public Builder blacklistedPokemon(String pokemon) {
            this.blacklistedPokemon.add(PokemonSpecificationProxy.create(pokemon));
            return this;
        }

        public Builder displayItem(ExtendedConfigItem displayItem) {
            this.displayItem = displayItem;
            return this;
        }

        public Builder cooldownItem(ExtendedConfigItem cooldownItem) {
            this.cooldownItem = cooldownItem;
            return this;
        }

        public BattleTower build() {
            BattleTower battleTower = new BattleTower();
            battleTower.id = this.id;
            battleTower.positions = this.positions;
            battleTower.teamOptions = this.teamOptions;
            battleTower.returnPosition = this.returnPosition;
            battleTower.maxFloor = this.maxFloor;
            battleTower.allowSpectating = this.allowSpectating;
            battleTower.cooldownSeconds = this.cooldownSeconds;
            battleTower.allowExpGain = this.allowExpGain;
            battleTower.battleRules = this.battleRules;
            battleTower.attemptFinishLossCommands = this.attemptFinishLossCommands;
            battleTower.attemptFinishWinCommands = this.attemptFinishWinCommands;
            battleTower.blacklistedPokemon = this.blacklistedPokemon;
            battleTower.displayItem = this.displayItem;
            battleTower.cooldownItem = this.cooldownItem;
            return battleTower;
        }
    }
}

package com.envyful.battle.tower.api;

import com.envyful.api.config.type.ConfigRandomWeightedSet;
import com.envyful.api.reforged.pixelmon.PokePasteReader;
import com.envyful.battle.tower.EnvyBattleTower;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

import java.util.List;

public class PokePaste {

    private String paste;
    private transient List<Pokemon> team;
    private ConfigRandomWeightedSet<List<String>> playerWinCommands = new ConfigRandomWeightedSet<>(
            new ConfigRandomWeightedSet.WeightedObject<>(10, Lists.newArrayList("broadcast %player%"))
    );
    private ConfigRandomWeightedSet<List<String>> playerLossCommands = new ConfigRandomWeightedSet<>(
            new ConfigRandomWeightedSet.WeightedObject<>(10, Lists.newArrayList("broadcast %player%"))
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
                EnvyBattleTower.getLogger().error("Error during PokePaste load attempt for URL `" + this.paste + "`", e);
            }
        }

        return this.team;
    }

    public List<String> getPlayerWinCommands() {
        return this.playerWinCommands.getRandom();
    }

    public List<String> getPlayerLossCommands() {
        return this.playerLossCommands.getRandom();
    }

    @Override
    public String toString() {
        return this.paste;
    }
}

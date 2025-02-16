package com.envyful.battle.tower.api;

import com.envyful.api.config.type.ConfigRandomWeightedSet;
import com.envyful.api.reforged.pixelmon.PokePasteReader;
import com.envyful.battle.tower.EnvyBattleTower;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;

@ConfigSerializable
public class PokePaste {

    @Comment("PokePaste URL to load the team from")
    private String paste;
    private transient List<Pokemon> team;
    @Comment("Commands to run when the player wins a battle against this team")
    private ConfigRandomWeightedSet<List<String>> playerWinCommands = new ConfigRandomWeightedSet<>(
            new ConfigRandomWeightedSet.WeightedObject<>(10, Lists.newArrayList("minecraft:tell %player% Well done you won!"))
    );
    @Comment("Commands to run when the player loses a battle against this team")
    private ConfigRandomWeightedSet<List<String>> playerLossCommands = new ConfigRandomWeightedSet<>(
            new ConfigRandomWeightedSet.WeightedObject<>(10, Lists.newArrayList("minecraft:tell %player% You lost! Better luck next time"))
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

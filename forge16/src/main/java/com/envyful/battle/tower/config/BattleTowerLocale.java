package com.envyful.battle.tower.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.DateFormatConfig;
import com.envyful.api.config.type.TimeFormatConfig;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/locale.yml")
public class BattleTowerLocale extends AbstractYamlConfig {

    @Comment("This is the message that will be sent to the player if they have a blacklisted pokemon in their party")
    private List<String> blacklistedPokemonError = Lists.newArrayList(
            "&c&l(!) &cError: you have a blacklisted pokemon in your party so you cannot enter the battle tower! %pokemon%"
    );

    @Comment("This is used for formatting any times that appear in GUIs, or in messages")
    private TimeFormatConfig timeFormat = new TimeFormatConfig();

    @Comment("This is used for formatting any dates that appear in GUIs, or in messages")
    private DateFormatConfig dateFormat = new DateFormatConfig("dd/MM/yyyy");

    public BattleTowerLocale() {
        super();
    }

    public List<String> getBlacklistedPokemonError() {
        return this.blacklistedPokemonError;
    }

    public TimeFormatConfig getTimeFormat() {
        return this.timeFormat;
    }

    public DateFormatConfig getDateFormat() {
        return this.dateFormat;
    }
}

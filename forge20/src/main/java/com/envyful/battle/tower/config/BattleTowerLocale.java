package com.envyful.battle.tower.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/locale.yml")
public class BattleTowerLocale extends AbstractYamlConfig {

    private List<String> blacklistedPokemonError = Lists.newArrayList(
            "&c&l(!) &cError: you have a blacklisted pokemon in your party so you cannot enter the battle tower! %pokemon%"
    );

    public BattleTowerLocale() {
        super();
    }

    public List<String> getBlacklistedPokemonError() {
        return this.blacklistedPokemonError;
    }
}

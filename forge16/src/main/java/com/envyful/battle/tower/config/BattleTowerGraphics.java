package com.envyful.battle.tower.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.type.Pair;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/guis.yml")
public class BattleTowerGraphics extends AbstractYamlConfig {

    private MainUI mainUI;

    public BattleTowerGraphics() {
        super();
    }

    @ConfigSerializable
    public static class MainUI {

        private ConfigInterface guiSettings = new ConfigInterface(
                "EnvyBattleTower", 3, "BLOCK", ImmutableMap.of("one",
                ConfigItem.builder()
                        .type("minecraft:black_stained_glass_pane")
                        .amount(1)
                        .name(" ")
                        .build()
        ));

        private ExtendedConfigItem leaderboardButton = ExtendedConfigItem.builder()
                .type("minecraft:diamond")
                .name("&aView leaderboard")
                .amount(1)
                .enable()
                .positions(Pair.of(2, 1))
                .build();

        private ExtendedConfigItem startAttemptButton = ExtendedConfigItem.builder()
                .type("minecraft:diamond_sword")
                .name("&aChallenge the tower!")
                .amount(1)
                .enable()
                .positions(Pair.of(4, 1))
                .build();

        private ExtendedConfigItem cooldownButton = ExtendedConfigItem.builder()
                .type("minecraft:red_stained_glass_pane")
                .name("&cYou're on cooldown")
                .lore("%remaining%")
                .amount(1)
                .enable()
                .positions(Pair.of(4, 1))
                .build();

        public MainUI() {
        }

        public ConfigInterface getGuiSettings() {
            return this.guiSettings;
        }

        public ExtendedConfigItem getLeaderboardButton() {
            return this.leaderboardButton;
        }

        public ExtendedConfigItem getStartAttemptButton() {
            return this.startAttemptButton;
        }

        public ExtendedConfigItem getCooldownButton() {
            return this.cooldownButton;
        }
    }
}

package com.envyful.battle.tower.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.type.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/guis.yml")
public class BattleTowerGraphics extends AbstractYamlConfig {

    private MainUI mainUI = new MainUI();
    private LeaderboardUI leaderboardUI = new LeaderboardUI();

    public BattleTowerGraphics() {
        super();
    }

    public MainUI getMainUI() {
        return this.mainUI;
    }

    public LeaderboardUI getLeaderboardUI() {
        return this.leaderboardUI;
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

    @ConfigSerializable
    public static class LeaderboardUI {

        private ConfigInterface guiSettings = new ConfigInterface(
                "EnvyBattleTower", 6, "BLOCK", ImmutableMap.of("one",
                ConfigItem.builder()
                        .type("minecraft:black_stained_glass_pane")
                        .amount(1)
                        .name(" ")
                        .build()
        ));

        private ExtendedConfigItem backButton = ExtendedConfigItem.builder()
                .type("minecraft:bartrier")
                .name("&cBack")
                .amount(1)
                .enable()
                .positions(Pair.of(4, 0))
                .build();

        private ExtendedConfigItem nextPageButton = ExtendedConfigItem.builder()
                .type("minecraft:stone")
                .name("&aNext page")
                .amount(1)
                .enable()
                .positions(Pair.of(8, 0))
                .build();

        private ExtendedConfigItem previousPageButton = ExtendedConfigItem.builder()
                .type("minecraft:stone")
                .name("&aPrevious page")
                .amount(1)
                .enable()
                .positions(Pair.of(0, 0))
                .build();

        private List<Integer> positions = Lists.newArrayList(
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29
        );

        private ConfigItem leaderboardPlayer = ConfigItem.builder()
                .type("minecraft:stone")
                .name("%player% %uuid%")
                .lore("%rank%")
                .amount(1)
                .build();

        private ConfigItem unfilledRank = ConfigItem.builder()
                .type("minecraft:stone")
                .name("None here!")
                .lore("%rank%")
                .amount(1)
                .build();

        private int pages = 1;

        public LeaderboardUI() {
        }

        public ConfigInterface getGuiSettings() {
            return this.guiSettings;
        }

        public ExtendedConfigItem getBackButton() {
            return this.backButton;
        }

        public ExtendedConfigItem getNextPageButton() {
            return this.nextPageButton;
        }

        public ExtendedConfigItem getPreviousPageButton() {
            return this.previousPageButton;
        }

        public int getPages() {
            return this.pages;
        }

        public List<Integer> getPositions() {
            return this.positions;
        }

        public ConfigItem getLeaderboardPlayer() {
            return this.leaderboardPlayer;
        }

        public ConfigItem getUnfilledRank() {
            return this.unfilledRank;
        }
    }
}

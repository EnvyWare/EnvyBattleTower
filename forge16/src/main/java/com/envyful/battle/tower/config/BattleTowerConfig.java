package com.envyful.battle.tower.config;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.database.DatabaseDetailsConfig;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.config.yaml.DefaultConfig;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.reforged.battle.ConfigBattleRule;
import com.envyful.api.sqlite.config.SQLiteDatabaseDetailsConfig;
import com.envyful.battle.tower.api.BattleTower;
import com.envyful.battle.tower.api.FloorPosition;
import com.envyful.battle.tower.api.TeamPossibilities;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.io.IOException;
import java.util.List;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/config.yml")
public class BattleTowerConfig extends AbstractYamlConfig {

    @Comment("The storage type and details for all battle tower player data. Only change this if you know what you're doing. For more information visit https://www.envyware.co.uk/docs/general-help/general-config/config-databases")
    private DatabaseDetailsConfig databaseDetails = new SQLiteDatabaseDetailsConfig("config/EnvyBattleTower/data.db");

    private transient List<BattleTower> battleTowers;

    public BattleTowerConfig() throws IOException {
        super();

        this.battleTowers = YamlConfigFactory.getInstances(BattleTower.class,
                "config/EnvyBattleTower/towers/",
                DefaultConfig.onlyNew("example.yml", BattleTower.builder()
                        .id("example")
                        .allowSpectating(false)
                        .allowExpGain(false)
                        .cooldownSeconds(60)
                        .blacklistedPokemon("shiny")
                        .returnPosition(ConfigLocation.builder().worldName("world").posX(0).posY(0).posZ(0).build())
                        .maxFloor(200)
                        .position(new FloorPosition(
                                ConfigLocation.builder().worldName("world").posX(1).posY(0).posZ(0).build(),
                                ConfigLocation.builder().worldName("world").posX(2).posY(0).posZ(0).build()
                        ))
                        .position(new FloorPosition(
                                ConfigLocation.builder().worldName("world").posX(2).posY(0).posZ(0).build(),
                                ConfigLocation.builder().worldName("world").posX(3).posY(0).posZ(0).build()
                        ))
                        .teamOption(new TeamPossibilities())
                        .battleRule("LEVEL_CAP", new ConfigBattleRule("LEVEL_CAP", "50"))
                        .attemptFinishWinCommand("give %player% diamond 1")
                        .attemptFinishLossCommand("minecraft:tell %player% You lost! Better luck next time")
                        .displayItem(ExtendedConfigItem.builder()
                                .type("pixelmon:ui_element")
                                .amount(1)
                                .name("&aExample Battle Tower")
                                .lore(
                                        "&7This is an example battle tower",
                                        "&7It has a cooldown of 60 seconds",
                                        "&7You can't use shiny Pokemon",
                                        "&7You can't gain exp",
                                        "&7You can't spectate",
                                        " ",
                                        "&aClick to enter!",
                                        " ",
                                        "&aLeaderboard:",
                                        "&71. %leaderboard_1_player% - %leaderboard_1_floor% floors (%leaderboard_1_time%) on %leaderboard_1_date%",
                                        "&72. %leaderboard_2_player% - %leaderboard_2_floor% floors (%leaderboard_2_time%) on %leaderboard_2_date%",
                                        "&73. %leaderboard_3_player% - %leaderboard_3_floor% floors (%leaderboard_3_time%) on %leaderboard_3_date%",
                                        "&74. %leaderboard_4_player% - %leaderboard_4_floor% floors (%leaderboard_4_time%) on %leaderboard_4_date%",
                                        "&75. %leaderboard_5_player% - %leaderboard_5_floor% floors (%leaderboard_5_time%) on %leaderboard_5_date%"
                                )
                                .nbt("UIImage", "pixelmon:textures/gui/uielements/tile_0049.png")
                                .build())
                        .cooldownItem(ExtendedConfigItem.builder()
                                .type("pixelmon:ui_element")
                                .amount(1)
                                .name("&aExample Battle Tower")
                                .lore(
                                        "&c&lYou are on cooldown",
                                        "&c%cooldown%",
                                        " ",
                                        "&aLeaderboard:",
                                        "&71. %leaderboard_1_player% - %leaderboard_1_floor% floors (%leaderboard_1_time%) on %leaderboard_1_date%",
                                        "&72. %leaderboard_2_player% - %leaderboard_2_floor% floors (%leaderboard_2_time%) on %leaderboard_2_date%",
                                        "&73. %leaderboard_3_player% - %leaderboard_3_floor% floors (%leaderboard_3_time%) on %leaderboard_3_date%",
                                        "&74. %leaderboard_4_player% - %leaderboard_4_floor% floors (%leaderboard_4_time%) on %leaderboard_4_date%",
                                        "&75. %leaderboard_5_player% - %leaderboard_5_floor% floors (%leaderboard_5_time%) on %leaderboard_5_date%"
                                )
                                .nbt("UIImage", "pixelmon:textures/gui/uielements/tile_0049.png")
                                .build())
                        .build()));

        this.battleTowers.removeIf(battleTower -> !battleTower.enabled());
    }

    public void init() {
        for (var battleTower : this.battleTowers) {
            battleTower.init();
        }
    }

    public DatabaseDetailsConfig getDatabaseDetails() {
        return this.databaseDetails;
    }

    public List<BattleTower> getBattleTowers() {
        return this.battleTowers;
    }

    public BattleTower getTower(String id) {
        for (BattleTower battleTower : this.battleTowers) {
            if (battleTower.id().equalsIgnoreCase(id)) {
                return battleTower;
            }
        }

        return null;
    }
}

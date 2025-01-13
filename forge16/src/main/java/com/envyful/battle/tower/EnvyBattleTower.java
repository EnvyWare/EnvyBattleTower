package com.envyful.battle.tower;

import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.database.Database;
import com.envyful.api.database.leaderboard.Order;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.command.parser.ForgeAnnotationCommandParser;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.leaderboard.Leaderboard;
import com.envyful.api.platform.PlatformProxy;
import com.envyful.battle.tower.command.BattleTowerCommand;
import com.envyful.battle.tower.command.tab.ForgePlayerCompleter;
import com.envyful.battle.tower.config.BattleTowerConfig;
import com.envyful.battle.tower.config.BattleTowerGraphics;
import com.envyful.battle.tower.config.BattleTowerLocale;
import com.envyful.battle.tower.config.BattleTowerQueries;
import com.envyful.battle.tower.listener.PlayerLogoutListener;
import com.envyful.battle.tower.player.BattleTowerAttribute;
import com.envyful.battle.tower.player.BattleTowerEntry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Mod("envybattletower")
public class EnvyBattleTower {

    private static final Logger LOGGER = LogManager.getLogger("envybattletower");

    private static EnvyBattleTower instance;

    private ForgePlayerManager playerManager = new ForgePlayerManager();
    private ForgeCommandFactory commandFactory = new ForgeCommandFactory(ForgeAnnotationCommandParser::new, playerManager);

    private BattleTowerConfig config;
    private BattleTowerLocale locale;
    private BattleTowerGraphics graphics;
    private Database database;
    private Leaderboard<BattleTowerEntry> leaderboard;

    public EnvyBattleTower() {
        instance = this;
        UtilLogger.setLogger(LOGGER);
        GuiFactory.setPlatformFactory(new ForgeGuiFactory());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PlayerLogoutListener.class);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.reloadConfig();

        this.database = this.config.getDatabaseDetails().createDatabase();



        this.leaderboard = Leaderboard.builder(BattleTowerEntry.class)
                .database(this.database)
                .cacheDuration(TimeUnit.MINUTES.toMillis(10))
                .formatter(BattleTowerEntry::fromQuery)
                .order(Order.DESCENDING)
                .table("envy_battle_tower_players")
                .column("floor_reached")
                .pageSize(10)
                .build();

        getDatabase()
                .update(BattleTowerQueries.CREATE_TABLE)
                .executeAsync();
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        this.playerManager.registerAttribute(BattleTowerAttribute.class, BattleTowerAttribute::new);
        this.commandFactory.registerCompleter(new ForgePlayerCompleter());

        this.commandFactory.registerInjector(ForgeEnvyPlayer.class, (sender, args) -> {
            ForgeEnvyPlayer onlinePlayer = this.playerManager.getOnlinePlayer(args[0]);

            if (onlinePlayer == null) {
                PlatformProxy.sendMessage(sender, List.of("&c&l(!) &cCannot find that player"));
            }

            return onlinePlayer;
        });

        this.commandFactory.registerCommand(event.getDispatcher(), this.commandFactory.parseCommand(new BattleTowerCommand()));
    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(BattleTowerConfig.class);
            this.locale = YamlConfigFactory.getInstance(BattleTowerLocale.class);
            this.graphics = YamlConfigFactory.getInstance(BattleTowerGraphics.class);
        } catch (IOException e) {
            LOGGER.error("Failed to load BattleTower configs", e);
        }
    }

    public static BattleTowerConfig getConfig() {
        return instance.config;
    }

    public static BattleTowerLocale getLocale() {
        return instance.locale;
    }

    public static BattleTowerGraphics getGraphics() {
        return instance.graphics;
    }

    public static Database getDatabase() {
        return instance.database;
    }

    public Leaderboard<BattleTowerEntry> getLeaderboard() {
        return this.leaderboard;
    }

    public static ForgePlayerManager getPlayerManager() {
        return instance.playerManager;
    }

    public static EnvyBattleTower getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}

package com.envyful.battle.tower;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.database.Database;
import com.envyful.api.database.impl.SimpleHikariDatabase;
import com.envyful.api.database.leaderboard.Order;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.leaderboard.Leaderboard;
import com.envyful.battle.tower.command.BattleTowerCommand;
import com.envyful.battle.tower.command.tab.ForgePlayerCompleter;
import com.envyful.battle.tower.config.BattleTowerConfig;
import com.envyful.battle.tower.config.BattleTowerGraphics;
import com.envyful.battle.tower.config.BattleTowerQueries;
import com.envyful.battle.tower.player.BattleTowerAttribute;
import com.envyful.battle.tower.player.BattleTowerEntry;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Mod("envybattletower")
public class EnvyBattleTower {

    private static final Logger LOGGER = LogManager.getLogger("envybattletower");

    private static EnvyBattleTower instance;

    private ForgePlayerManager playerManager = new ForgePlayerManager();
    private ForgeCommandFactory commandFactory = new ForgeCommandFactory(playerManager);

    private BattleTowerConfig config;
    private BattleTowerGraphics graphics;
    private Database database;
    private Leaderboard<BattleTowerEntry> leaderboard;

    public EnvyBattleTower() {
        instance = this;
        UtilLogger.setLogger(LOGGER);
        GuiFactory.setPlatformFactory(new ForgeGuiFactory());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.reloadConfig();
        this.database = new SimpleHikariDatabase(this.config.getDatabaseDetails());
        this.leaderboard = Leaderboard.builder(BattleTowerEntry.class)
                .database(this.database)
                .cacheDuration(TimeUnit.MINUTES.toMillis(10))
                .formatter(BattleTowerEntry::fromQuery)
                .order(Order.DESCENDING)
                .table("envy_battle_tower_players")
                .column("floor_reached")
                .pageSize(10)
                .build();
        UtilConcurrency.runAsync(this::createTable);
    }

    private void createTable() {
        try (Connection connection = this.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(BattleTowerQueries.CREATE_TABLE)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        this.playerManager.registerAttribute(this, BattleTowerAttribute.class);
        this.commandFactory.registerCompleter(new ForgePlayerCompleter());

        this.commandFactory.registerInjector(ForgeEnvyPlayer.class, (sender, args) -> {
            ForgeEnvyPlayer onlinePlayer = this.playerManager.getOnlinePlayer(args[0]);

            if (onlinePlayer == null) {
                sender.sendMessage(UtilChatColour.colour("&c&l(!) &cCannot find that player"), Util.NIL_UUID);
            }

            return onlinePlayer;
        });

        this.commandFactory.registerCommand(event.getDispatcher(), new BattleTowerCommand());
    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(BattleTowerConfig.class);
            this.graphics = YamlConfigFactory.getInstance(BattleTowerGraphics.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BattleTowerConfig getConfig() {
        return this.config;
    }

    public BattleTowerGraphics getGraphics() {
        return this.graphics;
    }

    public Database getDatabase() {
        return this.database;
    }

    public Leaderboard<BattleTowerEntry> getLeaderboard() {
        return this.leaderboard;
    }

    public ForgePlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public static EnvyBattleTower getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}

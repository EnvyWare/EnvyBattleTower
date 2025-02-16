package com.envyful.battle.tower;

import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.type.SQLDatabaseDetails;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.database.Database;
import com.envyful.api.forge.chat.ITextComponentTextFormatter;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.command.parser.ForgeAnnotationCommandParser;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.platform.ForgePlatformHandler;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.platform.PlatformProxy;
import com.envyful.api.player.Attribute;
import com.envyful.api.sqlite.config.SQLiteDatabaseDetailsConfig;
import com.envyful.battle.tower.api.BattleTower;
import com.envyful.battle.tower.api.attribute.BattleTowerAttribute;
import com.envyful.battle.tower.api.attribute.SQLBattleTowerAttributeAdapter;
import com.envyful.battle.tower.api.attribute.SQLiteBattleTowerAttributeAdapter;
import com.envyful.battle.tower.command.BattleTowerCommand;
import com.envyful.battle.tower.command.completer.BattleTowerTabCompleter;
import com.envyful.battle.tower.config.BattleTowerConfig;
import com.envyful.battle.tower.config.BattleTowerGraphics;
import com.envyful.battle.tower.config.BattleTowerLocale;
import com.envyful.battle.tower.listener.PlayerLogoutListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

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

    public EnvyBattleTower() {
        instance = this;
        UtilLogger.setLogger(LOGGER);
        GuiFactory.setPlatformFactory(new ForgeGuiFactory());
        PlatformProxy.setPlayerManager(this.playerManager);
        PlatformProxy.setHandler(ForgePlatformHandler.getInstance());
        PlatformProxy.setTextFormatter(ITextComponentTextFormatter.getInstance());

        SQLiteDatabaseDetailsConfig.register();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PlayerLogoutListener.class);

        this.playerManager.setGlobalSaveMode("SQL");

        this.playerManager.registerAttribute(Attribute.builder(BattleTowerAttribute.class, ForgeEnvyPlayer.class)
                .constructor(BattleTowerAttribute::new)
                .registerAdapter(SQLDatabaseDetails.ID, new SQLBattleTowerAttributeAdapter())
                .registerAdapter(SQLiteDatabaseDetailsConfig.ID, new SQLiteBattleTowerAttributeAdapter())
        );
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.reloadConfig();
        this.database = this.config.getDatabaseDetails().createDatabase();
        this.config.init();

        this.playerManager.overrideSaveMode(BattleTowerAttribute.class, this.config.getDatabaseDetails());
        this.playerManager.getAdapter(BattleTowerAttribute.class).initialize();
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        this.commandFactory.registerCompleter(new BattleTowerTabCompleter());

        this.commandFactory.registerInjector(BattleTower.class, (sender, args) -> {
            var tower = this.config.getTower(args[0]);

            if (tower == null) {
                PlatformProxy.sendMessage(sender, List.of("&c&l(!) &cCannot find that tower"));
            }

            return tower;
        });

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

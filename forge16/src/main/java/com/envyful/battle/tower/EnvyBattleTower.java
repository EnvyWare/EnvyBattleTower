package com.envyful.battle.tower;

import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.battle.tower.config.BattleTowerConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.io.IOException;

@Mod("envybattletower")
public class EnvyBattleTower {

    private static EnvyBattleTower instance;

    private ForgePlayerManager playerManager = new ForgePlayerManager();
    private ForgeCommandFactory commandFactory = new ForgeCommandFactory();

    private BattleTowerConfig config;

    public EnvyBattleTower() {
        instance = this;
        GuiFactory.setPlatformFactory(new ForgeGuiFactory());

        MinecraftForge.EVENT_BUS.register(this);
        this.reloadConfig();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(BattleTowerConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BattleTowerConfig getConfig() {
        return this.config;
    }
}

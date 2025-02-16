package com.envyful.battle.tower.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.battle.tower.gui.BattleTowerUI;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@ConfigPath("config/EnvyBattleTower/guis.yml")
public class BattleTowerGraphics extends AbstractYamlConfig {

    @Comment("The settings for the GUI that opens when the player runs /battletower. If you want to find the settings for specific items you need to look in the battle tower specific configs (e.g. config/EnvyBattleTower/towers/<tower>.yml)")
    private BattleTowerUI battleTowerUI = new BattleTowerUI();

    public BattleTowerGraphics() {
        super();
    }

    public BattleTowerUI getBattleTowerUI() {
        return this.battleTowerUI;
    }
}

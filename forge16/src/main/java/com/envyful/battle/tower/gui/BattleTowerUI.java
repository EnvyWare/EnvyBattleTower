package com.envyful.battle.tower.gui;

import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.api.attribute.BattleTowerAttribute;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class BattleTowerUI {

    private ConfigInterface guiSettings = ConfigInterface.defaultInterface("EnvyBattleTower", 3);

    public void open(ForgeEnvyPlayer player) {
        var attribute = player.getAttributeNow(BattleTowerAttribute.class);
        var pane = this.guiSettings.toPane(player);

        for (var battleTower : EnvyBattleTower.getConfig().getBattleTowers()) {
            var placeholder = attribute.wrap(battleTower);

            if (attribute.onCooldown(battleTower)) {
                battleTower.getCooldownItem().convert(player, pane, placeholder, battleTower);
                continue;
            }

            battleTower.getDisplayItem().convertToBuilder(player, pane, placeholder, battleTower)
                    .syncClick()
                    .singleClick()
                    .clickHandler((envyPlayer, clickType) -> {
                        player.closeInventory();
                        attribute.startAttempt(battleTower);
                    })
                    .build();
        }

        pane.open(player, guiSettings);
    }
}

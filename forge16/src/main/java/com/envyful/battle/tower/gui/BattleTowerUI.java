package com.envyful.battle.tower.gui;

import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.text.parse.SimplePlaceholder;
import com.envyful.api.time.UtilTimeFormat;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.player.BattleTowerAttribute;

import java.util.concurrent.TimeUnit;

public class BattleTowerUI {

    public static void open(ForgeEnvyPlayer player) {
        var config = EnvyBattleTower.getGraphics().getMainUI();
        var attribute = player.getAttributeNow(BattleTowerAttribute.class);
        var lastAttempt = attribute.getLastAttempt();
        var pane = config.getGuiSettings().toPane();

        UtilConfigItem.builder()
                .clickHandler((envyPlayer, clickType) -> LeaderboardUI.open(player, 1))
                .extendedConfigItem(player, pane, config.getLeaderboardButton());

        if (onCooldown(lastAttempt)) {
            UtilConfigItem.builder()
                    .extendedConfigItem(player, pane, config.getCooldownButton(),
                    (SimplePlaceholder) value -> value.replace("%remaining%",
                            UtilTimeFormat.getFormattedDuration(
                                    TimeUnit.SECONDS.toMillis(EnvyBattleTower.getConfig().getCooldownSeconds()) - (System.currentTimeMillis() - lastAttempt.getAttemptStart())
                            )));
        } else {
            UtilConfigItem.builder()
                    .singleClick()
                    .asyncClick(false)
                    .clickHandler((envyPlayer, clickType) -> {
                        player.closeInventory();
                        attribute.startAttempt();
                    })
                    .extendedConfigItem(player, pane, config.getStartAttemptButton());
        }

        GuiFactory.singlePaneGui(config.getGuiSettings(), pane).open(player);
    }

    private static boolean onCooldown(BattleTowerAttribute.AttemptDetails lastAttempt) {
        if (lastAttempt == null) {
            return false;
        }

        return (System.currentTimeMillis() - lastAttempt.getAttemptStart()) <= TimeUnit.SECONDS.toMillis(EnvyBattleTower.getConfig().getCooldownSeconds());
    }

}

package com.envyful.battle.tower.gui;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.config.BattleTowerGraphics;
import com.envyful.battle.tower.player.BattleTowerAttribute;

import java.util.concurrent.TimeUnit;

public class BattleTowerUI {

    public static void open(ForgeEnvyPlayer player) {
        BattleTowerGraphics.MainUI config = EnvyBattleTower.getInstance().getGraphics().getMainUI();
        BattleTowerAttribute attribute = player.getAttribute(EnvyBattleTower.class);
        BattleTowerAttribute.AttemptDetails lastAttempt = attribute.getLastAttempt();

        Pane pane = GuiFactory.paneBuilder()
                .topLeftX(0)
                .topLeftY(0)
                .width(9)
                .height(config.getGuiSettings().getHeight())
                .build();

        UtilConfigInterface.fillBackground(pane, config.getGuiSettings());

        UtilConfigItem.builder()
                .clickHandler((envyPlayer, clickType) -> LeaderboardUI.open(player, 1))
                .extendedConfigItem(player, pane, config.getLeaderboardButton());

        if (onCooldown(lastAttempt)) {
            UtilConfigItem.builder().extendedConfigItem(player, pane, config.getCooldownButton());
        } else {
            UtilConfigItem.builder()
                    .singleClick()
                    .asyncClick(false)
                    .clickHandler((envyPlayer, clickType) -> {
                        player.getParent().closeContainer();
                        attribute.startAttempt();
                    })
                    .extendedConfigItem(player, pane, config.getStartAttemptButton());
        }

        GuiFactory.guiBuilder()
                .setPlayerManager(EnvyBattleTower.getInstance().getPlayerManager())
                .addPane(pane)
                .height(config.getGuiSettings().getHeight())
                .title(UtilChatColour.colour(config.getGuiSettings().getTitle()))
                .build().open(player);
    }

    private static boolean onCooldown(BattleTowerAttribute.AttemptDetails lastAttempt) {
        if (1 == 1) {
            return false;
        }

        if (lastAttempt == null) {
            return false;
        }

        return (System.currentTimeMillis() - lastAttempt.getAttemptStart()) <= TimeUnit.SECONDS.toMillis(EnvyBattleTower.getInstance().getConfig().getCooldownSeconds());
    }

}

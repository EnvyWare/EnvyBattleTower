package com.envyful.battle.tower.gui;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.text.parse.SimplePlaceholder;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.config.BattleTowerGraphics;
import com.envyful.battle.tower.player.BattleTowerEntry;

import java.util.List;

public class LeaderboardUI {

    public static void open(ForgeEnvyPlayer player, int page) {
        BattleTowerGraphics.LeaderboardUI config = EnvyBattleTower.getInstance().getGraphics().getLeaderboardUI();

        Pane pane = GuiFactory.paneBuilder()
                .topLeftX(0)
                .topLeftY(0)
                .width(9)
                .height(config.getGuiSettings().getHeight())
                .build();

        UtilConfigInterface.fillBackground(pane, config.getGuiSettings());

        List<Integer> positions = config.getPositions();
        List<BattleTowerEntry> cache = EnvyBattleTower.getInstance().getLeaderboard().getPage(page - 1);

        for (int i = 0; i < positions.size(); i++) {
            Integer pos = positions.get(i);
            int rank = i + (10 * page) + 1;

            if (cache.size() <= i) {
                pane.set(pos % 9, pos / 9, GuiFactory.displayable(UtilConfigItem.fromConfigItem(config.getUnfilledRank(),
                        (SimplePlaceholder) name -> name.replace("%rank%", String.valueOf(rank)))));
                continue;
            }

            BattleTowerEntry battleTowerEntry = cache.get(i);

            pane.set(pos % 9, pos / 9, GuiFactory.displayable(UtilConfigItem.fromConfigItem(config.getLeaderboardPlayer(),
                    (SimplePlaceholder) name -> name.replace("%player%", battleTowerEntry.getName())
                            .replace("%uuid%", battleTowerEntry.getUuid().toString())
                            .replace("%rank%", String.valueOf(rank)))));
        }

        UtilConfigItem.builder()
                        .clickHandler((envyPlayer, clickType) -> {
                            if (page == config.getPages()) {
                                open(player, 1);
                            } else {
                                open(player, page + 1);
                            }
                        }).extendedConfigItem(player, pane, config.getNextPageButton());

        UtilConfigItem.builder()
                .clickHandler((envyPlayer, clickType) -> {
                    if (page == 1) {
                        open(player, config.getPages());
                    } else {
                        open(player, page - 1);
                    }
                }).extendedConfigItem(player, pane, config.getPreviousPageButton());

        GuiFactory.guiBuilder()
                .setPlayerManager(EnvyBattleTower.getInstance().getPlayerManager())
                .addPane(pane)
                .height(config.getGuiSettings().getHeight())
                .title(UtilChatColour.colour(config.getGuiSettings().getTitle()))
                .build().open(player);
    }
}

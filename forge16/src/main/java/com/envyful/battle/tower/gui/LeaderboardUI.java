package com.envyful.battle.tower.gui;

import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.text.parse.SimplePlaceholder;
import com.envyful.battle.tower.EnvyBattleTower;

public class LeaderboardUI {

    public static void open(ForgeEnvyPlayer player, int page) {
        var config = EnvyBattleTower.getGraphics().getLeaderboardUI();
        var pane = config.getGuiSettings().toPane();

        var positions = config.getPositions();
        var cache = EnvyBattleTower.getInstance().getLeaderboard().getPage(page - 1);

        for (int i = 0; i < positions.size(); i++) {
            Integer pos = positions.get(i);
            int rank = i + (10 * (page - 1)) + 1;

            if (cache.size() <= i) {
                pane.set(pos % 9, pos / 9, GuiFactory.displayable(UtilConfigItem.fromConfigItem(config.getUnfilledRank(),
                        (SimplePlaceholder) name -> name.replace("%rank%", String.valueOf(rank)))));
                continue;
            }

            var battleTowerEntry = cache.get(i);

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

        GuiFactory.singlePaneGui(config.getGuiSettings(), pane).open(player);
    }
}

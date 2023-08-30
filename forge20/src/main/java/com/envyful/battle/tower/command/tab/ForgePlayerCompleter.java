package com.envyful.battle.tower.command.tab;

import com.envyful.api.command.injector.TabCompleter;
import com.envyful.api.forge.command.completion.player.ExcludeSelfCompletion;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.google.common.collect.Lists;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.annotation.Annotation;
import java.util.List;

public class ForgePlayerCompleter implements TabCompleter<String, ForgeEnvyPlayer> {

    @Override
    public Class<ForgeEnvyPlayer> getSenderClass() {
        return ForgeEnvyPlayer.class;
    }

    @Override
    public Class<String> getCompletedClass() {
        return String.class;
    }

    @Override
    public List<String> getCompletions(ForgeEnvyPlayer sender, String[] currentData, Annotation... completionData) {
        List<String> playerNames = Lists.newArrayList();

        for (var player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (completionData.length < 1 || completionData[0] instanceof ExcludeSelfCompletion) {
                if (player.getName().equals(sender.getName())) {
                    continue;
                }
            }

            playerNames.add(player.getName().getString());
        }

        return playerNames;
    }
}

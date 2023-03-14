package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Child;
import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.command.tab.ForgePlayerCompleter;
import com.envyful.battle.tower.player.BattleTowerAttribute;
import net.minecraft.command.ICommandSource;

@Command(
        value = "resetcooldown",
        description = "Resets the cooldown for a player"
)
@Permissible("com.envyful.battle.tower.command.resetcooldown")
@Child
public class ResetCooldownCommand {

    @CommandProcessor
    public void onCommand(@Sender ICommandSource sender,
                          @Completable(ForgePlayerCompleter.class) @Argument ForgeEnvyPlayer target) {
        BattleTowerAttribute attribute = target.getAttribute(EnvyBattleTower.class);

        if (attribute == null) {
            return;
        }

        attribute.setLastAttempt(null);

    }
}

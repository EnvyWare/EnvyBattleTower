package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.command.completion.player.PlayerTabCompleter;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.platform.Messageable;
import com.envyful.battle.tower.api.BattleTower;
import com.envyful.battle.tower.api.attribute.BattleTowerAttribute;
import com.envyful.battle.tower.command.completer.BattleTowerTabCompleter;

import java.util.List;

@Command(
        value = "resetcooldown"
)
@Permissible("com.envyful.battle.tower.command.resetcooldown")
public class ResetCooldownCommand {

    @CommandProcessor
    public void onCommand(@Sender Messageable<?> sender,
                          @Completable(PlayerTabCompleter.class) @Argument ForgeEnvyPlayer target,
                          @Completable(BattleTowerTabCompleter.class) @Argument BattleTower tower,
                          String[] args) {
        var attribute = target.getAttributeNow(BattleTowerAttribute.class);

        if (attribute == null) {
            sender.message(List.of("Failed to reset cooldown for " + target.getName() + " please try again in a minute!"));
            return;
        }

        attribute.clearCooldown(tower);
        sender.message(List.of("Cooldown reset for " + target.getName()));
    }
}

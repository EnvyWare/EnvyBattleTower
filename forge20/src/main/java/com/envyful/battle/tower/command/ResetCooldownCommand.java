package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.battle.tower.command.tab.ForgePlayerCompleter;
import com.envyful.battle.tower.player.BattleTowerAttribute;
import net.minecraft.commands.CommandSource;

@Command(
        value = "resetcooldown"
)
@Permissible("com.envyful.battle.tower.command.resetcooldown")
public class ResetCooldownCommand {

    @CommandProcessor
    public void onCommand(@Sender CommandSource sender,
                          @Completable(ForgePlayerCompleter.class) @Argument ForgeEnvyPlayer target) {
        BattleTowerAttribute attribute = target.getAttribute(BattleTowerAttribute.class);

        if (attribute == null) {
            sender.sendSystemMessage(UtilChatColour.colour("Failed to reset cooldown for " + target.getName() + " please try again in a minute!"));
            return;
        }

        attribute.setLastAttempt(new BattleTowerAttribute.AttemptDetails(0, 0, 0));
        sender.sendSystemMessage(UtilChatColour.colour("Cooldown reset for " + target.getName()));
    }
}

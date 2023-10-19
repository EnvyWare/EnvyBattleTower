package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.battle.tower.EnvyBattleTower;
import net.minecraft.commands.CommandSource;

@Command(
        value = "reload"
)
@Permissible("com.envyful.battle.tower.command.reload")
public class ReloadCommand {

    @CommandProcessor
    public void onCommand(@Sender CommandSource sender) {
        EnvyBattleTower.getInstance().reloadConfig();
        sender.sendSystemMessage(UtilChatColour.colour("&a&l(!) &aReloaded"));
    }
}

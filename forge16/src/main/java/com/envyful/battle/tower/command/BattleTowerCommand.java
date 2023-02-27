package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.gui.BattleTowerUI;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.entity.player.ServerPlayerEntity;

@Command(
        value = "envybattletower",
        description = "Battle tower command",
        aliases = {
                "battletower",
                "ebattletower",
                "bt"
        }
)
@Permissible("com.envyful.battle.tower.command")
@SubCommands({ReloadCommand.class, ValidateCommand.class})
public class BattleTowerCommand {

    @CommandProcessor
    public void onCommand(@Sender ServerPlayerEntity sender) {
        if (StorageProxy.getParty(sender).guiOpened) {
            return;
        }

        BattleTowerUI.open(EnvyBattleTower.getInstance().getPlayerManager().getPlayer(sender));
    }
}

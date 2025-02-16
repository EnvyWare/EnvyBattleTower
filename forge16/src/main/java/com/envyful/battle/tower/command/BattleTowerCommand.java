package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.battle.tower.EnvyBattleTower;

@Command(
        value = {
                "envybattletower",
                "battletower",
                "ebattletower",
                "bt"
        }
)
@Permissible("com.envyful.battle.tower.command")
@SubCommands({ReloadCommand.class, ValidateCommand.class, ResetCooldownCommand.class})
public class BattleTowerCommand {

    @CommandProcessor
    public void onCommand(@Sender ForgeEnvyPlayer sender) {
        EnvyBattleTower.getGraphics().getBattleTowerUI().open(sender);
    }
}

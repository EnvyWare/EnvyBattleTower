package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.platform.Messageable;
import com.envyful.battle.tower.EnvyBattleTower;

import java.util.List;

@Command(
        value = "reload"
)
@Permissible("com.envyful.battle.tower.command.reload")
public class ReloadCommand {

    @CommandProcessor
    public void onCommand(@Sender Messageable<?> sender) {
        EnvyBattleTower.getInstance().reloadConfig();
        EnvyBattleTower.getConfig().init();
        sender.message(List.of("&a&l(!) &aReloaded"));
    }
}

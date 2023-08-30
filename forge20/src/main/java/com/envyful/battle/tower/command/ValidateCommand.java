package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Child;
import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.config.BattleTowerConfig;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

@Command(
        value = "validate",
        description = "Validates the pokepastes"
)
@Permissible("com.envyful.battle.tower.command.validate")
@Child
public class ValidateCommand {

    @CommandProcessor
    public void onCommand(@Sender ServerPlayer sender) {
        boolean found = false;

        for (BattleTowerConfig.TeamPossibilities teamPossibility : EnvyBattleTower.getInstance().getConfig().getTeamPossibilities()) {
            for (BattleTowerConfig.PokePaste pokePaste : teamPossibility.getTeams().getWeightedSet().keySet()) {
                List<Pokemon> team = pokePaste.getTeam();

                if (team == null || team.isEmpty()) {
                    found = true;
                    sender.sendSystemMessage(UtilChatColour.colour("&e&l(!) &eFound invalid team for " +
                            pokePaste.getPaste()).copy()
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, pokePaste.getPaste()))));
                }
            }
        }

        if (!found) {
            sender.sendSystemMessage(UtilChatColour.colour("&e&l(!) &eNo invalid pastes found"));
        }
    }
}

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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.event.ClickEvent;

import java.util.List;

@Command(
        value = "validate",
        description = "Validates the pokepastes"
)
@Permissible("com.envyful.battle.tower.command.validate")
@Child
public class ValidateCommand {

    @CommandProcessor
    public void onCommand(@Sender ServerPlayerEntity sender) {
        boolean found = false;

        for (BattleTowerConfig.TeamPossibilities teamPossibility : EnvyBattleTower.getInstance().getConfig().getTeamPossibilities()) {
            for (BattleTowerConfig.PokePaste pokePaste : teamPossibility.getTeams().getWeightedSet().keySet()) {
                List<Pokemon> team = pokePaste.getTeam();

                if (team == null || team.isEmpty()) {
                    found = true;
                    sender.sendMessage(UtilChatColour.colour("&e&l(!) &eFound invalid team for " + pokePaste.getPaste()).copy().withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, pokePaste.getPaste()))), Util.NIL_UUID);
                }
            }
        }

        if (!found) {
            sender.sendMessage(UtilChatColour.colour("&e&l(!) &eNo invalid pastes found"), Util.NIL_UUID);
        }
    }
}

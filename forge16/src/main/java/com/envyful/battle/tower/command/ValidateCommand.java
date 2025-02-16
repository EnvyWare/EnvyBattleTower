package com.envyful.battle.tower.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.platform.Messageable;
import com.envyful.battle.tower.EnvyBattleTower;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

import java.util.List;

@Command(
        value = "validate"
)
@Permissible("com.envyful.battle.tower.command.validate")
public class ValidateCommand {

    @CommandProcessor
    public void onCommand(@Sender Messageable<?> sender,
                          String[] args) {
        boolean found = false;

        for (var tower : EnvyBattleTower.getConfig().getBattleTowers()) {
            for (var teamPossibility : tower.getTeamPossibilities()) {
                for (var pokePaste : teamPossibility.getTeams().getWeightedSet().keySet()) {
                    List<Pokemon> team = pokePaste.getTeam();

                    if (team == null || team.isEmpty()) {
                        found = true;
                        sender.message(List.of("&e&l(!) &eFound invalid team for " + pokePaste.getPaste()));
                    }
                }
            }
        }

        if (!found) {
            sender.message(List.of("&e&l(!) &eNo invalid pastes found"));
        }
    }
}

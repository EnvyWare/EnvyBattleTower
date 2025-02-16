package com.envyful.battle.tower.command.completer;

import com.envyful.api.command.injector.TabCompleter;
import com.envyful.api.platform.Messageable;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.api.BattleTower;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class BattleTowerTabCompleter implements TabCompleter<Messageable<?>> {
    @Override
    public List<String> getCompletions(Messageable<?> messageable, String[] strings, Annotation... annotations) {
        return EnvyBattleTower.getConfig().getBattleTowers().stream().map(BattleTower::id).collect(Collectors.toList());
    }
}

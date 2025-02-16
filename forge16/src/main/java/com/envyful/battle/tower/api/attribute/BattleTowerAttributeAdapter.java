package com.envyful.battle.tower.api.attribute;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.leaderboard.Leaderboard;
import com.envyful.api.player.attribute.adapter.AttributeAdapter;
import com.envyful.battle.tower.api.AttemptDetails;
import com.envyful.battle.tower.api.BattleTowerEntry;

public interface BattleTowerAttributeAdapter extends AttributeAdapter<BattleTowerAttribute> {

    void addAttempt(ForgeEnvyPlayer player, AttemptDetails entry);

    Leaderboard<BattleTowerEntry> getLeaderboard(String tower);

}

package com.envyful.battle.tower.listener;

import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.player.BattleTowerAttribute;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerLogoutListener {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
        var player = EnvyBattleTower.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) event.getPlayer());

        if (player == null){
            return;
        }

        var attribute = player.getAttribute(BattleTowerAttribute.class);

        if (attribute == null){
            return;
        }

        if (attribute.isAttempting()){
            attribute.finishAttempt();
        }
    }

}

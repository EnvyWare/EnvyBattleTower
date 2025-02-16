package com.envyful.battle.tower.api.attribute;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.attribute.ManagedForgeAttribute;
import com.envyful.api.forge.world.UtilWorld;
import com.envyful.api.math.UtilRandom;
import com.envyful.api.platform.PlatformProxy;
import com.envyful.api.reforged.battle.BattleBuilder;
import com.envyful.api.reforged.battle.BattleParticipantBuilder;
import com.envyful.api.text.Placeholder;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.api.AttemptDetails;
import com.envyful.battle.tower.api.BattleTower;
import com.envyful.battle.tower.api.FloorPosition;
import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.api.battles.BattleAIMode;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.storage.TrainerPartyStorage;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.status.NoStatus;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ServerNPCRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.enums.EnumMegaItemsUnlocked;
import com.pixelmonmod.pixelmon.enums.EnumOldGenMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BattleTowerAttribute extends ManagedForgeAttribute<EnvyBattleTower> {

    protected Map<String, AttemptDetails> lastAttemptByTower = new HashMap<>();
    protected Map<String, AttemptDetails> bestAttemptByTower = new HashMap<>();
    protected String username;

    private long attemptStart;
    private int currentFloor;
    private Map<UUID, ItemStack> heldItem = Maps.newHashMap();
    private BattleTower currentTower;

    public BattleTowerAttribute(UUID uuid) {
        super(uuid, EnvyBattleTower.getInstance());
    }

    @Override
    public void setParent(ForgeEnvyPlayer parent) {
        super.setParent(parent);

        this.username = parent.getName();
    }

    public boolean onCooldown(BattleTower tower) {
        if (!lastAttemptByTower.containsKey(tower.id().toLowerCase(Locale.ROOT))) {
            return false;
        }

        return this.lastAttemptByTower.get(tower.id().toLowerCase(Locale.ROOT)).getAttemptStart() +
                TimeUnit.SECONDS.toMillis(tower.getCooldownSeconds()) > System.currentTimeMillis();
    }

    public void clearCooldown(BattleTower battleTower) {
        this.lastAttemptByTower.remove(battleTower.id().toLowerCase(Locale.ROOT));
    }

    public Placeholder wrap(BattleTower battleTower) {
        var lastAttempt = this.lastAttemptByTower.get(battleTower.id().toLowerCase(Locale.ROOT));
        var bestAttempt = this.bestAttemptByTower.get(battleTower.id().toLowerCase(Locale.ROOT));
        return Placeholder.composition(
                Placeholder.simple("%cooldown%", lastAttempt == null ? "N/A" : EnvyBattleTower.getLocale().getTimeFormat().format(
                        lastAttempt.getAttemptStart() + TimeUnit.SECONDS.toMillis(battleTower.getCooldownSeconds()) - System.currentTimeMillis()
                )),
                Placeholder.simple("%best_floor%", bestAttempt == null ? "N/A" : String.valueOf(bestAttempt.getFloorReached())),
                Placeholder.simple("%best_time%", bestAttempt == null ? "N/A" : EnvyBattleTower.getLocale().getTimeFormat().format(bestAttempt.getAttemptDuration())),
                Placeholder.simple("%last_floor%", lastAttempt == null ? "N/A" : String.valueOf(lastAttempt.getFloorReached()))
        );
    }

    public boolean inAttempt() {
        return this.currentTower != null;
    }

    public void startAttempt(BattleTower tower) {
        this.attemptStart = System.currentTimeMillis();
        this.currentFloor = 1;
        this.currentTower = tower;

        var blacklisted = tower.hasBlacklistedPokemon(this.parent);

        if (blacklisted != null) {
            this.parent.message(EnvyBattleTower.getLocale().getBlacklistedPokemonError(),
                    Placeholder.simple("%pokemon%", blacklisted.getDisplayName())
            );
            return;
        }

        var randomElement = UtilRandom.getRandomElement(tower.getPositions());

        if (randomElement == null) {
            EnvyBattleTower.getLogger().error("Could not find any trainer positions set for BattleTower '{}'", tower.id());
            this.finishAttempt();
            return;
        }

        this.beginBattle(randomElement);
    }

    public void beginBattle(FloorPosition position) {
        var world = UtilWorld.findWorld(position.getTrainerPosition().getWorldName());

        if (world == null) {
            EnvyBattleTower.getLogger().error("Invalid World Name {} found in BattleTower {} ", position.getTrainerPosition().getWorldName(), this.currentTower.id());
            this.finishAttempt();
            return;
        }

        var trainer = new NPCTrainer(UtilWorld.findWorld(position.getTrainerPosition().getWorldName()));

        this.currentTower.getRandomLeaderTeam(this.currentFloor).whenCompleteAsync((randomLeaderTeam, throwable) -> {
            if (randomLeaderTeam == null) {
                EnvyBattleTower.getLogger().error("There was not a valid team found for " + this.currentFloor + ". Ending attempt safely");
                this.finishAttempt();
                return;
            }

            trainer.setPos(position.getTrainerPosition().getPosX(), position.getTrainerPosition().getPosY(), position.getTrainerPosition().getPosZ());
            trainer.yRot = (float) position.getTrainerPosition().getPitch();
            trainer.xRot = (float) position.getTrainerPosition().getYaw();
            trainer.setNoAi(true);
            trainer.init(ServerNPCRegistry.trainers.getRandomBaseWithData());
            trainer.setBattleAIMode(BattleAIMode.ADVANCED);
            trainer.winMoney = 0;
            trainer.winMessage = "";
            trainer.loseMessage = "";
            trainer.setMegaItem(EnumMegaItemsUnlocked.Both);
            trainer.setOldGenMode(EnumOldGenMode.Both);
            TrainerPartyStorage pokemonStorage = trainer.getPokemonStorage();

            for(int i = 0; i < 6; ++i) {
                pokemonStorage.set(i, null);
            }

            for (Pokemon pokemon : randomLeaderTeam.getY()) {
                pokemonStorage.add(pokemon);
            }

            if (!currentTower.isVirtual()) {
                this.parent.getParent().level.addFreshEntity(trainer);
                this.parent.teleport(position.getPlayerPosition());
            }

            for (var pokemon : StorageProxy.getParty(this.parent.getParent()).getAll()) {
                if (pokemon != null) {
                    pokemon.setStatus(NoStatus.noStatus);
                    pokemon.heal();
                    ItemStack itemStack = this.heldItem.remove(pokemon.getUUID());

                    if (itemStack != null) {
                        pokemon.setHeldItem(itemStack);
                    }

                    if (pokemon.getHeldItem() != null) {
                        this.heldItem.put(pokemon.getUUID(), pokemon.getHeldItem().copy());
                    }

                    pokemon.getPixelmonEntity().ifPresent(PixelmonEntity::resetDataWatchers);
                    pokemon.getPixelmonEntity().ifPresent(pixelmonEntity -> pixelmonEntity.update(EnumUpdateType.ALL));
                }
            }

            BattleBuilder.builder()
                    .startSync()
                    .startDelayTicks(5L)
                    .teamOne(BattleParticipantBuilder.builder().entity(this.parent.getParent()).build())
                    .teamTwo(BattleParticipantBuilder.builder().entity(trainer).team(randomLeaderTeam.getY().toArray(new Pokemon[0])).build())
                    .teamSelection()
                    .teamSelectionBuilder(TeamSelectionRegistry.builder().notCloseable().hideOpponentTeam().showRules(false))
                    .rules(this.createRules())
                    .expEnabled(this.currentTower.isAllowExpGain())
                    .allowSpectators(this.currentTower.isAllowSpectating())
                    .startHandler(battleStartedEvent -> {})
                    .endHandler(battleEndEvent -> {
                        trainer.remove();

                        if (battleEndEvent.isAbnormal()) {
                            PlatformProxy.executeConsoleCommands(randomLeaderTeam.getX().getPlayerLossCommands(),
                                    Placeholder.simple("%player%", this.parent.getName()),
                                    Placeholder.simple("%floor%", this.currentFloor)
                            );

                            this.finishAttempt();
                            return;
                        }

                        BattleResults battleResults = battleEndEvent.getResult(this.parent.getParent()).orElse(null);

                        if (battleResults == null) {
                            PlatformProxy.executeConsoleCommands(randomLeaderTeam.getX().getPlayerLossCommands(),
                                    Placeholder.simple("%player%", this.parent.getName()),
                                    Placeholder.simple("%floor%", this.currentFloor));
                            this.finishAttempt();
                            return;
                        }

                        if (battleResults != BattleResults.VICTORY) {
                            PlatformProxy.executeConsoleCommands(randomLeaderTeam.getX().getPlayerLossCommands(),
                                    Placeholder.simple("%player%", this.parent.getName()),
                                    Placeholder.simple("%floor%", this.currentFloor)
                            );
                            PlatformProxy.executeConsoleCommands(this.currentTower.getAttemptFinishLossCommands(),
                                    Placeholder.simple("%player%", this.parent.getName()),
                                    Placeholder.simple("%floor%", this.currentFloor)
                            );

                            this.finishAttempt();
                            return;
                        }

                        if (!this.currentTower.canContinue(this.currentFloor + 1)) {
                            this.finishAttempt();
                            PlatformProxy.executeConsoleCommands(this.currentTower.getAttemptFinishWinCommands(),
                                    Placeholder.simple("%player%", this.parent.getName()),
                                    Placeholder.simple("%floor%", this.currentFloor)
                            );

                            PlatformProxy.executeConsoleCommands(randomLeaderTeam.getX().getPlayerWinCommands(),
                                    Placeholder.simple("%player%", this.parent.getName()),
                                    Placeholder.simple("%floor%", this.currentFloor)
                            );
                            return;
                        }

                        PlatformProxy.executeConsoleCommands(this.currentTower.getAttemptFinishWinCommands(),
                                Placeholder.simple("%player%", this.parent.getName()),
                                Placeholder.simple("%floor%", this.currentFloor)
                        );

                        this.currentFloor++;
                        this.beginBattle(position);
                    })
                    .start();
        }, ServerLifecycleHooks.getCurrentServer());
    }

    private BattleRules createRules() {
        var battleRules = new BattleRules().set(BattleRuleRegistry.BATTLE_TYPE, BattleType.SINGLE);

        for (var rule : this.currentTower.getRules()) {
            rule.with(battleRules);
        }

        return battleRules;
    }

    public void finishAttempt() {
        if (!this.inAttempt()) {
            return;
        }

        this.heldItem.clear();
        long duration = System.currentTimeMillis() - this.attemptStart;

        var attempt = new AttemptDetails(this.currentTower, this.attemptStart, duration, this.currentFloor);
        var adapter = (BattleTowerAttributeAdapter) EnvyBattleTower.getPlayerManager().getAdapter(BattleTowerAttribute.class);

        adapter.addAttempt(this.parent, attempt);

        var bestAttempt = this.bestAttemptByTower.get(this.currentTower.id().toLowerCase(Locale.ROOT));

        this.lastAttemptByTower.put(this.currentTower.id().toLowerCase(Locale.ROOT), attempt);

        if (bestAttempt == null || bestAttempt.getFloorReached() < attempt.getFloorReached()) {
            this.bestAttemptByTower.put(this.currentTower.id().toLowerCase(Locale.ROOT), attempt);
        }

        if (!currentTower.isVirtual()) {
            this.parent.teleport(this.currentTower.getReturnPosition());
        }

        this.attemptStart = -1;
        this.currentFloor = 0;
        this.currentTower = null;
    }

    public boolean isAttempting() {
        return this.attemptStart != -1 && currentFloor > 0;
    }
}

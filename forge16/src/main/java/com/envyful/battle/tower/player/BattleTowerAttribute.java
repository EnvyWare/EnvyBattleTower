package com.envyful.battle.tower.player;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.attribute.AbstractForgeAttribute;
import com.envyful.api.forge.server.UtilForgeServer;
import com.envyful.api.forge.world.UtilWorld;
import com.envyful.api.math.UtilRandom;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.reforged.battle.BattleBuilder;
import com.envyful.api.reforged.battle.BattleParticipantBuilder;
import com.envyful.api.reforged.battle.ConfigBattleRule;
import com.envyful.api.type.Pair;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.config.BattleTowerConfig;
import com.envyful.battle.tower.config.BattleTowerQueries;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.battles.BattleAIMode;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.storage.TrainerPartyStorage;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ServerNPCRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BattleTowerAttribute extends AbstractForgeAttribute<EnvyBattleTower> {

    private List<AttemptDetails> attempts = Lists.newArrayList();
    private AttemptDetails lastAttempt = null;
    private AttemptDetails bestAttempt = null;

    private long attemptStart;
    private int currentFloor;

    public BattleTowerAttribute(EnvyBattleTower manager, EnvyPlayer<?> parent) {
        super(manager, (ForgeEnvyPlayer) parent);
    }

    public BattleTowerAttribute(UUID uuid) {
        super(uuid);
    }

    public AttemptDetails getLastAttempt() {
        if (this.lastAttempt == null) {
            if (this.attempts.isEmpty()) {
                return null;
            }

            AttemptDetails lastAttempt = null;

            for (AttemptDetails attempt : this.attempts) {
                if (lastAttempt == null) {
                    lastAttempt = attempt;
                } else if (lastAttempt.getAttemptStart() < attempt.getAttemptStart()) {
                    lastAttempt = attempt;
                }
            }

            this.lastAttempt = lastAttempt;
        }

        return lastAttempt;
    }

    public AttemptDetails getBestAttempt() {
        if (this.bestAttempt == null) {
            if (this.attempts.isEmpty()) {
                return null;
            }

            AttemptDetails bestAttempt = null;

            for (AttemptDetails attempt : this.attempts) {
                if (bestAttempt == null) {
                    bestAttempt = attempt;
                } else if (bestAttempt.getFloorReached() < attempt.getFloorReached()) {
                    bestAttempt = attempt;
                }
            }
            this.bestAttempt = bestAttempt;
        }

        return bestAttempt;
    }

    public void startAttempt() {
        this.attemptStart = System.currentTimeMillis();
        this.currentFloor = 1;
        this.beginBattle();
    }

    public void beginBattle() {
        BattleTowerConfig.PossiblePosition position = UtilRandom.getRandomElement(this.manager.getConfig().getPositions());
        NPCTrainer trainer = new NPCTrainer(UtilWorld.findWorld(position.getTrainerPosition().getWorldName()));
        Pair<BattleTowerConfig.PokePaste, List<Pokemon>> randomLeaderTeam = getRandomLeaderTeam();

        trainer.setPos(position.getTrainerPosition().getPosX(), position.getTrainerPosition().getPosY(), position.getTrainerPosition().getPosZ());
        trainer.yRot = (float) position.getTrainerPosition().getPitch();
        trainer.xRot = (float) position.getTrainerPosition().getYaw();
        trainer.setBattleAIMode(BattleAIMode.ADVANCED);
        trainer.setNoAi(true);
        trainer.init(ServerNPCRegistry.trainers.getRandomBaseWithData());
        TrainerPartyStorage pokemonStorage = trainer.getPokemonStorage();

        for(int i = 0; i < 6; ++i) {
            pokemonStorage.set(i, null);
        }

        for (Pokemon pokemon : randomLeaderTeam.getY()) {
            pokemonStorage.add(pokemon);
        }

        this.getParent().getParent().level.addFreshEntity(trainer);

        this.getParent().teleport(position.getPlayerPosition());

        StorageProxy.getParty(this.getParent().getParent()).heal();

        BattleBuilder.builder()
                .startSync()
                .startDelayTicks(5L)
                .teamOne(BattleParticipantBuilder.builder().entity(this.getParent().getParent()).build())
                .teamTwo(BattleParticipantBuilder.builder().entity(trainer).team(randomLeaderTeam.getY().toArray(new Pokemon[0])).build())
                .teamSelection()
                .teamSelectionBuilder(TeamSelectionRegistry.builder().notCloseable().hideOpponentTeam().showRules(false))
                .rules(this.createRules())
                .expEnabled(this.manager.getConfig().isAllowExpGain())
                .allowSpectators(false)
                .startHandler(battleStartedEvent -> {})
                .endHandler(battleEndEvent -> {
                    trainer.remove();

                    if (battleEndEvent.isAbnormal()) {
                        for (String command : randomLeaderTeam.getX().getPlayerLossCommands()) {
                            UtilForgeServer.executeCommand(command
                                    .replace("%player%", this.getParent().getName())
                                    .replace("%floor%", String.valueOf(this.currentFloor))
                            );
                        }

                        this.finishAttempt();
                        return;
                    }

                    BattleResults battleResults = battleEndEvent.getResult(this.getParent().getParent()).orElse(null);

                    if (battleResults == null) {
                        for (String command : randomLeaderTeam.getX().getPlayerLossCommands()) {
                            UtilForgeServer.executeCommand(command
                                    .replace("%player%", this.getParent().getName())
                                    .replace("%floor%", String.valueOf(this.currentFloor))
                            );
                        }

                        this.finishAttempt();
                        return;
                    }

                    if (battleResults != BattleResults.VICTORY) {
                        for (String command : randomLeaderTeam.getX().getPlayerLossCommands()) {
                            UtilForgeServer.executeCommand(command
                                    .replace("%player%", this.getParent().getName())
                                    .replace("%floor%", String.valueOf(this.currentFloor))
                            );
                        }

                        this.finishAttempt();
                        return;
                    }

                    System.out.println("CURRENT FLOOR: " + this.currentFloor);

                    if (!this.manager.getConfig().canContinue(this.currentFloor + 1)) {
                        this.finishAttempt();
                        for (String command : this.manager.getConfig().getAttemptFinishWinCommands()) {
                            UtilForgeServer.executeCommand(command
                                    .replace("%player%", this.getParent().getName())
                                    .replace("%floor%", String.valueOf(this.currentFloor))
                            );
                        }

                        for (String command : randomLeaderTeam.getX().getPlayerWinCommands()) {
                            UtilForgeServer.executeCommand(command
                                    .replace("%player%", this.getParent().getName())
                                    .replace("%floor%", String.valueOf(this.currentFloor))
                            );
                        }
                        return;
                    }

                    for (String command : randomLeaderTeam.getX().getPlayerWinCommands()) {
                        UtilForgeServer.executeCommand(command
                                .replace("%player%", this.getParent().getName())
                                .replace("%floor%", String.valueOf(this.currentFloor))
                        );
                    }

                    this.currentFloor++;
                    this.beginBattle();
                })
                .start();
    }

    private Pair<BattleTowerConfig.PokePaste, List<Pokemon>> getRandomLeaderTeam() {
        List<Pokemon> team = Lists.newArrayList();
        BattleTowerConfig.PokePaste random = this.manager.getConfig().getTeamPossibilities(this.currentFloor).getTeams().getWeightedSet().getRandom();

        for (Pokemon pokemon : random.getTeam()) {
            Pokemon copy = PokemonBuilder.copy(pokemon).build();
            copy.heal();
            team.add(copy);
        }

        return Pair.of(random, team);
    }

    private BattleRules createRules() {
        BattleRules battleRules = new BattleRules().set(BattleRuleRegistry.BATTLE_TYPE, BattleType.SINGLE);

        for (ConfigBattleRule rule : this.manager.getConfig().getRules()) {
            battleRules.set(BattleRuleRegistry.getProperty(rule.getBattleRuleType()), rule.getBattleRuleValue());
        }

        return battleRules;
    }

    public void finishAttempt() {
        long duration = System.currentTimeMillis() - this.attemptStart;

        try (Connection connection = this.manager.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(BattleTowerQueries.ADD_USER_ATTEMPT)) {
            preparedStatement.setString(1, this.parent.getUuid().toString());
            preparedStatement.setString(2, this.parent.getName());
            preparedStatement.setLong(3, this.attemptStart);
            preparedStatement.setLong(4, duration);
            preparedStatement.setInt(5, this.currentFloor);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        AttemptDetails attempt = new AttemptDetails(this.attemptStart, duration, this.currentFloor);
        this.attempts.add(attempt);
        this.lastAttempt = attempt;


        if (this.bestAttempt == null || this.bestAttempt.getFloorReached() < attempt.getFloorReached()) {
            this.bestAttempt = attempt;
        }

        this.attemptStart = -1;
        this.currentFloor = 0;
    }

    @Override
    public void load() {
        try (Connection connection = this.manager.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(BattleTowerQueries.LOAD_USER_ATTEMPTS)) {
            preparedStatement.setString(1, this.parent.getUuid().toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    this.attempts.add(new AttemptDetails(
                            resultSet.getLong("start"),
                            resultSet.getLong("duration"),
                            resultSet.getInt("floor_reached")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try (Connection connection = this.manager.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(BattleTowerQueries.UPDATE_USERNAME)) {
            preparedStatement.setString(1, this.parent.getName());
            preparedStatement.setString(2, this.parent.getUuid().toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class AttemptDetails {

        private long attemptStart;
        private long attemptDuration;
        private int floorReached;

        public AttemptDetails(long attemptStart, long attemptDuration, int floorReached) {
            this.attemptStart = attemptStart;
            this.attemptDuration = attemptDuration;
            this.floorReached = floorReached;
        }

        public long getAttemptStart() {
            return this.attemptStart;
        }

        public long getAttemptDuration() {
            return this.attemptDuration;
        }

        public int getFloorReached() {
            return this.floorReached;
        }
    }
}

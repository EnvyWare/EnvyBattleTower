package com.envyful.battle.tower.player;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.attribute.AbstractForgeAttribute;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.config.BattleTowerQueries;
import com.google.common.collect.Lists;

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

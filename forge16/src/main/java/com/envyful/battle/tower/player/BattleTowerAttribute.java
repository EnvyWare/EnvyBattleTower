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

    public BattleTowerAttribute(EnvyBattleTower manager, EnvyPlayer<?> parent) {
        super(manager, (ForgeEnvyPlayer) parent);
    }

    public BattleTowerAttribute(UUID uuid) {
        super(uuid);
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

package com.envyful.battle.tower.api.attribute;

import com.envyful.api.database.leaderboard.Order;
import com.envyful.api.database.sql.SqlType;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.leaderboard.Leaderboard;
import com.envyful.battle.tower.EnvyBattleTower;
import com.envyful.battle.tower.api.AttemptDetails;
import com.envyful.battle.tower.api.BattleTowerEntry;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SQLBattleTowerAttributeAdapter implements BattleTowerAttributeAdapter {

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `envy_battle_tower_players`(" +
            "id             INT             UNSIGNED        NOT NULL    AUTO_INCREMENT, " +
            "uuid           VARCHAR(64)     NOT NULL, " +
            "name           VARCHAR(16)     NOT NULL, "  +
            "start          BIGINT          UNSIGNED        NOT NULL, " +
            "duration       BIGINT          UNSIGNED        NOT NULL, " +
            "floor_reached  INT             UNSIGNED        NOT NULL, " +
            "tower          VARCHAR(128)    NOT NULL, " +
            "PRIMARY KEY(id));";

    public static final String LOAD_BEST_ATTEMPTS = "SELECT start, duration, floor_reached FROM `envy_battle_tower_players` WHERE uuid = ? AND tower = ? ORDER BY floor_reached DESC LIMIT 1;";

    public static final String LOAD_LAST_ATTEMPTS = "SELECT start, duration, floor_reached FROM `envy_battle_tower_players` WHERE uuid = ? AND tower = ? ORDER BY start ASC LIMIT 1;";

    public static final String ADD_USER_ATTEMPT = "INSERT INTO `envy_battle_tower_players`(uuid, name, towerm start, duration, floor_reached) VALUES (?, ?, ?, ?, ?, ?);";

    public static final String GET_TOP_ATTEMPTS = "SELECT uuid, name, start, duration, floor_reached FROM `envy_battle_tower_players` ORDER BY floor_reached DESC LIMIT 10;";

    public static final String UPDATE_USERNAME = "UPDATE `envy_battle_tower_players` SET `name` = ? WHERE `uuid` = ?;";

    @Override
    public CompletableFuture<Void> save(BattleTowerAttribute attribute) {
        return EnvyBattleTower.getDatabase()
                .update(UPDATE_USERNAME)
                .data(
                        SqlType.text(attribute.username),
                        SqlType.text(attribute.getUniqueId().toString())
                ).executeAsync().thenApply(resultSet -> null);
    }

    @Override
    public void load(BattleTowerAttribute attribute) {
        for (var tower : EnvyBattleTower.getConfig().getBattleTowers()) {
            EnvyBattleTower.getDatabase().query(LOAD_BEST_ATTEMPTS)
                    .data(
                            SqlType.text(attribute.getUniqueId().toString()),
                            SqlType.text(tower.id())
                    )
                    .converter(resultSet -> {
                        attribute.bestAttemptByTower.put(tower.id().toLowerCase(Locale.ROOT), new AttemptDetails(
                                tower,
                                resultSet.getLong("start"),
                                resultSet.getLong("duration"),
                                resultSet.getInt("floor_reached")
                        ));
                        return null;
                    }).executeAsync();

            EnvyBattleTower.getDatabase().query(LOAD_LAST_ATTEMPTS)
                    .data(
                            SqlType.text(attribute.getUniqueId().toString()),
                            SqlType.text(tower.id())
                    )
                    .converter(resultSet -> {
                        attribute.lastAttemptByTower.put(tower.id().toLowerCase(Locale.ROOT), new AttemptDetails(
                                tower,
                                resultSet.getLong("start"),
                                resultSet.getLong("duration"),
                                resultSet.getInt("floor_reached")
                        ));
                        return null;
                    }).executeAsync();
        }
    }

    @Override
    public CompletableFuture<Void> delete(BattleTowerAttribute battleTowerAttribute) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteAll() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void initialize() {
        EnvyBattleTower.getDatabase().update(CREATE_TABLE).executeAsync();
    }

    @Override
    public void addAttempt(ForgeEnvyPlayer player, AttemptDetails entry) {
        EnvyBattleTower.getDatabase()
                .update(ADD_USER_ATTEMPT)
                .data(
                        SqlType.text(player.getUniqueId().toString()),
                        SqlType.text(player.getName()),
                        SqlType.text(entry.getTower().id()),
                        SqlType.bigInt(entry.getAttemptStart()),
                        SqlType.bigInt(entry.getAttemptDuration()),
                        SqlType.integer(entry.getFloorReached())
                )
                .executeAsync();
    }

    @Override
    public Leaderboard<BattleTowerEntry> getLeaderboard(String tower) {
        return Leaderboard.builder(BattleTowerEntry.class)
                .database(EnvyBattleTower.getDatabase())
                .cacheDuration(TimeUnit.MINUTES.toMillis(5))
                .formatter(BattleTowerEntry::fromQuery)
                .order(Order.DESCENDING)
                .table("envy_battle_tower_players")
                .column("floor_reached")
                .extraClauses("tower = '" + tower + "'")
                .pageSize(10)
                .build();
    }
}

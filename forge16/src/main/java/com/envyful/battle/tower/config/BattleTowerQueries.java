package com.envyful.battle.tower.config;

public class BattleTowerQueries {

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `envy_battle_tower_players`(" +
            "id             INT         UNSIGNED        NOT NULL    AUTO_INCREMENT, " +
            "uuid           VARCHAR(64) NOT NULL, " +
            "name           VARCHAR(16) NOT NULL, "  +
            "start          BIGINT      UNSIGNED        NOT NULL, " +
            "duration       BIGINT      UNSIGNED        NOT NULL, " +
            "floor_reached  INT         UNSIGNED        NOT NULL, " +
            "PRIMARY KEY(id));";

    public static final String LOAD_USER_ATTEMPTS = "SELECT start, duration, floor_reached FROM `envy_battle_tower_players` WHERE uuid = ? LIMIT 30;";

    public static final String ADD_USER_ATTEMPT = "INSERT INTO `envy_battle_tower_players`(uuid, name, start, duration, floor_reached) VALUES (?, ?, ?, ?);";

    public static final String GET_TOP_ATTEMPTS = "SELECT uuid, name, start, duration, floor_reached FROM `envy_battle_tower_players` ORDER BY floor_reached DESC LIMIT 10;";

}

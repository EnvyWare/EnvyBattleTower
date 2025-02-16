package com.envyful.battle.tower.api;

import com.envyful.api.text.Placeholder;
import com.envyful.battle.tower.EnvyBattleTower;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BattleTowerEntry {

    private final UUID uuid;
    private final String name;
    private final long start;
    private final long time;
    private final int floors;

    public BattleTowerEntry(UUID uuid, String name, long start, long time, int floors) {
        this.uuid = uuid;
        this.name = name;
        this.start = start;
        this.time = time;
        this.floors = floors;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public long getStart() {
        return this.start;
    }

    public long getTime() {
        return this.time;
    }

    public int getFloors() {
        return this.floors;
    }

    public Placeholder getPlaceholder(int pos) {
        return Placeholder.simple(s -> s.replace("%leaderboard_" + (pos + 1) + "_player%", this.getName())
                .replace("%leaderboard_" + (pos + 1) + "_floor%", this.getFloors() + "")
                .replace("%leaderboard_" + (pos + 1) + "_time%", EnvyBattleTower.getLocale().getTimeFormat().format(this.getTime()))
                .replace("%leaderboard_" + (pos + 1) + "_date%", EnvyBattleTower.getLocale().getDateFormat().format(this.getStart())));
    }

    public static BattleTowerEntry fromQuery(ResultSet resultSet) throws SQLException {
        return new BattleTowerEntry(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getString("name"),
                resultSet.getLong("start"),
                resultSet.getLong("duration"),
                resultSet.getInt("floor_reached")
        );
    }
}

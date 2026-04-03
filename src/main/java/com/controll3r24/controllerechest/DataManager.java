package com.controll3r24.controllerechest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class DataManager {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Connection connection;
    private final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public DataManager() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:" + ControllerEchest.getInstance().getDataFolder().getAbsolutePath() + "/data;MODE=MySQL;DB_CLOSE_DELAY=-1";
            connection = DriverManager.getConnection(url);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS player_chests (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "chest_index INTEGER NOT NULL, " +
                        "chest_size INTEGER NOT NULL, " +
                        "items BLOB, " +
                        "PRIMARY KEY (uuid, chest_index))");
            }
        } catch (Exception e) {
            ControllerEchest.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }

    private byte[] serializeItems(ItemStack[] items) {
        if (items == null) return new byte[0];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeObject(items);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private ItemStack[] deserializeItems(byte[] data) {
        if (data == null || data.length == 0) return new ItemStack[0];
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            return (ItemStack[]) bois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }

    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (cache.containsKey(uuid)) return cache.get(uuid);
            PlayerData data = new PlayerData();
            String sql = "SELECT chest_index, chest_size, items FROM player_chests WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int index = rs.getInt("chest_index");
                    int size = rs.getInt("chest_size");
                    byte[] itemsBlob = rs.getBytes("items");
                    ItemStack[] items = deserializeItems(itemsBlob);
                    PlayerData.EnderChestData chest = new PlayerData.EnderChestData(size);
                    if (items.length > 0) chest.setItems(items);
                    data.setChest(index, chest);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            cache.put(uuid, data);
            return data;
        }, executor);
    }

    public void savePlayerData(UUID uuid, PlayerData data) {
        CompletableFuture.runAsync(() -> {
            try {
                String deleteSql = "DELETE FROM player_chests WHERE uuid = ?";
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, uuid.toString());
                    deleteStmt.executeUpdate();
                }
                String insertSql = "INSERT INTO player_chests (uuid, chest_index, chest_size, items) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    for (int i = 0; i < 54; i++) {
                        PlayerData.EnderChestData chest = data.getChest(i);
                        if (chest == null) continue;
                        insertStmt.setString(1, uuid.toString());
                        insertStmt.setInt(2, i);
                        insertStmt.setInt(3, chest.getSize());
                        insertStmt.setBytes(4, serializeItems(chest.getItems()));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            savePlayerData(uuid, cache.get(uuid));
        }
    }

    public PlayerData getCachedData(UUID uuid) { return cache.get(uuid); }

    public boolean isStaff(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.hasPermission("controlenderchest.staff");
    }
}

package com.itemsplugin.managers;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.PlayerStorage;
import java.util.*;

public class StorageManager {
    
    private final ItemsPlugin plugin;
    private Map<String, PlayerStorage> playerStorages;
    
    public StorageManager(ItemsPlugin plugin) {
        this.plugin = plugin;
        this.playerStorages = new HashMap<>();
    }
    
    public PlayerStorage getPlayerStorage(String playerId) {
        return playerStorages.computeIfAbsent(playerId, k -> new PlayerStorage(playerId));
    }
    
    public void removePlayerStorage(String playerId) {
        playerStorages.remove(playerId);
    }
    
    public boolean hasPlayerStorage(String playerId) {
        return playerStorages.containsKey(playerId);
    }
    
    public Map<String, PlayerStorage> getAllStorages() {
        return new HashMap<>(playerStorages);
    }
    
    public void saveAllPlayers() {
        for (PlayerStorage storage : playerStorages.values()) {
            savePlayer(storage.getPlayerId());
        }
    }
    
    public void savePlayer(String playerId) {
        PlayerStorage storage = playerStorages.get(playerId);
        if (storage != null) {
            // Сохранение в YAML файл
        }
    }
    
    public void loadPlayer(String playerId) {
        // Загрузка из YAML файла
    }
}
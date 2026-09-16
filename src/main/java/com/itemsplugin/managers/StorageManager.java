package com.itemsplugin.managers;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.PlayerStorage;
import com.itemsplugin.models.Tag;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Лениво загружает и сохраняет бесконечные персональные хранилища. */
public class StorageManager {

    private final ItemsPlugin plugin;
    private final Map<String, PlayerStorage> playerStorages = new LinkedHashMap<>();
    private final Map<String, Boolean> loadedPlayers = new LinkedHashMap<>();
    private final File storageFolder;

    public StorageManager(ItemsPlugin plugin) {
        this.plugin = plugin;
        this.storageFolder = new File(plugin.getDataFolder(), "storage");
    }

    public PlayerStorage getPlayerStorage(String playerId) {
        String normalized = playerId.toLowerCase();
        if (!loadedPlayers.containsKey(normalized)) {
            PlayerStorage storage = new PlayerStorage(normalized);
            loadPlayerInto(storage);
            playerStorages.put(normalized, storage);
            loadedPlayers.put(normalized, true);
        }
        return playerStorages.get(normalized);
    }

    public PlayerStorage getPlayerStorage(UUID playerId) {
        return getPlayerStorage(playerId.toString());
    }

    public void removePlayerStorage(String playerId) {
        if (playerId != null) {
            playerStorages.remove(playerId.toLowerCase());
            loadedPlayers.remove(playerId.toLowerCase());
        }
    }

    public boolean hasPlayerStorage(String playerId) {
        return playerId != null && loadedPlayers.containsKey(playerId.toLowerCase());
    }

    public Map<String, PlayerStorage> getAllStorages() {
        return new LinkedHashMap<>(playerStorages);
    }

    public void saveAllPlayers() {
        for (PlayerStorage storage : playerStorages.values()) {
            savePlayer(storage.getPlayerId());
        }
    }

    public void savePlayer(String playerId) {
        if (playerId == null || !playerStorages.containsKey(playerId.toLowerCase())) {
            return;
        }
        if (!storageFolder.exists() && !storageFolder.mkdirs()) {
            plugin.getLogger().warning("Не удалось создать папку storage.");
            return;
        }

        PlayerStorage storage = playerStorages.get(playerId.toLowerCase());
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (CustomItem item : storage.getItems()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", item.getId());
            data.put("display-name", item.getDisplayName());
            data.put("material", item.getMaterial().name());
            data.put("max-durability", item.getMaxDurability());
            data.put("current-durability", item.getCurrentDurability());
            data.put("description", item.getDescription());
            List<String> tagIds = new ArrayList<>();
            for (Tag tag : item.getTags()) {
                tagIds.add(tag.getId());
            }
            data.put("tags", tagIds);
            serializedItems.add(data);
        }
        config.set("items", serializedItems);

        try {
            config.save(fileFor(playerId));
        } catch (IOException exception) {
            plugin.getLogger().warning("Не удалось сохранить хранилище " + playerId + ": "
                    + exception.getMessage());
        }
    }

    /** Совместимость со старым API: фактическая загрузка выполняется лениво. */
    public void loadPlayer(String playerId) {
        getPlayerStorage(playerId);
    }

    private void loadPlayerInto(PlayerStorage storage) {
        File file = fileFor(storage.getPlayerId());
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> raw : config.getMapList("items")) {
            String id = stringValue(raw.get("id"), "stored_item");
            Material material = Material.matchMaterial(stringValue(raw.get("material"), "STONE"));
            if (material == null || !material.isItem()) {
                material = Material.STONE;
            }
            CustomItem item = new CustomItem(id,
                    stringValue(raw.get("display-name"), id), material);
            item.setDescription(stringValue(raw.get("description"), ""));
            item.setMaxDurability(integerValue(raw.get("max-durability"), 100));
            item.setCurrentDurability(integerValue(raw.get("current-durability"), item.getMaxDurability()));
            Object rawTags = raw.get("tags");
            if (rawTags instanceof List<?>) {
                for (Object rawTagId : (List<?>) rawTags) {
                    Tag tag = plugin.getItemManager().getTag(String.valueOf(rawTagId));
                    if (tag != null) {
                        item.addTag(tag);
                    }
                }
            }
            storage.addItem(item);
        }
    }

    private File fileFor(String playerId) {
        // UUID содержит только безопасные для имени файла символы.
        return new File(storageFolder, playerId.toLowerCase() + ".yml");
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private int integerValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}

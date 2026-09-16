package com.itemsplugin;

import com.itemsplugin.commands.ChestCommand;
import com.itemsplugin.commands.ItemsCommand;
import com.itemsplugin.guis.ItemsMenuGUI;
import com.itemsplugin.listeners.InventoryListener;
import com.itemsplugin.managers.ItemManager;
import com.itemsplugin.managers.StorageManager;
import com.itemsplugin.models.ItemDraft;
import com.itemsplugin.models.TagDraft;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public final class ItemsPlugin extends JavaPlugin {

    public enum InputType {
        ITEM_ID,
        ITEM_NAME,
        ITEM_MAX_DURABILITY,
        ITEM_CURRENT_DURABILITY,
        ITEM_DESCRIPTION,
        TAG_ID,
        TAG_NAME,
        TAG_COLOR
    }

    private static ItemsPlugin instance;
    private ItemManager itemManager;
    private StorageManager storageManager;
    private final Map<UUID, ItemDraft> itemDrafts = new HashMap<>();
    private final Map<UUID, TagDraft> tagDrafts = new HashMap<>();
    private final Map<UUID, InputType> inputSessions = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Не удалось создать папку данных плагина.");
        }

        itemManager = new ItemManager(this);
        storageManager = new StorageManager(this);

        if (getCommand("items") != null) {
            getCommand("items").setExecutor(new ItemsCommand(this));
        }
        if (getCommand("chest") != null) {
            getCommand("chest").setExecutor(new ChestCommand(this));
        }
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getLogger().info("ItemsPlugin включен. Paper 1.18.2");
    }

    @Override
    public void onDisable() {
        if (itemManager != null) {
            itemManager.saveData();
        }
        if (storageManager != null) {
            storageManager.saveAllPlayers();
        }
        getLogger().info("ItemsPlugin выключен.");
    }

    public static ItemsPlugin getInstance() {
        return instance;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public ItemDraft getItemDraft(Player player) {
        return itemDrafts.get(player.getUniqueId());
    }

    public void setItemDraft(Player player, ItemDraft draft) {
        if (draft == null) {
            itemDrafts.remove(player.getUniqueId());
        } else {
            itemDrafts.put(player.getUniqueId(), draft);
        }
    }

    public TagDraft getTagDraft(Player player) {
        return tagDrafts.get(player.getUniqueId());
    }

    public void setTagDraft(Player player, TagDraft draft) {
        if (draft == null) {
            tagDrafts.remove(player.getUniqueId());
        } else {
            tagDrafts.put(player.getUniqueId(), draft);
        }
    }

    public void startInput(Player player, InputType type) {
        inputSessions.put(player.getUniqueId(), type);
    }

    public InputType getInput(Player player) {
        return inputSessions.get(player.getUniqueId());
    }

    public void clearInput(Player player) {
        inputSessions.remove(player.getUniqueId());
    }

    public void clearPlayerSession(Player player) {
        UUID uuid = player.getUniqueId();
        itemDrafts.remove(uuid);
        tagDrafts.remove(uuid);
        inputSessions.remove(uuid);
    }

    public void openMainMenu(Player player) {
        new ItemsMenuGUI(this, player).openMainMenu();
    }
}

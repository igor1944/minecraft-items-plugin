package com.itemsplugin;

import org.bukkit.plugin.java.JavaPlugin;
import com.itemsplugin.managers.ItemManager;
import com.itemsplugin.managers.StorageManager;
import com.itemsplugin.commands.ItemsCommand;
import com.itemsplugin.commands.ChestCommand;
import com.itemsplugin.listeners.InventoryListener;

public class ItemsPlugin extends JavaPlugin {
    
    private static ItemsPlugin instance;
    private ItemManager itemManager;
    private StorageManager storageManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Создание папок
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Инициализация менеджеров
        this.itemManager = new ItemManager(this);
        this.storageManager = new StorageManager(this);
        
        // Регистрация команд
        getCommand("items").setExecutor(new ItemsCommand(this));
        getCommand("chest").setExecutor(new ChestCommand(this));
        
        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        getLogger().info("✓ ItemsPlugin включен!");
    }
    
    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.saveAllPlayers();
        }
        getLogger().info("✓ ItemsPlugin отключен!");
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
}
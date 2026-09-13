package com.itemsplugin.listeners;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.guis.ChestGUI;
import com.itemsplugin.guis.ItemsMenuGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
    
    private final ItemsPlugin plugin;
    
    public InventoryListener(ItemsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getItemMeta() == null) {
            return;
        }
        
        String displayName = clickedItem.getItemMeta().getDisplayName();
        
        if (title.contains("Меню предметов")) {
            event.setCancelled(true);
            
            switch (event.getSlot()) {
                case 10:
                    ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
                    gui.openMyItems();
                    break;
                case 12:
                    ItemsMenuGUI createGui = new ItemsMenuGUI(plugin, player);
                    createGui.openCreateItem();
                    break;
                case 14:
                    ItemsMenuGUI tagsGui = new ItemsMenuGUI(plugin, player);
                    tagsGui.openTagsMenu();
                    break;
                case 16:
                    ChestGUI chestGui = new ChestGUI(plugin, player);
                    chestGui.openChest();
                    break;
                case 48:
                    sendHelp(player);
                    player.closeInventory();
                    break;
                case 49:
                    player.closeInventory();
                    break;
            }
        }
        
        if (title.contains("Персональное хранилище")) {
            if (event.getSlot() >= 45 && event.getSlot() <= 49) {
                event.setCancelled(true);
                
                switch (event.getSlot()) {
                    case 49:
                        player.closeInventory();
                        break;
                }
            }
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6════════════════════════════════════════");
        player.sendMessage("§6ItemsPlugin - Справка");
        player.sendMessage("§6════════════════════════════════════════");
        player.sendMessage("§e/items §7- Главное меню");
        player.sendMessage("§e/chest §7- Открыть персональное хранилище");
        player.sendMessage("");
        player.sendMessage("§7Функции:");
        player.sendMessage("  §a✓ §7Создание и редактирование кастомных предметов");
        player.sendMessage("  §a✓ §7Система тегов с разными цветами");
        player.sendMessage("  §a✓ §7Управление прочностью предметов");
        player.sendMessage("  §a✓ §7Персональное бесконечное хранилище (54 слота)");
        player.sendMessage("§6════════════════════════════════════════");
    }
}
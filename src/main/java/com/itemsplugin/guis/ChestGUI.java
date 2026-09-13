package com.itemsplugin.guis;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.PlayerStorage;
import com.itemsplugin.models.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ChestGUI {
    
    private final ItemsPlugin plugin;
    private final Player player;
    private static final int INVENTORY_SIZE = 54;
    
    public ChestGUI(ItemsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    public void openChest() {
        PlayerStorage storage = plugin.getStorageManager().getPlayerStorage(player.getUniqueId().toString());
        
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "§6Персональное хранилище");
        
        List<CustomItem> items = storage.getItems();
        
        int slot = 0;
        for (CustomItem item : items) {
            if (slot >= INVENTORY_SIZE - 9) break;
            ItemStack stack = createItemDisplay(item);
            inv.setItem(slot, stack);
            slot++;
        }
        
        ItemStack infoItem = createButton(Material.PAPER, "§eИнформация", 
            "§7Свободно: §a" + storage.getAvailableSpace() + "§7/§c" + storage.getItemCount() + "§7/54");
        inv.setItem(45, infoItem);
        
        ItemStack addItem = createButton(Material.LIME_STAINED_GLASS_PANE, "§aДобавить предмет", "");
        inv.setItem(46, addItem);
        
        ItemStack removeItem = createButton(Material.RED_STAINED_GLASS_PANE, "§cУдалить предмет", "");
        inv.setItem(47, removeItem);
        
        ItemStack sort = createButton(Material.HOPPER, "§bСортировать", "");
        inv.setItem(48, sort);
        
        ItemStack close = createButton(Material.BARRIER, "§cВыход", "");
        inv.setItem(49, close);
        
        player.openInventory(inv);
    }
    
    private ItemStack createButton(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            if (!description.isEmpty()) {
                lore.add(description);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createItemDisplay(CustomItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + item.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7ID: §a" + item.getId());
            
            if (!item.getDescription().isEmpty()) {
                lore.add("§8" + item.getDescription());
            }
            
            lore.add("§7Прочность: §c" + item.getCurrentDurability() + "§7/§c" + item.getMaxDurability());
            lore.add("§7(" + item.getDurabilityPercentage() + "%)");
            
            if (item.isBroken()) {
                lore.add("§4⚠ СЛОМАН");
            }
            
            if (!item.getTags().isEmpty()) {
                lore.add("§7Теги:");
                for (Tag tag : item.getTags()) {
                    lore.add("  §8• §d" + tag.getName());
                }
            }
            
            lore.add("");
            lore.add("§7ПКМ: редактировать | ЛКМ: удалить");
            
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
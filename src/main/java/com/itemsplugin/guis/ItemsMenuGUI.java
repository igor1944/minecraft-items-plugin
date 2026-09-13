package com.itemsplugin.guis;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemsMenuGUI {
    
    private final ItemsPlugin plugin;
    private final Player player;
    private static final int INVENTORY_SIZE = 54;
    
    public ItemsMenuGUI(ItemsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    public void openMenu() {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "§6Меню предметов");
        
        ItemStack myItems = createButton(Material.CHEST, "§aМои предметы", "Посмотреть все ваши предметы");
        inv.setItem(10, myItems);
        
        ItemStack createItem = createButton(Material.CRAFTING_TABLE, "§bСоздать предмет", "Создать новый кастомный предмет");
        inv.setItem(12, createItem);
        
        ItemStack tagsButton = createButton(Material.NAME_TAG, "§dУправление тегами", "Создать/изменить теги");
        inv.setItem(14, tagsButton);
        
        ItemStack storageButton = createButton(Material.SHULKER_BOX, "§eПерсональное хранилище", "Открыть бесконечный сундук");
        inv.setItem(16, storageButton);
        
        ItemStack helpButton = createButton(Material.BOOK, "§6Справка", "Узнайте о плагине");
        inv.setItem(48, helpButton);
        
        ItemStack closeButton = createButton(Material.BARRIER, "§cВыход", "Закрыть меню");
        inv.setItem(49, closeButton);
        
        player.openInventory(inv);
    }
    
    public void openMyItems() {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "§6Мои предметы");
        
        List<CustomItem> items = new ArrayList<>(plugin.getItemManager().getAllItems().values());
        
        int slot = 0;
        for (CustomItem item : items) {
            if (slot >= INVENTORY_SIZE - 1) break;
            ItemStack stack = createItemDisplay(item);
            inv.setItem(slot, stack);
            slot++;
        }
        
        ItemStack backButton = createButton(Material.ARROW, "§6Назад", "");
        inv.setItem(49, backButton);
        
        player.openInventory(inv);
    }
    
    public void openCreateItem() {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "§6Создание предмета");
        
        ItemStack materialSelect = createButton(Material.STONE, "§bВыбрать материал", "Нажмите для выбора");
        inv.setItem(10, materialSelect);
        
        ItemStack nameInput = createButton(Material.NAME_TAG, "§aУстановить имя", "Введите имя предмета");
        inv.setItem(12, nameInput);
        
        ItemStack durabilitySet = createButton(Material.IRON_INGOT, "§cУстановить прочность", "Максимальная прочность");
        inv.setItem(14, durabilitySet);
        
        ItemStack tagsSelect = createButton(Material.NAME_TAG, "§dДобавить теги", "Добавьте теги предмету");
        inv.setItem(16, tagsSelect);
        
        ItemStack save = createButton(Material.EMERALD_BLOCK, "§aСохранить", "Сохранить предмет");
        inv.setItem(48, save);
        
        ItemStack cancel = createButton(Material.BARRIER, "§cОтмена", "");
        inv.setItem(49, cancel);
        
        player.openInventory(inv);
    }
    
    public void openTagsMenu() {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "§6Управление тегами");
        
        List<Tag> tags = new ArrayList<>(plugin.getItemManager().getAllTags().values());
        
        int slot = 0;
        for (Tag tag : tags) {
            if (slot >= INVENTORY_SIZE - 1) break;
            ItemStack stack = createTagDisplay(tag);
            inv.setItem(slot, stack);
            slot++;
        }
        
        ItemStack createTag = createButton(Material.LIME_DYE, "§aСоздать новый тег", "");
        inv.setItem(48, createTag);
        
        ItemStack backButton = createButton(Material.ARROW, "§6Назад", "");
        inv.setItem(49, backButton);
        
        player.openInventory(inv);
    }
    
    private ItemStack createButton(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            if (!description.isEmpty()) {
                lore.add("§7" + description);
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
            lore.add("§7ID: " + item.getId());
            lore.add("§8" + item.getDescription());
            lore.add("§7Прочность: §c" + item.getCurrentDurability() + "§7/§c" + item.getMaxDurability());
            lore.add("§7(" + item.getDurabilityPercentage() + "%)");
            
            if (!item.getTags().isEmpty()) {
                lore.add("§7Теги:");
                for (Tag tag : item.getTags()) {
                    lore.add("  §8• §l" + tag.getName());
                }
            }
            
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }
    
    private ItemStack createTagDisplay(Tag tag) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§d" + tag.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7ID: " + tag.getId());
            lore.add("§7Цвет: " + tag.getColorHex());
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
package com.itemsplugin.managers;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.Tag;
import org.bukkit.Color;
import java.util.*;

public class ItemManager {
    
    private final ItemsPlugin plugin;
    private Map<String, CustomItem> items;
    private Map<String, Tag> tags;
    
    public ItemManager(ItemsPlugin plugin) {
        this.plugin = plugin;
        this.items = new HashMap<>();
        this.tags = new HashMap<>();
        loadData();
    }
    
    public CustomItem createItem(String id, String displayName, org.bukkit.Material material) {
        CustomItem item = new CustomItem(id, displayName, material);
        items.put(id, item);
        return item;
    }
    
    public CustomItem getItem(String id) {
        return items.get(id);
    }
    
    public void deleteItem(String id) {
        items.remove(id);
    }
    
    public Map<String, CustomItem> getAllItems() {
        return new HashMap<>(items);
    }
    
    public void updateItem(String id, CustomItem item) {
        items.put(id, item);
    }
    
    public Tag createTag(String id, String name, Color color) {
        Tag tag = new Tag(id, name, color);
        tags.put(id, tag);
        return tag;
    }
    
    public Tag getTag(String id) {
        return tags.get(id);
    }
    
    public void deleteTag(String id) {
        tags.remove(id);
        for (CustomItem item : items.values()) {
            item.removeTagById(id);
        }
    }
    
    public Map<String, Tag> getAllTags() {
        return new HashMap<>(tags);
    }
    
    public void updateTag(String id, Tag tag) {
        tags.put(id, tag);
    }
    
    private void loadData() {
        // Загрузка из файлов (реализация в следующих версиях)
    }
    
    public void saveData() {
        // Сохранение в файлы
    }
}
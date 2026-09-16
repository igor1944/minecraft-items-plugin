package com.itemsplugin.managers;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.Tag;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Управляет шаблонами предметов и тегами и сохраняет их в items.yml. */
public class ItemManager {

    private final ItemsPlugin plugin;
    private final Map<String, CustomItem> items = new LinkedHashMap<>();
    private final Map<String, Tag> tags = new LinkedHashMap<>();
    private final File dataFile;

    public ItemManager(ItemsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "items.yml");
        loadData();
    }

    public CustomItem createItem(String id, String displayName, Material material) {
        String normalized = id.toLowerCase(Locale.ROOT);
        CustomItem item = new CustomItem(normalized, displayName, material);
        items.put(normalized, item);
        return item;
    }

    public CustomItem getItem(String id) {
        return id == null ? null : items.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean hasItem(String id) {
        return getItem(id) != null;
    }

    public void deleteItem(String id) {
        if (id != null) {
            items.remove(id.toLowerCase(Locale.ROOT));
        }
    }

    public Map<String, CustomItem> getAllItems() {
        return new LinkedHashMap<>(items);
    }

    /** Заменяет предмет, корректно обрабатывая изменение его ID. */
    public void replaceItem(String oldId, CustomItem item) {
        if (oldId != null) {
            items.remove(oldId.toLowerCase(Locale.ROOT));
        }
        items.put(item.getId().toLowerCase(Locale.ROOT), item);
    }

    public void updateItem(String id, CustomItem item) {
        replaceItem(id, item);
    }

    public Tag createTag(String id, String name, Color color) {
        String normalized = id.toLowerCase(Locale.ROOT);
        Tag tag = new Tag(normalized, name, color);
        tags.put(normalized, tag);
        return tag;
    }

    public Tag getTag(String id) {
        return id == null ? null : tags.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean hasTag(String id) {
        return getTag(id) != null;
    }

    public void deleteTag(String id) {
        if (id == null) {
            return;
        }
        String normalizedId = id.toLowerCase(Locale.ROOT);
        tags.remove(normalizedId);
        for (CustomItem item : items.values()) {
            item.removeTagById(normalizedId);
        }
    }

    public Map<String, Tag> getAllTags() {
        return new LinkedHashMap<>(tags);
    }

    /** Заменяет тег, сохраняя ссылки в уже созданных предметах. */
    public void replaceTag(String oldId, Tag replacement) {
        String newId = replacement.getId().toLowerCase(Locale.ROOT);
        String oldNormalized = oldId == null ? null : oldId.toLowerCase(Locale.ROOT);
        Tag existing = oldNormalized == null ? null : tags.remove(oldNormalized);
        if (existing != null) {
            existing.setId(newId);
            existing.setName(replacement.getName());
            existing.setColor(replacement.getColor());
            tags.put(newId, existing);
            for (CustomItem item : items.values()) {
                for (Tag tag : item.getTags()) {
                    if (tag == existing || tag.getId().equals(oldNormalized)) {
                        tag.setId(newId);
                        tag.setName(replacement.getName());
                        tag.setColor(replacement.getColor());
                    }
                }
            }
        } else {
            tags.put(newId, replacement);
        }
    }

    public void updateTag(String id, Tag tag) {
        replaceTag(id, tag);
    }

    public void saveData() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Не удалось создать папку данных плагина.");
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        for (Tag tag : tags.values()) {
            String path = "tags." + tag.getId();
            config.set(path + ".name", tag.getName());
            config.set(path + ".red", tag.getColor().getRed());
            config.set(path + ".green", tag.getColor().getGreen());
            config.set(path + ".blue", tag.getColor().getBlue());
        }

        for (CustomItem item : items.values()) {
            String path = "items." + item.getId();
            config.set(path + ".display-name", item.getDisplayName());
            config.set(path + ".material", item.getMaterial().name());
            config.set(path + ".max-durability", item.getMaxDurability());
            config.set(path + ".current-durability", item.getCurrentDurability());
            config.set(path + ".description", item.getDescription());
            List<String> itemTags = new ArrayList<>();
            for (Tag tag : item.getTags()) {
                itemTags.add(tag.getId());
            }
            config.set(path + ".tags", itemTags);
        }

        try {
            config.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Не удалось сохранить items.yml: " + exception.getMessage());
        }
    }

    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection tagSection = config.getConfigurationSection("tags");
        if (tagSection != null) {
            for (String id : tagSection.getKeys(false)) {
                String path = "tags." + id;
                String name = config.getString(path + ".name", id);
                int red = clampColor(config.getInt(path + ".red", 255));
                int green = clampColor(config.getInt(path + ".green", 255));
                int blue = clampColor(config.getInt(path + ".blue", 255));
                tags.put(id.toLowerCase(Locale.ROOT), new Tag(id.toLowerCase(Locale.ROOT), name,
                        Color.fromRGB(red, green, blue)));
            }
        }

        ConfigurationSection itemSection = config.getConfigurationSection("items");
        if (itemSection == null) {
            return;
        }
        for (String id : itemSection.getKeys(false)) {
            String path = "items." + id;
            Material material = Material.matchMaterial(config.getString(path + ".material", "STONE"));
            if (material == null || !material.isItem()) {
                material = Material.STONE;
            }
            CustomItem item = new CustomItem(id.toLowerCase(Locale.ROOT),
                    config.getString(path + ".display-name", id), material);
            item.setDescription(config.getString(path + ".description", ""));
            item.setMaxDurability(Math.max(0, config.getInt(path + ".max-durability", 100)));
            item.setCurrentDurability(Math.max(0, config.getInt(path + ".current-durability",
                    item.getMaxDurability())));
            for (String tagId : config.getStringList(path + ".tags")) {
                Tag tag = getTag(tagId);
                if (tag != null) {
                    item.addTag(tag);
                }
            }
            items.put(item.getId(), item);
        }
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }
}

package com.itemsplugin.models;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание кастомного предмета. Один объект используется как шаблон, а его
 * копии помещаются в персональные хранилища игроков.
 */
public class CustomItem {

    private String id;
    private String displayName;
    private Material material;
    private int maxDurability;
    private int currentDurability;
    private List<Tag> tags;
    private String description;

    public CustomItem(String id, String displayName, Material material) {
        this.id = id;
        this.displayName = displayName;
        this.material = material == null || !material.isItem() ? Material.STONE : material;
        this.maxDurability = 100;
        this.currentDurability = 100;
        this.tags = new ArrayList<>();
        this.description = "";
    }

    public CustomItem copy() {
        CustomItem copy = new CustomItem(id, displayName, material);
        copy.maxDurability = maxDurability;
        copy.currentDurability = currentDurability;
        copy.description = description;
        copy.tags = new ArrayList<>(tags);
        return copy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        if (material != null && material.isItem()) {
            this.material = material;
        }
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public void setMaxDurability(int maxDurability) {
        this.maxDurability = Math.max(0, maxDurability);
        this.currentDurability = Math.min(this.currentDurability, this.maxDurability);
    }

    public int getCurrentDurability() {
        return currentDurability;
    }

    public void setCurrentDurability(int currentDurability) {
        this.currentDurability = Math.max(0, Math.min(currentDurability, maxDurability));
    }

    public void addDamage(int damage) {
        if (damage > 0) {
            setCurrentDurability(currentDurability - damage);
        }
    }

    public void repair(int amount) {
        if (amount > 0) {
            setCurrentDurability(currentDurability + amount);
        }
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public void addTag(Tag tag) {
        if (tag != null && !tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public void removeTagById(String tagId) {
        tags.removeIf(tag -> tag.getId().equals(tagId));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public int getDurabilityPercentage() {
        if (maxDurability == 0) {
            return 100;
        }
        return (currentDurability * 100) / maxDurability;
    }

    public boolean isBroken() {
        return maxDurability > 0 && currentDurability <= 0;
    }
}

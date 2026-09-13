package com.itemsplugin.models;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

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
        this.material = material;
        this.maxDurability = 100;
        this.currentDurability = 100;
        this.tags = new ArrayList<>();
        this.description = "";
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
        this.displayName = displayName;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public int getMaxDurability() {
        return maxDurability;
    }
    
    public void setMaxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
    }
    
    public int getCurrentDurability() {
        return currentDurability;
    }
    
    public void setCurrentDurability(int currentDurability) {
        this.currentDurability = Math.min(currentDurability, maxDurability);
    }
    
    public void addDamage(int damage) {
        this.currentDurability -= damage;
        if (this.currentDurability < 0) {
            this.currentDurability = 0;
        }
    }
    
    public void repair(int amount) {
        this.currentDurability += amount;
        if (this.currentDurability > maxDurability) {
            this.currentDurability = maxDurability;
        }
    }
    
    public List<Tag> getTags() {
        return tags;
    }
    
    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
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
        this.description = description;
    }
    
    public int getDurabilityPercentage() {
        if (maxDurability == 0) return 100;
        return (currentDurability * 100) / maxDurability;
    }
    
    public boolean isBroken() {
        return currentDurability <= 0;
    }
}
package com.itemsplugin.models;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/** Временные данные, которые игрок редактирует через GUI. */
public class ItemDraft {

    private String originalId;
    private String id;
    private String displayName;
    private Material material;
    private int maxDurability;
    private int currentDurability;
    private String description;
    private final List<String> tagIds = new ArrayList<>();

    public ItemDraft() {
        this.id = "new_item";
        this.displayName = "Новый предмет";
        this.material = Material.STONE;
        this.maxDurability = 100;
        this.currentDurability = 100;
    }

    public static ItemDraft fromItem(CustomItem item) {
        ItemDraft draft = new ItemDraft();
        draft.originalId = item.getId();
        draft.id = item.getId();
        draft.displayName = item.getDisplayName();
        draft.material = item.getMaterial();
        draft.maxDurability = item.getMaxDurability();
        draft.currentDurability = item.getCurrentDurability();
        draft.description = item.getDescription();
        for (Tag tag : item.getTags()) {
            draft.tagIds.add(tag.getId());
        }
        return draft;
    }

    public String getOriginalId() {
        return originalId;
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
        this.maxDurability = Math.max(0, maxDurability);
        this.currentDurability = Math.min(this.currentDurability, this.maxDurability);
    }

    public int getCurrentDurability() {
        return currentDurability;
    }

    public void setCurrentDurability(int currentDurability) {
        this.currentDurability = Math.max(0, Math.min(currentDurability, maxDurability));
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public List<String> getTagIds() {
        return tagIds;
    }

    public void toggleTag(String tagId) {
        if (tagIds.contains(tagId)) {
            tagIds.remove(tagId);
        } else {
            tagIds.add(tagId);
        }
    }

    public boolean hasTag(String tagId) {
        return tagIds.contains(tagId);
    }
}

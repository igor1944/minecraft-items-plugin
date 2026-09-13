package com.itemsplugin.models;

import java.util.*;

public class PlayerStorage {
    
    private String playerId;
    private List<CustomItem> items;
    private static final int MAX_ITEMS = 54;
    
    public PlayerStorage(String playerId) {
        this.playerId = playerId;
        this.items = new ArrayList<>();
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public List<CustomItem> getItems() {
        return items;
    }
    
    public void addItem(CustomItem item) {
        if (items.size() < MAX_ITEMS) {
            items.add(item);
        }
    }
    
    public void removeItem(CustomItem item) {
        items.remove(item);
    }
    
    public void removeItemById(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }
    
    public CustomItem getItemById(String itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }
    
    public List<CustomItem> getItemsByTag(String tagId) {
        List<CustomItem> result = new ArrayList<>();
        for (CustomItem item : items) {
            for (Tag tag : item.getTags()) {
                if (tag.getId().equals(tagId)) {
                    result.add(item);
                    break;
                }
            }
        }
        return result;
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public boolean hasSpace() {
        return items.size() < MAX_ITEMS;
    }
    
    public int getAvailableSpace() {
        return MAX_ITEMS - items.size();
    }
}
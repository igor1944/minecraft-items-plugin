package com.itemsplugin.models;

import java.util.ArrayList;
import java.util.List;

/** Персональное хранилище одного игрока. Количество предметов не ограничено. */
public class PlayerStorage {

    private final String playerId;
    private final List<CustomItem> items;

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
        if (item != null) {
            items.add(item);
        }
    }

    public CustomItem removeItem(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.remove(index);
    }

    public void removeItem(CustomItem item) {
        items.remove(item);
    }

    public void removeItemById(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }

    public void removeTagById(String tagId) {
        for (CustomItem item : items) {
            item.removeTagById(tagId);
        }
    }

    public CustomItem getItemById(String itemId) {
        for (CustomItem item : items) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
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

    /** Оставлено для совместимости с прежним API плагина. */
    public boolean hasSpace() {
        return true;
    }

    /** Хранилище бесконечное, поэтому свободное место не заканчивается. */
    public int getAvailableSpace() {
        return Integer.MAX_VALUE;
    }
}

package com.itemsplugin.guis;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Надёжный идентификатор наших меню, вместо проверки только по заголовку. */
public class MenuHolder implements InventoryHolder {

    public enum Type {
        MAIN,
        ITEMS,
        ITEM_EDITOR,
        MATERIALS,
        TAGS,
        TAG_EDITOR,
        TAG_SELECTOR,
        COLORS,
        STORAGE,
        STORAGE_ADD,
        HELP
    }

    private final Type type;
    private final int page;

    public MenuHolder(Type type) {
        this(type, 0);
    }

    public MenuHolder(Type type, int page) {
        this.type = type;
        this.page = Math.max(0, page);
    }

    public Type getType() {
        return type;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}

package com.itemsplugin.util;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.Tag;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Создаёт отображение предмета и хранит его ID/прочность в PDC. */
public final class ItemStackFactory {

    private ItemStackFactory() {
    }

    public static ItemStack create(ItemsPlugin plugin, CustomItem source) {
        ItemStack stack = new ItemStack(source.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        meta.setDisplayName(source.getDisplayName());
        meta.setLore(lore(source));
        setData(plugin, meta, source.getId(), source.getMaxDurability(), source.getCurrentDurability());
        stack.setItemMeta(meta);
        return stack;
    }

    public static boolean isCustomItem(ItemsPlugin plugin, ItemStack stack) {
        return stack != null && stack.hasItemMeta()
                && stack.getItemMeta().getPersistentDataContainer().has(itemIdKey(plugin), PersistentDataType.STRING);
    }

    public static String getId(ItemsPlugin plugin, ItemStack stack) {
        if (!isCustomItem(plugin, stack)) {
            return null;
        }
        return stack.getItemMeta().getPersistentDataContainer().get(itemIdKey(plugin), PersistentDataType.STRING);
    }

    public static int getCurrentDurability(ItemsPlugin plugin, ItemStack stack, int fallback) {
        if (!isCustomItem(plugin, stack)) {
            return fallback;
        }
        Integer value = stack.getItemMeta().getPersistentDataContainer()
                .get(currentKey(plugin), PersistentDataType.INTEGER);
        return value == null ? fallback : value;
    }

    public static int getMaxDurability(ItemsPlugin plugin, ItemStack stack, int fallback) {
        if (!isCustomItem(plugin, stack)) {
            return fallback;
        }
        Integer value = stack.getItemMeta().getPersistentDataContainer()
                .get(maxKey(plugin), PersistentDataType.INTEGER);
        return value == null ? fallback : value;
    }

    /** Обновляет только отображение и PDC, сохраняя количество предметов в стаке. */
    public static void updateDurability(ItemsPlugin plugin, ItemStack stack, CustomItem source, int current) {
        CustomItem display = source.copy();
        display.setCurrentDurability(current);
        int amount = stack.getAmount();
        ItemStack replacement = create(plugin, display);
        stack.setType(replacement.getType());
        stack.setItemMeta(replacement.getItemMeta());
        stack.setAmount(amount);
    }

    private static void setData(ItemsPlugin plugin, ItemMeta meta, String id, int max, int current) {
        meta.getPersistentDataContainer().set(itemIdKey(plugin), PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(maxKey(plugin), PersistentDataType.INTEGER, max);
        meta.getPersistentDataContainer().set(currentKey(plugin), PersistentDataType.INTEGER, current);
    }

    private static List<String> lore(CustomItem item) {
        List<String> lore = new ArrayList<>();
        lore.add("§8ID: §7" + item.getId());
        if (!item.getDescription().isBlank()) {
            lore.add("§7" + item.getDescription());
        }
        if (item.getMaxDurability() <= 0) {
            lore.add("§7Прочность: §aне ограничена");
        } else {
            lore.add("§7Прочность: §e" + item.getCurrentDurability() + "§7/§e" + item.getMaxDurability()
                    + " §8(" + item.getDurabilityPercentage() + "%)");
            if (item.isBroken()) {
                lore.add("§cПредмет сломан");
            }
        }
        if (!item.getTags().isEmpty()) {
            lore.add("§7Теги:");
            for (Tag tag : item.getTags()) {
                lore.add("  " + ChatColor.of(tag.getColorHex()) + "• " + tag.getName());
            }
        }
        return lore;
    }

    public static NamespacedKey itemIdKey(ItemsPlugin plugin) {
        return new NamespacedKey(plugin, "custom_item_id");
    }

    private static NamespacedKey maxKey(ItemsPlugin plugin) {
        return new NamespacedKey(plugin, "custom_item_max");
    }

    private static NamespacedKey currentKey(ItemsPlugin plugin) {
        return new NamespacedKey(plugin, "custom_item_current");
    }
}

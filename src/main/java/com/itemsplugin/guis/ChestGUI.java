package com.itemsplugin.guis;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.PlayerStorage;
import com.itemsplugin.util.ItemStackFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** GUI личного хранилища. Предметы хранятся на диске и отображаются страницами. */
public class ChestGUI {

    private static final int SIZE = 54;
    private static final int CONTENT_SIZE = 45;
    private final ItemsPlugin plugin;
    private final Player player;

    public ChestGUI(ItemsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void openChest() {
        openChest(0);
    }

    public void openChest(int page) {
        PlayerStorage storage = plugin.getStorageManager().getPlayerStorage(player.getUniqueId());
        page = clampPage(page, storage.getItemCount());
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.STORAGE, page), SIZE,
                "§6Личное хранилище §8| §f" + (page + 1));
        int start = page * CONTENT_SIZE;
        for (int i = 0; i < CONTENT_SIZE && start + i < storage.getItemCount(); i++) {
            inventory.setItem(i, ItemStackFactory.create(plugin, storage.getItems().get(start + i)));
        }

        inventory.setItem(45, button(Material.PAPER, "§eИнформация",
                "§7Предметов: §f" + storage.getItemCount(), "§7Хранилище бесконечное"));
        inventory.setItem(46, button(Material.LIME_STAINED_GLASS_PANE, "§aДобавить",
                "Выбрать шаблон и добавить его сюда"));
        inventory.setItem(47, button(Material.HOPPER, "§bСортировать", "По ID предмета"));
        if (page > 0) {
            inventory.setItem(48, button(Material.ARROW, "§fПредыдущая страница", ""));
        }
        if (page < pageCount(storage.getItemCount()) - 1) {
            inventory.setItem(49, button(Material.ARROW, "§fСледующая страница", ""));
        }
        inventory.setItem(50, button(Material.CHEST, "§6Меню предметов", "Вернуться в главное меню"));
        inventory.setItem(51, button(Material.BARRIER, "§cЗакрыть", ""));
        inventory.setItem(52, button(Material.BOOK, "§7Управление", "ЛКМ по предмету — получить", "ПКМ по предмету — удалить"));
        inventory.setItem(53, button(Material.PAPER, "§fСтраница " + (page + 1) + "/"
                + pageCount(storage.getItemCount()), ""));
        player.openInventory(inventory);
    }

    public void openAddMenu(int page) {
        List<CustomItem> items = new ArrayList<>(plugin.getItemManager().getAllItems().values());
        items.sort(Comparator.comparing(CustomItem::getId));
        page = clampPage(page, items.size());
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuHolder.Type.STORAGE_ADD, page), SIZE,
                "§6Добавить в хранилище §8| §f" + (page + 1));
        int start = page * CONTENT_SIZE;
        for (int i = 0; i < CONTENT_SIZE && start + i < items.size(); i++) {
            inventory.setItem(i, ItemStackFactory.create(plugin, items.get(start + i)));
        }
        inventory.setItem(45, button(Material.ARROW, "§6Назад", "В хранилище"));
        if (page > 0) {
            inventory.setItem(46, button(Material.ARROW, "§fПредыдущая", ""));
        }
        if (page < pageCount(items.size()) - 1) {
            inventory.setItem(47, button(Material.ARROW, "§fСледующая", ""));
        }
        inventory.setItem(49, button(Material.BARRIER, "§cЗакрыть", ""));
        player.openInventory(inventory);
    }

    public static ItemStack button(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lines = new ArrayList<>();
            for (String line : lore) {
                if (line != null && !line.isEmpty()) {
                    lines.add(line);
                }
            }
            meta.setLore(lines);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private int clampPage(int page, int size) {
        return Math.max(0, Math.min(Math.max(0, page), pageCount(size) - 1));
    }

    private int pageCount(int size) {
        return Math.max(1, (size + CONTENT_SIZE - 1) / CONTENT_SIZE);
    }
}

package com.itemsplugin.listeners;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.guis.ChestGUI;
import com.itemsplugin.guis.ItemsMenuGUI;
import com.itemsplugin.guis.MenuHolder;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.ItemDraft;
import com.itemsplugin.models.PlayerStorage;
import com.itemsplugin.models.Tag;
import com.itemsplugin.models.TagDraft;
import com.itemsplugin.util.ItemStackFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.ClickType;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryListener implements Listener {

    private final ItemsPlugin plugin;

    public InventoryListener(ItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        // Ни один предмет нельзя вынести из виртуального меню или положить в него.
        event.setCancelled(true);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        switch (holder.getType()) {
            case MAIN -> handleMain(player, event.getSlot());
            case ITEMS -> handleItems(player, holder.getPage(), event.getSlot(), event.getClick());
            case ITEM_EDITOR -> handleItemEditor(player, event.getSlot());
            case MATERIALS -> handleMaterials(player, holder.getPage(), event.getSlot());
            case TAGS -> handleTags(player, holder.getPage(), event.getSlot(), event.getClick());
            case TAG_EDITOR -> handleTagEditor(player, event.getSlot());
            case TAG_SELECTOR -> handleTagSelector(player, holder.getPage(), event.getSlot());
            case COLORS -> handleColors(player, event.getSlot());
            case STORAGE -> handleStorage(player, holder.getPage(), event.getSlot(), event.getClick());
            case STORAGE_ADD -> handleStorageAdd(player, holder.getPage(), event.getSlot());
            case HELP -> player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Состояние черновика не удаляется: игрок может закрыть меню и продолжить
        // редактирование командами или открыть его снова.
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ItemsPlugin.InputType inputType = plugin.getInput(player);
        if (inputType == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> processInput(player, inputType, message));
    }

    @EventHandler
    public void onCustomItemDamage(PlayerItemDamageEvent event) {
        ItemStack stack = event.getItem();
        String id = ItemStackFactory.getId(plugin, stack);
        if (id == null) {
            return;
        }
        CustomItem template = plugin.getItemManager().getItem(id);
        if (template == null) {
            return;
        }
        int max = ItemStackFactory.getMaxDurability(plugin, stack, template.getMaxDurability());
        if (max <= 0) {
            event.setCancelled(true);
            return;
        }
        int current = ItemStackFactory.getCurrentDurability(plugin, stack, max);
        current = Math.max(0, current - Math.max(0, event.getDamage()));
        event.setCancelled(true);
        ItemStackFactory.updateDurability(plugin, stack, template, current);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.clearPlayerSession(event.getPlayer());
    }

    private void handleMain(Player player, int slot) {
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        switch (slot) {
            case 10 -> gui.openItemList(0);
            case 12 -> {
                if (checkAdmin(player)) gui.beginCreateItem(null);
            }
            case 14 -> gui.openTagList(0);
            case 16 -> new ChestGUI(plugin, player).openChest();
            case 48 -> sendHelp(player);
            case 49 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleItems(Player player, int page, int slot, ClickType click) {
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        List<CustomItem> items = ItemsMenuGUI.sortedItems(plugin);
        if (slot < 45) {
            int index = page * 45 + slot;
            if (index >= 0 && index < items.size() && checkAdmin(player)) {
                CustomItem item = items.get(index);
                if (click.isRightClick()) {
                    plugin.getItemManager().deleteItem(item.getId());
                    plugin.getItemManager().saveData();
                    player.sendMessage("§aШаблон удалён: §f" + item.getId());
                    gui.openItemList(page);
                } else {
                    gui.beginEditItem(item);
                }
            }
            return;
        }
        switch (slot) {
            case 45 -> gui.openMainMenu();
            case 46 -> { if (checkAdmin(player)) gui.beginCreateItem(null); }
            case 47 -> gui.openTagList(0);
            case 48 -> new ChestGUI(plugin, player).openChest();
            case 49 -> player.closeInventory();
            case 50 -> gui.openItemList(page - 1);
            case 51 -> gui.openItemList(page + 1);
            case 52 -> player.sendMessage("§7ЛКМ по предмету — изменить. ПКМ — удалить шаблон.");
            default -> {
            }
        }
    }

    private void handleItemEditor(Player player, int slot) {
        if (!checkAdmin(player)) return;
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        switch (slot) {
            case 10 -> gui.openMaterialSelector(0);
            case 12 -> ask(player, ItemsPlugin.InputType.ITEM_ID, "Введите ID предмета (a-z, 0-9, _ или -). Для отмены напишите отмена.");
            case 14 -> ask(player, ItemsPlugin.InputType.ITEM_NAME, "Введите отображаемое название предмета. Можно использовать цветовые коды &a. Для отмены — отмена.");
            case 16 -> ask(player, ItemsPlugin.InputType.ITEM_MAX_DURABILITY, "Введите максимальную прочность. 0 — убрать прочность.");
            case 28 -> ask(player, ItemsPlugin.InputType.ITEM_CURRENT_DURABILITY, "Введите текущую прочность предмета.");
            case 30 -> ask(player, ItemsPlugin.InputType.ITEM_DESCRIPTION, "Введите описание предмета. Для пустого описания напишите -.");
            case 32 -> gui.openTagSelector(0);
            case 48 -> gui.saveItemDraft();
            case 49 -> {
                plugin.setItemDraft(player, null);
                gui.openItemList(0);
            }
            default -> {
            }
        }
    }

    private void handleMaterials(Player player, int page, int slot) {
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        if (slot < 45) {
            ItemDraft draft = plugin.getItemDraft(player);
            org.bukkit.Material material = ItemsMenuGUI.materialAt(page, slot);
            if (draft != null && material != null) {
                draft.setMaterial(material);
                gui.openItemEditor();
            }
        } else if (slot == 45) {
            gui.openItemEditor();
        } else if (slot == 48) {
            gui.openMaterialSelector(page - 1);
        } else if (slot == 49) {
            gui.openMaterialSelector(page + 1);
        }
    }

    private void handleTags(Player player, int page, int slot, ClickType click) {
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        List<Tag> tags = ItemsMenuGUI.sortedTags(plugin);
        if (slot < 45) {
            int index = page * 45 + slot;
            if (index >= 0 && index < tags.size() && checkAdmin(player)) {
                Tag tag = tags.get(index);
                if (click.isRightClick()) {
                    plugin.getItemManager().deleteTag(tag.getId());
                    for (PlayerStorage loadedStorage : plugin.getStorageManager().getAllStorages().values()) {
                        loadedStorage.removeTagById(tag.getId());
                        plugin.getStorageManager().savePlayer(loadedStorage.getPlayerId());
                    }
                    plugin.getItemManager().saveData();
                    player.sendMessage("§aТег удалён: §f" + tag.getId());
                    gui.openTagList(page);
                } else {
                    gui.beginEditTag(tag);
                }
            }
            return;
        }
        switch (slot) {
            case 45 -> gui.openMainMenu();
            case 46 -> { if (checkAdmin(player)) gui.beginCreateTag(); }
            case 47 -> gui.openItemList(0);
            case 49 -> player.closeInventory();
            case 50 -> gui.openTagList(page - 1);
            case 51 -> gui.openTagList(page + 1);
            case 52 -> player.sendMessage("§7ЛКМ по тегу — изменить. ПКМ — удалить.");
            default -> {
            }
        }
    }

    private void handleTagEditor(Player player, int slot) {
        if (!checkAdmin(player)) return;
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        switch (slot) {
            case 10 -> ask(player, ItemsPlugin.InputType.TAG_ID, "Введите ID тега (a-z, 0-9, _ или -).");
            case 12 -> ask(player, ItemsPlugin.InputType.TAG_NAME, "Введите название тега.");
            case 14 -> gui.openColorSelector();
            case 48 -> gui.saveTagDraft();
            case 49 -> {
                plugin.setTagDraft(player, null);
                gui.openTagList(0);
            }
            default -> {
            }
        }
    }

    private void handleTagSelector(Player player, int page, int slot) {
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        List<Tag> tags = ItemsMenuGUI.sortedTags(plugin);
        if (slot < 45) {
            int index = page * 45 + slot;
            ItemDraft draft = plugin.getItemDraft(player);
            if (draft != null && index >= 0 && index < tags.size()) {
                draft.toggleTag(tags.get(index).getId());
                gui.openTagSelector(page);
            }
        } else if (slot == 46) {
            gui.openTagSelector(page - 1);
        } else if (slot == 47) {
            gui.openTagSelector(page + 1);
        } else if (slot == 48 || slot == 49) {
            gui.openItemEditor();
        }
    }

    private void handleColors(Player player, int slot) {
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        TagDraft draft = plugin.getTagDraft(player);
        if (draft == null) {
            gui.openTagEditor();
            return;
        }
        if (slot < ItemsMenuGUI.colorCount()) {
            Color color = ItemsMenuGUI.colorAt(slot);
            if (color != null) {
                draft.setColor(color);
                gui.openTagEditor();
            }
        } else if (slot == 45) {
            ask(player, ItemsPlugin.InputType.TAG_COLOR, "Введите цвет в формате #RRGGBB, например #33AAFF.");
        } else if (slot == 49) {
            gui.openTagEditor();
        }
    }

    private void handleStorage(Player player, int page, int slot, ClickType click) {
        ChestGUI gui = new ChestGUI(plugin, player);
        PlayerStorage storage = plugin.getStorageManager().getPlayerStorage(player.getUniqueId());
        if (slot < 45) {
            int index = page * 45 + slot;
            if (index < 0 || index >= storage.getItemCount()) return;
            if (click.isRightClick()) {
                CustomItem removed = storage.removeItem(index);
                plugin.getStorageManager().savePlayer(player.getUniqueId().toString());
                player.sendMessage("§eПредмет удалён из хранилища: §f" + removed.getDisplayName());
                gui.openChest(page);
            } else {
                CustomItem item = storage.getItems().get(index);
                Map<Integer, ItemStack> leftovers = player.getInventory().addItem(ItemStackFactory.create(plugin, item));
                if (leftovers.isEmpty()) {
                    storage.removeItem(index);
                    plugin.getStorageManager().savePlayer(player.getUniqueId().toString());
                    player.sendMessage("§aПредмет получен.");
                    gui.openChest(page);
                } else {
                    player.sendMessage("§cВ инвентаре нет свободного места.");
                }
            }
            return;
        }
        switch (slot) {
            case 46 -> gui.openAddMenu(0);
            case 47 -> {
                storage.getItems().sort(Comparator.comparing(CustomItem::getId));
                plugin.getStorageManager().savePlayer(player.getUniqueId().toString());
                gui.openChest(page);
            }
            case 48 -> gui.openChest(page - 1);
            case 49 -> gui.openChest(page + 1);
            case 50 -> new ItemsMenuGUI(plugin, player).openMainMenu();
            case 51 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleStorageAdd(Player player, int page, int slot) {
        ChestGUI gui = new ChestGUI(plugin, player);
        List<CustomItem> items = ItemsMenuGUI.sortedItems(plugin);
        if (slot < 45) {
            int index = page * 45 + slot;
            if (index >= 0 && index < items.size()) {
                PlayerStorage storage = plugin.getStorageManager().getPlayerStorage(player.getUniqueId());
                storage.addItem(items.get(index).copy());
                plugin.getStorageManager().savePlayer(player.getUniqueId().toString());
                player.sendMessage("§aПредмет добавлен в ваше хранилище.");
                gui.openAddMenu(page);
            }
        } else if (slot == 45) {
            gui.openChest(0);
        } else if (slot == 46) {
            gui.openAddMenu(page - 1);
        } else if (slot == 47) {
            gui.openAddMenu(page + 1);
        } else if (slot == 49) {
            player.closeInventory();
        }
    }

    private void ask(Player player, ItemsPlugin.InputType type, String message) {
        plugin.startInput(player, type);
        player.closeInventory();
        player.sendMessage("§e" + message);
    }

    private void processInput(Player player, ItemsPlugin.InputType type, String rawMessage) {
        if (plugin.getInput(player) != type) {
            return;
        }
        plugin.clearInput(player);
        String message = rawMessage.trim();
        if (message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("cancel")) {
            reopenDraft(player, type);
            return;
        }

        ItemDraft itemDraft = plugin.getItemDraft(player);
        TagDraft tagDraft = plugin.getTagDraft(player);
        switch (type) {
            case ITEM_ID -> {
                if (itemDraft != null && message.matches("[a-zA-Z0-9_-]{1,32}")) {
                    itemDraft.setId(message.toLowerCase(Locale.ROOT));
                    player.sendMessage("§aID установлен.");
                } else {
                    player.sendMessage("§cНеверный ID. Используйте a-z, 0-9, _ или - (до 32 символов).");
                }
                reopenDraft(player, type);
            }
            case ITEM_NAME -> {
                if (itemDraft != null && message.length() >= 1 && message.length() <= 64) {
                    itemDraft.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
                    player.sendMessage("§aНазвание установлено.");
                } else {
                    player.sendMessage("§cНазвание должно быть от 1 до 64 символов.");
                }
                reopenDraft(player, type);
            }
            case ITEM_MAX_DURABILITY -> {
                Integer value = positiveInt(message, 1_000_000);
                if (itemDraft != null && value != null) {
                    itemDraft.setMaxDurability(value);
                    if (value == 0) itemDraft.setCurrentDurability(0);
                    player.sendMessage("§aМаксимальная прочность установлена.");
                } else {
                    player.sendMessage("§cВведите целое число от 0 до 1000000.");
                }
                reopenDraft(player, type);
            }
            case ITEM_CURRENT_DURABILITY -> {
                Integer value = positiveInt(message, 1_000_000);
                if (itemDraft != null && value != null && value <= itemDraft.getMaxDurability()) {
                    itemDraft.setCurrentDurability(value);
                    player.sendMessage("§aТекущая прочность установлена.");
                } else {
                    player.sendMessage("§cВведите число от 0 до максимальной прочности.");
                }
                reopenDraft(player, type);
            }
            case ITEM_DESCRIPTION -> {
                if (itemDraft != null && message.length() <= 200) {
                    itemDraft.setDescription(message.equals("-") ? "" : ChatColor.translateAlternateColorCodes('&', message));
                    player.sendMessage("§aОписание установлено.");
                } else {
                    player.sendMessage("§cОписание не должно быть длиннее 200 символов.");
                }
                reopenDraft(player, type);
            }
            case TAG_ID -> {
                if (tagDraft != null && message.matches("[a-zA-Z0-9_-]{1,32}")) {
                    tagDraft.setId(message.toLowerCase(Locale.ROOT));
                    player.sendMessage("§aID тега установлен.");
                } else {
                    player.sendMessage("§cНеверный ID тега.");
                }
                reopenDraft(player, type);
            }
            case TAG_NAME -> {
                if (tagDraft != null && message.length() >= 1 && message.length() <= 32) {
                    tagDraft.setName(ChatColor.translateAlternateColorCodes('&', message));
                    player.sendMessage("§aНазвание тега установлено.");
                } else {
                    player.sendMessage("§cНазвание должно быть от 1 до 32 символов.");
                }
                reopenDraft(player, type);
            }
            case TAG_COLOR -> {
                Color color = parseColor(message);
                if (tagDraft != null && color != null) {
                    tagDraft.setColor(color);
                    player.sendMessage("§aЦвет тега установлен.");
                } else {
                    player.sendMessage("§cНеверный цвет. Пример правильного значения: #33AAFF.");
                }
                reopenDraft(player, type);
            }
        }
    }

    private void reopenDraft(Player player, ItemsPlugin.InputType type) {
        ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
        if (type.name().startsWith("ITEM_")) {
            if (plugin.getItemDraft(player) != null) gui.openItemEditor();
        } else if (plugin.getTagDraft(player) != null) {
            gui.openTagEditor();
        }
    }

    private Integer positiveInt(String value, int max) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 0 && parsed <= max ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Color parseColor(String value) {
        String normalized = value.trim();
        if (normalized.startsWith("#")) normalized = normalized.substring(1);
        if (!normalized.matches("[0-9a-fA-F]{6}")) return null;
        try {
            return Color.fromRGB(Integer.parseInt(normalized, 16));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean checkAdmin(Player player) {
        if (!player.hasPermission("items.admin")) {
            player.sendMessage("§cНедостаточно прав. Требуется items.admin.");
            return false;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6========== ItemsPlugin ==========");
        player.sendMessage("§eЛКМ по предмету §7— открыть редактор, §eПКМ §7— удалить.");
        player.sendMessage("§eРедактор §7позволяет выбрать материал, имя, описание, теги и прочность.");
        player.sendMessage("§eТеги §7поддерживают готовые цвета и любой HEX-цвет.");
        player.sendMessage("§eХранилище §7персональное, бесконечное и сохраняется в storage/<UUID>.yml.");
        player.sendMessage("§e/chest §7открывает хранилище, ЛКМ получает предмет, ПКМ удаляет.");
        player.sendMessage("§6=================================");
    }
}

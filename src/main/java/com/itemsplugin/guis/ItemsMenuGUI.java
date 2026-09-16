package com.itemsplugin.guis;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.models.CustomItem;
import com.itemsplugin.models.ItemDraft;
import com.itemsplugin.models.Tag;
import com.itemsplugin.models.TagDraft;
import com.itemsplugin.util.ItemStackFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/** Все меню управления шаблонами предметов и тегами. */
public class ItemsMenuGUI {

    private static final int SIZE = 54;
    private static final int CONTENT_SIZE = 45;
    private static final List<Material> MATERIALS = loadMaterials();
    private static final List<ColorChoice> COLORS = loadColors();

    private final ItemsPlugin plugin;
    private final Player player;

    public ItemsMenuGUI(ItemsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void openMainMenu() {
        Inventory inventory = inventory(MenuHolder.Type.MAIN, 0, "§6Меню предметов");
        inventory.setItem(10, button(Material.CHEST, "§aВсе предметы", "Создание, изменение и удаление шаблонов"));
        inventory.setItem(12, button(Material.CRAFTING_TABLE, "§bСоздать предмет", "Открыть редактор нового предмета"));
        inventory.setItem(14, button(Material.NAME_TAG, "§dТеги", "Создать теги и выбрать им цвет"));
        inventory.setItem(16, button(Material.SHULKER_BOX, "§eЛичное хранилище", "Бесконечное хранилище только для вас"));
        inventory.setItem(48, button(Material.BOOK, "§6Справка", "Команды и управление"));
        inventory.setItem(49, button(Material.BARRIER, "§cЗакрыть", ""));
        player.openInventory(inventory);
    }

    public void openItemList(int page) {
        List<CustomItem> items = new ArrayList<>(plugin.getItemManager().getAllItems().values());
        items.sort(Comparator.comparing(CustomItem::getId));
        page = clampPage(page, items.size());
        Inventory inventory = inventory(MenuHolder.Type.ITEMS, page, "§6Шаблоны предметов §8| §f" + (page + 1));
        fillPage(inventory, items, page, item -> ItemStackFactory.create(plugin, item));

        inventory.setItem(45, button(Material.ARROW, "§6Назад", "Вернуться в главное меню"));
        if (pluginHasAdmin()) {
            inventory.setItem(46, button(Material.EMERALD, "§aСоздать", "Создать новый шаблон"));
        }
        inventory.setItem(47, button(Material.NAME_TAG, "§dТеги", "Управление тегами"));
        inventory.setItem(48, button(Material.SHULKER_BOX, "§eХранилище", "Открыть личное хранилище"));
        inventory.setItem(49, button(Material.BARRIER, "§cЗакрыть", ""));
        if (page > 0) {
            inventory.setItem(50, button(Material.ARROW, "§fПредыдущая страница", ""));
        }
        if (page < pageCount(items.size()) - 1) {
            inventory.setItem(51, button(Material.ARROW, "§fСледующая страница", ""));
        }
        inventory.setItem(52, button(Material.BOOK, "§7Подсказка", "ЛКМ — изменить, ПКМ — удалить"));
        player.openInventory(inventory);
    }

    public void beginCreateItem(String requestedId) {
        if (!pluginHasAdmin()) {
            player.sendMessage("§cНедостаточно прав. Требуется items.admin.");
            return;
        }
        ItemDraft draft = new ItemDraft();
        if (requestedId != null && requestedId.matches("[a-zA-Z0-9_-]{1,32}")) {
            draft.setId(requestedId.toLowerCase(Locale.ROOT));
        }
        plugin.setItemDraft(player, draft);
        openItemEditor();
    }

    public void beginEditItem(CustomItem item) {
        if (!pluginHasAdmin()) {
            player.sendMessage("§cНедостаточно прав. Требуется items.admin.");
            return;
        }
        plugin.setItemDraft(player, ItemDraft.fromItem(item));
        openItemEditor();
    }

    public void openItemEditor() {
        ItemDraft draft = plugin.getItemDraft(player);
        if (draft == null) {
            beginCreateItem(null);
            return;
        }
        Inventory inventory = inventory(MenuHolder.Type.ITEM_EDITOR, 0, "§6Редактор предмета");
        inventory.setItem(10, button(draft.getMaterial(), "§bМатериал: §f" + draft.getMaterial().name(),
                "Нажмите, чтобы выбрать материал"));
        inventory.setItem(12, button(Material.NAME_TAG, "§aID: §f" + draft.getId(),
                "Нажмите и введите ID латиницей"));
        inventory.setItem(14, button(Material.WRITABLE_BOOK, "§aНазвание", "§f" + draft.getDisplayName(),
                "Нажмите и введите название"));
        String maxText = draft.getMaxDurability() <= 0 ? "без ограничений" : String.valueOf(draft.getMaxDurability());
        inventory.setItem(16, button(Material.IRON_INGOT, "§cМакс. прочность: §f" + maxText,
                "0 — убрать прочность; нажмите для ввода"));
        String currentText = draft.getMaxDurability() <= 0 ? "не используется" : String.valueOf(draft.getCurrentDurability());
        inventory.setItem(28, button(Material.ANVIL, "§eТекущая прочность: §f" + currentText,
                "Начальная прочность; нажмите для ввода"));
        inventory.setItem(30, button(Material.PAPER, "§7Описание", draft.getDescription().isEmpty()
                ? "Не задано" : draft.getDescription(), "Нажмите и введите описание"));
        inventory.setItem(32, button(Material.NAME_TAG, "§dТеги: §f" + draft.getTagIds().size(),
                "Нажмите, чтобы выбрать теги"));
        inventory.setItem(48, button(Material.EMERALD_BLOCK, "§aСохранить", "Сохранить предмет"));
        inventory.setItem(49, button(Material.BARRIER, "§cОтмена", "Вернуться без сохранения"));
        player.openInventory(inventory);
    }

    public void openMaterialSelector(int page) {
        page = clampPage(page, MATERIALS.size());
        Inventory inventory = inventory(MenuHolder.Type.MATERIALS, page, "§6Выбор материала §8| §f" + (page + 1));
        int start = page * CONTENT_SIZE;
        for (int i = 0; i < CONTENT_SIZE && start + i < MATERIALS.size(); i++) {
            Material material = MATERIALS.get(start + i);
            inventory.setItem(i, button(material, "§f" + material.name(), "Выбрать этот материал"));
        }
        inventory.setItem(45, button(Material.ARROW, "§6Назад", "Вернуться к редактору"));
        if (page > 0) {
            inventory.setItem(48, button(Material.ARROW, "§fПредыдущая", ""));
        }
        if (page < pageCount(MATERIALS.size()) - 1) {
            inventory.setItem(49, button(Material.ARROW, "§fСледующая", ""));
        }
        player.openInventory(inventory);
    }

    public void openTagList(int page) {
        List<Tag> tags = new ArrayList<>(plugin.getItemManager().getAllTags().values());
        tags.sort(Comparator.comparing(Tag::getId));
        page = clampPage(page, tags.size());
        Inventory inventory = inventory(MenuHolder.Type.TAGS, page, "§6Управление тегами §8| §f" + (page + 1));
        fillPage(inventory, tags, page, this::tagStack);
        inventory.setItem(45, button(Material.ARROW, "§6Назад", "Вернуться в главное меню"));
        if (pluginHasAdmin()) {
            inventory.setItem(46, button(Material.LIME_DYE, "§aСоздать тег", "Создать новый тег"));
        }
        inventory.setItem(47, button(Material.CHEST, "§bПредметы", "Вернуться к предметам"));
        inventory.setItem(49, button(Material.BARRIER, "§cЗакрыть", ""));
        if (page > 0) {
            inventory.setItem(50, button(Material.ARROW, "§fПредыдущая страница", ""));
        }
        if (page < pageCount(tags.size()) - 1) {
            inventory.setItem(51, button(Material.ARROW, "§fСледующая страница", ""));
        }
        inventory.setItem(52, button(Material.BOOK, "§7Подсказка", "ЛКМ — изменить, ПКМ — удалить"));
        player.openInventory(inventory);
    }

    public void beginCreateTag() {
        if (!pluginHasAdmin()) {
            player.sendMessage("§cНедостаточно прав. Требуется items.admin.");
            return;
        }
        plugin.setTagDraft(player, new TagDraft());
        openTagEditor();
    }

    public void beginEditTag(Tag tag) {
        if (!pluginHasAdmin()) {
            player.sendMessage("§cНедостаточно прав. Требуется items.admin.");
            return;
        }
        plugin.setTagDraft(player, TagDraft.fromTag(tag));
        openTagEditor();
    }

    public void openTagEditor() {
        TagDraft draft = plugin.getTagDraft(player);
        if (draft == null) {
            beginCreateTag();
            return;
        }
        Inventory inventory = inventory(MenuHolder.Type.TAG_EDITOR, 0, "§6Редактор тега");
        inventory.setItem(10, button(Material.NAME_TAG, "§aID: §f" + draft.getId(),
                "Латиница, цифры, _ и -"));
        inventory.setItem(12, button(Material.PAPER, "§fНазвание: " + draft.getName(),
                "Отображаемое имя тега"));
        inventory.setItem(14, colorButton(draft.getColor(), "§dЦвет: " + hex(draft.getColor()),
                "Выбрать цвет или ввести HEX"));
        inventory.setItem(48, button(Material.EMERALD_BLOCK, "§aСохранить", "Сохранить тег"));
        inventory.setItem(49, button(Material.BARRIER, "§cОтмена", "Вернуться без сохранения"));
        player.openInventory(inventory);
    }

    public void openColorSelector() {
        Inventory inventory = inventory(MenuHolder.Type.COLORS, 0, "§6Цвет тега");
        for (int i = 0; i < COLORS.size(); i++) {
            ColorChoice choice = COLORS.get(i);
            inventory.setItem(i, colorButton(choice.color, "§f" + choice.name,
                    "§7" + hex(choice.color)));
        }
        inventory.setItem(45, button(Material.PAPER, "§fСвой HEX-цвет", "Например: #33AAFF"));
        inventory.setItem(49, button(Material.ARROW, "§6Назад", "Вернуться к редактору"));
        player.openInventory(inventory);
    }

    public void openTagSelector(int page) {
        ItemDraft draft = plugin.getItemDraft(player);
        if (draft == null) {
            openItemEditor();
            return;
        }
        List<Tag> tags = new ArrayList<>(plugin.getItemManager().getAllTags().values());
        tags.sort(Comparator.comparing(Tag::getId));
        page = clampPage(page, tags.size());
        Inventory inventory = inventory(MenuHolder.Type.TAG_SELECTOR, page, "§6Теги предмета §8| §f" + (page + 1));
        int start = page * CONTENT_SIZE;
        for (int i = 0; i < CONTENT_SIZE && start + i < tags.size(); i++) {
            Tag tag = tags.get(start + i);
            boolean selected = draft.hasTag(tag.getId());
            inventory.setItem(i, colorButton(tag.getColor(), (selected ? "§a✔ " : "§7○ ") + tag.getName(),
                    selected ? "Выбран — нажмите, чтобы убрать" : "Нажмите, чтобы выбрать"));
        }
        inventory.setItem(45, button(Material.PAPER, "§fВыбрано: " + draft.getTagIds().size(), ""));
        if (page > 0) {
            inventory.setItem(46, button(Material.ARROW, "§fПредыдущая", ""));
        }
        if (page < pageCount(tags.size()) - 1) {
            inventory.setItem(47, button(Material.ARROW, "§fСледующая", ""));
        }
        inventory.setItem(48, button(Material.EMERALD, "§aГотово", "Вернуться к редактору"));
        inventory.setItem(49, button(Material.ARROW, "§6Назад", "Вернуться к редактору"));
        player.openInventory(inventory);
    }

    public static ItemStack createItemStack(ItemsPlugin plugin, CustomItem item) {
        return ItemStackFactory.create(plugin, item);
    }

    public static Material materialAt(int page, int slot) {
        int index = page * CONTENT_SIZE + slot;
        return index >= 0 && index < MATERIALS.size() ? MATERIALS.get(index) : null;
    }

    public static Color colorAt(int slot) {
        return slot >= 0 && slot < COLORS.size() ? COLORS.get(slot).color : null;
    }

    public static int colorCount() {
        return COLORS.size();
    }

    public static List<Tag> sortedTags(ItemsPlugin plugin) {
        List<Tag> tags = new ArrayList<>(plugin.getItemManager().getAllTags().values());
        tags.sort(Comparator.comparing(Tag::getId));
        return tags;
    }

    public static List<CustomItem> sortedItems(ItemsPlugin plugin) {
        List<CustomItem> items = new ArrayList<>(plugin.getItemManager().getAllItems().values());
        items.sort(Comparator.comparing(CustomItem::getId));
        return items;
    }

    public void saveItemDraft() {
        ItemDraft draft = plugin.getItemDraft(player);
        if (draft == null) {
            return;
        }
        String id = draft.getId() == null ? "" : draft.getId().toLowerCase(Locale.ROOT);
        if (!id.matches("[a-z0-9_-]{1,32}")) {
            player.sendMessage("§cID должен содержать 1–32 символа: a-z, 0-9, _ или -.");
            return;
        }
        if (draft.getDisplayName() == null || draft.getDisplayName().isBlank()) {
            player.sendMessage("§cНазвание предмета не может быть пустым.");
            return;
        }
        CustomItem old = draft.getOriginalId() == null ? null : plugin.getItemManager().getItem(draft.getOriginalId());
        CustomItem sameId = plugin.getItemManager().getItem(id);
        if (sameId != null && (old == null || !sameId.getId().equals(old.getId()))) {
            player.sendMessage("§cПредмет с таким ID уже существует.");
            return;
        }
        CustomItem item = new CustomItem(id, draft.getDisplayName(), draft.getMaterial());
        item.setDescription(draft.getDescription());
        item.setMaxDurability(draft.getMaxDurability());
        item.setCurrentDurability(draft.getCurrentDurability());
        for (String tagId : draft.getTagIds()) {
            Tag tag = plugin.getItemManager().getTag(tagId);
            if (tag != null) {
                item.addTag(tag);
            }
        }
        plugin.getItemManager().replaceItem(draft.getOriginalId(), item);
        plugin.getItemManager().saveData();
        plugin.setItemDraft(player, null);
        player.sendMessage("§aПредмет сохранён: §f" + id);
        openItemList(0);
    }

    public void saveTagDraft() {
        TagDraft draft = plugin.getTagDraft(player);
        if (draft == null) {
            return;
        }
        String id = draft.getId() == null ? "" : draft.getId().toLowerCase(Locale.ROOT);
        if (!id.matches("[a-z0-9_-]{1,32}")) {
            player.sendMessage("§cID должен содержать 1–32 символа: a-z, 0-9, _ или -.");
            return;
        }
        if (draft.getName() == null || draft.getName().isBlank()) {
            player.sendMessage("§cНазвание тега не может быть пустым.");
            return;
        }
        Tag old = draft.getOriginalId() == null ? null : plugin.getItemManager().getTag(draft.getOriginalId());
        Tag sameId = plugin.getItemManager().getTag(id);
        if (sameId != null && (old == null || !sameId.getId().equals(old.getId()))) {
            player.sendMessage("§cТег с таким ID уже существует.");
            return;
        }
        Tag tag = new Tag(id, draft.getName(), draft.getColor());
        plugin.getItemManager().replaceTag(draft.getOriginalId(), tag);
        plugin.getItemManager().saveData();
        plugin.setTagDraft(player, null);
        player.sendMessage("§aТег сохранён: §f" + id);
        openTagList(0);
    }

    private boolean pluginHasAdmin() {
        return player.hasPermission("items.admin");
    }

    private Inventory inventory(MenuHolder.Type type, int page, String title) {
        return Bukkit.createInventory(new MenuHolder(type, page), SIZE, title);
    }

    private <T> void fillPage(Inventory inventory, List<T> entries, int page, Function<T, ItemStack> factory) {
        int start = page * CONTENT_SIZE;
        for (int i = 0; i < CONTENT_SIZE && start + i < entries.size(); i++) {
            inventory.setItem(i, factory.apply(entries.get(start + i)));
        }
    }

    private ItemStack tagStack(Tag tag) {
        return colorButton(tag.getColor(), "§f" + tag.getName(), "§7ID: " + tag.getId(),
                "ЛКМ — изменить, ПКМ — удалить");
    }

    private ItemStack button(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material == null ? Material.STONE : material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lines = new ArrayList<>();
            for (String line : lore) {
                if (line != null && !line.isEmpty()) {
                    lines.add("§7" + line);
                }
            }
            meta.setLore(lines);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack colorButton(Color color, String name, String... lore) {
        Material material = Material.WHITE_DYE;
        ItemStack stack = button(material, name, lore);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            // Цвет отображается RGB-именем; цветной краситель остаётся понятным на 1.18.2.
            meta.setDisplayName(net.md_5.bungee.api.ChatColor.of(hex(color))
                    + name.replaceAll("§[0-9a-fk-or]", ""));
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

    private String hex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static List<Material> loadMaterials() {
        List<Material> result = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material != Material.AIR && material.isItem()) {
                result.add(material);
            }
        }
        result.sort(Comparator.comparing(Material::name));
        return result;
    }

    private static List<ColorChoice> loadColors() {
        List<ColorChoice> result = new ArrayList<>();
        result.add(new ColorChoice("Красный", Color.RED));
        result.add(new ColorChoice("Оранжевый", Color.ORANGE));
        result.add(new ColorChoice("Жёлтый", Color.YELLOW));
        result.add(new ColorChoice("Зелёный", Color.GREEN));
        result.add(new ColorChoice("Бирюзовый", Color.TEAL));
        result.add(new ColorChoice("Голубой", Color.AQUA));
        result.add(new ColorChoice("Синий", Color.BLUE));
        result.add(new ColorChoice("Фиолетовый", Color.PURPLE));
        result.add(new ColorChoice("Розовый", Color.FUCHSIA));
        result.add(new ColorChoice("Белый", Color.WHITE));
        result.add(new ColorChoice("Серый", Color.GRAY));
        result.add(new ColorChoice("Чёрный", Color.BLACK));
        result.add(new ColorChoice("Коричневый", Color.MAROON));
        return result;
    }

    private static class ColorChoice {
        private final String name;
        private final Color color;

        private ColorChoice(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }
}

package com.itemsplugin.commands;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.guis.ItemsMenuGUI;
import com.itemsplugin.models.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemsCommand implements CommandExecutor {

    private final ItemsPlugin plugin;

    public ItemsCommand(ItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Эта часть команды доступна только игроку.");
                return true;
            }
            plugin.openMainMenu((Player) sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("help")) {
            sendHelp(sender);
            return true;
        }
        if (subCommand.equals("reload")) {
            if (!isAdmin(sender)) {
                sendNoPermission(sender);
                return true;
            }
            // Данные сохраняются автоматически; перезагрузка без перезапуска намеренно
            // не выполняется, чтобы не потерять открытые редакторы и хранилища.
            sender.sendMessage(ChatColor.GREEN + "Данные плагина уже загружаются при старте сервера.");
            return true;
        }

        if (subCommand.equals("create")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Создание через меню доступно только игроку.");
                return true;
            }
            if (!isAdmin(sender)) {
                sendNoPermission(sender);
                return true;
            }
            Player player = (Player) sender;
            ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
            gui.beginCreateItem(args.length > 1 ? args[1] : null);
            return true;
        }

        if (subCommand.equals("edit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Редактирование через меню доступно только игроку.");
                return true;
            }
            if (!isAdmin(sender)) {
                sendNoPermission(sender);
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.YELLOW + "Использование: /items edit <id>");
                return true;
            }
            CustomItem item = plugin.getItemManager().getItem(args[1]);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Предмет с таким ID не найден.");
                return true;
            }
            new ItemsMenuGUI(plugin, (Player) sender).beginEditItem(item);
            return true;
        }

        if (subCommand.equals("delete")) {
            if (!isAdmin(sender)) {
                sendNoPermission(sender);
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.YELLOW + "Использование: /items delete <id>");
                return true;
            }
            if (plugin.getItemManager().getItem(args[1]) == null) {
                sender.sendMessage(ChatColor.RED + "Предмет с таким ID не найден.");
                return true;
            }
            plugin.getItemManager().deleteItem(args[1]);
            plugin.getItemManager().saveData();
            sender.sendMessage(ChatColor.GREEN + "Предмет удалён: " + args[1]);
            return true;
        }

        if (subCommand.equals("give")) {
            if (!isAdmin(sender)) {
                sendNoPermission(sender);
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.YELLOW + "Использование: /items give <id> [игрок]");
                return true;
            }
            Player target;
            if (args.length >= 3) {
                target = Bukkit.getPlayerExact(args[2]);
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                target = null;
            }
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок должен быть в сети.");
                return true;
            }
            CustomItem item = plugin.getItemManager().getItem(args[1]);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Предмет с таким ID не найден.");
                return true;
            }
            Map<Integer, ItemStack> leftovers = target.getInventory().addItem(
                    ItemsMenuGUI.createItemStack(plugin, item));
            for (ItemStack leftover : leftovers.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), leftover);
            }
            sender.sendMessage(ChatColor.GREEN + "Предмет выдан игроку " + target.getName() + ".");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используйте /items help.");
        return true;
    }

    private boolean isAdmin(CommandSender sender) {
        return sender.hasPermission("items.admin");
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Недостаточно прав. Требуется items.admin.");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== ItemsPlugin ==========");
        sender.sendMessage(ChatColor.YELLOW + "/items" + ChatColor.GRAY + " — открыть главное меню");
        sender.sendMessage(ChatColor.YELLOW + "/items create [id]" + ChatColor.GRAY + " — создать предмет");
        sender.sendMessage(ChatColor.YELLOW + "/items edit <id>" + ChatColor.GRAY + " — изменить предмет");
        sender.sendMessage(ChatColor.YELLOW + "/items delete <id>" + ChatColor.GRAY + " — удалить шаблон");
        sender.sendMessage(ChatColor.YELLOW + "/items give <id> [игрок]" + ChatColor.GRAY + " — выдать предмет");
        sender.sendMessage(ChatColor.YELLOW + "/chest" + ChatColor.GRAY + " — открыть личное бесконечное хранилище");
        sender.sendMessage(ChatColor.GOLD + "================================");
    }
}

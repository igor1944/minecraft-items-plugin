package com.itemsplugin.commands;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.guis.ChestGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChestCommand implements CommandExecutor {

    private final ItemsPlugin plugin;

    public ChestCommand(ItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда доступна только игроку.");
            return true;
        }
        Player player = (Player) sender;
        int page = 0;
        if (args.length > 0) {
            try {
                page = Math.max(1, Integer.parseInt(args[0])) - 1;
            } catch (NumberFormatException ignored) {
                player.sendMessage(ChatColor.RED + "Номер страницы должен быть числом.");
                return true;
            }
        }
        new ChestGUI(plugin, player).openChest(page);
        return true;
    }
}

package com.itemsplugin.commands;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.guis.ChestGUI;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Открытие персонального хранилища
        ChestGUI gui = new ChestGUI(plugin, player);
        gui.openChest();
        
        return true;
    }
}

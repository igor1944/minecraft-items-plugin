package com.itemsplugin.commands;

import com.itemsplugin.ItemsPlugin;
import com.itemsplugin.guis.ItemsMenuGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemsCommand implements CommandExecutor {
    
    private final ItemsPlugin plugin;
    
    public ItemsCommand(ItemsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
            gui.openMenu();
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /items create <ID>");
                    return true;
                }
                player.sendMessage("§aОткройте меню создания предмета");
                ItemsMenuGUI gui = new ItemsMenuGUI(plugin, player);
                gui.openMenu();
                break;
                
            case "help":
                sendHelp(player);
                break;
                
            default:
                player.sendMessage("§cНеизвестная подкоманда!");
                sendHelp(player);
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§6ItemsPlugin - Справка");
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§e/items §7- Открыть меню управления");
        player.sendMessage("§e/items create <ID> §7- Создать новый предмет");
        player.sendMessage("§e/chest §7- Открыть персональное хранилище");
        player.sendMessage("§6═══════════════════════════════════");
    }
}
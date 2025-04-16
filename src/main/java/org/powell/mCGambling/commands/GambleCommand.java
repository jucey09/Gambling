package org.powell.mCGambling.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.powell.mCGambling.MCGambling;

public class GambleCommand implements CommandExecutor {
    private final MCGambling main;
    public GambleCommand(MCGambling main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player player){
            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("line")){
                    main.getSpinner().openGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "INVALID USAGE! Do /mcg <type>, the types are line, _, _.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "INVALID USAGE! Do /mcg <type>.");
            }
        }
        return false;
    }
}

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (args.length != 1) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "line" -> main.getLineGamble().openGUI(player);
            case "slot" -> main.getSlotMachine().openGUI(player);
            case "roulette" -> main.getRussianRoulett().openGUI(player);
            default -> sendUsage(player);
        }
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /mcg <game>");
        player.sendMessage(ChatColor.GRAY + "Available games:");
        player.sendMessage(ChatColor.GRAY + "- line: Line gambling game");
        player.sendMessage(ChatColor.GRAY + "- slot: Slot machine");
        player.sendMessage(ChatColor.GRAY + "- roulette: Russian roulette");
    }
}

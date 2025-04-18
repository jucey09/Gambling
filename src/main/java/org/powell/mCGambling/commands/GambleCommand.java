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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use gambling commands!");
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
            case "dice" -> main.getDiceGame().openGUI(player);
            case "tower" -> main.getCoinTower().openGUI(player);
            case "treasure" -> main.getTreasureHunt().openGUI(player);
            case "wheel" -> main.getWheelOfFortune().openGUI(player);
            case "blackjack" -> main.getBlackjack().openGUI(player);
            case "highlow" -> main.getHighLow().openGUI(player);
            case "crash" -> main.getCrashGame().openGUI(player);
            case "poker" -> main.getPoker().openGUI(player);
            default -> {
                sendUsage(player);
                return true;
            }
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== MCGambling Games ===");
        player.sendMessage(ChatColor.YELLOW + "/mcg line" + ChatColor.GRAY + " - Play Line Gambling");
        player.sendMessage(ChatColor.YELLOW + "/mcg slot" + ChatColor.GRAY + " - Play Slot Machine");
        player.sendMessage(ChatColor.YELLOW + "/mcg roulette" + ChatColor.GRAY + " - Play Russian Roulette");
        player.sendMessage(ChatColor.YELLOW + "/mcg dice" + ChatColor.GRAY + " - Play Dice Game");
        player.sendMessage(ChatColor.YELLOW + "/mcg tower" + ChatColor.GRAY + " - Play Coin Tower");
        player.sendMessage(ChatColor.YELLOW + "/mcg treasure" + ChatColor.GRAY + " - Play Treasure Hunt");
        player.sendMessage(ChatColor.YELLOW + "/mcg wheel" + ChatColor.GRAY + " - Play Wheel of Fortune");
        player.sendMessage(ChatColor.YELLOW + "/mcg blackjack" + ChatColor.GRAY + " - Play Blackjack");
        player.sendMessage(ChatColor.YELLOW + "/mcg highlow" + ChatColor.GRAY + " - Play High/Low");
        player.sendMessage(ChatColor.YELLOW + "/mcg crash" + ChatColor.GRAY + " - Play Crash Game");
        player.sendMessage(ChatColor.YELLOW + "/mcg poker" + ChatColor.GRAY + " - Play Five Card Draw Poker");
    }
}

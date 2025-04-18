package org.powell.mCGambling.guis;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.MCGambling;

import java.util.Random;

public class HighLow implements Listener {
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 99;
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.GOLD + "High/Low";
    private final Inventory inv;
    private boolean isPlaying = false;
    private int currentNumber;

    public HighLow(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
        this.inv = main.getGuiApi().createGui(null, 27, title);
        setupGUI();
    }

    public void openGUI(Player player) {
        player.openInventory(inv);
    }

    private void setupGUI() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        gui.setItemName(closeButton, ChatColor.RED, "Close");
        gui.setItem(inv, closeButton, 0);

        ItemStack wager = new ItemStack(Material.DIAMOND);
        gui.setItem(inv, wager, 22);

        ItemStack startButton = new ItemStack(Material.LIME_CONCRETE);
        gui.setItemName(startButton, ChatColor.GREEN, "START");
        gui.setItemLore(startButton, ChatColor.GRAY, "Place your bet and click to start!");
        gui.setItem(inv, startButton, 26);

        ItemStack higherButton = new ItemStack(Material.ARROW);
        gui.setItemName(higherButton, ChatColor.GREEN, "HIGHER ↑");
        gui.setItem(inv, higherButton, 15);

        ItemStack lowerButton = new ItemStack(Material.ARROW);
        gui.setItemName(lowerButton, ChatColor.RED, "LOWER ↓");
        gui.setItem(inv, lowerButton, 11);

        updateNumberDisplay();

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                gui.setFrames(inv, i);
            }
        }
    }

    private void updateNumberDisplay() {
        ItemStack numberDisplay = new ItemStack(Material.PAPER);
        if (isPlaying) {
            gui.setItemName(numberDisplay, ChatColor.GOLD, "Current Number: " + currentNumber);
            gui.setItemLore(numberDisplay, ChatColor.GRAY, "Will the next number be higher or lower?");
        } else {
            gui.setItemName(numberDisplay, ChatColor.GRAY, "Place a bet to start!");
        }
        gui.setItem(inv, numberDisplay, 13);
    }

    private void startGame(Player player) {
        if (isPlaying) return;

        ItemStack wager = inv.getItem(22);
        if (wager == null || wager.getType() != Material.DIAMOND) {
            player.sendMessage(ChatColor.RED + "Please place a diamond wager first!");
            return;
        }

        isPlaying = true;
        currentNumber = new Random().nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
        updateNumberDisplay();
        player.sendMessage(ChatColor.GREEN + "Game started! Will the next number be higher or lower than " + currentNumber + "?");
    }

    private void makeGuess(Player player, boolean guessedHigher) {
        if (!isPlaying) return;

        int nextNumber = new Random().nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
        boolean isHigher = nextNumber > currentNumber;

        ItemStack result = new ItemStack(Material.PAPER);
        gui.setItemName(result, ChatColor.GOLD, "Result: " + nextNumber);
        gui.setItem(inv, result, 13);

        if ((guessedHigher && isHigher) || (!guessedHigher && !isHigher)) {
            win(player);
        } else if (nextNumber == currentNumber) {
            push(player);
        } else {
            lose(player, "Wrong guess! The number was " + nextNumber + "!");
        }
    }

    private void win(Player player) {
        ItemStack wager = inv.getItem(22);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            int winAmount = wager.getAmount() * 2;
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + "You won " + winAmount + " diamonds!");
            inv.setItem(22, null);
        }
        isPlaying = false;
        updateNumberDisplay();
    }

    private void push(Player player) {
        ItemStack wager = inv.getItem(22);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            player.getInventory().addItem(wager);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.sendMessage(ChatColor.YELLOW + "Push! Same number! Your wager has been returned.");
            inv.setItem(22, null);
        }
        isPlaying = false;
        updateNumberDisplay();
    }

    private void lose(Player player, String message) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        player.sendMessage(ChatColor.RED + message);
        inv.setItem(22, null);
        isPlaying = false;
        updateNumberDisplay();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(title)) return;

        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory() != null && e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;

        if (e.getSlot() == 22 && (e.getCurrentItem().getType() == Material.DIAMOND || e.getCursor().getType() == Material.DIAMOND)) {
            e.setCancelled(false);
            return;
        }

        switch (e.getSlot()) {
            case 0 -> player.closeInventory();
            case 26 -> {
                if (!isPlaying) {
                    startGame(player);
                }
            }
            case 15 -> {
                if (isPlaying) {
                    makeGuess(player, true);
                }
            }
            case 11 -> {
                if (isPlaying) {
                    makeGuess(player, false);
                }
            }
        }
    }
}

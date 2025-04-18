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
import org.bukkit.scheduler.BukkitRunnable;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.MCGambling;

import java.util.Random;

public class CrashGame implements Listener {
    private static final double CRASH_CHANCE_INCREASE = 0.05;
    private static final double BASE_CRASH_CHANCE = 0.05;
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.GOLD + "Stock Market Game";
    private final Inventory inv;
    private boolean isPlaying = false;
    private double multiplier = 1.0;
    private BukkitRunnable gameTask;

    public CrashGame(MCGambling main, GuiApi gui) {
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

        ItemStack cashOutButton = new ItemStack(Material.GOLD_BLOCK);
        gui.setItemName(cashOutButton, ChatColor.YELLOW, "CASH OUT");
        gui.setItemLore(cashOutButton, ChatColor.GRAY, "Click to collect your winnings!");
        gui.setItem(inv, cashOutButton, 13);

        updateMultiplierDisplay();

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                gui.setFrames(inv, i);
            }
        }
    }

    private void updateMultiplierDisplay() {
        ItemStack multiplierDisplay = new ItemStack(Material.PAPER);
        if (isPlaying) {
            gui.setItemName(multiplierDisplay, ChatColor.GOLD, String.format("Multiplier: %.2fx", multiplier));
            gui.setItemLore(multiplierDisplay, ChatColor.GRAY, "Cash out before it crashes!");
        } else {
            gui.setItemName(multiplierDisplay, ChatColor.GRAY, "Place a bet to start!");
        }
        gui.setItem(inv, multiplierDisplay, 4);
    }

    private void startGame(Player player) {
        if (isPlaying) return;

        ItemStack wager = inv.getItem(22);
        if (wager == null || wager.getType() != Material.DIAMOND) {
            player.sendMessage(ChatColor.RED + "Please place a diamond wager first!");
            return;
        }

        isPlaying = true;
        multiplier = 1.0;
        updateMultiplierDisplay();

        gameTask = new BukkitRunnable() {
            private double crashChance = BASE_CRASH_CHANCE;

            @Override
            public void run() {
                if (!isPlaying) {
                    this.cancel();
                    return;
                }

                if (new Random().nextDouble() < crashChance) {
                    crash(player);
                    this.cancel();
                    return;
                }

                multiplier += 0.1;
                crashChance += CRASH_CHANCE_INCREASE;
                updateMultiplierDisplay();

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
            }
        };

        gameTask.runTaskTimer(main, 0L, 10L);
        player.sendMessage(ChatColor.GREEN + "Game started! Cash out before it crashes!");
    }

    private void cashOut(Player player) {
        if (!isPlaying) return;

        ItemStack wager = inv.getItem(22);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            int baseAmount = wager.getAmount();
            int winAmount = (int) (baseAmount * multiplier);
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + String.format("You cashed out at %.2fx and won %d diamonds!", multiplier, winAmount));
            inv.setItem(22, null);
        }

        endGame();
    }

    private void crash(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        player.sendMessage(ChatColor.RED + String.format("CRASH at %.2fx! Better luck next time!", multiplier));
        inv.setItem(22, null);
        endGame();
    }

    private void endGame() {
        isPlaying = false;
        multiplier = 1.0;
        updateMultiplierDisplay();
        if (gameTask != null) {
            gameTask.cancel();
        }
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
            case 13 -> {
                if (isPlaying) {
                    cashOut(player);
                }
            }
        }
    }
}

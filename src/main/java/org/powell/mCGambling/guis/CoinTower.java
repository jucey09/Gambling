package org.powell.mCGambling.guis;

import org.bukkit.*;
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

public class CoinTower implements Listener {
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.GOLD + "Coin Tower";
    private final Inventory inv;
    private final int[] towerSlots = {21, 12, 3};
    private boolean isPlaying = false;

    public CoinTower(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
        this.inv = main.getGuiApi().createGui(null, 27, title);
        setupGUI();
    }

    private void setupGUI() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        gui.setItemName(closeButton, ChatColor.RED, "Close");
        gui.setItem(inv, closeButton, 0);

        ItemStack wager = new ItemStack(Material.DIAMOND);
        gui.setItem(inv, wager, 25);

        ItemStack playButton = new ItemStack(main.getGuiApi().getHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756"));
        gui.setItemName(playButton, ChatColor.GREEN, "CLIMB!");
        gui.setItemLore(playButton, ChatColor.GRAY, "Click to start climbing! Each level multiplies your wager!");
        gui.setItem(inv, playButton, 26);

        resetTower();

        for (int i = 0; i < inv.getSize(); i++) {
            if (i != 0 && i != 25 && i != 26 && !isTowerSlot(i) && inv.getItem(i) == null) {
                gui.setFrames(inv, i);
            }
        }
    }

    private void resetTower() {
        ItemStack coin = new ItemStack(Material.GOLD_NUGGET);
        gui.setItemName(coin, ChatColor.GOLD, "Start Here");
        gui.setItem(inv, coin, towerSlots[0]);

        ItemStack emptySlot = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        for (int i = 1; i < towerSlots.length; i++) {
            gui.setItemName(emptySlot, ChatColor.GRAY, "Level " + (i + 1));
            gui.setItem(inv, emptySlot.clone(), towerSlots[i]);
        }
    }

    private boolean isTowerSlot(int slot) {
        for (int towerSlot : towerSlots) {
            if (towerSlot == slot) return true;
        }
        return false;
    }

    public void openGUI(Player player) {
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 0) {
            player.closeInventory();
        } else if (slot == 26 && !isPlaying) {
            startGame(player);
        }
    }

    private void startGame(Player player) {
        if (isPlaying) return;
        isPlaying = true;

        new BukkitRunnable() {
            int currentLevel = 0;
            final Random random = new Random();

            @Override
            public void run() {
                if (currentLevel >= towerSlots.length) {
                    win(player);
                    isPlaying = false;
                    this.cancel();
                    return;
                }

                if (random.nextDouble() < 0.4) {
                    lose(player);
                    isPlaying = false;
                    this.cancel();
                    return;
                }

                ItemStack coin = new ItemStack(Material.GOLD_NUGGET);
                gui.setItemName(coin, ChatColor.GOLD, "Current Position");
                gui.setItem(inv, coin, towerSlots[currentLevel]);

                if (currentLevel > 0) {
                    ItemStack completed = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                    gui.setItemName(completed, ChatColor.GREEN, "Level " + currentLevel + " Complete!");
                    gui.setItem(inv, completed, towerSlots[currentLevel - 1]);
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (currentLevel * 0.2f));
                currentLevel++;
            }
        }.runTaskTimer(main, 20L, 20L);
    }

    private void win(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Congratulations! You reached the top of the tower!");
        resetTower();
    }

    private void lose(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        player.sendMessage(ChatColor.RED + "Oh no! Your coin fell!");
        resetTower();
    }
}

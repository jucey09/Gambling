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

import java.util.*;

public class TreasureHunt implements Listener {
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.GOLD + "Treasure Hunt";
    private final Inventory inv;
    private final List<Integer> treasureSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25);
    private final Set<Integer> revealedSlots = new HashSet<>();
    private int treasureLocation;
    private boolean isPlaying = false;

    public TreasureHunt(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
        this.inv = main.getGuiApi().createGui(null, 36, title);
        setupGUI();
    }

    private void setupGUI() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        gui.setItemName(closeButton, ChatColor.RED, "Close");
        gui.setItem(inv, closeButton, 0);

        ItemStack wager = new ItemStack(Material.DIAMOND);
        gui.setItem(inv, wager, 31);

        ItemStack startButton = new ItemStack(main.getGuiApi().getHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756"));
        gui.setItemName(startButton, ChatColor.GREEN, "START HUNT!");
        gui.setItemLore(startButton, ChatColor.GRAY, "Click squares to dig for treasure! Find the chest to win big!");
        gui.setItem(inv, startButton, 35);

        resetBoard();

        for (int i = 0; i < inv.getSize(); i++) {
            if (i != 0 && i != 31 && i != 35 && !treasureSlots.contains(i) && inv.getItem(i) == null) {
                gui.setFrames(inv, i);
            }
        }
    }

    private void resetBoard() {
        revealedSlots.clear();
        treasureLocation = treasureSlots.get(new Random().nextInt(treasureSlots.size()));

        ItemStack hiddenSlot = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        gui.setItemName(hiddenSlot, ChatColor.GRAY, "Click to Dig!");

        for (int slot : treasureSlots) {
            gui.setItem(inv, hiddenSlot.clone(), slot);
        }
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
        } else if (slot == 35 && !isPlaying) {
            startGame(player);
        } else if (isPlaying && treasureSlots.contains(slot) && !revealedSlots.contains(slot)) {
            checkSlot(player, slot);
        }
    }

    private void startGame(Player player) {
        if (isPlaying) return;

        ItemStack wager = inv.getItem(31);
        if (wager == null || wager.getType() != Material.DIAMOND) {
            player.sendMessage(ChatColor.RED + "Please place a diamond wager first!");
            return;
        }

        isPlaying = true;
        resetBoard();
        player.sendMessage(ChatColor.GREEN + "The hunt begins! Click squares to dig for treasure!");
    }

    private void checkSlot(Player player, int slot) {
        revealedSlots.add(slot);

        if (slot == treasureLocation) {
            ItemStack treasure = new ItemStack(Material.CHEST);
            gui.setItemName(treasure, ChatColor.GOLD, "TREASURE!");
            gui.setItem(inv, treasure, slot);
            win(player);
        } else {
            ItemStack dirt = new ItemStack(Material.DIRT);
            gui.setItemName(dirt, ChatColor.GRAY, "Just dirt...");
            gui.setItem(inv, dirt, slot);
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);

            if (new Random().nextInt(5) == 0) {
                ItemStack trap = new ItemStack(Material.TNT);
                gui.setItemName(trap, ChatColor.RED, "TRAP!");
                gui.setItem(inv, dirt.clone(), slot);
                lose(player, "You hit a trap! Your diamonds are lost!");
            } else if (revealedSlots.size() >= treasureSlots.size() - 1) {
                lose(player, "You dug everywhere but found nothing! Your diamonds are lost!");
            }
        }
    }

    private void win(Player player) {
        ItemStack wager = inv.getItem(31);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            int winAmount = wager.getAmount() * 3;
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + "Congratulations! You found the treasure and won " + winAmount + " diamonds!");
            inv.setItem(31, null);
        }
        isPlaying = false;
    }

    private void lose(Player player, String message) {
        ItemStack treasure = new ItemStack(Material.CHEST);
        gui.setItemName(treasure, ChatColor.GOLD, "TREASURE WAS HERE!");
        gui.setItem(inv, treasure, treasureLocation);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        player.sendMessage(ChatColor.RED + message);
        inv.setItem(31, null);
        isPlaying = false;
    }
}

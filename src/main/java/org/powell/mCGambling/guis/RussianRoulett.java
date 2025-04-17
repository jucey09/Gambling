package org.powell.mCGambling.guis;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
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

public class RussianRoulett implements Listener {
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.DARK_RED + "Russian Roulette";
    private final Inventory inv;
    private final int[] chamberSlots = {10, 11, 12, 14, 15, 16};
    private boolean isSpinning = false;

    public RussianRoulett(MCGambling main, GuiApi gui) {
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
        gui.setItemName(wager, ChatColor.BLUE, "Place Wager Here");
        gui.setItemLore(wager, ChatColor.GRAY, "Win 5x your bet or Lose Everything");
        gui.setItem(inv, wager, 22);

        ItemStack spinButton = new ItemStack(main.getGuiApi().getHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756"));
        gui.setItemName(spinButton, ChatColor.RED, "PULL TRIGGER");
        gui.setItemLore(spinButton, ChatColor.GRAY, "5/6 Chance to Win, 1/6 Chance to Lose");
        gui.setItem(inv, spinButton, 26);

        ItemStack chamber = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        gui.setItemName(chamber, ChatColor.GRAY, "Chamber");
        for (int slot : chamberSlots) {
            gui.setItem(inv, chamber.clone(), slot);
        }

        ItemStack bullet = new ItemStack(Material.FIRE_CHARGE);
        gui.setItemName(bullet, ChatColor.RED, "Bullet");
        gui.setItem(inv, bullet, 13);

        for (int i = 0; i < 27; i++) {
            if (!isChamberSlot(i) && i != 0 && i != 13 && i != 22 && i != 26) {
                gui.setFrames(inv, i);
            }
        }
    }

    private boolean isChamberSlot(int slot) {
        for (int chamberSlot : chamberSlots) {
            if (chamberSlot == slot) return true;
        }
        return false;
    }

    public void openGUI(Player player) {
        player.openInventory(inv);
    }

    private void startSpinAnimation(Player player) {
        if (isSpinning) return;
        isSpinning = true;

        for (int slot : chamberSlots) {
            ItemStack chamber = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            gui.setItemName(chamber, ChatColor.GRAY, "Chamber");
            gui.setItem(inv, chamber, slot);
        }

        new BukkitRunnable() {
            final int maxTicks = 30;
            final Random random = new Random();
            int ticks = 0;
            int currentChamber = 0;

            @Override
            public void run() {
                ItemStack prevChamber = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                gui.setItemName(prevChamber, ChatColor.GRAY, "Chamber");
                gui.setItem(inv, prevChamber, chamberSlots[currentChamber]);

                if (ticks < maxTicks - 1) {
                    currentChamber = (currentChamber + 1) % chamberSlots.length;
                } else {
                    currentChamber = random.nextInt(chamberSlots.length);
                }

                ItemStack currentChamberItem = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
                gui.setItemName(currentChamberItem, ChatColor.WHITE, "Spinning...");
                gui.setItem(inv, currentChamberItem, chamberSlots[currentChamber]);

                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);

                if (++ticks >= maxTicks) {
                    isSpinning = false;
                    this.cancel();
                    checkResult(player, currentChamber);
                }
            }
        }.runTaskTimer(main, 0L, 2L);
    }

    private void checkResult(Player player, int finalChamber) {
        ItemStack wager = inv.getItem(22);
        if (wager == null || wager.getType() != Material.DIAMOND) return;

        boolean lost = finalChamber == 0;

        if (lost) {
            ItemStack deadChamberItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            gui.setItemName(deadChamberItem, ChatColor.RED, "BANG!");
            gui.setItem(inv, deadChamberItem, chamberSlots[finalChamber]);

            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            player.sendMessage(ChatColor.RED + "BANG! You lost everything!");
            World world = player.getWorld();
            Location location = player.getLocation();
            player.getInventory().remove(Material.TOTEM_OF_UNDYING);
            player.setHealth(0);
            for (int i = 0; i < 50; i++) {
                world.spawnEntity(location, EntityType.TNT);
            }

            wager.setAmount(1);
        } else {
            ItemStack safeChamber = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            gui.setItemName(safeChamber, ChatColor.GREEN, "Click!");
            gui.setItem(inv, safeChamber, chamberSlots[finalChamber]);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
            int winAmount = wager.getAmount() * 5;
            player.sendMessage(ChatColor.GREEN + "Click! You survived and won " + winAmount + " diamonds!");
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(title)) {
            return;
        }

        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory() != null && e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        e.setCancelled(true);

        if (e.getCurrentItem() == null) {
            return;
        }

        if (e.getSlot() == 22 && (e.getCurrentItem().getType() == Material.DIAMOND || e.getCursor().getType() == Material.DIAMOND)) {
            e.setCancelled(false);
            return;
        }

        if (isSpinning) {
            return;
        }

        switch (e.getSlot()) {
            case 0 -> player.closeInventory();
            case 26 -> {
                ItemStack wager = inv.getItem(22);
                if (wager != null && wager.getType() == Material.DIAMOND) {
                    startSpinAnimation(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Please place a diamond wager first!");
                }
            }
        }
    }
}

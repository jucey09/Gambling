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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WheelOfFortune implements Listener {
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.GOLD + "Wheel of Fortune";
    private final Inventory inv;
    private final List<Integer> wheelSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 25, 24, 23, 22, 21, 20, 19);
    private final int arrowSlot = 4;
    private final List<WheelSegment> segments = Arrays.asList(new WheelSegment(Material.DIAMOND_BLOCK, ChatColor.AQUA + "JACKPOT", 10.0), new WheelSegment(Material.GOLD_BLOCK, ChatColor.GOLD + "BIG WIN", 5.0), new WheelSegment(Material.EMERALD_BLOCK, ChatColor.GREEN + "GOOD WIN", 3.0), new WheelSegment(Material.IRON_BLOCK, ChatColor.GRAY + "SMALL WIN", 2.0), new WheelSegment(Material.COAL_BLOCK, ChatColor.DARK_GRAY + "LOSE", 0.0));
    private boolean isSpinning = false;
    private int currentPosition = 0;

    public WheelOfFortune(MCGambling main, GuiApi gui) {
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

        ItemStack spinButton = new ItemStack(main.getGuiApi().getHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756"));
        gui.setItemName(spinButton, ChatColor.GREEN, "SPIN!");
        gui.setItemLore(spinButton, ChatColor.GRAY, "Click to spin the wheel! Land on diamonds for the jackpot!");
        gui.setItem(inv, spinButton, 35);

        setupWheel();

        ItemStack pointer = new ItemStack(Material.ARROW);
        gui.setItemName(pointer, ChatColor.YELLOW, "⬇ Your Prize ⬇");
        gui.setItem(inv, pointer, arrowSlot);

        for (int i = 0; i < inv.getSize(); i++) {
            if (i != 0 && i != 31 && i != 35 && i != arrowSlot && !wheelSlots.contains(i) && inv.getItem(i) == null) {
                gui.setFrames(inv, i);
            }
        }
    }

    private void setupWheel() {
        for (int i = 0; i < wheelSlots.size(); i++) {
            WheelSegment segment = segments.get(i % segments.size());
            ItemStack item = new ItemStack(segment.material);
            gui.setItemName(item, null, segment.name);
            gui.setItem(inv, item, wheelSlots.get(i));
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
        } else if (slot == 35 && !isSpinning) {
            startSpin(player);
        }
    }

    private void startSpin(Player player) {
        if (isSpinning) return;
        isSpinning = true;

        new BukkitRunnable() {
            final int maxTicks = 60 + new Random().nextInt(20);
            final Random random = new Random();
            int ticks = 0;
            int speed = 1;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    checkWin(player);
                    isSpinning = false;
                    this.cancel();
                    return;
                }

                rotateWheel();

                float pitch = 1.0f + (ticks / (float) maxTicks);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);

                if (ticks < maxTicks / 3) speed = Math.min(speed + 1, 4);
                if (ticks > maxTicks / 2) speed = Math.max(1, speed - 1);

                ticks += speed;
            }
        }.runTaskTimer(main, 0L, 2L);
    }

    private void rotateWheel() {
        List<ItemStack> items = new ArrayList<>();
        for (int slot : wheelSlots) {
            items.add(inv.getItem(slot));
        }

        for (int i = 0; i < wheelSlots.size(); i++) {
            int newIndex = (i + 1) % wheelSlots.size();
            gui.setItem(inv, items.get(i), wheelSlots.get(newIndex));
        }

        currentPosition = (currentPosition + 1) % segments.size();
    }

    private void checkWin(Player player) {
        WheelSegment segment = segments.get(currentPosition);
        player.sendMessage(ChatColor.GREEN + "You landed on: " + segment.name);

        if (segment.multiplier > 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            int i = inv.getItem(31).getAmount();
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, 5 + i));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        }
    }

    private static class WheelSegment {
        final Material material;
        final String name;
        final double multiplier;

        WheelSegment(Material material, String name, double multiplier) {
            this.material = material;
            this.name = name;
            this.multiplier = multiplier;
        }
    }
}

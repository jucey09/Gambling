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

public class DiceGame implements Listener {
    private MCGambling main;
    private GuiApi gui;
    private String title = ChatColor.GOLD + "Dice Game";
    private Inventory inv;
    private int[] diceSlots = {11, 13, 15};
    private boolean isRolling = false;

    public DiceGame(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
        this.inv = Bukkit.createInventory(null, 27, title);
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

        ItemStack rollButton = new ItemStack(main.getGuiApi().getHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756"));
        gui.setItemName(rollButton, ChatColor.GREEN, "ROLL DICE!");
        gui.setItemLore(rollButton, ChatColor.GRAY, "Match 3 numbers to win big!Higher numbers = Better rewards");
        gui.setItem(inv, rollButton, 26);

        for (int slot : diceSlots) {
            setDiceFace(slot, 1);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if (i != 0 && i != 22 && i != 26 && !isDiceSlot(i) && inv.getItem(i) == null) {
                gui.setFrames(inv, i);
            }
        }
    }

    private void setDiceFace(int slot, int number) {
        ItemStack dice = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        gui.setItemName(dice, ChatColor.GOLD, "Dice: " + number);
        gui.setItem(inv, dice, slot);
    }

    private boolean isDiceSlot(int slot) {
        for (int diceSlot : diceSlots) {
            if (slot == diceSlot) return true;
        }
        return false;
    }

    private void startRollAnimation(Player player) {
        if (isRolling) return;
        isRolling = true;

        new BukkitRunnable() {
            final int maxTicks = 20;
            final Random random = new Random();
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    isRolling = false;
                    this.cancel();
                    checkResult(player);
                    return;
                }

                for (int slot : diceSlots) {
                    if (ticks < maxTicks - (diceSlots.length - (slot - 11)/2) * 3) {
                        setDiceFace(slot, random.nextInt(6) + 1);
                    }
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, random.nextFloat() * 2);
                ticks++;
            }
        }.runTaskTimer(main, 0L, 2L);
    }

    private void checkResult(Player player) {
        ItemStack wager = inv.getItem(22);
        if (wager == null || wager.getType() != Material.DIAMOND) return;

        int[] numbers = new int[3];
        for (int i = 0; i < diceSlots.length; i++) {
            String diceName = inv.getItem(diceSlots[i]).getItemMeta().getDisplayName();
            numbers[i] = Integer.parseInt(diceName.substring(diceName.length() - 1));
        }

        if (numbers[0] == numbers[1] && numbers[1] == numbers[2]) {
            int multiplier = numbers[0] * 2;
            int winAmount = wager.getAmount() * multiplier;
            player.sendMessage(ChatColor.GREEN + "Three " + numbers[0] + "s! You won " + winAmount + " diamonds!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
        } else {
            player.sendMessage(ChatColor.RED + "No match! Try again!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            wager.setAmount(1);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        if (isRolling) {
            player.sendMessage(ChatColor.RED + "Wait for the current roll to finish!");
            return;
        }

        if (e.getSlot() == 0) {
            player.closeInventory();
            return;
        }

        if (e.getSlot() == 22 && (e.getCurrentItem().getType() == Material.DIAMOND || e.getCursor().getType() == Material.DIAMOND)) {
            e.setCancelled(false);
            return;
        }

        if (e.getSlot() == 26) {
            ItemStack wager = inv.getItem(22);
            if (wager == null || wager.getType() != Material.DIAMOND) {
                player.sendMessage(ChatColor.RED + "Please place a diamond wager first!");
                return;
            }
            startRollAnimation(player);
        }
    }
}

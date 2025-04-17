package org.powell.mCGambling.guis;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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

public class SlotMachine implements Listener {
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.DARK_AQUA + "Slot Machine";
    private final Inventory inv;
    private final int[] reelSlots = {11, 13, 15};
    private final Material[] symbols = {
            Material.DIAMOND,
            Material.GOLD_INGOT,
            Material.EMERALD,
            Material.IRON_INGOT,
            Material.COAL
    };
    private boolean isSpinning = false;

    public SlotMachine(MCGambling main, GuiApi gui) {
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
        gui.setItem(inv, wager, 22);

        ItemStack spinButton = new ItemStack(main.getGuiApi().getHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756"));
        gui.setItemName(spinButton, ChatColor.GREEN, "SPIN!");
        gui.setItem(inv, spinButton, 26);

        for (int slot : reelSlots) {
            setRandomSymbol(slot);
        }

        for (int i = 0; i < 27; i++) {
            if (!isReelSlot(i) && i != 0 && i != 22 && i != 26) {
                gui.setFrames(inv, i);
            }
        }
    }

    private boolean isReelSlot(int slot) {
        for (int reelSlot : reelSlots) {
            if (reelSlot == slot) return true;
        }
        return false;
    }

    private void setRandomSymbol(int slot) {
        Material symbol = symbols[new Random().nextInt(symbols.length)];
        ItemStack item = new ItemStack(symbol);
        gui.setItemName(item, ChatColor.GOLD, symbol.name().replace("_", " "));
        gui.setItem(inv, item, slot);
    }

    public void openGUI(Player player) {
        player.openInventory(inv);
    }

    private void startSpinAnimation(Player player) {
        if (isSpinning) return;
        isSpinning = true;

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 30;
            final Random random = new Random();

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    checkWin(player);
                    isSpinning = false;
                    this.cancel();
                    return;
                }

                for (int slot : reelSlots) {
                    if (ticks < maxTicks - (reelSlots.length - (slot - 11)/2) * 5) {
                        setRandomSymbol(slot);
                    }
                }

                player.updateInventory();
                ticks++;
            }
        }.runTaskTimer(main, 0L, 2L);
    }

    private void checkWin(Player player) {
        ItemStack firstSymbol = inv.getItem(reelSlots[0]);
        boolean isWinner = true;

        for (int slot : reelSlots) {
            ItemStack symbol = inv.getItem(slot);
            if (symbol == null || firstSymbol == null || symbol.getType() != firstSymbol.getType()) {
                isWinner = false;
                break;
            }
        }

        ItemStack wager = inv.getItem(22);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            if (isWinner) {
                int multiplier = getMultiplier(firstSymbol.getType());
                int winAmount = wager.getAmount() * multiplier;
                player.sendMessage(ChatColor.GREEN + "Congratulations! You won " + winAmount + " diamonds!");
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
            } else {
                player.sendMessage(ChatColor.RED + "Sorry, you lost!");
                wager.setAmount(1);
            }
        }
    }

    private int getMultiplier(Material symbol) {
        return switch (symbol) {
            case DIAMOND -> 5;
            case GOLD_INGOT -> 4;
            case EMERALD -> 3;
            case IRON_INGOT -> 2;
            default -> 1;
        };
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

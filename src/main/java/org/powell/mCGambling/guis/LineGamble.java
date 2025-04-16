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

public class LineGamble implements Listener {
    private MCGambling main;
    private final GuiApi gui;
    private final int[] middleRow = {19, 20, 21, 22, 23, 24, 25};
    private String selectedColor = null; // Store the selected color
    private String title = ChatColor.DARK_AQUA + "MCGambling";
    private Inventory inv;

    public LineGamble(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
        this.inv = main.getGuiApi().createGui(null, 45, title);
    }

    public void openGUI(Player player) {


        //CLOSE
        ItemStack closebutton = new ItemStack(Material.BARRIER);
        gui.setItemName(closebutton, ChatColor.RED, "Close");
        gui.setItemLore(closebutton, null, "");
        gui.setItem(inv, closebutton, 0);

        //WAGER ARROW
        ItemStack arrow = new ItemStack(main.getGuiApi().getHead("76ebaa41d1d405eb6b60845bb9ac724af70e85eac8a96a5544b9e23ad6c96c62"));
        gui.setItemName(arrow, ChatColor.GRAY, "Put Your Wager Where The Arrow Points");
        gui.setItemLore(arrow, null, "");
        gui.setItem(inv, arrow, 37);

        //POT ARROW
        ItemStack parrow = new ItemStack(main.getGuiApi().getHead("8399e5da82ef7765fd5e472f3147ed118d981887730ea7bb80d7a1bed98d5ba"));
        gui.setItemName(parrow, ChatColor.GRAY, "That is the red");
        gui.setItemLore(parrow, null, "");
        gui.setItem(inv, parrow, 7);

        //POT
        ItemStack pot = new ItemStack(Material.DIAMOND, 32);
        gui.setItemName(pot, ChatColor.BLUE, "Pot");
        gui.setItemLore(pot, null, "");
        gui.setItem(inv, pot, 8);

        //WAGER
        ItemStack wager = new ItemStack(Material.DIAMOND, 1);
        gui.setItem(inv, wager, 36);

        //RED
        ItemStack red = new ItemStack(Material.RED_CONCRETE);
        gui.setItemName(red, ChatColor.RED, "Choose Red");
        gui.setItemLore(red, null, "");
        gui.setItem(inv, red, 39);

        //BLACK
        ItemStack black = new ItemStack(Material.BLACK_CONCRETE);
        gui.setItemName(black, ChatColor.BLACK, "Choose Black");
        gui.setItemLore(black, null, "");
        gui.setItem(inv, black, 41);

        //POINTING ARROW
        ItemStack pd = new ItemStack(main.getGuiApi().getHead("1cb8be16d40c25ace64e09f6086d408ebc3d545cfb2990c5b6c25dabcedeacc"));
        gui.setItemName(pd, ChatColor.GOLD, "_ _ _");
        gui.setItemLore(pd, null, "");
        gui.setItem(inv, pd, 13);

        //POINTING ARROW
        ItemStack pu = new ItemStack(main.getGuiApi().getHead("45c588b9ec0a08a37e01a809ed0903cc34c3e3f176dc92230417da93b948f148"));
        gui.setItemName(pu, ChatColor.GOLD, "_ _ _");
        gui.setItemLore(pu, null, "");
        gui.setItem(inv, pu, 31);

        //START
        ItemStack start = new ItemStack(main.getGuiApi().getHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756"));
        gui.setItemName(start, ChatColor.GOLD, "Start The Wheel");
        gui.setItemLore(start, null, "");
        gui.setItem(inv, start, 44);

        //FRAME
        for(int i : new int[]{9, 10, 11, 12, 14, 15, 16, 17, 18, 26, 27, 28, 29, 30, 32, 33, 34, 35}) {
            gui.setFrames(inv, i);
        }

        // Set up initial middle row items
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        gui.setItemName(redPane, ChatColor.RED, "RED");
        gui.setItemName(blackPane, ChatColor.DARK_GRAY, "GRAY");

        for (int i = 0; i < middleRow.length; i++) {
            gui.setItem(inv, (i % 2 == 0) ? redPane.clone() : blackPane.clone(), middleRow[i]);
        }

        player.openInventory(inv);

    }

    private String getTitle() { return title; }

    private void startSpinAnimation(Player player, Inventory inv) {
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40; // Duration of animation

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    this.cancel();
                    // Check if the middle slot matches the selected color
                    if (selectedColor != null) {
                        ItemStack middleItem = inv.getItem(middleRow[3]); // Slot 22
                        boolean isWinner = false;
                        
                        if (middleItem != null) {
                            if (selectedColor.equals("RED") && middleItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                                isWinner = true;
                            } else if (selectedColor.equals("BLACK") && middleItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                                isWinner = true;
                            }
                        }
                        
                        if (isWinner) {
                            player.sendMessage(ChatColor.GREEN + "Congratulations! You won!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Sorry, you lost!");
                        }
                    }
                    return;
                }

                // Shift items one slot to the left
                ItemStack temp = inv.getItem(middleRow[0]);
                for (int i = 0; i < middleRow.length - 1; i++) {
                    inv.setItem(middleRow[i], inv.getItem(middleRow[i + 1]));
                }
                inv.setItem(middleRow[middleRow.length - 1], temp);

                player.updateInventory();
                ticks++;
            }
        }.runTaskTimer(main, 0L, 2L); // Run every 2 ticks (0.1 seconds)

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() != null && e.getView().getTitle().equals(getTitle())) {
            e.setCancelled(true);
            switch (e.getSlot()) {
                case 0:
                    player.closeInventory();
                    break;
                case 39: // Red selection
                    selectedColor = "RED";
                    player.sendMessage(ChatColor.RED + "You selected RED!");
                    break;
                case 41: // Black selection
                    selectedColor = "BLACK";
                    player.sendMessage(ChatColor.DARK_GRAY + "You selected BLACK!");
                    break;
                case 44:
                    if (selectedColor == null) {
                        player.sendMessage(ChatColor.RED + "Please select a color first!");
                        return;
                    }
                    startSpinAnimation(player, inv);
                    break;
                default:
                    if (e.getSlot() != 36) { // Allow wager slot to be modified
                        e.setCancelled(true);
                    }
                    break;
            }
        }
    }
}

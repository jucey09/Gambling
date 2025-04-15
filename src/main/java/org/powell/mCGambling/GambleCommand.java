package org.powell.mCGambling;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.powell.guiApi.GuiApi;

public class GambleCommand implements CommandExecutor {
    private MCGambling main;
    private GuiApi gui;
    public GambleCommand(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            String title = ChatColor.DARK_AQUA + "MCGambling";
            Inventory inv = main.getGuiApi().createGui(null, 45, title);

            ItemStack closebutton = new ItemStack(Material.BARRIER);
            gui.setItemName(closebutton, ChatColor.RED, "Close");
            gui.setItemLore(closebutton, null, "");
            gui.setItem(inv, closebutton, 0);

            ItemStack arrow = new ItemStack(main.getGuiApi().getHead("76ebaa41d1d405eb6b60845bb9ac724af70e85eac8a96a5544b9e23ad6c96c62"));
            gui.setItemName(arrow, ChatColor.GOLD, "Put Your Wager Where The Arrow Points");
            gui.setItemLore(arrow, null, "");
            gui.setItem(inv, arrow, 38);

            //POT
            ItemStack pot = new ItemStack(Material.DIAMOND);
            gui.setItemName(pot, ChatColor.BLUE, "Close");
            gui.setItemLore(pot, null, "");
            gui.setItem(inv, pot, 37);

            player.openInventory(inv);
        }
        return false;
    }
}

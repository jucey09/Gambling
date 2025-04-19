package org.powell.mCGambling.guis;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.MCGambling;

public class ProjectLauncher implements Listener {
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.DARK_AQUA + "My Other Projects";
    private final Inventory inv;

    private static final int[] PROJECT_SLOTS = {10, 12, 14, 16};

    public ProjectLauncher(MCGambling main, GuiApi gui) {
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
        gui.setItem(inv, closeButton, 22);

        String[] projectNames = {
            "Admin Control Center",
            "Craftify",
            "Rankify",
            "Simplified Resource Pack"
        };
        for (int i = 0; i < PROJECT_SLOTS.length; i++) {
            ItemStack projectItem = new ItemStack(Material.BOOK);
            gui.setItemName(projectItem, ChatColor.GOLD, projectNames[i]);
            gui.setItemLore(projectItem, ChatColor.GRAY, "Click to launch " + projectNames[i]);
            gui.setItem(inv, projectItem, PROJECT_SLOTS[i]);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == 22) {
            player.closeInventory();
            return;
        }
        String[] links = {
            "https://modrinth.com/plugin/admin-control-center",
            "https://modrinth.com/plugin/craftify-ccg",
            "https://modrinth.com/plugin/rankify",
            "https://modrinth.com/resourcepack/simplified-resource-pack"
        };
        String[] names = {
            "Admin Control Center",
            "Craftify",
            "Rankify",
            "Simplified Resource Pack"
        };
        for (int i = 0; i < PROJECT_SLOTS.length; i++) {
            if (slot == PROJECT_SLOTS[i]) {
                net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent("Click here to open " + names[i]);
                message.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, links[i]
                ));
                player.spigot().sendMessage(message);
                player.sendMessage(ChatColor.GRAY + "(If the link doesn't open, check your chat settings.)");
                return;
            }
        }
    }
}

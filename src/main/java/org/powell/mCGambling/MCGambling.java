package org.powell.mCGambling;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.powell.guiApi.GuiApi;

public final class MCGambling extends JavaPlugin {
    GuiApi guiApi;

    @Override
    public void onEnable() {
        // Plugin startup logic
        guiApi = (GuiApi) getServer().getPluginManager().getPlugin("GuiApi");

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "MCGambling Enabled!");

        getCommand("mcg").setExecutor(new GambleCommand(this, guiApi));
    }
    public GuiApi getGuiApi() { return guiApi; }
}
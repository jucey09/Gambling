package org.powell.mCGambling;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.commands.GambleCommand;
import org.powell.mCGambling.guis.LineGamble;

public final class MCGambling extends JavaPlugin {
    GuiApi guiApi;

    private LineGamble spinner;

    @Override
    public void onEnable() {
        // Plugin startup logic
        guiApi = (GuiApi) getServer().getPluginManager().getPlugin("GuiApi");

        spinner = new LineGamble(this, guiApi);

        getServer().getPluginManager().registerEvents(spinner, this);

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "MCGambling Enabled!");

        getCommand("mcg").setExecutor(new GambleCommand(this));
    }
    public GuiApi getGuiApi() { return guiApi; }
    public LineGamble getSpinner() { return spinner; }
}
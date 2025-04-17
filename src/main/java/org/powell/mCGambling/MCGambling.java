package org.powell.mCGambling;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.commands.GambleCommand;
import org.powell.mCGambling.guis.LineGamble;
import org.powell.mCGambling.guis.SlotMachine;
import org.powell.mCGambling.guis.RussianRoulett;

public final class MCGambling extends JavaPlugin {
    private GuiApi guiApi;
    private LineGamble lineGamble;
    private SlotMachine slotMachine;
    private RussianRoulett russianRoulett;

    @Override
    public void onEnable() {
        guiApi = (GuiApi) getServer().getPluginManager().getPlugin("GuiApi");

        lineGamble = new LineGamble(this, guiApi);
        slotMachine = new SlotMachine(this, guiApi);
        russianRoulett = new RussianRoulett(this, guiApi);

        getServer().getPluginManager().registerEvents(lineGamble, this);
        getServer().getPluginManager().registerEvents(slotMachine, this);
        getServer().getPluginManager().registerEvents(russianRoulett, this);

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "MCGambling Enabled!");

        getCommand("mcg").setExecutor(new GambleCommand(this));
    }

    public GuiApi getGuiApi() { return guiApi; }

    public LineGamble getLineGamble() { return lineGamble; }

    public SlotMachine getSlotMachine() { return slotMachine; }

    public RussianRoulett getRussianRoulett() { return russianRoulett; }
}
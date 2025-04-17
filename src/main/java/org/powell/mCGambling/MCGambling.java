package org.powell.mCGambling;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.commands.GambleCommand;
import org.powell.mCGambling.guis.LineGamble;
import org.powell.mCGambling.guis.SlotMachine;
import org.powell.mCGambling.guis.RussianRoulett;
import org.powell.mCGambling.guis.DiceGame;
import org.powell.mCGambling.guis.CoinTower;
import org.powell.mCGambling.guis.TreasureHunt;
import org.powell.mCGambling.guis.WheelOfFortune;

public final class MCGambling extends JavaPlugin {
    private GuiApi guiApi;
    private LineGamble lineGamble;
    private SlotMachine slotMachine;
    private RussianRoulett russianRoulett;
    private DiceGame diceGame;
    private CoinTower coinTower;
    private TreasureHunt treasureHunt;
    private WheelOfFortune wheelOfFortune;

    @Override
    public void onEnable() {
        guiApi = (GuiApi) getServer().getPluginManager().getPlugin("GuiApi");

        lineGamble = new LineGamble(this, guiApi);
        slotMachine = new SlotMachine(this, guiApi);
        russianRoulett = new RussianRoulett(this, guiApi);
        diceGame = new DiceGame(this, guiApi);
        coinTower = new CoinTower(this, guiApi);
        treasureHunt = new TreasureHunt(this, guiApi);
        wheelOfFortune = new WheelOfFortune(this, guiApi);

        getServer().getPluginManager().registerEvents(lineGamble, this);
        getServer().getPluginManager().registerEvents(slotMachine, this);
        getServer().getPluginManager().registerEvents(russianRoulett, this);
        getServer().getPluginManager().registerEvents(diceGame, this);
        getServer().getPluginManager().registerEvents(coinTower, this);
        getServer().getPluginManager().registerEvents(treasureHunt, this);
        getServer().getPluginManager().registerEvents(wheelOfFortune, this);

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "MCGambling Enabled!");

        getCommand("mcg").setExecutor(new GambleCommand(this));
    }

    public GuiApi getGuiApi() { return guiApi; }

    public LineGamble getLineGamble() { return lineGamble; }

    public SlotMachine getSlotMachine() { return slotMachine; }

    public RussianRoulett getRussianRoulett() { return russianRoulett; }

    public DiceGame getDiceGame() { return diceGame; }

    public CoinTower getCoinTower() { return coinTower; }

    public TreasureHunt getTreasureHunt() { return treasureHunt; }

    public WheelOfFortune getWheelOfFortune() { return wheelOfFortune; }
}
package org.powell.mCGambling;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.commands.GambleCommand;
import org.powell.mCGambling.guis.*;

public final class MCGambling extends JavaPlugin {
    private GuiApi guiApi;
    private LineGamble lineGamble;
    private SlotMachine slotMachine;
    private RussianRoulett russianRoulett;
    private DiceGame diceGame;
    private CoinTower coinTower;
    private TreasureHunt treasureHunt;
    private WheelOfFortune wheelOfFortune;
    private Blackjack blackjack;
    private HighLow highLow;
    private CrashGame crashGame;
    private Poker poker;

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
        blackjack = new Blackjack(this, guiApi);
        highLow = new HighLow(this, guiApi);
        crashGame = new CrashGame(this, guiApi);
        poker = new Poker(this, guiApi);

        getServer().getPluginManager().registerEvents(lineGamble, this);
        getServer().getPluginManager().registerEvents(slotMachine, this);
        getServer().getPluginManager().registerEvents(russianRoulett, this);
        getServer().getPluginManager().registerEvents(diceGame, this);
        getServer().getPluginManager().registerEvents(coinTower, this);
        getServer().getPluginManager().registerEvents(treasureHunt, this);
        getServer().getPluginManager().registerEvents(wheelOfFortune, this);
        getServer().getPluginManager().registerEvents(blackjack, this);
        getServer().getPluginManager().registerEvents(highLow, this);
        getServer().getPluginManager().registerEvents(crashGame, this);
        getServer().getPluginManager().registerEvents(poker, this);

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "MCGambling Enabled!");

        getCommand("mcg").setExecutor(new GambleCommand(this));
    }

    public GuiApi getGuiApi() {
        return guiApi;
    }

    public LineGamble getLineGamble() {
        return lineGamble;
    }

    public SlotMachine getSlotMachine() {
        return slotMachine;
    }

    public RussianRoulett getRussianRoulett() {
        return russianRoulett;
    }

    public DiceGame getDiceGame() {
        return diceGame;
    }

    public CoinTower getCoinTower() {
        return coinTower;
    }

    public TreasureHunt getTreasureHunt() {
        return treasureHunt;
    }

    public WheelOfFortune getWheelOfFortune() {
        return wheelOfFortune;
    }

    public Blackjack getBlackjack() {
        return blackjack;
    }

    public HighLow getHighLow() {
        return highLow;
    }

    public CrashGame getCrashGame() {
        return crashGame;
    }

    public Poker getPoker() {
        return poker;
    }
}
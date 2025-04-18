package org.powell.mCGambling.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.powell.guiApi.GuiApi;
import org.powell.mCGambling.MCGambling;

import java.util.*;

public class Poker implements Listener {
    private static final Map<HandRank, Integer> PAYOUT_MULTIPLIERS = new HashMap<>() {{
        put(HandRank.ROYAL_FLUSH, 100);
        put(HandRank.STRAIGHT_FLUSH, 50);
        put(HandRank.FOUR_OF_A_KIND, 25);
        put(HandRank.FULL_HOUSE, 9);
        put(HandRank.FLUSH, 6);
        put(HandRank.STRAIGHT, 4);
        put(HandRank.THREE_OF_A_KIND, 3);
        put(HandRank.TWO_PAIR, 2);
        put(HandRank.ONE_PAIR, 1);
        put(HandRank.HIGH_CARD, 0);
    }};
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.GOLD + "Texas Hold'em Poker";
    private final Inventory inv;
    private final List<Integer> playerCardSlots = Arrays.asList(48, 49);
    private final List<Integer> communityCardSlots = Arrays.asList(21, 22, 23, 24, 25);
    private boolean isPlaying = false;
    private List<Card> deck;
    private List<Card> playerHand;
    private List<Card> communityCards;
    private List<Card> bot1Hand;
    private List<Card> bot2Hand;
    private int gameStage = 0;
    private int pot = 0;
    private int currentBet = 0;
    private int playerBet = 0;
    private boolean playerFolded = false;
    private boolean bot1Folded = false;
    private boolean bot2Folded = false;
    private String playerName;

    public Poker(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
        this.inv = main.getGuiApi().createGui(null, 54, title);
        setupGUI();
    }

    public void openGUI(Player player) {
        this.playerName = player.getName();
        player.openInventory(inv);
    }

    private void setupGUI() {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, null);
        }

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        gui.setItemName(closeButton, ChatColor.RED, "Close");
        gui.setItem(inv, closeButton, 0);

        ItemStack wager = new ItemStack(Material.DIAMOND);
        gui.setItem(inv, wager, 4);

        ItemStack dealButton = new ItemStack(Material.LIME_CONCRETE);
        gui.setItemName(dealButton, ChatColor.GREEN, "DEAL");
        gui.setItemLore(dealButton, ChatColor.GRAY, "Place your bet and click to start!");
        gui.setItem(inv, dealButton, 8);

        createActionButtons();

        hideActionButtons();

        displayPaytable();

        clearBotCards();

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                gui.setFrames(inv, i);
            }
        }
    }

    private void createActionButtons() {
        ItemStack checkButton = new ItemStack(Material.LIGHT_BLUE_CONCRETE);
        gui.setItemName(checkButton, ChatColor.AQUA, "CHECK/CALL");
        inv.setItem(45, checkButton);

        ItemStack raiseButton = new ItemStack(Material.YELLOW_CONCRETE);
        gui.setItemName(raiseButton, ChatColor.YELLOW, "RAISE");
        inv.setItem(46, raiseButton);

        ItemStack foldButton = new ItemStack(Material.RED_CONCRETE);
        gui.setItemName(foldButton, ChatColor.RED, "FOLD");
        inv.setItem(47, foldButton);
    }

    private void showActionButtons() {
        createActionButtons();
    }

    private void clearBotCards() {
        for (int i = 0; i < 2; i++) {
            ItemStack frame = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            gui.setItemName(frame, ChatColor.AQUA, "Bot1");
            gui.setItem(inv, frame, 10 + i);
        }

        for (int i = 0; i < 2; i++) {
            ItemStack frame = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            gui.setItemName(frame, ChatColor.LIGHT_PURPLE, "Bot2");
            gui.setItem(inv, frame, 28 + i);
        }

        for (int slot : communityCardSlots) {
            ItemStack frame = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            gui.setItemName(frame, ChatColor.GREEN, "Community");
            gui.setItem(inv, frame, slot);
        }

        for (int slot : playerCardSlots) {
            ItemStack frame = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            gui.setItemName(frame, ChatColor.YELLOW, "Your Cards");
            gui.setItem(inv, frame, slot);
        }
    }

    private void displayPaytable() {
        ItemStack paytable = new ItemStack(Material.BOOK);
        gui.setItemName(paytable, ChatColor.GOLD, "Paytable");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "Royal Flush: 100x");
        lore.add(ChatColor.WHITE + "Straight Flush: 50x");
        lore.add(ChatColor.WHITE + "Four of a Kind: 25x");
        lore.add(ChatColor.WHITE + "Full House: 9x");
        lore.add(ChatColor.WHITE + "Flush: 6x");
        lore.add(ChatColor.WHITE + "Straight: 4x");
        lore.add(ChatColor.WHITE + "Three of a Kind: 3x");
        lore.add(ChatColor.WHITE + "Two Pair: 2x");
        lore.add(ChatColor.WHITE + "One Pair: 1x");
        gui.setItemLore(paytable, null, lore.toString());
        gui.setItem(inv, paytable, 2);
    }

    private void initializeDeck() {
        deck = new ArrayList<>();
        String[] suits = {"♠", "♥", "♦", "♣"};
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(rank, suit));
            }
        }
    }

    private Card drawCard() {
        if (deck.isEmpty()) {
            initializeDeck();
        }
        return deck.remove(new Random().nextInt(deck.size()));
    }

    private void startGame(Player player) {
        if (isPlaying) return;

        ItemStack wager = inv.getItem(4);
        if (wager == null || wager.getType() != Material.DIAMOND) {
            player.sendMessage(ChatColor.RED + "Please place a diamond wager first!");
            return;
        }

        clearBotCards();

        initializeDeck();
        playerHand = new ArrayList<>();
        communityCards = new ArrayList<>();
        bot1Hand = new ArrayList<>();
        bot2Hand = new ArrayList<>();
        isPlaying = true;
        gameStage = 0;
        pot = wager.getAmount();
        currentBet = wager.getAmount();
        playerBet = wager.getAmount();
        playerFolded = false;
        bot1Folded = false;
        bot2Folded = false;
        playerName = player.getName();

        updatePotDisplay(player);

        for (int i = 0; i < 2; i++) {
            playerHand.add(drawCard());
            bot1Hand.add(drawCard());
            bot2Hand.add(drawCard());
        }

        botDecision(player, bot1Hand, 1);
        botDecision(player, bot2Hand, 2);

        updateCards();
        ItemStack dealButton = inv.getItem(8);
        gui.setItemName(dealButton, ChatColor.GREEN, "DEAL FLOP");
        gui.setItemLore(dealButton, ChatColor.GRAY, "Click to deal the flop!");

        showActionButtons();
    }

    private void updatePotDisplay(Player player) {
        ItemStack potDisplay = new ItemStack(Material.GOLD_INGOT);
        gui.setItemName(potDisplay, ChatColor.GOLD, "Pot: " + pot + " diamonds");
        gui.setItem(inv, potDisplay, 3);

        ItemStack playerInfo = new ItemStack(Material.PLAYER_HEAD);
        gui.setItemName(playerInfo, ChatColor.GREEN, player.getName());
        gui.setItemLore(playerInfo, ChatColor.GRAY, "Bet: " + playerBet + " diamonds");
        gui.setItem(inv, playerInfo, 50);

        ItemStack bot1Info = new ItemStack(Material.SKELETON_SKULL);
        gui.setItemName(bot1Info, ChatColor.AQUA, "Bot1");
        gui.setItemLore(bot1Info, ChatColor.GRAY, bot1Folded ? "FOLDED" : "In game");
        gui.setItem(inv, bot1Info, 12);

        ItemStack bot2Info = new ItemStack(Material.ZOMBIE_HEAD);
        gui.setItemName(bot2Info, ChatColor.LIGHT_PURPLE, "Bot2");
        gui.setItemLore(bot2Info, ChatColor.GRAY, bot2Folded ? "FOLDED" : "In game");
        gui.setItem(inv, bot2Info, 14);
    }

    private void botDecision(Player player, List<Card> botHand, int botNumber) {
        boolean isFolded = (botNumber == 1) ? bot1Folded : bot2Folded;
        if (isFolded) return;

        int handStrength = 0;
        boolean hasPair = botHand.get(0).rank.equals(botHand.get(1).rank);

        for (Card card : botHand) {
            handStrength += card.getRankValue();
        }

        List<Card> allCards = new ArrayList<>(botHand);
        if (!communityCards.isEmpty()) {
            allCards.addAll(communityCards);

            Map<String, Integer> rankCounts = new HashMap<>();
            for (Card c : allCards) {
                rankCounts.put(c.rank, rankCounts.getOrDefault(c.rank, 0) + 1);
            }

            if (rankCounts.containsValue(2)) handStrength += 5;
            if (rankCounts.containsValue(3)) handStrength += 10;
            if (rankCounts.containsValue(4)) handStrength += 20;
        }

        if (hasPair) handStrength += 10;

        Random random = new Random();
        String botName = (botNumber == 1) ? "Bot1" : "Bot2";

        if (handStrength < 10 && random.nextDouble() < 0.7) {
            if (botNumber == 1) bot1Folded = true;
            else bot2Folded = true;
            player.sendMessage(ChatColor.GRAY + botName + " folds.");
        } else if (handStrength < 20 || random.nextDouble() < 0.6) {
            int amountToCall = currentBet - playerBet;
            pot += amountToCall;
            player.sendMessage(ChatColor.GRAY + botName + " calls.");
        } else {
            int raiseAmount = 1 + random.nextInt(2);
            currentBet += raiseAmount;
            pot += raiseAmount;
            player.sendMessage(ChatColor.GRAY + botName + " raises by " + raiseAmount + " diamonds.");
        }

        updatePotDisplay(player);
    }

    private void updateCards() {
        for (int i = 0; i < playerCardSlots.size(); i++) {
            if (i < playerHand.size()) {
                Card card = playerHand.get(i);
                ItemStack cardItem = new ItemStack(Material.PAPER);
                gui.setItemName(cardItem, ChatColor.WHITE, card.toString());
                gui.setItem(inv, cardItem, playerCardSlots.get(i));
            } else {
                inv.setItem(playerCardSlots.get(i), null);
            }
        }
        for (int i = 0; i < communityCardSlots.size(); i++) {
            if (i < communityCards.size()) {
                Card card = communityCards.get(i);
                ItemStack cardItem = new ItemStack(Material.PAPER);
                gui.setItemName(cardItem, ChatColor.WHITE, card.toString());
                gui.setItem(inv, cardItem, communityCardSlots.get(i));
            } else {
                inv.setItem(communityCardSlots.get(i), null);
            }
        }
    }

    private void dealFlop() {
        for (int i = 0; i < 3; i++) {
            communityCards.add(drawCard());
        }
        updateCards();
        gameStage = 1;

        botDecision(Bukkit.getPlayer(playerName), bot1Hand, 1);
        botDecision(Bukkit.getPlayer(playerName), bot2Hand, 2);

        ItemStack dealButton = inv.getItem(8);
        gui.setItemName(dealButton, ChatColor.GREEN, "DEAL TURN");
        gui.setItemLore(dealButton, ChatColor.GRAY, "Click to deal the turn card!");
    }

    private void dealTurn() {
        communityCards.add(drawCard());
        updateCards();
        gameStage = 2;

        botDecision(Bukkit.getPlayer(playerName), bot1Hand, 1);
        botDecision(Bukkit.getPlayer(playerName), bot2Hand, 2);

        ItemStack dealButton = inv.getItem(8);
        gui.setItemName(dealButton, ChatColor.GREEN, "DEAL RIVER");
        gui.setItemLore(dealButton, ChatColor.GRAY, "Click to deal the river card!");
    }

    private void dealRiver() {
        communityCards.add(drawCard());
        updateCards();
        gameStage = 3;

        botDecision(Bukkit.getPlayer(playerName), bot1Hand, 1);
        botDecision(Bukkit.getPlayer(playerName), bot2Hand, 2);

        ItemStack dealButton = inv.getItem(8);
        gui.setItemName(dealButton, ChatColor.GREEN, "SHOWDOWN");
        gui.setItemLore(dealButton, ChatColor.GRAY, "Click to see who wins!");
    }

    private void showdown(Player player) {
        showAllCards();

        if (playerFolded) {
            lose(player, "You folded!");
            return;
        }

        if (bot1Folded && bot2Folded) {
            win(player, HandRank.HIGH_CARD, 1);
            return;
        }

        HandRank playerRank = evaluateHand(playerHand, communityCards);
        HandRank bot1Rank = bot1Folded ? HandRank.HIGH_CARD : evaluateHand(bot1Hand, communityCards);
        HandRank bot2Rank = bot2Folded ? HandRank.HIGH_CARD : evaluateHand(bot2Hand, communityCards);

        HandRank bestRank = playerRank;
        String winner = player.getName();

        if (!bot1Folded && bot1Rank.ordinal() < bestRank.ordinal()) {
            bestRank = bot1Rank;
            winner = "Bot1";
        }

        if (!bot2Folded && bot2Rank.ordinal() < bestRank.ordinal()) {
            bestRank = bot2Rank;
            winner = "Bot2";
        }

        updateCards();
        if (winner.equals(player.getName())) {
            int multiplier = PAYOUT_MULTIPLIERS.get(playerRank);
            win(player, playerRank, multiplier);
            player.sendMessage(ChatColor.GOLD + "You win with " + playerRank + "!");
        } else {
            lose(player, winner + " wins with " + bestRank + "!");
        }

        hideActionButtons();
        ItemStack dealButton = inv.getItem(8);
        gui.setItemName(dealButton, ChatColor.GREEN, "DEAL");
        gui.setItemLore(dealButton, ChatColor.GRAY, "Place your bet and click to start!");
    }

    private void showAllCards() {
        for (int i = 0; i < bot1Hand.size(); i++) {
            if (!bot1Folded) {
                ItemStack cardItem = new ItemStack(Material.PAPER);
                gui.setItemName(cardItem, ChatColor.AQUA, bot1Hand.get(i).toString());
                gui.setItemLore(cardItem, ChatColor.GRAY, "Bot1");
                gui.setItem(inv, cardItem, 10 + i);
            }
        }

        for (int i = 0; i < bot2Hand.size(); i++) {
            if (!bot2Folded) {
                ItemStack cardItem = new ItemStack(Material.PAPER);
                gui.setItemName(cardItem, ChatColor.LIGHT_PURPLE, bot2Hand.get(i).toString());
                gui.setItemLore(cardItem, ChatColor.GRAY, "Bot2");
                gui.setItem(inv, cardItem, 28 + i);
            }
        }
    }

    private HandRank evaluateHand(List<Card> hand, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(hand);
        allCards.addAll(communityCards);
        allCards.sort((a, b) -> Integer.compare(a.getRankValue(), b.getRankValue()));
        boolean isFlush = false;
        for (String suit : new String[]{"♠", "♥", "♦", "♣"}) {
            int count = 0;
            for (Card c : allCards) {
                if (c.suit.equals(suit)) count++;
            }
            if (count >= 5) {
                isFlush = true;
                break;
            }
        }
        boolean isStraight = false;
        for (int i = 0; i < allCards.size() - 4; i++) {
            if (allCards.get(i + 4).getRankValue() == allCards.get(i).getRankValue() + 4) {
                isStraight = true;
                break;
            }
        }
        if (isFlush && isStraight) {
            if (allCards.get(allCards.size() - 1).getRankValue() == 14) return HandRank.ROYAL_FLUSH;
            return HandRank.STRAIGHT_FLUSH;
        }
        Map<String, Integer> rankCounts = new HashMap<>();
        for (Card c : allCards) rankCounts.put(c.rank, rankCounts.getOrDefault(c.rank, 0) + 1);
        if (rankCounts.containsValue(4)) return HandRank.FOUR_OF_A_KIND;
        if (rankCounts.containsValue(3) && rankCounts.containsValue(2)) return HandRank.FULL_HOUSE;
        if (isFlush) return HandRank.FLUSH;
        if (isStraight) return HandRank.STRAIGHT;
        if (rankCounts.containsValue(3)) return HandRank.THREE_OF_A_KIND;
        int pairCount = 0;
        for (int v : rankCounts.values()) if (v == 2) pairCount++;
        if (pairCount == 2) return HandRank.TWO_PAIR;
        if (pairCount == 1) return HandRank.ONE_PAIR;
        return HandRank.HIGH_CARD;
    }

    private void win(Player player, HandRank rank, int multiplier) {
        ItemStack wager = inv.getItem(4);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            int winAmount = wager.getAmount() * multiplier;
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + rank.toString() + "! You won " + winAmount + " diamonds!");
            inv.setItem(4, null);
        }
        isPlaying = false;
    }

    private void lose(Player player, String message) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        player.sendMessage(ChatColor.RED + message);
        inv.setItem(4, null);
        isPlaying = false;
    }

    private void hideActionButtons() {
        inv.setItem(45, null);
        inv.setItem(46, null);
        inv.setItem(47, null);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() == null || !e.getInventory().equals(inv)) return;
        Player player = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null) return;

        if (e.getSlot() == 4 && ((e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.DIAMOND) || (e.getCursor() != null && e.getCursor().getType() == Material.DIAMOND))) {
            e.setCancelled(false);
            return;
        }

        e.setCancelled(true);

        if (e.getSlot() == 0) {
            player.closeInventory();
            return;
        }

        if (e.getSlot() == 8) {
            if (!isPlaying) {
                startGame(player);
            } else if (gameStage == 0) {
                dealFlop();
            } else if (gameStage == 1) {
                dealTurn();
            } else if (gameStage == 2) {
                dealRiver();
            } else if (gameStage == 3) {
                showdown(player);
            }
            return;
        }

        if (e.getSlot() == 45) {
            if (currentBet > playerBet) {
                int amountToCall = currentBet - playerBet;
                player.getInventory().removeItem(new ItemStack(Material.DIAMOND, amountToCall));
                playerBet += amountToCall;
                pot += amountToCall;
            }
            if (gameStage == 1) {
                dealTurn();
            } else if (gameStage == 2) {
                dealRiver();
            } else if (gameStage == 3) {
                showdown(player);
            }
            return;
        }

        if (e.getSlot() == 46) {
            int amountToRaise = 1;
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, amountToRaise));
            playerBet += amountToRaise;
            pot += amountToRaise;
            currentBet += amountToRaise;
            return;
        }

        if (e.getSlot() == 47) {
            playerFolded = true;
            hideActionButtons();
            if (bot1Folded && bot2Folded) {
                showdown(player);
            }
        }
    }

    private enum HandRank {
        ROYAL_FLUSH, STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE, FLUSH, STRAIGHT, THREE_OF_A_KIND, TWO_PAIR, ONE_PAIR, HIGH_CARD
    }

    private static class Card {
        private final String rank;
        private final String suit;

        public Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public int getRankValue() {
            return switch (rank) {
                case "A" -> 14;
                case "K" -> 13;
                case "Q" -> 12;
                case "J" -> 11;
                default -> Integer.parseInt(rank);
            };
        }

        @Override
        public String toString() {
            return rank + suit;
        }
    }
}

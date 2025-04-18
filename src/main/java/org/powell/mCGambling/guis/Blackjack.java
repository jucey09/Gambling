package org.powell.mCGambling.guis;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Blackjack implements Listener {
    private final GuiApi gui;
    private final MCGambling main;
    private final String title = ChatColor.GOLD + "Blackjack";
    private final Inventory inv;
    private final List<Integer> playerCardSlots = Arrays.asList(19, 20, 21, 22, 23);
    private final List<Integer> dealerCardSlots = Arrays.asList(10, 11, 12, 13, 14);
    private boolean isPlaying = false;
    private List<Card> deck;
    private List<Card> playerHand;
    private List<Card> dealerHand;
    private boolean playerStand = false;

    public Blackjack(MCGambling main, GuiApi gui) {
        this.main = main;
        this.gui = gui;
        this.inv = main.getGuiApi().createGui(null, 36, title);
        setupGUI();
    }

    public void openGUI(Player player) {
        player.openInventory(inv);
    }

    private void setupGUI() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        gui.setItemName(closeButton, ChatColor.RED, "Close");
        gui.setItem(inv, closeButton, 0);

        ItemStack wager = new ItemStack(Material.DIAMOND);
        gui.setItem(inv, wager, 31);

        ItemStack dealButton = new ItemStack(Material.LIME_CONCRETE);
        gui.setItemName(dealButton, ChatColor.GREEN, "DEAL");
        gui.setItemLore(dealButton, ChatColor.GRAY, "Place your bet and click to start!");
        gui.setItem(inv, dealButton, 25);

        ItemStack hitButton = new ItemStack(Material.EMERALD_BLOCK);
        gui.setItemName(hitButton, ChatColor.GREEN, "HIT");
        gui.setItemLore(hitButton, ChatColor.GRAY, "Draw another card");
        gui.setItem(inv, hitButton, 33);

        ItemStack standButton = new ItemStack(Material.REDSTONE_BLOCK);
        gui.setItemName(standButton, ChatColor.RED, "STAND");
        gui.setItemLore(standButton, ChatColor.GRAY, "Keep your current hand");
        gui.setItem(inv, standButton, 34);

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null && !playerCardSlots.contains(i) && !dealerCardSlots.contains(i)) {
                gui.setFrames(inv, i);
            }
        }
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

        ItemStack wager = inv.getItem(31);
        if (wager == null || wager.getType() != Material.DIAMOND) {
            player.sendMessage(ChatColor.RED + "Please place a diamond wager first!");
            return;
        }

        initializeDeck();
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();
        playerStand = false;
        isPlaying = true;

        playerHand.add(drawCard());
        dealerHand.add(drawCard());
        playerHand.add(drawCard());
        dealerHand.add(drawCard());

        updateCards();

        if (calculateHandValue(playerHand) == 21) {
            blackjack(player);
        }
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

        for (int i = 0; i < dealerCardSlots.size(); i++) {
            if (i < dealerHand.size()) {
                Card card = dealerHand.get(i);
                ItemStack cardItem = new ItemStack(Material.PAPER);
                if (i == 1 && !playerStand) {
                    gui.setItemName(cardItem, ChatColor.WHITE, "?");
                } else {
                    gui.setItemName(cardItem, ChatColor.WHITE, card.toString());
                }
                gui.setItem(inv, cardItem, dealerCardSlots.get(i));
            } else {
                inv.setItem(dealerCardSlots.get(i), null);
            }
        }
    }

    private int calculateHandValue(List<Card> hand) {
        int value = 0;
        int aces = 0;

        for (Card card : hand) {
            if (card.rank.equals("A")) {
                aces++;
            } else if (card.rank.equals("K") || card.rank.equals("Q") || card.rank.equals("J")) {
                value += 10;
            } else {
                value += Integer.parseInt(card.rank);
            }
        }

        for (int i = 0; i < aces; i++) {
            if (value + 11 <= 21) {
                value += 11;
            } else {
                value += 1;
            }
        }

        return value;
    }

    private void hit(Player player) {
        if (!isPlaying || playerStand) return;

        playerHand.add(drawCard());
        updateCards();

        int value = calculateHandValue(playerHand);
        if (value > 21) {
            bust(player);
        } else if (value == 21) {
            stand(player);
        }
    }

    private void stand(Player player) {
        if (!isPlaying || playerStand) return;

        playerStand = true;
        updateCards();

        while (calculateHandValue(dealerHand) < 17) {
            dealerHand.add(drawCard());
            updateCards();
        }

        checkWinner(player);
    }

    private void checkWinner(Player player) {
        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);

        if (dealerValue > 21 || playerValue > dealerValue) {
            win(player);
        } else if (dealerValue > playerValue) {
            lose(player, "Dealer wins with " + dealerValue + "!");
        } else {
            push(player);
        }
    }

    private void blackjack(Player player) {
        ItemStack wager = inv.getItem(31);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            int winAmount = wager.getAmount() * 3;
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + "BLACKJACK! You won " + winAmount + " diamonds!");
            inv.setItem(31, null);
        }
        isPlaying = false;
    }

    private void win(Player player) {
        ItemStack wager = inv.getItem(31);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            int winAmount = wager.getAmount() * 2;
            player.getInventory().addItem(new ItemStack(Material.DIAMOND, winAmount));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(ChatColor.GREEN + "You won " + winAmount + " diamonds!");
            inv.setItem(31, null);
        }
        isPlaying = false;
    }

    private void push(Player player) {
        ItemStack wager = inv.getItem(31);
        if (wager != null && wager.getType() == Material.DIAMOND) {
            player.getInventory().addItem(wager);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.sendMessage(ChatColor.YELLOW + "Push! Your wager has been returned.");
            inv.setItem(31, null);
        }
        isPlaying = false;
    }

    private void bust(Player player) {
        lose(player, "Bust! Your hand is over 21!");
    }

    private void lose(Player player, String message) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        player.sendMessage(ChatColor.RED + message);
        inv.setItem(31, null);
        isPlaying = false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(title)) return;

        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory() != null && e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        e.setCancelled(true);

        if (e.getCurrentItem() == null && e.getCursor() == null) return;

        if (e.getSlot() == 31 &&
            ((e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.DIAMOND) ||
             (e.getCursor() != null && e.getCursor().getType() == Material.DIAMOND))) {
            e.setCancelled(false);
            return;
        }

        switch (e.getSlot()) {
            case 0 -> player.closeInventory();
            case 25 -> {
                if (!isPlaying) {
                    startGame(player);
                }
            }
            case 33 -> {
                if (isPlaying && !playerStand) {
                    hit(player);
                }
            }
            case 34 -> {
                if (isPlaying && !playerStand) {
                    stand(player);
                }
            }
        }
    }

    private static class Card {
        private final String rank;
        private final String suit;

        public Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        @Override
        public String toString() {
            return rank + suit;
        }
    }
}

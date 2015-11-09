package com.elena;

import java.util.*;

/**
 * This program plays the Eight Crazy card game imitating 2 players.
 *
 * Rules:
 *
 * 1. If the top card of the discard pile is not an eight,
 * player can play any card which matches the rank or suit of the previous card.
 *
 * 2. An eight can played on any card, and the player of the eight must nominate a suit.
 *
 * 3. If an eight is on top of the pile, player can play either another eight or
 * card of the suit nominated by the person who played the eight.
 *
 *
 * The game stops if any occurred:
 * 1. One player played all his/her cards. This player is a winner.
 *
 * 2. None of the players can play the key/top card and card stock is empty.
 * In this case, players score penalty points according to the cards they have
 * 50 points for an eight, 10 for a picture, and spot cards at face value
 */
public class Main {

    public static final int CARD_TO_EACH_PLAYER = 7;
    public static final int TOTAL_PLAYERS = 2;

    public static void main(String[] args) {

        System.out.println("This program plays Eight Crazy card game with 2 players.");
        play();   // Play the game and get the winner

    }  // end main()

    /**
     * This method implements  the Eight Crazy game strategy
     */
    private static void play() {

        int round = 0;
        int playedCard;  // position of the cards in the player's set
        Card keyCard; // the card that players has to bit

        // create objects
        Deck deck = new Deck();
        Hand player1 = new Hand();
        Hand player2 = new Hand();
        Hand playerX = new Hand();  // temp object
        Hand stock = new Hand ();  //undealt stock
        Hand dpile = new Hand(); //discard pile

        System.out.println("************************ Start the game ************************");
        deck.shuffle();
        startGame (player1, player2, deck, stock, dpile); // set players with initial set of cards

        String [] players = new String[TOTAL_PLAYERS];   // array with the name of players: "player1", "player2"
        for (int i=0; i<TOTAL_PLAYERS; i++)
            players[i] = "player"+ Integer.toString(i+1);

        boolean gameContinue = true;
        while (gameContinue) {

            for (int i=0; i<TOTAL_PLAYERS; i++) {

                round = round + 1;
                System.out.println ();
                System.out.println("======================================================");
                System.out.println("Round: " + round);
                System.out.println("Card stock size: " + stock.getCardCount());
                System.out.println("Discard pile size: " + dpile.getCardCount());
                printPlayerCards (player1, 1);
                printPlayerCards (player2, 2);
                System.out.println(".........................................");

                keyCard = getKeyCard (dpile); // identify the key card

                /* define who is playing */
                if (i==0) playerX = player1;
                else if (i==1) playerX = player2;
                System.out.println(players[i].toUpperCase() + "'s turn to play.");

                // check if player has cards in hand. if no cards left, player is a winner
                if (isWinner (playerX, i+1)) {
                    gameContinue = false;
                    break;
                }
                else {
                    playedCard = playTheCard (playerX, keyCard, stock, dpile); // returns a number of card in player's hand (if returns -1, no card to play)

                    if (isWinner (playerX, i+1)) {
                        gameContinue = false;
                        break;
                    }

                    if (playedCard == -1) { // no card to play
                        if (stock.getCardCount()>0) {
                            System.out.println ("___Player needs an additional card.");
                            addCard (playerX, stock);  // allow one additional card
                            playedCard = playTheCard (playerX, keyCard, stock, dpile); // player plays after getting additional card

                            if (isWinner (playerX, i+1)) {
                                gameContinue = false;
                                break;
                            }
                            if (playedCard == -1) System.out.println ("___No card to play. Pass.");
                        }
                        else if (stock.getCardCount()== 0) {
                            System.out.println ("Stock is empty. Finish the game.");
                            /* player cannot get additional card because stock is empty */
                            finishGame(player1, player2);
                            gameContinue = false;
                        }
                    }
                }
            }
        }

    } // end of play method

    /**
     * This method  implement initial game setup
     * @param player1 // player1 cards
     * @param player2 // player2's cards
     * @param deck // cards in deck
     * @param st // cards in undealt card stock
     * @param dp  // cards in discard pile
     */
    public static void startGame (Hand player1, Hand player2, Deck deck, Hand st, Hand dp) {
        // at the game start, each player gets 7 cards from the deck
        // the remaining cards will become a card stock
        Card nextCard;

        for (int i = 0; i< CARD_TO_EACH_PLAYER; i++) {
            nextCard = deck.dealCard();
            player1.addCard(nextCard);  //give a card to player1
            nextCard = deck.dealCard();
            player2.addCard(nextCard); //give a card to player2
        }

        nextCard = deck.dealCard();
        dp.addCard(nextCard);

        int cardInDeck =  deck.cardsLeft();
        for (int i = 0; i<cardInDeck; i++) {
            nextCard = deck.dealCard();
            st.addCard(nextCard);
        }

    }

    /**
     * This method prints card sets (players/stock/discard pile).
     * @param player
     * @param k // used to identify card set: 1-player1, 2-player2, 3-stock, 4-discard pile
     */
    public static void printPlayerCards (Hand player, int k) {
        String cardSet= "";
        switch (k) {
            case 1: cardSet = "Player1";
                break;
            case 2: cardSet = "Player2";
                break;
            case 3: cardSet = "Stock";
                break;
            case 4: cardSet = "Discard Pile";
                break;
        }
        System.out.println(cardSet + ":");
        int size = player.getCardCount();
        for (int i = 0; i<size; i++)
            System.out.println(player.getCard(i).toString());

        System.out.println();
    }

    /**
     * Implements rules used to play the top card.
     * Player checks cards in the hand to match either the key card's value or suit.
     * If the payer's card is the Eight, it can play any key card.
     * @param player
     * @param kCard
     * @param st
     * @param dp
     * @return position, the card number in the player's set that played the key card. Returns -1 if no card to play
     */
    public static int playTheCard (Hand player, Card kCard, Hand st, Hand dp) {

        Card t;  // stores the next card that players checks
        int position = -1; // stores the player's card number that can play the top card; -1 if no card to play the top card

        for (int j = 0; j< player.getCardCount(); j++) {

            t = player.getCard(j);
            System.out.println("___Player is checking the card[" + j + "]: " + t.toString());

            if (matchValue(kCard, t) ||
                    matchSuit(kCard, t)||
                    isEight(t))
            {
                position = j;
                System.out.println ("___Found a good card: " + t.toString() + "! Playing!");
                moveToDiscardPile(t, player, dp);
                kCard = t;
                break;
            }
        }// end of for-loop

        return position;

    }

    /**
     * Used to check if player's card match the key card's value
     * @param keyCard
     * @param playerCard
     * @return true if the player's card match the key card's value; false - if no match found
     */
    public static boolean matchValue (Card keyCard, Card playerCard) {
        boolean sameValue = false;
        if (playerCard.getValue() == keyCard.getValue()) {
            sameValue = true;
            System.out.println ("___VALUE match: " + playerCard.toString());
        }
        return sameValue;
    }

    /**
     * Used to check if player's card match the key card's suit
     * @param keyCard
     * @param playerCard
     * @return true if the player's card match the key card's suit; false - if no match found
     */
    public static boolean matchSuit (Card keyCard, Card playerCard) {
        boolean sameSuit = false;
        if (playerCard.getSuit() == keyCard.getSuit()) {
            sameSuit = true;
            System.out.println ("___SUIT match: " + playerCard.toString());
        }

        return sameSuit;
    }

    /**
     * This method checks if the given card is the Eight
     * @param playerCard
     * @return true if the card is "8" card
     */
    public static boolean isEight (Card playerCard) {
        boolean card8 = false;
        if (playerCard.getValue() == 8) {
            card8 = true;
            System.out.println ("___Found the Eight Card: " + playerCard.toString());
        }

        return card8;
    }

    /**
     * This methods remove the given card from player's set and add the card to the discard pile
     * @param playerCard
     * @param player
     * @param dp  // discard pile card set
     */
    public static void moveToDiscardPile(Card playerCard, Hand player, Hand dp) {
        player.removeCard(playerCard);
        System.out.println ("___Moving the card to  discard pile: " + playerCard.toString());
        dp.addCard (playerCard);
    }

    /**
     * This methods adds an additional card to player from the stock
     * @param player
     * @param st // stock card set
     */
    public static void addCard (Hand player, Hand st) {
        Card newCard;
        int sizeOfStock = st.getCardCount();
        newCard = st.getCard(sizeOfStock-1);
        st.removeCard(newCard);
        player.addCard(newCard);
        System.out.println ("___Player got an additional card from the stock: " + newCard.toString());
    }
    /**
     * This methods completes the game by calculating the player's point
     * @param player1
     * @param player2
     */
    public static void finishGame (Hand player1,  Hand player2)  {
        System.out.println("*********** End Of Game *************");

        printPlayerCards (player1, 1);
        printPlayerCards (player2, 2);

        // count each player points
        int p1 = getPlayerPoints(player1, 1);
        int p2 = getPlayerPoints(player2, 2);

        if (p1<p2)   System.out.println("Player1 wins.");
        else if (p1>p2) System.out.println("Player2 wins.");
        else System.out.println("Player1 and Player2 have the same points. \n Winner is decided by playing Rock, Paper, Scisors.");
    }
    /**
     * This method  counts the player's point.
     * @param player
     * @param nbr
     * @return player's points
     */
    public static int getPlayerPoints (Hand player, int nbr) {
        int points = 0;
        int numCard = player.getCardCount();
        for (int i = 0; i<numCard; i++) {
            points += player.getCard(i).
                    getCardPoint();
        }

        System.out.println("Player" + nbr + " points total: " + points);
        return points;
    }

    /**
     * This method is used to nominate the suit if the key card is the Eight
     * @param theCard
     * @return the key card with the suit nominated by player
     */
    public static Card nominate8Suit(Card theCard) {
        int nominated8Suit = theCard.getSuit();
        Scanner scanner = new Scanner (System.in);

        System.out.println("Player has to nominate a suit for K-Card.");
        boolean decided = false;
        while (!decided) {
            System.out.println("Enter your choice: 0 (SPADES), 1 (HEARTS), 2 (DIAMONDS), 3 (CLUBS)");
            nominated8Suit = scanner.nextInt();

            if ((nominated8Suit == 0)||
                    (nominated8Suit == 1)||
                    (nominated8Suit == 2)||
                    (nominated8Suit == 3))
                decided = true;
        }

        theCard = new Card (theCard.getValue(), nominated8Suit) ;
        System.out.println("Nominated suit:" + theCard.getSuitAsString());
        System.out.println(".........................................");
        System.out.println("|           K-Card: " + theCard.toString() + "         |");
        System.out.println(".........................................");
        return theCard;
    }

    /**
     * This method is used to get the next key card
     * @param dp
     * @return the key card
     */
    public static Card getKeyCard (Hand dp) {
        Card keyCard;
        int counter = dp.getCardCount();
        keyCard = dp.getCard(counter-1); // the last card in the discard pile will be a key card
        System.out.println("|           K-Card: " + keyCard.toString() + "      |");
        System.out.println(".........................................");
        if (isEight (keyCard)) keyCard = nominate8Suit(keyCard);

        return keyCard;
    }

    /**
     * Checks if the player has no card left. Such player will be a winner
     * @param player
     * @param x
     * @return
     */
    public static boolean isWinner (Hand player, int x) {
        boolean winner = false;
        String currentPlayer = "";
        if (x==1) currentPlayer = "Player1";
        else if(x==2) currentPlayer = "Player2";

        if (player.getCardCount()==0) { // all player's cards were played
            System.out.println("........................................");
            System.out.println(currentPlayer + ": WINNER! (used all his cards)");
            System.out.println("End of game. ");
            winner = true;
        }
        return winner;
    }
} // end class


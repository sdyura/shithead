package net.yura.shithead.common;

import net.yura.cardsengine.Card;
import net.yura.cardsengine.Rank;
import net.yura.cardsengine.Suit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Shithead!");

        List<String> playerNames = new ArrayList<>();
        System.out.print("Enter number of players (2-4): ");
        int numPlayers = Integer.parseInt(scanner.nextLine());
        while (numPlayers < 2 || numPlayers > 4) {
            System.out.print("Invalid number of players. Enter number of players (2-4): ");
            numPlayers = Integer.parseInt(scanner.nextLine());
        }

        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for player " + (i + 1) + ": ");
            playerNames.add(scanner.nextLine());
        }

        ShitheadGame game = new ShitheadGame(playerNames);
        game.deal();

        // Initial rearrangement phase (simplified)
        for (Player player : game.getPlayers()) {
            System.out.println("\n" + player.getName() + ", it's time to rearrange your hand and face-up cards.");
            System.out.println("Your hand: " + player.getHand());
            System.out.println("Your face-up cards: " + player.getUpcards());
            System.out.println("Enter cards from your hand to swap with face-up cards (e.g., 'AS,KH' or 'done').");
            // This is a simplified version. A real implementation would involve selecting specific face-up cards to swap.
            // For now, we'll just allow them to pick from hand, and assume they are swapping with the first available face-up slots.
            // A more robust UI would be needed for proper selection.
            // This part is complex for a simple CLI and is often done with more visual feedback.
            // We will skip the actual swapping logic here for brevity in the CLI test.
            System.out.println("(Skipping actual rearrangement for this simple CLI test version)");
        }


        while (!game.isFinished()) {
            Player currentPlayer = game.getCurrentPlayer();
            System.out.println("\n------------------------------------");
            System.out.println(currentPlayer.getName() + "'s turn.");
            Card topCard = game.getWastePile().isEmpty() ? null : game.getWastePile().get(game.getWastePile().size() - 1);
            System.out.println("Waste pile top: " + (topCard != null ? topCard : "Empty"));
            System.out.println("Waste pile size: " + game.getWastePile().size());


            if (!currentPlayer.getHand().isEmpty()) {
                System.out.println("Your hand: " + currentPlayer.getHand());
                System.out.print("Play card(s) (e.g., 'AS,KH' or '5S'), type 'pickup' to take the pile: ");
                String input = scanner.nextLine().trim().toUpperCase();

                if (input.equalsIgnoreCase("pickup")) {
                    game.pickUpWastePile(currentPlayer);
                }
                else {
                    List<Card> cardsToPlay = parseCards(input);
                    if (cardsToPlay == null) {
                        System.out.println("Invalid card format. Try again.");
                        // Let the loop continue for the current player to try again
                        continue;
                    }
                    // Validate player has the cards
                    boolean hasAllCards = true;
                    for(Card c : cardsToPlay) {
                        if (!currentPlayer.getHand().contains(c)) {
                            hasAllCards = false;
                            break;
                        }
                    }
                    if (!hasAllCards) {
                        System.out.println("You don't have all the specified cards in your hand. Try again.");
                        continue;
                    }

                    if (!game.playCards(currentPlayer, cardsToPlay)) {
                        System.out.println("Invalid move.");
                    }
                }
            } else if (!currentPlayer.getUpcards().isEmpty()) {
                System.out.println("Your face-up cards: " + currentPlayer.getUpcards());
                System.out.print("Play a face-up card (e.g., 'AS') or type 'pickup': ");
                String input = scanner.nextLine().trim().toUpperCase();
                if (input.equalsIgnoreCase("pickup")) {
                    game.pickUpWastePile(currentPlayer);
                    // nextPlayer() is called by pickUpWastePile
                } else {
                    List<Card> cardsToPlay = parseCards(input);
                    if (cardsToPlay == null || cardsToPlay.isEmpty()) {
                        System.out.println("Invalid input. Play one card or type 'pickup'.");
                        continue;
                    }
                    if (!currentPlayer.getUpcards().containsAll(cardsToPlay)) {
                        System.out.println("You don't have that card in your face-up pile. Try again.");
                        continue;
                    }
                    if (!game.playCards(currentPlayer, cardsToPlay)) {
                        System.out.println("Invalid move.");
                    }
                }

            } else if (!currentPlayer.getDowncards().isEmpty()) {
                System.out.println("Your face-down cards remaining: " + currentPlayer.getDowncards().size());
                System.out.print("Play a face-down card by index (0 to " + (currentPlayer.getDowncards().size() - 1) + "): ");
                int index = -1;
                try {
                    index = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid index. Try again.");
                    continue;
                }

                if (index < 0 || index >= currentPlayer.getDowncards().size()) {
                    System.out.println("Invalid index. Try again.");
                    continue;
                }
                List<Card> cardToPlay = new ArrayList<>();
                cardToPlay.add(currentPlayer.getDowncards().get(index));
                if (!game.playCards(currentPlayer, cardToPlay)) {
                    System.out.println("Invalid move, you picked up the pile");
                }
            } else {
                // Should not happen if game over logic is correct
                System.out.println(currentPlayer.getName() + " has no cards left but game is not over?");
                break;
            }

            if (game.isFinished()) {
                System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                List<Player> remainingPlayers = game.getPlayers();
                if (remainingPlayers.size() == 1) {
                    System.out.println("Game Over! " + remainingPlayers.get(0).getName() + " is the Shithead!");
                } else {
                    System.out.println("Game Over!");
                }
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
        scanner.close();
    }

    private static List<Card> parseCards(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>(); // Represents choosing to pick up the pile or an empty action
        }
        List<Card> cards = new ArrayList<>();
        String[] cardStrings = input.split(",");
        for (String cardStr : cardStrings) {
            cardStr = cardStr.trim();
            if (cardStr.length() < 2 || cardStr.length() > 3) return null; // Invalid format e.g. "AS" or "10S"

            Rank rank = null;
            Suit suit = null;
            String rankStr;
            char suitChar;

            if (cardStr.startsWith("10")) {
                rankStr = "10";
                suitChar = cardStr.charAt(2);
            } else {
                rankStr = "" + cardStr.charAt(0);
                suitChar = cardStr.charAt(1);
            }

            for (Rank r : Rank.THIRTEEN_RANKS) {
                if (r.toString().equalsIgnoreCase(rankStr)) {
                    rank = r;
                    break;
                }
            }

            for (Suit s : Suit.FOUR_SUITS) {
                if (Character.toUpperCase(suitChar) == s.toChar()) {
                    suit = s;
                    break;
                }
            }

            if (rank == null || suit == null) { return null; }
            cards.add(Card.getCardByRankSuit(rank, suit));
        }
        return cards;
    }
}
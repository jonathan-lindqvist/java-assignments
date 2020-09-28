import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import static java.lang.System.*;
/*
 * The Pig game
 * See http://en.wikipedia.org/wiki/Pig_%28dice_game%29
 *
 */
public class Pig {
    public static void main(String[] args) {
        new Pig().program();
    }
    // The only allowed instance variables (i.e. declared outside any method)
    // Accessible from any method
    final Scanner sc = new Scanner(in);
    final Random rand = new Random();
    void program() {
        // test();                  // <-------------- Uncomment to run tests!
        final int winPts = 20;      // Points to win (decrease if testing)
        Player[] players;           // The players (array of Player objects)
        Player current;             // Current player for round (must use)
        boolean aborted = false;    // Game aborted by player?
        boolean won = false;        // Game won by player?
        welcomeMsg(winPts);
        players = getPlayers();     // ... this (method to read in all players)
        statusMsg(players);
        current = getRandomPlayer(players);   // Set random player to start
        while(!aborted && !won){
            String playerChoice = getPlayerChoice(current);
            if(playerChoice.equals("q")){
                aborted = true;
            }else if(playerChoice.equals("n")){
                current.totalPts += current.roundPts;
                current = nextPlayer(players, current);
            }else if(playerChoice.equals("r")){
                current = rollDiceForPlayer(players, current);
            }else{
                out.println("Please enter correct commands");
                out.println("Commands are: r = roll , n = next, q = quit");
            }
            if(isPlayerWinner(current, winPts)){
                won = true;
            }
        }
        gameOverMsg(current, aborted);
    }
    // ---- Game logic methods --------------
    // Can not test because of randomness
    Player rollDiceForPlayer(Player[] players, Player current) {
        int diceNumber = getDiceNumber();
        if(diceNumber > 1){
            current.roundPts += diceNumber;
            roundMsg(diceNumber, current);
        }else{
            roundMsg(diceNumber, current);
            current = nextPlayer(players, current);
        }
        return current;
    }
    private int getDiceNumber() {
        return rand.nextInt(6) + 1;
    }
    // Can not test because of randomness
    Player getRandomPlayer(Player[]players){
        int playerIndex = rand.nextInt(players.length);
        return players[playerIndex];
    }
    Player nextPlayer(Player[] players, Player current){
        current.roundPts = 0;
        statusMsg(players);
        int currentIndex = Arrays.asList(players).indexOf(current);
        return players[(currentIndex + 1) % players.length];
    }
    boolean isPlayerWinner(Player current, int winPts) {
        return current.totalPts + current.roundPts >= winPts;
    }
    // ---- IO methods ------------------
    void welcomeMsg(int winPoints) {
        out.println("Welcome to PIG!");
        out.println("First player to get " + winPoints + " points will win!");
        out.println("Commands are: r = roll , n = next, q = quit");
        out.println();
    }
    void statusMsg(Player[] players) {
        out.print("Points: ");
        for (int i = 0; i < players.length; i++) {
            out.print(players[i].name + " = " + players[i].totalPts + " ");
        }
        out.println();
    }
    void roundMsg(int result, Player current) {
        if (result > 1) {
            out.println("Got " + result + " running total are " + current.roundPts);
        } else {
            out.println("Got 1 lost it all!");
        }
    }
    void gameOverMsg(Player player, boolean aborted) {
        if (aborted) {
            out.println("Aborted");
        } else {
            out.println("Game over! Winner is player " + player.name + " with "
                    + (player.totalPts + player.roundPts) + " points");
        }
    }
    String getPlayerChoice(Player player) {
        out.print("Player is " + player.name + " > ");
        return sc.nextLine();
    }
    Player[] getPlayers() {
        out.print("How many players? > ");
        int nPlayers = sc.nextInt();
        sc.nextLine();
        Player[] players = new Player[nPlayers];
        for(int index = 0; index < players.length; index++){
            out.print("Enter name for player " + (index + 1) + " > ");
            players[index] = new Player(sc.nextLine());
        }
        return players;
    }
    // ---------- Class -------------
    // Class representing the concept of a player
    // Use the class to create (instantiate) Player objects
    class Player {
        String name;     // Default null
        int totalPts;    // Total points for all rounds, default 0
        int roundPts;    // Points for a single round, default 0
        Player(String name){
            this.name = name;
        }
        Player(){
        }
    }
    // ----- Testing -----------------
    // Here you run your tests i.e. call your game logic methods
    // to see that they really work (IO methods not tested here)
    void test() {
        // This is hard coded test data
        // An array of (no name) Players (probably don't need any name to test)
        Player[] players = {new Player(), new Player(), new Player()};
        out.println(isPlayerWinner(players[0], 20) == false);
        out.println("Ignore printouts, or we will have redundant code :)");
        out.println(nextPlayer(players, players[0]) == players[1]); // check if next player
        out.println(nextPlayer(players, players[2]) == players[0]); // check if method loop players
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        for(int i = 0; i < 100; i++) {
            numbers.add(getDiceNumber());
        }
        out.println("Random test:");
        out.println(!numbers.contains(0));
        out.println(numbers.contains(1));
        out.println(numbers.contains(2));
        out.println(numbers.contains(3));
        out.println(numbers.contains(4));
        out.println(numbers.contains(5));
        out.println(numbers.contains(6));
        out.println(!numbers.contains(7));
        // TODO Use for testing of logcial methods (i.e. non-IO methods)
        exit(0);   // End program
    }
}
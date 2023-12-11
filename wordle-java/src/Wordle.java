// A simple program to demonstrate
// Tic-Tac-Toe Game.
import java.util.*;

public class Wordle {

    static String userWordToGuess;
    static Map<Character, Set<Integer>> lettersWithPlacements;
    static boolean startNewRound = true;

    static Scanner in = new Scanner(System.in);

    static final int totalTurns = 7;
    static final int lettersInWord = 5;


    public static void main(String[] args)
    {
        while (startNewRound) {
            newRound();
        }
        in.close();
    }

    public static void newRound() {
        getWordAndCreateLetterMap();
        System.out.println("Guess a " + lettersInWord + "-letter word. Enter q to quit the current round at any time");
        playGame();
        System.out.println();
        System.out.println();
    }

    public static void playGame() {
        String userInput;

        int numCorrectlyPlacedLetters;
        int [] resultsOfUserInput = new int [lettersInWord]; // 0 = not in word, 1 = in word but wrong placements, 2 = correct placement

        boolean stillPlayingCurrentRound = true;
        boolean guessedCorrectly = false;
        boolean quitGame = false;
        int numTurnsLeft = totalTurns;

        while (stillPlayingCurrentRound) {
            System.out.println("You have " + numTurnsLeft + " guesses");
            userInput = in.nextLine();
            if (userInput.equals("q")) {
                stillPlayingCurrentRound = false;
                quitGame = true;
                continue;
            }
            if (userInput.length() != lettersInWord) {
                System.out.println("Please enter a " + lettersInWord + "-letter word");
                continue;
            }
            Arrays.fill(resultsOfUserInput, 0);
            numCorrectlyPlacedLetters = 0;
            for (int i = 0; i < lettersInWord; i ++) {
                char letter = userInput.charAt(i);
                if (lettersWithPlacements.containsKey(letter)) {
                    if (lettersWithPlacements.get(letter).contains(i)) {
                        resultsOfUserInput[i] = 2;
                        numCorrectlyPlacedLetters ++;
                    } else {
                        resultsOfUserInput[i] = 1;
                    }
                } else {
                    resultsOfUserInput[i] = 0;
                }
            }

            for (int number : resultsOfUserInput) {
                System.out.print(number + " ");
            }
            System.out.println();

            if (numCorrectlyPlacedLetters == lettersInWord) {
                stillPlayingCurrentRound = false;
                guessedCorrectly = true;
            }

            numTurnsLeft --;
            if (numTurnsLeft == 0) {
                stillPlayingCurrentRound = false;
                guessedCorrectly = false;
            }
        }
        if (quitGame) {
            System.out.println("You have entered q to quit the current round.");
        }
        else if (guessedCorrectly) {
            System.out.println("Congrats! The word was " + userWordToGuess);
        } else {
            System.out.println("You did not guess the word correctly. The word was " + userWordToGuess);
        }
        System.out.println("Press a to re-start a new round, and Press b to quit the game entirely");
        userInput = in.nextLine();
        if (userInput.equals("b")) {
            startNewRound = false;
        }
    }

    public static void getWordAndCreateLetterMap() {
        // Get new word
        // Get the instance of APIDataManager
        WordsFromApi wordsFromApi = WordsFromApi.getInstance();
        String word = wordsFromApi.getRandomWordFromList();
        userWordToGuess = word;

        //setup hash map
        lettersWithPlacements = new HashMap<>();
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            lettersWithPlacements.computeIfAbsent(letter, k -> new HashSet<>()).add(i);
        }
    }
}
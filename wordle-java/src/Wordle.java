import java.util.*;

public class Wordle {

    static String userWordToGuess;
    static Map<Character, Set<Integer>> lettersWithPlacements;
    static boolean keepPlayingGame = true;

    static Scanner in = new Scanner(System.in);

    static final int totalTurns = 7;
    static final int lettersInWord = 5;


    public static void main(String[] args)
    {
        while (keepPlayingGame) {
            startNewRound();
        }
        in.close();
    }

    public static void startNewRound() {
        getWordAndCreateLetterMap();
        System.out.println("\n\nYou have " + totalTurns + " turns to guess a " + lettersInWord + "-letter word. Enter q to quit the current round at any time?");
        playGame();
    }

    public static void playGame() {
        String userInput;

        int numCorrectlyPlacedLetters;
        int [] resultsOfUserInput = new int [lettersInWord]; // 0 = not in word, 1 = in word but wrong placements, 2 = correct placement

        boolean stillPlaying = true;
        int numTurnsLeft = totalTurns;

        while (stillPlaying) {
            userInput = in.nextLine();
            if (userInput.equals("q")) {
                System.out.println("You have entered q to quit the current round. Press a to re-start a new round, and Press b to quit the game entirely");
                userInput = in.nextLine();
                if (userInput.equals("b")) {
                    keepPlayingGame = false;
                }
                break;
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
                System.out.println("Congrats! The word was " + userWordToGuess + ". You win in " + (totalTurns - numTurnsLeft + 1) + " guesses");
                System.out.println("Press a to re-start a new round, and Press b to quit the game entirely");
                userInput = in.nextLine();
                if (userInput.equals("b")) {
                    keepPlayingGame = false;
                }
                stillPlaying = false;
            }

            numTurnsLeft --;
            System.out.println("You have " + numTurnsLeft + " guesses");
            if (numTurnsLeft == 0) {
                System.out.println("You did not guess the word correctly. The word was " + userWordToGuess);
                System.out.println("Press a to re-start a new round, and Press b to quit the game entirely");
                userInput = in.nextLine();
                if (userInput.equals("b")) {
                    keepPlayingGame = false;
                }
                stillPlaying = false;
            }
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

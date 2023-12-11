import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
data class WordFromApiData(
    @SerialName("word") val word: String,
    @SerialName("score") val score: Int
)

class DataMuseApiCaller {
    companion object {
        private var wordList: MutableList<String> = mutableListOf()

        fun getWordList(): List<String> {
            return wordList.toList()
        }

        fun getRandomWordFromList(): String {
            return wordList.random();
        }

        suspend fun setWordList() {
            try {
                val apiUrl = "https://api.datamuse.com/words?sp=?????"

                var jsonResponse = withContext(Dispatchers.IO) {
                    URL(apiUrl).readText()
                }

                val wordDataList: List<WordFromApiData> = Json.decodeFromString(jsonResponse)

                wordList.addAll(wordDataList.map { it.word })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class GameStatusValues {
    companion object {
        var startNewRound: Boolean = true
        var wordForUserToGuess: String = ""
        var lettersWithPlacements: MutableMap<Char, MutableSet<Int>> = HashMap()
        val totalTurns: Int = 7
        val lettersInWord: Int = 5
    }
}

fun getWordAndCreateLetterMap() {
    val word = DataMuseApiCaller.getRandomWordFromList()
    GameStatusValues.lettersWithPlacements = HashMap()
    for (i in word.indices) {
        val letter = word[i]
        GameStatusValues.lettersWithPlacements.computeIfAbsent(letter) { HashSet() }.add(i)
    }
    GameStatusValues.wordForUserToGuess = word
}

fun playGame() {
//    println(GameStatusValues.wordForUserToGuess)
//    println(GameStatusValues.lettersWithPlacements)


    var numCorrectlyPlacedLetters: Int
    var resultsOfUserInput: MutableList<Int>
    var stillPlayingCurrentRound = true
    var guessedCorrectly = false
    var quitGame = false
    var numTurnsLeft: Int = GameStatusValues.totalTurns

    var userInput: String?

    while (stillPlayingCurrentRound) {
        println("You have $numTurnsLeft left")
        userInput = readLine()
        if (userInput == "q") {
            stillPlayingCurrentRound = false
            quitGame = true
            continue
        }
        if (userInput == null || userInput.length != GameStatusValues.lettersInWord) {
            println("Please enter a ${GameStatusValues.lettersInWord}-letter word")
            continue
        }
        numCorrectlyPlacedLetters = 0
        resultsOfUserInput = mutableListOf()
        for (i in 0 until GameStatusValues.lettersInWord) {
            val letter = userInput[i]
            if (GameStatusValues.lettersWithPlacements.containsKey(letter)) {
                if (GameStatusValues.lettersWithPlacements[letter]?.contains(i) == true) {
                    resultsOfUserInput.add(2)
                    numCorrectlyPlacedLetters ++
                } else {
                    resultsOfUserInput.add(1)
                }
            } else {
                resultsOfUserInput.add(0)
            }
        }

        for (number in resultsOfUserInput) {
            print("$number ")
        }
        println()

        if (numCorrectlyPlacedLetters == GameStatusValues.lettersInWord) {
            guessedCorrectly = true
            stillPlayingCurrentRound = false
        }

        numTurnsLeft --
        if (numTurnsLeft == 0) {
            guessedCorrectly = false
            stillPlayingCurrentRound = false
        }
    }

    if (quitGame) {
        println("You have entered q to quit the current round.")
    } else if (guessedCorrectly) {
        println("Congrats! The word was ${GameStatusValues.wordForUserToGuess}")
    } else {
        println("You did not guess the word correctly. The word was ${GameStatusValues.wordForUserToGuess}")
    }
    println("Press a to re-start a new round, and Press b to quit the game entirely")
    userInput = readLine()
    if (userInput == "b") {
        GameStatusValues.startNewRound = false
    }
}

fun newRound() {
    getWordAndCreateLetterMap()
    println("Guess a ${GameStatusValues.lettersInWord}-letter word. Enter q to quit the current round at any time")
    playGame()
    println()
    println()
}

suspend fun main() {
    DataMuseApiCaller.setWordList()
    while (GameStatusValues.startNewRound) {
        newRound()
    }
}

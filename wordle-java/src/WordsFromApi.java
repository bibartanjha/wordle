import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class WordsFromApi {
    private static WordsFromApi instance = new WordsFromApi();
    private String[] wordsList;

    private WordsFromApi() {
        try {
            String apiUrl = "https://api.datamuse.com/words?sp=?????";

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                String jsonResponse = response.toString();
                wordsList = parseString(jsonResponse);
            } else {
                wordsList = new String[0];
                System.err.println("Failed to retrieve a word. HTTP Status: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] parseString(String allWordsString) {

        String[] seperatedWords = allWordsString.split(",\"score\":\\d+},\\{\"word\":");
        seperatedWords[0] = seperatedWords[0].replace("[{\"word\":", "");
        seperatedWords[seperatedWords.length - 1] = seperatedWords[seperatedWords.length - 1].split(",\"score\":\\d+}]")[0];

        for (int i = 0; i < seperatedWords.length; i ++) {
            seperatedWords[i] = seperatedWords[i].replace("\"", "");
        }

        return seperatedWords;
    }

    public static WordsFromApi getInstance() {
        return instance;
    }

    public String getRandomWordFromList() {
        // Create a Random object
        Random random = new Random();

        // Generate a random index within the bounds of the array
        int randomIndex = random.nextInt(wordsList.length);

        // Get the random item from the array
        return wordsList[randomIndex];
    }
}

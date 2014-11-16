package at.tuwien.aic.tweetanalysis.preprocessing;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Preprocess the Trainingdata
 * <p/>
 * This Creates the training.csv from the 6GB File
 *
 * @author Group 1
 */
public class PreprocessingCSVTraindata {

    //BufferedReader br = null;
    FileWriter writer = null;

    public PreprocessingCSVTraindata() throws Exception {

        //br = new BufferedReader(new FileReader("tweets.txt"));
        writer = new FileWriter("test.csv");
    }

    /**
     * Parse the JSON Tweets
     * TODO: Only the Tweet Content is parsed at the Time!
     */
    public void extractTheTweetContent() throws Exception {

        JsonFactory f = new MappingJsonFactory();
        JsonParser jp = f.createJsonParser(new File("tweets.txt"));

        // TODO: parse the whole file - not only the first few!
        for (int i = 0; i <= 5000; i++) {

            jp.nextToken();
            String fieldName = jp.getCurrentName();

            if (fieldName != null && fieldName.equals("text")) {
                jp.nextToken();
                String content = formatTheTweetContent(jp.getText());
                System.out.println("WriteContent: " + content);
                writeCsvFile(content);
            }
        }
    }

    /**
     * Replace the Smiles and so on...
     * TODO: Moar Replacement for more Accuracy!
     */
    public String formatTheTweetContent(String input) {
        input = input.replace("\n", "");
        input = input.replace(":)", "POSITIVESMILE");
        input = input.replace(":D", "POSITIVESMILE");
        input = input.replace(":(", "NEGATIVESMILE");
        input = input.replace(":S", "NEGATIVESMILE");

        return input;
    }

    private void writeCsvFile(String tweet) {
        try {
            if (tweet.contains("POSITIVESMILE")) {
                writer.append(tweet);
                writer.append(',');
                writer.append("positive");
                writer.append('\n');
            } else if (tweet.contains("NEGATIVESMILE")) {
                writer.append(tweet);
                writer.append(',');
                writer.append("negative");
                writer.append('\n');
            } else {
                writer.append(tweet);
                writer.append(',');
                writer.append("neutral");
                writer.append('\n');
            }

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import at.tuwien.aic.tweetanalysis.provider.ITweetProvider;
import at.tuwien.aic.tweetanalysis.provider.JSONTweetProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Preprocess the Training and Testing Data
 * TODO: The Training and Testing Data is not very good!
 * TODO: More JSONPreprocessing!
 * TODO: Maybe manually fixes needed?
 *
 * <p/>
 * This Creates the training.csv and testing.csv from the 6GB File
 * Uses the first 100 Entries for training and the second 100 for the Testing
 *
 * @author Group 1
 */
public class CSVPreprocessingTrainingAndTestingData {

    private static final String file = "tweets.txt";
    FileWriter trainingWriter = null;
    FileWriter testingWriter = null;
    private ITweetProvider tp;

    public CSVPreprocessingTrainingAndTestingData() throws Exception {

        trainingWriter = new FileWriter("training.csv");
        testingWriter = new FileWriter("testing.csv");
        this.tp = new JSONTweetProvider(file);
    }

    /**
     * Parse the Tweets from the File into the CSV
     *
     */
    public void extractTheTweetContentIntoCSV() throws Exception {

        System.out.println("extractTheTweetContentIntoCSV");

        int size = 200;
        Future<List<Tweet>> f = this.tp.getTweets(size, 0, 0, null, null);
        List<Tweet> list = f.get();

        //preprozess the tweets
        NaiveTweetPreprocessor n = new NaiveTweetPreprocessor();
        n.preprocess(list);

        //first 100 into Training
        writeCsvFile(list.subList(0, 99), trainingWriter);

        //second 100 into Testing
        writeCsvFile(list.subList(100, 199), testingWriter);

    }


    private void writeCsvFile(List<Tweet> list, FileWriter writer) {

        try {
            writer.append("Tweet,Sentiment\n");
            writer.flush();

            for (Tweet t : list) {

                //few adjustments to the strings for better results
                String content = t.getContent();
                content = content.replace("\n","");
                content = content.replace("\"","");

                if (t.getContent().contains("POSITIVESMILE")) {
                    writer.append("\"" + content + "\"");
                    writer.append(',');
                    writer.append("positive");
                    writer.append('\n');
                } else if (t.getContent().contains("NEGATIVESMILE")) {
                    writer.append("\"" + content + "\"");
                    writer.append(',');
                    writer.append("negative");
                    writer.append('\n');
                } else {
                    writer.append("\"" + content + "\"");
                    writer.append(',');
                    writer.append("neutral");
                    writer.append('\n');
                }
            }

            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

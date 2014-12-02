package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.TweetAnalysis;
import at.tuwien.aic.tweetanalysis.classifier.Sentiment;
import at.tuwien.aic.tweetanalysis.classifier.WekaUtils;
import at.tuwien.aic.tweetanalysis.entities.Tweet;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * <p/>
 * Date: 24.11.2014
 * Time: 16:39
 *
 * @author Stefan Victora
 */
public final class TrainingDataPreprocessor {

    public static final Logger log = LoggerFactory.getLogger(TrainingDataPreprocessor.class);

    private TrainingDataPreprocessor() {
    }

    public static void main(String[] args) throws IOException {
        TrainingDataPreprocessor trainingDataPreprocessor = new TrainingDataPreprocessor();


//        try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter("processed-tweets.csv")))) {
//            csvWriter.writeNext(new String[]{"Tweet", "Sentiment"});
//            trainingDataPreprocessor.preprocess(new StandardTweetPreprocessor(), "/trainingData/negative-tweets.csv", csvWriter);
//            trainingDataPreprocessor.preprocess(new StandardTweetPreprocessor(), "/trainingData/positive-tweets.csv", csvWriter);
//        }

//        trainingDataPreprocessor.preprocessAndCreateInstances(new StandardTweetPreprocessor(),
//                "/trainingData/training.1600000.csv", 1600000);

        trainingDataPreprocessor.preprocessAndCreateInstances2(new StandardTweetPreprocessor(), "/tweets.csv", 6000);

//        try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter("processedSentences.csv")))) {
//            csvWriter.writeNext(new String[]{"Text", "Sentiment"});
//            trainingDataPreprocessor.preprocess2(new StandardTweetPreprocessor(), csvWriter, "/rt-polarity.pos", "1");
//            trainingDataPreprocessor.preprocess2(new StandardTweetPreprocessor(), csvWriter, "/rt-polarity.neg", "0");
//        }


        // "0","1467810369","Mon Apr 06 22:19:45 PDT 2009","NO_QUERY","_TheSpecialOne_","@switchfoot http://twitpic.com/2y1zl - Awww, that's a bummer.  You shoulda got David Carr of Third Day to do it. ;D"
    }

    /**
     * Simple reader and writer method that reads the unprocessed csv file, sets the content to the temp tweet class
     * and calls the given TweetPreprocessor on it.
     * The output is then written in a simpler format to the output file.
     *
     * @param tweetPreprocessor     the tweet processor that should be used on the unprocessed read tweets
     * @param inputResourceLocation the resource path of the input to read
     * @param initialCapacity       the initial capacity of the created instances object
     */
    public void preprocessAndCreateInstances(ITweetPreprocessor tweetPreprocessor, String inputResourceLocation, int initialCapacity) {
        InputStream trainingDataStream = TweetAnalysis.class.getResourceAsStream(inputResourceLocation);

        /* create empty instances */
        Instances instances = WekaUtils.createEmptyInstances(initialCapacity, true);

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(trainingDataStream))) {
            /* format: sentiment = 0, id = 1, date = 2, query = 3, user = 4, tweet = 5
             * sentiment of 0 = negative, sentiment of 4 = positive, sentiment of 2 = neutral */

            Tweet tweet = new Tweet();
            List<Tweet> tweets = Arrays.asList(tweet); // workaround, since the methods for a single tweet have not been implemented fully
            String[] nextLine;
            /* process each csv entry separately */
            while ((nextLine = csvReader.readNext()) != null) {
                /* check if line size is correct */
                String sentimentText = nextLine[0];
                if ("2".equals(sentimentText)) continue;
                if (nextLine.length == 6) {
                    String tweetText = nextLine[5];

                    /* preprocess the content of the tweet */
                    tweet.setContent(tweetText);
                    tweetPreprocessor.preprocess(tweets);
                    /* set sentiment */
                    Sentiment sentiment = null;
                    if ("0".equals(sentimentText)) {
                        sentiment = Sentiment.negative;
                    } else if ("4".equals(sentimentText)) {
                        sentiment = Sentiment.positive;
                    }
                    if (sentiment != null) {
                        tweet.setSentiment(sentiment);
                        WekaUtils.addTweetToUnprocessedInstances(instances, tweet);
                    } else {
                        log.warn("Could not read sentiment from: {}", sentimentText);
                    }
                } else {
                    log.warn("Wrong number of entries: ID: {}; {}", nextLine[1], nextLine.length);
                }
            }
        } catch (IOException e) {
            log.error("Failed to preprocess the read tweets!", e);
        }

        /* save instances to disk */
        WekaUtils.writeInstancesToDisk(instances, "trainData.arff");
    }

    /**
     * Simple reader and writer method that reads the unprocessed csv file, sets the content to the temp tweet class
     * and calls the given TweetPreprocessor on it.
     * The output is then written in a simpler format to the output file.
     *
     * @param tweetPreprocessor     the tweet processor that should be used on the unprocessed read tweets
     * @param inputResourceLocation the resource path of the input to read
     * @param initialCapacity       the initial capacity of the created instances object
     */
    public void preprocessAndCreateInstances2(ITweetPreprocessor tweetPreprocessor, String inputResourceLocation, int initialCapacity) {
        InputStream trainingDataStream = TweetAnalysis.class.getResourceAsStream(inputResourceLocation);

        /* create empty instances */
        Instances instances = WekaUtils.createEmptyInstances(initialCapacity, true);

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(trainingDataStream))) {
            /* format: tweet = 0, sentiment = 1 */

            /* consume header */
            csvReader.readNext();

            Tweet tweet = new Tweet();
            List<Tweet> tweets = Arrays.asList(tweet); // workaround, since the methods for a single tweet have not been implemented fully
            String[] nextLine;
            /* process each csv entry separately */
            while ((nextLine = csvReader.readNext()) != null) {
                /* check if line size is correct */
                if (nextLine.length == 2) {
                    String tweetText = nextLine[0];
                    String sentimentText = nextLine[1];

                    /* preprocess the content of the tweet */
                    tweet.setContent(tweetText);
                    tweetPreprocessor.preprocess(tweets);
                    /* set sentiment */
                    Sentiment sentiment = null;
                    if ("positive".equals(sentimentText)) {
                        sentiment = Sentiment.positive;
                    } else if ("negative".equals(sentimentText)) {
                        sentiment = Sentiment.negative;
                    }
                    if (sentiment != null) {
                        tweet.setSentiment(sentiment);
                        WekaUtils.addTweetToUnprocessedInstances(instances, tweet);
                    } else {
                        log.warn("Could not read sentiment from: {}", sentimentText);
                    }
                } else {
                    log.warn("Wrong number of entries: ID: {}; {}", nextLine[0], nextLine.length);
                }
            }
        } catch (IOException e) {
            log.error("Failed to preprocess the read tweets!", e);
        }

        /* save instances to disk */
        WekaUtils.writeInstancesToDisk(instances, "trainData_our.arff");
    }

    /**
     * Simple reader and writer method that reads the unprocessed csv file, sets the content to the temp tweet class
     * and calls the given TweetPreprocessor on it.
     * The output is then written in a simpler format to the output file.
     *
     * @param tweetPreprocessor     the tweet processor that should be used on the unprocessed read tweets
     * @param inputResourceLocation the resource path of the input to read
     * @param csvWriter             the csv writer
     */
    public void preprocess(ITweetPreprocessor tweetPreprocessor, String inputResourceLocation, CSVWriter csvWriter) {
        InputStream trainingDataStream = TweetAnalysis.class.getResourceAsStream(inputResourceLocation);

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(trainingDataStream))) {

            /* consume header */
            csvReader.readNext();

            Tweet tweet = new Tweet();
            List<Tweet> tweets = Arrays.asList(tweet); // workaround, since the methods for a single tweet have not been implemented fully
            String[] nextLine;
            /* process each csv entry separately */
            while ((nextLine = csvReader.readNext()) != null) {
                /* check if line size is correct */
                if (nextLine.length == 2) {
                    String tweetText = nextLine[0];

                    /* preprocess the content of the tweet */
                    tweet.setContent(tweetText);
                    tweetPreprocessor.preprocess(tweets);
                    /* write content to new csv file */
                    String processedContent = tweet.getContent();
//                    csvWriter.writeNext(new String[]{processedContent, "0".equals(nextLine[1]) ? "negative" : "positive"}); // content, sentiment
                    csvWriter.writeNext(new String[]{processedContent, nextLine[1]}); // content, sentiment (0 for negative, 1 for positive)
//                    log.trace("{}; {} -> {}", nextLine[1], tweetText, processedContent);
                } else {
                    log.warn("Wrong number of entries: ID: {}; {}", nextLine[0], nextLine.length);
                }
            }
        } catch (IOException e) {
            log.error("Failed to preprocess the read tweets!", e);
        }
    }

    /**
     * Simple reader and writer method that reads the unprocessed csv file, sets the content to the temp tweet class
     * and calls the given TweetPreprocessor on it.
     * The output is then written in a simpler format to the output file.
     *
     * @param tweetPreprocessor the tweet processor that should be used on the unprocessed read tweets
     * @param csvWriter
     * @param inputFileName
     * @param sentiment
     */
    public void preprocess2(ITweetPreprocessor tweetPreprocessor, CSVWriter csvWriter, String inputFileName, String sentiment) throws IOException {
        InputStream trainingDataStream = TweetAnalysis.class.getResourceAsStream(inputFileName);

        try (Scanner reader = new Scanner(new InputStreamReader(trainingDataStream))) {
            Tweet tweet = new Tweet();
            List<Tweet> tweets = Arrays.asList(tweet); // workaround, since the methods for a single tweet have not been implemented fully
            String nextLine;
            /* process each csv entry separately */
            while (reader.hasNextLine()) {
                nextLine = reader.nextLine();
                /* preprocess the content of the tweet */
                tweet.setContent(nextLine);
                tweetPreprocessor.preprocess(tweets);
                /* write content to new csv file */
                String processedContent = tweet.getContent();
//                    csvWriter.writeNext(new String[]{processedContent, "0".equals(nextLine[1]) ? "negative" : "positive"}); // content, sentiment
                csvWriter.writeNext(new String[]{processedContent, sentiment}, false); // content, sentiment (0 for negative, 1 for positive)
                log.trace("{}; {} -> {}", sentiment, nextLine, processedContent);
            }
        }
    }

}

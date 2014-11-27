package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.TweetAnalysis;
import at.tuwien.aic.tweetanalysis.entities.Tweet;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

        // todo: unused at the moment

//        trainingDataPreprocessor.preprocess(new StandardTweetPreprocessor(), "processed.csv");

//        try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter("processedSentences.csv")))) {
//            csvWriter.writeNext(new String[]{"Text", "Sentiment"});
//            trainingDataPreprocessor.preprocess2(new StandardTweetPreprocessor(), csvWriter, "/rt-polarity.pos", "1");
//            trainingDataPreprocessor.preprocess2(new StandardTweetPreprocessor(), csvWriter, "/rt-polarity.neg", "0");
//        }
    }

    /**
     * Simple reader and writer method that reads the unprocessed csv file, sets the content to the temp tweet class
     * and calls the given TweetPreprocessor on it.
     * The output is then written in a simpler format to the output file.
     *
     * @param tweetPreprocessor the tweet processor that should be used on the unprocessed read tweets
     * @param outputFileName    the filename the processed tweets should be written to
     */
    public void preprocess(ITweetPreprocessor tweetPreprocessor, String outputFileName) {
        InputStream trainingDataStream = TweetAnalysis.class.getResourceAsStream("/Sentiment Analysis Dataset.csv");

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(trainingDataStream));
             CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(outputFileName)))) {

            String[] header = csvReader.readNext();
            log.trace("Header: {}", Arrays.toString(header));
            /* write new header */
            csvWriter.writeNext(new String[]{"Tweet", "Sentiment"});

            Tweet tweet = new Tweet();
            List<Tweet> tweets = Arrays.asList(tweet); // workaround, since the methods for a single tweet have not been implemented fully
            String[] nextLine;
            /* process each csv entry separately */
            while ((nextLine = csvReader.readNext()) != null) {
                /* check if line size is correct */
                if (nextLine.length == 4) {
                    String tweetText = nextLine[3];

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

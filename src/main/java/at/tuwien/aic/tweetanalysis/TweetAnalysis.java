/*
 * Tweet Sentiment Analysis
 * Vienna UT
 *
 * Advanced Internet Computing
 * Winter term 2014/15
 * Group 01
 * 
 * Patrick Löwenstein
 * Thomas Schreiber
 * Alexander Suchan
 * Stefan Victoria
 * Andreas Waltenberg
 *
 * All rights reserved
 */

package at.tuwien.aic.tweetanalysis;

import at.tuwien.aic.tweetanalysis.classifier.IClassifier;
import at.tuwien.aic.tweetanalysis.classifier.WekaClassifier;
import at.tuwien.aic.tweetanalysis.entities.Tweet;
import at.tuwien.aic.tweetanalysis.preprocessing.StandardTweetPreprocessor;
import at.tuwien.aic.tweetanalysis.provider.TweetProvider;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 *
 *
 * @author Group 1
 */
public class TweetAnalysis {

    public static final Logger log = LoggerFactory.getLogger(TweetAnalysis.class);

    public static void main(String[] args) throws Exception {

        System.out.println("WEKA Test!");

//        testClassifier();
//        testLiveData();
        getLiveData();

//        NaiveTweetPreprocessor naiveTweetPreprocessor = new NaiveTweetPreprocessor();

//        log.trace("{}", naiveTweetPreprocessor);

        // INFO:
        // Ignore the database errors, only important if we use a database for the tweets.

        // Preporcessing Test: Read the BIG Tweetfile and try to make an simple learning CSV File
        //CSVPreprocessingTrainingAndTestingData testPreprocessing = new CSVPreprocessingTrainingAndTestingData();
        //testPreprocessing.extractTheTweetContentIntoCSV();

        // TweetProvider provider = new TweetProvider();
        // List<Tweet> tweets = provider.getTweets("test", 20, new Date(114, 10, 18), new Date(114, 10, 20)).get();
        // System.out.println("Got " + tweets.size() + " tweets");
    }

    private static void testLiveData() throws InterruptedException, java.util.concurrent.ExecutionException {
        TweetProvider tweetProvider = new TweetProvider();

        Future<List<Tweet>> tweets = tweetProvider.getTweets(":)", 20, null, null, "en", null, null);

        List<Tweet> tweetList = tweets.get();

        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        standardTweetPreprocessor.preprocess(tweetList);

        for (Tweet tweet : tweetList) {
            log.info("{}", tweet);
        }

        tweetProvider.shutdown();
    }

    private static void getLiveData() throws Exception {

        TweetProvider tweetProvider = new TweetProvider();

        Future<List<Tweet>> tweets = tweetProvider.getTweets("#hate", 200, null, null, "en", null, null);
        List<Tweet> tweetList = tweets.get();

        try (CSVWriter csvOutput = new CSVWriter(new BufferedWriter(new FileWriter("e.csv")))) {
            csvOutput.writeNext(new String[]{"Tweet", "Sentiment"}, false);

            for (Tweet tweet : tweetList) {
                String z = tweet.getContent();
                z = z.replace("\n", "");

                csvOutput.writeNext(new String[]{z, "negative"}, false);
            }
        }

        tweetProvider.shutdown();
    }

    private static void testClassifier() throws IOException {
        IClassifier tweetTest;
        try (InputStream modelStream = TweetAnalysis.class.getResourceAsStream("/tweet-model.model")) {
            tweetTest = new WekaClassifier(modelStream);
        }

        // Use the Classifier to evaluate a Tweet
        Tweet t = new Tweet();
        t.setContent("bad bad bad bad :(");

        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        ArrayList<Tweet> tweets = new ArrayList<>();
        tweets.add(t);
        standardTweetPreprocessor.preprocess(tweets);
        double[] fDistribution = tweetTest.classifyTweet(t);

        System.out.println("Evaluation of a String: " + t.getContent());
        System.out.println("probability of being positive: " + fDistribution[0]);
        System.out.println("probability of being negative: " + fDistribution[1]);
    }


}

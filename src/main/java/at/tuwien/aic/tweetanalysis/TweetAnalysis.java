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

import java.io.*;
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

        testClassifier();
//        testLiveData();
//        getLiveData();

//        NaiveTweetPreprocessor naiveTweetPreprocessor = new NaiveTweetPreprocessor();
//        log.trace("{}", naiveTweetPreprocessor);

        /*
         * INFO:
         * Ignore the database errors, only important if we use a database for the tweets.
         *
        */

        // TweetProvider provider = new TweetProvider();
        // List<Tweet> tweets = provider.getTweets("test", 20, new Date(114, 10, 18), new Date(114, 10, 20)).get();
        // System.out.println("Got " + tweets.size() + " tweets");
    }

    private static void testLiveData() throws InterruptedException, java.util.concurrent.ExecutionException, IOException {
        TweetProvider tweetProvider = new TweetProvider();

        Future<List<Tweet>> tweets = tweetProvider.getTweets(":)", 20, null, null, "en", null, null);

        List<Tweet> tweetList = tweets.get();

        testClassifierOnTweets(tweetList);

        tweetProvider.shutdown();
    }

    private static void getLiveData() throws Exception {

        TweetProvider tweetProvider = new TweetProvider();

        Future<List<Tweet>> tweets = tweetProvider.getTweets(":D", 200, null, null, "en", null, null);
        List<Tweet> tweetList = tweets.get();

        try (CSVWriter csvOutput = new CSVWriter(new BufferedWriter(new FileWriter("pos.csv")))) {
            csvOutput.writeNext(new String[]{"Tweet", "Sentiment"}, false);

            for (Tweet tweet : tweetList) {
                String z = tweet.getContent();
                z = z.replace("\n", "");

                csvOutput.writeNext(new String[]{z, "positive"}, false);
            }
        }
        tweetProvider.shutdown();
    }

    private static void testClassifier() throws IOException {
        IClassifier tweetTest;
        // TODO:this doesnt work at the moment!
        //try (InputStream modelStream = TweetAnalysis.class.getResourceAsStream("/fff1.model")) {
        //    tweetTest = new WekaClassifier(modelStream);
        //}
        tweetTest = new WekaClassifier();

        // Use the Classifier to evaluate a Tweet
        Tweet t = new Tweet();
        //t.setContent("bad bad bad bad :(");
        //t.setContent("#hate sad :( :(");
        //t.setContent("happy joy :) :) #happy congratulations");
        t.setContent("I love them, thank u mum !! <3 http://t.co/rMQEeRYhnT");

        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        ArrayList<Tweet> tweets = new ArrayList<>();
        tweets.add(t);
        standardTweetPreprocessor.preprocess(tweets);
        double[] fDistribution = tweetTest.classifyTweet(t);

        logResults(t.getContent(), fDistribution);
    }

    private static void testClassifierOnTweets(List<Tweet> tweets) throws IOException {
        /* load model from file */
        IClassifier classifier;
        try (InputStream modelStream = TweetAnalysis.class.getResourceAsStream("/trainingData/tweet-model.model")) {
            classifier = new WekaClassifier(modelStream);
        }
        /* preprocess tweets */
        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        standardTweetPreprocessor.preprocess(tweets);

        /* classify */
        double[] fDistribution;
        for (Tweet tweet : tweets) {
            fDistribution = classifier.classifyTweet(tweet);
            logResults(tweet.getContent(), fDistribution);
        }
    }

    private static void logResults(String content, double[] fDistribution) {
        log.info("Evaluation of a String: {}", content);
        log.info("positive: {}", fDistribution[1]);
        log.info("negative: {}\n", fDistribution[0]);
    }

}
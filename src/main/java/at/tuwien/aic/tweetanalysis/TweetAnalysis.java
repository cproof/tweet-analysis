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

/**
 *
 *
 *
 * @author Group 1
 */
public class TweetAnalysis {


    public static void main(String[] args) throws Exception {

        System.out.println("WEKA Test!");


        IClassifier tweetTest = new WekaClassifier();

        // Use the Classifier to evaluate a Tweet
        Tweet t = new Tweet();
        t.setContent("microsoft sucks bad :(");
        double[] fDistribution = tweetTest.classifyTweet(t);

        System.out.println("Evaluation of a String: " + t.getContent());
        System.out.println("probability of being positive: " + fDistribution[0]);
        System.out.println("probability of being negative: " + fDistribution[1]);

        // INFO:
        // Ignore the database errors, only important if we use a database for the tweets.

        // Preporcessing Test: Read the BIG Tweetfile and try to make an simple learning CSV File
        //CSVPreprocessingTrainingAndTestingData testPreprocessing = new CSVPreprocessingTrainingAndTestingData();
        //testPreprocessing.extractTheTweetContentIntoCSV();

        // TweetProvider provider = new TweetProvider();
        // List<Tweet> tweets = provider.getTweets("test", 20, new Date(114, 10, 18), new Date(114, 10, 20)).get();
        // System.out.println("Got " + tweets.size() + " tweets");
    }


}

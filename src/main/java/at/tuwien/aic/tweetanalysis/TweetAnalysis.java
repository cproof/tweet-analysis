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

import at.tuwien.aic.tweetanalysis.classifier.WekaClassifier;
import at.tuwien.aic.tweetanalysis.preprocessing.CSVPreprocessingTrainingAndTestingData;
import at.tuwien.aic.tweetanalysis.provider.TweetProvider;
import at.tuwien.aic.tweetanalysis.entities.Tweet;
import twitter4j.GeoLocation;

import java.util.Date;
import java.util.List;

/**
 * @author Group 1
 */
public class TweetAnalysis {


    public static void main(String[] args) throws Exception {

        System.out.println("WEKA Test!");

        WekaClassifier tweetTest = new WekaClassifier();
        tweetTest.trainClassifier("manualCreatedTrainingData.csv");
        tweetTest.evaluate("manualCreatedTestingData.csv");

        // Then use the classifier to evaluate tweets
        tweetTest.useClassifier();

        // INFO:
        // Ignore the database errors, only important if we use a database for the tweets.
        // Testing and Training Dataset from the Internet, we need our Testdata from Tuwel.


        // Preporcessing Test: Read the BIG Tweetfile and try to make an simple learning CSV File
        //CSVPreprocessingTrainingAndTestingData testPreprocessing = new CSVPreprocessingTrainingAndTestingData();
        //testPreprocessing.extractTheTweetContentIntoCSV();

        /* TweetProvider provider = new TweetProvider();
        List<Tweet> tweets = provider.getTweets("hi", 200, new Date(114, 10, 18), new Date(114, 10, 25), "de", new GeoLocation(48.209206, 16.372778), 10.0).get();
        System.out.println("\"hi\" query + date + lang + location got " + tweets.size() + " tweets");
        tweets = provider.getTweets("hi", 200).get();
        System.out.println("\"hi\" query got " + tweets.size() + " tweets"); */
    }


}

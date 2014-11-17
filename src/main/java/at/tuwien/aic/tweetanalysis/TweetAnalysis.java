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

/**
 * @author Group 1
 */
public class TweetAnalysis {


    public static void main(String[] args) throws Exception {

        //System.out.println("WEKA Test!");

        //WekaClassifier tweetTest = new WekaClassifier();
        //tweetTest.trainClassifier("training.csv");
        //tweetTest.evaluate("testing.csv");

        // Then use the classifier to evaluate tweets
        //tweetTest.useClassifier();

        // INFO:
        // Ignore the database errors, only important if we use a database for the tweets.
        // Testing and Training Dataset from the Internet, we need our Testdata from Tuwel.


        // Preporcessing Test: Read the BIG Tweetfile and try to make an simple learning CSV File
        System.out.println("Prepairing training and testing Data!");
        CSVPreprocessingTrainingAndTestingData testPreprocessing = new CSVPreprocessingTrainingAndTestingData();
        testPreprocessing.extractTheTweetContentIntoCSV();

    }


}

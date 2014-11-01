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

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Group 1
 */
public class TweetAnalysis {


    Instances trainingDataset = null;
    Instances testingDataset = null;
    private Classifier classifier;


    public static void main (String[] args) throws Exception {

        System.out.println("WEKA Test!");

        TweetAnalysis tweet = new TweetAnalysis();


        tweet.trainClassifier("training.csv");
        tweet.evaluate("testing.csv");

        // Then use the classifier to evaluate tweets
        tweet.useClassifier();

        // INFO:
        // Ignore the database errors, only important if we use a database for the tweets.
        // Testing and Training Dataset from the Internet, we need our Testdata from Tuwel.

    }

    /*
    * Read in a new Instance of training Sets
    * Saves the Results in the trainingDataset variable
    */
    private void getTrainingDataset(final String INPUT_FILENAME)
    {
        try {
            //reading the training dataset from CSV file
            CSVLoader trainingLoader = new CSVLoader();
            trainingLoader.setSource(new File(INPUT_FILENAME));
            trainingDataset = trainingLoader.getDataSet();
            trainingDataset.setClassIndex(trainingDataset.numAttributes() - 1);
        } catch(IOException ex)
        {
            System.out.println("Exception getTrainingDataset: " + ex.getMessage());
        }
    }

    /*
    * Read in a new Instance of testing Sets
    * Saves the Results in the testingDataset variable
    */
    private void getTestingDataset(final String INPUT_FILENAME)
    {
        try {
            //reading the training dataset from CSV file
            CSVLoader trainingLoader = new CSVLoader();
            trainingLoader.setSource(new File(INPUT_FILENAME));
            testingDataset = trainingLoader.getDataSet();
            testingDataset.setClassIndex(testingDataset.numAttributes() - 1);
        } catch(IOException ex)
        {
            System.out.println("Exception getTrainingDataset: " + ex.getMessage());
        }
    }

    /*
    * Train the Classifier with the training data set
    */
    public void trainClassifier(final String INPUT_FILENAME)
    {
        getTrainingDataset(INPUT_FILENAME);

        //In this case we use NaiveBayes Classifier.
        classifier = (Classifier)new NaiveBayes();

        try {
            //classifier training code
            classifier.buildClassifier(trainingDataset);
        } catch (Exception ex)
        {
            System.out.println("Exception training classifier: "  + ex.getMessage());
        }
    }

        /**
        *  testing the classifier
        * evaluate classifier and print some statistics
        */
        public void evaluate(final String INPUT_FILENAME) {

            getTestingDataset(INPUT_FILENAME);

            try {

                Evaluation eval = new Evaluation(trainingDataset);

                eval.evaluateModel(classifier, testingDataset);
                System.out.println(eval.toSummaryString("\nResults\n======\n", false));
                System.out.println(eval.toMatrixString());

            } catch (Exception ex) {
                System.out.println("Exception testing classifier: " + ex.getMessage());
            }
        }



        /*
        * Use the Classifier with the training data set
        */
        public void useClassifier()
        {
            try {
                double[] fDistribution = classifier.distributionForInstance(testingDataset.firstInstance());

                // Get the likelihood of each classes
                // fDistribution[0] is the probability of being “positive”
                // fDistribution[1] is the probability of being “negative”
                System.out.println("Evaluation of a String");
                System.out.println("probability of being positive: " + fDistribution[0]);
                System.out.println("probability of being negative: " + fDistribution[1]);

            } catch (Exception ex)
            {
                System.out.println("Exception using classifier: "  + ex.getMessage());
            }
        }



}

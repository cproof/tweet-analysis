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
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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

        //testClassifier();
//        testLiveData();
//        getLiveData();
        tryMergeTweetIntoDataset("this is a test with only one matching awesome word");

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
        //try (InputStream modelStream = TweetAnalysis.class.getResourceAsStream("/fff1.model")) {
        //    tweetTest = new WekaClassifier(modelStream);
        //}
        tweetTest = new WekaClassifier();

        // Use the Classifier to evaluate a Tweet
        Tweet t = new Tweet();
//        t.setContent("bad bad bad bad :(");
 //       t.setContent("#hate sad :( :(");
        t.setContent("happy joy :) :) #happy congratulations");
        //t.setContent("I love them, thank u mum !! <3 http://t.co/rMQEeRYhnT");

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


    private static void tryMergeTweetIntoDataset(String string_to_match_into) throws Exception {
        Instances original_instances = getARFFDataset("tttest.arff");
        System.out.println("Original Instances before add: \n" + original_instances);
        System.out.println("-------------------------------------");
        System.out.println("-------------------------------------");

        StringTokenizer my_tokenizer = new StringTokenizer(string_to_match_into, " ");

        //create instance with same attributes but only one empty instance
        Instances new_instances = new Instances(original_instances,1);
        Instance inst = new Instance(new_instances.numAttributes());
        inst.setDataset(new_instances);

        //set the values to a zero value without the nominal
        int j = 1;
        while(j<new_instances.numAttributes()) {
            inst.setValue(j, 0);
            j++;
        }

        //run throu the new string an match to the attributes: set the presence to 1
        while(my_tokenizer.hasMoreTokens()) {
            String tmp = my_tokenizer.nextToken();
            if(new_instances.attribute(tmp)!=null) //wenns die instance gibt
                inst.setValue(new_instances.attribute(tmp), 1);
        }
        new_instances.add(inst);

        //to sparse instance
        NonSparseToSparse toSparseeee = new NonSparseToSparse();
        toSparseeee.setInputFormat(new_instances);
        Instances sparseInstances = Filter.useFilter(new_instances, toSparseeee);

        //add to original instances
        original_instances.add(sparseInstances.firstInstance());

        System.out.println("instances with the added string: \n" + original_instances);

        //ArffSaver blub = new ArffSaver();
        //blub.setInstances(new_instances);
        //blub.setFile(new File("new.arff"));
        //blub.writeBatch();

    }

    private static Instances getARFFDataset(final String INPUT_FILE_DATASET) {
        Instances dataset = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_DATASET));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            dataset = arff.getData();
            dataset.setClassIndex(1);
        } catch (IOException ex) {
            System.err.println("Exception in getARFFDataset: " + ex.getMessage());
        }

        return dataset;
    }


}
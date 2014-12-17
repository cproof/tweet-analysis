package at.tuwien.aic.tweetanalysis.classifier;

import at.tuwien.aic.tweetanalysis.entities.Tweet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The Classifier for Tweets
 *
 * @author Group 1
 */
public interface IClassifier {

    /**
     * Classifies the given tweet
     *
     * x[0] is the probability of being “positive”
     * x[1] is the probability of being “negative”
     *
     * @param tweet the preprocessed Tweet
     * @return the likelihood of each class
     */
    public double[] classifyTweet(Tweet tweet);

    /**
     * Save the actual Model of the Classifier to the Filesystem
     *
     * @param modelName the Path to save the Model
     */
    public void saveModel(String modelName);

    /**
     * Load a Model into the Classifier from the Filesystem
     * @param modelName the Path to the Model
     * @param instancesFileName
     */
    public void loadModel(String modelName, String instancesFileName);

    /**
     * Load a Model into the Classifier from the Filesystem
     *
     * @param modelStream the Path to the Stream
     * @param instancesStream
     */
    void loadModel(InputStream modelStream, InputStream instancesStream);

    /**
     * Test the Classifier against preprocessed Tweets and print out some statistics
     *
     */
    public void testClassifierAgainstPreprocessedTweets(List<Tweet> tweets);
}

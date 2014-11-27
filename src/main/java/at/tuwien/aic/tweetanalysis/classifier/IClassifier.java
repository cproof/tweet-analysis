package at.tuwien.aic.tweetanalysis.classifier;

import at.tuwien.aic.tweetanalysis.entities.Tweet;

import java.io.InputStream;

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
     * @param tweet
     * @return the liklehood of each class
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
     *
     * @param modelName the Path to the Model
     */
    public void loadModel(String modelName);


    void loadModel(InputStream modelStream);
}

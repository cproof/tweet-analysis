package at.tuwien.aic.tweetanalysis.classifier;

import at.tuwien.aic.tweetanalysis.entities.Tweet;

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


}

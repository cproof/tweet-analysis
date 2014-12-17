package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.List;

/**
 *
 * @author Thomas
 */
public interface ITweetPreprocessor {
    
    /**
     * Preprocess the given tweet
     * @param tweet
     * @return the reference to the given tweet
     */
    @Deprecated
    public Tweet preprocess(Tweet tweet);
    
    /**
     * Preprocess all tweets given in the list
     * @param tweets
     * @return the reference to the given list
     */
    public List<Tweet> preprocess(List<Tweet> tweets);
}

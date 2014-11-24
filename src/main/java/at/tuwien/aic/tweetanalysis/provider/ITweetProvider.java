package at.tuwien.aic.tweetanalysis.provider;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * @author Thomas
 */
public interface ITweetProvider {
    

    /**
     * Get (count) tweets from the selected provider
     * retweeted tweets will be excluded
     * @param searchTerm search term to be used when querying the API
     * @param count max number of tweets
     * @param beginTime if not null: tweet has to have creation date after this point
     * @param endTime if not null: tweet has to have creation date before this point
     * @return List of matching tweets
     */
    public Future<List<Tweet>> getTweets (String searchTerm, int count, Date beginTime, Date endTime);
    
}

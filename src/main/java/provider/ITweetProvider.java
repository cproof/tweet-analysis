package provider;

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
     * @param count max number of tweets
     * @param minFavoriteCount min times a tweet has to be favorited
     * @param minRetweetCount min times a tweet has to be retweeted
     * @param beginTime if not null: tweet has to have creation date after this point
     * @param endTime if not null: tweet has to have creation date before this point
     * @return List of matching tweets
     */
    public Future<List<Tweet>> getTweets (int count, int minFavoriteCount, int minRetweetCount, Date beginTime, Date endTime);
    
}

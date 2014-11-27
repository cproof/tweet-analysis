package at.tuwien.aic.tweetanalysis.provider;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import twitter4j.GeoLocation;

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
     * @param searchTerm search term to be used when querying the API
     * @param count max number of tweets
     * @param beginTime if not null: tweet has to have creation date after this point
     * @param endTime if not null: tweet has to have creation date before this point
     * @param language if not null: tweet has to be written in this ISO language code
     * @param location if not null: tweet has to be written in this location, radius is also needed
     * @param radius if not null: tweet location radius in km
     * @return List of matching tweets */
    public Future<List<Tweet>> getTweets (String searchTerm, int count, Date beginTime, Date endTime, String language, GeoLocation location, Double radius);

    /**
     * Get (count) tweets from the selected provider
     * @param searchTerm search term to be used when querying the API
     * @param count max number of tweets
     * @return List of matching tweets */
    public Future<List<Tweet>> getTweets (String searchTerm, int count);

}

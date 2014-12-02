package at.tuwien.aic.tweetanalysis.provider;

import at.tuwien.aic.tweetanalysis.Utils;
import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.slf4j.*;
import twitter4j.*;
import twitter4j.LoggerFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class TweetProvider implements ITweetProvider {
    public static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TweetProvider.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Future<List<Tweet>> getTweets(final String searchTerm, final int count, final Date beginTime, final Date endTime, final String language, final GeoLocation location, final Double radius) {

        Callable<List<Tweet>> task = new Callable<List<Tweet>>() {
            @Override
            public List<Tweet> call() throws IOException, TwitterException {
                List<Tweet> tweets = new LinkedList<>();
                ITwitterCredentials creds = new TwitterCredentials();
                int fetched = 0;
                int pages_done = 0;
                final int MAX_PAGES_TO_TRY = 1000;

                try {

                    ConfigurationBuilder cb = new ConfigurationBuilder();
                    cb.setOAuthConsumerKey(creds.getConsumerKey())
                            .setOAuthConsumerSecret(creds.getConsumerSecret())
                            .setOAuthAccessToken(creds.getAccessToken())
                            .setOAuthAccessTokenSecret(creds.getAccessTokenSecret());
                    TwitterFactory tf = new TwitterFactory(cb.build());
                    Twitter twitter = tf.getInstance();

                    Query query = new Query(searchTerm);
                    query.setResultType(Query.ResultType.recent);
                    if (beginTime != null) { query.setSince(dateFormat.format(beginTime)); }
                    if (endTime != null) { query.setUntil(dateFormat.format(endTime)); }
                    if (language != null && Arrays.asList(Locale.getISOLanguages()).contains(language)) {
                        query.setLang(language);
                    } else {
                        /* default to english */
                        query.setLang("en");
                    }
                    if (location != null && radius != null && radius > 0) { query.setGeoCode(location, radius, Query.Unit.km); }


                    QueryResult result;

                    do {
                        result = twitter.search(query);
                        fetched = addTweetsToList(result, tweets, fetched, count);
                        pages_done++;
                    } while (pages_done < MAX_PAGES_TO_TRY &&
                            fetched < count &&
                            (query = result.nextQuery()) != null);

                } catch(TwitterException e) {
                    log.warn("TwitterException: " + e.getMessage());

                    if(e.exceededRateLimitation()) {
                        RateLimitStatus status = e.getRateLimitStatus();
                        System.out.println("This application exceeded the Twitter API rate limiting." + (status != null ? " You have to wait ~" + status.getSecondsUntilReset() + " seconds until the next request can be made." : ""));
                    }
                }

                return tweets;
            }

            private int addTweetsToList(QueryResult result, List<Tweet> tweets, int fetched, int count) {
                for (Status status : result.getTweets()) {
                    // do not add retweets and mentions
                    String text = status.getText();
                    if (status.getRetweetedStatus() != null ||
                            text.contains(" RT ")) continue;

                    // todo: replace search term
//                    text = text.replace(searchTerm, "SEARCH_TERM");

                    List<String> hashtags = new LinkedList<>();
                    List<String> urls = new LinkedList<>();
                    List<String> mentionedUsers = new LinkedList<>();

                    for (HashtagEntity entity : status.getHashtagEntities()) {
                        hashtags.add(entity.getText());
                    }

                    for (URLEntity entity : status.getURLEntities()) {
                        urls.add(entity.getURL());
                    }

                    for (UserMentionEntity entity : status.getUserMentionEntities()) {
                        mentionedUsers.add(entity.getScreenName());
                    }

                    tweets.add(new Tweet(
                            status.getId(),
                            text,
                            hashtags,
                            urls,
                            mentionedUsers,
                            status.getUser().getScreenName(),
                            status.getLang(),
                            searchTerm, status.getCreatedAt(),
                            status.getRetweetCount(),
                            status.getFavoriteCount(),
                            status.getGeoLocation()
                    ));

                    fetched++;

                    if(fetched >= count) break;

                }

                return fetched;
            }



        };


        return executor.submit(task);
    }

    @Override
    public Future<List<Tweet>> getTweets(final String searchTerm, final int count) {
        return getTweets(searchTerm, count, null, null, null, null, null);
    }

    @Override
    public void shutdown() {
        Utils.shutdownAndAwaitTermination(executor);
    }

}

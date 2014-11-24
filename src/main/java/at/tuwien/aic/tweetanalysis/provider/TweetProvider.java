package at.tuwien.aic.tweetanalysis.provider;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TweetProvider implements ITweetProvider {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Future<List<Tweet>> getTweets(final String searchTerm, final int count, final Date beginTime, final Date endTime) {

        Callable<List<Tweet>> task = new Callable<List<Tweet>>() {
            @Override
            public List<Tweet> call() throws IOException, TwitterException {
                List<Tweet> tweets = new LinkedList<>();
//                ITwitterCredentials creds = new TwitterCredentials();
                ITwitterCredentials creds = null; // todo: change back if implemented

                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setOAuthConsumerKey(creds.getConsumerKey())
                    .setOAuthConsumerSecret(creds.getConsumerSecret())
                    .setOAuthAccessToken(creds.getAccessToken())
                    .setOAuthAccessTokenSecret(creds.getAccessTokenSecret());
                TwitterFactory tf = new TwitterFactory(cb.build());
                Twitter twitter = tf.getInstance();

                Query query = new Query(searchTerm);
                query.setCount(count);
                if(beginTime != null) { query.setSince(dateFormat.format(beginTime)); }
                if(endTime != null) { query.setUntil(dateFormat.format(endTime)); }

                QueryResult result = twitter.search(query);

                for (Status status : result.getTweets()) {
                    List<String> hashtags = new LinkedList<>();
                    List<String> urls = new LinkedList<>();
                    List<String> mentionedUsers = new LinkedList<>();

                    for (HashtagEntity entity : status.getHashtagEntities()) {
                        hashtags.add(entity.getText());
                    }

                    for (URLEntity entity : status.getURLEntities()) {
                        hashtags.add(entity.getURL());
                    }

                    for (UserMentionEntity entity : status.getUserMentionEntities()) {
                        mentionedUsers.add(entity.getScreenName());
                    }

                    tweets.add(new Tweet(
                            status.getId(),
                            status.getText(),
                            hashtags,
                            urls,
                            mentionedUsers,
                            status.getUser().getScreenName(),
                            status.getLang(),
                            status.getCreatedAt(),
                            status.getRetweetCount(),
                            status.getFavoriteCount(),
                            status.getGeoLocation()
                    ));

                }

                return tweets;
            }

        };


        return executor.submit(task);
    }

}

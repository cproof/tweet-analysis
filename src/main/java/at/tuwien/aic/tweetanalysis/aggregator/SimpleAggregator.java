package at.tuwien.aic.tweetanalysis.aggregator;

import at.tuwien.aic.tweetanalysis.entities.ClassifiedTweet;
import at.tuwien.aic.tweetanalysis.entities.Tweet;

import java.util.List;

public class SimpleAggregator {

    private List<ClassifiedTweet> tweets;
    private int positive;
    private int negative;

    private static final int NEG = 0;
    private static final int POS = 1;

    // weight of a favorite, at 0.25 this means for every 4 favs the tweet counts as an extra positive/negative
    private static final double WEIGHT_FAV = 0.25;
    // weight of a retweet, at 0.5 this means for every 2 RTs the tweet counts as an extra positive/negative
    private static final double WEIGHT_RETWEET = 0.5;

    // threshold for distinguishing between positive and negative tweets, if the classification is too close (<0.1) the tweet will be discarded
    private static final double THRESHOLD = 0.1;

    public SimpleAggregator() {
        positive = 0;
        negative = 0;
    }

    public double calculate(List<ClassifiedTweet> tweets) {
        for(ClassifiedTweet tweet : tweets) {
            double rts = tweet.tweet.getRetweetCount();
            double favs = tweet.tweet.getFavoriteCount();

            if(tweet.fDistribution[POS] - tweet.fDistribution[NEG] > THRESHOLD) {           // tweet is positive
                positive++;
                if(rts * WEIGHT_RETWEET > 1) {
                    positive += rts * WEIGHT_RETWEET;
                }
                if(favs * WEIGHT_FAV > 1) {
                    positive += favs * WEIGHT_FAV;
                }
            } else if(tweet.fDistribution[NEG] - tweet.fDistribution[POS] > THRESHOLD){     // tweet is negative
                negative++;
                if(rts * WEIGHT_RETWEET > 1) {
                    negative += rts * WEIGHT_RETWEET;
                }
                if(favs * WEIGHT_FAV > 1) {
                    negative += favs * WEIGHT_FAV;
                }
            } else {                                                                        // threshold is not met, discard tweet
                continue;
            }
        }

        double sum = positive + negative;

        if(sum > 0) {
            return (double)positive / sum;
        } else {
            return 1;
        }
    }

    public static double weight(Tweet tweet) {
        double rts = tweet.getRetweetCount();
        double favs = tweet.getFavoriteCount();
        double result = 1.0;
        if (rts * WEIGHT_RETWEET > 1) {
            result += rts * WEIGHT_RETWEET;
        }
        if (favs * WEIGHT_FAV > 1) {
            result += favs * WEIGHT_FAV;
        }
        return result;
    }
}

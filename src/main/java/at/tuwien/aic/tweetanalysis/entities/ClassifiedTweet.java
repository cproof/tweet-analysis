package at.tuwien.aic.tweetanalysis.entities;

public class ClassifiedTweet {
    public Tweet tweet;
    public double[] fDistribution;

    public ClassifiedTweet(Tweet tweet, double[] fDistribution) {
        this.tweet = tweet;
        this.fDistribution = fDistribution;
    }
}

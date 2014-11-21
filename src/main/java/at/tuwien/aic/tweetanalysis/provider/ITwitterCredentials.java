package at.tuwien.aic.tweetanalysis.provider;

public interface ITwitterCredentials {
    public String getConsumerKey();
    public String getConsumerSecret();
    public String getAccessToken();
    public String getAccessTokenSecret();
}

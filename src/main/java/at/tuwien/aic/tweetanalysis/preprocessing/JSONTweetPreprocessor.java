package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas
 */
public class JSONTweetPreprocessor implements ITweetPreprocessor {
    
    
    @Override
    public Tweet preprocess(Tweet tweet) {
        String content = tweet.getContent();
        content = replaceSmilies(content);
        
        tweet.setContent(content);
        return tweet;
    }

    private List<String> getHashtags(String content) {
        List<String> hashtags = new LinkedList<>();
        
        
        return hashtags;
    }
    
    private List<String> getUrls(String content) {
        List<String> urls = new LinkedList<>();
        
        
        return urls;
    }
    
    private String replaceSmilies(String input) {
        input = input.replaceAll(":\\)|:-\\)", "POSITIVESMILE");
        input = input.replaceAll(":\\(|:-\\(", "NEGATIVESMILE");

        return input;
    }
    
    
    @Override
    public List<Tweet> proprocess(List<Tweet> tweets) {
        for (Tweet t : tweets) {
            this.preprocess(t);
        }
        
        return tweets;
    }
    
}

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Thomas
 */
public class NaiveTweetPreprocessor implements ITweetPreprocessor {
    private final Pattern hashtagPattern = Pattern.compile("#[0-9a-zA-Z\\-_]*");
    private final Pattern userPattern = Pattern.compile("@[0-9a-zA-Z\\-_]*");
    //private final Pattern urlPattern = Pattern.compile("(http://){0,1}(www\\.){0,1}[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*");
    private final Pattern urlPattern = Pattern.compile("[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*");
    
    @Override
    public Tweet preprocess(Tweet tweet) {
        String content = tweet.getContent();
        
        content = replaceSmilies(content);
        
        
               
        //remove hash symbol from the content
        //for equal treatment of tweets using hashtags
        //and tweets that use none
        tweet.setHashtags(getHashtags(content));
        content = content.replace("#", "");
        
        //same with other users
        tweet.setMentionedUsers(this.getUsers(content));
        //@TODO: Experiment if it makes more sense to
        //  1 leave users as they are, including the @
        //  2 remove users completely including the name
        content = content.replace("@", "");
        
        //normalize urls
        content = content.replace("www.", "").replace("http://", "").replace("https://", "");
        tweet.setUrls(getUrls(content));
        
        tweet.setContent(content);
        return tweet;
    }

    /**
     * Get Hashtags from the tweet, put them in the list
     * @param content text content of the tweet
     * @return List of Hashtags
     */
    private List<String> getHashtags(String content) {
        List<String> hashtags = new LinkedList<>();
        
        Matcher m = hashtagPattern.matcher(content);
        while(m.find()) {
            //add hashtag to the list without the hash symbol
            hashtags.add(m.group().substring(1));
        }
        
        return hashtags;
    }
    
    /**
     * Get the users mentioned in a the tweet, put them in the list
     * @param content text content of the tweet
     * @return List of mentioned Users
     */
    private List<String> getUsers(String content) {
        List<String> users = new LinkedList<>();
        
        Matcher m = userPattern.matcher(content);
        while(m.find()) {
            //add hashtag to the list without the hash symbol
            users.add(m.group().substring(1));
        }
        
        return users;
    }
    
    /**
     * Get List of urls from the tweet
     * @param content text content of the tweet
     * @return List of Urls
     */
    private List<String> getUrls(String content) {
        List<String> urls = new LinkedList<>();
        
        Matcher m = urlPattern.matcher(content);
        while(m.find()) {
            //add url to the list without the hash symbol
            String url = m.group();
            url = url.replace("www.", "").replace("http://", "").replace("https://", "");
            urls.add(url);
        }

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

/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas
 */
public class NaiveTweetPreprocessorTest {
    private ITweetPreprocessor preprocessor;
    
    public NaiveTweetPreprocessorTest() {
        
    }
    
    
    @Before
    public void setUp() {
        this.preprocessor = new NaiveTweetPreprocessor();
    }


    /**
     * Test of preprocess method, of class NaiveTweetPreprocessor.
     */
    @Test
    public void testPreprocessSmilies() {
        Tweet tweet = new Tweet();
        
        tweet.setContent("This is a tweet containing smilies like :)");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getContent().contains("POSITIVESMILE"));
        
        tweet.setContent("This is a tweet containing smilies like :-)");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getContent().contains("POSITIVESMILE"));
        
        tweet.setContent("This is a tweet :( containing smilies like :( :(");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
        assertFalse(tweet.getContent().contains(":("));
        
        tweet.setContent("This is a tweet containing smilies like :-(");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
        
        tweet.setContent("This is a tweet containing smilies like :-( or :-)");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
        assertTrue(tweet.getContent().contains("POSITIVESMILE"));
    }


    @Test
    public void testGetUrls() {
        Tweet tweet = new Tweet();
        
        tweet.setContent("This tweet contains http://bit.ly/asfd as an url");
        this.preprocessor.preprocess(tweet);
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0),"bit.ly/asfd");
        
        tweet.setContent("This tweet contains https://bit.ly/asfd as an url");
        this.preprocessor.preprocess(tweet);
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0),"bit.ly/asfd");
        
        tweet.setContent("This tweet contains http://www.bit.ly/asfd as an url");
        this.preprocessor.preprocess(tweet);
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0),"bit.ly/asfd");
        
        tweet.setContent("This tweet contains bit.ly/asfd as an url");
        this.preprocessor.preprocess(tweet);
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0),"bit.ly/asfd");
    }
    
    @Test
    public void testGetHashtags() {
        Tweet tweet = new Tweet();
        
        tweet.setContent("This tweet contains a #hashtag!");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getHashtags().contains("hashtag"));
        assertEquals(tweet.getHashtags().size(), 1);
        assertEquals(tweet.getContent().indexOf("#"),-1);
        
        tweet.setContent("This tweet contains #two2 #hashtags!");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getHashtags().contains("two2"));
        assertTrue(tweet.getHashtags().contains("hashtags"));
        assertEquals(tweet.getContent().indexOf("#"),-1);
        assertEquals(tweet.getHashtags().size(), 2);
    }
    
    @Test
    public void testGetAuthors() {
        Tweet tweet = new Tweet();
        
        tweet.setContent("This tweet contains an @user!");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getMentionedUsers().contains("user"));
        assertEquals(tweet.getMentionedUsers().size(), 1);
        assertEquals(tweet.getContent().indexOf("@"),-1);
        
        tweet.setContent("This tweet contains @two2 @users!");
        this.preprocessor.preprocess(tweet);
        assertTrue(tweet.getMentionedUsers().contains("two2"));
        assertTrue(tweet.getMentionedUsers().contains("users"));
        assertEquals(tweet.getContent().indexOf("@"),-1);
        assertEquals(tweet.getMentionedUsers().size(), 2);
    }
    
}

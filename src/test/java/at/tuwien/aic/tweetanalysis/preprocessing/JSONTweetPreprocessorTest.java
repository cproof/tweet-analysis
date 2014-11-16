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
public class JSONTweetPreprocessorTest {
    private ITweetPreprocessor preprocessor;
    
    public JSONTweetPreprocessorTest() {
        
    }
    
    
    @Before
    public void setUp() {
        this.preprocessor = new JSONTweetPreprocessor();
    }


    /**
     * Test of preprocess method, of class JSONTweetPreprocessor.
     */
    @Test
    public void testPreprocessSmilies() {
        System.out.println("preprocess");
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


    
}

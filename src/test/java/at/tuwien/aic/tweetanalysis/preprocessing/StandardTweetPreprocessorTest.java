/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas
 */
public class StandardTweetPreprocessorTest {
    
    private ITweetPreprocessor preprocessor;
    
    public StandardTweetPreprocessorTest() {
    }

    @Before
    public void setUp() {
        this.preprocessor = new StandardTweetPreprocessor();
    }

    /**
     * Test of preprocess method, of class StandardTweetPreprocessor.
     */
    @Test
    public void testPreprocess_List() {
        List<Tweet> tweets = new LinkedList<>();
        Tweet t = new Tweet();
        t.setContent("this is some english tweet! #english");
        tweets.add(t);

        t = new Tweet();
        t.setContent("Dies ist ein deutscher Tweet! #deutsch");
        tweets.add(t);

        this.preprocessor.preprocess(tweets);

        //lang
        assertEquals(tweets.get(0).getLanguage(), "en");
        assertEquals(tweets.get(1).getLanguage(), "de");
        
        //naive filter
        assertEquals(tweets.get(0).getHashtags().get(0), "english");
        assertEquals(tweets.get(1).getHashtags().get(0), "deutsch");
        assertEquals(tweets.get(1).getHashtags().size(), 1);
    }

}

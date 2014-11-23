/*
 */

package at.tuwien.aic.tweetanalysis.provider;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas
 */
public class JSONTweetProviderTest {
    private static final String file = "E:\\tmp\\tweets.txt";
    
    
    private ITweetProvider tp;
    
    public JSONTweetProviderTest() {
    }
   
    @Before
    public void setUp() throws IOException {
        this.tp = new JSONTweetProvider(file);
    }
    
    /**
     * Test of getTweets method, of classJSONTweetProviderr.
     */
    @Test
    public void testGetTweets() throws InterruptedException, ExecutionException {
        System.out.println("getTweets");
        int size = 100;
        Future<List<Tweet>> f = this.tp.getTweets(size, 0, 0, null, null);
        
        List<Tweet> list = f.get();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.size() <= size);
        System.out.println("got " + list.size() + " tweets");
    }
    
}
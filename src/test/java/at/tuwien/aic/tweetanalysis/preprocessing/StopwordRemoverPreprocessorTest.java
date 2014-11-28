package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas
 */
public class StopwordRemoverPreprocessorTest {
    private ITweetPreprocessor tweetPreprocessor;
    
    
    public StopwordRemoverPreprocessorTest() {
        tweetPreprocessor = new StopwordRemoverPreprocessor();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of preprocess method, of class StopwordRemoverPreprocessor.
     */
    @Test
    public void testPreprocess_List() {
        Tweet t = new Tweet();
        t.setContent("This is some\t sample english\r\ntext, containing\na stopword or two!?");
        List<Tweet> l = new LinkedList<>();
        l.add(t);
        
        this.tweetPreprocessor.preprocess(l);
        
        assertEquals(t.getContent(), "sample english text containing stopword two");
    }
    
}

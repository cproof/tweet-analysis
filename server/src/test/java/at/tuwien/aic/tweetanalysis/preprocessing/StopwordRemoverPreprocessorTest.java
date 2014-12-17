package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Thomas
 */
public class StopwordRemoverPreprocessorTest {
    private ITweetPreprocessor tweetPreprocessor;
    private Tweet tweet;


    public StopwordRemoverPreprocessorTest() {
        tweetPreprocessor = new StopwordRemoverPreprocessor();
    }

    @Before
    public void setUp() {
        tweet = new Tweet();
    }
    
    @After
    public void tearDown() {
    }

    private void setContentAndProcess(String content) {
        tweet.setContent(content);
        tweetPreprocessor.preprocess(tweet);
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

    @Test
    public void testStopWords() {
        setContentAndProcess("not yet, we hopefully will make one soon :)");
        assertThat(tweet.getContent(), equalTo("yet hopefully will make one soon"));


        setContentAndProcess("not not-yet, we hopefully will make one soon :)");
        assertThat(tweet.getContent(), equalTo("yet hopefully will make one soon"));

    }
    
}

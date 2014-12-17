/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Thomas
 */
public class StandardTweetPreprocessorTest {
    
    private ITweetPreprocessor preprocessor;
    private Tweet tweet;
    private ArrayList<Tweet> tweets;

    public StandardTweetPreprocessorTest() {
    }

    @Before
    public void setUp() {
        preprocessor = new StandardTweetPreprocessor();
        tweet = new Tweet();
        tweets = new ArrayList<>(1);
        tweets.add(tweet);
    }

    private void setContentAndProcess(String content) {
        tweet.setContent(content);
        preprocessor.preprocess(tweets);
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

        t = new Tweet();
        t.setContent("Just saw the new #Transformers movie. I didn't like it, it sucked soooooo hard :-(");
        tweets.add(t);
        
        this.preprocessor.preprocess(tweets);

        //lang
        assertEquals(tweets.get(0).getLanguage(), "en");
        assertEquals(tweets.get(1).getLanguage(), "de");
        
        //naive filter
        assertEquals(tweets.get(0).getHashtags().get(0), "english");
        assertEquals(tweets.get(1).getHashtags().get(0), "deutsch");
        assertEquals(tweets.get(1).getHashtags().size(), 1);
        
        System.out.println(t);
    }

    @Test
    public void negationsTest() {
        setContentAndProcess("@indieboyash not yet, we hopefully will make one soon :)");
        assertThat(tweet.getContent(), IsEqual.equalTo("MENTION not_yet hope will make one soon"));

        setContentAndProcess("I don't think I've ever been so pissed off before.");
        assertThat(tweet.getContent(), IsEqual.equalTo("not_think ever piss"));
    }

    @Test
    public void contractionsTest() {
        setContentAndProcess("I've never be so pissed off in my life !");
        assertThat(tweet.getContent(), IsEqual.equalTo("never piss life"));
    }

}

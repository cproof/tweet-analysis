/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Thomas
 */
public class SnowballPreprocessorTest {
    private ITweetPreprocessor preprocessor;
    private Tweet tweet;


    public SnowballPreprocessorTest() {
    }
    
    @Before
    public void setUp() {
        preprocessor = new SnowballPreprocessor();
        tweet = new Tweet();
    }
    
    @After
    public void tearDown() {
    }

    private void setContentAndProcess(String content) {
        tweet.setContent(content);
        tweet.setLanguage("en");
        preprocessor.preprocess(tweet);
    }

    /**
     * Test of preprocess method, of class SnowballPreprocessor.
     */
    @Test
    public void testPreprocess_List() {
        List<Tweet> tweets = new LinkedList<>();
        Tweet t = new Tweet();
        t.setContent("I was just playing outside yesterday");
        t.setLanguage("en");
        tweets.add(t);
        
        Tweet t2 = new Tweet();
        t2.setContent("Ich spielte gemeinsam mit meinen Freunden gestern eine lange Schachpartie");
        t2.setLanguage("de");
        tweets.add(t2);
        
        this.preprocessor.preprocess(tweets);
        
        assertEquals(t.getContent(),"I was just play outsid yesterday");
        assertEquals(t2.getContent(),"Ich spielt gemeinsam mit mein Freund gest ein lang Schachparti");
    }
    
    @Test
    public void testPreprocess_ignoreSpecials() {
        String special = "I like POSITIVESMILE mots bit.ly/asdf";
        
        List<Tweet> tweets = new LinkedList<>();
        Tweet t = new Tweet();
        t.setContent(special);
        t.setLanguage("en");
        t.getMentionedUsers().add("mots");
        t.getHashtags().add("like");
        t.getUrls().add("bit.ly/asdf");
        
        
        
        tweets.add(t);

        this.preprocessor.preprocess(tweets);

        assertEquals(t.getContent(), special);
    }

    @Test
    public void testStemming() {
        setContentAndProcess("this is not not-like");
        assertThat(tweet.getContent(), IsEqual.equalTo("this is not not-lik"));
    }
    
}

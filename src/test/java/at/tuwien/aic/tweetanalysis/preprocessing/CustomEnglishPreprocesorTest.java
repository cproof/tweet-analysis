/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.List;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Thomas
 */
public class CustomEnglishPreprocesorTest {
    
    private final ITweetPreprocessor preprocessor;
    private Tweet tweet;
    
    public CustomEnglishPreprocesorTest() {
        this.preprocessor = new CustomEnglishPreprocesor();
        this.tweet = new Tweet();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    private void setContentAndProcess(String content) {
        tweet.setContent(content);
        preprocessor.preprocess(tweet);
    }
    
    @Test
    public void testHandleNegations() {
        setContentAndProcess("not cool bro");
        assertThat(tweet.getContent(), equalTo("not not-cool bro"));

        setContentAndProcess("wow not cool");
        assertThat(tweet.getContent(), equalTo("wow not not-cool"));

        setContentAndProcess("like not");
        assertThat(tweet.getContent(), equalTo("not-like not"));

        setContentAndProcess("! not !");
        assertThat(tweet.getContent(), equalTo("! not !"));

        setContentAndProcess(" not ");
        assertThat(tweet.getContent(), equalTo("not"));

        setContentAndProcess("don't like it");
        assertThat(tweet.getContent(), equalTo("dont not-like it"));

        setContentAndProcess("no likely");
        assertThat(tweet.getContent(), equalTo("no not-likely"));

        setContentAndProcess("not not cool");
        assertThat(tweet.getContent(), equalTo("not not not-cool"));

        setContentAndProcess("i don't");
        assertThat(tweet.getContent(), equalTo("not-i dont")); // not really optimal
    }

    
}

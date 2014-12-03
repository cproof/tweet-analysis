/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

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
        assertThat(tweet.getContent(), equalTo("not not_cool bro"));

        setContentAndProcess("wow not cool");
        assertThat(tweet.getContent(), equalTo("wow not not_cool"));

        setContentAndProcess("like not");
        assertThat(tweet.getContent(), equalTo("not_like not"));

        setContentAndProcess("! not !");
        assertThat(tweet.getContent(), equalTo("! not !"));

        setContentAndProcess(" not ");
        assertThat(tweet.getContent(), equalTo("not"));

        setContentAndProcess("don't like it");
        assertThat(tweet.getContent(), equalTo("do not not_like it"));

        setContentAndProcess("no likely");
        assertThat(tweet.getContent(), equalTo("no not_likely"));

        setContentAndProcess("not not cool");
        assertThat(tweet.getContent(), equalTo("not not not_cool"));

        setContentAndProcess("i don't like that");
        assertThat(tweet.getContent(), equalTo("i do not not_like that")); // not really optimal

        setContentAndProcess("not yet, we hopefully will make one soon :)");
        assertThat(tweet.getContent(), equalTo("not not_yet, we hopefully will make one soon :)"));
    }

    @Test
    public void testContractions() {
        setContentAndProcess("i'm good");
        assertThat(tweet.getContent(), equalTo("i am good"));

    }

    @Test
    public void testAbbreviations() {
        setContentAndProcess("omfg");
        assertThat(tweet.getContent(), equalTo("oh my fucking god"));

        setContentAndProcess("omg");
        assertThat(tweet.getContent(), equalTo("oh my god"));

        setContentAndProcess("wtf");
        assertThat(tweet.getContent(), equalTo("what the fuck"));

        setContentAndProcess("omfg");
        assertThat(tweet.getContent(), equalTo("oh my fucking god"));

        setContentAndProcess("wth!?");
        assertThat(tweet.getContent(), equalTo("what the hell!?"));

        setContentAndProcess("yeah ftw");
        assertThat(tweet.getContent(), equalTo("yeah for the win"));

        setContentAndProcess("great, thx!");
        assertThat(tweet.getContent(), equalTo("great, thanks!"));
    }

    @Test
    public void testAbbreviations_notReplacingInsideWords() {
        setContentAndProcess("womgi");
        assertThat(tweet.getContent(), equalTo("womgi"));

    }

    @Test
    public void testFailedContractions() {
        setContentAndProcess("I don't think i've ever been so pissed off before.");
        assertThat(tweet.getContent(), equalTo("I do not not_think i have ever been so pissed off before."));
    }

    
}

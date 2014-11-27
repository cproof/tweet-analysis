/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.junit.Before;
import org.junit.Test;

import static at.tuwien.aic.tweetanalysis.preprocessing.NaiveTweetPreprocessor.NEGATIVE_HASHTAG;
import static at.tuwien.aic.tweetanalysis.preprocessing.NaiveTweetPreprocessor.POSITIVE_HASHTAG;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 *
 */
public class NaiveTweetPreprocessorTest {
    private NaiveTweetPreprocessor preprocessor;
    private Tweet tweet;

    public NaiveTweetPreprocessorTest() {

    }

    @Before
    public void setUp() {
        preprocessor = new NaiveTweetPreprocessor();
        tweet = new Tweet();
    }

    private void setContentAndProcess(String content) {
        tweet.setContent(content);
        preprocessor.preprocess(tweet);
    }

    @Test
    public void testNormaliseSpaces() {
        setContentAndProcess("this contains spaces    ! too much  yes");
        assertThat(tweet.getContent(), equalTo("this contains spaces ! too much yes"));

        setContentAndProcess("    what       is     happening                 !             ");
        assertThat(tweet.getContent(), equalTo("what is happening !"));
    }

    @Test
    public void testNoMentions() {
        setContentAndProcess("@me is so cool!");
        assertThat(tweet.getContent(), equalTo("is so cool!"));

        setContentAndProcess("@me is so cool @yeah!");
        assertThat(tweet.getContent(), equalTo("is so cool !"));

        setContentAndProcess("@me@and@not@you@go@away content");
        assertThat(tweet.getContent(), equalTo("content"));
    }

    /**
     * Test of preprocess method, of class NaiveTweetPreprocessor.
     */
    @Test
    public void testPreprocessSmilies() {
        setContentAndProcess("This is a tweet containing smilies like :)");
        assertTrue(tweet.getContent().contains("POSITIVESMILE"));

        setContentAndProcess("This is a tweet containing smilies like :-)");
        assertTrue(tweet.getContent().contains("POSITIVESMILE"));

        setContentAndProcess("This is a tweet :( containing smilies like :( :(");
        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
        assertFalse(tweet.getContent().contains(":("));

        setContentAndProcess("This is a tweet containing smilies like :-(");
        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));

        setContentAndProcess("This is a tweet containing smilies like :-( or :-)");
        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
        assertTrue(tweet.getContent().contains("POSITIVESMILE"));
    }


    @Test
    public void testGetUrls() {
        setContentAndProcess("This tweet contains http://bit.ly/asfd as an url");
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0), "bit.ly/asfd");

        setContentAndProcess("This tweet contains https://bit.ly/asfd as an url");
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0), "bit.ly/asfd");

        setContentAndProcess("This tweet contains http://www.bit.ly/asfd as an url");
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0), "bit.ly/asfd");

        setContentAndProcess("This tweet contains bit.ly/asfd as an url");
        assertEquals(tweet.getUrls().size(), 1);
        assertEquals(tweet.getUrls().get(0), "bit.ly/asfd");
    }

    @Test
    public void testRemovedUrls() {
        setContentAndProcess("This tweet contains http://bit.ly/asfd as an url");
        assertThat(tweet.getContent(), equalTo("this tweet contains URL as an url"));

        setContentAndProcess("This tweet contains https://bit.ly/asfd as an url");
        assertThat(tweet.getContent(), equalTo("this tweet contains URL as an url"));

        setContentAndProcess("This tweet contains http://www.bit.ly/asfd as an url");
        assertThat(tweet.getContent(), equalTo("this tweet contains URL as an url"));

        setContentAndProcess("This tweet contains bit.ly/asfd as an url");
        assertThat(tweet.getContent(), equalTo("this tweet contains URL as an url"));

        setContentAndProcess("This tweet contains google.at/?q=Test as an url");
        assertThat(tweet.getContent(), equalTo("this tweet contains URL as an url"));
    }

    @Test
    public void testGetHashtags() {
        setContentAndProcess("This tweet contains a #hashtag!");
        assertTrue(tweet.getHashtags().contains("hashtag"));
        assertEquals(tweet.getHashtags().size(), 1);
        assertEquals(tweet.getContent().indexOf("#"), -1);

        setContentAndProcess("This tweet contains #two2 #hashtags!");
        assertTrue(tweet.getHashtags().contains("two2"));
        assertTrue(tweet.getHashtags().contains("hashtags"));
        assertEquals(tweet.getContent().indexOf("#"), -1);
        assertEquals(tweet.getHashtags().size(), 2);
    }

    @Test
    public void testGetAuthors() {
        setContentAndProcess("This tweet contains an @user!");
        assertTrue(tweet.getMentionedUsers().contains("user"));
        assertEquals(tweet.getMentionedUsers().size(), 1);
        assertEquals(tweet.getContent().indexOf("@"), -1);

        setContentAndProcess("This tweet contains @two2 @users!");
        assertTrue(tweet.getMentionedUsers().contains("two2"));
        assertTrue(tweet.getMentionedUsers().contains("users"));
        assertEquals(tweet.getContent().indexOf("@"), -1);
        assertEquals(tweet.getMentionedUsers().size(), 2);
    }

    @Test
    public void testReplaceConsecutiveLetters() {
        setContentAndProcess("heyyyyyyyyyyyyyyyyyyy youuuuuuuuuuuuuu");
        assertThat(tweet.getContent(), equalTo("hey you"));

        setContentAndProcess("wazuuuuuuuuuuuuuuuuup");
        assertThat(tweet.getContent(), equalTo("wazup"));
    }

    @Test
    public void testReplaceConsecutiveLetters_NotReplacingDoubleLetters() {
        setContentAndProcess("heyy you again");
        assertThat(tweet.getContent(), equalTo("heyy you again"));

        setContentAndProcess("letters add good bees too tee all goo");
        assertThat(tweet.getContent(), equalTo("letters add good bees too tee all goo"));
    }

    @Test
    public void testReplaceHashTags() {
        setContentAndProcess("Hello #intelligent person");
        assertThat(tweet.getContent(), equalTo("hello " + POSITIVE_HASHTAG.trim() + " person"));

        setContentAndProcess("this is #superb!");
        assertThat(tweet.getContent(), equalTo("this is " + POSITIVE_HASHTAG.trim() + " !"));

        setContentAndProcess("#worst#fail");
        assertThat(tweet.getContent(), equalTo(NEGATIVE_HASHTAG.trim() + " " + NEGATIVE_HASHTAG.trim()));

        setContentAndProcess("#worst#notknown#fail");
        assertThat(tweet.getContent(), equalTo(NEGATIVE_HASHTAG.trim() + " " + NEGATIVE_HASHTAG.trim()));
    }

    @Test
    public void testReplaceHashTags_MultipleHashTags_AddedSpaces() {
        setContentAndProcess("Hello #intelligent#beautiful person");
        assertThat(tweet.getContent(), equalTo("hello " + POSITIVE_HASHTAG.trim() + " " + POSITIVE_HASHTAG.trim()
                + " person"));

        setContentAndProcess("this is #superb #classy #flawless!");
        assertThat(tweet.getContent(), equalTo("this is " + POSITIVE_HASHTAG.trim() + " " + POSITIVE_HASHTAG.trim()
                + " " + POSITIVE_HASHTAG.trim() + " !"));
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

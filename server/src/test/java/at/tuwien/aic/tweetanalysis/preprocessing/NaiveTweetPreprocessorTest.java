/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.junit.Before;
import org.junit.Test;

import static at.tuwien.aic.tweetanalysis.preprocessing.NaiveTweetPreprocessor.*;
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

    private void assertFeatureValue(String featureName, double value) {
        assertThat(tweet.getFeatureMap().get(featureName), equalTo(value));
    }

    @Test
    public void testAllCapsFails() {
        setContentAndProcess("Sorry, Microsoft! A Bunch Of Teenagers Just Talked About Doing School Work And None Of Them Use… http://t.co/SvxwZe0QVx #social #mobile #fb");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("I'M SO PISSED AT EVERYTHING WHAT IS GOING ON");
        assertFeatureValue(ALL_CAPS_FEATURE, 1.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 9.0);
    }

    @Test
    public void testAllCapsChecks() {
        setContentAndProcess("WHAT UP?!");
        assertFeatureValue(ALL_CAPS_FEATURE, 1.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 2.0);

        setContentAndProcess("WHAT Up?");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 1.0);

        setContentAndProcess("THIs is cool");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("@YO this is cool");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("I HATE IT");
        assertFeatureValue(ALL_CAPS_FEATURE, 1.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 2.0); // I gets ignored as count

        setContentAndProcess("AN APPLE");
        assertFeatureValue(ALL_CAPS_FEATURE, 1.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 2.0);
    }

    @Test
    public void testAllCaps_ignoresMentionsAndHashTags() {
        setContentAndProcess("I HATE @justinbieber");
        assertFeatureValue(ALL_CAPS_FEATURE, 1.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 1.0);

        setContentAndProcess("I HATE #beaverfever");
        assertFeatureValue(ALL_CAPS_FEATURE, 1.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 1.0);
    }

    @Test
    public void testAllCapsIgnoredValues() {
        setContentAndProcess("I");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("DM");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("I DM");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("O");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("A tree");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);

        setContentAndProcess("I have");
        assertFeatureValue(ALL_CAPS_FEATURE, 0.0);
        assertFeatureValue(ALL_CAPS_WORDS_FEATURE, 0.0);
    }

    @Test
    public void testNormaliseSpaces() {
        setContentAndProcess("this contains spaces    ! too much  yes");
        assertThat(tweet.getContent(), equalTo("this contains spaces ! too much yes"));

        setContentAndProcess("    what       is     happening                 !             ");
        assertThat(tweet.getContent(), equalTo("what is happening !"));
    }

//    public void testNoMentions() {
//        setContentAndProcess("@me is so cool!");
//        assertThat(tweet.getContent(), equalTo("is so cool!"));
//
//        setContentAndProcess("@me is so cool @yeah!");
//        assertThat(tweet.getContent(), equalTo("is so cool !"));
//
//        setContentAndProcess("@me@and@not@you@go@away content");
//        assertThat(tweet.getContent(), equalTo("content"));
//    }

    @Test
    public void testReplacedMentions() {
        setContentAndProcess("look at this @superawesomeperson");
        assertThat(tweet.getContent(), equalTo("look at this MENTION"));

        setContentAndProcess("look at this @superawesomeperson@anotherperson");
        assertThat(tweet.getContent(), equalTo("look at this MENTION MENTION"));

        setContentAndProcess("look at this @superawesomeperson @anotherperson!");
        assertThat(tweet.getContent(), equalTo("look at this MENTION MENTION !"));
    }

//    /**
//     * Test of preprocess method, of class NaiveTweetPreprocessor.
//     */
//    @Test
//    public void testPreprocessSmilies() {
//        setContentAndProcess("This is a tweet containing smilies like :)");
//        assertTrue(tweet.getContent().contains("POSITIVESMILE"));
//
//        setContentAndProcess("This is a tweet containing smilies like :-)");
//        assertTrue(tweet.getContent().contains("POSITIVESMILE"));
//
//        setContentAndProcess("This is a tweet :( containing smilies like :( :(");
//        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
//        assertFalse(tweet.getContent().contains(":("));
//
//        setContentAndProcess("This is a tweet containing smilies like :-(");
//        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
//
//        setContentAndProcess("This is a tweet containing smilies like :-( or :-)");
//        assertTrue(tweet.getContent().contains("NEGATIVESMILE"));
//        assertTrue(tweet.getContent().contains("POSITIVESMILE"));

//        setContentAndProcess(": )");
//        assertThat(tweet.getContent(), equalTo(POSITIVE_SMILE.trim()));
//
//        setContentAndProcess(": (");
//        assertThat(tweet.getContent(), equalTo(NEGATIVE_SMILE.trim()));
//
//        setContentAndProcess("<3");
//        assertThat(tweet.getContent(), equalTo(POSITIVE_SMILE.trim() + " " + ENLARGED_POSITIVE_SMILE.trim()));
//
//        setContentAndProcess("</3 lol");
//        assertThat(tweet.getContent(), equalTo(NEGATIVE_SMILE.trim() + " lol " + ENLARGED_NEGATIVE_SMILE.trim()));
//
//        setContentAndProcess("♥ bb");
//        assertThat(tweet.getContent(), equalTo(POSITIVE_SMILE.trim() + " bb " + ENLARGED_POSITIVE_SMILE.trim()));
//
//        setContentAndProcess(":(:");
//        assertThat(tweet.getContent(), equalTo(NEGATIVE_SMILE.trim() + " :"));
//    }

    @Test
    public void testFailedSmilieDetection() {
        setContentAndProcess("#Hot #Sales Microsoft - Xbox One Console Assassin's Creed: Unity Bundle: $349.99… http://t.co/2NmAW8DyQA #Buzz http://t.co/ziA30CH0oh");
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 0.0);
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 0.0);

        setContentAndProcess("Microsoft OneNote 2013 32-bit/x64 English Medialess S26-05028 US Free Shipping http://t.co/CzmUyGgLG6 #464 http://t.co/RIlVS3o0ki");
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 0.0);
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 0.0);
    }

    @Test
    public void testReversedSmiliesMustContainASpaceBefore() {
        setContentAndProcess("(:");
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 0.0);
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 0.0);

        setContentAndProcess(" (:");
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 0.0);
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 1.0);

        setContentAndProcess(" d:");
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 1.0);
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 0.0);

        setContentAndProcess(":(:");
        assertThat(tweet.getContent(), equalTo(":"));
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 0.0);
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 1.0);
    }

    @Test
    public void testRemovedSmilies() {
        setContentAndProcess(":-)");
        assertThat(tweet.getContent(), equalTo(""));
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 1.0);
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 0.0);

        setContentAndProcess(":-(");
        assertThat(tweet.getContent(), equalTo(""));
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 1.0);
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 0.0);
    }

    @Test
    public void testRemovedElongatedSmilies() {
        setContentAndProcess(":)))))))))))))))");
        assertThat(tweet.getContent(), equalTo(""));
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 1.5);
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 0.0);
        assertFeatureValue(ENLARGED_WORD_COUNT_FEATURE, 0.0);

        setContentAndProcess(":((((((((((((((((((((((((((((((((((((((((((((");
        assertThat(tweet.getContent(), equalTo(""));
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 1.5);
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 0.0);
        assertFeatureValue(ENLARGED_WORD_COUNT_FEATURE, 0.0);
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

//        setContentAndProcess("This tweet contains google.at/?q=Test as an url");
//        assertThat(tweet.getContent(), equalTo("this tweet contains URL as an url")); // todo: fix maybe
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
        assertThat(tweet.getContent(), equalTo("heyyy youuu"));
        assertFeatureValue(ENLARGED_WORD_COUNT_FEATURE, 2.0);

        setContentAndProcess("heyyyyyy yyyyyyyyyyyyy");
        assertThat(tweet.getContent(), equalTo("heyyy yyy"));
        assertFeatureValue(ENLARGED_WORD_COUNT_FEATURE, 2.0);

        setContentAndProcess("wazuuuuuuuuuuuuuuuuup");
        assertThat(tweet.getContent(), equalTo("wazuuup"));
        assertFeatureValue(ENLARGED_WORD_COUNT_FEATURE, 1.0);
    }

    @Test
    public void testChecksForConsecutiveMarks() {
        setContentAndProcess("what?????????????? ??????????????????? !!!!");
        assertThat(tweet.getContent(), equalTo("what??? ??? !!!"));
        assertFeatureValue(CONSECUTIVE_QUESTION_MARKS_COUNT_FEATURE, 2.0);
        assertFeatureValue(CONSECUTIVE_EXCLAMATION_MARKS_COUNT_FEATURE, 1.0);
    }

    @Test
    public void testReplaceDots() {
        setContentAndProcess("lol ...");
        assertThat(tweet.getContent(), equalTo("lol " + DOTS.trim()));

        setContentAndProcess("super sad.................");
        assertThat(tweet.getContent(), equalTo("super sad " + DOTS.trim()));

        setContentAndProcess("...yeah...super...great");
        assertThat(tweet.getContent(), equalTo(DOTS.trim() + " yeah " + DOTS.trim() + " super " + DOTS.trim() + " great"));

        setContentAndProcess("…");
        assertThat(tweet.getContent(), equalTo(DOTS.trim()));

        setContentAndProcess("................. ........................... ..........................................");
        assertThat(tweet.getContent(), equalTo(DOTS.trim() + " " + DOTS.trim() + " " + DOTS.trim()));
    }

    @Test
    public void testReplaceConsecutiveLetters_NotReplacingDoubleLetters() {
        setContentAndProcess("heyy you again");
        assertThat(tweet.getContent(), equalTo("heyy you again"));

        setContentAndProcess("letters add good bees too tee all goo");
        assertThat(tweet.getContent(), equalTo("letters add good bees too tee all goo"));
    }

    @Test
    public void testReplaceEnlargedSmilie() {
        setContentAndProcess("what is this shit?! :(((((((");
        assertThat(tweet.getContent(), equalTo("what is this shit?!"));
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 1.5); // large smilies count as 1.5

        setContentAndProcess("oh so cool :))");
        assertThat(tweet.getContent(), equalTo("oh so cool"));
        assertFeatureValue(POSITIVE_SMILIES_FEATURE, 1.5);

        setContentAndProcess("oh :((");
        assertThat(tweet.getContent(), equalTo("oh"));
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 1.5);

        setContentAndProcess("oh :(( :((( :((((");
        assertThat(tweet.getContent(), equalTo("oh"));
        assertFeatureValue(NEGATIVE_SMILIES_FEATURE, 4.5);
    }

    @Test
    public void testReplaceHashTags() {
        setContentAndProcess("hello #intelligent person");
        assertThat(tweet.getContent(), equalTo("hello intelligent person"));
        assertFeatureValue(POSITIVE_HASHTAGS_FEATURE, 1.0);
        assertFeatureValue(HASHTAGS_COUNT_FEATURE, 1.0);

        setContentAndProcess("this is #superb");
        assertThat(tweet.getContent(), equalTo("this is superb"));
        assertFeatureValue(POSITIVE_HASHTAGS_FEATURE, 1.0);
        assertFeatureValue(HASHTAGS_COUNT_FEATURE, 1.0);

        setContentAndProcess("#worst#fail");
        assertThat(tweet.getContent(), equalTo("worst fail"));
        assertFeatureValue(NEGATIVE_HASHTAGS_FEATURE, 2.0);
        assertFeatureValue(HASHTAGS_COUNT_FEATURE, 2.0);

        setContentAndProcess("#worst #notknown #fail #superb");
        assertThat(tweet.getContent(), equalTo("worst notknown fail superb"));
        assertFeatureValue(NEGATIVE_HASHTAGS_FEATURE, 2.0);
        assertFeatureValue(POSITIVE_HASHTAGS_FEATURE, 1.0);
        assertFeatureValue(HASHTAGS_COUNT_FEATURE, 4.0);

        setContentAndProcess("#worst#worstworst#worstworstworst");
        assertThat(tweet.getContent(), equalTo("worst worstworst worstworstworst"));
    }

    @Test
    public void testReplaceHashTags_MultipleHashTags_AddedSpaces() {
        setContentAndProcess("Hello #intelligent#beautiful person");
        assertThat(tweet.getContent(), equalTo("hello intelligent beautiful person"));

        setContentAndProcess("this is #superb #classy #flawless!");
        assertThat(tweet.getContent(), equalTo("this is superb classy flawless !"));
    }

}

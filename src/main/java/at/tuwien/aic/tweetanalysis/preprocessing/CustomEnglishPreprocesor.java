/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import static at.tuwien.aic.tweetanalysis.preprocessing.NaiveTweetPreprocessor.log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Thomas
 */
public class CustomEnglishPreprocesor implements ITweetPreprocessor {

    private final Pattern negationsPattern = Pattern.compile("(\\w*\\s|^)(not|no)(\\s\\w*|$)");
    private final Map<String, String> standardContractions = new HashMap<>();

    public CustomEnglishPreprocesor() {
        this.loadContractions();
    }

    @Override
    public Tweet preprocess(Tweet tweet) {
        String content = tweet.getContent();

        content = handleContractions(content);
        content = handleNegations(content);

        content = content.trim();

        tweet.setContent(content);
        return tweet;
    }

    @Override
    public List<Tweet> preprocess(List<Tweet> tweets) {
        for (Tweet t : tweets) {
            preprocess(t);
        }
        return tweets;
    }

    private String handleContractions(String input) {
        for (String contraction : this.standardContractions.keySet()) {
            input = input.replaceAll("(?i)" + contraction, this.standardContractions.get(contraction));
        }
        
        
        
        return input;
    }

    /**
     * Handles special negation words like "not", "no" and "don't" and searches
     * for surrounding words.
     * <p/>
     * If one after the negation word is found, it gets "not-" prepended.
     * <p/>
     * If none was found after but one before the negation, it also gets "not-"
     * prepended.
     * <p/>
     * If no words are found immediately around the negation, nothing changes.
     * <p/>
     * Example: "not cool bro" -> "not not-cool bro"; "like not" -> "not-like
     * not"
     * <p/>
     * This is useful for machine learning since now the algorithm won't see the
     * words "cool" and "like" in the tweet and would otherwise maybe think this
     * is positive.
     *
     * @param input the content of the tweet
     * @return the processed content
     */
    private String handleNegations(String input) {
        String output = input;
        Matcher matcher = negationsPattern.matcher(input);
        while (matcher.find()) {
            String fullString = matcher.group(0);
            String group1 = matcher.group(1).trim();
            String group2 = matcher.group(3).trim(); // group[2] is the negation word
            if (!group2.isEmpty()) {
                fullString = fullString.replace(group2, "not-" + group2);
            } else if (!group1.isEmpty()) {
                fullString = fullString.replace(group1, "not-" + group1);
            }
            output = output.replace(matcher.group(0), fullString);
            log.trace("Group1: '{}', Group2: '{}'. '{}' -> '{}'", group1, group2, matcher.group(0), fullString);
        }
        return output;
    }

    //http://en.wikipedia.org/wiki/Wikipedia:List_of_English_contractions
    private void loadContractions() {
        standardContractions.put("ain't", "am not");
        standardContractions.put("aren't", "are not");
        standardContractions.put("can't", "cannot");
        standardContractions.put("could've", "could have");
        standardContractions.put("couldn't", "could not");
        standardContractions.put("couldn't've", "could not have");
        standardContractions.put("didn't", "did not");
        standardContractions.put("doesn't", "does not");
        standardContractions.put("don't", "do not");
        standardContractions.put("hadn't", "had not");
        standardContractions.put("hadn't've", "had not have");
        standardContractions.put("hasn't", "has not");
        standardContractions.put("haven't", "have not");
        standardContractions.put("he'd", "e would");
        standardContractions.put("he'd've", "he would have");
        standardContractions.put("he'll", "he will");
        standardContractions.put("he's", "he is");
        standardContractions.put("how'd", "how did");
        standardContractions.put("how'll", "how will");
        standardContractions.put("how's", "how is");
        standardContractions.put("I'd", "I would");
        standardContractions.put("I'd've", "I would have");
        standardContractions.put("I'll", "I will");
        standardContractions.put("I'm", "I am");
        standardContractions.put("I've", "I have");
        standardContractions.put("isn't", "is not");
        standardContractions.put("it'd", "it had");
        standardContractions.put("it'd've", "it would have");
        standardContractions.put("it'll", "it will");
        standardContractions.put("it's", "it is");
        standardContractions.put("let's", "let us");
        standardContractions.put("ma'am", "madam");
        standardContractions.put("mightn't", "might not");
        standardContractions.put("mightn't've", "might not have");
        standardContractions.put("might've", "might have");
        standardContractions.put("mustn't", "must not");
        standardContractions.put("must've", "must have");
        standardContractions.put("needn't", "need not");
        standardContractions.put("not've", "not have");
        standardContractions.put("o'clock", "of the clock");
        standardContractions.put("shan't", "shall not");
        standardContractions.put("she'd", "she would");
        standardContractions.put("she'd've", "she would have");
        standardContractions.put("she'll", "she will");
        standardContractions.put("she's", "she is");
        standardContractions.put("should've", "should have");
        standardContractions.put("shouldn't", "should not");
        standardContractions.put("shouldn't've", "should not have");
        standardContractions.put("that'll", "that will");
        standardContractions.put("that's", "that is");
        standardContractions.put("there'd", "there would");
        standardContractions.put("there'd've", "there would have");
        standardContractions.put("there're", "there are");
        standardContractions.put("there's", "there has");
        standardContractions.put("they'd", "they would");
        standardContractions.put("they'd've", "they would have");
        standardContractions.put("they'll", "they will");
        standardContractions.put("they're", "they are");
        standardContractions.put("they've", "they have");
        standardContractions.put("wasn't", "was not");
        standardContractions.put("we'd", "we had");
        standardContractions.put("we'd've", "we would have");
        standardContractions.put("we'll", "we will");
        standardContractions.put("we're", "we are");
        standardContractions.put("we've", "we have");
        standardContractions.put("weren't", "were not");
        standardContractions.put("what'll", "what will");
        standardContractions.put("what're", "what are");
        standardContractions.put("what's", "what does");
        standardContractions.put("what've", "what have");
        standardContractions.put("when's", "when is");
        standardContractions.put("where'd", "where did");
        standardContractions.put("where's", "where is");
        standardContractions.put("where've", "where have");
        standardContractions.put("who'd", "who had");
        standardContractions.put("who'll", "who will");
        standardContractions.put("who're", "who are");
        standardContractions.put("who's", "who is");
        standardContractions.put("who've", "who have");
        standardContractions.put("why'll", "why will");
        standardContractions.put("why're", "why are");
        standardContractions.put("why's", "why is");
        standardContractions.put("won't", "will not");
        standardContractions.put("would've", "would have");
        standardContractions.put("wouldn't", "would not");
        standardContractions.put("wouldn't've", "would not have");
        standardContractions.put("y'all", "you all");
        standardContractions.put("y'all'd've", "you all would have");
        standardContractions.put("you'd", "you would");
        standardContractions.put("you'd've", "you would have");
        standardContractions.put("you'll", "you will");
        standardContractions.put("you're", "you are");
        standardContractions.put("you've", "you have");
    }

}

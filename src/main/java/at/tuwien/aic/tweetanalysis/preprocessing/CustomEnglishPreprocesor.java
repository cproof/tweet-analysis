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
    private final Pattern negationsPattern = Pattern.compile("(\\w*\\s|^)([Nn]ot|[Nn]o)(\\s\\w*|$)");
    private static final Map<String, String> negativeContractions = new HashMap<>();
    
    public CustomEnglishPreprocesor() {
        negativeContractions.put("aren't", "are not");
        negativeContractions.put("can't", "cannot, can not");
        negativeContractions.put("couldn't", "could not");
        negativeContractions.put("daren't", "dare not");
        negativeContractions.put("didn't", "did not");
        negativeContractions.put("doesn't", "does not");
        negativeContractions.put("don't", "do not");
        negativeContractions.put("hasn't", "has not");
        negativeContractions.put("haven't", "have not");
        negativeContractions.put("hadn't", "had not");
        negativeContractions.put("isn't", "is not");
        negativeContractions.put("mayn't", "may not");
        negativeContractions.put("mightn't", "might not");
        negativeContractions.put("mustn't", "must not");
        negativeContractions.put("needn't", "need not");
        negativeContractions.put("oughtn't", "ought not");
        negativeContractions.put("shan't", "shall not");
        negativeContractions.put("shouldn't", "should not");
        negativeContractions.put("wasn't", "was not");
        negativeContractions.put("weren't", "were not");
        negativeContractions.put("won't", "will not");
        negativeContractions.put("wouldn't", "would not");
    }
    
    @Override
    public Tweet preprocess(Tweet tweet) {
        String content = tweet.getContent();
        
        content = handleNegations(content);
        
        content = content.trim();
        
        tweet.setContent(content);
        return tweet;
    }

    @Override
    public List<Tweet> preprocess(List<Tweet> tweets) {
        for(Tweet t : tweets) {
            preprocess(t);
        }
        return tweets;
    }
    
    
    private String handleAbbreviations(String input) {
        return input;
    }
    
        /**
     * Handles special negation words like "not", "no" and "don't" and searches for surrounding words.
     * <p/>
     * If one after the negation word is found, it gets "not-" prepended.
     * <p/>
     * If none was found after but one before the negation, it also gets "not-" prepended.
     * <p/>
     * If no words are found immediately around the negation, nothing changes.
     * <p/>
     * Example: "not cool bro" -> "not not-cool bro"; "like not" -> "not-like not"
     * <p/>
     * This is useful for machine learning since now the algorithm won't see the words "cool" and "like" in the tweet
     * and would otherwise maybe think this is positive.
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
}

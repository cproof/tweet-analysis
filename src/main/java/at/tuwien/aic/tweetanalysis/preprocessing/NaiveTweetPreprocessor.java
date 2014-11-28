package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class NaiveTweetPreprocessor implements ITweetPreprocessor {

    public static final Logger log = LoggerFactory.getLogger(NaiveTweetPreprocessor.class);
    public static final String POSITIVE_HASHTAG = " POSITIVEHASHTAG ";
    public static final String NEGATIVE_HASHTAG = " NEGATIVEHASHTAG ";
    public static final String HASHTAG = " HASHTAG ";
    public static final String POSITIVE_SMILE = " POSITIVESMILE ";
    public static final String NEGATIVE_SMILE = " NEGATIVESMILE ";
    public static final String ENLARGED_WORD = " ENLARGEDDWORD ";
    public static final String ENLARGED_POSITIVE_SMILE = " ENLARGED_POSITIVESMILE ";
    public static final String ENLARGED_NEGATIVE_SMILE = " ENLARGED_NEGATIVESMILE ";
    public static final String DOTS = " DOTS ";

    private final Pattern hashtagPattern = Pattern.compile("#[0-9a-zA-Z\\-_]*");
    private final Pattern userPattern = Pattern.compile("@[0-9a-zA-Z\\-_]*");
    //private final Pattern urlPattern = Pattern.compile("(http://){0,1}(www\\.){0,1}[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*");
    private final Pattern urlPattern = Pattern.compile("[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*"); // todo: improve or don't use regex for urls
    private final Pattern doublePatter = Pattern.compile("((\\S)\\2+)");
    private final Pattern dotsPattern = Pattern.compile("\\.{3,}|…");


    private final HashSet<String> positiveHashTags;
    private final HashSet<String> negativeHashTags;

    public NaiveTweetPreprocessor() {
        positiveHashTags = new HashSet<>();
        try (Scanner scanner = new Scanner(NaiveTweetPreprocessor.class.getResourceAsStream("/positive-hashtags.txt"))) {
            while (scanner.hasNextLine()) {
                positiveHashTags.add(scanner.nextLine());
            }
        }

        negativeHashTags = new HashSet<>();
        try (Scanner scanner = new Scanner(NaiveTweetPreprocessor.class.getResourceAsStream("/negative-hashtags.txt"))) {
            while (scanner.hasNextLine()) {
                negativeHashTags.add(scanner.nextLine());
            }
        }
    }

    @Override
    public Tweet preprocess(Tweet tweet) {
        String content = tweet.getContent();

        //to lowercase
        content = content.toLowerCase();

        content = replaceSmilies(content);

        //remove hash symbol from the content
        //for equal treatment of tweets using hashtags
        //and tweets that use none
        tweet.setHashtags(getHashtags(content));
        content = replaceHashTags(content);
        content = content.replace("#", "");


        //same with other users
        tweet.setMentionedUsers(this.getUsers(content));
        //@TODO: Experiment if it makes more sense to
        //  1 leave users as they are, including the @
        //  2 remove users completely including the name
//        content = content.replace("@", "");
        content = content.replaceAll("@[0-9a-zA-Z\\-_]*", "");

        //normalize urls
        content = content.replace("www.", "").replace("http://", "").replace("https://", "");
        tweet.setUrls(getUrls(content));
        // remove urls
        content = content.replaceAll("[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*", "URL");

        // replace all "....."
        content = replaceConsecutiveDots(content);

        content = replaceConsecutiveLetters(content);

        //normalize spaces
        content = content.replaceAll("\\s+", " ");


        content = content.replace("'", "");
        content = content.replace("\"", "");

        content = content.trim();
        tweet.setContent(content);
        return tweet;
    }

    /**
     * Get Hashtags from the tweet, put them in the list
     *
     * @param content text content of the tweet
     * @return List of Hashtags
     */
    private List<String> getHashtags(String content) {
        List<String> hashtags = new LinkedList<>();

        Matcher m = hashtagPattern.matcher(content);
        while (m.find()) {
            //add hashtag to the list without the hash symbol
            hashtags.add(m.group().substring(1));
        }

        return hashtags;
    }

    /**
     * Get the users mentioned in a the tweet, put them in the list
     *
     * @param content text content of the tweet
     * @return List of mentioned Users
     */
    private List<String> getUsers(String content) {
        List<String> users = new LinkedList<>();

        Matcher m = userPattern.matcher(content);
        while (m.find()) {
            //add hashtag to the list without the hash symbol
            users.add(m.group().substring(1));
        }

        return users;
    }

    /**
     * Get List of urls from the tweet
     *
     * @param content text content of the tweet
     * @return List of Urls
     */
    private List<String> getUrls(String content) {
        List<String> urls = new LinkedList<>();

        Matcher m = urlPattern.matcher(content);
        while (m.find()) {
            //add url to the list without the hash symbol
            String url = m.group();
            url = url.replace("www.", "").replace("http://", "").replace("https://", "");
            urls.add(url);
        }

        return urls;
    }

    private String replaceSmilies(String input) {
        String output = input;
        if (output.matches(".*:-?\\({2,}.*|.*</3.*")) {
            output += ENLARGED_NEGATIVE_SMILE;
        } else if (output.matches(".*:-?\\){2,}.*|.*♥.*|.*<3.*")) {
            output += ENLARGED_POSITIVE_SMILE;
        }

        // todo: fix reversed smilies like "(:" and "):". they should not match for example :(: as positive. currently they are just removed

        output = output.replaceAll(":(-?|\\s)\\)+|:-?d|8-?d|\\sxd|x-d|=d|:o\\)|:-?]|:3|:>|=]|=\\)|;-?\\)|;d|;-?d|\\\\o//|:'-?\\)|:-?p=p|\\sxp|\\^\\^|♥|<3|ツ", POSITIVE_SMILE);
        output = output.replaceAll(":(-?|\\s)\\(+|>:-?\\[|:c|:-?<|:-?\\[|=\\[|:-?\\{|:'-?\\(|\\sd-?:|:-?@|>:-?\\(|:-?\\|\\||:-?\\$|=/|</3", NEGATIVE_SMILE);

        return output;
    }

    /**
     * Replaces known positive or negative hashtags with the corresponding tag.
     * Unknown hashtags are kept but the label "HASHTAG" is added afterwards to indicate, that there was a hashtag
     *
     * @param input the content of the tweet
     * @return the processed content
     */
    private String replaceHashTags(String input) {
        String output = input;

        Matcher matcher = hashtagPattern.matcher(input);
        while (matcher.find()) {
            String hashTag = matcher.group();
            if (positiveHashTags.contains(hashTag)) {
                output = output.replace(hashTag, POSITIVE_HASHTAG);
            } else if (negativeHashTags.contains(hashTag)) {
                output = output.replace(hashTag, NEGATIVE_HASHTAG);
            } else {
//                log.trace("'{}' has no sentiment stored", hashTag);
                output = output.replace(hashTag, " " + hashTag + HASHTAG);
            }
        }

        return output;
    }

    /**
     * Replaces consecutive letters that occur more than twice with the single letter.
     * E.g.: heyyyyyyyyyyyy -> hey; hello -> hello.
     * <p>
     * New: we now also add the label "ENLONGEDWORD" to signal a processed word with double letters.
     * This is added at the end of the output and the number of labels depends on the number of words that got corrected.
     *
     * @param input the content of the tweet
     * @return the processed content
     */
    private String replaceConsecutiveLetters(String input) {
        String output = input;
        Matcher matcher = doublePatter.matcher(input);
        int counter = 0;
        while (matcher.find()) {
            String group1 = matcher.group(1);
            String group2 = matcher.group(2);
            if (group1.length() > 2) {
                output = output.replace(group1, group2);
                counter++;
                log.trace("Group1: {}, Group2: {}. {} -> {}", group1, group2, input, output);
            }
        }
        if (counter > 0) {
            for (int i = 0; i < counter; i++) {
                output += " " + ENLARGED_WORD.trim();
            }
        }
        return output;
    }

    private String replaceConsecutiveDots(String input) {
        String output = input;
        Matcher matcher = dotsPattern.matcher(input);
        while (matcher.find()) {
            output = output.replace(matcher.group(), DOTS);
        }
        return output;
    }

    // todo: maybe use in the future
    public static String negatePossibleTag(String input) {
        String output = input.trim();
        if (output.equalsIgnoreCase(POSITIVE_SMILE.trim())) {
            output = NEGATIVE_SMILE;
        } else if (output.equalsIgnoreCase(NEGATIVE_SMILE.trim())) {
            output = POSITIVE_SMILE;
        } else if (output.equalsIgnoreCase(POSITIVE_HASHTAG.trim())) {
            output = NEGATIVE_HASHTAG;
        } else if (output.equalsIgnoreCase(NEGATIVE_HASHTAG.trim())) {
            output = POSITIVE_HASHTAG;
        }

        return output;
    }

    @Override
    public List<Tweet> preprocess(List<Tweet> tweets) {
        for (Tweet t : tweets) {
            this.preprocess(t);
        }

        return tweets;
    }

}

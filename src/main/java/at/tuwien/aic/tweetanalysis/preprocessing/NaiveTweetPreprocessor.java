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
    public static final String POSITIVE_SMILE = " POSITIVESMILE ";
    public static final String NEGATIVE_SMILE = " NEGATIVESMILE ";

    private final Pattern hashtagPattern = Pattern.compile("#[0-9a-zA-Z\\-_]*");
    private final Pattern userPattern = Pattern.compile("@[0-9a-zA-Z\\-_]*");
    //private final Pattern urlPattern = Pattern.compile("(http://){0,1}(www\\.){0,1}[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*");
    private final Pattern urlPattern = Pattern.compile("[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*"); // todo: improve or don't use regex for urls
    private final Pattern doubleLetterPatter = Pattern.compile("((.)\\2+)");

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
//        content = content.replace("#", "");
        content = replaceHashTags(content);


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

        //normalize spaces
        content = content.replaceAll("\\s+", " ");

        content = replaceConsecutiveLetters(content);

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
        input = input.replaceAll(":-(\\))+|:(\\))+|:d|:-d|8-d|\\sxd|x-d|=d|:o\\)|:]|:-]|:3|:>|=]|=\\)|;\\)|;-\\)|;d|;-d|\\\\o//|:'-\\)|:'\\)|:p|:-p|=p|\\sxp|\\(:|\\(-:|\\^\\^", POSITIVE_SMILE);
        input = input.replaceAll(":\\(|:-\\(|>:\\[|:c|:<|:-<|:\\[|:-\\[|=\\[|:\\{|:'\\(|:'-\\(|\\sd:|\\):|\\)-:|:@|>:\\(|:-\\|\\||:\\$|=/", NEGATIVE_SMILE);

        return input;
    }

    /**
     * Replaces known positive or negative hashtags with the corresponding tag.
     * Unknown hashtags are removed completely -> //todo: maybe replace unknown with just HASHTAG or TAG
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
                log.trace("'{}' has no sentiment stored", hashTag);
                output = output.replace(hashTag, "");
            }
        }

        return output;
    }

    /**
     * Replaces consecutive letters that occur more than twice with the single letter.
     * E.g.: heyyyyyyyyyyyy -> hey; hello -> hello
     *
     * @param input the content of the tweet
     * @return the processed content
     */
    private String replaceConsecutiveLetters(String input) {
        String output = input;
        Matcher matcher = doubleLetterPatter.matcher(input);
        while (matcher.find()) {
            String group1 = matcher.group(1);
            String group2 = matcher.group(2);
            if (group1.length() > 2) {
                output = output.replace(group1, group2);
                log.trace("Group1: {}, Group2: {}. {} -> {}", group1, group2, input, output);
            }
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

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    public static final String POSITIVE_SMILIES_FEATURE = "POSITIVE_SMILIES";
    public static final String NEGATIVE_SMILIES_FEATURE = "NEGATIVE_SMILIES";
    public static final String POSITIVE_WORDS_FEATURE = "POSITIVE_WORDS";
    public static final String NEGATIVE_WORDS_FEATURE = "NEGATIVE_WORDS";
    public static final String POSITIVE_HASHTAGS_FEATURE = "POSITIVE_HASHTAGS";
    public static final String NEGATIVE_HASHTAGS_FEATURE = "NEGATIVE_HASHTAGS";
    public static final String HASHTAGS_COUNT_FEATURE = "HASHTAGS_COUNT";
    public static final String ENLARGED_WORD_COUNT_FEATURE = "ENLARGED_WORD";
    public static final String CONSECUTIVE_QUESTION_MARKS_COUNT_FEATURE = "CONSECUTIVE_QUESTION_MARKS";
    public static final String CONSECUTIVE_EXCLAMATION_MARKS_COUNT_FEATURE = "CONSECUTIVE_EXCLAMATIONS_MARKS";
    public static final String HAS_ALL_CAPS_WORDS_FEATURE = "HAS_ALL_CAPS_WORDS";
    public static final String ALL_CAPS_FEATURE = "ALL_CAPS";

    private final Pattern hashtagPattern = Pattern.compile("#[0-9a-zA-Z\\-_]*");
    private final Pattern userPattern = Pattern.compile("@[0-9a-zA-Z\\-_]*");
    //private final Pattern urlPattern = Pattern.compile("(http://){0,1}(www\\.){0,1}[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*");
    private final Pattern urlPattern = Pattern.compile("[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*"); // todo: improve or don't use regex for urls
    private final Pattern doublePattern = Pattern.compile("((\\S)\\2{2,})");
    private final Pattern dotsPattern = Pattern.compile("\\.{3,}|…");

    private final Pattern allEmoticonPattern;
    private final Pattern positiveEmoticonsPattern;
    private final Pattern negativeEmoticonsPattern;

    private final Pattern bigNegativeEmoticonsPattern = Pattern.compile(":-?\\({2,}");
    private final Pattern bigPositiveEmoticonsPattern = Pattern.compile(":-?\\){2,}");

    private final HashSet<String> positiveHashTags;
    private final HashSet<String> negativeHashTags;
    private final HashSet<String> positiveWords;
    private final HashSet<String> negativeWords;

    private final HashSet<String> ignoredAllCapsWords;

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

        positiveWords = new HashSet<>();
        try (Scanner scanner = new Scanner(NaiveTweetPreprocessor.class.getResourceAsStream("/positive-words.txt"))) {
            while (scanner.hasNextLine()) {
                positiveWords.add(scanner.nextLine());
            }
        }
        negativeWords = new HashSet<>();
        try (Scanner scanner = new Scanner(NaiveTweetPreprocessor.class.getResourceAsStream("/negative-words.txt"))) {
            while (scanner.hasNextLine()) {
                negativeWords.add(scanner.nextLine());
            }
        }
        /* modified versions from: http://sentiment.christopherpotts.net/code-data/happyfuntokenizing.py; Copyright 2011, Christopher Potts */
        allEmoticonPattern = Pattern.compile("[<>]?" + // optional hat
                "[:;=8Xx]" + // eyes
                "[\\-o\\*']?" + // optional nose
                "[\\)\\]\\(\\[dDpP/\\}\\{@\\|\\\\\\*]" +  // mouth
                "|" +
                "[\\)\\]\\(\\[dDpP/\\}\\{@\\|\\\\\\*]" + // mouth
                "[\\-o\\*']?" + // optional nose
                "[:;=8Xx]" + // eyes
                "[<>]?" + // optional hat
                "|" +
                "o\\.?O|O\\.?o" + // o.O, O.o
                "|-\\.-|\\._\\." +
                "|\\^\\^");

        positiveEmoticonsPattern = Pattern.compile("[<>]?" + // optional hat
                "[:;=8Xx]" + // eyes
                "[\\-o\\*']?" + // optional nose
                "[\\)\\]dDpP\\}\\*]" +  // mouth
                "|" +
                "[\\(\\[pP\\{\\*]" + // mouth
                "[\\-o\\*']?" + // optional nose
                "[:;=8Xx]" + // eyes
                "[<>]?" + // optional hat
                "|\\^\\^");

        negativeEmoticonsPattern = Pattern.compile("[<>]?" + // optional hat
                "[:;=8Xx]" + // eyes
                "[\\-o\\*']?" + // optional nose
                "[\\(\\[/\\{@\\|\\\\]" +  // mouth
                "|" +
                "[\\)\\]dD/\\}@\\|\\\\]" + // mouth
                "[\\-o\\*']?" + // optional nose
                "[:;=8Xx]" + // eyes
                "[<>]?" + // optional hat
                "|" +
                "o\\.?O|O\\.?o" + // o.O, O.o
                "|-\\.-|\\._\\.");

        ignoredAllCapsWords = new HashSet<>();
        Collections.addAll(ignoredAllCapsWords, "DM", "I");

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

    private HashMap<String, Double> generateFeatureHashMap() {
        HashMap<String, Double> featureHashMap = new HashMap<>();
        featureHashMap.put(POSITIVE_SMILIES_FEATURE, 0.0);
        featureHashMap.put(NEGATIVE_SMILIES_FEATURE, 0.0);
        featureHashMap.put(POSITIVE_WORDS_FEATURE, 0.0);
        featureHashMap.put(NEGATIVE_WORDS_FEATURE, 0.0);
        featureHashMap.put(POSITIVE_HASHTAGS_FEATURE, 0.0);
        featureHashMap.put(NEGATIVE_HASHTAGS_FEATURE, 0.0);
        featureHashMap.put(HASHTAGS_COUNT_FEATURE, 0.0);
        featureHashMap.put(ENLARGED_WORD_COUNT_FEATURE, 0.0);
        featureHashMap.put(CONSECUTIVE_EXCLAMATION_MARKS_COUNT_FEATURE, 0.0);
        featureHashMap.put(CONSECUTIVE_QUESTION_MARKS_COUNT_FEATURE, 0.0);
        featureHashMap.put(HAS_ALL_CAPS_WORDS_FEATURE, 0.0);
        featureHashMap.put(ALL_CAPS_FEATURE, 0.0);

        return featureHashMap;
    }

    @Override
    public Tweet preprocess(Tweet tweet) {
        HashMap<String, Double> featureMap = generateFeatureHashMap();
        String content = tweet.getContent();

        checkIfWordsAreAllCaps(content, featureMap);

        // to lowercase
        content = content.toLowerCase();

        /* replace search term..but only if its not the training search for :) or :( */
        String searchTerm = tweet.getSearchTerm();
        if (searchTerm != null && !searchTerm.trim().isEmpty() && !":)".equals(searchTerm) && !":(".equals(searchTerm)) {
            content = content.replace(searchTerm.trim().toLowerCase(), " ");
        }

        // remove hash symbol from the content
        // for equal treatment of tweets using hashtags
        // and tweets that use none
        tweet.setHashtags(getHashtags(content));
//        content = replaceHashTags(content);
        content = extractHashtagsToFeatureMap(content, featureMap);
        content = content.replace("#", "");

        // same with other users
        tweet.setMentionedUsers(getUsers(content));
        //@TODO: Experiment if it makes more sense to
        //  1 leave users as they are, including the @
        //  2 remove users completely including the name
//        content = content.replace("@", "");
        content = content.replaceAll("@[0-9a-zA-Z\\-_]*", " MENTION ");

        // normalize urls
        content = content.replace("www.", "").replace("http://", "").replace("https://", "");
        tweet.setUrls(getUrls(content));
        // remove urls
        content = content.replaceAll("[a-zA-Z0-9]+\\.[a-zA-Z]{2,10}[a-zA-Z0-9/.?#&]*", " URL ");

        content = extractSmiliesToFeatureMap(content, featureMap);
        // replace all "....."
        content = replaceConsecutiveDots(content);

        // normalize repeated letters
        content = replaceConsecutiveLetters(content, featureMap);

        // normalize spaces
        content = content.replaceAll("\\s+", " ");

        content = content.replace("'", "");
        content = content.replace("\"", "");

        // get number of positive and negative words
        extractSentimentWordsToFeatureMap(content, featureMap);

        content = content.trim();
        tweet.setContent(content);
        tweet.setFeatureMap(featureMap);
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

    /**
     *
     * @param input
     * @return
     */
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

    private String extractSmiliesToFeatureMap(String input, HashMap<String, Double> featureMap) {
        String output = input;

        double positiveSmiliesCounter = 0;

        /* special case for larger positive standard smilies */
        Matcher bigPositiveMatcher = bigPositiveEmoticonsPattern.matcher(output);
        while (bigPositiveMatcher.find()) {
            positiveSmiliesCounter += 1.5;
        }
        output = bigPositiveMatcher.replaceAll(" ");
        /* normal positive smilies */
        Matcher positiveMatcher = positiveEmoticonsPattern.matcher(output);
        while (positiveMatcher.find()) {
            positiveSmiliesCounter++;
        }
        /* remove positive smilies */
        output = positiveMatcher.replaceAll(" ");
        featureMap.put(POSITIVE_SMILIES_FEATURE, positiveSmiliesCounter);

        double negativeSmiliesCounter = 0;

        /* special case for larger negative standard smilies */
        Matcher bigNegativeMatcher = bigNegativeEmoticonsPattern.matcher(output);
        while (bigNegativeMatcher.find()) {
            negativeSmiliesCounter += 1.5;
        }
        output = bigNegativeMatcher.replaceAll(" ");
        /* normal negative smilies */
        Matcher negativeMatcher = negativeEmoticonsPattern.matcher(output);
        while (negativeMatcher.find()) {
            negativeSmiliesCounter++;
        }
        /* remove negative smilies */
        output = negativeMatcher.replaceAll(" ");
        featureMap.put(NEGATIVE_SMILIES_FEATURE, negativeSmiliesCounter);

        return output;
    }

    private String extractHashtagsToFeatureMap(String input, HashMap<String, Double> featureMap) {
        String output = input;
        Matcher matcher = hashtagPattern.matcher(output);
        double positiveHashtagsCounter = 0;
        double negativeHashtagsCounter = 0;
        double hashTagsCounter = 0;
        while (matcher.find()) {
            String hashTag = matcher.group();
            if (positiveHashTags.contains(hashTag)) {
                positiveHashtagsCounter++;
            } else if (negativeHashTags.contains(hashTag)) {
                negativeHashtagsCounter++;
            }
            hashTagsCounter++;
            output = Pattern.compile(hashTag, Pattern.LITERAL).matcher(output).replaceFirst(Matcher.quoteReplacement(" " + hashTag + " "));
//            output = output.replace(hashTag, " " + hashTag + " ");
        }

        featureMap.put(POSITIVE_HASHTAGS_FEATURE, positiveHashtagsCounter);
        featureMap.put(NEGATIVE_HASHTAGS_FEATURE, negativeHashtagsCounter);
        featureMap.put(HASHTAGS_COUNT_FEATURE, hashTagsCounter);

        return output;
    }

    private void checkIfWordsAreAllCaps(String content, HashMap<String, Double> featureMap) {
        StringTokenizer tokenizer = new StringTokenizer(content, " \n\t.,;:'\"()?!");

        boolean containsAllCapsWord = false;
        boolean isAllCaps = true;
        boolean processedAtLeastOne = false;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            /* ignore special values */
            if (ignoredAllCapsWords.contains(token)) continue;
            processedAtLeastOne = true; // workaround for single word tweets with an ignored all caps word
            if (token.matches("[A-Z]+")) {
                containsAllCapsWord = true;
            } else if (isAllCaps) {
                isAllCaps = false;
            }
        }

        featureMap.put(HAS_ALL_CAPS_WORDS_FEATURE, containsAllCapsWord ? 1.0 : 0.0);
        featureMap.put(ALL_CAPS_FEATURE, isAllCaps && processedAtLeastOne ? 1.0 : 0.0);
    }

    private void extractSentimentWordsToFeatureMap(String content, HashMap<String, Double> featureMap) {
        StringTokenizer tokenizer = new StringTokenizer(content, " \n\t.,;:'\"()?!");

        double negativeWordCount = 0;
        double positiveWordCount = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (negativeWords.contains(token)) {
                negativeWordCount++;
            } else if (positiveWords.contains(token)) {
                positiveWordCount++;
            }
        }

        featureMap.put(NEGATIVE_WORDS_FEATURE, negativeWordCount);
        featureMap.put(POSITIVE_WORDS_FEATURE, positiveWordCount);
    }

    /**
     * Replaces consecutive letters that occur more than twice with the single letter.
     * E.g.: heyyyyyyyyyyyy -> hey; hello -> hello.
     * <p/>
     * New: we now also add the label "ENLONGEDWORD" to signal a processed word with double letters.
     * This is added at the end of the output and the number of labels depends on the number of words that got corrected.
     *
     * @param input the content of the tweet
     * @param featureMap
     * @return the processed content
     */
    private String replaceConsecutiveLetters(String input, HashMap<String, Double> featureMap) {
        String output = input;
        Matcher matcher = doublePattern.matcher(output);
        double consecutiveLettersCounter = 0;
        double consecutiveExclamationsCounter = 0;
        double consecutiveQuestionsCounter = 0;
        while (matcher.find()) {
            String group1 = matcher.group(1);
            String group2 = matcher.group(2); // the single char that got repeated
            if (group1.length() > 2) {
//                output = output.replace(group1, group2 + group2 + group2);
                if ("!".equals(group2)) {
                    consecutiveExclamationsCounter++;
                } else if ("?".equals(group2)) {
                    consecutiveQuestionsCounter++;
                } else {
                    consecutiveLettersCounter++;
                }
                if (group1.length() > 3) {
                    String oldOutput = output;
                    output = Pattern.compile(group1, Pattern.LITERAL).matcher(output).replaceFirst(Matcher.quoteReplacement(group2 + group2 + group2));
//                    output = output.replace(group1, group2 + group2 + group2);
                    log.trace("Group1: {}, Group2: {}. {} -> {}", group1, group2, oldOutput, output);
                }
            }
        }
        featureMap.put(ENLARGED_WORD_COUNT_FEATURE, consecutiveLettersCounter);
        featureMap.put(CONSECUTIVE_EXCLAMATION_MARKS_COUNT_FEATURE, consecutiveExclamationsCounter);
        featureMap.put(CONSECUTIVE_QUESTION_MARKS_COUNT_FEATURE, consecutiveQuestionsCounter);
        return output;
    }

    private String replaceConsecutiveDots(String input) {
        String output = input;
        Matcher matcher = dotsPattern.matcher(input);
        while (matcher.find()) {
            output = Pattern.compile(matcher.group(), Pattern.LITERAL).matcher(output).replaceFirst(DOTS);
//            output = output.replace(matcher.group(), DOTS);
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

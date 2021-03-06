/*
 * Tweet Sentiment Analysis
 * Vienna UT
 *
 * Advanced Internet Computing
 * Winter term 2014/15
 * Group 01
 * 
 * Patrick Löwenstein
 * Thomas Schreiber
 * Alexander Suchan
 * Stefan Victora
 * Andreas Waltenberger
 *
 * All rights reserved
 */

package at.tuwien.aic.tweetanalysis;

import asg.cliche.*;
import at.tuwien.aic.tweetanalysis.aggregator.SimpleAggregator;
import at.tuwien.aic.tweetanalysis.classifier.IClassifier;
import at.tuwien.aic.tweetanalysis.classifier.WekaClassifier;
import at.tuwien.aic.tweetanalysis.entities.ClassifiedTweet;
import at.tuwien.aic.tweetanalysis.entities.Tweet;
import at.tuwien.aic.tweetanalysis.preprocessing.StandardTweetPreprocessor;
import at.tuwien.aic.tweetanalysis.preprocessing.TrainingDataPreprocessor;
import at.tuwien.aic.tweetanalysis.provider.TweetProvider;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.RateLimitStatus;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 *
 *
 *
 * @author Group 1
 */
public class TweetAnalysis {

    public static final Logger log = LoggerFactory.getLogger(TweetAnalysis.class);
    public static Shell shell = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    TweetProvider tweetProvider;
    IClassifier classifier;
    StandardTweetPreprocessor standardTweetPreprocessor;

    public TweetAnalysis() throws Exception {
        classifier = loadClassifierFromModel("smo", false);
        standardTweetPreprocessor = new StandardTweetPreprocessor();
        tweetProvider = new TweetProvider();
//        classifier = new WekaClassifier();
    }

    private static IClassifier loadClassifierFromModel(String classifierType, boolean useSmilies) throws IOException {
        IClassifier tweetTest;

//        if (!classifierType.startsWith("smo") && !"bayes".equals(classifierType)) {
//            throw new IllegalArgumentException("Unknown classifier type '" + classifierType + "'. Only 'smo' and 'bayes' are valid");
//        }

        int size;
        String baseDirectory = "/trainingData";

        if (classifierType.endsWith("_large")) {
            baseDirectory += "/large_models";
            classifierType = classifierType.replace("_large", "");
            size = 18000;
        } else if (classifierType.endsWith("_small")) {
            baseDirectory += "/small_models";
            classifierType = classifierType.replace("_small", "");
            size = 1000;
        } else {
            baseDirectory += "/models";
            size = 6000;
        }

        String modelName = baseDirectory + "/" + classifierType;
        String trainingInstancesName = baseDirectory + "/" + "trainData_bigramme";
        if (useSmilies) {
            modelName += "_smilies";
            trainingInstancesName += "_smilies";
        }
        modelName += ".model";
        trainingInstancesName += ".arff";
        try (InputStream modelStream = TweetAnalysis.class.getResourceAsStream(modelName);
             InputStream instancesStream = TweetAnalysis.class.getResourceAsStream(trainingInstancesName)) {
            tweetTest = new WekaClassifier(modelStream, instancesStream);
        }
        System.out.println("Loaded " + classifierType + " model from file. Model contains smilies: " + useSmilies + ". Instances used for training: " + size);
        return tweetTest;
    }


    private boolean correctDate(String date, Date fromDate) {
        try {
            Date untilDate = dateFormat.parse(date);

            if(fromDate != null && fromDate.after(untilDate)) {
                System.out.println("Please enter a date after " + dateFormat.format(fromDate));
                return false;
            }

            return true;
        } catch(Exception e) {
            System.out.println("Please enter a correct date of format YYYY-MM-DD.");
            return false;
        }
    }

    private boolean correctLocationRadius(String locRadius) {
        String hint = "Please enter a correct location of format lat,lon,radius as double values.";
        try {
            String[] split = locRadius.split(",");
            if(split.length != 3) { System.out.println(hint); return false; }

            Double.parseDouble(split[0]);
            Double.parseDouble(split[1]);
            Double.parseDouble(split[2]);

            return true;
        } catch(Exception e) {
            System.out.println(hint);
            return false;
        }
    }

    private boolean correctLanguage(String language) {
        List<String> availLangs = Arrays.asList(Locale.getISOLanguages());
        if(availLangs.contains(language)) {
            return true;
        } else {
            System.out.println("Please enter a valid ISO language code.");
            return false;
        }
    }

    @Command
    public void quit() {
        tweetProvider.shutdown();
        System.exit(0);
    }

    @Command
    public void help() throws CLIException {
        shell.processLine("?list");
    }

    @Command
    public void help(@Param(name="command", description="Help for command") String command) throws CLIException {
        shell.processLine("?help " + command);
    }

    @Command
     public void load(@Param(name = "classifierName", description = "the name of the classifier. Valid: 'smo' and 'bayes'") String classifierName,
                      @Param(name = "smilies", description = "load model with smilies") boolean smilies) throws CLIException, IOException {
        classifier = loadClassifierFromModel(classifierName, smilies);
    }

    @Command
    public void classify(@Param(name = "tweet", description="Single tweet to classify") String tweet) throws Exception {
        Tweet t = new Tweet();
        t.setContent(tweet);

        ArrayList<Tweet> tweets = new ArrayList<>();
        tweets.add(t);
        standardTweetPreprocessor.preprocess(tweets);
        double[] fDistribution = classifier.classifyTweet(t);

        logResults(t, fDistribution, true);
    }

    @Command
    public void selfTrain() throws Exception {
        System.out.println("Train classifier on basic training set...");
        classifier = new WekaClassifier();
        System.out.println("Basic smo classifier ready");
    }

    @Command
    public void testAgainstTweets() throws Exception {
        System.out.println("Test classifier against a manually created testing-set...");

        List<Tweet> t = TrainingDataPreprocessor.preprocessAndCreateTweets(standardTweetPreprocessor, "/trainingData/manuallyCreatedTraindata/tweets.csv", 600);
        classifier.testClassifierAgainstPreprocessedTweets(t, true);

        System.out.println("done");
    }

    @Command
    public void testAgainstTweetsAll(@Param(name = "smilies", description = "test models that contain smilies") boolean smilies) throws Exception {
        System.out.println("Test all classifiers against a manually created testing-set. Test models with smilies: " + smilies);

        ArrayList<String> classifiers = new ArrayList<>();
        Collections.addAll(classifiers, "smo_small", "smo", "smo_large",
                "bayes_small", "bayes", "bayes_large",
                "c-svc_small", "c-svc", "c-svc_large",
                "nu-svc_small", "nu-svc", "nu-svc_large");

        List<Tweet> t = TrainingDataPreprocessor.preprocessAndCreateTweets(standardTweetPreprocessor, "/trainingData/manuallyCreatedTraindata/tweets.csv", 600);
        for (String c : classifiers) {
            // without smilies
            classifier = loadClassifierFromModel(c, false);
            classifier.testClassifierAgainstPreprocessedTweets(t, true);

            if (smilies) {
                // with smilies
                classifier = loadClassifierFromModel(c, true);
                classifier.testClassifierAgainstPreprocessedTweets(t, true);
            }
        }

        System.out.println("done");
    }

    @Command
    public String analyze(@Param(name="query", description="Twitter search query") String query,
                         @Param(name="count", description="Number of tweets to analyse") int count) throws Exception {

        RateLimitStatus status = tweetProvider.getRateLimitStatus();
        if(status != null && status.getRemaining() == 0) {
            System.out.println("This application exceeded the Twitter API rate limiting." + (status != null ? " You have to wait ~" + status.getSecondsUntilReset() + " seconds until the next request can be made." : ""));
            return null;
        }

        Future<List<Tweet>> tweets = tweetProvider.getTweets(query, count);
        List<Tweet> tweetList = tweets.get();

        standardTweetPreprocessor.preprocess(tweetList);

        List<ClassifiedTweet> classifiedTweetList = new LinkedList<>();

        for (Tweet tweet : tweetList) {
            classifiedTweetList.add(new ClassifiedTweet(tweet, classifier.classifyTweet(tweet)));
        }

        SimpleAggregator aggregator = new SimpleAggregator();
        return "Got " + classifiedTweetList.size() + " tweets with a combined sentiment of " + aggregator.calculate(classifiedTweetList);
    }

    @Command
    public String analyze(@Param(name="query", description="Twitter search query") String query,
                         @Param(name="count", description="Number of tweets to analyse") int count,
                         @Param(name="filters", description="{fulg} - filter tweets by: From date, Until date, Language, Geolocation") String filters) throws Exception {

        RateLimitStatus status = tweetProvider.getRateLimitStatus();
        if(status != null && status.getRemaining() == 0) {
            System.out.println("This application exceeded the Twitter API rate limiting." + (status != null ? " You have to wait ~" + status.getSecondsUntilReset() + " seconds until the next request can be made." : ""));
            return null;
        }

        Date beginDate = null;
        Date endDate = null;
        String language = null;
        GeoLocation location = null;
        Double radius = null;

        Scanner s = new Scanner(System.in);

        if(filters.indexOf('f') >= 0) {
            String beginDateString;
            do {
                System.out.print("analyze \""+query+"\", from date> ");
                beginDateString = s.nextLine();
                if(beginDateString.equals("exit")) { return null; }
            } while(!correctDate(beginDateString, null));

            beginDate = dateFormat.parse(beginDateString);
        }

        if(filters.indexOf('u') >= 0) {
            String endDateString;
            do {
                System.out.print("analyze \""+query+"\", until date> ");
                endDateString = s.nextLine();
                if(endDateString.equals("exit")) { return null; }
            } while(!correctDate(endDateString, beginDate));
        }

        if(filters.indexOf('l') >= 0) {

            do {
                System.out.print("analyze \""+query+"\", language> ");
                language = s.nextLine();
                if(language.equals("exit")) { return null; }
            } while(!correctLanguage(language));
        }

        if(filters.indexOf('g') >= 0) {
            String temp = null;
            do {
                System.out.print("analyze \""+query+"\", location> ");
                temp = s.nextLine();
                if(temp.equals("exit")) { return null; }
            } while(!correctLocationRadius(temp));

            location = new GeoLocation(Double.parseDouble(temp.split(",")[0]), Double.parseDouble(temp.split(",")[1]));
            radius = Double.parseDouble(temp.split(",")[2]);
        }

        Future<List<Tweet>> tweets = tweetProvider.getTweets(query, count, beginDate, endDate, language, location, radius);
        List<Tweet> tweetList = tweets.get();

        standardTweetPreprocessor.preprocess(tweetList);

        List<ClassifiedTweet> classifiedTweetList = new LinkedList<>();

        for (Tweet tweet : tweetList) {
            double[] fDistribution = classifier.classifyTweet(tweet);
            if (filters.contains("d")) {
                logResults(tweet, fDistribution, filters.contains("d+"));
            }
            classifiedTweetList.add(new ClassifiedTweet(tweet, fDistribution));
        }

        SimpleAggregator aggregator = new SimpleAggregator();
        return "Got " + classifiedTweetList.size() + " tweets with a combined sentiment of " + aggregator.calculate(classifiedTweetList);
    }

    public static void main(String[] args) throws Exception {
        shell = ShellFactory.createConsoleShell("", "Tweet Sentiment Analysis", new TweetAnalysis());
        shell.commandLoop();
//        testClassifier();
//        testLiveData("Nike", 50);
//        TweetProvider tweetProvider1 = new TweetProvider();
//        getLiveData(":(", ".*:-?\\).*|.*@Harry_Styles.*|.*follow\\s?(me|back).*", 10000, "negative", "neg3.csv", false, true, tweetProvider1);
//        getLiveData(":)", ".*:-?\\(.*|.*@Harry_Styles.*|.*follow\\s?(me|back).*", 10000, "positive", "pos3.csv", false, true, tweetProvider1);
        // yeah..one direction gets lots of spam bots..I mean an ridiculous amount..so just ignore him. He will not be missed in our analysis.
//        tweetProvider1.shutdown();
    }
    
    private static void testLiveData(String searchTerm, int count) throws Exception {
        TweetProvider tweetProvider = new TweetProvider();

        Future<List<Tweet>> tweets = tweetProvider.getTweets(searchTerm, count);

        List<Tweet> tweetList = tweets.get();

        testClassifierOnTweets(tweetList);

        tweetProvider.shutdown();
    }

    private static void getLiveData(String searchTerm, String ignorePattern, int count, String sentiment, String fileName, boolean append, boolean writeHeader, TweetProvider tweetProvider) throws Exception {
        Future<List<Tweet>> tweets = tweetProvider.getTweets(searchTerm, count);
        List<Tweet> tweetList = tweets.get();

        try (CSVWriter csvOutput = new CSVWriter(new BufferedWriter(new FileWriter(fileName, append)))) {
            if (writeHeader) {
                csvOutput.writeNext(new String[]{"Tweet", "Sentiment"}, false);
            }

            for (Tweet tweet : tweetList) {
                String z = tweet.getContent();
                z = z.replace("\n", "");

                if (!z.matches(ignorePattern)) {
                    csvOutput.writeNext(new String[]{z, sentiment}, true);
                } else {
                    log.trace("Ignoring '{}'. It contains the ignore pattern.", z);
                }
            }
        }
    }

    private static void testClassifier() throws Exception {
        IClassifier tweetTest;
        tweetTest = loadClassifierFromModel("smo", false);
//        tweetTest = new WekaClassifier();

        // Use the Classifier to evaluate a Tweet
        Tweet t = new Tweet();
//        t.setContent("bad bad bad bad :(");
//        t.setContent("#hate sad :( :(");
        //t.setContent("happy joy :) :) #happy congratulations");
//        t.setContent("I love them, thank u mum !! <3 http://t.co/rMQEeRYhnT");
        t.setContent("fuck this shit");
//        t.setContent("fuck wit my shit http://t.co/rscJB4yd0G");

        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        ArrayList<Tweet> tweets = new ArrayList<>();
        tweets.add(t);
        standardTweetPreprocessor.preprocess(tweets);
        double[] fDistribution = tweetTest.classifyTweet(t);
        logResults(t, fDistribution, true);
    }

    private static void testClassifierOnTweets(List<Tweet> tweets) throws Exception {
        /* load model from file */
        IClassifier classifier;
        classifier = loadClassifierFromModel("smo", true);
//        classifier = new WekaClassifier();

        /* preprocess tweets */
        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        standardTweetPreprocessor.preprocess(tweets);

        /* classify */
        double[] fDistribution;
        for (Tweet tweet : tweets) {
            fDistribution = classifier.classifyTweet(tweet);
            logResults(tweet, fDistribution, true);
        }
    }

    private static void logResults(Tweet tweet, double[] fDistribution, boolean verbose) {
        log.info("Tweet text: {}", tweet.getOriginalContent().replace("\n", ""));
        log.info("Processed:  {}", tweet.getContent());
        log.info("positive:   {}", fDistribution[1]);
        log.info("negative:   {}", fDistribution[0]);
        int retweetCount = tweet.getRetweetCount();
        int favoriteCount = tweet.getFavoriteCount();
        if (verbose) {
            StringBuilder str = new StringBuilder();
            for (Map.Entry<String, Double> featureEntry : tweet.getFeatureMap().entrySet()) {
                if (featureEntry.getValue() != 0.0) {
                    str.append(featureEntry.getKey().toLowerCase()).append(": ").append(featureEntry.getValue()).append("; ");
                }
            }
            String verboseOutput = str.toString();
            if (!verboseOutput.isEmpty()) {
                log.info("features:   {}", verboseOutput);
            }
        }
        log.info("weight:     {}; retweets: {}; favorites: {}\n", SimpleAggregator.weight(tweet), retweetCount, favoriteCount);
    }

}
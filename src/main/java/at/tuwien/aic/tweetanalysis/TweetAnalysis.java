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
 * Stefan Victoria
 * Andreas Waltenberg
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
import at.tuwien.aic.tweetanalysis.provider.TweetProvider;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.RateLimitStatus;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
    TweetProvider tweetProvider = new TweetProvider();
    IClassifier classifier = new WekaClassifier();
    StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();

    public TweetAnalysis() throws Exception { }

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
            classifiedTweetList.add(new ClassifiedTweet(tweet, classifier.classifyTweet(tweet)));
        }

        SimpleAggregator aggregator = new SimpleAggregator();
        return "Got " + classifiedTweetList.size() + " tweets with a combined sentiment of " + aggregator.calculate(classifiedTweetList);
    }

    public static void main(String[] args) throws Exception {
        shell = ShellFactory.createConsoleShell("", "Tweet Sentiment Analysis", new TweetAnalysis());
        shell.commandLoop();
    }

    private static void testLiveData() throws Exception {
        TweetProvider tweetProvider = new TweetProvider();

        Future<List<Tweet>> tweets = tweetProvider.getTweets(":)", 20, null, null, "en", null, null);

        List<Tweet> tweetList = tweets.get();

        testClassifierOnTweets(tweetList);

        tweetProvider.shutdown();
    }

    private static void getLiveData() throws Exception {

        TweetProvider tweetProvider = new TweetProvider();

        Future<List<Tweet>> tweets = tweetProvider.getTweets(":D", 200, null, null, "en", null, null);
        List<Tweet> tweetList = tweets.get();

        try (CSVWriter csvOutput = new CSVWriter(new BufferedWriter(new FileWriter("pos.csv")))) {
            csvOutput.writeNext(new String[]{"Tweet", "Sentiment"}, false);

            for (Tweet tweet : tweetList) {
                String z = tweet.getContent();
                z = z.replace("\n", "");

                csvOutput.writeNext(new String[]{z, "positive"}, false);
            }
        }
        tweetProvider.shutdown();
    }

    private static void testClassifier() throws Exception {
        IClassifier tweetTest;
        // TODO:this doesnt work at the moment!
        //try (InputStream modelStream = TweetAnalysis.class.getResourceAsStream("/fff1.model")) {
        //    tweetTest = new WekaClassifier(modelStream);
        //}
        tweetTest = new WekaClassifier();

        // Use the Classifier to evaluate a Tweet
        Tweet t = new Tweet();
        //t.setContent("bad bad bad bad :(");
//        t.setContent("#hate sad :( :(");
        t.setContent("happy joy :) :) #happy congratulations");
//        t.setContent("I love them, thank u mum !! <3 http://t.co/rMQEeRYhnT");

        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        ArrayList<Tweet> tweets = new ArrayList<>();
        tweets.add(t);
        standardTweetPreprocessor.preprocess(tweets);
        double[] fDistribution = tweetTest.classifyTweet(t);

        logResults(t.getContent(), fDistribution);
    }

    private static void testClassifierOnTweets(List<Tweet> tweets) throws Exception {
        /* load model from file */
        IClassifier classifier;
//        try (InputStream modelStream = TweetAnalysis.class.getResourceAsStream("/trainingData/tweet-model.model")) {
//            classifier = new WekaClassifier(modelStream);
//        }
        classifier = new WekaClassifier();

        /* preprocess tweets */
        StandardTweetPreprocessor standardTweetPreprocessor = new StandardTweetPreprocessor();
        standardTweetPreprocessor.preprocess(tweets);

        /* classify */
        double[] fDistribution;
        for (Tweet tweet : tweets) {
            fDistribution = classifier.classifyTweet(tweet);
            logResults(tweet.getContent(), fDistribution);
        }
    }

    private static void logResults(String content, double[] fDistribution) {
        log.info("Evaluation of a String: {}", content);
        log.info("positive: {}", fDistribution[1]);
        log.info("negative: {}\n", fDistribution[0]);
    }

}
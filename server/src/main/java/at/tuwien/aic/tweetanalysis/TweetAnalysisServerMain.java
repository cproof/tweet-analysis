/*
 */

package at.tuwien.aic.tweetanalysis;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tuwien.aic.tweetanalysis.aggregator.SimpleAggregator;
import at.tuwien.aic.tweetanalysis.classifier.IClassifier;
import at.tuwien.aic.tweetanalysis.classifier.WekaClassifier;
import at.tuwien.aic.tweetanalysis.entities.ClassifiedTweet;
import at.tuwien.aic.tweetanalysis.entities.Tweet;
import at.tuwien.aic.tweetanalysis.preprocessing.StandardTweetPreprocessor;
import at.tuwien.aic.tweetanalysis.provider.TweetProvider;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import twitter4j.*;

/**
 *
 * @author Thomas
 */
public class TweetAnalysisServerMain {
    protected static TweetProvider tweetProvider;
    protected static IClassifier classifier;
    protected static StandardTweetPreprocessor standardTweetPreprocessor;
    protected static SimpleAggregator aggregator;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static IClassifier loadClassifierFromModel(String classifierType, boolean useSmilies) throws IOException {
        IClassifier tweetTest;

        if (!classifierType.startsWith("smo") && !"bayes".equals(classifierType)) {
            throw new IllegalArgumentException("Unknown classifier type '" + classifierType + "'. Only 'smo' and 'bayes' are valid");
        }

        String modelName = "/trainingData/tweet-model_new_" + classifierType;
        String trainingInstancesName = "/trainingData/trainData_our_bigramme_selected";
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
        System.out.println("Loaded " + classifierType + " model from file. Model contains smilies: " + useSmilies);
        return tweetTest;
    }
    
    public static void main(String[] args) throws InterruptedException, Exception {
        classifier = loadClassifierFromModel("smo", false);
        standardTweetPreprocessor = new StandardTweetPreprocessor();
        tweetProvider = new TweetProvider();
        aggregator = new SimpleAggregator();


        final Server server = new Server(8080);
        server.setHandler(new TweetAnalysisServerHandler());
        server.start();
        
        System.out.println("Server started\n\nPress return to exit");
        System.in.read();
        server.stop();
        
    }

    public static class TweetAnalysisServerHandler extends AbstractHandler {
        //Entry point
        @Override
        public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
            baseRequest.setHandled(true);
            response.setContentType("application/json;charset=utf-8");            
            response.setHeader("Access-Control-Allow-Origin", "*");
            
            switch(target) {
                case "/search":
                    handleSearchRequest(baseRequest, request, response);
                    break;
                case "/":
                default:
                    handleBadRequest(baseRequest, request, response);
            }
            
        }
        
        private void handleSearchRequest(Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException {
            try {
                JSONObject ret = new JSONObject();
                
                String query = baseRequest.getParameter("q");
                if(query == null) {
                    handleBadRequest(baseRequest, request, response);
                    return;
                }

                String countString = baseRequest.getParameter("c");
                int count;
                try {
                    count = Integer.parseInt(countString);
                } catch(Exception e) {
                    count = 10;
                }

                String classifierString = baseRequest.getParameter("cl");
                if(classifierString == null) {
                    classifier = loadClassifierFromModel("smo", false);
                } else {
                    switch(classifierString) {
                        case "smoSmileys":
                            classifier = loadClassifierFromModel("smo", true);
                            break;
                        case "bayes":
                            classifier = loadClassifierFromModel("bayes", false);
                            break;
                        case "bayesSmileys":
                            classifier = loadClassifierFromModel("bayes", true);
                            break;
                        default:
                            classifier = loadClassifierFromModel("smo", false);
                            break;
                    }
                }

                String beginDateString = baseRequest.getParameter("bd");
                Date beginDate = null;
                try {
                    beginDate = dateFormat.parse(beginDateString);
                } catch(Exception e) {}

                String endDateString = baseRequest.getParameter("ed");
                Date endDate = null;
                try {
                    endDate = dateFormat.parse(endDateString);
                } catch(Exception e) {}

                String locationString = baseRequest.getParameter("gl");
                GeoLocation location = null;
                String radiusString = baseRequest.getParameter("r");
                Double radius = null;
                try {
                    String[] split = locationString.split(",");
                    if(split.length == 2) {
                        location = new GeoLocation(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                        radius = Double.parseDouble(radiusString);
                    }
                } catch(Exception e) {
                    location = null;
                    radius = null;
                }

                String language = baseRequest.getParameter("l");
                
                boolean showTweets = (baseRequest.getParameter("verbose") != null && !baseRequest.getParameter("verbose").equals("false"));

                RateLimitStatus status = tweetProvider.getRateLimitStatus();
                if(status != null && status.getRemaining() == 0) {
                    handleRateLimitingExceeded(baseRequest, request, response);
                    return;
                }

                Future<List<Tweet>> tweets = tweetProvider.getTweets(query, count, beginDate, endDate, language, location, radius);
                List<Tweet> tweetList = null;

                try {
                    tweetList = tweets.get();
                } catch(Exception e) {
                    handleInternalServerError(baseRequest, request, response);
                    return;
                }

                standardTweetPreprocessor.preprocess(tweetList);

                List<ClassifiedTweet> classifiedTweetList = new LinkedList<>();

                for (Tweet tweet : tweetList) {
                    classifiedTweetList.add(new ClassifiedTweet(tweet, classifier.classifyTweet(tweet)));
                }

                ret.put("sentiment", aggregator.calculate(classifiedTweetList));
                
                
                //maybe attach tweets?
                JSONArray tweetsJSON = null;
                if (showTweets) {
                    tweetsJSON = new JSONArray();
                    for(ClassifiedTweet t : classifiedTweetList) {
                        tweetsJSON.put(verboseJSON(t.tweet, t.fDistribution, true));
                    }
                }
                ret.put("tweets", tweetsJSON);
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(ret.toString());
            } catch (JSONException ex) {
                handleBadRequest(baseRequest, request, response);
            }
        }
        
        /**
         * Product detailed JSON for a single tweet
         * (copied from TweetAnalysis:logResults()
         * @param tweet
         * @param fDistribution
         * @param verbose
         * @return
         * @throws JSONException 
         */
        private static JSONObject verboseJSON(Tweet tweet, double[] fDistribution, boolean verbose) throws JSONException {
            JSONObject ret = new JSONObject();
            ret.put("original",tweet.getOriginalContent().replace("\n", ""));
            ret.put("processed", tweet.getContent());
            ret.put("positive", fDistribution[1]);
            ret.put("negative", fDistribution[0]);
            ret.put("id", tweet.getId());
            ret.put("author", tweet.getAuthor());
            
            int retweetCount = tweet.getRetweetCount();
            int favoriteCount = tweet.getFavoriteCount();
            
            JSONObject features = new JSONObject();
            if (verbose) { //@TODO: allow different settings
                for (Map.Entry<String, Double> featureEntry : tweet.getFeatureMap().entrySet()) {
                    if (featureEntry.getValue() != 0.0) {
                        features.put(featureEntry.getKey().toLowerCase(), featureEntry.getValue());
                    }
                }
                if (features.length() > 0) {
                    ret.put("featureMap", features);
                }
            }
            
            ret.put("retweets", retweetCount);
            ret.put("favorites", favoriteCount);
            ret.put("weight", SimpleAggregator.weight(tweet));
            
            return ret;
        }

        private void handleBadRequest(Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"Error\" : \"Bad request\"}");
        }

        private void handleRateLimitingExceeded(Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().print("{\"Error\" : \"Rate limiting exceeded\"}");
        }

        private void handleInternalServerError(Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"Error\" : \"Internal server error\"}");
        }
        
    }
}

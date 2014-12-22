/*
 */

package at.tuwien.aic.tweetanalysis;

import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import static at.tuwien.aic.tweetanalysis.TweetAnalysis.shell;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

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
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.RateLimitStatus;

/**
 *
 * @author Thomas
 */
public class TweetAnalysisServerMain {
    protected static TweetProvider tweetProvider;
    protected static IClassifier classifier;
    protected static StandardTweetPreprocessor standardTweetPreprocessor;
    protected static SimpleAggregator aggregator;

    private static IClassifier loadClassifiedFromModel(String classifierType, boolean useSmilies) throws IOException {
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
        classifier = loadClassifiedFromModel("smo", false);
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
                    count = 100;
                }

                RateLimitStatus status = tweetProvider.getRateLimitStatus();
                if(status != null && status.getRemaining() == 0) {
                    handleRateLimitingExceeded(baseRequest, request, response);
                    return;
                }

                Future<List<Tweet>> tweets = tweetProvider.getTweets(query, count);
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
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(ret.toString());
            } catch (JSONException ex) {
                handleBadRequest(baseRequest, request, response);
            }
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

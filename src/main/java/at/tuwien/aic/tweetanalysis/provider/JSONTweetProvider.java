/*
 */

package at.tuwien.aic.tweetanalysis.provider;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;

/**
 *
 * @author Thomas
 */
public class JSONTweetProvider implements ITweetProvider {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final JsonParser jp;
    
    /**
     * Create a simple CSV tweet provider
     * @param file path to file including filename and extension
     * @throws IOException if file is not found or no valid JSON
     */
    public JSONTweetProvider(String file) throws IOException {
        JsonFactory f = new MappingJsonFactory();
        this.jp = f.createJsonParser(new File(file));
    }

    public Future<List<Tweet>> getTweets(String searchTerm, final int count, final Date beginTime, final Date endTime) {
        Callable<List<Tweet>> task = new Callable<List<Tweet>>() {

            @Override
            public List<Tweet> call() throws IOException {
                List<Tweet> tweets = new LinkedList<>();

                //shameless stolen from ProprocessingCSVTraindata.java                
                //@TODO: also abort when end is reached
                while (tweets.size() < count) {                    
                    jp.nextToken();
                    String fieldName = jp.getCurrentName();
                    System.out.println(fieldName);
                    if (fieldName != null && fieldName.equals("text")) {
                        jp.nextToken();
                        String content = jp.getText();
                        
                        Tweet tweet = new Tweet();
                        tweet.setContent(content);
                        tweets.add(tweet);
                        //@TODO: Extract time, author and all that sweet stuff :)

                    }
                }
                
                return tweets;
            }
        };

        

        return executor.submit(task);
    }
    
}

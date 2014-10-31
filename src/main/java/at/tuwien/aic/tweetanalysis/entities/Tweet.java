package at.tuwien.aic.tweetanalysis.entities;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Thomas
 */
public class Tweet {
    private String id;
    
    private String content;
    private List<String> hashtags;
    private String author;
    
    private Date timestamp;
    
    private int retweetCount;
    private int favoriteCount;
    
    private Location location;
    
    public class Location {
        private float latitude;
        private float longitude;
    }
}

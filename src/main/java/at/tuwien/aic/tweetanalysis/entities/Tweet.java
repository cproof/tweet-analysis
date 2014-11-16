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
    private List<String> urls;
    private List<String> users;
    private String author;
    
    private Date timestamp;
    
    private int retweetCount;
    private int favoriteCount;
    
    private Location location;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the hashtags
     */
    public List<String> getHashtags() {
        return hashtags;
    }

    /**
     * @param hashtags the hashtags to set
     */
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    /**
     * @return the urls
     */
    public List<String> getUrls() {
        return urls;
    }

    /**
     * @param urls the urls to set
     */
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the retweetCount
     */
    public int getRetweetCount() {
        return retweetCount;
    }

    /**
     * @param retweetCount the retweetCount to set
     */
    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    /**
     * @return the favoriteCount
     */
    public int getFavoriteCount() {
        return favoriteCount;
    }

    /**
     * @param favoriteCount the favoriteCount to set
     */
    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the users
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(List<String> users) {
        this.users = users;
    }
    
    public class Location {
        private float latitude;
        private float longitude;
    }
    
    @Override
    public String toString() {
        return "Tweet from " + this.getAuthor() + " with content: " + this.getContent();
    }
}
